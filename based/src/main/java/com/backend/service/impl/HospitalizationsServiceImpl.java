package com.backend.service.impl;

import com.backend.common.UserContext;
import com.backend.exception.BusinessException;
import com.backend.exception.ErrorCode;
import com.backend.mapper.DepartmentsMapper;
import com.backend.mapper.DoctorsMapper;
import com.backend.mapper.HospitalizationsMapper;
import com.backend.mapper.PatientsMapper;
import com.backend.mapper.UsersMapper;
import com.backend.model.entity.Departments;
import com.backend.model.entity.Doctors;
import com.backend.model.entity.Hospitalizations;
import com.backend.model.entity.Patients;
import com.backend.model.entity.Users;
import com.backend.service.HospitalizationsService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 住院管理服务实现类。
 *
 * @author 佳尔宇柔
 */
@Service
public class HospitalizationsServiceImpl extends ServiceImpl<HospitalizationsMapper, Hospitalizations> implements HospitalizationsService {

    @Resource
    private PatientsMapper patientsMapper;

    @Resource
    private UsersMapper usersMapper;

    @Resource
    private DoctorsMapper doctorsMapper;

    @Resource
    private DepartmentsMapper departmentsMapper;

    @Override
    public Hospitalizations createHospitalization(Hospitalizations hospitalizations) {
        if (hospitalizations.getPatientId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择患者");
        }
        if (hospitalizations.getDoctorId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择主治医生");
        }
        if (hospitalizations.getDeptId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择科室");
        }
        // 检查患者是否已住院
        long activeCount = count(QueryWrapper.create()
                .eq("patient_id", hospitalizations.getPatientId())
                .eq("status", "admitted"));
        if (activeCount > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该患者已住院，不能重复登记");
        }

        // 检查病房号+床位号是否已被占用
        if (hospitalizations.getWardNumber() != null && hospitalizations.getBedNumber() != null) {
            long bedUsed = count(QueryWrapper.create()
                    .eq("ward_number", hospitalizations.getWardNumber())
                    .eq("bed_number", hospitalizations.getBedNumber())
                    .eq("status", "admitted"));
            if (bedUsed > 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR,
                        "病房" + hospitalizations.getWardNumber() + "床位" + hospitalizations.getBedNumber() + "已被占用");
            }
        }

