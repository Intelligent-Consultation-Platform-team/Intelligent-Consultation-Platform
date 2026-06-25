# 智能问诊平台测试计划（TestPlan）

> 版本：v1.0  
> 日期：2026-06-26  
> 项目：Intelligent-Consultstion-Platform（Spring Boot 3.5 + Vue 3 + MySQL）

---

## 1. 文档目的

本计划为智能问诊平台从零搭建自动化测试体系提供蓝图，目标是在**保证测试质量**的前提下**覆盖大部分业务代码**，形成可回归、可度量、可持续维护的测试基线。

**当前基线：**

| 层级 | 现状 |
|------|------|
| 后端 | `src/test/` 仅有空的 `IntelligentConsultstionPlatformApplicationTests.java`，无可运行用例 |
| 前端 | 无 Vitest/Jest/Cypress/Playwright，无 `*.test.js` / `*.spec.vue` |
| 依赖 | 仅有 `spring-boot-starter-test`，无 H2/Testcontainers/WireMock |

---

## 2. 测试目标与质量指标

### 2.1 总体目标

1. **后端**：Service 层核心业务逻辑覆盖率 ≥ **80%**；Controller 层 API 集成测试覆盖全部 **65** 个端点中的 **P0/P1** 路径（约 45 个）。
2. **前端**：工具层（`request.js`、`session.js`、路由守卫）单元测试覆盖率 ≥ **90%**；核心 E2E 场景覆盖 **10** 条主流程。
3. **回归**：CI 中 `mvn test` 与前端 `npm test` 可在 **5 分钟内**完成（不含 E2E）。
4. **缺陷预防**：重点覆盖预约状态机、支付余额、住院冲突、JWT 鉴权四类高风险逻辑。

### 2.2 覆盖率目标（JaCoCo / Vitest coverage）

| 包/模块 | 行覆盖率目标 | 优先级 |
|---------|-------------|--------|
| `utils/`（JwtUtils, PasswordUtils） | ≥ 95% | P0 |
| `interceptor/`（TokenInterceptor） | ≥ 90% | P0 |
| `exception/`（GlobalExceptionHandler） | ≥ 85% | P0 |
| `service/impl/`（9 个实现类） | ≥ 80% | P0 |
| `service/`（AiConsultationService, DeepSeekService） | ≥ 75% | P1 |
| `controller/`（11 个控制器） | 端点 P0/P1 100% | P0 |
| `front/src/utils/` | ≥ 90% | P1 |
| `front/src/router/` | 守卫逻辑 100% | P1 |

### 2.3 退出标准（Definition of Done）

- [ ] 后端至少 **120** 个 `@Test` 方法，全部通过
- [ ] 前端至少 **30** 个单元测试 + **10** 条 E2E 场景
- [ ] JaCoCo 报告整体行覆盖率 ≥ **70%**（含 controller + service + utils）
- [ ] 无 P0 模块未覆盖的已知业务规则（见第 6 节）
- [ ] README/AGENTS.md 补充测试运行说明

---

## 3. 测试策略

### 3.1 测试金字塔

```
                    ┌─────────────┐
                    │  E2E (10)   │  Playwright — 角色主流程
                   ┌┴─────────────┴┐
                   │ API 集成 (45) │  MockMvc + Testcontainers MySQL
                  ┌┴───────────────┴┐
                  │ Service 单元(60)│  @ExtendWith(Mockito) + Mapper Mock
                 ┌┴─────────────────┴┐
                 │ Utils/组件 (35)   │  纯单元，无 Spring 上下文
                 └───────────────────┘
```

### 3.2 分层职责

| 层级 | 框架 | 测什么 | 不测什么 |
|------|------|--------|----------|
| **L1 单元** | JUnit 5 + Mockito | 工具类、纯逻辑、Service 业务规则（Mock Mapper） | 数据库 IO、HTTP |
| **L2 集成** | `@SpringBootTest` + MockMvc | Controller 端到端（真实 Spring 容器 + 测试 DB） | 前端 UI |
| **L3 组件** | `@WebMvcTest` | TokenInterceptor、GlobalExceptionHandler 隔离测试 | 完整业务流程 |
| **L4 E2E** | Playwright | 患者/医生/管理员跨页面流程 | 单个函数细节 |

