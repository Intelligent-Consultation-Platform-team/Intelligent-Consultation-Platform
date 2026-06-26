package com.backend.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.backend.exception.BusinessException;
import com.backend.exception.ErrorCode;
import com.backend.mapper.DoctorsMapper;
import com.backend.mapper.PatientsMapper;
import com.backend.mapper.AdminsMapper;
import com.backend.mapper.UsersMapper;
import com.backend.model.dto.UserLoginRequest;
import com.backend.model.dto.UserRegisterRequest;
import com.backend.model.entity.Doctors;
import com.backend.model.entity.Patients;
import com.backend.model.entity.Admins;
import com.backend.model.entity.Users;
import com.backend.service.UsersService;
import com.backend.utils.JwtUtils;
import com.backend.utils.PasswordUtils;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private DoctorsMapper doctorsMapper;

    @Autowired
    private PatientsMapper patientsMapper;

    @Autowired
    private AdminsMapper adminsMapper;

    @Override
    public Long userRegister(UserRegisterRequest userRegisterRequest) {
        String username = userRegisterRequest.getUsername();
        String realName = userRegisterRequest.getRealName();
        String phone = userRegisterRequest.getPhone();
        String email = userRegisterRequest.getEmail();
        String password = userRegisterRequest.getPassword();
        String confirmPassword = userRegisterRequest.getConfirmPassword();
        String role = userRegisterRequest.getRole();

        // 如果 realName 为空，用 username 代替
        if (StrUtil.isBlank(realName)) {
            realName = username;
        }

        if (StrUtil.hasBlank(username, phone, email, password, confirmPassword, role)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }

        if (username.length() < 3 || username.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名长度必须在3-20位之间");
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名只能包含字母、数字和下划线");
        }

        if (realName != null && (realName.length() < 2 || realName.length() > 20)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "真实姓名长度必须在2-20位之间");
        }

        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式错误");
        }

        if (!Validator.isEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_FORMAT_ERROR, "邮箱格式错误");
        }

        if (password.length() < 6 || password.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度必须在6-32位之间");
        }

        if (!password.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致");
        }

        if (!role.matches("^(patient|doctor|admin)$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色必须是patient、doctor或admin");
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("username", username);
        long usernameCount = count(queryWrapper);
        if (usernameCount > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS, "用户名已存在");
        }

        queryWrapper = QueryWrapper.create()
                .eq("email", email);
        long emailCount = count(queryWrapper);
        if (emailCount > 0) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS, "邮箱已注册");
        }

        queryWrapper = QueryWrapper.create()
                .eq("phone", phone);
        long phoneCount = count(queryWrapper);
        if (phoneCount > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号已注册");
        }

        String encryptedPassword = PasswordUtils.encrypt(password);

        Users user = Users.builder()
                .username(username)
                .password(encryptedPassword)
                .realName(realName)
                .phone(phone)
                .email(email)
                .role(role)
                .status("active")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();

        boolean saved = save(user);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }

        if ("admin".equals(role)) {
            Admins admin = Admins.builder()
                    .userId(user.getUserId())
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .updatedAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            adminsMapper.insert(admin);
        }

        if ("doctor".equals(role)) {
            Doctors doctor = Doctors.builder()
                    .userId(user.getUserId())
                    .deptId(0)
                    .status("available")
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .updatedAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            doctorsMapper.insert(doctor);
        }

        if ("patient".equals(role)) {
            Patients patient = Patients.builder()
                    .userId(user.getUserId())
                    .idCard("TEMP_" + user.getUserId())
                    .balance(java.math.BigDecimal.ZERO)
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .updatedAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            patientsMapper.insert(patient);
        }

        return user.getUserId().longValue();
    }

    @Override
    public Map<String, Object> userLogin(UserLoginRequest userLoginRequest) {
        String username = userLoginRequest.getUsername();
        String password = userLoginRequest.getPassword();

        Users user = null;
        
        if (username.matches("^1[3-9]\\d{9}$")) {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq("phone", username);
            user = getOne(queryWrapper);
        } else {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq("username", username);
            user = getOne(queryWrapper);
        }

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_OR_PASSWORD_ERROR, "用户名或密码错误");
        }

        if (!PasswordUtils.verify(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.USER_OR_PASSWORD_ERROR, "用户名或密码错误");
        }

        String token = jwtUtils.generateToken(user.getUserId(), user.getUsername(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("tokenType", "Bearer");
        result.put("expiresIn", jwtUtils.getExpirationTime());
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getUserId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole());
        result.put("user", userInfo);

        return result;
    }

    @Override
    public boolean resetPassword(Integer userId) {
        if (userId == null) {
            return false;
        }
        String encryptedPassword = PasswordUtils.encrypt("123456");
        Users user = Users.builder()
                .userId(userId)
                .password(encryptedPassword)
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        return updateById(user);
    }

    @Override
    public boolean deleteUser(Integer userId) {
        if (userId == null) {
            return false;
        }
        Users user = Users.builder()
                .userId(userId)
                .status("inactive")
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        return updateById(user);
    }
}
