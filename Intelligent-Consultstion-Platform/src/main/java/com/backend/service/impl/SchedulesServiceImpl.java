package com.backend.service.impl;

import com.backend.mapper.DepartmentsMapper;
import com.backend.mapper.SchedulesMapper;
import com.backend.model.dto.ScheduleDTO;
import com.backend.model.entity.Departments;
import com.backend.model.entity.Doctors;
import com.backend.model.entity.Schedules;
import com.backend.model.entity.Users;
import com.backend.service.DoctorsService;
import com.backend.service.SchedulesService;
import com.backend.service.UsersService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  排班服务实现类。
 *
 * @author 佳尔宇柔
 */
@Service
public class SchedulesServiceImpl extends ServiceImpl<SchedulesMapper, Schedules> implements SchedulesService {

    @Resource
    private DoctorsService doctorsService;

    @Resource
    private UsersService usersService;

    @Resource
    private DepartmentsMapper departmentsMapper;

    @Override
    public List<Schedules> getDoctorSchedules(Integer doctorId, String date) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (doctorId != null) {
            queryWrapper.eq("doctor_id", doctorId);
        }
        if (date != null) {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            int dayOfWeek = localDate.getDayOfWeek().getValue();
            queryWrapper.eq("day_of_week", dayOfWeek);
        }
        return list(queryWrapper);
    }

    @Override
    public List<ScheduleDTO> getSchedulesWithDoctor(Integer deptId, Integer doctorId, String date) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        
        if (doctorId != null) {
            queryWrapper.eq("doctor_id", doctorId);
        }
        
        if (date != null) {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            int dayOfWeek = localDate.getDayOfWeek().getValue();
            queryWrapper.eq("day_of_week", dayOfWeek);
        }

        List<Schedules> schedulesList = list(queryWrapper);
        
        List<Integer> doctorIds = schedulesList.stream()
                .map(Schedules::getDoctorId)
                .distinct()
                .toList();
        
        Map<Integer, Doctors> doctorMap = new HashMap<>();
        if (!doctorIds.isEmpty()) {
            List<Doctors> doctors = doctorsService.list(
                QueryWrapper.create().in("doctor_id", doctorIds)
            );
            for (Doctors doctor : doctors) {
                doctorMap.put(doctor.getDoctorId(), doctor);
            }
        }
        
        List<Integer> userIds = doctorMap.values().stream()
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
        
        List<ScheduleDTO> result = new ArrayList<>();
        for (Schedules schedule : schedulesList) {
            Doctors doctor = doctorMap.get(schedule.getDoctorId());
            if (doctor != null && (deptId == null || deptId.equals(doctor.getDeptId()))) {
                Users user = userMap.get(doctor.getUserId());
                String doctorName = user != null ? user.getRealName() : "未知医生";
                
                result.add(ScheduleDTO.builder()
                        .scheduleId(schedule.getScheduleId())
                        .doctorId(doctor.getDoctorId())
                        .doctorName(doctorName)
                        .title(doctor.getTitle())
                        .deptId(doctor.getDeptId())
                        .deptName(getDeptName(doctor.getDeptId()))
                        .dayOfWeek(schedule.getDayOfWeek())
                        .date(date)
                        .startTime(schedule.getStartTime())
                        .endTime(schedule.getEndTime())
                        .availableSlots(schedule.getAvailableSlots())
                        .maxNumber(schedule.getAvailableSlots())
                        .remaining(schedule.getAvailableSlots())
                        .status(schedule.getStatus())
                        .createdAt(schedule.getCreatedAt())
                        .build());
            }
        }
        
        return result;
    }

    private Map<Integer, String> deptNameCache = null;

    private String getDeptName(Integer deptId) {
        if (deptNameCache == null) {
            List<Departments> allDepts = departmentsMapper.selectAll();
            deptNameCache = allDepts.stream()
                    .collect(Collectors.toMap(Departments::getDeptId, Departments::getDeptName));
        }
        return deptNameCache.getOrDefault(deptId, "未知科室");
    }

    @Override
    public boolean addSchedule(Schedules schedules) {
        if (schedules.getAvailableSlots() == null) {
            schedules.setAvailableSlots(20);
        }
        if (schedules.getStatus() == null) {
            schedules.setStatus("active");
        }
        return save(schedules);
    }

    @Override
    public boolean updateSchedule(Schedules schedules) {
        return updateById(schedules);
    }

    @Override
    public boolean deleteSchedule(Long scheduleId) {
        return removeById(scheduleId);
    }

    @Override
    public Schedules getScheduleById(Long scheduleId) {
        return getById(scheduleId);
    }

}