#!/bin/bash
# ===========================================
# Ubuntu 服务器环境初始化脚本
# 用于 GitHub Actions Self-hosted Runner
# ===========================================

set -e

echo "🚀 开始服务器环境初始化..."

# 1. 更新系统
echo "📦 更新系统包..."
sudo apt-get update -y

# 2. 安装 Docker
echo "🐳 安装 Docker..."
if ! command -v docker &> /dev/null; then
    sudo apt-get install -y docker.io docker-compose
    sudo systemctl enable docker
    sudo systemctl start docker
    sudo usermod -aG docker $USER
    echo "✅ Docker 安装完成"
else
    echo "✅ Docker 已安装"
fi

# 3. 安装 Java 8
echo "☕ 安装 Java 8..."
if ! command -v java &> /dev/null; then
    sudo apt-get install -y openjdk-8-jdk
    echo "✅ Java 安装完成"
else
    echo "✅ Java 已安装"
fi

# 4. 安装 Maven
echo "📦 安装 Maven..."
if ! command -v mvn &> /dev/null; then
    sudo apt-get install -y maven
    echo "✅ Maven 安装完成"
else
    echo "✅ Maven 已安装"
fi

# 5. 安装 curl 和 git
echo "🔧 安装常用工具..."
sudo apt-get install -y curl git

# 6. 创建应用目录
echo "📁 创建应用目录..."
mkdir -p ~/Campfire/server/logs

# 7. 验证安装
echo ""
echo "=========================================="
echo "✅ 环境初始化完成！版本信息："
echo "=========================================="
docker --version
java -version 2>&1 | head -n 1
mvn --version | head -n 1
echo ""

echo "=========================================="
echo "📋 下一步：安装 GitHub Actions Runner"
echo "=========================================="
echo ""
echo "1. 登录 GitHub 仓库 -> Settings -> Actions -> Runners"
echo "2. 点击 'New self-hosted runner'"
echo "3. 选择 Linux x64，复制并执行显示的命令"
echo ""
echo "或者执行以下命令创建 Runner 目录："
echo "  mkdir -p ~/actions-runner && cd ~/actions-runner"
echo ""
