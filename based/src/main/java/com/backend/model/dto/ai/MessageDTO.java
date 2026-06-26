package com.backend.model.dto.ai;

import lombok.Data;

@Data
public class MessageDTO {

    private String messageId;

    private String role;

    private String content;

    private String createdAt;
}