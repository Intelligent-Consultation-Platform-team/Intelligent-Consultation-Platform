package com.backend.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 添加医生请求DTO。
 *
 * @author 佳尔宇柔
 */
@Data
public class DoctorAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 科室ID
     */
    private Integer deptId;

    /**
     * 职称
     */
    private String title;

    /**
     * 专业
     */
    private String specialty;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 医生用户名（用于创建账户）
     */
    private String username;

    /**
     * 医生真实姓名
     */
    private String realName;

    /**
     * 医生电话
     */
    private String phone;

}
