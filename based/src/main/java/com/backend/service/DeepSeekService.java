package com.backend.service;

import com.backend.config.DeepSeekConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class DeepSeekService {

    private final WebClient webClient;
    private final DeepSeekConfig config;
    private final ObjectMapper objectMapper;

    public DeepSeekService(DeepSeekConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024 * 10))
                .build();
    }

    public String sendMessage(String sessionId, String userMessage, List<Map<String, String>> history) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());

        List<Map<String, String>> messages = new ArrayList<>();

        // 系统提示
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一名医生助理，负责根据患者描述的症状和健康问题，给出通俗易懂的日常建议。" +
                "请严格遵循以下规则：\n" +
                "1. 基于对话历史理解患者的完整情况，结合之前的问答进行思考和回复；\n" +
                "2. 每次回答控制在50~200字以内，简洁明了；\n" +
                "3. 用词贴近日常生活，避免使用过多的专业医学术语，让普通人能听懂；\n" +
                "4. 如果症状较轻，给出居家护理建议；如果症状严重或持续不缓解，提醒患者及时就医；\n" +
                "5. 语气温和、耐心，像一位关心患者的家庭医生助理。");
        messages.add(systemMsg);

        // 注入历史对话
        if (history != null && !history.isEmpty()) {
            // 限制上下文长度，取最近 20 轮（40 条消息）
            int maxHistory = Math.min(history.size(), 40);
            int startIdx = history.size() - maxHistory;
            for (int i = startIdx; i < history.size(); i++) {
                Map<String, String> h = history.get(i);
                Map<String, String> historyMsg = new HashMap<>();
                historyMsg.put("role", h.get("role"));
                historyMsg.put("content", h.get("content"));
                messages.add(historyMsg);
            }
        }

        // 当前用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 600);

        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            log.debug("DeepSeek request with {} messages ({} history + 1 current)", messages.size(),
                    history != null ? history.size() : 0);

            Mono<String> responseMono = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(java.time.Duration.ofMillis(config.getTimeout()));

            String response = responseMono.block();
            log.debug("DeepSeek response received");

            if (response != null) {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode choices = rootNode.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null && message.has("content")) {
                        return message.get("content").asText();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error calling DeepSeek API", e);
        }

        return "抱歉，暂时无法获取AI问诊建议，请稍后重试。";
    }

    public String analyzeRisk(String symptoms) {
        String riskPrompt = String.format("请根据以下症状描述评估风险等级（low/medium/high）：%s\n请只返回风险等级：low、medium 或 high", symptoms);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", riskPrompt);
        messages.add(userMsg);
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.1);
        requestBody.put("max_tokens", 10);

        try {
            Mono<String> responseMono = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(java.time.Duration.ofMillis(config.getTimeout()));

            String response = responseMono.block();
            
            if (response != null) {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode choices = rootNode.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null && message.has("content")) {
                        String result = message.get("content").asText().trim().toLowerCase();
                        if (result.contains("high")) {
                            return "high";
                        } else if (result.contains("medium")) {
                            return "medium";
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error calling DeepSeek API for risk analysis", e);
        }
        
        return "medium";
    }

    public String generateSuggestion(String symptoms, String riskLevel) {
        String suggestionPrompt = String.format("症状：%s\n风险等级：%s\n请给出相应的就医建议和健康指导。", symptoms, riskLevel);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", suggestionPrompt);
        messages.add(userMsg);
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 512);

        try {
            Mono<String> responseMono = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(java.time.Duration.ofMillis(config.getTimeout()));

            String response = responseMono.block();
            
            if (response != null) {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode choices = rootNode.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null && message.has("content")) {
                        return message.get("content").asText();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error calling DeepSeek API for suggestion", e);
        }
        
        return "建议及时就医，进行专业诊断。";
    }

    public String formatDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}