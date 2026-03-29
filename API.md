# 前端联调用接口文档（认证模块）

> 适用范围：登录 / 注册
>
> 说明：该文档为前端联调版本，后端可按此实现。

## 1. 基础约定

- Base URL：`/api`
- Content-Type：`application/json`
- 鉴权方式：登录成功后返回 `token`，前端后续请求放在 Header：`Authorization: Bearer <token>`
- 时间格式：ISO 8601，例如 `2026-03-29T10:00:00Z`

### 1.1 统一响应结构

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

字段说明：

- `code`: 业务状态码，`0` 表示成功，非 `0` 表示失败
- `message`: 结果描述
- `data`: 业务数据，失败时可为 `null`

### 1.2 通用错误码

| code | 含义 |
|---|---|
| 0 | 成功 |
| 40001 | 参数错误 |
| 40002 | 用户名或密码错误 |
| 40003 | 邮箱格式错误 |
| 40004 | 用户名已存在 |
| 40005 | 邮箱已注册 |
| 40100 | 未登录或 token 无效 |
| 50000 | 服务端异常 |

---

## 2. 注册

### 2.1 接口信息

- 方法：`POST`
- 路径：`/api/auth/register`

### 2.2 请求参数

```json
{
  "username": "zhangsan",
  "nickname": "张三",
  "email": "zhangsan@example.com",
  "password": "123456",
  "confirmPassword": "123456"
}
```

字段说明：

| 字段 | 类型 | 必填 | 规则 |
|---|---|---|---|
| username | string | 是 | 3-20 位，字母数字下划线 |
| nickname | string | 是 | 2-20 位 |
| email | string | 是 | 邮箱格式 |
| password | string | 是 | 6-32 位 |
| confirmPassword | string | 是 | 必须与 `password` 一致 |

### 2.3 成功响应

```json
{
  "code": 0,
  "message": "注册成功",
  "data": {
    "userId": 10001,
    "username": "zhangsan",
    "createdAt": "2026-03-29T10:00:00Z"
  }
}
```

### 2.4 失败响应示例

```json
{
  "code": 40004,
  "message": "用户名已存在",
  "data": null
}
```

---

## 3. 登录

### 3.1 接口信息

- 方法：`POST`
- 路径：`/api/auth/login`

### 3.2 请求参数

```json
{
  "username": "zhangsan",
  "email": "zhangsan@example.com",
  "password": "123456"
}
```

字段说明：

| 字段 | 类型 | 必填 | 规则 |
|---|---|---|---|
| username | string | 是 | 3-20 位 |
| email | string | 是 | 邮箱格式 |
| password | string | 是 | 6-32 位 |

### 3.3 成功响应

```json
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "token": "jwt-token-demo",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "user": {
      "userId": 10001,
      "username": "zhangsan",
      "nickname": "张三",
      "email": "zhangsan@example.com"
    }
  }
}
```

### 3.4 失败响应示例

```json
{
  "code": 40002,
  "message": "用户名或密码错误",
  "data": null
}
```

---

## 4. 前端联调建议

1. 注册成功后：提示成功并切换到登录页（已在当前前端逻辑实现）。
2. 登录成功后：
   - 保存 `token` 到 `localStorage`（或 `cookie`）
   - 设置请求拦截器自动带上 `Authorization`
   - 跳转业务首页
3. 对 `code != 0` 的响应统一弹出 `message`。

---

## 5. curl 示例

### 5.1 注册

```bash
curl -X POST 'http://localhost:3000/api/auth/register' \
  -H 'Content-Type: application/json' \
  -d '{
    "username":"zhangsan",
    "nickname":"张三",
    "email":"zhangsan@example.com",
    "password":"123456",
    "confirmPassword":"123456"
  }'
```

### 5.2 登录

```bash
curl -X POST 'http://localhost:3000/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "username":"zhangsan",
    "email":"zhangsan@example.com",
    "password":"123456"
  }'
```

