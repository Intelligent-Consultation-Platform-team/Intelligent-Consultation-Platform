package com.backend.service;

import com.backend.config.DeepSeekConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DeepSeek 服务测试。
 * <p>
 * 覆盖 AI 问诊底层客户端的兜底行为、风险分析默认值，以及日期格式输出。
 */
class DeepSeekServiceTest {

    private DeepSeekService newService() {
        DeepSeekConfig config = new DeepSeekConfig();
        config.setApiKey("test-key");
        config.setBaseUrl("http://127.0.0.1:65535");
        config.setModel("deepseek-chat");
        config.setTimeout(200);
        return new DeepSeekService(config, new ObjectMapper());
    }

    /** 验证格式化时间输出符合 yyyy-MM-dd HH:mm:ss 规则。 */
    @Test
    void formatDateTime_returnsExpectedPattern() {
        String value = newService().formatDateTime();
        assertTrue(value.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    /** 验证 DeepSeek 不可用时，问诊回复会返回兜底文案。 */
    @Test
    void sendMessage_whenApiUnavailable_returnsFallbackMessage() {
        String result = newService().sendMessage("session-1", "头痛", Collections.emptyList());
        assertEquals("抱歉，暂时无法获取AI问诊建议，请稍后重试。", result);
    }

    /** 验证风险评估接口失败时，默认返回 medium 风险等级。 */
    @Test
    void analyzeRisk_whenApiUnavailable_defaultsToMedium() {
        String result = newService().analyzeRisk("头痛");
        assertEquals("medium", result);
    }

    /** 验证建议生成接口失败时，返回固定的就医兜底建议。 */
    @Test
    void generateSuggestion_whenApiUnavailable_returnsFallbackSuggestion() {
        String result = newService().generateSuggestion("头痛", "medium");
        assertEquals("建议及时就医，进行专业诊断。", result);
    }

    /** 验证传入空历史记录时，消息发送逻辑仍能正常执行兜底分支。 */
    @Test
    void sendMessage_handlesNullHistoryWithoutFailure() {
        String result = newService().sendMessage("session-2", "咳嗽", null);
        assertEquals("抱歉，暂时无法获取AI问诊建议，请稍后重试。", result);
    }
}
