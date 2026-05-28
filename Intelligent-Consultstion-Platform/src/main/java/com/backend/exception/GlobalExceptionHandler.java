package com.backend.exception;

import com.backend.common.BaseResponse;
import com.backend.common.ResultUtils;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException", e);
        FieldError fieldError = e.getBindingResult().getFieldError();
        if (fieldError != null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, fieldError.getDefaultMessage());
        }
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, "参数验证失败");
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BaseResponse<?> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException", e);
        Throwable cause = e.getCause();
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ex = (InvalidFormatException) cause;
            if (ex.getTargetType() == java.sql.Date.class) {
                return ResultUtils.error(ErrorCode.PARAMS_ERROR, "日期格式错误，请使用 yyyy-MM-dd 格式");
            }
            if (ex.getTargetType() == java.sql.Time.class) {
                return ResultUtils.error(ErrorCode.PARAMS_ERROR, "时间格式错误，请使用 HH:mm:ss 格式");
            }
            if (ex.getTargetType() == java.sql.Timestamp.class) {
                return ResultUtils.error(ErrorCode.PARAMS_ERROR, "时间戳格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
            }
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "参数格式错误: " + ex.getValue());
        }
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, "请求参数格式错误");
    }
}

