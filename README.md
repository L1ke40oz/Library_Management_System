# 图书管理系统（Library Management System）

一个前后端分离的图书管理系统课程设计项目。前端基于 Vue 3 + Vite，后端基于 Spring Boot 2 + MyBatis + MySQL，支持读者与管理员两种角色，覆盖图书检索、借阅、续借、归还、收藏、评论、站内消息以及后台图书 / 用户 / 借阅管理等功能。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vue Router 4、Axios、Vite |
| 后端 | Spring Boot 2.7、MyBatis、Lombok |
| 数据库 | MySQL 8（`library_db`，`utf8mb4`） |
| 构建 | 前端 npm / Vite，后端 Maven Wrapper |

## 目录结构

```
library-management-system/
├── frontend/                 # Vue 3 前端
│   ├── src/
│   │   ├── api/              # Axios 封装与各模块接口
│   │   ├── components/       # 通用组件
│   │   ├── views/           # 页面（首页、检索、详情、后台等）
│   │   ├── router/          # 路由配置
│   │   └── main.js
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── backend/                  # Spring Boot 后端
│   ├── src/main/java/com/library/librarymanagementsystem/
│   │   ├── controller/       # REST 控制器
│   │   ├── service/          # 业务逻辑
│   │   ├── mapper/           # MyBatis Mapper
│   │   ├── entity/           # 实体
│   │   ├── dto/              # 请求 / 响应对象
│   │   └── config/           # 跨域等配置
│   ├── src/main/resources/
│   │   ├── application.yml   # 数据源与端口配置
│   │   └── init_all.sql      # 一键初始化数据库脚本（建库 + 建表 + 示例数据）
│   ├── pom.xml
│   └── mvnw / mvnw.cmd        # Maven Wrapper
├── docs/
│   └── API.md                # 后端 REST API 文档
└── README.md
```

## 环境要求

- JDK 8+
- Maven 3.6+（或直接使用项目自带的 `mvnw`，无需本地安装 Maven）
- MySQL 8.x
- Node.js 18+（推荐 20+）与 npm

## 快速开始

下载源码后按以下三步即可在本地跑起来：先初始化数据库，再启动后端，最后启动前端。

### 1. 克隆项目

```bash
git clone <你的仓库地址>
cd library-management-system
```

### 2. 初始化数据库

确保本地 MySQL 已启动，然后执行仓库自带的一键初始化脚本。它会自动创建 `library_db` 数据库、所有数据表、默认管理员账号以及示例图书数据。

```bash
# 在项目根目录执行（会提示输入 MySQL 密码）
mysql -u root -p < backend/src/main/resources/init_all.sql
```

也可以在 MySQL 客户端（如 Navicat / DBeaver / MySQL Workbench）中打开该 SQL 文件并执行。

### 3. 启动后端

后端默认监听 **8088** 端口（8080 常被占用，故改用 8088）。

首先修改数据库连接配置：

```bash
# 编辑 backend/src/main/resources/application.yml
```

将其中的 `password` 改成你本地 MySQL 的实际密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_mysql_password   # 改成你自己的 MySQL 密码
    driver-class-name: com.mysql.cj.jdbc.Driver
server:
  port: 8088
```

然后启动：

```bash
cd backend

# Windows（PowerShell / CMD）
mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

看到 Spring Boot 启动日志且无报错，即表示后端已运行在 `http://localhost:8088`。可访问 `http://localhost:8088/api/hello` 验证。

### 4. 启动前端

新开一个终端：

```bash
cd frontend
npm install
npm run dev
```

Vite 默认在 `http://localhost:5173` 启动。前端在开发环境会把请求代理到 `http://localhost:8088/api`（见 `frontend/src/api/index.js`），因此请确保后端已先启动。

打开浏览器访问 **http://localhost:5173** 即可使用。

## 默认账号

数据库初始化脚本会自动创建一个管理员账号：

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `admin123` |

普通读者账号可通过前端「注册」页面自行创建，或用管理员账号登录后台创建。

## 生产构建

前端打包：

```bash
cd frontend
npm run build      # 产物输出到 frontend/dist
npm run preview    # 本地预览打包结果
```

后端打包为可执行 jar：

```bash
cd backend
mvnw.cmd clean package        # Windows
./mvnw clean package          # macOS / Linux
java -jar target/LibraryManagementSystem-0.0.1-SNAPSHOT.jar
```

## API 文档

后端所有 REST 接口的说明（路径、参数、请求体、响应结构、错误码）见 [docs/API.md](docs/API.md)。

## 说明与注意事项

- 本项目为课程设计 / 学习用途，**密码以明文存储、Token 为随机 UUID 占位**，未做真实的加密与鉴权，请勿用于生产环境。
- 后端跨域已在 `WebConfig` 中放开所有来源，方便本地前后端联调。
- 若 8088 端口被占用，可在 `application.yml` 中修改 `server.port`，并同步修改 `frontend/src/api/index.js` 中的开发环境 `baseURL`。
