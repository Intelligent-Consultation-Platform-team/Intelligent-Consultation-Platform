package com.backend.service.impl;

import com.backend.mapper.DoctorsMapper;
import com.backend.model.dto.DoctorDTO;
import com.backend.model.entity.Doctors;
import com.backend.model.entity.Users;
import com.backend.service.DoctorsService;
import com.backend.service.UsersService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  医生服务实现类。
 *
 * @author 佳尔宇柔
 */
@Service
public class DoctorsServiceImpl extends ServiceImpl<DoctorsMapper, Doctors> implements DoctorsService {

    @Resource
    private UsersService usersService;

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
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
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
        Map<Integer, String> deptMap = new HashMap<>();
        deptMap.put(1, "内科");
        deptMap.put(2, "外科");
        deptMap.put(3, "妇产科");
        deptMap.put(4, "儿科");
        deptMap.put(5, "急诊科");
        deptMap.put(6, "皮肤科");
        return deptMap.getOrDefault(deptId, "未知科室");
    }

    @Override
    public boolean addDoctor(Doctors doctors) {
        return save(doctors);
    }

    @Override
    public boolean updateDoctor(Doctors doctors) {
        return updateById(doctors);
    }

    @Override
    public boolean deleteDoctor(Long doctorId) {
        return removeById(doctorId);
    }

    @Override
    public Doctors getDoctorById(Long doctorId) {
        return getById(doctorId);
    }

}