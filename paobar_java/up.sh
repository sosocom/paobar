#!/bin/bash
pass="Jack6585009"
ip="8.163.10.6"
port="8001"

# 部署路径
deploy_path="/root/deploy"
log_path="/tmp/logs"
jar_name="paobar.jar"

mvn clean install -DskipTests
echo "上传 paobar JAR 并重启：=====$ip:$port"

# 确保部署目录存在
sshpass -p "$pass" ssh "root@$ip" "mkdir -p $deploy_path"

# 加了新的外部依赖，就要更新lib
echo "开始上传lib"
sshpass -p "$pass" ssh "root@$ip" "rm -Rf $deploy_path/lib"
sshpass -p "$pass" scp -r "target/lib" "root@$ip:$deploy_path/lib"

echo "开始上传jar"
sshpass -p "$pass" ssh "root@$ip" "rm -f $deploy_path/$jar_name"
sshpass -p "$pass" scp "target/$jar_name" "root@$ip:$deploy_path/$jar_name"
echo "上传完成"

echo "开始启动..."
sshpass -p "$pass" ssh "root@$ip" "pkill -9 -f $jar_name"
sleep 2

# 清空旧日志
sshpass -p "$pass" ssh "root@$ip" "mkdir -p $log_path && echo '' > $log_path/paobar.log"

# 启动应用，输出到日志文件（指定依赖库路径）
sshpass -p "$pass" ssh "root@$ip" "cd $deploy_path && /opt/jdk/jdk21/bin/java -Dloader.path=lib -jar $jar_name > $log_path/paobar.log 2>&1 &"
sleep 3
echo "启动完成，查看日志..."

sshpass -p "$pass" ssh "root@$ip" "tail -f $log_path/paobar.log"

echo "DONE"



