package com.backend.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Timestamp;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("ai_consultation_messages")
public class AiConsultationMessage implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String messageId;

    private String sessionId;

    private String role;

    private String content;

    private Timestamp createdAt;
}
