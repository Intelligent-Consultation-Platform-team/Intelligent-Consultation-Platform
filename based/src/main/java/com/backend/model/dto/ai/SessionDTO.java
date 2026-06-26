package com.backend.model.dto.ai;

import lombok.Data;

@Data
public class SessionDTO {

    private String sessionId;

    private String status;

    private String createdAt;

    private Integer userId;

    private String username;

    private String chiefComplaint;

    private String riskLevel;

    private String symptomTags;

    private Integer age;

    private String gender;

    private String summary;

    private String suggestion;
}