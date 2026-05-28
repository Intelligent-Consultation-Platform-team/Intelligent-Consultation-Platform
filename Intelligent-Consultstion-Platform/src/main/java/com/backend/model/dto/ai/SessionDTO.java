package com.backend.model.dto.ai;

import lombok.Data;

@Data
public class SessionDTO {

    private String sessionId;

    private String status;

    private String createdAt;
}