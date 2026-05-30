# 智能问诊平台

## 1.1 项目概况

* **项目名称**：智能问诊平台
* **开发模式**：前后端分离
* **技术栈**：后端 JAVA（Spring Boot + MyBatis-Flex），前端 Vue.js
* **主要受众**：患者（用户）及医院管理员

## 1.2 背景与意义

### 1.2.1 现状与问题

* **就医压力**：线下门诊挂号窗口压力大，患者看病时间成本高
* **信息焦虑**：用户对突发小病症缺乏专业科普，盲目搜索（如百度）易受广告干扰或产生过度焦虑
* **资源分布**：权威医院门户网站往往侧重政务宣传，缺乏深入的卫生知识和医疗常识科普

### 1.2.2 项目意义

* **便民科普**：提供专业病症查询与家用治疗方案，引导用户科学处理小病，实现自行调节
* **分流减压**：通过线上问诊与科普，减少非必要到院人数，缓解医院人流压力
* **医患透明**：通过专业的信息管理维护，减少医患间信息不对称，提升社会稳定性

## 1.3 项目目标

* **实现智能问诊**：与AI医生助手进行对话交流
* **优化预约流程**：用户可在线查看科室位置剩余情况，并根据需要自主预约
* **提升管理效率**：为医院提供后端管理系统，实时维护科室状态及症状信息

## 1.4 系统功能范围

### 1.4.1 用户端（前台）

| 功能模块 | 功能描述 |
|---------|---------|
| 登录注册 | 支持用户注册登录，是预约和收藏功能的前提 |
| 信息管理 | 提供科室，公告，医生排班等信息 |
| 症状查看 | AI问诊：智能分析症状并提供医疗建议 |
| 预约就诊 | 用户可以进行预约挂号，查看就诊记录，医嘱病历，住院登记，清单缴纳 |
| 个人中心 | 统一管理个人账号信息 |

### 1.4.2 管理端（后台）

| 功能模块 | 功能描述 |
|---------|---------|
| 科室信息管理 | 对科室内容进行增、删、改、查 |
| 患者挂号管理 | 管理患者挂号信息 |
| 预约挂号管理 | 可以查看当天可预约挂号的医生信息 |
| 患者就诊管理 | 管理患者就诊信息 |
| 医生排班管理 | 给平台所有医生进行排班 |
| 公告管理 | 管理能够上传公告 |

## 1.5 业务约束与非功能需求

* **业务规则**：同一科室同一时间段内不可重复预约
* **数据维护**：系统需具备专业的管理后台，确保信息的专业性与权威性，以获取用户信任
* **核心指标**：优化患者住院/就诊时间，缓解医院拥挤程度

---

## 技术栈

### 后端技术

* **框架**：Spring Boot 3.5.13
* **ORM**：MyBatis-Flex
* **安全**：JWT（JSON Web Token）认证
* **数据库**：MySQL
* **构建工具**：Maven

### 前端技术

* **框架**：Vue.js
* **HTTP客户端**：Axios
* **UI组件**：Element Plus

### 部署

* **后端端口**：8123
* **前端端口**：3000

---

## API 接口列表

### 认证模块 `/auth`

#### 1. 用户注册
- **方法**: POST
- **路径**: `/auth/register`
- **描述**: 用户注册新账号
- **请求体**:
```json
{
  "username": "string",
  "password": "string",
  "realName": "string",
  "phone": "string",
  "idCard": "string",
  "role": "patient"
}
```
- **响应示例**:
```json
{
  "code": 0,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "zhangsan",
    "realName": "张三",
    "role": "patient",
    "createdAt": "2026-05-30T10:00:00Z"
  }
}
```

