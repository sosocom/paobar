# 后端开发指南

## 开发环境配置

### 必需软件
- JDK 17 或更高版本
- Maven 3.6+
- MySQL 8.0+
- IDE: IntelliJ IDEA (推荐) 或 Eclipse

### IDEA 配置

1. 打开 IDEA，选择 File -> Open，选择 backend 目录
2. 等待 Maven 自动导入依赖
3. 安装 Lombok 插件：File -> Settings -> Plugins -> 搜索 "Lombok" -> Install
4. 启用 Annotation Processing：File -> Settings -> Build, Execution, Deployment -> Compiler -> Annotation Processors -> Enable annotation processing

## 数据库配置

### 1. 创建数据库

```bash
mysql -u root -p
```

```sql
CREATE DATABASE guitar_tab DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 执行初始化脚本

```bash
mysql -u root -p guitar_tab < ../database/schema.sql
```

### 3. 修改配置文件

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/guitar_tab?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root          # 修改为你的数据库用户名
    password: root          # 修改为你的数据库密码
```

## 运行项目

### 命令行方式

```bash
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run

# 打包项目
mvn clean package -DskipTests
```

### IDEA 方式

1. 找到 `GuitarTabApplication.java` 文件
2. 右键点击 -> Run 'GuitarTabApplication'
3. 或者点击类名左侧的绿色运行按钮

启动成功后访问：`http://localhost:8080`

## 项目结构说明

```
backend/src/main/java/com/guitar/
├── config/                     # 配置类
│   └── WebConfig.java         # Web 配置（CORS）
├── controller/                 # 控制器层
│   ├── GuitarTabController.java   # 吉他谱接口
│   └── CrawlerController.java     # 爬虫接口
├── service/                    # 服务层
│   ├── GuitarTabService.java
│   ├── CrawlerService.java
│   └── impl/                  # 服务实现类
├── mapper/                     # 数据访问层
│   ├── GuitarTabMapper.java
│   ├── CrawlerTaskMapper.java
│   └── TagMapper.java
├── entity/                     # 实体类
│   ├── GuitarTab.java
│   ├── CrawlerTask.java
│   └── Tag.java
├── crawler/                    # 爬虫模块
│   └── YopuCrawler.java       # Yopu 网站爬虫
├── common/                     # 公共类
│   └── Result.java            # 统一响应结果
└── GuitarTabApplication.java   # 启动类
```

## API 测试

### 使用 cURL 测试

```bash
# 查询吉他谱列表
curl http://localhost:8080/api/tabs/page?pageNum=1&pageSize=10

# 爬取吉他谱
curl -X POST http://localhost:8080/api/crawler/crawl \
  -H "Content-Type: application/json" \
  -d '{"url":"https://yopu.co/view/xXAxzLL1"}'

# 查询详情
curl http://localhost:8080/api/tabs/1
```

### 使用 Postman

1. 导入 API 集合
2. 设置环境变量 `baseUrl=http://localhost:8080`
3. 测试各个接口

## 常见问题

### 1. 数据库连接失败

**问题**：`Communications link failure`

**解决**：
- 检查 MySQL 服务是否启动
- 确认数据库名称、用户名、密码是否正确
- 确认 MySQL 端口（默认 3306）

### 2. Lombok 注解不生效

**问题**：找不到 getter/setter 方法

**解决**：
- 安装 Lombok 插件
- 启用 Annotation Processing
- 重启 IDEA

### 3. 爬虫超时

**问题**：爬取失败，提示 timeout

**解决**：
- 检查网络连接
- 增加 `application.yml` 中的 `crawler.timeout` 值
- 检查目标网站是否可访问

### 4. 端口被占用

**问题**：`Port 8080 was already in use`

**解决**：
```bash
# 查找占用端口的进程
lsof -i :8080

# 杀死进程
kill -9 <PID>

# 或修改 application.yml 中的端口号
server:
  port: 8081
```

## 开发建议

### 代码规范

1. 使用 Lombok 简化代码
2. 遵循 RESTful API 设计规范
3. 添加适当的日志输出
4. 统一异常处理
5. 编写单元测试

### Git 提交规范

```
feat: 新功能
fix: 修复 bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
test: 测试
chore: 构建配置
```

## 扩展功能建议

1. **用户认证**：添加 Spring Security
2. **缓存支持**：集成 Redis
3. **搜索优化**：集成 Elasticsearch
4. **文件上传**：支持用户上传吉他谱
5. **定时任务**：定期爬取最新吉他谱
6. **API 文档**：集成 Swagger/Knife4j

## 性能优化

1. 使用连接池优化数据库连接
2. 添加缓存减少数据库查询
3. 异步处理爬虫任务
4. 分页查询优化
5. SQL 查询优化

## 调试技巧

### 1. 查看 SQL 日志

配置文件已启用 SQL 日志输出：

```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

### 2. 远程调试

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

然后在 IDEA 中配置 Remote Debug，连接到 5005 端口。

## 联系方式

如有问题，请提交 Issue 或联系项目维护者。
