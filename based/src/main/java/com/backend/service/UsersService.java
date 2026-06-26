package com.backend.service;

import com.mybatisflex.core.service.IService;
import com.backend.model.entity.Users;
import com.backend.model.dto.UserLoginRequest;
import com.backend.model.dto.UserRegisterRequest;
import java.util.Map;

public interface UsersService extends IService<Users> {

    Long userRegister(UserRegisterRequest userRegisterRequest);

    Map<String, Object> userLogin(UserLoginRequest userLoginRequest);

    /**
     * 重置用户密码为默认密码123456
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean resetPassword(Integer userId);

    /**
     * 逻辑删除用户（设置状态为 inactive）
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Integer userId);
}
