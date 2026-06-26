package com.backend.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 重置密码请求DTO。
 */
@Data
public class ResetPasswordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Integer userId;

}
