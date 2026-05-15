# 前端联调用接口文档（认证模块）

> 适用范围：登录 / 注册
>
> 说明：该文档为前端联调版本，后端可按此实现。

## 1. 基础约定

- Base URL：``
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
- 路径：`/auth/register`

### 2.2 请求参数

```json
{
  "username": "zhangsan",
  "realName": "张三",
  "phone": "13800138001",
  "email": "zhangsan@example.com",
  "password": "123456",
  "confirmPassword": "123456",
  "role": "patient"
}
```

字段说明：

| 字段 | 类型 | 必填 | 规则 |
|---|---|---|---|
| username | string | 是 | 3-20 位，字母数字下划线 |
| realName | string | 是 | 2-20 位，真实姓名 |
| phone | string | 是 | 11 位手机号 |
| email | string | 是 | 邮箱格式 |
| password | string | 是 | 6-32 位 |
| confirmPassword | string | 是 | 必须与 `password` 一致 |
| role | string | 是 | 枚举值：`patient`（患者）、`doctor`（医生） |

### 2.3 成功响应

```json
{
  "code": 0,
  "message": "注册成功",
  "data": {
    "userId": 10001,
    "username": "zhangsan",
    "realName": "张三",
    "role": "patient",
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
- 路径：`/auth/login`

### 3.2 请求参数

```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

字段说明：

| 字段 | 类型 | 必填 | 规则 |
|---|---|---|---|
| username | string | 是 | 用户名或手机号 |
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
      "realName": "张三",
      "email": "zhangsan@example.com",
      "role": "patient"
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

## 5. 科室管理

### 5.1 获取科室列表

- 方法：`GET`
- 路径：`/departments`
- 权限：所有角色

#### 成功响应

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "deptId": 1,
      "deptName": "内科",
      "description": "内科诊疗科室",
      "location": "门诊楼1楼",
      "createdAt": "2026-03-29T10:00:00Z"
    }
  ]
}
```

---

## 6. 医生管理

### 6.1 获取医生列表

- 方法：`GET`
- 路径：`/doctors`
- 权限：所有角色

#### 查询参数

| 字段 | 类型 | 描述 |
|---|---|---|
| deptId | number | 科室ID |
| status | string | 状态（available/unavailable） |

#### 成功响应

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "doctorId": 1,
      "userId": 2,
      "deptId": 1,
      "realName": "张医生",
      "title": "主任医师",
      "specialty": "心血管疾病",
      "bio": "从事心血管疾病诊疗20年，经验丰富",
      "status": "available",
      "createdAt": "2026-03-29T10:00:00Z"
    }
  ]
}
```

---

## 7. 排班管理

### 7.1 获取医生排班

- 方法：`GET`
- 路径：`/schedules`
- 权限：所有角色

#### 查询参数

| 字段 | 类型 | 描述 |
|---|---|---|
| doctorId | number | 医生ID |
| date | string | 日期（YYYY-MM-DD） |

#### 成功响应

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "scheduleId": 1,
      "doctorId": 1,
      "dayOfWeek": "1",
      "startTime": "08:00:00",
      "endTime": "12:00:00",
      "availableSlots": 20,
      "status": "active"
    }
  ]
}
```

---

## 8. 预约挂号

### 8.1 创建预约

- 方法：`POST`
- 路径：`/appointments`
- 权限：患者

#### 请求参数

```json
{
  "patientId": 1,
  "doctorId": 1,
  "scheduleId": 1,
  "appointmentDate": "2026-04-01",
  "appointmentTime": "08:30:00"
}
```

#### 成功响应

```json
{
  "code": 0,
  "message": "预约成功",
  "data": {
    "appointmentId": 1,
    "status": "pending",
    "createdAt": "2026-03-31T10:00:00Z"
  }
}
```

### 8.2 获取患者预约列表

- 方法：`GET`
- 路径：`/appointments/patient`
- 权限：患者

#### 成功响应

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "appointmentId": 1,
      "doctorId": 1,
      "doctorName": "张医生",
      "deptName": "内科",
      "appointmentDate": "2026-04-01",
      "appointmentTime": "08:30:00",
      "status": "confirmed",
      "createdAt": "2026-03-31T10:00:00Z"
    }
  ]
}
```

---

## 9. 就诊记录

