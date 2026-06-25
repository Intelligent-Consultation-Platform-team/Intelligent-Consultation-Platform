package com.backend.controller;

import com.backend.common.BaseResponse;
import com.backend.common.UserContext;
import com.backend.exception.GlobalExceptionHandler;
import com.backend.model.dto.ai.CloseSessionRequest;
import com.backend.model.dto.ai.CreateSessionRequest;
import com.backend.model.dto.ai.MessageDTO;
import com.backend.model.dto.ai.SendMessageRequest;
import com.backend.model.dto.ai.SendMessageResponse;
import com.backend.model.dto.ai.SessionDTO;
import com.backend.service.AiConsultationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AI 问诊控制器测试。
 * <p>
 * 覆盖路由映射、请求绑定、返回体结构、权限分支和上传兜底提示。
 */
@ExtendWith(MockitoExtension.class)
class AiConsultationControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AiConsultationService aiConsultationService;

    @InjectMocks
    private AiConsultationController aiConsultationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiConsultationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        UserContext.setUserInfo(new UserContext.UserInfo(1001, "test_patient", "patient"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    /** 验证创建 AI 问诊会话的接口会返回会话信息。 */
    @Test
    void createSession_returnsSession() throws Exception {
        SessionDTO session = new SessionDTO();
        session.setSessionId("cs_1");
        session.setStatus("active");
        when(aiConsultationService.createSession(any(CreateSessionRequest.class))).thenReturn(session);

        CreateSessionRequest request = new CreateSessionRequest();
        request.setChiefComplaint("头痛");

        mockMvc.perform(post("/api/ai-consultation/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.sessionId").value("cs_1"));
    }

    /** 验证发送消息接口会返回用户消息和 AI 回复的包装结果。 */
    @Test
    void sendMessage_returnsResponse() throws Exception {
        SendMessageResponse response = new SendMessageResponse();
        MessageDTO user = new MessageDTO();
        user.setMessageId("u1");
        response.setUserMessage(user);
        when(aiConsultationService.sendMessage(any(SendMessageRequest.class))).thenReturn(response);

        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId("cs_1");
        request.setContent("头痛");

        mockMvc.perform(post("/api/ai-consultation/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    /** 验证会话详情接口会透传服务层返回的会话 DTO。 */
    @Test
    void getSessionDetail_returnsSession() throws Exception {
        SessionDTO session = new SessionDTO();
        session.setSessionId("cs_2");
        when(aiConsultationService.getSessionDetail("cs_2")).thenReturn(session);

        mockMvc.perform(get("/api/ai-consultation/session/cs_2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value("cs_2"));
    }

    /** 验证消息分页接口会返回消息列表。 */
    @Test
    void getSessionMessages_returnsList() throws Exception {
        when(aiConsultationService.getSessionMessages(eq("cs_3"), eq(1), eq(20))).thenReturn(List.of(new MessageDTO()));

        mockMvc.perform(get("/api/ai-consultation/session/cs_3/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    /** 验证关闭会话接口会返回 closed 状态。 */
    @Test
    void closeSession_returnsClosedStatus() throws Exception {
        mockMvc.perform(post("/api/ai-consultation/session/cs_4/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CloseSessionRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("closed"));
    }

    /** 验证当前用户的会话列表接口能正常返回分页结果。 */
    @Test
    void getMySessions_returnsList() throws Exception {
        when(aiConsultationService.getMySessions(eq(1), eq(20), eq(null))).thenReturn(List.of(new SessionDTO()));

        mockMvc.perform(get("/api/ai-consultation/session/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    /** 验证非管理员访问全量会话接口会被拒绝。 */
    @Test
    void getAllSessions_deniesNonAdmin() throws Exception {
        mockMvc.perform(get("/api/ai-consultation/session/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    /** 验证管理员访问全量会话接口时会放行并返回列表。 */
    @Test
    void getAllSessions_allowsAdmin() throws Exception {
        UserContext.setUserInfo(new UserContext.UserInfo(1001, "test_patient", "admin"));
        when(aiConsultationService.getAllSessions(eq(1), eq(20), eq(null))).thenReturn(List.of(new SessionDTO()));

        mockMvc.perform(get("/api/ai-consultation/session/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    /** 验证风险查询接口会返回服务层提供的风险信息。 */
    @Test
    void getSessionRisk_returnsRiskMap() throws Exception {
        when(aiConsultationService.getSessionRisk("cs_5")).thenReturn(Map.of("riskLevel", "medium"));

        mockMvc.perform(get("/api/ai-consultation/session/cs_5/risk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.riskLevel").value("medium"));
    }

    /** 验证上传接口当前返回未实现提示。 */
    @Test
    void upload_returnsMessage() throws Exception {
        mockMvc.perform(post("/api/ai-consultation/upload"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("文件上传功能暂未实现"));
    }
}
