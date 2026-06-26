package com.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 医生信息DTO（包含用户信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDTO {

    private Integer doctorId;

    private Integer userId;

    private String username;

    private String realName;

    private String title;

    private Integer deptId;

    private String deptName;

    private String specialty;

    private String bio;

    private String status;

    private String phone;

    private Timestamp createdAt;
}