### 9.1 获取就诊记录

- 方法：`GET`
- 路径：`/consultations`
- 权限：患者、医生

#### 查询参数

| 字段 | 类型 | 描述 |
|---|---|---|
| patientId | number | 患者ID |
| doctorId | number | 医生ID |

#### 成功响应

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "consultationId": 1,
      "appointmentId": 1,
      "doctorId": 1,
      "patientId": 1,
      "symptoms": "胸闷、气短、心悸",
      "diagnosis": "冠心病",
      "treatment": "药物治疗、定期复查",
      "prescription": "阿司匹林肠溶片、硝酸甘油片",
      "consultationDate": "2026-03-31T09:00:00Z",
      "createdAt": "2026-03-31T10:00:00Z"
    }
  ]
}
```

---

## 10. 住院管理

### 10.1 住院登记

- 方法：`POST`
- 路径：`/hospitalizations`
- 权限：管理员、医生

#### 请求参数

```json
{
  "patientId": 1,
  "doctorId": 1,
  "deptId": 1,
  "admissionDate": "2026-04-01T10:00:00Z",
  "reason": "冠心病，需要进一步检查和治疗"
}
```

#### 成功响应

```json
{
  "code": 0,
  "message": "登记成功",
  "data": {
    "hospitalizationId": 1,
    "status": "admitted",
    "createdAt": "2026-03-31T10:00:00Z"
  }
}
```

---

## 11. 系统公告

### 11.1 获取公告列表

- 方法：`GET`
- 路径：`/notices`
- 权限：所有角色

#### 成功响应

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "noticeId": 1,
      "title": "医院门诊时间调整通知",
      "content": "从2026年4月1日起，医院门诊时间调整为：上午8:00-12:00，下午14:00-18:00。请各位患者合理安排就诊时间。",
      "author": "系统管理员",
      "publishDate": "2026-03-25T10:00:00Z",
      "status": "active",
      "createdAt": "2026-03-25T10:00:00Z"
    }
  ]
}
```

---

## 12. 患者账户

### 12.1 充值

- 方法：`POST`
- 路径：`/recharge`
- 权限：患者

#### 请求参数

```json
{
  "patientId": 1,
  "amount": 500.00,
  "paymentMethod": "online"
}
```

#### 成功响应

```json
{
  "code": 0,
  "message": "充值成功",
  "data": {
    "recordId": 1,
    "amount": 500.00,
    "balance": 1500.00,
    "rechargeDate": "2026-03-31T10:00:00Z"
  }
}
```

### 12.2 缴费

- 方法：`POST`
- 路径：`/payment`
- 权限：患者

#### 请求参数

```json
{
  "patientId": 1,
  "consultationId": 1,
  "amount": 200.00,
  "paymentMethod": "online"
}
```

#### 成功响应

```json
{
  "code": 0,
  "message": "缴费成功",
  "data": {
    "paymentId": 1,
    "amount": 200.00,
    "balance": 1300.00,
    "paymentDate": "2026-03-31T10:00:00Z"
  }
}
```

---

## 13. curl 示例

### 13.1 注册

```bash
curl -X POST 'http://localhost:3000/auth/register' \
  -H 'Content-Type: application/json' \
  -d '{
    "username":"zhangsan",
    "realName":"张三",
    "phone":"13800138001",
    "email":"zhangsan@example.com",
    "password":"123456",
    "confirmPassword":"123456",
    "role":"patient"
  }'
```

### 13.2 登录

```bash
curl -X POST 'http://localhost:3000/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "username":"zhangsan",
    "password":"123456"
  }'
```

### 13.3 获取科室列表

```bash
curl -X GET 'http://localhost:3000/departments' \
  -H 'Authorization: Bearer jwt-token-demo'
```

### 13.4 获取医生列表

```bash
curl -X GET 'http://localhost:3000/doctors?deptId=1' \
  -H 'Authorization: Bearer jwt-token-demo'
```

### 13.5 创建预约

```bash
curl -X POST 'http://localhost:3000/appointments' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer jwt-token-demo' \
  -d '{
    "patientId": 1,
    "doctorId": 1,
    "scheduleId": 1,
    "appointmentDate": "2026-04-01",
    "appointmentTime": "08:30:00"
  }'
```

