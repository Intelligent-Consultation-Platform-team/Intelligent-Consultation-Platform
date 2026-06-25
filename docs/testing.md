# 测试指南

## 概览

本项目目前包含后端单元测试与集成测试、前端 Vitest 单元测试，以及 Playwright 端到端测试。

## 后端测试

在 `Intelligent-Consultstion-Platform` 目录下运行后端测试：

```bash
mvn test
```

如果只想运行后端 AI 问诊相关的测试子集，可以执行：

```bash
mvn "-Dtest=DeepSeekServiceTest,AiConsultationServiceTest,AiConsultationControllerTest" test
```

执行 `mvn test` 时会自动生成 JaCoCo 覆盖率报告，报告路径如下：

```text
target/site/jacoco/index.html
```

## 前端单元测试

在 `front` 目录下运行 Vitest 测试：

```bash
npm test
```

生成覆盖率报告：

```bash
npm run test:coverage
```

## 前端 E2E 测试

在 `front` 目录下运行 Playwright 测试：

```bash
npm run test:e2e
```

E2E 测试依赖本地前端开发服务，并且在测试用例中通过路由拦截模拟接口返回。

## 说明

- 前端单元测试使用 `jsdom` 环境。
- E2E 测试使用 `@playwright/test`，默认运行 Chromium 项目。
- 前端测试用例中使用的登录会话键名为 `demo_session`。
