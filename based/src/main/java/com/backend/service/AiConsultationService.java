package com.backend.service;

import com.backend.common.UserContext;
import com.backend.mapper.AiConsultationMessageMapper;
import com.backend.mapper.AiConsultationSessionMapper;
import com.backend.mapper.UsersMapper;
import com.backend.model.dto.ai.*;
import com.backend.model.entity.AiConsultationMessage;
import com.backend.model.entity.AiConsultationSession;
import com.backend.model.entity.Users;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiConsultationService {

    private static final long SESSION_TIMEOUT_MS = 5 * 60 * 1000; // 5 分钟超时

    private final DeepSeekService deepSeekService;

    @Resource
    private AiConsultationSessionMapper sessionMapper;

    @Resource
    private AiConsultationMessageMapper messageMapper;

    @Resource
    private UsersMapper usersMapper;

    public AiConsultationService(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    @Transactional
    public SessionDTO createSession(CreateSessionRequest request) {
        String sessionId = "cs_" + System.currentTimeMillis();
        Integer userId = UserContext.getUserId();
        String username = UserContext.getUsername();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        AiConsultationSession session = AiConsultationSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .chiefComplaint(request.getChiefComplaint())
                .symptomTags(request.getSymptomTags() != null ? String.join(",", request.getSymptomTags()) : null)
                .age(request.getAge())
                .gender(request.getGender())
                .status("active")
                .createdAt(now)
                .updatedAt(now)
                .build();
        sessionMapper.insert(session);

        SessionDTO dto = new SessionDTO();
        dto.setSessionId(sessionId);
        dto.setStatus("active");
        dto.setCreatedAt(now.toString());
        dto.setUserId(userId);
        dto.setUsername(username);

        log.info("Created AI consultation session: {} by user {}", sessionId, userId);
        return dto;
    }

    @Transactional
    public SendMessageResponse sendMessage(SendMessageRequest request) {
        AiConsultationSession session = sessionMapper.selectOneById(request.getSessionId());

        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        // 自动关闭超时会话
        if (isSessionExpired(session)) {
            closeExpiredSession(request.getSessionId());
            throw new RuntimeException("会话已超时自动结束，请创建新的问诊");
        }

        if (!"active".equals(session.getStatus())) {
            throw new RuntimeException("会话已结束");
        }

        // 非管理员只能操作自己的会话
        String currentRole = UserContext.getRole();
        Integer currentUserId = UserContext.getUserId();
        if (!"admin".equals(currentRole) && !currentUserId.equals(session.getUserId())) {
            throw new RuntimeException("无权操作此会话");
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        String formattedTime = deepSeekService.formatDateTime();

        // 保存用户消息
        String userMessageId = "msg_u_" + System.currentTimeMillis();
        AiConsultationMessage userMsg = AiConsultationMessage.builder()
                .messageId(userMessageId)
                .sessionId(request.getSessionId())
                .role("user")
                .content(request.getContent())
                .createdAt(now)
                .build();
        messageMapper.insert(userMsg);

        // 加载历史对话上下文
        List<AiConsultationMessage> historyMessages = messageMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("session_id", request.getSessionId())
                        .orderBy("created_at", true));
        List<Map<String, String>> history = new ArrayList<>();
        for (AiConsultationMessage m : historyMessages) {
            Map<String, String> h = new HashMap<>();
            h.put("role", m.getRole());
            h.put("content", m.getContent());
            history.add(h);
        }

        // 调用 AI（传入完整历史上下文）
        String aiResponse = deepSeekService.sendMessage(request.getSessionId(), request.getContent(), history);

        // 保存 AI 回复
        String assistantMessageId = "msg_a_" + System.currentTimeMillis();
        AiConsultationMessage assistantMsg = AiConsultationMessage.builder()
                .messageId(assistantMessageId)
                .sessionId(request.getSessionId())
                .role("assistant")
                .content(aiResponse)
                .createdAt(now)
                .build();
        messageMapper.insert(assistantMsg);

        String riskLevel = deepSeekService.analyzeRisk(request.getContent());
        String suggestion = deepSeekService.generateSuggestion(request.getContent(), riskLevel);

        // 更新会话的风险等级
        sessionMapper.update(
                AiConsultationSession.builder()
                        .sessionId(request.getSessionId())
                        .riskLevel(riskLevel)
                        .updatedAt(now)
                        .build()
        );

        MessageDTO userMsgDto = new MessageDTO();
        userMsgDto.setMessageId(userMessageId);
        userMsgDto.setRole("user");
        userMsgDto.setContent(request.getContent());
        userMsgDto.setCreatedAt(formattedTime);

        MessageDTO assistantMsgDto = new MessageDTO();
        assistantMsgDto.setMessageId(assistantMessageId);
        assistantMsgDto.setRole("assistant");
        assistantMsgDto.setContent(aiResponse);
        assistantMsgDto.setCreatedAt(formattedTime);

        SendMessageResponse response = new SendMessageResponse();
        response.setUserMessage(userMsgDto);
        response.setAssistantMessage(assistantMsgDto);
        response.setRiskLevel(riskLevel);
        response.setSuggestion(suggestion);

        log.info("Message sent in session {}: {}", request.getSessionId(), request.getContent());
        return response;
    }

    public SessionDTO getSessionDetail(String sessionId) {
        AiConsultationSession session = sessionMapper.selectOneById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }

        Integer currentUserId = UserContext.getUserId();
        String currentRole = UserContext.getRole();
        if (!"admin".equals(currentRole) && !currentUserId.equals(session.getUserId())) {
            throw new RuntimeException("无权查看此会话");
        }

        Users user = usersMapper.selectOneById(session.getUserId());

        SessionDTO dto = new SessionDTO();
        dto.setSessionId(sessionId);
        dto.setStatus(session.getStatus());
        dto.setCreatedAt(session.getCreatedAt() != null ? session.getCreatedAt().toString() : null);
        dto.setUserId(session.getUserId());
        dto.setUsername(user != null ? user.getRealName() : null);
        dto.setChiefComplaint(session.getChiefComplaint());
        dto.setRiskLevel(session.getRiskLevel());
        dto.setSymptomTags(session.getSymptomTags());
        dto.setAge(session.getAge());
        dto.setGender(session.getGender());

        // 生成摘要
        long msgCount = messageMapper.selectCountByQuery(
                QueryWrapper.create().eq("session_id", sessionId));
        String summaryText = session.getChiefComplaint() != null ? session.getChiefComplaint() : "未知主诉";
        if (msgCount > 0) {
            summaryText += "（共 " + msgCount + " 条对话记录）";
        }
        dto.setSummary(summaryText);

        // 根据风险等级给出建议
        String riskLevel = session.getRiskLevel();
        if ("high".equals(riskLevel)) {
            dto.setSuggestion("您的症状风险较高，强烈建议您尽快前往医院就诊，不要延误。");
        } else if ("medium".equals(riskLevel)) {
            dto.setSuggestion("您的症状需要引起重视，建议近期安排时间就医检查。");
        } else if ("low".equals(riskLevel)) {
            dto.setSuggestion("您的症状风险较低，可以先居家观察，注意休息和饮食。如有加重请及时就医。");
        } else {
            dto.setSuggestion("请根据自身情况判断是否需要就医，如有不适请及时就诊。");
        }

        return dto;
    }

    public List<MessageDTO> getSessionMessages(String sessionId, Integer page, Integer pageSize) {
        AiConsultationSession session = sessionMapper.selectOneById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }

        Integer currentUserId = UserContext.getUserId();
        String currentRole = UserContext.getRole();
        if (!"admin".equals(currentRole) && !currentUserId.equals(session.getUserId())) {
            throw new RuntimeException("无权查看此会话");
        }

        List<AiConsultationMessage> allMessages = messageMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("session_id", sessionId)
                        .orderBy("created_at", true)
        );

        if (page == null) page = 1;
        if (pageSize == null) pageSize = 20;

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, allMessages.size());

        if (start >= allMessages.size()) {
            return Collections.emptyList();
        }

        return allMessages.subList(start, end).stream().map(m -> {
            MessageDTO dto = new MessageDTO();
            dto.setMessageId(m.getMessageId());
            dto.setRole(m.getRole());
            dto.setContent(m.getContent());
            dto.setCreatedAt(m.getCreatedAt() != null ? m.getCreatedAt().toString() : null);
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void closeSession(String sessionId, String closeReason) {
        AiConsultationSession session = sessionMapper.selectOneById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }

        String currentRole = UserContext.getRole();
        Integer currentUserId = UserContext.getUserId();
        if (!"admin".equals(currentRole) && !currentUserId.equals(session.getUserId())) {
            throw new RuntimeException("无权操作此会话");
        }

        sessionMapper.update(
                AiConsultationSession.builder()
                        .sessionId(sessionId)
                        .status("closed")
                        .updatedAt(new Timestamp(System.currentTimeMillis()))
                        .build()
        );

        log.info("Closed session {}: {}", sessionId, closeReason);
    }

    public List<SessionDTO> getMySessions(Integer page, Integer pageSize, String status) {
        Integer currentUserId = UserContext.getUserId();
        String currentRole = UserContext.getRole();

        QueryWrapper qw = QueryWrapper.create();
        if (status != null && !status.isEmpty()) {
            qw.eq("status", status);
        }

        // 非管理员只查自己的
        if (!"admin".equals(currentRole)) {
            qw.eq("user_id", currentUserId);
        }

        qw.orderBy("created_at", false);

        // 先自动关闭超时会话
        autoCloseExpiredSessions();

        List<AiConsultationSession> sessions = sessionMapper.selectListByQuery(qw);

        // 收集 userId 批量查用户名
        Set<Integer> userIds = sessions.stream()
                .map(AiConsultationSession::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<Users> users = usersMapper.selectListByQuery(
                    QueryWrapper.create().in("user_id", new ArrayList<>(userIds)));
            for (Users u : users) {
                userNameMap.put(u.getUserId(), u.getRealName());
            }
        }

        List<SessionDTO> result = new ArrayList<>();
        for (AiConsultationSession s : sessions) {
            SessionDTO dto = new SessionDTO();
            dto.setSessionId(s.getSessionId());
            dto.setStatus(s.getStatus());
            dto.setCreatedAt(s.getCreatedAt() != null ? s.getCreatedAt().toString() : null);
            dto.setUserId(s.getUserId());
            dto.setUsername(userNameMap.getOrDefault(s.getUserId(), null));
            dto.setChiefComplaint(s.getChiefComplaint());
            dto.setRiskLevel(s.getRiskLevel());
            dto.setSymptomTags(s.getSymptomTags());
            dto.setAge(s.getAge());
            dto.setGender(s.getGender());
            result.add(dto);
        }

        if (page == null) page = 1;
        if (pageSize == null) pageSize = 20;

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, result.size());

        if (start >= result.size()) {
            return Collections.emptyList();
        }

        return result.subList(start, end);
    }

    public List<SessionDTO> getAllSessions(Integer page, Integer pageSize, String status) {
        return getMySessions(page, pageSize, status);
    }

    public Map<String, Object> getSessionRisk(String sessionId) {
        AiConsultationSession session = sessionMapper.selectOneById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }

        Integer currentUserId = UserContext.getUserId();
        String currentRole = UserContext.getRole();
        if (!"admin".equals(currentRole) && !currentUserId.equals(session.getUserId())) {
            throw new RuntimeException("无权查看此会话");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("riskLevel", session.getRiskLevel() != null ? session.getRiskLevel() : "low");
        result.put("suggestion", "请根据风险等级及时就医");

        return result;
    }

    private boolean isSessionExpired(AiConsultationSession session) {
        if (session == null || session.getUpdatedAt() == null) return false;
        if (!"active".equals(session.getStatus())) return false;
        long elapsed = System.currentTimeMillis() - session.getUpdatedAt().getTime();
        return elapsed > SESSION_TIMEOUT_MS;
    }

    private void closeExpiredSession(String sessionId) {
        sessionMapper.update(
                AiConsultationSession.builder()
                        .sessionId(sessionId)
                        .status("closed")
                        .updatedAt(new Timestamp(System.currentTimeMillis()))
                        .build()
        );
        log.info("Auto-closed expired session: {}", sessionId);
    }

    private void autoCloseExpiredSessions() {
        QueryWrapper qw = QueryWrapper.create()
                .eq("status", "active")
                .lt("updated_at", new Timestamp(System.currentTimeMillis() - SESSION_TIMEOUT_MS));
        List<AiConsultationSession> expired = sessionMapper.selectListByQuery(qw);
        for (AiConsultationSession s : expired) {
            closeExpiredSession(s.getSessionId());
        }
    }
}
