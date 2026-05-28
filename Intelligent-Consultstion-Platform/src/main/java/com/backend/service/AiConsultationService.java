package com.backend.service;

import com.backend.model.dto.ai.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
@Service
public class AiConsultationService {

    private final DeepSeekService deepSeekService;

    private final Map<String, SessionData> sessionStorage = new ConcurrentHashMap<>();

    public AiConsultationService(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    private static class SessionData {
        String sessionId;
        String status;
        String createdAt;
        String chiefComplaint;
        List<String> symptomTags;
        Integer age;
        String gender;
        Deque<MessageDTO> messages;
        String lastSymptoms;

        SessionData(String sessionId, String chiefComplaint, List<String> symptomTags, Integer age, String gender) {
            this.sessionId = sessionId;
            this.status = "active";
            this.createdAt = new java.util.Date().toString();
            this.chiefComplaint = chiefComplaint;
            this.symptomTags = symptomTags;
            this.age = age;
            this.gender = gender;
            this.messages = new ConcurrentLinkedDeque<>();
            this.lastSymptoms = chiefComplaint;
        }
    }

    public SessionDTO createSession(CreateSessionRequest request) {
        String sessionId = "cs_" + System.currentTimeMillis();
        
        SessionData sessionData = new SessionData(
                sessionId,
                request.getChiefComplaint(),
                request.getSymptomTags(),
                request.getAge(),
                request.getGender()
        );
        
        sessionStorage.put(sessionId, sessionData);
        
        SessionDTO dto = new SessionDTO();
        dto.setSessionId(sessionId);
        dto.setStatus("active");
        dto.setCreatedAt(sessionData.createdAt);
        
        log.info("Created AI consultation session: {}", sessionId);
        return dto;
    }

    public SendMessageResponse sendMessage(SendMessageRequest request) {
        SessionData sessionData = sessionStorage.get(request.getSessionId());
        
        if (sessionData == null) {
            throw new RuntimeException("会话不存在");
        }

        if (!"active".equals(sessionData.status)) {
            throw new RuntimeException("会话已结束");
        }

        String userMessageId = "msg_u_" + System.currentTimeMillis();
        MessageDTO userMessage = new MessageDTO();
        userMessage.setMessageId(userMessageId);
        userMessage.setRole("user");
        userMessage.setContent(request.getContent());
        userMessage.setCreatedAt(deepSeekService.formatDateTime());
        sessionData.messages.add(userMessage);

        sessionData.lastSymptoms = request.getContent();

        String aiResponse = deepSeekService.sendMessage(request.getSessionId(), request.getContent());

        String assistantMessageId = "msg_a_" + System.currentTimeMillis();
        MessageDTO assistantMessage = new MessageDTO();
        assistantMessage.setMessageId(assistantMessageId);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(aiResponse);
        assistantMessage.setCreatedAt(deepSeekService.formatDateTime());
        sessionData.messages.add(assistantMessage);

        String riskLevel = deepSeekService.analyzeRisk(sessionData.lastSymptoms);
        String suggestion = deepSeekService.generateSuggestion(sessionData.lastSymptoms, riskLevel);

        SendMessageResponse response = new SendMessageResponse();
        response.setUserMessage(userMessage);
        response.setAssistantMessage(assistantMessage);
        response.setRiskLevel(riskLevel);
        response.setSuggestion(suggestion);

        log.info("Message sent in session {}: {}", request.getSessionId(), request.getContent());
        return response;
    }

    public SessionDTO getSessionDetail(String sessionId) {
        SessionData sessionData = sessionStorage.get(sessionId);
        
        if (sessionData == null) {
            throw new RuntimeException("会话不存在");
        }

        SessionDTO dto = new SessionDTO();
        dto.setSessionId(sessionId);
        dto.setStatus(sessionData.status);
        dto.setCreatedAt(sessionData.createdAt);
        
        return dto;
    }

    public List<MessageDTO> getSessionMessages(String sessionId, Integer page, Integer pageSize) {
        SessionData sessionData = sessionStorage.get(sessionId);
        
        if (sessionData == null) {
            throw new RuntimeException("会话不存在");
        }

        List<MessageDTO> allMessages = new ArrayList<>(sessionData.messages);
        
        if (page == null) page = 1;
        if (pageSize == null) pageSize = 20;
        
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, allMessages.size());
        
        if (start >= allMessages.size()) {
            return Collections.emptyList();
        }
        
        return allMessages.subList(start, end);
    }

    public void closeSession(String sessionId, String closeReason) {
        SessionData sessionData = sessionStorage.get(sessionId);
        
        if (sessionData == null) {
            throw new RuntimeException("会话不存在");
        }

        sessionData.status = "closed";
        
        log.info("Closed session {}: {}", sessionId, closeReason);
    }

    public List<SessionDTO> getMySessions(Integer page, Integer pageSize, String status) {
        List<SessionDTO> sessions = new ArrayList<>();
        
        for (SessionData data : sessionStorage.values()) {
            if (status == null || status.isEmpty() || status.equals(data.status)) {
                SessionDTO dto = new SessionDTO();
                dto.setSessionId(data.sessionId);
                dto.setStatus(data.status);
                dto.setCreatedAt(data.createdAt);
                sessions.add(dto);
            }
        }

        sessions.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        if (page == null) page = 1;
        if (pageSize == null) pageSize = 20;
        
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, sessions.size());
        
        if (start >= sessions.size()) {
            return Collections.emptyList();
        }
        
        return sessions.subList(start, end);
    }

    public Map<String, Object> getSessionRisk(String sessionId) {
        SessionData sessionData = sessionStorage.get(sessionId);
        
        if (sessionData == null) {
            throw new RuntimeException("会话不存在");
        }

        String riskLevel = deepSeekService.analyzeRisk(sessionData.lastSymptoms);
        String suggestion = deepSeekService.generateSuggestion(sessionData.lastSymptoms, riskLevel);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("riskLevel", riskLevel);
        result.put("suggestion", suggestion);
        
        return result;
    }
}