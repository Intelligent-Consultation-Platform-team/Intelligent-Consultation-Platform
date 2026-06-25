package com.backend.controller;

import com.backend.exception.BusinessException;
import com.backend.exception.ErrorCode;
import com.backend.exception.GlobalExceptionHandler;
import com.backend.model.dto.UserLoginRequest;
import com.backend.model.dto.UserRegisterRequest;
import com.backend.service.UsersService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static com.backend.TestDataFactory.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证控制器 Web 层测试。
 * <p>
 * 测试功能：{@code /auth/register} 与 {@code /auth/login} 接口的 HTTP 请求/响应格式。
 * 使用 Standalone MockMvc 隔离测试，不启动完整 Spring 容器。
 * 对应生产类：{@link AuthController}
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UsersService usersService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /** 验证 POST /auth/register 注册成功时返回 userId 与成功消息 */
    @Test
    void register_success() throws Exception {
        when(usersService.userRegister(any(UserRegisterRequest.class))).thenReturn(1001L);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPatientRegisterRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userId").value(1001))
                .andExpect(jsonPath("$.message").value("注册成功"));
    }

    /** 验证 Service 层校验失败（如邮箱格式错误）时，Controller 通过全局异常处理返回业务错误码 */
    @Test
    void register_serviceValidationFails_returnsBusinessError() throws Exception {
        when(usersService.userRegister(any(UserRegisterRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.EMAIL_FORMAT_ERROR));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPatientRegisterRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.EMAIL_FORMAT_ERROR.getCode()));
    }

    /** 验证 POST /auth/login 登录成功时返回 token 与 expiresIn */
    @Test
    void login_success() throws Exception {
        Map<String, Object> loginResult = new HashMap<>();
        loginResult.put("token", "test-token");
        loginResult.put("tokenType", "Bearer");
        loginResult.put("expiresIn", 7200L);
        when(usersService.userLogin(any(UserLoginRequest.class))).thenReturn(loginResult);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest("test_patient", "123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").value("test-token"))
                .andExpect(jsonPath("$.message").value("登录成功"));
    }
}