        if (hospitalizations.getAdmissionDate() == null) {
            hospitalizations.setAdmissionDate(new Timestamp(System.currentTimeMillis()));
        }
        hospitalizations.setStatus("admitted");
        save(hospitalizations);
        return hospitalizations;
    }

    @Override
    public Map<String, Object> getPage(Integer current, Integer size, String patientName, String status) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .orderBy("created_at", false);

        if (patientName != null && !patientName.trim().isEmpty()) {
            List<Users> matchedUsers = usersMapper.selectListByQuery(
                    QueryWrapper.create().like("real_name", "%" + patientName.trim() + "%")
            );
            if (matchedUsers.isEmpty()) {
                return emptyPage(current, size);
            }
            List<Integer> matchedUserIds = matchedUsers.stream()
                    .map(Users::getUserId).collect(Collectors.toList());
            List<Patients> matchedPatients = patientsMapper.selectListByQuery(
                    QueryWrapper.create().in("user_id", matchedUserIds)
            );
            List<Integer> matchedPatientIds = matchedPatients.stream()
                    .map(Patients::getPatientId).collect(Collectors.toList());
            if (matchedPatientIds.isEmpty()) {
                return emptyPage(current, size);
            }
            queryWrapper.in("patient_id", matchedPatientIds);
        }

        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq("status", status.trim());
        }

        // 医生只看自己的住院患者，管理员看全部
        String role = UserContext.getRole();
        Integer userId = UserContext.getUserId();
        if ("doctor".equals(role) && userId != null) {
            Doctors doctor = doctorsMapper.selectOneByQuery(
                    QueryWrapper.create().eq("user_id", userId));
            if (doctor != null) {
                queryWrapper.eq("doctor_id", doctor.getDoctorId());
            }
        }

        Page<Hospitalizations> page = new Page<>(current, size);
        page = page(page, queryWrapper);
        List<Hospitalizations> list = page.getRecords();

        if (list.isEmpty()) {
            return emptyPage(current, size);
        }

        List<Integer> patientIds = list.stream()
                .map(Hospitalizations::getPatientId).distinct().collect(Collectors.toList());
        List<Integer> doctorIds = list.stream()
                .map(Hospitalizations::getDoctorId).distinct().collect(Collectors.toList());
        List<Integer> deptIds = list.stream()
                .map(Hospitalizations::getDeptId).distinct().collect(Collectors.toList());

        Map<Integer, Patients> patientMap = patientsMapper.selectListByQuery(
                QueryWrapper.create().in("patient_id", patientIds)
        ).stream().collect(Collectors.toMap(Patients::getPatientId, p -> p));
        List<Integer> patientUserIds = patientMap.values().stream()
                .map(Patients::getUserId).distinct().collect(Collectors.toList());
        Map<Integer, Users> userMap = patientUserIds.isEmpty() ? new HashMap<>() :
                usersMapper.selectListByQuery(
                        QueryWrapper.create().in("user_id", patientUserIds)
                ).stream().collect(Collectors.toMap(Users::getUserId, u -> u));

        Map<Integer, Doctors> doctorMap = doctorsMapper.selectListByQuery(
                QueryWrapper.create().in("doctor_id", doctorIds)
        ).stream().collect(Collectors.toMap(Doctors::getDoctorId, d -> d));
        List<Integer> doctorUserIds = doctorMap.values().stream()
                .map(Doctors::getUserId).distinct().collect(Collectors.toList());
        Map<Integer, Users> doctorUserMap = doctorUserIds.isEmpty() ? new HashMap<>() :
                usersMapper.selectListByQuery(
                        QueryWrapper.create().in("user_id", doctorUserIds)
                ).stream().collect(Collectors.toMap(Users::getUserId, u -> u));

        Map<Integer, Departments> deptMap = departmentsMapper.selectListByQuery(
                QueryWrapper.create().in("dept_id", deptIds)
        ).stream().collect(Collectors.toMap(Departments::getDeptId, d -> d));

        List<Map<String, Object>> records = list.stream().map(h -> {
            Map<String, Object> item = new HashMap<>();
            item.put("hospitalizationId", h.getHospitalizationId());
            item.put("patientId", h.getPatientId());
            item.put("doctorId", h.getDoctorId());
            item.put("deptId", h.getDeptId());
            item.put("wardNumber", h.getWardNumber());
            item.put("bedNumber", h.getBedNumber());
            item.put("consultationId", h.getConsultationId());
            item.put("admissionDate", fmtDate(h.getAdmissionDate()));
            item.put("dischargeDate", fmtDate(h.getDischargeDate()));
            item.put("reason", h.getReason());
            item.put("status", h.getStatus());
            item.put("createdAt", h.getCreatedAt());

            Patients p = patientMap.get(h.getPatientId());
            if (p != null) {
                Users u = userMap.get(p.getUserId());
                item.put("patientName", u != null ? u.getRealName() : "患者" + h.getPatientId());
            } else {
                item.put("patientName", "患者" + h.getPatientId());
            }

            Doctors d = doctorMap.get(h.getDoctorId());
            if (d != null) {
                Users du = doctorUserMap.get(d.getUserId());
                item.put("doctorName", du != null ? du.getRealName() : null);
            }

            Departments dept = deptMap.get(h.getDeptId());
            item.put("deptName", dept != null ? dept.getDeptName() : null);

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
    public void discharge(Integer hospitalizationId) {
        if (hospitalizationId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "住院ID不能为空");
        }
        Hospitalizations h = getById(hospitalizationId);
        if (h == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "住院记录不存在");
        }
        if ("discharged".equals(h.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该患者已出院");
        }
        h.setStatus("discharged");
        h.setDischargeDate(new Timestamp(System.currentTimeMillis()));
        updateById(h);
    }

    private Map<String, Object> emptyPage(Integer current, Integer size) {
        Map<String, Object> result = new HashMap<>();
        result.put("records", new ArrayList<>());
        result.put("total", 0L);
        result.put("current", current);
        result.put("size", size);
        return result;
    }

    @Override
    public boolean isPatientAdmitted(Integer patientId) {
        if (patientId == null) return false;
        return count(QueryWrapper.create()
                .eq("patient_id", patientId)
                .eq("status", "admitted")) > 0;
    }

    private static String fmtDate(Timestamp ts) {
        if (ts == null) return null;
        return new SimpleDateFormat("yyyy-MM-dd").format(ts);
    }

}
