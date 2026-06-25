package com.backend.service;

import com.backend.common.UserContext;
import com.backend.mapper.AiConsultationMessageMapper;
import com.backend.mapper.AiConsultationSessionMapper;
import com.backend.mapper.UsersMapper;
import com.backend.model.dto.ai.CreateSessionRequest;
import com.backend.model.dto.ai.MessageDTO;
import com.backend.model.dto.ai.SendMessageRequest;
import com.backend.model.dto.ai.SendMessageResponse;
import com.backend.model.dto.ai.SessionDTO;
import com.backend.model.entity.AiConsultationMessage;
import com.backend.model.entity.AiConsultationSession;
import com.backend.model.entity.Users;
import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * AI 问诊服务测试。
 * <p>
 * 覆盖会话创建、消息发送、权限校验、分页查询、关闭会话和风险查询等核心业务。
 */
@ExtendWith(MockitoExtension.class)
class AiConsultationServiceTest {

    @Mock
    private DeepSeekService deepSeekService;
    @Mock
    private AiConsultationSessionMapper sessionMapper;
    @Mock
    private AiConsultationMessageMapper messageMapper;
    @Mock
    private UsersMapper usersMapper;

    private AiConsultationService service;

    @BeforeEach
    void setUp() {
        service = new AiConsultationService(deepSeekService);
        ReflectionTestUtils.setField(service, "sessionMapper", sessionMapper);
        ReflectionTestUtils.setField(service, "messageMapper", messageMapper);
        ReflectionTestUtils.setField(service, "usersMapper", usersMapper);
        UserContext.setUserInfo(new UserContext.UserInfo(1001, "test_patient", "patient"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    /** 验证创建会话时会写入 active 会话并返回基础 DTO。 */
    @Test
    void createSession_persistsActiveSession() {
        CreateSessionRequest request = new CreateSessionRequest();
        request.setChiefComplaint("头痛");
        request.setAge(28);
        request.setGender("female");
        request.setSymptomTags(List.of("头痛", "发热"));

        SessionDTO result = service.createSession(request);

        assertNotNull(result.getSessionId());
        assertEquals("active", result.getStatus());
        assertEquals(1001, result.getUserId());
        ArgumentCaptor<AiConsultationSession> captor = ArgumentCaptor.forClass(AiConsultationSession.class);
        verify(sessionMapper).insert(captor.capture());
        assertEquals("头痛", captor.getValue().getChiefComplaint());
        assertEquals("头痛,发热", captor.getValue().getSymptomTags());
    }

    /** 验证发消息会落库用户/助手消息，并同步更新风险等级。 */
    @Test
    void sendMessage_writesMessagesAndUpdatesRisk() {
        AiConsultationSession session = AiConsultationSession.builder()
                .sessionId("cs_1")
                .userId(1001)
                .status("active")
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        when(sessionMapper.selectOneById("cs_1")).thenReturn(session);
        when(deepSeekService.formatDateTime()).thenReturn("2026-06-26 10:00:00");
        when(deepSeekService.sendMessage(eq("cs_1"), eq("头痛"), any())).thenReturn("建议休息");
        when(deepSeekService.analyzeRisk("头痛")).thenReturn("medium");
        when(deepSeekService.generateSuggestion("头痛", "medium")).thenReturn("建议及时就医");
        when(messageMapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId("cs_1");
        request.setContent("头痛");

        SendMessageResponse response = service.sendMessage(request);

        assertEquals("medium", response.getRiskLevel());
        assertEquals("建议及时就医", response.getSuggestion());
        verify(messageMapper, times(2)).insert(any(AiConsultationMessage.class));
        verify(sessionMapper).update(any(AiConsultationSession.class));
    }

    /** 验证非当前用户访问会话时会触发权限拒绝。 */
    @Test
    void sendMessage_rejectsUnauthorizedAccess() {
        AiConsultationSession session = AiConsultationSession.builder()
                .sessionId("cs_2")
                .userId(2002)
                .status("active")
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        when(sessionMapper.selectOneById("cs_2")).thenReturn(session);

        SendMessageRequest request = new SendMessageRequest();
        request.setSessionId("cs_2");
        request.setContent("头痛");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.sendMessage(request));
        assertEquals("无权操作此会话", ex.getMessage());
    }

    /** 验证会话详情会拼装用户信息、摘要和建议文案。 */
    @Test
    void getSessionDetail_returnsMappedDto() {
        AiConsultationSession session = AiConsultationSession.builder()
                .sessionId("cs_3")
                .userId(1001)
                .status("active")
                .chiefComplaint("咳嗽")
                .riskLevel("high")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
        Users user = Users.builder().userId(1001).realName("测试患者").build();
        when(sessionMapper.selectOneById("cs_3")).thenReturn(session);
        when(usersMapper.selectOneById(1001)).thenReturn(user);
        when(messageMapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(3L);

        SessionDTO dto = service.getSessionDetail("cs_3");

        assertEquals("cs_3", dto.getSessionId());
        assertTrue(dto.getSummary().contains("3 条对话记录"));
        assertTrue(dto.getSuggestion().contains("尽快前往医院就诊"));
    }

    /** 验证消息列表查询会按页码和页大小返回切片结果。 */
    @Test
    void getSessionMessages_appliesPagination() {
        AiConsultationSession session = AiConsultationSession.builder()
                .sessionId("cs_4")
                .userId(1001)
                .status("active")
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        List<AiConsultationMessage> messages = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            messages.add(AiConsultationMessage.builder()
                    .messageId("m" + i)
                    .sessionId("cs_4")
                    .role("user")
                    .content("msg" + i)
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        when(sessionMapper.selectOneById("cs_4")).thenReturn(session);
        when(messageMapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(messages);

        List<MessageDTO> result = service.getSessionMessages("cs_4", 2, 1);

        assertEquals(1, result.size());
        assertEquals("m1", result.get(0).getMessageId());
    }

    /** 验证关闭会话会把状态改成 closed。 */
    @Test
    void closeSession_updatesStatusToClosed() {
        AiConsultationSession session = AiConsultationSession.builder()
                .sessionId("cs_5")
                .userId(1001)
                .status("active")
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        when(sessionMapper.selectOneById("cs_5")).thenReturn(session);

        service.closeSession("cs_5", "用户主动结束");

        ArgumentCaptor<AiConsultationSession> captor = ArgumentCaptor.forClass(AiConsultationSession.class);
        verify(sessionMapper).update(captor.capture());
        assertEquals("closed", captor.getValue().getStatus());
    }

    /** 验证普通用户只能查询自己的会话列表。 */
    @Test
    void getMySessions_filtersByCurrentUser() {
        AiConsultationSession session = AiConsultationSession.builder()
                .sessionId("cs_6")
                .userId(1001)
                .status("active")
                .chiefComplaint("头晕")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
        when(sessionMapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(session));
        when(usersMapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(Users.builder().userId(1001).realName("测试患者").build()));

        List<SessionDTO> result = service.getMySessions(1, 20, null);

        assertEquals(1, result.size());
        assertEquals("cs_6", result.get(0).getSessionId());
    }

    /** 验证管理员可以走全量会话查询逻辑。 */
    @Test
    void getAllSessions_delegatesToMySessionsForAdmin() {
        UserContext.setUserInfo(new UserContext.UserInfo(1001, "test_patient", "admin"));
        AiConsultationSession session = AiConsultationSession.builder()
                .sessionId("cs_7")
                .userId(1001)
                .status("active")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
        when(sessionMapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(session));
        when(usersMapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(Users.builder().userId(1001).realName("测试患者").build()));

        List<SessionDTO> result = service.getAllSessions(1, 20, null);

        assertEquals(1, result.size());
    }

    /** 验证没有风险等级时，会返回安全默认值。 */
    @Test
    void getSessionRisk_returnsDefaultPayloadWhenRiskMissing() {
        AiConsultationSession session = AiConsultationSession.builder()
                .sessionId("cs_8")
                .userId(1001)
                .status("active")
                .build();
        when(sessionMapper.selectOneById("cs_8")).thenReturn(session);

        Map<String, Object> result = service.getSessionRisk("cs_8");

        assertEquals("low", result.get("riskLevel"));
        assertEquals("请根据风险等级及时就医", result.get("suggestion"));
    }
}
