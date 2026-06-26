# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## 项目概况

智能问诊平台 — 前后端分离的医院管理系统。患者可进行AI问诊、预约挂号、查看病历、住院登记；管理员管理科室/医生/排班/公告/用户。

## 常用命令

### 后端（Spring Boot + Maven，端口 8123）

```bash
mvnw spring-boot:run          # 启动开发服务器
mvnw test                      # 运行所有测试
mvnw test -Dtest=XxxTest       # 运行单个测试类
mvnw clean package             # 打包
```

Swagger UI 启动后访问：`http://localhost:8123/swagger-ui/index.html`

### 前端（Vue 3 + Vite，端口 3000）

```bash
cd front
npm install                    # 安装依赖
npm run dev                    # 启动开发服务器（自动代理API到 8123）
npm run build                  # 生产构建
npm run preview                # 预览生产构建
```

## 架构概要

### 后端分层 (`src/main/java/com/backend/`)

```
controller/     # REST 控制器，HTTP 入口
service/        # 接口（在 service/ 下） + 实现（在 service/impl/ 下，继承 MyBatis-Flex ServiceImpl）
mapper/         # MyBatis-Flex BaseMapper，对应每张数据库表
model/entity/   # 数据库实体（Lombok @Data + @Builder + MyBatis-Flex @Id 雪花ID）
model/dto/      # 请求/响应 DTO
config/         # Spring 配置（CORS 全局放行、TokenInterceptor 注册、DeepSeek API 属性）
interceptor/    # JWT Token 验证拦截器
common/         # BaseResponse 统一响应、ResultUtils 工厂、UserContext（ThreadLocal）
exception/      # BusinessException + ErrorCode 枚举 + GlobalExceptionHandler
utils/          # JwtUtils、PasswordUtils（BCrypt）
```

Service 层惯例：接口定义在 `service/` 下，实现类继承 `com.mybatisflex.spring.service.impl.ServiceImpl<M, T>` 并放在 `service/impl/` 下。例外：`AiConsultationService` 和 `DeepSeekService` 是直接 `@Service` 注解的具体类，没有接口。

### 前端分层 (`front/src/`)

```
views/              # 按功能分组的页面组件
  aiConsultation/   # AI问诊（首页、聊天、历史、详情）
  appoint/          # 预约相关（挂号、患者管理、就诊、住院）
  manage/           # 信息管理（公告、科室、排班）
  user/             # 用户管理（管理员、医生、用户、账户）
  App.vue           # 根组件：未登录=登录/注册卡片；已登录=侧边栏布局+router-view
components/         # 可复用组件（仅 aiConsultation 子组件）
router/index.js     # 18 条路由，beforeEach 守卫做认证+角色鉴权
utils/
  request.js        # 基于原生 fetch 封装的 HTTP 客户端（非 axios）
  api.js            # 集中式 API 服务对象（10 组资源，~40 个方法）
  session.js        # localStorage 会话管理（get/set/clear/expire）
```

**前端没有 Pinia/Vuex、没有 TypeScript、没有 axios**。状态通过 `localStorage`（`demo_session` key）和组件本地 `ref()/reactive()` 管理。所有页面共用同一个 `request.js` 发请求，Vite 在开发模式下将 `/auth`、`/departments` 等路径代理到 `http://localhost:8123`。

### 数据库

MySQL，数据库名 `intelligent-consultation-platform`。SQL 建库脚本在 `sql/database.sql`，包含 12 张表及种子数据。注意：SQL 脚本中密码使用 MD5 hash，但 Java 代码使用 BCrypt，种子数据账户将无法直接登录。

### 认证流程

1. `POST /auth/login` 验证密码（BCrypt），返回 JWT token
2. `TokenInterceptor` 从 `Authorization: Bearer <token>` 头提取并验证 JWT，将 userId/username/role 存入 `UserContext`（ThreadLocal）
3. 白名单路径（`/auth/register`、`/auth/login`、Swagger 路径）不拦截
4. 前端路由守卫读取 `localStorage.demo_session` 做认证+角色检查

### AI 问诊

`AiConsultationService` 使用内存 `ConcurrentHashMap` 存储会话（不持久化到数据库）。`DeepSeekService` 通过 WebFlux `WebClient` 调用 DeepSeek API（`https://api.deepseek.com/chat/completions`），配置键前缀 `deepseek.*`。前端与 `/api/ai-consultation` 端点交互。

## 关键注意事项

- **两个 `notice` 模块重复**：`HELP.md` 中列出了 `/notices`（管理端公告CRUD）和 `/notice`（对外公告列表），但后端 controller 中只有 `NoticesController`（映射 `/notices`）
- **MyBatis-Flex 代码生成器**：`com.backend.generator.MyBatisCodeGenerator` 可直接生成实体/Mapper/Service/Controller
- **Snowflake ID**：所有实体主键使用 `KeyGenerators.snowFlakeId`，类型为 `Long`
- **预约状态流转**：`pending → confirmed → processing → completed`（或 `cancelled`）。其中 `processing` 由医生创建问诊记录时自动设置
- **前端 `App.vue` 的双重角色**：同时处理未登录状态（登录/注册卡片）和已登录状态（主布局），这一点在修改认证 UI 时需要特别注意