#### 2. 用户登录
- **方法**: POST
- **路径**: `/auth/login`
- **描述**: 用户登录获取Token
- **请求体**:
```json
{
  "username": "string",
  "password": "string"
}
```
- **响应示例**:
```json
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "zhangsan",
    "realName": "张三",
    "role": "patient",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

### 用户模块 `/users`

#### 1. 获取用户列表
- **方法**: GET
- **路径**: `/users/list`
- **描述**: 获取所有用户列表
- **响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "username": "zhangsan",
      "realName": "张三",
      "phone": "13800138000",
      "role": "patient"
    }
  ]
}
```

#### 2. 添加用户
- **方法**: POST
- **路径**: `/users/save`
- **描述**: 添加新用户
- **请求体**:
```json
{
  "username": "string",
  "password": "string",
  "realName": "string",
  "phone": "string",
  "idCard": "string",
  "role": "string"
}
```
- **响应**: `true` 或 `false`

#### 3. 更新用户信息
- **方法**: PUT
- **路径**: `/users/update`
- **描述**: 更新用户信息
- **请求体**:
```json
{
  "id": 1,
  "username": "string",
  "realName": "string",
  "phone": "string"
}
```
- **响应**: `true` 或 `false`

#### 4. 删除用户
- **方法**: DELETE
- **路径**: `/users/remove/{id}`
- **描述**: 根据ID删除用户
- **响应**: `true` 或 `false`

#### 5. 获取用户详情
- **方法**: GET
- **路径**: `/users/getInfo/{id}`
- **描述**: 根据ID获取用户详情
- **响应示例**:
```json
{
  "id": 1,
  "username": "zhangsan",
  "realName": "张三",
  "phone": "13800138000",
  "role": "patient"
}
```

#### 6. 分页查询用户
- **方法**: GET
- **路径**: `/users/page`
- **描述**: 分页查询用户列表
- **参数**: `pageNumber`, `pageSize`

---

### 科室模块 `/departments`

#### 1. 获取科室列表
- **方法**: GET
- **路径**: `/departments`
- **描述**: 获取所有科室列表
- **响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "deptId": 1,
      "deptName": "内科",
      "location": "1号楼2层",
      "description": "内科门诊",
      "createdAt": "2026-05-30T10:00:00Z"
    }
  ]
}
```

#### 2. 获取科室详情
- **方法**: GET
- **路径**: `/departments/{deptId}`
- **描述**: 根据ID获取科室详情
- **响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "deptId": 1,
    "deptName": "内科",
    "location": "1号楼2层",
    "description": "内科门诊",
    "createdAt": "2026-05-30T10:00:00Z"
  }
}
```

#### 3. 添加科室
- **方法**: POST
- **路径**: `/departments`
- **描述**: 添加新科室
- **请求体**:
```json
{
  "deptName": "string",
  "location": "string",
  "description": "string"
}
```
- **响应**: `true` 或 `false`

#### 4. 更新科室
- **方法**: PUT
- **路径**: `/departments`
- **描述**: 更新科室信息
- **请求体**:
```json
{
  "deptId": 1,
  "deptName": "string",
  "location": "string",
  "description": "string"
}
```
- **响应**: `true` 或 `false`

#### 5. 删除科室
- **方法**: DELETE
- **路径**: `/departments/{deptId}`
- **描述**: 删除指定科室
- **响应**: `true` 或 `false`

---

### 医生模块 `/doctors`

#### 1. 获取医生列表
- **方法**: GET
- **路径**: `/doctors`
- **描述**: 获取医生列表（支持按科室和状态筛选）
- **参数**:
  - `deptId` (可选): 科室ID
  - `status` (可选): 医生状态
- **响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "doctorId": 1,
      "doctorName": "李医生",
      "title": "主任医师",
      "deptId": 1,
      "deptName": "内科",
      "specialty": "心血管疾病",
      "status": "active"
    }
  ]
}
```

#### 2. 获取医生详情
- **方法**: GET
- **路径**: `/doctors/{doctorId}`
- **描述**: 根据ID获取医生详情
- **响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "doctorId": 1,
    "doctorName": "李医生",
    "title": "主任医师",
    "specialty": "心血管疾病",
    "experience": 20
  }
}
```

