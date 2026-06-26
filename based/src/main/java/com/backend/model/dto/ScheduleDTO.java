package com.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.sql.Timestamp;

/**
 * 排班信息DTO（包含医生信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {

    private Integer scheduleId;

    private Integer doctorId;

    private String doctorName;

    private String title;

    private Integer deptId;

    private String deptName;

    private String dayOfWeek;

    private String date;

    private Time startTime;

    private Time endTime;

    private Integer availableSlots;

    private Integer maxNumber;

    private Integer remaining;

    private String status;

    private Timestamp createdAt;
}