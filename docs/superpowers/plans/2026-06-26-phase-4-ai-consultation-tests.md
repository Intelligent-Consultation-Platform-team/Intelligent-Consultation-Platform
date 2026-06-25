# Phase 4 AI 问诊测试实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标：** 为 AI 问诊模块补充确定性强、信号清晰的测试，覆盖服务逻辑、DeepSeek API 行为，以及控制器接口。

**架构：** 将 AI 问诊相关测试按职责拆分：`DeepSeekService` 使用独立的 HTTP / 客户端行为测试，`AiConsultationService` 使用 Mock 的持久层与权限测试，`AiConsultationController` 使用 Mock 的请求/响应映射测试。这样既能保持测试快速、稳定，也能贴合现有代码结构，方便排查问题。

**技术栈：** Java 17、Spring Boot 3.5.13、JUnit 5、Mockito、MockMvc、Jackson、WebFlux `WebClient`、MyBatis-Flex、现有 `UserContext` 辅助类。

## 全局约束

- Spring Boot 3.5.13
- Java 17
- 除非测试暴露出真实缺陷，否则不要修改生产 API 行为
- Phase 4 优先使用快速的单元测试和 Web 层测试，不优先做全量集成测试
- DeepSeek 返回结果必须通过 Mock HTTP 边界保持确定性
- 复用 `TestDataFactory` 中已有的测试数据模式

---

### 任务 1：为 `DeepSeekService` 补充聚焦测试覆盖

**文件：**
- 修改：`Intelligent-Consultstion-Platform/src/test/java/com/backend/service/DeepSeekServiceTest.java`
- 新建：`Intelligent-Consultstion-Platform/src/test/java/com/backend/testsupport/DeepSeekServiceTestConfig.java`（仅在确实需要最小辅助类时使用）

**接口：**
- 输入：`DeepSeekService`、`DeepSeekConfig`、`ObjectMapper`
- 输出：`sendMessage(String, String, List<Map<String, String>>)`、`analyzeRisk(String)`、`generateSuggestion(String, String)`、`formatDateTime()` 的确定性测试

- [ ] **步骤 1：编写会失败的测试**

```java
package com.backend.service;

import com.backend.config.DeepSeekConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DeepSeekServiceTest {

    @Test
    void formatDateTime_returnsExpectedPattern() {
        DeepSeekService service = new DeepSeekService(new DeepSeekConfig(), new ObjectMapper());
        String value = service.formatDateTime();
        assertTrue(value.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }
}
```

- [ ] **步骤 2：运行测试，确认它先失败**

运行：`mvn -Dtest=DeepSeekServiceTest test`
预期：在测试桩补全前，出现编译错误或断言失败。

- [ ] **步骤 3：实现最小测试支架**

创建一个测试夹具，使用受控的 `DeepSeekConfig` 和真实的 `ObjectMapper` 实例化 `DeepSeekService`，再通过 stub HTTP 响应路径扩展 JSON 解析与兜底分支覆盖。

- [ ] **步骤 4：运行测试，确认通过**

运行：`mvn -Dtest=DeepSeekServiceTest test`
预期：通过

- [ ] **步骤 5：提交**

```bash
git add Intelligent-Consultstion-Platform/src/test/java/com/backend/service/DeepSeekServiceTest.java
git commit -m "test: add DeepSeek service coverage for AI consultation"
```

### 任务 2：为 `AiConsultationService` 补充单元测试

**文件：**
- 新建：`Intelligent-Consultstion-Platform/src/test/java/com/backend/service/AiConsultationServiceTest.java`
- 修改：`Intelligent-Consultstion-Platform/src/test/java/com/backend/TestDataFactory.java`（如果需要额外的构造器）

**接口：**
- 输入：`AiConsultationService`、`DeepSeekService`、`AiConsultationSessionMapper`、`AiConsultationMessageMapper`、`UsersMapper`、`UserContext`、`com.backend.model.dto.ai` 下的 DTO
- 输出：会话生命周期、权限校验、分页查询和自动关闭行为的测试覆盖

- [ ] **步骤 1：编写会失败的测试**

```java
package com.backend.service;

import org.junit.jupiter.api.Test;

class AiConsultationServiceTest {

    @Test
    void createSession_persistsActiveSession() {
        // Arrange
        // Act
        // Assert
    }
}
```

- [ ] **步骤 2：运行测试，确认它先失败**

运行：`mvn -Dtest=AiConsultationServiceTest test`
预期：由于测试类尚未存在而失败。

- [ ] **步骤 3：实现最小服务测试**

使用 Mockito Mock 所有 mapper 依赖和 `DeepSeekService`。明确覆盖以下行为：
- `createSession` 会写入 active 会话并返回完整的 `SessionDTO`
- `sendMessage` 会写入用户消息与助手消息、更新风险等级，并拒绝过期或无权限的会话
- `getSessionDetail` 和 `getSessionMessages` 会拒绝无权限访问，并对合法访问返回映射后的 DTO
- `closeSession` 会把会话状态改为 `closed`
- `getMySessions` 会按用户和状态过滤，并支持分页结果
- `getAllSessions` 会复用相同查询流程，并符合管理员访问预期
- `getSessionRisk` 会返回当前风险信息，在没有风险时安全降级

