package com.backend.controller;

import com.backend.common.BaseResponse;
import com.backend.common.ResultUtils;
import com.backend.model.dto.ai.CloseSessionRequest;
import com.backend.model.dto.ai.CreateSessionRequest;
import com.backend.model.dto.ai.SendMessageRequest;
import com.backend.model.dto.ai.SessionDTO;
import com.backend.service.AiConsultationService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai-consultation")
public class AiConsultationController {

    @Resource
    private AiConsultationService aiConsultationService;

    @PostMapping("/session")
    public BaseResponse<SessionDTO> createSession(@Valid @RequestBody CreateSessionRequest request) {
        log.info("Creating AI consultation session with complaint: {}", request.getChiefComplaint());
        SessionDTO session = aiConsultationService.createSession(request);
        return ResultUtils.success(session, "创建成功");
    }

    @PostMapping("/message")
    public BaseResponse<?> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        log.info("Sending message to session: {}", request.getSessionId());
        try {
            var response = aiConsultationService.sendMessage(request);
            return ResultUtils.success(response, "发送成功");
        } catch (RuntimeException e) {
            return ResultUtils.error(400, e.getMessage());
        }
    }

    @GetMapping("/session/{sessionId}")
    public BaseResponse<?> getSessionDetail(@PathVariable String sessionId) {
        log.info("Getting session detail: {}", sessionId);
        try {
            SessionDTO session = aiConsultationService.getSessionDetail(sessionId);
            return ResultUtils.success(session, "ok");
        } catch (RuntimeException e) {
            return ResultUtils.error(404, e.getMessage());
        }
    }

    @GetMapping("/session/{sessionId}/messages")
    public BaseResponse<?> getSessionMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("Getting messages for session {}: page={}, pageSize={}", sessionId, page, pageSize);
        try {
            List<?> messages = aiConsultationService.getSessionMessages(sessionId, page, pageSize);
            return ResultUtils.success(messages, "ok");
        } catch (RuntimeException e) {
            return ResultUtils.error(404, e.getMessage());
        }
    }

    @PostMapping("/session/{sessionId}/close")
    public BaseResponse<?> closeSession(
            @PathVariable String sessionId,
            @RequestBody(required = false) CloseSessionRequest request) {
        String closeReason = request != null ? request.getCloseReason() : "用户主动结束";
        log.info("Closing session {}: {}", sessionId, closeReason);
        
        try {
            aiConsultationService.closeSession(sessionId, closeReason);
            Map<String, Object> result = new HashMap<>();
            result.put("sessionId", sessionId);
            result.put("status", "closed");
            return ResultUtils.success(result, "会话已结束");
        } catch (RuntimeException e) {
            return ResultUtils.error(400, e.getMessage());
        }
    }

    @GetMapping("/session/my")
    public BaseResponse<List<SessionDTO>> getMySessions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String status) {
        log.info("Getting my sessions: page={}, pageSize={}, status={}", page, pageSize, status);
        List<SessionDTO> sessions = aiConsultationService.getMySessions(page, pageSize, status);
        return ResultUtils.success(sessions, "ok");
    }

    @GetMapping("/session/all")
    public BaseResponse<?> getAllSessions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String status) {
        String role = com.backend.common.UserContext.getRole();
        if (!"admin".equals(role)) {
            return ResultUtils.error(403, "无权限访问");
        }
        log.info("Admin getting all sessions: page={}, pageSize={}, status={}", page, pageSize, status);
        List<SessionDTO> sessions = aiConsultationService.getAllSessions(page, pageSize, status);
        return ResultUtils.success(sessions, "ok");
    }

    @GetMapping("/session/{sessionId}/risk")
    public BaseResponse<?> getSessionRisk(@PathVariable String sessionId) {
        log.info("Getting risk assessment for session: {}", sessionId);
        try {
            Map<String, Object> risk = aiConsultationService.getSessionRisk(sessionId);
            return ResultUtils.success(risk, "ok");
        } catch (RuntimeException e) {
            return ResultUtils.error(404, e.getMessage());
        }
    }

    @PostMapping("/upload")
    public BaseResponse<Map<String, Object>> uploadFile() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "文件上传功能暂未实现");
        return ResultUtils.success(result, "ok");
    }
}