package com.backend.service.impl;

import com.backend.common.UserContext;
import com.backend.exception.BusinessException;
import com.backend.exception.ErrorCode;
import com.backend.mapper.AppointmentsMapper;
import com.backend.mapper.DepartmentsMapper;
import com.backend.mapper.DoctorsMapper;
import com.backend.mapper.SchedulesMapper;
import com.backend.mapper.UsersMapper;
import com.backend.model.dto.AppointmentDTO;
import com.backend.model.entity.Appointments;
import com.backend.model.entity.Departments;
import com.backend.model.entity.Doctors;
import com.backend.model.entity.Schedules;
import com.backend.model.entity.Users;
import com.backend.service.AppointmentsService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  预约挂号服务实现类。
 *
 * @author 佳尔宇柔
 */
@Service
public class AppointmentsServiceImpl extends ServiceImpl<AppointmentsMapper, Appointments> implements AppointmentsService {

    @Resource
    private UsersMapper usersMapper;

    @Resource
    private DoctorsMapper doctorsMapper;

    @Resource
    private SchedulesMapper schedulesMapper;

    @Resource
    private DepartmentsMapper departmentsMapper;

    @Override
    public Map<String, Object> getAppointmentsPage(Integer current, Integer size, String patientName, String status) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .orderBy("created_at", false);

        // 根据当前用户角色决定查询范围
        String role = UserContext.getRole();
        Integer userId = UserContext.getUserId();
        if ("patient".equals(role) && userId != null) {
            // 患者只能看自己的
            queryWrapper.eq("patient_id", userId);
        }
        // 管理员和医生可以看全部，支持按患者姓名搜索

