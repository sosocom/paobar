#!/bin/bash

###############################################################################
# Guitar Tab System - CentOS Selenium 环境准备脚本
# 
# 功能：
# 1. 安装 Google Chrome (用于 Selenium)
# 2. 安装 Chrome 无头模式依赖
# 3. 安装中文字体（正确渲染中文网页）
# 4. 配置 Selenium 环境
#
# 前提条件：
# - 系统已安装 Java 17+
# - 系统已安装 Maven (可选)
#
# 使用方法：
#   chmod +x setup-centos.sh
#   sudo ./setup-centos.sh
###############################################################################

set -e  # 遇到错误立即退出

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否为 root 用户
check_root() {
    if [ "$EUID" -ne 0 ]; then 
        log_error "请使用 root 权限运行此脚本 (sudo ./setup-centos.sh)"
        exit 1
    fi
}

# 检查 Java 环境
check_java() {
    log_info "检查 Java 环境..."
    
    if ! command -v java &> /dev/null; then
        log_error "未检测到 Java，请先安装 Java 17+"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        log_error "Java 版本过低 ($JAVA_VERSION)，需要 Java 17+"
        exit 1
    fi
    
    log_info "Java 版本检查通过: $JAVA_VERSION"
    java -version
}

# 安装基础依赖
install_dependencies() {
    log_info "安装基础依赖..."
    yum install -y wget curl unzip
    log_info "基础依赖安装完成"
}

# 安装 Google Chrome
install_chrome() {
    log_info "检查 Google Chrome 安装..."
    
    if command -v google-chrome &> /dev/null || command -v google-chrome-stable &> /dev/null; then
        log_info "Google Chrome 已安装"
        google-chrome --version 2>/dev/null || google-chrome-stable --version
        return
    fi
    
    log_info "安装 Google Chrome..."
    
    # 创建 Chrome 仓库配置
    cat > /etc/yum.repos.d/google-chrome.repo <<EOF
[google-chrome]
name=google-chrome
baseurl=http://dl.google.com/linux/chrome/rpm/stable/x86_64
enabled=1
gpgcheck=1
gpgkey=https://dl.google.com/linux/linux_signing_key.pub
EOF
    
    # 安装 Chrome
    yum install -y google-chrome-stable
    
    log_info "Google Chrome 安装完成"
    google-chrome-stable --version
}

# 安装 Chrome 无头模式依赖
install_chrome_headless_deps() {
    log_info "安装 Chrome 无头模式依赖..."
    
    yum install -y \
        xorg-x11-server-Xvfb \
        gtk3 \
        dbus-glib \
        libXScrnSaver \
        alsa-lib \
        nss
    
    log_info "Chrome 无头模式依赖安装完成"
}

# 安装中文字体
install_chinese_fonts() {
    log_info "安装中文字体..."
    
    yum install -y \
        wqy-microhei-fonts \
        wqy-zenhei-fonts
    
    # 刷新字体缓存
    fc-cache -fv
    
    log_info "中文字体安装完成"
}

# 测试 Chrome 无头模式
test_chrome_headless() {
    log_info "测试 Chrome 无头模式..."
    
    if google-chrome-stable --headless --disable-gpu --no-sandbox --dump-dom https://www.baidu.com > /dev/null 2>&1; then
        log_info "✓ Chrome 无头模式测试成功"
    else
        log_warn "Chrome 无头模式测试失败，但这不影响继续部署"
    fi
}

# 显示安装总结
show_summary() {
    echo ""
    log_info "========================================"
    log_info "  Selenium 环境准备完成！"
    log_info "========================================"
    echo ""
    log_info "已安装组件："
    echo "  ✓ Google Chrome"
    echo "  ✓ Chrome 无头模式依赖"
    echo "  ✓ 中文字体"
    echo ""
    log_info "下一步操作："
    echo "  1. 上传项目 JAR 包"
    echo "  2. 配置 application.yml"
    echo "  3. 运行应用: java -jar guitar-tab-system-1.0.0.jar"
    echo ""
    log_info "重要提示："
    echo "  - ChromeDriver 由 WebDriverManager 自动管理"
    echo "  - 应用会在首次启动时自动下载匹配的 ChromeDriver"
    echo "  - 无需手动配置 ChromeDriver"
    echo ""
}

# 主函数
main() {
    log_info "开始配置 Selenium 环境..."
    echo ""
    
    check_root
    check_java
    
    install_dependencies
    install_chrome
    install_chrome_headless_deps
    install_chinese_fonts
    test_chrome_headless
    
    show_summary
    
    log_info "脚本执行完成！"
}

# 执行主函数
main
