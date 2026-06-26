package com.backend.model.dto.ai;

import lombok.Data;

@Data
public class CloseSessionRequest {

    private String closeReason;
}