- [ ] **步骤 4：运行测试，确认通过**

运行：`mvn -Dtest=AiConsultationServiceTest test`
预期：通过

- [ ] **步骤 5：提交**

```bash
git add Intelligent-Consultstion-Platform/src/test/java/com/backend/service/AiConsultationServiceTest.java Intelligent-Consultstion-Platform/src/test/java/com/backend/TestDataFactory.java
git commit -m "test: add AI consultation service coverage"
```

### 任务 3：为 `AiConsultationController` 补充控制器测试

**文件：**
- 新建：`Intelligent-Consultstion-Platform/src/test/java/com/backend/controller/AiConsultationControllerTest.java`

**接口：**
- 输入：`AiConsultationController`、`AiConsultationService`、`UserContext`、`com.backend.model.dto.ai` 下的请求 DTO
- 输出：请求绑定、成功响应、错误响应，以及管理员专属路由的端点级验证

- [ ] **步骤 1：编写会失败的测试**

```java
package com.backend.controller;

import org.junit.jupiter.api.Test;

class AiConsultationControllerTest {

    @Test
    void upload_returnsNotImplementedMessage() {
        // Arrange
        // Act
        // Assert
    }
}
```

- [ ] **步骤 2：运行测试，确认它先失败**

运行：`mvn -Dtest=AiConsultationControllerTest test`
预期：由于测试类尚未存在而失败。

- [ ] **步骤 3：实现控制器测试**

使用 `@WebMvcTest(AiConsultationController.class)` 和 `MockMvc`，并将 `AiConsultationService` Mock 掉。覆盖：
- `POST /api/ai-consultation/session`
- `POST /api/ai-consultation/message`
- `GET /api/ai-consultation/session/{sessionId}`
- `GET /api/ai-consultation/session/{sessionId}/messages`
- `POST /api/ai-consultation/session/{sessionId}/close`
- `GET /api/ai-consultation/session/my`
- `GET /api/ai-consultation/session/all`（管理员与非管理员两种情况）
- `GET /api/ai-consultation/session/{sessionId}/risk`
- `POST /api/ai-consultation/upload`

同时显式验证控制器当前的错误映射行为：服务层抛出的运行时异常应当映射到文档约定的响应码，管理员专属路由对非管理员应返回 `403`。

- [ ] **步骤 4：运行测试，确认通过**

运行：`mvn -Dtest=AiConsultationControllerTest test`
预期：通过

- [ ] **步骤 5：提交**

```bash
git add Intelligent-Consultstion-Platform/src/test/java/com/backend/controller/AiConsultationControllerTest.java
git commit -m "test: add AI consultation controller coverage"
```

### 任务 4：验证 Phase 4 覆盖并保持套件稳定

**文件：**
- 修改：`Intelligent-Consultstion-Platform/src/test/java/com/backend/IntelligentConsultstionPlatformApplicationTests.java`（仅在需要轻量上下文冒烟测试时）
- 修改：`Intelligent-Consultstion-Platform/src/test/java/com/backend/TestDataFactory.java`（仅在前面任务需要共享构造器时）

**接口：**
- 输入：新增的三个 AI 问诊测试类
- 输出：通过的 Phase 4 测试套件，以及与计划一致的缺口检查结果

- [ ] **步骤 1：一起运行 AI 问诊相关测试**

运行：`mvn -Dtest=DeepSeekServiceTest,AiConsultationServiceTest,AiConsultationControllerTest test`
预期：通过

- [ ] **步骤 2：运行模块测试子集**

运行：`mvn -Dtest='*AiConsultAiConsultation*Test,*DeepSeek*Test' test`
预期：通过

- [ ] **步骤 3：检查测试报告是否有回归**

检查 Surefire 输出，确认新增测试不依赖外部网络、真实认证状态或不稳定的测试顺序。

- [ ] **步骤 4：提交本阶段**

```bash
git add Intelligent-Consultstion-Platform/src/test/java/com/backend/service/DeepSeekServiceTest.java Intelligent-Consultstion-Platform/src/test/java/com/backend/service/AiConsultationServiceTest.java Intelligent-Consultstion-Platform/src/test/java/com/backend/controller/AiConsultationControllerTest.java
git commit -m "test: complete phase 4 AI consultation coverage"
```

## 需求覆盖检查

- `AiConsultationService` 的会话生命周期、权限、分页和自动关闭行为 → 任务 2
- `DeepSeekService` 的 HTTP 解析、兜底行为、风险分析、建议生成和时间格式化 → 任务 1
- `AiConsultationController` 的路由绑定、成功/错误响应和管理员门禁行为 → 任务 3
- 阶段级稳定性与回归检查 → 任务 4

## 占位符检查

不存在 `TBD`、`TODO` 或含糊不清的实现说明。每一步都明确了文件、命令或断言目标。

## 类型一致性检查

该计划使用了代码库中现有的生产类与方法名：`DeepSeekService`、`AiConsultationService`、`AiConsultationController`、`createSession`、`sendMessage`、`getSessionDetail`、`getSessionMessages`、`closeSession`、`getMySessions`、`getAllSessions` 和 `getSessionRisk`。测试文件名也与目标包结构保持一致。
