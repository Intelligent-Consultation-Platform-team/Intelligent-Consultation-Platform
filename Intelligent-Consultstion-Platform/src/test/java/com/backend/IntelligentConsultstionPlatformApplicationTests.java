package com.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 应用启动集成测试。
 * <p>
 * 验证 Spring Boot 应用在 test profile（H2 内存库）下能否正常启动，
 * 确保各 Bean、MyBatis-Flex、Web 配置等基础设施无冲突。
 */
@SpringBootTest
@ActiveProfiles("test")
class IntelligentConsultstionPlatformApplicationTests {

    /** 测试 Spring 上下文能否成功加载（冒烟测试） */
    @Test
    void contextLoads() {
    }
}
