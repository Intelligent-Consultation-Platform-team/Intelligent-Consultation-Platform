package com.backend.service.impl;

import com.backend.common.UserContext;
import com.backend.exception.BusinessException;
import com.backend.exception.ErrorCode;
import com.backend.mapper.AppointmentsMapper;
import com.backend.mapper.ConsultationsMapper;
import com.backend.mapper.DepartmentsMapper;
import com.backend.mapper.DoctorsMapper;
import com.backend.mapper.PatientsMapper;
import com.backend.mapper.SchedulesMapper;
import com.backend.mapper.UsersMapper;
import com.backend.model.dto.AppointmentDTO;
import com.backend.model.entity.Appointments;
import com.backend.model.entity.Consultations;
import com.backend.model.entity.Departments;
import com.backend.model.entity.Doctors;
import com.backend.model.entity.Patients;
import com.backend.model.entity.Schedules;
import com.backend.model.entity.Users;
import com.backend.service.AppointmentsService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
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
    private PatientsMapper patientsMapper;

    @Resource
    private DoctorsMapper doctorsMapper;

    @Resource
    private SchedulesMapper schedulesMapper;

    @Resource
    private DepartmentsMapper departmentsMapper;

    @Resource
    private ConsultationsMapper consultationsMapper;

    @Override
    public Map<String, Object> getAppointmentsPage(Integer current, Integer size, String patientName, String status) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .orderBy("created_at", false);

        // 根据当前用户角色决定查询范围
        String role = UserContext.getRole();
        Integer userId = UserContext.getUserId();
        if ("patient".equals(role) && userId != null) {
            Patients patient = patientsMapper.selectOneByQuery(
                    QueryWrapper.create().eq("user_id", userId));
            if (patient != null) {
                queryWrapper.eq("patient_id", patient.getPatientId());
            }
        } else if ("doctor".equals(role) && userId != null) {
            Doctors doctor = doctorsMapper.selectOneByQuery(
                    QueryWrapper.create().eq("user_id", userId));
            if (doctor != null) {
                queryWrapper.eq("doctor_id", doctor.getDoctorId());
            }
        }
        // 管理员看全部

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
            List<Integer> matchedUserIds = matchedUsers.stream()
                    .map(Users::getUserId).collect(Collectors.toList());
            List<Patients> matchedPatients = patientsMapper.selectListByQuery(
                    QueryWrapper.create().in("user_id", matchedUserIds)
            );
            List<Integer> matchedPatientIds = matchedPatients.stream()
                    .map(Patients::getPatientId).collect(Collectors.toList());
            if (matchedPatientIds.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("records", new ArrayList<>());
                result.put("total", 0L);
                result.put("current", current);
                result.put("size", size);
                return result;
            }
            queryWrapper.in("patient_id", matchedPatientIds);
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

        Map<Integer, Patients> patientIdMap = patientsMapper.selectListByQuery(
                QueryWrapper.create().in("patient_id", patientIds)
        ).stream().collect(Collectors.toMap(Patients::getPatientId, p -> p));
        List<Integer> patientUserIds = patientIdMap.values().stream()
                .map(Patients::getUserId).distinct().collect(Collectors.toList());
        Map<Integer, Users> patientUserMap = patientUserIds.isEmpty() ? new HashMap<>() :
                usersMapper.selectListByQuery(
                        QueryWrapper.create().in("user_id", patientUserIds)
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
            item.put("symptoms", apt.getSymptoms());
            item.put("createdAt", apt.getCreatedAt());

            Patients patientRecord = patientIdMap.get(apt.getPatientId());
            if (patientRecord != null) {
                Users patientUser = patientUserMap.get(patientRecord.getUserId());
                item.put("patientName", patientUser != null ? patientUser.getRealName() : null);
            } else {
                item.put("patientName", null);
            }

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
        if ("cancelled".equals(apt.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "预约已取消，无需重复操作");
        }
        apt.setStatus("cancelled");
        updateById(apt);

        // 恢复号源
        Schedules schedule = schedulesMapper.selectOneById(apt.getScheduleId());
        if (schedule != null) {
            Schedules updateSchedule = new Schedules();
            updateSchedule.setAvailableSlots(schedule.getAvailableSlots() + 1);
            schedulesMapper.updateByQuery(updateSchedule,
                    QueryWrapper.create().eq("schedule_id", schedule.getScheduleId()));
        }
    }

    @Override
    public Appointments createAppointment(Appointments appointments) {
        if (appointments.getScheduleId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排班ID不能为空");
        }
        if (appointments.getAppointmentDate() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "预约日期不能为空");
        }

        // 查询排班
        Schedules schedule = schedulesMapper.selectOneById(appointments.getScheduleId());
        if (schedule == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "排班不存在");
        }
        if (!"active".equals(schedule.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该排班已停用");
        }

        // 校验预约日期必须匹配排班的星期几
        LocalDate appointmentLocalDate = appointments.getAppointmentDate().toLocalDate();
        int dayOfWeek = appointmentLocalDate.getDayOfWeek().getValue();
        if (!String.valueOf(dayOfWeek).equals(schedule.getDayOfWeek())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "预约日期与医生排班不匹配，该医生仅在此时间段出诊");
        }

        // 校验日期：不能是过去，且只能在未来15天内
        LocalDate today = LocalDate.now();
        if (appointmentLocalDate.isBefore(today)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能预约过去的日期");
        }
        if (appointmentLocalDate.isAfter(today.plusDays(15))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只能预约未来15天内的号源");
        }

        // 校验号源
        if (schedule.getAvailableSlots() == null || schedule.getAvailableSlots() <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该排班号源已用完");
        }

        // 解析患者ID：患者角色自动获取，管理员/医生从请求体传入
        String role = UserContext.getRole();
        Integer userId = UserContext.getUserId();
        if ("patient".equals(role) && userId != null) {
            Patients patient = patientsMapper.selectOneByQuery(
                    QueryWrapper.create().eq("user_id", userId));
            if (patient != null) {
                appointments.setPatientId(patient.getPatientId());
            }
        }
        if (appointments.getPatientId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请指定就诊患者");
        }

        // 防重：同一患者同一排班同一日期已有非取消预约时拒绝
        long existing = count(QueryWrapper.create()
                .eq("patient_id", appointments.getPatientId())
                .eq("schedule_id", appointments.getScheduleId())
                .eq("appointment_date", appointments.getAppointmentDate())
                .ne("status", "cancelled"));
        if (existing > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该时段您已有预约，请勿重复预约");
        }

        // 扣减号源
        Schedules updateSchedule = new Schedules();
        updateSchedule.setAvailableSlots(schedule.getAvailableSlots() - 1);
        schedulesMapper.updateByQuery(updateSchedule,
                QueryWrapper.create().eq("schedule_id", schedule.getScheduleId()));

        // 设置医生ID和预约时间（来自排班）
        appointments.setDoctorId(schedule.getDoctorId());
        if (appointments.getAppointmentTime() == null) {
            appointments.setAppointmentTime(schedule.getStartTime());
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
     * 接诊，将预约状态改为 processing，并创建就诊记录
     */
    @Override
    public void processAppointment(Integer appointmentId) {
        if (appointmentId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "预约ID不能为空");
        }
        Appointments apt = getById(appointmentId);
        if (apt == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "预约不存在");
        }
        if (!"pending".equals(apt.getStatus()) && !"confirmed".equals(apt.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前状态不支持接诊操作");
        }
        Appointments update = Appointments.builder()
                .appointmentId(appointmentId)
                .status("processing")
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        updateById(update);

        // 创建就诊记录
        Consultations consultation = Consultations.builder()
                .appointmentId(appointmentId)
                .doctorId(apt.getDoctorId())
                .patientId(apt.getPatientId())
                .symptoms(apt.getSymptoms())
                .consultationDate(new Timestamp(System.currentTimeMillis()))
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        consultationsMapper.insert(consultation);
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

        Map<Integer, Patients> patientIdMap = patientsMapper.selectListByQuery(
                QueryWrapper.create().in("patient_id", patientIds)
        ).stream().collect(Collectors.toMap(Patients::getPatientId, p -> p));
        List<Integer> patientUserIds = patientIdMap.values().stream()
                .map(Patients::getUserId).distinct().collect(Collectors.toList());
        Map<Integer, Users> patientUserMap = patientUserIds.isEmpty() ? new HashMap<>() :
                usersMapper.selectListByQuery(
                        QueryWrapper.create().in("user_id", patientUserIds)
                ).stream().collect(Collectors.toMap(Users::getUserId, u -> u));

        return appointmentsList.stream().map(apt -> {
            Map<String, Object> item = new HashMap<>();
            item.put("appointmentId", apt.getAppointmentId());
            item.put("appointmentDate", apt.getAppointmentDate());
            item.put("appointmentTime", apt.getAppointmentTime());
            item.put("status", apt.getStatus());
            item.put("symptoms", apt.getSymptoms());

            Patients patientRecord = patientIdMap.get(apt.getPatientId());
            item.put("patientId", apt.getPatientId());
            if (patientRecord != null) {
                Users patientUser = patientUserMap.get(patientRecord.getUserId());
                item.put("patientName", patientUser != null ? patientUser.getRealName() : "患者" + apt.getPatientId());
            } else {
                item.put("patientName", "患者" + apt.getPatientId());
            }

            return item;
        }).collect(Collectors.toList());
    }

}