### 3.3 测试环境

#### 后端 `application-test.yml`（待创建）

```yaml
spring:
  datasource:
    url: jdbc:tc:mysql:8.0:///hospital   # Testcontainers 动态端口
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
  sql:
    init:
      mode: always
      schema-locations: classpath:sql/test-schema.sql
      data-locations: classpath:sql/test-data.sql

deepseek:
  api-key: test-key-mock
  base-url: http://localhost:${wiremock.server.port}  # WireMock 桩
  model: deepseek-chat
  timeout: 5000

jwt:
  secret: TestSecretKeyForJWTUnitTests2024!
```

#### 测试数据原则

1. **不使用** `sql/database.sql` 种子数据（密码为 MD5，与 BCrypt 不兼容）。
2. 新建 `src/test/resources/sql/test-data.sql`，所有用户密码统一为 BCrypt 哈希的 `123456`。
3. 每个集成测试类使用 `@Transactional` + `@Rollback`，或 `@Sql` 按类重置数据。
4. 固定测试账号：

| 角色 | 用户名 | 密码 | user_id（雪花 ID 在 test-data 中固定） |
|------|--------|------|----------------------------------------|
| patient | test_patient | 123456 | 1001 |
| doctor | test_doctor | 123456 | 1002 |
| admin | test_admin | 123456 | 1003 |

#### 外部依赖 Mock 策略

| 依赖 | 策略 |
|------|------|
| DeepSeek API | WireMock 桩返回固定 JSON；`DeepSeekService` 单元测试直接 Mock WebClient |
| MySQL | Testcontainers `mysql:8.0`（集成测试）；H2 仅用于纯 Mapper 测试（可选） |
| JWT 时间 | 单元测试注入固定 `Clock` 或使用短过期 token |

### 3.4 需新增的 Maven 依赖

```xml
<!-- Testcontainers -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <scope>test</scope>
</dependency>
<!-- WireMock for DeepSeek -->
<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-standalone</artifactId>
    <version>3.9.1</version>
    <scope>test</scope>
</dependency>
<!-- JaCoCo 插件（build 段） -->
```

### 3.5 需新增的前端依赖

```json
{
  "devDependencies": {
    "vitest": "^3.x",
    "@vue/test-utils": "^2.x",
    "jsdom": "^25.x",
    "@playwright/test": "^1.x"
  },
  "scripts": {
    "test": "vitest run",
    "test:coverage": "vitest run --coverage",
    "test:e2e": "playwright test"
  }
}
```

---

## 4. 测试目录结构（目标）

