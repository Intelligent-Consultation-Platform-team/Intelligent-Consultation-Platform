package com.backend.service.impl;

import com.backend.exception.BusinessException;
import com.backend.exception.ErrorCode;
import com.backend.mapper.AppointmentsMapper;
import com.backend.mapper.ConsultationsMapper;
import com.backend.mapper.DoctorsMapper;
import com.backend.mapper.DepartmentsMapper;
import com.backend.mapper.PatientsMapper;
import com.backend.mapper.PaymentRecordsMapper;
import com.backend.mapper.RechargeRecordsMapper;
import com.backend.mapper.UsersMapper;
import com.backend.model.entity.Appointments;
import com.backend.model.entity.Consultations;
import com.backend.model.entity.Departments;
import com.backend.model.entity.Doctors;
import com.backend.model.entity.Patients;
import com.backend.model.entity.PaymentRecords;
import com.backend.model.entity.RechargeRecords;
import com.backend.model.entity.Users;
import com.backend.service.PatientAccountService;
import com.mybatisflex.core.query.QueryWrapper;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  患者账户服务实现类。
 *
 * @author 佳尔宇柔
 */
@Service
public class PatientAccountServiceImpl implements PatientAccountService {

    @Resource
    private PatientsMapper patientsMapper;

    @Resource
    private RechargeRecordsMapper rechargeRecordsMapper;

    @Resource
    private PaymentRecordsMapper paymentRecordsMapper;

    @Resource
    private ConsultationsMapper consultationsMapper;

    @Resource
    private AppointmentsMapper appointmentsMapper;

    @Resource
    private DoctorsMapper doctorsMapper;

    @Resource
    private DepartmentsMapper departmentsMapper;

    @Resource
    private UsersMapper usersMapper;

