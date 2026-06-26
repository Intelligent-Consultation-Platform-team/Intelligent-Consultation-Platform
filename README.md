# 智能问诊平台

## 项目简介

智能问诊平台是一个基于 Spring Boot + Vue 3 的医疗服务管理系统，集成 DeepSeek AI 提供智能问诊功能，支持患者预约挂号、医生接诊、住院管理等完整医疗服务流程。

## 技术栈

### 后端 (`based/`)
| 组件 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.5.13 |
| ORM | MyBatis Flex | 1.11.0 |
| 数据库 | MySQL | 8.0+ |
| 认证 | JWT | 0.11.5 |
| AI集成 | DeepSeek API | - |
| API文档 | SpringDoc OpenAPI | 2.8.5 |

### 前端 (`fronted/`)
| 组件 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue | 3.4.27 |
| UI组件 | Element Plus | 2.7.6 |
| 路由 | Vue Router | 4.6.4 |
| 构建工具 | Vite | 5.4.21 |

## 项目结构

```
ICP/
├── based/                        # 后端主项目
│   ├── src/main/java/com/backend/
│   │   ├── controller/           # REST API 控制器
│   │   ├── service/              # 业务逻辑层
│   │   ├── mapper/               # 数据访问层
│   │   ├── entity/               # 数据库实体
│   │   ├── dto/                  # 数据传输对象
│   │   ├── config/               # 配置类
│   │   ├── interceptor/          # 拦截器（JWT验证）
│   │   ├── common/               # 公共工具类
│   │   └── utils/                # 工具函数
│   ├── src/main/resources/
│   │   ├── application.yml       # 应用配置
│   │   └── schema.sql            # 数据库初始化脚本
│   ├── sql/                      # 数据库脚本
│   └── pom.xml                   # Maven 配置
├── fronted/                      # 前端主项目
│   ├── src/
│   │   ├── views/                # 页面组件
│   │   ├── components/           # 可复用组件
│   │   ├── router/               # 路由配置
│   │   ├── api/                  # API 封装
│   │   └── utils/                # 工具函数
│   ├── .env                      # 环境变量
│   ├── vite.config.js            # Vite 配置
│   └── package.json              # 依赖配置
└── README.md                     # 项目说明
```

## 功能特性

### 核心功能
- ✅ **AI问诊**：智能对话、风险评估、健康建议
- ✅ **预约挂号**：科室选择、医生排班、在线预约
- ✅ **就诊管理**：医生接诊、病历记录、费用结算
- ✅ **住院管理**：住院登记、出院办理、费用统计

### 管理功能
- ✅ **科室管理**：科室信息维护
- ✅ **医生管理**：医生信息维护
- ✅ **排班管理**：医生出诊时间安排
- ✅ **公告管理**：系统公告发布
- ✅ **用户管理**：多角色用户管理

### 用户功能
- ✅ **账户充值**：在线充值
- ✅ **费用缴纳**：诊疗费用支付
- ✅ **个人中心**：个人信息管理

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+

### 1. 数据库配置

创建数据库并导入初始化脚本：

```sql
CREATE DATABASE icp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE icp_db;
SOURCE based/sql/schema.sql;
```

修改后端配置文件 `based/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/icp_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### 2. AI 配置（可选）

如需使用 AI 问诊功能，配置 DeepSeek API Key：

```yaml
deepseek:
  api-key: your_deepseek_api_key
```

### 3. 启动后端服务

```bash
cd based
mvn spring-boot:run
```

服务启动后访问：
- API 文档：http://localhost:8123/swagger-ui/index.html

### 4. 启动前端服务

```bash
cd fronted
npm install
npm run dev
```

服务启动后访问：http://localhost:3000

## API 接口

### 认证接口
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /auth/register | 用户注册 |
| POST | /auth/login | 用户登录 |
| GET | /auth/me | 获取当前用户 |
| PUT | /auth/profile | 更新个人信息 |

### AI问诊接口
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/ai-consultation/session | 创建问诊会话 |
| POST | /api/ai-consultation/message | 发送消息 |
| GET | /api/ai-consultation/session/{id} | 获取会话详情 |
| POST | /api/ai-consultation/session/{id}/close | 关闭会话 |

### 预约挂号接口
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /appointment | 创建预约 |
| GET | /appointment | 分页查询预约 |
| PUT | /appointment/{id}/cancel | 取消预约 |
| PUT | /appointment/{id}/process | 医生接诊 |

### 完整 API 文档

访问 Swagger UI：http://localhost:8123/swagger-ui/index.html

## 角色权限

### 患者角色
- 系统首页
- AI问诊
- 预约挂号
- 我的流程
- 缴纳费用
- 个人信息
- 账户充值

### 医生角色
- 系统首页
- AI问诊
- 患者挂号
- 患者就诊
- 住院登记
- 个人信息

### 管理员角色
- 系统首页
- AI问诊
- 信息管理（公告、科室、排班）
- 预约就诊（患者挂号、住院登记）
- 用户管理（管理员、医生、用户）

## 注意事项

1. **数据库密码**：SQL 脚本使用 MD5 哈希存储密码，Java 代码使用 BCrypt 验证，首次使用需通过注册创建账户
2. **AI功能**：需要配置 DeepSeek API Key 才能使用 AI 问诊功能
3. **端口配置**：后端默认端口 8123，前端默认端口 3000
4. **跨域处理**：Vite 已配置代理，开发环境下 API 请求自动转发到后端

## 许可证

MIT License

## 联系方式

如有问题，请联系项目开发者。