```
Intelligent-Consultstion-Platform/
├── TestPlan.md                          ← 本文档
├── src/test/
│   ├── java/com/backend/
│   │   ├── IntelligentConsultstionPlatformApplicationTests.java  # 上下文加载
│   │   ├── TestDataFactory.java         # 测试数据构建器
│   │   ├── BaseIntegrationTest.java     # Testcontainers + MockMvc 基类
│   │   ├── utils/
│   │   │   ├── JwtUtilsTest.java
│   │   │   └── PasswordUtilsTest.java
│   │   ├── interceptor/
│   │   │   └── TokenInterceptorTest.java
│   │   ├── exception/
│   │   │   └── GlobalExceptionHandlerTest.java
│   │   ├── service/
│   │   │   ├── UsersServiceImplTest.java
│   │   │   ├── AppointmentsServiceImplTest.java
│   │   │   ├── ConsultationsServiceImplTest.java
│   │   │   ├── PatientAccountServiceImplTest.java
│   │   │   ├── HospitalizationsServiceImplTest.java
│   │   │   ├── DoctorsServiceImplTest.java
│   │   │   ├── DepartmentsServiceImplTest.java
│   │   │   ├── SchedulesServiceImplTest.java
│   │   │   ├── NoticesServiceImplTest.java
│   │   │   ├── AiConsultationServiceTest.java
│   │   │   └── DeepSeekServiceTest.java
│   │   └── controller/
│   │       ├── AuthControllerTest.java
│   │       ├── AppointmentsControllerTest.java
│   │       ├── ConsultationsControllerTest.java
│   │       ├── PatientAccountControllerTest.java
│   │       ├── HospitalizationsControllerTest.java
│   │       ├── AiConsultationControllerTest.java
│   │       └── CrudControllersTest.java   # departments/doctors/schedules/users/notices
│   └── resources/
│       ├── application-test.yml
│       └── sql/
│           ├── test-schema.sql            # 自 hospital.sql 精简
│           └── test-data.sql
└── front/
    ├── vitest.config.js
    ├── playwright.config.js
    ├── tests/
    │   ├── unit/
    │   │   ├── session.test.js
    │   │   ├── request.test.js
    │   │   └── router-guard.test.js
    │   └── e2e/
    │       ├── auth.spec.js
    │       ├── appointment-flow.spec.js
    │       ├── payment-flow.spec.js
    │       ├── ai-consultation.spec.js
    │       └── admin-crud.spec.js
    └── mocks/
        └── handlers.js                    # MSW 或 Playwright route mock
```

---

## 5. 实施阶段

| 阶段 | 内容 | 预估用例数 | 工期 |
|------|------|-----------|------|
| **Phase 0** | 测试基础设施（application-test.yml、Testcontainers、test-data.sql、BaseIntegrationTest） | 2 | 0.5 天 |
| **Phase 1** | Utils + Interceptor + Exception 单元测试 | 25 | 0.5 天 |
| **Phase 2** | Service 层单元测试（Mock Mapper） | 60 | 2 天 |
| **Phase 3** | Controller API 集成测试（P0 业务流） | 45 | 2 天 |
| **Phase 4** | AI 问诊 + DeepSeek Mock | 15 | 1 天 |
| **Phase 5** | 前端 Vitest 单元测试 | 30 | 1 天 |
| **Phase 6** | Playwright E2E | 10 场景 | 1.5 天 |
| **Phase 7** | JaCoCo 报告 + CI 脚本 + 文档 | — | 0.5 天 |

**合计：约 187 个测试方法/场景，9 个工作日。**

---

## 6. 详细测试用例设计

### 6.1 工具层（P0）

#### JwtUtilsTest

| ID | 用例 | 输入 | 期望 |
|----|------|------|------|
| JWT-01 | 生成 token 含正确 claims | userId=1001, username, role=patient | extractUserId/Username/Role 一致 |
| JWT-02 | 有效 token 校验通过 | 刚生成的 token | validateToken=true |
| JWT-03 | 过期 token 校验失败 | 手动构造过期 token | validateToken=false |
| JWT-04 | 篡改 token 校验失败 | 修改 payload 后重签 | validateToken=false |
| JWT-05 | 错误 secret 校验失败 | 不同 secret 签名的 token | validateToken=false |

#### PasswordUtilsTest

| ID | 用例 | 期望 |
|----|------|------|
| PWD-01 | encrypt 同一密码两次 hash 不同 | 两次结果不相等（salt） |
| PWD-02 | verify 正确密码 | true |
| PWD-03 | verify 错误密码 | false |
| PWD-04 | verify 空密码 | false |

#### TokenInterceptorTest

| ID | 用例 | 期望 |
|----|------|------|
| INT-01 | 白名单 `/auth/login` 无 token 放行 | 200，不写入 UserContext |
| INT-02 | 非白名单无 token | JSON code=40100 |
| INT-03 | 有效 Bearer token | UserContext 写入 userId/username/role |
| INT-04 | 过期 token | JSON code=40100 |
| INT-05 | afterCompletion 清理 ThreadLocal | UserContext.get() 为 null |

---

### 6.2 认证模块 Auth（P0）

#### UsersServiceImplTest（单元）