        if (patientName != null && !patientName.trim().isEmpty()) {
            List<Users> matchedUsers = usersMapper.selectListByQuery(
                    QueryWrapper.create().like("real_name", "%" + patientName.trim() + "%")
            );
            if (matchedUsers.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("records", new ArrayList<>());
                result.put("total", 0L);
                result.put("current", current);
                result.put("size", size);
                return result;
            }
            List<Integer> matchedIds = matchedUsers.stream()
                    .map(Users::getUserId).collect(Collectors.toList());
            queryWrapper.in("patient_id", matchedIds);
        }
        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq("status", status.trim());
        }

        Page<Appointments> page = new Page<>(current, size);
        page = page(page, queryWrapper);
        List<Appointments> appointmentsList = page.getRecords();

        if (appointmentsList.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("records", new ArrayList<>());
            result.put("total", 0L);
            result.put("current", current);
            result.put("size", size);
            return result;
        }

        List<Integer> patientIds = appointmentsList.stream()
                .map(Appointments::getPatientId).distinct().collect(Collectors.toList());
        List<Integer> doctorIds = appointmentsList.stream()
                .map(Appointments::getDoctorId).distinct().collect(Collectors.toList());

        Map<Integer, Users> patientMap = usersMapper.selectListByQuery(
                QueryWrapper.create().in("user_id", patientIds)
        ).stream().collect(Collectors.toMap(Users::getUserId, u -> u));

        Map<Integer, Doctors> doctorMap = doctorsMapper.selectListByQuery(
                QueryWrapper.create().in("doctor_id", doctorIds)
        ).stream().collect(Collectors.toMap(Doctors::getDoctorId, d -> d));

        List<Integer> doctorUserIds = doctorMap.values().stream()
                .map(Doctors::getUserId).distinct().collect(Collectors.toList());
        Map<Integer, Users> doctorUserMap = usersMapper.selectListByQuery(
                QueryWrapper.create().in("user_id", doctorUserIds)
        ).stream().collect(Collectors.toMap(Users::getUserId, u -> u));

        List<Integer> deptIds = doctorMap.values().stream()
                .map(Doctors::getDeptId).distinct().collect(Collectors.toList());
        Map<Integer, Departments> deptMap = departmentsMapper.selectListByQuery(
                QueryWrapper.create().in("dept_id", deptIds)
        ).stream().collect(Collectors.toMap(Departments::getDeptId, d -> d));

        List<Map<String, Object>> records = appointmentsList.stream().map(apt -> {
            Map<String, Object> item = new HashMap<>();
            item.put("appointmentId", apt.getAppointmentId());
            item.put("patientId", apt.getPatientId());
            item.put("appointmentDate", apt.getAppointmentDate());
            item.put("appointmentTime", apt.getAppointmentTime());
            item.put("status", apt.getStatus());
            item.put("createdAt", apt.getCreatedAt());

            Users patient = patientMap.get(apt.getPatientId());
            item.put("patientName", patient != null ? patient.getRealName() : null);

            Doctors doctor = doctorMap.get(apt.getDoctorId());
            if (doctor != null) {
                item.put("doctorId", doctor.getDoctorId());
                Users doctorUser = doctorUserMap.get(doctor.getUserId());
                item.put("doctorName", doctorUser != null ? doctorUser.getRealName() : null);
                item.put("doctorTitle", doctor.getTitle());
                Departments dept = deptMap.get(doctor.getDeptId());
                item.put("deptName", dept != null ? dept.getDeptName() : null);
            }

            return item;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", page.getTotalRow());
        result.put("current", current);
        result.put("size", size);
        return result;
    }

    @Override
    public void cancelAppointment(Integer appointmentId) {
        if (appointmentId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "预约ID不能为空");
        }
        Appointments apt = getById(appointmentId);
        if (apt == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "预约不存在");
        }
        apt.setStatus("cancelled");
        updateById(apt);
    }

    @Override
    public Appointments createAppointment(Appointments appointments) {
        if (appointments.getDoctorId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "医生ID不能为空");
        }

        if (appointments.getScheduleId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排班ID不能为空");
        }

        if (appointments.getAppointmentDate() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "预约日期不能为空");
        }

        if (appointments.getPatientId() == null) {
            Integer userId = UserContext.getUserId();
            if (userId != null) {
                appointments.setPatientId(userId);
            }
        }

        if (appointments.getPatientId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }

        appointments.setStatus("pending");
        save(appointments);
        return appointments;
    }

    @Override
    public List<AppointmentDTO> getPatientAppointments(Integer patientId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("patient_id", patientId);
        List<Appointments> appointmentsList = list(queryWrapper);

        if (appointmentsList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> doctorIds = appointmentsList.stream()
                .map(Appointments::getDoctorId)
                .distinct()
                .collect(Collectors.toList());
        List<Integer> scheduleIds = appointmentsList.stream()
                .map(Appointments::getScheduleId)
                .distinct()
                .collect(Collectors.toList());

        Users patient = usersMapper.selectOneById(patientId);

        Map<Integer, Doctors> doctorMap = doctorsMapper.selectListByQuery(
                QueryWrapper.create().in("doctor_id", doctorIds)
        ).stream().collect(Collectors.toMap(Doctors::getDoctorId, d -> d));

        List<Integer> doctorUserIds = doctorMap.values().stream()
                .map(Doctors::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, Users> doctorUserMap = usersMapper.selectListByQuery(
                QueryWrapper.create().in("user_id", doctorUserIds)
        ).stream().collect(Collectors.toMap(Users::getUserId, u -> u));

        Map<Integer, Schedules> scheduleMap = schedulesMapper.selectListByQuery(
                QueryWrapper.create().in("schedule_id", scheduleIds)
        ).stream().collect(Collectors.toMap(Schedules::getScheduleId, s -> s));

        return appointmentsList.stream().map(apt -> {
            Doctors doctor = doctorMap.get(apt.getDoctorId());
            Schedules schedule = scheduleMap.get(apt.getScheduleId());

            AppointmentDTO dto = AppointmentDTO.builder()
                    .appointmentId(apt.getAppointmentId())
                    .patientId(apt.getPatientId())
                    .patientName(patient != null ? patient.getRealName() : null)
                    .doctorId(apt.getDoctorId())
                    .scheduleId(apt.getScheduleId())
                    .appointmentDate(apt.getAppointmentDate())
                    .appointmentTime(apt.getAppointmentTime())
                    .status(apt.getStatus())
                    .createdAt(apt.getCreatedAt())
                    .build();

            if (doctor != null) {
                Users doctorUser = doctorUserMap.get(doctor.getUserId());
                dto.setDoctorName(doctorUser != null ? doctorUser.getRealName() : null);
                dto.setDoctorTitle(doctor.getTitle());
                dto.setDeptId(doctor.getDeptId());
            }

            if (schedule != null) {
                dto.setAppointmentTime(schedule.getStartTime());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 患者到院签到，将预约状态改为 confirmed
     */
    @Override
    public void confirmAppointment(Integer appointmentId) {
        if (appointmentId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "预约ID不能为空");
        }
        Appointments apt = getById(appointmentId);
        if (apt == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "预约不存在");
        }
        if (!"pending".equals(apt.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前状态不支持签到操作");
        }
        apt.setStatus("confirmed");
        updateById(apt);
    }

    /**
     * 医生完成就诊，将预约状态改为 completed
     */
    @Override
    public void completeAppointment(Integer appointmentId) {
        if (appointmentId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "预约ID不能为空");
        }
        Appointments apt = getById(appointmentId);
        if (apt == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "预约不存在");
        }
        if (!"processing".equals(apt.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前状态不支持完成操作");
        }
        apt.setStatus("completed");
        updateById(apt);
    }

    /**
     * 医生获取自己的出诊列表（包含患者信息）
     */
    public List<Map<String, Object>> getDoctorAppointments(Integer doctorUserId) {
        Doctors doctor = doctorsMapper.selectOneByQuery(
                QueryWrapper.create().eq("user_id", doctorUserId)
        );
        if (doctor == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "医生信息不存在");
        }

        List<Appointments> appointmentsList = list(
                QueryWrapper.create()
                        .eq("doctor_id", doctor.getDoctorId())
                        .orderBy("appointment_date", false)
        );

        if (appointmentsList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> patientIds = appointmentsList.stream()
                .map(Appointments::getPatientId).distinct().collect(Collectors.toList());

        Map<Integer, Users> patientMap = usersMapper.selectListByQuery(
                QueryWrapper.create().in("user_id", patientIds)
        ).stream().collect(Collectors.toMap(Users::getUserId, u -> u));

        return appointmentsList.stream().map(apt -> {
            Map<String, Object> item = new HashMap<>();
            item.put("appointmentId", apt.getAppointmentId());
            item.put("appointmentDate", apt.getAppointmentDate());
            item.put("appointmentTime", apt.getAppointmentTime());
            item.put("status", apt.getStatus());

            Users patient = patientMap.get(apt.getPatientId());
            item.put("patientId", apt.getPatientId());
            item.put("patientName", patient != null ? patient.getRealName() : "患者" + apt.getPatientId());

            return item;
        }).collect(Collectors.toList());
    }

}
