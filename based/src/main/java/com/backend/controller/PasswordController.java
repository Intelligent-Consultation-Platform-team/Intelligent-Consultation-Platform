package com.backend.controller;

import com.backend.utils.PasswordUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PasswordController {

    @GetMapping("/generate-password")
    public Map<String, Object> generatePassword(@RequestParam String password) {
        String hashedPassword = PasswordUtils.encrypt(password);
        Map<String, Object> result = new HashMap<>();
        result.put("original", password);
        result.put("hashed", hashedPassword);
        result.put("verify", PasswordUtils.verify(password, hashedPassword));
        return result;
    }
}
