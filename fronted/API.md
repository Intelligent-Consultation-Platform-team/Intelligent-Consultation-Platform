# 智能问诊平台接口文档（前端联调版）

> 适用范围：前端联调、接口对齐、开发自测
>
> 说明：本文档基于当前前端请求封装、路由代理与已存在页面能力整理，补充了当前文档中缺失的接口，并统一了字段、状态码与分页约定。

---

## 1. 基础约定

### 1.1 基础地址

- 开发环境前缀：`/`
- 本地代理目标：`http://localhost:8123`
- 前端请求会自动通过 `vite.config.js` 中的代理转发到后端

### 1.2 通用请求头

- `Content-Type: application/json`
- 登录后统一携带：`Authorization: Bearer <token>`

### 1.3 统一响应结构

当前前端请求工具兼容两种常见响应风格，建议后端统一为以下格式之一：

#### 方案 A：业务码风格
```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

#### 方案 B：HTTP 成功码风格
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

> 建议：项目内尽量统一为 `code = 0` 或 `code = 200` 其中一种，避免联调时出现判断不一致。

### 1.4 前端成功判定逻辑

前端当前兼容以下成功条件：
- `result.code === 0`
- `result.code === 200`
- `result.success === true`

### 1.5 通用错误建议

| HTTP状态码 | 含义 |
|---|---|
| 400 | 参数错误 |
| 401 | 未登录或登录失效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务端异常 |

### 1.6 时间格式

- 日期：`YYYY-MM-DD`
- 时间：`HH:mm:ss`
- 日期时间：`YYYY-MM-DD HH:mm:ss` 或 ISO 8601

---

## 2. 认证模块

### 2.1 用户注册

- 方法：`POST`
- 路径：`/auth/register`
- 权限：无需登录

#### 请求参数
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

#### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | string | 是 | 用户名，建议 3-20 位 |
| realName | string | 是 | 真实姓名 |
| phone | string | 是 | 手机号 |
| email | string | 否 | 邮箱 |
| password | string | 是 | 密码 |
| confirmPassword | string | 是 | 确认密码 |
| role | string | 是 | `patient` / `doctor` / `admin` |

#### 成功响应
```json
{
  "code": 0,
  "message": "注册成功",
  "data": {
    "userId": 10001,
    "username": "zhangsan",
    "realName": "张三",
    "role": "patient",
    "createdAt": "2026-03-29 10:00:00"
  }
}
```

### 2.2 用户登录

- 方法：`POST`
- 路径：`/auth/login`
- 权限：无需登录

#### 请求参数
```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

#### 成功响应
```json
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "token": "jwt-token-demo",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "loginAt": 1711706400000,
    "user": {
      "userId": 10001,
      "username": "zhangsan",
      "realName": "张三",
      "phone": "13800138001",
      "role": "patient"
    }
  }
}
```

> 说明：前端会把 `token` 和 `tokenType` 写入本地会话信息，用于后续请求自动携带认证头。

---

## 3. 科室模块

### 3.1 获取科室列表

- 方法：`GET`
- 路径：`/departments`
- 权限：所有角色

#### 查询参数
无

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
      "status": "enabled",
      "createdAt": "2026-03-29 10:00:00"
    }
  ]
}
```

#### 建议补充字段
| 字段 | 类型 | 说明 |
|---|---|---|
| status | string | `enabled` / `disabled`，用于控制是否可预约或展示 |

---

## 4. 医生模块

### 4.1 获取医生列表

- 方法：`GET`
- 路径：`/doctors`
- 权限：所有角色

#### 查询参数

| 字段 | 类型 | 说明 |
|---|---|---|
| deptId | number | 科室ID |
| status | string | 医生状态：`available` / `unavailable` |
| keyword | string | 医生姓名或专长关键字 |

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
      "createdAt": "2026-03-29 10:00:00"
    }
  ]
}
```

---

## 5. 排班模块

### 5.1 获取医生排班

- 方法：`GET`
- 路径：`/schedules`
- 权限：所有角色

#### 查询参数

| 字段 | 类型 | 说明 |
|---|---|---|
| doctorId | number | 医生ID |
| date | string | 日期，格式 `YYYY-MM-DD` |
| deptId | number | 科室ID（可选） |

