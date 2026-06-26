package com.backend.service.impl;

import com.backend.common.UserContext;
import com.backend.exception.BusinessException;
import com.backend.exception.ErrorCode;
import com.backend.mapper.ConsultationsMapper;
import com.backend.mapper.DepartmentsMapper;
import com.backend.mapper.DoctorsMapper;
import com.backend.mapper.PatientsMapper;
import com.backend.mapper.PaymentRecordsMapper;
import com.backend.mapper.AppointmentsMapper;
import com.backend.mapper.UsersMapper;
import com.backend.model.entity.*;
import com.backend.service.ConsultationsService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  就诊记录服务实现类。
 *
 * @author 佳尔宇柔
 */
@Service
public class ConsultationsServiceImpl extends ServiceImpl<ConsultationsMapper, Consultations> implements ConsultationsService {

    @Resource
    private PaymentRecordsMapper paymentRecordsMapper;

    @Resource
    private AppointmentsMapper appointmentsMapper;

    @Resource
    private DoctorsMapper doctorsMapper;

    @Resource
    private PatientsMapper patientsMapper;

    @Resource
    private UsersMapper usersMapper;

    @Resource
    private DepartmentsMapper departmentsMapper;

    @Override
    public List<Consultations> getConsultations(Integer patientId, Integer doctorId) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (patientId != null) {
            queryWrapper.eq("patient_id", patientId);
        }
        if (doctorId != null) {
            queryWrapper.eq("doctor_id", doctorId);
        }
        return list(queryWrapper);
    }

    @Override
    public Map<String, Object> getConsultationDetail(Integer consultationId) {
        Consultations consultation = getById(consultationId);
        if (consultation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "就诊记录不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("consultationId", consultation.getConsultationId());
        data.put("appointmentId", consultation.getAppointmentId());
        data.put("doctorId", consultation.getDoctorId());
        data.put("patientId", consultation.getPatientId());
        data.put("symptoms", consultation.getSymptoms());
        data.put("diagnosis", consultation.getDiagnosis());
        data.put("treatment", consultation.getTreatment());
        data.put("prescription", consultation.getPrescription());
        data.put("consultationDate", consultation.getConsultationDate());

        // 查缴费记录
        PaymentRecords payment = paymentRecordsMapper.selectOneByQuery(
                QueryWrapper.create().eq("consultation_id", consultationId));
        if (payment != null) {
            data.put("amount", payment.getAmount());
            data.put("paymentStatus", payment.getStatus());
            data.put("paymentId", payment.getPaymentId());
        } else {
            data.put("amount", null);
            data.put("paymentStatus", null);
        }

        // 查预约信息
        Appointments apt = appointmentsMapper.selectOneById(consultation.getAppointmentId());
        if (apt != null) {
            data.put("appointmentDate", apt.getAppointmentDate());
            data.put("appointmentTime", apt.getAppointmentTime());
        }

        // 查医生信息
        Doctors doctor = doctorsMapper.selectOneById(consultation.getDoctorId());
        if (doctor != null) {
            Users doctorUser = usersMapper.selectOneById(doctor.getUserId());
            data.put("doctorName", doctorUser != null ? doctorUser.getRealName() : null);
            Departments dept = departmentsMapper.selectOneById(doctor.getDeptId());
            data.put("deptName", dept != null ? dept.getDeptName() : null);
        }

        return data;
    }

    /**
     * 医生填写病历并开处方（问诊）
     * 自动生成待缴费单（unpaid），并更新预约状态为 processing
     */
    @Override
    @Transactional
    public Consultations createConsultation(Consultations consultation) {
        if (consultation.getAppointmentId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "预约ID不能为空");
        }
        if (consultation.getDoctorId() == null) {
            Integer userId = UserContext.getUserId();
            if (userId != null) {
                Doctors doctor = doctorsMapper.selectOneByQuery(
                        QueryWrapper.create().eq("user_id", userId));
                if (doctor != null) {
                    consultation.setDoctorId(doctor.getDoctorId());
                }
            }
        }
        if (consultation.getPatientId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "患者ID不能为空");
        }

        consultation.setConsultationDate(new Timestamp(System.currentTimeMillis()));
        consultation.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        consultation.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        save(consultation);

        // 更新预约状态为就诊中
        Appointments appointment = appointmentsMapper.selectOneById(consultation.getAppointmentId());
        if (appointment != null) {
            appointment.setStatus("processing");
            appointmentsMapper.update(appointment);
        }

        return consultation;
    }

    @Override
    public List<Map<String, Object>> getDoctorConsultations() {
        Integer userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        Doctors doctor = doctorsMapper.selectOneByQuery(
                QueryWrapper.create().eq("user_id", userId));
        if (doctor == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "医生信息不存在");
        }
        List<Consultations> list = list(QueryWrapper.create()
                .eq("doctor_id", doctor.getDoctorId())
                .orderBy("created_at", false));

        if (list.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> patientIds = list.stream()
                .map(Consultations::getPatientId).distinct().collect(Collectors.toList());
        List<Integer> appointmentIds = list.stream()
                .map(Consultations::getAppointmentId).distinct().collect(Collectors.toList());

        Map<Integer, Patients> patientIdMap = patientsMapper.selectListByQuery(
                QueryWrapper.create().in("patient_id", patientIds)
        ).stream().collect(Collectors.toMap(Patients::getPatientId, p -> p));
        List<Integer> patientUserIds = patientIdMap.values().stream()
                .map(Patients::getUserId).distinct().collect(Collectors.toList());
        Map<Integer, Users> patientUserMap = patientUserIds.isEmpty() ? new HashMap<>() :
                usersMapper.selectListByQuery(
                        QueryWrapper.create().in("user_id", patientUserIds)
                ).stream().collect(Collectors.toMap(Users::getUserId, u -> u));

        Map<Integer, Appointments> appointmentMap = appointmentsMapper.selectListByQuery(
                QueryWrapper.create().in("appointment_id", appointmentIds)
        ).stream().collect(Collectors.toMap(Appointments::getAppointmentId, a -> a));

        return list.stream().map(c -> {
            Map<String, Object> item = new HashMap<>();
            item.put("consultationId", c.getConsultationId());
            item.put("appointmentId", c.getAppointmentId());
            item.put("doctorId", c.getDoctorId());
            item.put("deptId", doctor.getDeptId());
            item.put("patientId", c.getPatientId());
            item.put("symptoms", c.getSymptoms());
            item.put("diagnosis", c.getDiagnosis());
            item.put("treatment", c.getTreatment());
            item.put("prescription", c.getPrescription());
            item.put("amount", c.getAmount());
            item.put("consultationDate", c.getConsultationDate());

            Patients patient = patientIdMap.get(c.getPatientId());
            if (patient != null) {
                Users patientUser = patientUserMap.get(patient.getUserId());
                item.put("patientName", patientUser != null ? patientUser.getRealName() : "患者" + c.getPatientId());
            } else {
                item.put("patientName", "患者" + c.getPatientId());
            }

            Appointments apt = appointmentMap.get(c.getAppointmentId());
            item.put("status", apt != null ? apt.getStatus() : "processing");

            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 更新就诊记录（诊断、治疗方案、处方、医嘱）
     */
    @Transactional
    public void updateConsultation(Integer consultationId, String diagnosis, String treatment, String prescription, java.math.BigDecimal amount) {
        Consultations consultation = getById(consultationId);
        if (consultation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "就诊记录不存在");
        }
        Consultations update = Consultations.builder()
                .consultationId(consultationId)
                .diagnosis(diagnosis)
                .treatment(treatment)
                .prescription(prescription)
                .amount(amount)
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        updateById(update);
    }

    /**
     * 完成就诊：生成待缴费记录，患者自行缴费
     */
    @Transactional
    public void completeConsultation(Integer consultationId, java.math.BigDecimal amount) {
        Consultations consultation = getById(consultationId);
        if (consultation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "就诊记录不存在");
        }
        if (consultation.getDiagnosis() == null || consultation.getDiagnosis().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请先填写诊断结果");
        }
        Consultations update = Consultations.builder()
                .consultationId(consultationId)
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        updateById(update);

        boolean exists = paymentRecordsMapper.selectCountByQuery(
                QueryWrapper.create().eq("consultation_id", consultationId)) > 0;
        if (exists) {
            // 仍需要更新预约状态
            Appointments apt = appointmentsMapper.selectOneById(consultation.getAppointmentId());
            if (apt != null && !"unpaid".equals(apt.getStatus())) {
                apt.setStatus("unpaid");
                appointmentsMapper.update(apt);
            }
            return;
        }

        java.math.BigDecimal finalAmount = (amount != null && amount.compareTo(java.math.BigDecimal.ZERO) > 0)
                ? amount
                : (consultation.getAmount() != null && consultation.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0)
                    ? consultation.getAmount()
                    : new java.math.BigDecimal("50.00");

        PaymentRecords payment = PaymentRecords.builder()
                .patientId(consultation.getPatientId())
                .consultationId(consultationId)
                .amount(finalAmount)
                .paymentMethod("offline")
                .status("unpaid")
                .paymentDate(new Timestamp(System.currentTimeMillis()))
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        paymentRecordsMapper.insert(payment);

        // 更新预约状态为待支付
        Appointments apt = appointmentsMapper.selectOneById(consultation.getAppointmentId());
        if (apt != null) {
            apt.setStatus("unpaid");
            appointmentsMapper.update(apt);
        }
    }

    /**
     * 医生提交诊断后，自动生成待缴费单
     */
    @Transactional
    public PaymentRecords generatePaymentRecord(Integer consultationId, Integer patientId, java.math.BigDecimal amount) {
        PaymentRecords record = PaymentRecords.builder()
                .consultationId(consultationId)
                .patientId(patientId)
                .amount(amount)
                .paymentMethod("account")
                .status("unpaid")
                .paymentDate(new Timestamp(System.currentTimeMillis()))
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        paymentRecordsMapper.insert(record);
        return record;
    }

}
