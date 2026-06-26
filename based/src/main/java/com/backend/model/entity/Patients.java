package com.backend.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  患者信息实体类。
 *
 * @author 佳尔宇柔
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("patients")
public class Patients implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator,value= KeyGenerators.snowFlakeId)
    private Integer patientId;

    private Integer userId;

    private String idCard;

    private String gender;

    private Integer age;

    private String address;

    private BigDecimal balance;

    private Timestamp createdAt;

    private Timestamp updatedAt;

}