| ID | 用例 | 期望 |
|----|------|------|
| AUTH-S01 | 注册成功（patient） | 创建 users + patients 记录 |
| AUTH-S02 | 用户名重复 | BusinessException USERNAME_EXISTS |
| AUTH-S03 | 登录成功（用户名） | 返回 token |
| AUTH-S04 | 登录成功（手机号） | 返回 token |
| AUTH-S05 | 密码错误 | USER_OR_PASSWORD_ERROR |
| AUTH-S06 | 用户 status=inactive | 登录失败 |
| AUTH-S07 | resetPassword | 新密码 BCrypt 可 verify |

#### AuthControllerTest（集成）

| ID | 用例 | HTTP | 期望 |
|----|------|------|------|
| AUTH-C01 | POST /auth/register 正常注册 | 201/200, code=0 | |
| AUTH-C02 | POST /auth/register 缺字段 | code=40001 | |
| AUTH-C03 | POST /auth/login 正确凭证 | 返回 token + expiresIn | |
| AUTH-C04 | GET /auth/me 带 token | 返回当前用户 | |
| AUTH-C05 | GET /auth/me 无 token | code=40100 | |
| AUTH-C06 | PUT /auth/profile 更新昵称 | 数据库已更新 | |

---

### 6.3 预约模块 Appointments（P0 — 最高优先级）

**状态机（必须全覆盖）：**

```
pending ──confirm──► confirmed ──process──► processing ──completeConsultation──► unpaid ──payment──► completed
   │                      │                      │
   └──── cancel ──────────┴──── cancel ──────────┘
                              cancelled
```

#### AppointmentsServiceImplTest

| ID | 用例 | 前置 | 期望 |
|----|------|------|------|
| APT-S01 | 创建预约成功 | 有效排班、未来日期、有号源 | status=pending, available_slots-1 |
| APT-S02 | 排班 day_of_week 不匹配 | 日期与排班 weekday 不符 | PARAMS_ERROR |
| APT-S03 | 预约日期超过 15 天 | date > today+15 | PARAMS_ERROR |
| APT-S04 | 号源为 0 | available_slots=0 | OPERATION_ERROR |
| APT-S05 | 同患者同排班同日复预约 | 已有 pending 预约 | OPERATION_ERROR |
| APT-S06 | confirm 签到 | status=pending | status=confirmed |
| APT-S07 | process 接诊 | status=confirmed | status=processing, 创建 consultation |
| APT-S08 | process 非 confirmed 状态 | status=pending | OPERATION_ERROR |
| APT-S09 | cancel 取消 | status=pending/confirmed | status=cancelled, 号源+1 |
| APT-S10 | cancel 已 processing | status=processing | OPERATION_ERROR |
| APT-S11 | complete 完成就诊 | status=processing | 关联 consultation, appointment→unpaid |
| APT-S12 | 医生只能看自己的出诊列表 | doctorId 过滤 | 仅返回该医生预约 |

#### AppointmentsControllerTest

| ID | 用例 | 端点 | 期望 |
|----|------|------|------|
| APT-C01 | 患者创建预约 | POST /appointment | 201 |
| APT-C02 | 患者查看自己的预约 | GET /appointment/patient/{id} | 列表非空 |
| APT-C03 | 医生接诊 | PUT /appointment/{id}/process | status=processing |
| APT-C04 | 患者签到 | PUT /appointment/{id}/confirm | status=confirmed |
| APT-C05 | 取消预约 | PUT /appointment/{id}/cancel | status=cancelled |
| APT-C06 | 分页查询带 status 筛选 | GET /appointment?status=pending | 过滤正确 |
| APT-C07 | 非本人 patientId 越权 | 其他患者 token | NO_AUTH_ERROR |

---

### 6.4 就诊与支付 Consultation + Payment（P0）

#### ConsultationsServiceImplTest

