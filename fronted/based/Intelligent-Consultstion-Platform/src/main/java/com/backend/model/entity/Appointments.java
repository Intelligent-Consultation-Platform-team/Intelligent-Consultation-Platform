package com.backend.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  预约挂号实体类。
 *
 * @author 佳尔宇柔
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("appointments")
public class Appointments implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator,value= KeyGenerators.snowFlakeId)
    private Integer appointmentId;

    private Integer patientId;

    private Integer doctorId;

    private Integer scheduleId;

    private Date appointmentDate;

    private Time appointmentTime;

    private String status;

    private Timestamp createdAt;

    private Timestamp updatedAt;

}
