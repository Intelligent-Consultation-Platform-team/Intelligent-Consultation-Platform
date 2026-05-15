package com.backend.service;

import com.mybatisflex.core.service.IService;
import com.backend.model.entity.Users;
import com.backend.model.dto.UserLoginRequest;
import com.backend.model.dto.UserRegisterRequest;
import java.util.Map;

public interface UsersService extends IService<Users> {

    Long userRegister(UserRegisterRequest userRegisterRequest);

    Map<String, Object> userLogin(UserLoginRequest userLoginRequest);
}
