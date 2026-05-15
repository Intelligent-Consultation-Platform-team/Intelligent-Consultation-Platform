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

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | /auth/login | 用户登录 |
| POST | /auth/register | 用户注册 |

### 用户模块 `/users`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /users/list | 获取用户列表 |
| POST | /users | 添加用户 |
| PUT | /users | 更新用户信息 |
| DELETE | /users/{userId} | 删除用户 |
| GET | /users/{userId} | 获取用户详情 |

### 科室模块 `/departments`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /departments | 获取科室列表 |
| GET | /departments/{deptId} | 获取科室详情 |
| POST | /departments | 添加科室 |
| PUT | /departments | 更新科室 |
| DELETE | /departments/{deptId} | 删除科室 |

### 医生模块 `/doctors`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /doctors | 获取医生列表 |
| GET | /doctors/{doctorId} | 获取医生详情 |
| POST | /doctors | 添加医生 |
| PUT | /doctors | 更新医生信息 |
| DELETE | /doctors/{doctorId} | 删除医生 |

### 排班模块 `/schedules`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /schedules | 获取排班列表 |
| GET | /schedules/{scheduleId} | 获取排班详情 |
| POST | /schedules | 添加排班 |
| PUT | /schedules | 更新排班信息 |
| DELETE | /schedules/{scheduleId} | 删除排班 |

### 公告模块 `/notices`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /notices | 获取公告列表 |
| GET | /notices/{noticeId} | 获取公告详情 |
| POST | /notices | 添加公告 |
| PUT | /notices | 更新公告 |
| DELETE | /notices/{noticeId} | 删除公告 |

### 预约模块 `/appointments`

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | /appointments | 创建预约 |
| GET | /appointments/patient | 获取患者预约列表 |

### 就诊记录模块 `/consultations`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /consultations | 获取就诊记录列表 |

### 住院管理模块 `/hospitalizations`

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | /hospitalizations | 住院登记 |

### 患者账户模块 `/patientAccount`

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | /recharge | 账户充值 |
| POST | /payment | 费用缴纳 |

---

## Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the
parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.
