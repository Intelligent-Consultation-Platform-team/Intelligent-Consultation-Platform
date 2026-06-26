package com.backend.model.dto.ai;

import lombok.Data;

@Data
public class SendMessageResponse {

    private MessageDTO userMessage;

    private MessageDTO assistantMessage;

    private String riskLevel;

    private String suggestion;
}