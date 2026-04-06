package com.backend.controller;

import com.backend.common.BaseResponse;
import com.backend.common.ResultUtils;
import com.backend.model.dto.UserLoginRequest;
import com.backend.model.dto.UserRegisterRequest;
import com.backend.service.UsersService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsersService usersService;

    @PostMapping("/register")
    public BaseResponse<Map<String, Object>> register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        Long userId = usersService.userRegister(userRegisterRequest);
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", userRegisterRequest.getUsername());
        data.put("role", userRegisterRequest.getRole());
        data.put("createdAt", new java.util.Date().toInstant());
        
        return ResultUtils.success(data, "注册成功");
    }

    @PostMapping("/login")
    public BaseResponse<Map<String, Object>> login(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        Map<String, Object> result = usersService.userLogin(userLoginRequest);
        return ResultUtils.success(result, "登录成功");
    }
}