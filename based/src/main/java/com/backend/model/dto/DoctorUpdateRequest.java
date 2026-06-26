package com.backend.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新医生请求DTO。
 *
 * @author 佳尔宇柔
 */
@Data
public class DoctorUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 医生ID
     */
    private Integer doctorId;

    /**
     * 用户ID
     */
    private Integer userId;

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
     * 科室ID
     */
    private Integer deptId;

    /**
     * 医生真实姓名
     */
    private String realName;

    /**
     * 医生电话
     */
    private String phone;

    /**
     * 状态
     */
    private String status;

}
