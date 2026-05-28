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

    public String sendMessage(String sessionId, String userMessage) {
        String prompt = buildPrompt(sessionId, userMessage);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一名专业的AI医疗问诊助手，需要根据用户描述的症状提供专业的医疗建议。请保持回答专业、准确，并在必要时建议用户及时就医。");
        messages.add(systemMsg);
        
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2048);

        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            log.debug("DeepSeek request body: {}", jsonBody);

            Mono<String> responseMono = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(java.time.Duration.ofMillis(config.getTimeout()));

            String response = responseMono.block();
            log.debug("DeepSeek response: {}", response);

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

    private String buildPrompt(String sessionId, String userMessage) {
        return String.format("会话ID: %s\n用户症状描述: %s\n请根据以上信息，提供专业的医疗建议和健康指导。", 
                sessionId, userMessage);
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