#### 3. 添加医生
- **方法**: POST
- **路径**: `/doctors`
- **描述**: 添加新医生
- **请求体**:
```json
{
  "userId": 1,
  "deptId": 1,
  "doctorName": "string",
  "title": "string",
  "specialty": "string",
  "experience": 10,
  "status": "active"
}
```
- **响应**: `true` 或 `false`

#### 4. 更新医生信息
- **方法**: PUT
- **路径**: `/doctors`
- **描述**: 更新医生信息
- **请求体**:
```json
{
  "doctorId": 1,
  "doctorName": "string",
  "title": "string",
  "specialty": "string"
}
```
- **响应**: `true` 或 `false`

#### 5. 删除医生
- **方法**: DELETE
- **路径**: `/doctors/{doctorId}`
- **描述**: 删除指定医生
- **响应**: `true` 或 `false`

---

### 排班模块 `/schedules`

#### 1. 获取排班列表
- **方法**: GET
- **路径**: `/schedules`
- **描述**: 获取医生排班列表（包含医生信息）
- **参数**:
  - `deptId` (可选): 科室ID
  - `doctorId` (可选): 医生ID
  - `date` (可选): 日期，格式 YYYY-MM-DD
- **响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "scheduleId": 1,
      "doctorId": 1,
      "doctorName": "李医生",
      "deptName": "内科",
      "scheduleDate": "2026-05-30",
      "timeSlot": "09:00-12:00",
      "maxAppointments": 20,
      "currentAppointments": 5,
      "remainingSlots": 15
    }
  ]
}
```

#### 2. 获取排班详情
- **方法**: GET
- **路径**: `/schedules/{scheduleId}`
- **描述**: 根据ID获取排班详情
- **响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "scheduleId": 1,
    "doctorId": 1,
    "scheduleDate": "2026-05-30",
    "timeSlot": "09:00-12:00",
    "status": "available"
  }
}
```

#### 3. 添加排班
- **方法**: POST
- **路径**: `/schedules`
- **描述**: 添加新的排班
- **请求体**:
```json
{
  "doctorId": 1,
  "scheduleDate": "2026-05-30",
  "timeSlot": "09:00-12:00",
  "maxAppointments": 20,
  "status": "available"
}
```
- **响应**: `true` 或 `false`

#### 4. 更新排班信息
- **方法**: PUT
- **路径**: `/schedules`
- **描述**: 更新排班信息
- **请求体**:
```json
{
  "scheduleId": 1,
  "scheduleDate": "2026-05-30",
  "timeSlot": "14:00-17:00",
  "maxAppointments": 25
}
```
- **响应**: `true` 或 `false`

#### 5. 删除排班
- **方法**: DELETE
- **路径**: `/schedules/{scheduleId}`
- **描述**: 删除指定排班
- **响应**: `true` 或 `false`

---

### 公告模块 `/notices`

#### 1. 获取公告列表
- **方法**: GET
- **路径**: `/notices`
- **描述**: 获取所有公告列表
- **响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "noticeId": 1,
      "title": "系统维护通知",
      "content": "系统将于周末进行维护...",
      "publishTime": "2026-05-29T10:00:00Z",
      "publisher": "管理员"
    }
  ]
}
```

#### 2. 获取公告详情
- **方法**: GET
- **路径**: `/notices/{noticeId}`
- **描述**: 根据ID获取公告详情
- **响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "noticeId": 1,
    "title": "系统维护通知",
    "content": "系统将于周末进行维护...",
    "publishTime": "2026-05-29T10:00:00Z"
  }
}
```

#### 3. 添加公告
- **方法**: POST
- **路径**: `/notices`
- **描述**: 发布新公告
- **请求体**:
```json
{
  "title": "string",
  "content": "string",
  "publisher": "string"
}
```
- **响应**: `true` 或 `false`

