# 智能问诊平台前端

## 项目简介

智能问诊平台前端项目，基于 Vue 3 + Element Plus 开发，提供完整的医疗服务管理界面，包括用户注册登录、AI问诊、预约挂号、就诊管理等功能。

## 技术栈

| 组件 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue | 3.4.27 |
| UI组件 | Element Plus | 2.7.6 |
| 路由 | Vue Router | 4.6.4 |
| 构建工具 | Vite | 5.4.21 |
| HTTP客户端 | Fetch API | - |

## 项目结构

```
src/
├── App.vue                        # 主应用组件
├── main.js                        # 入口文件
├── style.css                      # 全局样式
├── router/
│   └── index.js                   # 路由配置（18条路由）
├── api/
│   └── aiConsultation.js          # AI问诊API
├── utils/
│   ├── request.js                 # HTTP请求封装
│   ├── api.js                     # 业务API封装
│   └── session.js                 # 会话管理
├── components/
│   └── aiConsultation/            # AI问诊组件
│       ├── ConsultationChatWindow.vue    # 聊天窗口
│       ├── ConsultationInputBar.vue      # 输入栏
│       ├── ConsultationMessageItem.vue   # 消息项
│       ├── ConsultationRiskCard.vue      # 风险卡片
│       └── ConsultationHistoryList.vue   # 历史列表
└── views/
    ├── Home.vue                   # 系统首页
    ├── Appoint.vue                # 预约就诊（布局）
    ├── Manage.vue                 # 信息管理（布局）
    ├── User.vue                   # 用户管理（布局）
    ├── aiConsultation/            # AI问诊页面
    │   ├── index.vue              # 问诊首页
    │   ├── chat.vue               # 聊天对话
    │   ├── history.vue            # 问诊历史
    │   └── detail.vue             # 会话详情
    ├── appoint/                   # 预约就诊页面
    │   ├── Book.vue               # 预约挂号（患者）
    │   ├── MyJourney.vue          # 我的流程（患者）
    │   ├── PatientBook.vue        # 患者挂号（医生/管理员）
    │   ├── Visit.vue              # 患者就诊（医生）
    │   ├── Hospital.vue           # 住院登记（医生/管理员）
    │   └── Payment.vue            # 缴纳费用（患者）
    ├── manage/                    # 信息管理页面
    │   ├── Notice.vue             # 公告信息
    │   ├── Office.vue             # 科室信息
    │   └── Schedule.vue           # 医生排班
    └── user/                      # 用户管理页面
        ├── Profile.vue            # 个人信息
        ├── Recharge.vue           # 账户充值（患者）
        ├── Admin.vue              # 管理员信息
        ├── Doctor.vue             # 医生信息
        ├── UserInfo.vue           # 用户信息
        └── PatientAccount.vue     # 患者账户
```

## 功能特性

- ✅ 用户注册（支持患者、医生、管理员角色）
- ✅ 用户登录（JWT认证）
- ✅ 基于角色的权限控制
- ✅ AI智能问诊（对话、风险评估、建议）
- ✅ 预约挂号系统
- ✅ 就诊流程管理
- ✅ 住院登记管理
- ✅ 账户充值与缴费
- ✅ 响应式界面设计
- ✅ 表单验证
- ✅ 路由守卫

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问地址：http://localhost:3000

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 环境配置

### .env 文件

```bash
# API 基础地址（开发环境）
VITE_API_BASE_URL=http://localhost:8123

# 开发服务器端口
VITE_DEV_PORT=3000
```

### 代理配置

Vite 已配置自动代理，以下路径自动转发到后端：

- `/auth`
- `/departments`
- `/doctors`
- `/schedules`
- `/appointments`
- `/consultations`
- `/hospitalizations`
- `/notices`
- `/recharge`
- `/payment`
- `/users`
- `/api/ai-consultation`

## 角色权限

### 患者角色
- 系统首页
- AI问诊（全部功能）
- 预约挂号
- 我的流程
- 缴纳费用
- 个人信息
- 账户充值

### 医生角色
- 系统首页
- AI问诊（全部功能）
- 患者挂号
- 患者就诊
- 住院登记
- 个人信息

### 管理员角色
- 系统首页
- AI问诊（全部功能）
- 信息管理（公告、科室、排班）
- 预约就诊（患者挂号、住院登记）
- 用户管理（管理员、医生、用户）

## API 文档

详细的 API 接口说明请查看 [API.md](API.md) 文件。

后端 API 文档：http://localhost:8123/swagger-ui/index.html

## 注意事项

- 前端依赖后端服务，请确保后端服务已启动（端口 8123）
- 注册时可选择患者、医生角色，管理员角色需要通过后台创建
- 会话信息存储在 localStorage 中，登录状态持久化
- 401 状态码会自动跳转到登录页

## 许可证

MIT License