package com.backend.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Timestamp;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  就诊记录实体类。
 *
 * @author 佳尔宇柔
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("consultations")
public class Consultations implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator,value= KeyGenerators.snowFlakeId)
    private Integer consultationId;

    private Integer appointmentId;

    private Integer doctorId;

    private Integer patientId;

    private String symptoms;

    private String diagnosis;

    private String treatment;

    private String prescription;

    private Timestamp consultationDate;

    private Timestamp createdAt;

    private Timestamp updatedAt;

}