#### 成功响应
```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "scheduleId": 1,
      "doctorId": 1,
      "deptId": 1,
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

## 6. 预约模块

### 6.1 创建预约

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

#### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| patientId | number | 是 | 患者ID |
| doctorId | number | 是 | 医生ID |
| scheduleId | number | 是 | 排班ID |
| appointmentDate | string | 是 | 预约日期 |
| appointmentTime | string | 是 | 预约时间 |

#### 成功响应
```json
{
  "code": 0,
  "message": "预约成功",
  "data": {
    "appointmentId": 1,
    "status": "pending",
    "createdAt": "2026-03-31 10:00:00"
  }
}
```

### 6.2 获取患者预约列表

- 方法：`GET`
- 路径：`/appointments/patient`
- 权限：患者

#### 查询参数
无

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
      "createdAt": "2026-03-31 10:00:00"
    }
  ]
}
```

---

## 7. 就诊记录模块

### 7.1 获取就诊记录

- 方法：`GET`
- 路径：`/consultations`
- 权限：患者、医生、管理员（按业务控制）

#### 查询参数

| 字段 | 类型 | 说明 |
|---|---|---|
| patientId | number | 患者ID |
| doctorId | number | 医生ID |
| appointmentId | number | 预约ID（可选） |
| page | number | 页码（可选） |
| pageSize | number | 每页条数（可选） |

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
      "consultationDate": "2026-03-31 09:00:00",
      "createdAt": "2026-03-31 10:00:00"
    }
  ]
}
```

---

## 8. 住院模块

### 8.1 住院登记

- 方法：`POST`
- 路径：`/hospitalizations`
- 权限：管理员、医生

#### 请求参数
```json
{
  "patientId": 1,
  "doctorId": 1,
  "deptId": 1,
  "admissionDate": "2026-04-01 10:00:00",
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
    "createdAt": "2026-03-31 10:00:00"
  }
}
```

---

## 9. 系统公告模块

### 9.1 获取公告列表

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
      "content": "从2026年4月1日起，医院门诊时间调整为：上午8:00-12:00，下午14:00-18:00。",
      "author": "系统管理员",
      "publishDate": "2026-03-25 10:00:00",
      "status": "active",
      "createdAt": "2026-03-25 10:00:00"
    }
  ]
}
```

---

## 10. 患者账户模块

### 10.1 充值

- 方法：`POST`
- 路径：`/recharge`
- 权限：患者

#### 请求参数
```json
{
  "patientId": 1,
  "amount": 500.0,
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
    "amount": 500.0,
    "balance": 1500.0,
    "rechargeDate": "2026-03-31 10:00:00"
  }
}
```

### 10.2 缴费

- 方法：`POST`
- 路径：`/payment`
- 权限：患者

#### 请求参数
```json
{
  "patientId": 1,
  "consultationId": 1,
  "amount": 200.0,
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
    "amount": 200.0,
    "balance": 1300.0,
    "paymentDate": "2026-03-31 10:00:00"
  }
}
```

---

## 11. AI 问诊模块

> 说明：该模块来自前端代码 `src/api/aiConsultation.js`，是当前接口文档中缺失的部分，建议补充到后端联调文档中。

### 11.1 创建问诊会话

- 方法：`POST`
- 路径：`/api/ai-consultation/session`
- 权限：登录用户

#### 请求参数
```json
{
  "chiefComplaint": "头痛、发热两天",
  "symptomTags": ["头痛", "发热"],
  "age": 28,
  "gender": "female"
}
```

#### 成功响应
```json
{
  "code": 0,
  "message": "创建成功",
  "data": {
    "sessionId": "cs_202501010001",
    "status": "active",
    "createdAt": "2025-01-01 10:00:00"
  }
}
```

### 11.2 发送问诊消息

- 方法：`POST`
- 路径：`/api/ai-consultation/message`
- 权限：登录用户

#### 请求参数
```json
{
  "sessionId": "cs_202501010001",
  "content": "我最近咳嗽，有点胸闷，需要注意什么？",
  "contentType": "text"
}
```

