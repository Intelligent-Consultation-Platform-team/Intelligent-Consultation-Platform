package com.backend.controller;

import com.backend.common.BaseResponse;
import com.backend.common.ResultUtils;
import com.backend.common.UserContext;
import com.backend.exception.BusinessException;
import com.backend.exception.ErrorCode;
import com.backend.mapper.AdminsMapper;
import com.backend.mapper.DoctorsMapper;
import com.backend.mapper.PatientsMapper;
import com.backend.mapper.UsersMapper;
import com.backend.model.dto.UserLoginRequest;
import com.backend.model.dto.UserRegisterRequest;
import com.backend.model.entity.Admins;
import com.backend.model.entity.Doctors;
import com.backend.model.entity.Patients;
import com.backend.model.entity.Users;
import com.backend.service.UsersService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private UsersService usersService;

    @Resource
    private UsersMapper usersMapper;

    @Resource
    private DoctorsMapper doctorsMapper;

    @Resource
    private PatientsMapper patientsMapper;

    @Resource
    private AdminsMapper adminsMapper;

    @PostMapping("/register")
    public BaseResponse<Map<String, Object>> register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        Long userId = usersService.userRegister(userRegisterRequest);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", userRegisterRequest.getUsername());
        data.put("realName", userRegisterRequest.getRealName());
        data.put("role", userRegisterRequest.getRole());
        data.put("createdAt", new java.util.Date().toInstant());
        return ResultUtils.success(data, "注册成功");
    }

    @PostMapping("/login")
    public BaseResponse<Map<String, Object>> login(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        Map<String, Object> result = usersService.userLogin(userLoginRequest);
        return ResultUtils.success(result, "登录成功");
    }

    /**
     * 获取当前登录用户的完整信息
     */
    @GetMapping("/me")
    public BaseResponse<Map<String, Object>> getCurrentUser() {
        Integer userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        Users user = usersMapper.selectOneById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("phone", user.getPhone());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());
        data.put("status", user.getStatus());

        String role = user.getRole();
        if ("doctor".equals(role)) {
            Doctors doctor = doctorsMapper.selectOneByQuery(
                    QueryWrapper.create().eq("user_id", userId));
            if (doctor != null) {
                data.put("title", doctor.getTitle());
                data.put("specialty", doctor.getSpecialty());
                data.put("bio", doctor.getBio());
                data.put("deptId", doctor.getDeptId());
                data.put("doctorStatus", doctor.getStatus());
            }
        } else if ("patient".equals(role)) {
            Patients patient = patientsMapper.selectOneByQuery(
                    QueryWrapper.create().eq("user_id", userId));
            if (patient != null) {
                data.put("idCard", patient.getIdCard());
                data.put("gender", patient.getGender());
                data.put("age", patient.getAge());
                data.put("address", patient.getAddress());
                data.put("balance", patient.getBalance());
            }
        } else if ("admin".equals(role)) {
            Admins admin = adminsMapper.selectOneByQuery(
                    QueryWrapper.create().eq("user_id", userId));
            if (admin != null) {
                data.put("department", admin.getDepartment());
                data.put("position", admin.getPosition());
            }
        }

        return ResultUtils.success(data);
    }

    /**
     * 更新当前登录用户的个人信息
     */
    @PutMapping("/profile")
    public BaseResponse<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> body) {
        Integer userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        Users user = usersMapper.selectOneById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 更新 users 表
        Users updateUser = Users.builder()
                .userId(userId)
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        boolean userChanged = false;

        if (body.containsKey("realName")) {
            updateUser.setRealName((String) body.get("realName"));
            userChanged = true;
        }
        if (body.containsKey("phone")) {
            updateUser.setPhone((String) body.get("phone"));
            userChanged = true;
        }
        if (body.containsKey("email")) {
            updateUser.setEmail((String) body.get("email"));
            userChanged = true;
        }
        if (userChanged) {
            usersMapper.update(updateUser);
        }

        String role = user.getRole();
        if ("doctor".equals(role)) {
            Doctors doctor = doctorsMapper.selectOneByQuery(
                    QueryWrapper.create().eq("user_id", userId));
            if (doctor != null) {
                Doctors updateDoctor = Doctors.builder()
                        .doctorId(doctor.getDoctorId())
                        .updatedAt(new Timestamp(System.currentTimeMillis()))
                        .build();
                boolean doctorChanged = false;
                if (body.containsKey("title")) {
                    updateDoctor.setTitle((String) body.get("title"));
                    doctorChanged = true;
                }
                if (body.containsKey("specialty")) {
                    updateDoctor.setSpecialty((String) body.get("specialty"));
                    doctorChanged = true;
                }
                if (body.containsKey("bio")) {
                    updateDoctor.setBio((String) body.get("bio"));
                    doctorChanged = true;
                }
                if (doctorChanged) {
                    doctorsMapper.update(updateDoctor);
                }
            }
        } else if ("patient".equals(role)) {
            Patients patient = patientsMapper.selectOneByQuery(
                    QueryWrapper.create().eq("user_id", userId));
            if (patient != null) {
                Patients updatePatient = Patients.builder()
                        .patientId(patient.getPatientId())
                        .updatedAt(new Timestamp(System.currentTimeMillis()))
                        .build();
                boolean patientChanged = false;
                if (body.containsKey("gender")) {
                    updatePatient.setGender((String) body.get("gender"));
                    patientChanged = true;
                }
                if (body.containsKey("age")) {
                    Object ageVal = body.get("age");
                    updatePatient.setAge(ageVal != null ? Integer.valueOf(ageVal.toString()) : null);
                    patientChanged = true;
                }
                if (body.containsKey("address")) {
                    updatePatient.setAddress((String) body.get("address"));
                    patientChanged = true;
                }
                if (patientChanged) {
                    patientsMapper.update(updatePatient);
                }
            }
        } else if ("admin".equals(role)) {
            Admins admin = adminsMapper.selectOneByQuery(
                    QueryWrapper.create().eq("user_id", userId));
            if (admin != null) {
                Admins updateAdmin = Admins.builder()
                        .adminId(admin.getAdminId())
                        .updatedAt(new Timestamp(System.currentTimeMillis()))
                        .build();
                boolean adminChanged = false;
                if (body.containsKey("department")) {
                    updateAdmin.setDepartment((String) body.get("department"));
                    adminChanged = true;
                }
                if (body.containsKey("position")) {
                    updateAdmin.setPosition((String) body.get("position"));
                    adminChanged = true;
                }
                if (adminChanged) {
                    adminsMapper.update(updateAdmin);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        return ResultUtils.success(result, "个人信息已更新");
    }
}