| ID | 用例 | 期望 |
|----|------|------|
| CON-S01 | 创建问诊记录 | 关联 appointment, status=processing |
| CON-S02 | 更新诊断/处方 | 字段持久化 |
| CON-S03 | completeConsultation 未填 amount | 默认 amount=50.00 |
| CON-S04 | completeConsultation | appointment→unpaid, 创建 payment_records(unpaid) |
| CON-S05 | 重复 complete | OPERATION_ERROR |

#### PatientAccountServiceImplTest

| ID | 用例 | 前置 | 期望 |
|----|------|------|------|
| PAY-S01 | recharge 充值 | balance=100 | balance=200, recharge_records 新增 |
| PAY-S02 | payment 账户余额充足 | balance≥amount, unpaid 账单 | balance 减少, payment paid, appointment→completed |
| PAY-S03 | payment 余额不足 | balance<amount | OPERATION_ERROR |
| PAY-S04 | payment 重复缴费 | 已 paid | OPERATION_ERROR |
| PAY-S05 | getBalance | — | 返回正确余额 |
| PAY-S06 | getJourney | 患者有多条预约/就诊 | 聚合数据完整 |
| PAY-S07 | searchPatients 按姓名 | name 模糊 | 匹配列表 |

#### PatientAccountControllerTest + ConsultationsControllerTest

| ID | 用例 | 端点 | 期望 |
|----|------|------|------|
| PAY-C01 | 充值 | POST /patient/{id}/recharge | balance 增加 |
| PAY-C02 | 缴费 | POST /patient/{id}/payment | 全流程 completed |
| PAY-C03 | 获取 journey | GET /patient/journey | 含 appointments + consultations |
| PAY-C04 | 完成就诊 | PUT /consultation/{id}/complete | unpaid 状态 |
| PAY-C05 | 交易记录 | GET /patient/records | recharge + payment 列表 |

---

### 6.5 住院模块 Hospitalization（P1）

#### HospitalizationsServiceImplTest

| ID | 用例 | 期望 |
|----|------|------|
| HOS-S01 | 住院登记成功 | status=admitted |
| HOS-S02 | 患者已在院 | 已有 admitted 记录 | OPERATION_ERROR |
| HOS-S03 | 床位已被占用 | 同 ward+bed | OPERATION_ERROR |
| HOS-S04 | discharge 出院 | status=discharged, discharge_date 非空 |
| HOS-S05 | isPatientAdmitted true/false | — | 布尔正确 |

#### HospitalizationsControllerTest

| ID | 用例 | 端点 | 期望 |
|----|------|------|------|
| HOS-C01 | POST /hospitalization | 201 |
| HOS-C02 | GET /hospitalization/patient/{id}/active | admitted 时 true |
| HOS-C03 | PUT /hospitalization/{id}/discharge | discharged |

---

### 6.6 AI 问诊模块（P1）

#### AiConsultationServiceTest

| ID | 用例 | Mock | 期望 |
|----|------|------|------|
| AI-S01 | createSession | — | DB 写入 session, status=active |
| AI-S02 | sendMessage | DeepSeek 返回固定回复 | user+assistant 两条 message |
| AI-S03 | closeSession | — | status=closed |
| AI-S04 | getMySessions | — | 仅当前用户 |
| AI-S05 | getAllSessions 非 admin | — | NO_AUTH_ERROR |
| AI-S06 | getSessionRisk | 含关键词消息 | risk_level 非空 |
| AI-S07 | 会话 5 分钟无活动 | 模拟时间 | 自动 closed |

#### AiConsultationControllerTest

| ID | 用例 | 端点 | 期望 |
|----|------|------|------|
| AI-C01 | POST /api/ai-consultation/session | sessionId 返回 |
| AI-C02 | POST /api/ai-consultation/message | 200 + 回复内容 |
| AI-C03 | GET /api/ai-consultation/session/my | 列表 |
| AI-C04 | POST /api/ai-consultation/session/{id}/close | closed |
| AI-C05 | POST /api/ai-consultation/upload | 暂未实现提示 |

---

### 6.7 管理 CRUD 模块（P2）

对 `Departments`、`Doctors`、`Schedules`、`Notices`、`Users` 各覆盖：