#### 4. 更新公告
- **方法**: PUT
- **路径**: `/notices`
- **描述**: 更新公告信息
- **请求体**:
```json
{
  "noticeId": 1,
  "title": "string",
  "content": "string"
}
```
- **响应**: `true` 或 `false`

#### 5. 删除公告
- **方法**: DELETE
- **路径**: `/notices/{noticeId}`
- **描述**: 删除指定公告
- **响应**: `true` 或 `false`

---

### 预约模块 `/appointment`

#### 1. 创建预约
- **方法**: POST
- **路径**: `/appointment`
- **描述**: 创建新的预约挂号
- **请求体**:
```json
{
  "patientId": 1,
  "doctorId": 1,
  "scheduleId": 1,
  "appointmentDate": "2026-05-30"
}
```
- **响应示例**:
```json
{
  "code": 0,
  "message": "预约成功",
  "data": {
    "appointmentId": 1,
    "status": "pending"
  }
}
```

#### 2. 获取预约列表 / 分页
- **方法**: GET
- **路径**: `/appointment`
- **描述**: 分页查询所有预约，支持按患者姓名和状态筛选
- **参数**:
  - `current` (可选, 默认1): 当前页
  - `size` (可选, 默认10): 每页条数
  - `patientName` (可选): 患者姓名（模糊搜索）
  - `status` (可选): 状态筛选

#### 3. 获取患者预约列表
- **方法**: GET
- **路径**: `/appointment/patient/{patientId}`
- **描述**: 获取指定患者的预约列表
- **响应示例**:
```json
{
  "code": 0,
  "data": [
    {
      "appointmentId": 1,
      "patientId": 1,
      "doctorName": "李医生",
      "deptName": "内科",
      "appointmentDate": "2026-05-30",
      "status": "pending"
    }
  ]
}
```

#### 4. 医生获取出诊列表
- **方法**: GET
- **路径**: `/appointment/doctor`
- **描述**: 医生获取自己的出诊列表（含患者信息）

#### 5. 获取预约详情
- **方法**: GET
- **路径**: `/appointment/{appointmentId}`
- **描述**: 根据ID获取预约详情

#### 6. 取消预约
- **方法**: PUT
- **路径**: `/appointment/{appointmentId}/cancel`

#### 7. 患者签到
- **方法**: PUT
- **路径**: `/appointment/{appointmentId}/confirm`

#### 8. 完成就诊
- **方法**: PUT
- **路径**: `/appointment/{appointmentId}/complete`

---

### 就诊记录模块 `/consultation`

#### 1. 获取就诊记录列表
- **方法**: GET
- **路径**: `/consultation`
- **描述**: 获取就诊记录列表
- **参数**:
  - `patientId` (可选): 患者ID
  - `doctorId` (可选): 医生ID

#### 2. 获取就诊记录详情
- **方法**: GET
- **路径**: `/consultation/{consultationId}`

#### 3. 医生填写病历（问诊）
- **方法**: POST
- **路径**: `/consultation`
- **描述**: 医生填写病历并开处方，自动更新预约状态为 `processing`
- **请求体**:
```json
{
  "appointmentId": 1,
  "doctorId": 1,
  "patientId": 1,
  "symptoms": "头痛、发热",
  "diagnosis": "上呼吸道感染",
  "treatment": "口服药物治疗",
  "prescription": "阿莫西林胶囊 0.5g tid"
}
```

---

### 住院管理模块 `/hospitalization`

#### 1. 住院登记
- **方法**: POST
- **路径**: `/hospitalization`

---

### 系统公告模块 `/notice`

#### 1. 获取公告列表
- **方法**: GET
- **路径**: `/notice`

#### 2. 获取公告详情
- **方法**: GET
- **路径**: `/notice/{noticeId}`

#### 3. 添加公告
- **方法**: POST
- **路径**: `/notice`

#### 4. 更新公告
- **方法**: PUT
- **路径**: `/notice`

#### 5. 删除公告
- **方法**: DELETE
- **路径**: `/notice/{noticeId}`