    @Override
    @Transactional
    public RechargeRecords recharge(Integer patientId, BigDecimal amount, String paymentMethod) {
        Patients patient = getPatientByUserId(patientId);
        if (patient == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "患者信息不存在");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "充值金额必须大于0");
        }

        RechargeRecords record = RechargeRecords.builder()
                .patientId(patient.getPatientId())
                .amount(amount)
                .rechargeDate(new Timestamp(System.currentTimeMillis()))
                .paymentMethod(paymentMethod)
                .status("success")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
        rechargeRecordsMapper.insert(record);

        patient.setBalance(patient.getBalance().add(amount));
        patient.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        patientsMapper.update(patient);

        return record;
    }

    @Override
    @Transactional
    public PaymentRecords payment(Integer patientId, Integer consultationId, BigDecimal amount, String paymentMethod) {
        Patients patient = getPatientByUserId(patientId);
        if (patient == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "患者信息不存在");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "缴费金额必须大于0");
        }
        if (patient.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "余额不足，请先充值");
        }

        // 查找已有的待缴费记录并更新，避免重复创建
        PaymentRecords record = paymentRecordsMapper.selectOneByQuery(
                QueryWrapper.create().eq("consultation_id", consultationId).eq("status", "unpaid"));
        if (record != null) {
            record.setStatus("paid");
            record.setPaymentMethod(paymentMethod);
            record.setPaymentDate(new Timestamp(System.currentTimeMillis()));
            record.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            paymentRecordsMapper.update(record);
        } else {
            record = PaymentRecords.builder()
                    .patientId(patient.getPatientId())
                    .consultationId(consultationId)
                    .amount(amount)
                    .paymentDate(new Timestamp(System.currentTimeMillis()))
                    .paymentMethod(paymentMethod)
                    .status("paid")
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .updatedAt(new Timestamp(System.currentTimeMillis()))
                    .build();
            paymentRecordsMapper.insert(record);
        }

        patient.setBalance(patient.getBalance().subtract(amount));
        patient.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        patientsMapper.update(patient);

        // 缴费成功，将关联预约标记为已完成
        if (consultationId != null) {
            Consultations consultation = consultationsMapper.selectOneById(consultationId);
            if (consultation != null && consultation.getAppointmentId() != null) {
                Appointments appointment = appointmentsMapper.selectOneById(consultation.getAppointmentId());
                if (appointment != null) {
                    appointment.setStatus("completed");
                    appointment.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                    appointmentsMapper.update(appointment);
                }
            }
        }

        return record;
    }

    @Override
    public BigDecimal getBalance(Integer patientId) {
        Patients patient = getPatientByUserId(patientId);
        if (patient == null) {
            return BigDecimal.ZERO;
        }
        return patient.getBalance() != null ? patient.getBalance() : BigDecimal.ZERO;
    }

    @Override
    public Map<String, Object> getJourney(Integer userId) {
        Patients patient = getPatientByUserId(userId);
        if (patient == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "患者信息不存在");
        }
        Integer patientId = patient.getPatientId();

        // 所有预约
        List<Appointments> appointments = appointmentsMapper.selectListByQuery(
                QueryWrapper.create().eq("patient_id", patientId).orderBy("created_at", false));

        List<Integer> appointmentIds = appointments.stream()
                .map(Appointments::getAppointmentId).distinct().collect(Collectors.toList());
        List<Integer> doctorIds = appointments.stream()
                .map(Appointments::getDoctorId).distinct().collect(Collectors.toList());

        // 就诊记录
        Map<Integer, Consultations> consultationMap = appointmentIds.isEmpty() ? new HashMap<>() :
                consultationsMapper.selectListByQuery(
                        QueryWrapper.create().in("appointment_id", appointmentIds)
                ).stream().collect(Collectors.toMap(Consultations::getAppointmentId, c -> c, (a, b) -> a));

        // 缴费记录
        List<Integer> consultationIds = consultationMap.values().stream()
                .map(Consultations::getConsultationId).distinct().collect(Collectors.toList());
        Map<Integer, PaymentRecords> paymentMap = consultationIds.isEmpty() ? new HashMap<>() :
                paymentRecordsMapper.selectListByQuery(
                        QueryWrapper.create().in("consultation_id", consultationIds)
                ).stream().collect(Collectors.toMap(PaymentRecords::getConsultationId, p -> p, (a, b) -> a));

        // 医生信息
        Map<Integer, Doctors> doctorMap = doctorIds.isEmpty() ? new HashMap<>() :
                doctorsMapper.selectListByQuery(
                        QueryWrapper.create().in("doctor_id", doctorIds)
                ).stream().collect(Collectors.toMap(Doctors::getDoctorId, d -> d));
        List<Integer> doctorUserIds = doctorMap.values().stream()
                .map(Doctors::getUserId).distinct().collect(Collectors.toList());
        Map<Integer, Users> doctorUserMap = doctorUserIds.isEmpty() ? new HashMap<>() :
                usersMapper.selectListByQuery(
                        QueryWrapper.create().in("user_id", doctorUserIds)
                ).stream().collect(Collectors.toMap(Users::getUserId, u -> u));
        Map<Integer, Departments> deptMap = doctorMap.values().stream()
                .map(Doctors::getDeptId).distinct().collect(Collectors.toList()).isEmpty() ? new HashMap<>() :
                departmentsMapper.selectListByQuery(
                        QueryWrapper.create().in("dept_id", doctorMap.values().stream()
                                .map(Doctors::getDeptId).distinct().collect(Collectors.toList()))
                ).stream().collect(Collectors.toMap(Departments::getDeptId, d -> d));

        List<Map<String, Object>> items = new ArrayList<>();
        for (Appointments apt : appointments) {
            Map<String, Object> item = new HashMap<>();
            item.put("appointmentId", apt.getAppointmentId());
            item.put("appointmentDate", apt.getAppointmentDate());
            item.put("appointmentTime", apt.getAppointmentTime());
            item.put("symptoms", apt.getSymptoms());
            item.put("status", apt.getStatus());
            item.put("createdAt", apt.getCreatedAt());

            Doctors doctor = doctorMap.get(apt.getDoctorId());
            if (doctor != null) {
                Users doctorUser = doctorUserMap.get(doctor.getUserId());
                item.put("doctorName", doctorUser != null ? doctorUser.getRealName() : null);
                item.put("doctorTitle", doctor.getTitle());
                Departments dept = deptMap.get(doctor.getDeptId());
                item.put("deptName", dept != null ? dept.getDeptName() : null);
            }

            Consultations consultation = consultationMap.get(apt.getAppointmentId());
            if (consultation != null) {
                item.put("consultationId", consultation.getConsultationId());
                item.put("diagnosis", consultation.getDiagnosis());
                item.put("treatment", consultation.getTreatment());
                item.put("prescription", consultation.getPrescription());

                PaymentRecords payment = paymentMap.get(consultation.getConsultationId());
                if (payment != null) {
                    item.put("paymentId", payment.getPaymentId());
                    item.put("amount", payment.getAmount());
                    item.put("paymentStatus", payment.getStatus());
                }
            }

            items.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        return result;
    }

    @Override
    public List<Map<String, Object>> getRecords(Integer userId) {
        Patients patient = getPatientByUserId(userId);
        if (patient == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "患者信息不存在");
        }
        Integer patientId = patient.getPatientId();

        List<RechargeRecords> recharges = rechargeRecordsMapper.selectListByQuery(
                QueryWrapper.create().eq("patient_id", patientId).orderBy("created_at", false));
        List<PaymentRecords> payments = paymentRecordsMapper.selectListByQuery(
                QueryWrapper.create().eq("patient_id", patientId).orderBy("created_at", false));

        List<Map<String, Object>> records = new ArrayList<>();
        for (RechargeRecords r : recharges) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "充值");
            item.put("amount", r.getAmount());
            item.put("method", r.getPaymentMethod());
            item.put("time", r.getRechargeDate());
            records.add(item);
        }
        for (PaymentRecords p : payments) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "缴费");
            item.put("amount", p.getAmount().negate());
            item.put("method", p.getPaymentMethod());
            item.put("time", p.getPaymentDate());
            item.put("consultationId", p.getConsultationId());
            records.add(item);
        }
        records.sort((a, b) -> {
            Timestamp ta = (Timestamp) a.get("time");
            Timestamp tb = (Timestamp) b.get("time");
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        });
        return records;
    }

    private Patients getPatientByUserId(Integer userId) {
        if (userId == null) return null;
        QueryWrapper queryWrapper = QueryWrapper.create().eq("user_id", userId);
        return patientsMapper.selectOneByQuery(queryWrapper);
    }

}