| 通用用例 | 说明 |
|----------|------|
| CRUD-01 | GET 列表非空（test-data 有种子） |
| CRUD-02 | POST 创建成功，返回 ID |
| CRUD-03 | PUT 更新字段生效 |
| CRUD-04 | DELETE 逻辑删除/物理删除后 GET 404 |
| CRUD-05 | GET by ID 不存在 → NOT_FOUND_ERROR |
| CRUD-06 | 非 admin 角色调用管理接口 → NO_AUTH_ERROR |

**Doctors 专项：**

| ID | 用例 | 期望 |
|----|------|------|
| DOC-S01 | addDoctor 同时创建 users + doctors | 两表记录 |
| DOC-S02 | getDoctorsWithUserInfo | 含 username、deptName |

**Schedules 专项：**

| ID | 用例 | 期望 |
|----|------|------|
| SCH-S01 | getDoctorSchedules 按 deptId/doctorId/date 过滤 | 结果正确 |
| SCH-S02 | addSchedule 时间冲突检测（若有） | 或验证成功创建 |

---

### 6.8 异常与边界（P0）

#### GlobalExceptionHandlerTest

| ID | 用例 | 期望 |
|----|------|------|
| EX-01 | BusinessException | code + message 来自 ErrorCode |
| EX-02 | MethodArgumentNotValidException | code=40001 |
| EX-03 | 未知 Exception | code=50000, 不泄露堆栈 |

#### 跨模块边界

| ID | 用例 | 期望 |
|----|------|------|
| BND-01 | 雪花 ID 格式错误路径参数 | 400 或 NOT_FOUND |
| BND-02 | 分页 current=0 或 size=0 | PARAMS_ERROR 或默认值 |
| BND-03 | SQL 注入尝试（keyword 参数） | 无异常，无注入 |
| BND-04 | 并发预约同一号源（可选） | 仅一个成功 |

---

## 7. 前端测试用例

### 7.1 单元测试（Vitest）

#### session.test.js

| ID | 用例 | 期望 |
|----|------|------|
| FE-S01 | setSession + getSession 往返 | 数据一致 |
| FE-S02 | isExpired 未过期 | false |
| FE-S03 | isExpired 已过期 | true, clearSession 被调用 |
| FE-S04 | parse 非法 JSON | 不抛错，返回 null |

#### request.test.js

| ID | 用例 | 期望 |
|----|------|------|
| FE-R01 | 默认请求带 Authorization 头 | Bearer token |
| FE-R02 | withAuth: false 不带 token | 无 Authorization |
| FE-R03 | 响应 code≠0 | 抛出含 message 的错误 |
| FE-R04 | HTTP 401 | 触发登出/跳转逻辑 |

#### router-guard.test.js

| ID | 用例 | 期望 |
|----|------|------|
| FE-G01 | 未登录访问 /manage | redirect /auth/login |
| FE-G02 | patient 访问 /appoint/visit | redirect /home |
| FE-G03 | 已登录访问 /auth/login | redirect /home |
| FE-G04 | admin 访问 /manage/notice | 放行 |

### 7.2 E2E 测试（Playwright）

| ID | 场景 | 角色 | 步骤摘要 | 断言 |
|----|------|------|----------|------|
| E2E-01 | 注册登录 | 新 patient | 注册→登录→见 Home | URL=/home, 侧边栏可见 |
| E2E-02 | 预约挂号 | patient | 选科室→选排班→提交 | MyJourney 出现 pending |
| E2E-03 | 取消预约 | patient | MyJourney 点取消 | status=cancelled |
| E2E-04 | 医生接诊 | doctor | PatientBook 点接诊 | Visit 页可见该患者 |
| E2E-05 | 完成就诊 | doctor | 填诊断→完成 | 患者端 unpaid |
| E2E-06 | 充值缴费 | patient | Recharge 100→MyJourney 缴费 | completed |
| E2E-07 | 余额不足充值并付 | patient | 余额 0→弹窗充值+付 | completed |
| E2E-08 | 住院登记出院 | doctor | Visit 建议住院→Hospital 登记→出院 | admitted→discharged |
| E2E-09 | AI 问诊 | patient | 输入主诉→聊天→关闭 | history 有记录 |
| E2E-10 | 管理员 CRUD | admin | 新增公告→编辑→删除 | 列表变化正确 |

