# 环境设置指南

## 系统要求

### 硬件要求
- 内存: 最小 8GB RAM
- 磁盘空间: 最小 2GB
- 处理器: 双核以上

### 软件要求
- Java JDK 11+
- Maven 3.8+
- Git 2.20+
- Chrome/Firefox 浏览器

## 安装步骤

### 1. 安装 Java
```bash
# Ubuntu/Debian
sudo apt install openjdk-11-jdk

# macOS
brew install openjdk@11

# 验证安装
java -version
```

### 2. 安装 Maven
```bash
# Ubuntu/Debian
sudo apt install maven

# 验证安装
mvn -version
```

### 3. 克隆项目
```bash
git clone <repository-url>
cd testng-enterprise-framework
```

### 4. 配置环境
```bash
# 复制配置文件
cp config/example.properties config/dev.properties
cp config/example.properties config/qa.properties

# 编辑配置文件
# 修改 config/dev.properties 中的配置值
```

### 5. 构建项目
```bash
mvn clean install
```

## 运行测试

### 使用脚本
```bash
# 运行冒烟测试
./run-tests.sh -e qa -s smoke-test.xml

# 运行回归测试
./run-tests.sh -e qa -s regression-test.xml --parallel

# 查看帮助
./run-tests.sh --help
```

### 使用 Maven
```bash
# 运行特定套件
mvn test -DsuiteXmlFile=test-suites/smoke-test.xml

# 运行特定测试组
mvn test -Dgroups="smoke,api"
```

## IDE 配置

### IntelliJ IDEA
1. 打开项目
2. 配置 JDK 11
3. 启用注解处理
4. 安装 TestNG 插件

### Eclipse
1. 导入 Maven 项目
2. 安装 TestNG 插件
3. 配置运行配置

## 常见问题

### Java 版本问题
```
错误: 不支持的目标发行版: 11
```
解决方案: 确保使用 JDK 11

### 浏览器驱动问题
```
Unable to locate chromedriver
```
解决方案: 框架会自动下载，检查网络连接

### 内存不足
```
java.lang.OutOfMemoryError
```
解决方案:
```bash
export MAVEN_OPTS="-Xmx2g -XX:MaxPermSize=512m"
```
