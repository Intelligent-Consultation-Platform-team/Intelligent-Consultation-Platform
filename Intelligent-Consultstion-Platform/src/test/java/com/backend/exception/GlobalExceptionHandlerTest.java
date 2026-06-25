package com.backend.exception;

import com.backend.common.BaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 全局异常处理器单元测试。
 * <p>
 * 测试功能：将各类异常统一转换为 {@link BaseResponse} 格式返回前端。
 * 对应生产类：{@link GlobalExceptionHandler}
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    /** 验证业务异常返回对应的错误码与错误信息 */
    @Test
    void businessExceptionHandler_returnsErrorCodeAndMessage() {
        BusinessException ex = new BusinessException(ErrorCode.USERNAME_EXISTS);

        BaseResponse<?> response = handler.businessExceptionHandler(ex);

        assertEquals(ErrorCode.USERNAME_EXISTS.getCode(), response.getCode());
        assertEquals(ErrorCode.USERNAME_EXISTS.getMessage(), response.getMessage());
    }

    /** 验证未捕获的运行时异常返回系统错误（50000），不暴露内部细节 */
    @Test
    void runtimeExceptionHandler_returnsSystemError() {
        BaseResponse<?> response = handler.runtimeExceptionHandler(new RuntimeException("unexpected"));

        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), response.getCode());
        assertEquals("系统错误", response.getMessage());
    }
}