---

### 患者账户模块 `/patient`

#### 1. 获取余额
- **方法**: GET
- **路径**: `/patient/{patientId}/balance`

#### 2. 账户充值
- **方法**: POST
- **路径**: `/patient/{patientId}/recharge`
- **请求体**:
```json
{
  "amount": 1000.00,
  "paymentMethod": "alipay"
}
```

#### 3. 费用缴纳
- **方法**: POST
- **路径**: `/patient/{patientId}/payment`
- **请求体**:
```json
{
  "consultationId": 1,
  "amount": 200.00,
  "paymentMethod": "account"
}
```

---

### 业务流程状态流转

#### 预约单状态 `appointments.status`
| 状态 | 说明 |
|------|------|
| `pending` | 待确认（刚预约） |
| `confirmed` | 已确认 / 已签到候诊 |
| `processing` | 就诊中（医生正在接诊） |
| `completed` | 已完成 |
| `cancelled` | 已取消 |

#### 缴费单状态 `payment_records.status`
| 状态 | 说明 |
|------|------|
| `unpaid` | 待缴费 |
| `paid` | 已缴费 |

#### 住院状态 `hospitalizations.status`
| 状态 | 说明 |
|------|------|
| `admitted` | 住院中 |
| `discharged` | 已出院 |

---

### AI问诊模块 `/api/ai-consultation`

#### 1. 创建问诊会话
- **方法**: POST
- **路径**: `/api/ai-consultation/session`
- **描述**: 创建新的AI问诊会话
- **请求体**:
```json
{
  "chiefComplaint": "头痛、发热三天"
}
```
- **响应示例**:
```json
{
  "code": 0,
  "message": "创建成功",
  "data": {
    "sessionId": "sess_abc123",
    "chiefComplaint": "头痛、发热三天",
    "status": "active",
    "createdAt": "2026-05-30T10:00:00Z"
  }
}
```

#### 2. 发送消息
- **方法**: POST
- **路径**: `/api/ai-consultation/message`
- **描述**: 在问诊会话中发送消息
- **请求体**:
```json
{
  "sessionId": "sess_abc123",
  "message": "我已经发烧三天了，体温38.5度"
}
```
- **响应示例**:
```json
{
  "code": 0,
  "message": "发送成功",
  "data": {
    "messageId": 1,
    "content": "您好，根据您描述的症状，建议您先进行体温监测...",
    "isAi": true,
    "timestamp": "2026-05-30T10:01:00Z"
  }
}
```

#### 3. 获取会话详情
- **方法**: GET
- **路径**: `/api/ai-consultation/session/{sessionId}`
- **描述**: 获取指定会话的详细信息
- **响应示例**:
```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "sessionId": "sess_abc123",
    "chiefComplaint": "头痛、发热三天",
    "status": "active",
    "createdAt": "2026-05-30T10:00:00Z"
  }
}
```

#### 4. 获取会话消息列表
- **方法**: GET
- **路径**: `/api/ai-consultation/session/{sessionId}/messages`
- **描述**: 获取会话中的消息列表
- **参数**:
  - `page` (可选, 默认1): 页码
  - `pageSize` (可选, 默认20): 每页数量
- **响应示例**:
```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "messageId": 1,
      "content": "我已经发烧三天了",
      "isAi": false,
      "timestamp": "2026-05-30T10:00:30Z"
    },
    {
      "messageId": 2,
      "content": "您好，根据您的描述...",
      "isAi": true,
      "timestamp": "2026-05-30T10:00:35Z"
    }
  ]
}
```

#### 5. 结束会话
- **方法**: POST
- **路径**: `/api/ai-consultation/session/{sessionId}/close`
- **描述**: 结束AI问诊会话
- **请求体**:
```json
{
  "closeReason": "已完成问诊"
}
```
- **响应示例**:
```json
{
  "code": 0,
  "message": "会话已结束",
  "data": {
    "sessionId": "sess_abc123",
    "status": "closed"
  }
}
```