**E2E 前置：** Playwright `globalSetup` 调用后端测试 API 或 SQL 脚本初始化 test-data；`baseURL=http://localhost:3000`，API mock 或真实后端 `:8123`。

---

## 8. 测试辅助设施

### 8.1 TestDataFactory（Java）

```java
// 示例：统一构建 AppointmentDTO、Users 等，避免各测试类重复造数据
public class TestDataFactory {
    public static UserRegisterRequest patientRegisterRequest();
    public static AppointmentDTO validAppointment(Long patientId, Long scheduleId);
    public static String bearerToken(String role); // 按角色签发 JWT
}
```

### 8.2 BaseIntegrationTest（Java）

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");
    @Autowired protected MockMvc mockMvc;
    protected String tokenAs(String role) { ... }
}
```

### 8.3 前端 Mock

- E2E 中 DeepSeek 相关：Playwright `page.route()` 拦截 `/api/ai-consultation/message`
- 单元测试：`vi.mock('../utils/request.js')` 隔离 fetch

---

## 9. CI 集成建议

```yaml
# .github/workflows/test.yml（建议）
jobs:
  backend-test:
    runs-on: ubuntu-latest
    services:
      mysql: ...
    steps:
      - run: mvn test -Ptest
      - run: mvn jacoco:report
      - uses: codecov/codecov-action@v4

  frontend-test:
    steps:
      - run: npm ci && npm test
      - run: npx playwright install --with-deps && npm run test:e2e
```

本地快速验证：

```bash
# 后端
cd Intelligent-Consultstion-Platform
mvnw test

# 前端
cd front
npm test
npm run test:e2e
```

---

## 10. 风险与已知缺口

| 风险 | 影响 | 缓解 |
|------|------|------|
| 种子 SQL 密码 MD5 vs BCrypt | 集成测试登录失败 | 专用 test-data.sql |
| DeepSeek 外部 API | 测试不稳定/收费 | WireMock 100% Mock |
| Testcontainers 需 Docker | CI/本地无 Docker 失败 | 文档说明；可选 H2 降级 profile |
| `appointment.confirm` 无前端入口 | E2E 无法覆盖签到 UI | API 集成测试覆盖；记为 UI 缺口 |
| `Payment.vue` 仅 redirect | E2E 跳过该路由 | 在 MyJourney 测缴费 |
| target/ 编译产物被 git 跟踪 | 测试污染 working tree | .gitignore 补充 target/ |
| 雪花 ID 非确定性 | 断言 ID 需灵活 | 测试数据用固定 ID 或断言非 null |

---

## 11. 覆盖率追踪

实施完成后按模块填写：

| 模块 | 目标 | 实际 | 状态 |
|------|------|------|------|
| utils | 95% | — | 待实施 |
| interceptor | 90% | — | 待实施 |
| service/impl | 80% | — | 待实施 |
| controller | P0/P1 100% | — | 待实施 |
| front/utils | 90% | — | 待实施 |
| **整体** | **70%** | — | 待实施 |

JaCoCo 报告路径：`target/site/jacoco/index.html`

---

## 12. 下一步行动

1. **确认本计划**：评审 Phase 划分与优先级是否符合团队预期。
2. **Phase 0 实施**：创建 `application-test.yml`、Testcontainers 基类、BCrypt 测试数据。
3. **按 Phase 1→7 迭代**：每阶段结束运行全量测试并更新第 11 节覆盖率表。
4. **可选**：将 Phase 1–4 拆为独立 PR，便于 Code Review。

---

*本计划由代码库静态分析生成，覆盖 11 Controller / 65 端点 / 11 Service / 14 数据库表 / 22 前端路由。*