#### 成功响应
```json
{
  "code": 0,
  "message": "发送成功",
  "data": {
    "userMessage": {
      "messageId": "msg_u_001",
      "role": "user",
      "content": "我最近咳嗽，有点胸闷，需要注意什么？",
      "createdAt": "2025-01-01 10:01:00"
    },
    "assistantMessage": {
      "messageId": "msg_a_001",
      "role": "assistant",
      "content": "根据你描述的症状，建议先观察是否伴有发热、气促或持续加重...",
      "createdAt": "2025-01-01 10:01:02"
    },
    "riskLevel": "medium",
    "suggestion": "如症状持续或加重，请及时前往呼吸科就诊。"
  }
}
```

### 11.3 获取会话详情

- 方法：`GET`
- 路径：`/api/ai-consultation/session/{sessionId}`
- 权限：登录用户

### 11.4 获取会话消息列表

- 方法：`GET`
- 路径：`/api/ai-consultation/session/{sessionId}/messages`
- 权限：登录用户

#### 查询参数

| 字段 | 类型 | 说明 |
|---|---|---|
| page | number | 页码，默认 1 |
| pageSize | number | 每页条数，默认 20 |

### 11.5 结束会话

- 方法：`POST`
- 路径：`/api/ai-consultation/session/{sessionId}/close`
- 权限：登录用户

#### 请求参数
```json
{
  "closeReason": "用户已获取建议，结束问诊"
}
```

### 11.6 查询我的会话

- 方法：`GET`
- 路径：`/api/ai-consultation/session/my`
- 权限：登录用户

#### 查询参数

| 字段 | 类型 | 说明 |
|---|---|---|
| page | number | 页码 |
| pageSize | number | 每页条数 |
| status | string | 会话状态，可选 |

### 11.7 风险评估

- 方法：`GET`
- 路径：`/api/ai-consultation/session/{sessionId}/risk`
- 权限：登录用户

### 11.8 上传文件

- 方法：`POST`
- 路径：`/api/ai-consultation/upload`
- 权限：登录用户

#### 请求说明
- 推荐使用 `multipart/form-data`
- 文件类型可支持图片、PDF、检查单等

---

## 12. 当前文档缺失项与优化建议

### 12.1 当前文档已缺失或不完整的接口

根据前端代码与代理配置，当前接口文档至少缺少以下内容：

1. `POST /auth/register` 注册接口
2. `GET /doctors` 医生列表接口
3. `GET /schedules` 排班接口
4. `POST /hospitalizations` 住院登记接口
5. `POST /recharge` 充值接口
6. `POST /api/ai-consultation/*` AI 问诊相关接口整组
7. `GET /consultations` 的分页与筛选说明
8. `department / doctor / schedule / appointment` 的字段状态说明

### 12.2 建议优化点

1. **统一响应格式**：建议统一 `code/message/data` 结构，避免有的接口返回 `0`，有的返回 `200`。
2. **补充权限说明**：每个接口明确“所有角色 / 患者 / 医生 / 管理员”。
3. **补充分页规范**：列表接口统一 `page`、`pageSize`、`total`、`list` 或直接数组的返回方式。
4. **补充枚举值**：如 `status`、`role`、`riskLevel`、`paymentMethod` 等。
5. **补充错误示例**：至少给出参数错误、未登录、无权限、资源不存在四类。
6. **补充 AI 问诊模块**：这是当前最明显的文档缺口，建议单独成章。

---

## 13. curl 示例

### 13.1 登录
```bash
curl -X POST 'http://localhost:3000/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "username":"zhangsan",
    "password":"123456"
  }'
```

### 13.2 获取科室列表
```bash
curl -X GET 'http://localhost:3000/departments' \
  -H 'Authorization: Bearer jwt-token-demo'
```

### 13.3 创建预约
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

### 13.4 创建 AI 问诊会话
```bash
curl -X POST 'http://localhost:3000/api/ai-consultation/session' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer jwt-token-demo' \
  -d '{
    "chiefComplaint": "头痛、发热两天",
    "symptomTags": ["头痛", "发热"],
    "age": 28,
    "gender": "female"
  }'
```
