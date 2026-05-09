#!/bin/bash
# =============================================================================
# paobar_java 后端部署脚本
#
#   1. 本地 mvn package 构建
#   2. 上传 target/paobar.jar + target/lib 到服务器
#   3. graceful 停掉旧进程（TERM → 等 10s → KILL 兜底）
#   4. nohup 启动新进程
#   5. 健康检查：循环访问 /api/ping 直到 200，或 45s 超时
#   6. 打印最近 40 行日志，脚本返回
# =============================================================================

set -eo pipefail

# ---------- 配置 ----------
pass="Jack6585009"
ip="8.163.10.6"
port="22"
app_port="8001"
deploy_path="/root/deploy"
log_path="/tmp/logs"
jar_name="paobar.jar"
java_bin="/opt/jdk/jdk21/bin/java"
# Spring Boot 2.4+ loader 用这个路径加载外部依赖
jvm_opts="-Dloader.path=lib -Xms512m -Xmx1024m -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"
# 健康检查端点（后端自身提供；如无，则改为 /api 并接受任意 2xx/4xx）
health_url="http://127.0.0.1:${app_port}/api"
health_timeout=45   # 秒

# ---------- 工具函数 ----------
ssh_run() {
    sshpass -p "$pass" ssh -o StrictHostKeyChecking=no -o LogLevel=ERROR -p "$port" "root@$ip" "$@"
}
scp_up() {
    sshpass -p "$pass" scp -o StrictHostKeyChecking=no -o LogLevel=ERROR -P "$port" "$@"
}

log() { echo -e "\033[1;32m[$(date +%H:%M:%S)]\033[0m $*"; }
err() { echo -e "\033[1;31m[ERROR]\033[0m $*" >&2; }

# ---------- 1. 构建 ----------
log "========================================="
log "  开始部署 Paobar 后端"
log "  服务器: $ip:$app_port"
log "  部署路径: $deploy_path"
log "========================================="

log "[1/5] Maven 构建（跳过测试）..."
mvn -q clean package -DskipTests

if [ ! -f "target/${jar_name}" ] || [ ! -d "target/lib" ]; then
    err "构建产物缺失：target/${jar_name} 或 target/lib 不存在"
    exit 1
fi
log "✓ 构建完成：$(ls -lh target/${jar_name} | awk '{print $5}') + lib/ $(ls target/lib | wc -l | tr -d ' ') 个依赖"

# ---------- 2. 上传 ----------
log "[2/5] 上传 JAR + lib ..."
ssh_run "mkdir -p $deploy_path $log_path"

# lib 里依赖很多，按差异同步：先打包上去再解压，比 scp -r 快得多
# COPYFILE_DISABLE / --no-xattrs：阻断 macOS 把 ._xxx 资源叉文件塞进 tar 包
COPYFILE_DISABLE=1 tar --no-xattrs -czf target/lib.tar.gz -C target lib
scp_up "target/lib.tar.gz" "root@${ip}:${deploy_path}/lib.tar.gz"
scp_up "target/${jar_name}" "root@${ip}:${deploy_path}/${jar_name}.new"
ssh_run "set -e; cd $deploy_path && rm -rf lib && tar -xzf lib.tar.gz && rm -f lib.tar.gz ._lib ._*.jar lib/._* 2>/dev/null || true"
rm -f target/lib.tar.gz
log "✓ 上传完成"

# ---------- 3. 优雅停止旧进程 ----------
# pgrep pattern 只认 "-jar paobar.jar"（或 "-jar /abs/paobar.jar"）——
# 这样不会误伤 ssh 会话里的 bash（后者的命令行里虽然也含 java/paobar.jar 字样，
# 但没有 "-jar " 前缀）。
log "[3/5] 停止旧进程..."
ssh_run "
pattern='\-jar [^ ]*${jar_name}\$|\-jar [^ ]*${jar_name} '
pid=\$(pgrep -f \"\$pattern\" | head -1)
if [ -n \"\$pid\" ]; then
    echo \"  发现旧进程 PID=\$pid，发送 TERM 信号...\"
    kill -15 \$pid || true
    for i in \$(seq 1 10); do
        if kill -0 \$pid 2>/dev/null; then
            sleep 1
        else
            echo \"  已优雅退出（用时 \${i}s）\"
            break
        fi
    done
    if kill -0 \$pid 2>/dev/null; then
        echo \"  10s 内未退出，强制 KILL\"
        kill -9 \$pid || true
        sleep 1
    fi
else
    echo \"  没有在跑的旧进程\"
fi
"

# ---------- 4. 启动新进程 ----------
log "[4/5] 启动新进程..."
ssh_run "
set -e
cd $deploy_path
mv -f ${jar_name}.new ${jar_name}
# 归档旧日志（按日期+时间），保留最近 5 份
if [ -f $log_path/paobar.log ]; then
    mv $log_path/paobar.log $log_path/paobar.log.\$(date +%Y%m%d_%H%M%S)
fi
ls -1t $log_path/paobar.log.* 2>/dev/null | tail -n +6 | xargs -r rm -f

# nohup 配合 < /dev/null 脱离 ssh 会话；new session 防止 ssh 断开时 HUP
setsid nohup $java_bin $jvm_opts -jar ${jar_name} > $log_path/paobar.log 2>&1 < /dev/null &
echo \"  启动命令已发出，pid=\$(pgrep -f 'java.*${jar_name}' | head -1)\"
"

# ---------- 5. 健康检查 ----------
log "[5/5] 健康检查 ($health_url)，最多等 ${health_timeout}s ..."
ok=0
start_ts=$(date +%s)
while true; do
    now=$(date +%s)
    elapsed=$((now - start_ts))
    if [ $elapsed -ge $health_timeout ]; then
        break
    fi

    # 服务器内部 curl：只要端口起来就算基础可用（404/405 也接受）
    status=$(ssh_run "curl -s -o /dev/null -w '%{http_code}' --max-time 3 $health_url || echo 000")
    if [ "$status" != "000" ]; then
        log "  ✓ 后端已起来，HTTP $status（${elapsed}s）"
        ok=1
        break
    fi
    printf "."
    sleep 2
done
echo

if [ $ok -eq 0 ]; then
    err "后端启动超时（${health_timeout}s 内未响应），最近日志："
    ssh_run "tail -n 80 $log_path/paobar.log"
    exit 1
fi

# ---------- 收尾 ----------
log "========================================="
log "  ✅ 部署成功！"
log "  外部访问: https://paobar.com/api/..."
log "  查看日志: ssh root@${ip} 'tail -f $log_path/paobar.log'"
log "========================================="
echo
log "最近 30 行日志："
ssh_run "tail -n 30 $log_path/paobar.log"
