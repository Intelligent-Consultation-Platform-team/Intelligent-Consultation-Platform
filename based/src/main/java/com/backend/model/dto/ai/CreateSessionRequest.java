package com.backend.model.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateSessionRequest {

    @NotBlank(message = "主诉不能为空")
    private String chiefComplaint;

    private List<String> symptomTags;

    private Integer age;

    private String gender;
}