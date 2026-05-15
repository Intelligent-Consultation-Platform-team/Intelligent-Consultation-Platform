package com.backend.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  排班实体类。
 *
 * @author 佳尔宇柔
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("schedules")
public class Schedules implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator,value= KeyGenerators.snowFlakeId)
    private Integer scheduleId;

    private Integer doctorId;

    private String dayOfWeek;

    private Time startTime;

    private Time endTime;

    private Integer availableSlots;

    private String status;

    private Timestamp createdAt;

    private Timestamp updatedAt;

}
