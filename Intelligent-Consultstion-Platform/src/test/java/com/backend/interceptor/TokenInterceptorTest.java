package com.backend.interceptor;

import com.backend.common.UserContext;
import com.backend.utils.JwtUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Token 拦截器单元测试。
 * <p>
 * 测试功能：HTTP 请求鉴权 — 从 Authorization 头提取 JWT，写入 {@link UserContext}，
 * 无 Token / 过期 / 无效时返回 401 JSON。
 * 对应生产类：{@link TokenInterceptor}
 */
@ExtendWith(MockitoExtension.class)
class TokenInterceptorTest {

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private TokenInterceptor tokenInterceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        objectMapper = new ObjectMapper();
        UserContext.clear();
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    /** 验证未携带 Token 时拦截请求并返回「请先登录」 */
    @Test
    void preHandle_noToken_returnsUnauthorized() throws Exception {
        boolean result = tokenInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(200, response.getStatus());
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertEquals(401, body.get("code").asInt());
        assertEquals("请先登录", body.get("message").asText());
        assertNull(UserContext.getUserInfo());
    }

    /** 验证有效 Token 放行，并将 userId/username/role 写入 UserContext */
    @Test
    void preHandle_validToken_setsUserContext() throws Exception {
        request.addHeader("Authorization", "Bearer valid-token");
        when(jwtUtils.extractUserId("valid-token")).thenReturn(1001);
        when(jwtUtils.extractUsername("valid-token")).thenReturn("test_user");
        when(jwtUtils.extractRole("valid-token")).thenReturn("patient");
        when(jwtUtils.isTokenExpired("valid-token")).thenReturn(false);

        boolean result = tokenInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertEquals(1001, UserContext.getUserId());
        assertEquals("test_user", UserContext.getUsername());
        assertEquals("patient", UserContext.getRole());
    }

    /** 验证 Token 已过期时拦截并返回「登录已过期，请重新登录」 */
    @Test
    void preHandle_expiredToken_returnsUnauthorized() throws Exception {
        request.addHeader("Authorization", "Bearer expired-token");
        when(jwtUtils.extractUserId("expired-token")).thenReturn(1001);
        when(jwtUtils.extractUsername("expired-token")).thenReturn("test_user");
        when(jwtUtils.extractRole("expired-token")).thenReturn("patient");
        when(jwtUtils.isTokenExpired("expired-token")).thenReturn(true);

        boolean result = tokenInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertEquals("登录已过期，请重新登录", body.get("message").asText());
        assertNull(UserContext.getUserInfo());
    }

    /** 验证 Token 格式/签名无效时拦截并返回「token无效，请重新登录」 */
    @Test
    void preHandle_invalidToken_returnsUnauthorized() throws Exception {
        request.addHeader("Authorization", "Bearer bad-token");
        when(jwtUtils.extractUserId("bad-token")).thenThrow(new io.jsonwebtoken.JwtException("invalid"));

        boolean result = tokenInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertEquals("token无效，请重新登录", body.get("message").asText());
    }

    /** 验证请求结束后清理 ThreadLocal，防止线程复用导致用户信息泄漏 */
    @Test
    void afterCompletion_clearsUserContext() throws Exception {
        UserContext.setUserInfo(new UserContext.UserInfo(1001, "test_user", "patient"));

        tokenInterceptor.afterCompletion(request, response, new Object(), null);

        assertNull(UserContext.getUserInfo());
    }
}
