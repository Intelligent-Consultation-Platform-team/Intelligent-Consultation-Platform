package com.backend.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("ai_consultation_sessions")
public class AiConsultationSession implements Serializable {

    @Id
    private String sessionId;

    private Integer userId;

    private Integer age;

    private String gender;

    private String chiefComplaint;

    private String symptomTags;

    private String riskLevel;

    private String status;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}
