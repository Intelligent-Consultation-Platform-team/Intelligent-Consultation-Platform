\# 后端接口文档（认证模块）



\## 1. 基础约定



\- \*\*Base URL\*\*：``

\- \*\*Content-Type\*\*：`application/json`

\- \*\*鉴权方式\*\*：登录成功后返回 `token`，前端后续请求放在 Header：`Authorization: Bearer <token>`

\- \*\*时间格式\*\*：ISO 8601，例如 `2026-03-29T10:00:00Z`



\### 1.1 统一响应结构



```json

{

&#x20; "code": 0,

&#x20; "message": "ok",

&#x20; "data": {}

}

```



字段说明：

\- `code`: 业务状态码，`0` 表示成功，非 `0` 表示失败

\- `message`: 结果描述

\- `data`: 业务数据，失败时可为 `null`



\### 1.2 通用错误码



| code | 含义 |

|------|------|

| 0 | 成功 |

| 40001 | 参数错误 |

| 40002 | 用户名或密码错误 |

| 40003 | 邮箱格式错误 |

| 40004 | 用户名已存在 |

| 40005 | 邮箱已注册 |

| 40100 | 未登录或 token 无效 |

| 50000 | 服务端异常 |



\## 2. 注册接口



\### 2.1 接口信息



\- \*\*方法\*\*：`POST`

\- \*\*路径\*\*：`/auth/register`



\### 2.2 请求参数



```json

{

&#x20; "username": "zhangsan",

&#x20; "nickname": "张三",

&#x20; "email": "zhangsan@example.com",

&#x20; "password": "123456",

&#x20; "confirmPassword": "123456",

&#x20; "role": "patient"

}

```



字段说明：

| 字段 | 类型 | 必填 | 规则 |

|------|------|------|------|

| username | string | 是 | 3-20 位，字母数字下划线 |

| nickname | string | 是 | 2-20 位 |

| email | string | 是 | 邮箱格式 |

| password | string | 是 | 6-32 位 |

| confirmPassword | string | 是 | 必须与 `password` 一致 |

| role | string | 是 | 枚举值：`admin`（管理员）、`doctor`（医生）、`patient`（患者） |



\### 2.3 成功响应



```json

{

&#x20; "code": 0,

&#x20; "message": "注册成功",

&#x20; "data": {

&#x20;   "userId": 10001,

&#x20;   "username": "zhangsan",

&#x20;   "role": "patient",

&#x20;   "createdAt": "2026-03-29T10:00:00Z"

&#x20; }

}

```



\### 2.4 失败响应示例



```json

{

&#x20; "code": 40004,

&#x20; "message": "用户名已存在",

&#x20; "data": null

}

```



\## 3. 登录接口



\### 3.1 接口信息



\- \*\*方法\*\*：`POST`

\- \*\*路径\*\*：`/auth/login`



\### 3.2 请求参数



```json

{

&#x20; "username": "zhangsan",

&#x20; "email": "zhangsan@example.com",

&#x20; "password": "123456"

}

```



字段说明：

| 字段 | 类型 | 必填 | 规则 |

|------|------|------|------|

| username | string | 是 | 3-20 位 |

| email | string | 是 | 邮箱格式 |

| password | string | 是 | 6-32 位 |



\### 3.3 成功响应



```json

{

&#x20; "code": 0,

&#x20; "message": "登录成功",

&#x20; "data": {

&#x20;   "token": "jwt-token-demo",

&#x20;   "tokenType": "Bearer",

&#x20;   "expiresIn": 7200,

&#x20;   "user": {

&#x20;     "userId": 10001,

&#x20;     "username": "zhangsan",

&#x20;     "nickname": "张三",

&#x20;     "email": "zhangsan@example.com",

&#x20;     "role": "patient"

&#x20;   }

&#x20; }

}

```



\### 3.4 失败响应示例



```json

{

&#x20; "code": 40002,

&#x20; "message": "用户名或密码错误",

&#x20; "data": null

}

```



\## 4. 实现细节



\### 4.1 技术栈



\- \*\*框架\*\*：Spring Boot 3.5.13

\- \*\*数据库\*\*：MySQL

\- \*\*ORM\*\*：MyBatis-Flex

\- \*\*认证\*\*：JWT

\- \*\*密码加密\*\*：BCrypt



\### 4.2 核心功能



1\. \*\*注册流程\*\*：

&#x20;  - 参数校验

&#x20;  - 用户名和邮箱重复性检查

&#x20;  - 密码加密

&#x20;  - 用户信息保存



2\. \*\*登录流程\*\*：

&#x20;  - 参数校验

&#x20;  - 用户名和邮箱联合验证

&#x20;  - 密码验证

&#x20;  - JWT token 生成



\### 4.3 接口安全



\- 密码使用 BCrypt 加密存储

\- 使用 JWT 进行无状态认证

\- 接口参数进行严格验证



\## 5. 测试示例



\### 5.1 注册测试



```bash

curl -X POST 'http://localhost:3000/auth/register' \\

&#x20; -H 'Content-Type: application/json' \\

&#x20; -d '{

&#x20;   "username":"zhangsan",

&#x20;   "nickname":"张三",

&#x20;   "email":"zhangsan@example.com",

&#x20;   "password":"123456",

&#x20;   "confirmPassword":"123456",

&#x20;   "role":"patient"

&#x20; }'

```



\### 5.2 登录测试



```bash

curl -X POST 'http://localhost:3000/auth/login' \\

&#x20; -H 'Content-Type: application/json' \\

&#x20; -d '{

&#x20;   "username":"zhangsan",

&#x20;   "email":"zhangsan@example.com",

&#x20;   "password":"123456"

&#x20; }'

```



\## 6. 部署说明



\- \*\*服务端口\*\*：3000

\- \*\*数据库配置\*\*：在 `application.yml` 中配置

\- \*\*依赖管理\*\*：使用 Maven 管理

\- \*\*启动命令\*\*：`java -jar backend-0.0.1-SNAPSHOT.jar`



\## 7. 注意事项



\- 接口返回的 `createdAt` 字段为 ISO 8601 格式

\- 登录成功后，前端需要将 `token` 保存到 `localStorage` 或 `cookie`

\- 后续请求需要在 Header 中添加 `Authorization: Bearer <token>`

\- 对于 `code != 0` 的响应，前端需要统一处理错误信息

