package com.backend.service.impl;

import com.backend.mapper.DepartmentsMapper;
import com.backend.mapper.DoctorsMapper;
import com.backend.mapper.UsersMapper;
import com.backend.model.dto.DoctorAddRequest;
import com.backend.model.dto.DoctorDTO;
import com.backend.model.dto.DoctorUpdateRequest;
import com.backend.model.entity.Departments;
import com.backend.model.entity.Doctors;
import com.backend.model.entity.Users;
import com.backend.service.DoctorsService;
import com.backend.service.UsersService;
import com.backend.utils.PasswordUtils;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.sql.Timestamp;

/**
 *  医生服务实现类。
 *
 * @author 佳尔宇柔
 */
@Service
public class DoctorsServiceImpl extends ServiceImpl<DoctorsMapper, Doctors> implements DoctorsService {

    @Resource
    private UsersService usersService;

    @Resource
    private DepartmentsMapper departmentsMapper;

    private Map<Integer, String> deptNameCache = null;

    @Override
    public List<Doctors> getDoctorsList(Integer deptId, String status) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (deptId != null) {
            queryWrapper.eq("dept_id", deptId);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        return list(queryWrapper);
    }

    @Override
    public List<DoctorDTO> getDoctorsWithUserInfo(Integer deptId, String status) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (deptId != null) {
            queryWrapper.eq("dept_id", deptId);
        }
        queryWrapper.eq("status", status != null ? status : "available");

        List<Doctors> doctorsList = list(queryWrapper);
        
        List<Integer> userIds = doctorsList.stream()
                .map(Doctors::getUserId)
                .distinct()
                .toList();
        
        Map<Integer, Users> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<Users> users = usersService.list(
                QueryWrapper.create().in("user_id", userIds)
            );
            for (Users user : users) {
                userMap.put(user.getUserId(), user);
            }
        }
        
        List<DoctorDTO> result = new ArrayList<>();
        for (Doctors doctor : doctorsList) {
            Users user = userMap.get(doctor.getUserId());
            String realName = user != null ? user.getRealName() : "未知";
            String phone = user != null ? user.getPhone() : "";
            
            result.add(DoctorDTO.builder()
                    .doctorId(doctor.getDoctorId())
                    .userId(doctor.getUserId())
                    .username(user != null ? user.getUsername() : null)
                    .realName(realName)
                    .title(doctor.getTitle())
                    .deptId(doctor.getDeptId())
                    .deptName(getDeptName(doctor.getDeptId()))
                    .specialty(doctor.getSpecialty())
                    .bio(doctor.getBio())
                    .status(doctor.getStatus())
                    .phone(phone)
                    .createdAt(doctor.getCreatedAt())
                    .build());
        }
        
        return result;
    }

    private String getDeptName(Integer deptId) {
        if (deptNameCache == null) {
            List<Departments> allDepts = departmentsMapper.selectAll();
            deptNameCache = allDepts.stream()
                    .collect(Collectors.toMap(Departments::getDeptId, Departments::getDeptName));
        }
        return deptNameCache.getOrDefault(deptId, deptId != null ? "科室" + deptId : "未分配");
    }

    @Override
    public boolean addDoctor(DoctorAddRequest request) {
        // 检查用户名是否已存在
        QueryWrapper checkWrapper = QueryWrapper.create().eq("username", request.getUsername());
        if (usersService.count(checkWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 1. 创建用户账户
        Users user = Users.builder()
                .username(request.getUsername())
                .password(PasswordUtils.encrypt("123456"))
                .realName(request.getRealName())
                .phone(request.getPhone())
                .role("doctor")
                .status("active")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        boolean userSaved = usersService.save(user);
        if (!userSaved) {
            return false;
        }

        // 2. 创建医生记录，关联用户
        Doctors doctor = Doctors.builder()
                .userId(user.getUserId())
                .deptId(request.getDeptId())
                .title(request.getTitle())
                .specialty(request.getSpecialty())
                .bio(request.getBio())
                .status("available")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        return save(doctor);
    }

    @Override
    public boolean updateDoctor(DoctorUpdateRequest request) {
        // 1. 更新医生信息
        Doctors doctor = Doctors.builder()
                .doctorId(request.getDoctorId())
                .deptId(request.getDeptId())
                .title(request.getTitle())
                .specialty(request.getSpecialty())
                .bio(request.getBio())
                .status(request.getStatus())
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        boolean doctorUpdated = updateById(doctor);

        // 2. 更新关联的用户信息
        if (request.getUserId() != null && (request.getRealName() != null || request.getPhone() != null)) {
            Users user = Users.builder()
                    .userId(request.getUserId())
                    .realName(request.getRealName())
                    .phone(request.getPhone())
                    .updatedAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            usersService.updateById(user);
        }

        return doctorUpdated;
    }

    @Override
    @Transactional
    public boolean deleteDoctor(Long doctorId) {
        Doctors doctor = getById(doctorId);
        if (doctor == null) {
            return false;
        }
        Doctors updateDoctor = Doctors.builder()
                .doctorId(Math.toIntExact(doctorId))
                .status("unavailable")
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        boolean result = updateById(updateDoctor);
        if (result && doctor.getUserId() != null) {
            usersService.deleteUser(doctor.getUserId());
        }
        return result;
    }

    @Override
    public Doctors getDoctorById(Long doctorId) {
        return getById(doctorId);
    }

}