package com.backend.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40001, "参数错误"),
    USER_OR_PASSWORD_ERROR(40002, "用户名或密码错误"),
    EMAIL_FORMAT_ERROR(40003, "邮箱格式错误"),
    USERNAME_EXISTS(40004, "用户名已存在"),
    EMAIL_EXISTS(40005, "邮箱已注册"),
    NOT_LOGIN_ERROR(40100, "未登录或token无效"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "服务端异常"),
    OPERATION_ERROR(50001, "操作失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
