package com.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * 预约信息DTO（包含患者和医生信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {

    private Integer appointmentId;

    private Integer patientId;

    private String patientName;

    private Integer doctorId;

    private String doctorName;

    private String doctorTitle;

    private Integer deptId;

    private String deptName;

    private Integer scheduleId;

    private Date appointmentDate;

    private Time appointmentTime;

    private String status;

    private String symptoms;

    private Timestamp createdAt;
}