#### 6. 获取我的会话列表
- **方法**: GET
- **路径**: `/api/ai-consultation/session/my`
- **描述**: 获取当前用户的问诊会话列表
- **参数**:
  - `page` (可选, 默认1): 页码
  - `pageSize` (可选, 默认20): 每页数量
  - `status` (可选): 筛选状态 (active/closed)
- **响应示例**:
```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "sessionId": "sess_abc123",
      "chiefComplaint": "头痛、发热三天",
      "status": "active",
      "createdAt": "2026-05-30T10:00:00Z"
    }
  ]
}
```

#### 7. 获取风险评估
- **方法**: GET
- **路径**: `/api/ai-consultation/session/{sessionId}/risk`
- **描述**: 获取会话的风险评估结果
- **响应示例**:
```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "riskLevel": "low",
    "suggestion": "建议观察，若症状加重请及时就医",
    "keywords": ["发热", "头痛"]
  }
}
```

---

## API 测试指南

### 测试工具
推荐使用以下工具进行API测试：
- **Postman**: 功能强大的API测试工具
- **Apifox**: 国产API管理平台
- **curl**: 命令行测试工具

### 测试示例（使用curl）

#### 1. 用户注册测试
```bash
curl -X POST http://localhost:8123/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "realName": "测试用户",
    "phone": "13800138000",
    "idCard": "110101199001011234",
    "role": "patient"
  }'
```

#### 2. 用户登录测试
```bash
curl -X POST http://localhost:8123/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

#### 3. 获取科室列表测试
```bash
curl -X GET http://localhost:8123/departments
```

#### 4. 创建预约测试（需要Token）
```bash
curl -X POST http://localhost:8123/appointment \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your_token>" \
  -d '{
    "patientId": 1,
    "doctorId": 1,
    "scheduleId": 1,
    "appointmentDate": "2026-05-30"
  }'
```

#### 5. 患者签到测试（需要Token）
```bash
curl -X PUT http://localhost:8123/appointment/1/confirm \
  -H "Authorization: Bearer <your_token>"
```

#### 6. 医生问诊测试（需要Token）
```bash
curl -X POST http://localhost:8123/consultation \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your_token>" \
  -d '{
    "appointmentId": 1,
    "doctorId": 1,
    "patientId": 1,
    "symptoms": "头痛、发热",
    "diagnosis": "上呼吸道感染",
    "treatment": "口服药物治疗",
    "prescription": "阿莫西林胶囊 0.5g tid"
  }'
```

#### 7. 患者充值测试（需要Token）
```bash
curl -X POST http://localhost:8123/patient/1/recharge \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your_token>" \
  -d '{
    "amount": 1000.00,
    "paymentMethod": "alipay"
  }'
```

#### 8. 患者缴费测试（需要Token）
```bash
curl -X POST http://localhost:8123/patient/1/payment \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your_token>" \
  -d '{
    "consultationId": 1,
    "amount": 200.00,
    "paymentMethod": "account"
  }'
```

#### 9. 获取患者余额（需要Token）
```bash
curl -X GET http://localhost:8123/patient/1/balance \
  -H "Authorization: Bearer <your_token>"
```

#### 9. AI问诊会话测试
```bash
# 创建会话
curl -X POST http://localhost:8123/api/ai-consultation/session \
  -H "Content-Type: application/json" \
  -d '{"chiefComplaint": "头痛发热"}'

# 发送消息
curl -X POST http://localhost:8123/api/ai-consultation/message \
  -H "Content-Type: application/json" \
  -d '{"sessionId": "sess_xxx", "message": "我已经发烧三天了"}'

# 获取消息列表
curl -X GET "http://localhost:8123/api/ai-consultation/session/sess_xxx/messages?page=1&pageSize=20"
```

### 通用响应格式
```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

| code | 说明 |
|------|------|
| 0 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（Token无效或过期） |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the
parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.
