#!/bin/bash

# 配置信息
pass="Jack6585009"
ip="8.163.10.6"
port="22"  # SSH 端口
app_port="8001"  # 应用端口
deploy_path="/opt/nginx/html/paobar"

echo "========================================="
echo "  开始部署 Paobar 前端"
echo "  服务器: $ip"
echo "  部署路径: $deploy_path"
echo "========================================="

# 1. 构建前端
echo ""
echo "[1/4] 构建前端..."
npx vite build

if [ $? -ne 0 ]; then
    echo "❌ 构建失败！"
    exit 1
fi
echo "✓ 构建完成"

# 2. 打包构建产物
echo ""
echo "[2/4] 打包文件..."
tar -czf dist.tar.gz dist/
echo "✓ 打包完成: dist.tar.gz"

# 3. 上传到服务器
echo ""
echo "[3/4] 上传到服务器..."
sshpass -p "$pass" scp -P "$port" dist.tar.gz "root@$ip:/tmp/"

if [ $? -ne 0 ]; then
    echo "❌ 上传失败！"
    exit 1
fi
echo "✓ 上传完成"

# 4. 在服务器上部署
echo ""
echo "[4/4] 部署到目标目录..."
sshpass -p "$pass" ssh -p "$port" "root@$ip" << 'EOF'
cd /tmp
if [ -f dist.tar.gz ]; then
    # 备份旧版本
    if [ -d /opt/nginx/html/paobar/dist ]; then
        echo "  备份旧版本..."
        mv /opt/nginx/html/paobar/dist /opt/nginx/html/paobar/dist.bak.$(date +%Y%m%d_%H%M%S)
    fi
    
    # 解压新版本
    echo "  解压新版本..."
    mkdir -p /opt/nginx/html/paobar
    tar -xzf dist.tar.gz -C /opt/nginx/html/paobar/
    
    # 设置权限（这台机器 nginx worker 跑的是 nobody 用户）
    chown -R nobody:nobody /opt/nginx/html/paobar/dist 2>/dev/null || true

    # 重启 nginx 清除缓存（源码编译安装，没走 systemd）
    echo "  reload nginx..."
    /opt/nginx/sbin/nginx -t && /opt/nginx/sbin/nginx -s reload

    # 清理临时文件
    rm -f /tmp/dist.tar.gz

    # 只保留最近 3 份备份，防止盘越堆越大
    ls -1dt /opt/nginx/html/paobar/dist.bak.* 2>/dev/null | tail -n +4 | xargs -r rm -rf

    echo "  ✓ 部署完成"
else
    echo "  ❌ 未找到上传的文件"
    exit 1
fi
EOF

if [ $? -ne 0 ]; then
    echo "❌ 部署失败！"
    exit 1
fi

# 清理本地临时文件
rm -f dist.tar.gz

echo ""
echo "========================================="
echo "  ✅ 部署成功！"
echo "  访问地址: https://paobar.com"
echo "========================================="
