package com.backend.service.impl;

import com.backend.common.UserContext;
import com.backend.exception.BusinessException;
import com.backend.exception.ErrorCode;
import com.backend.mapper.ConsultationsMapper;
import com.backend.mapper.PaymentRecordsMapper;
import com.backend.mapper.AppointmentsMapper;
import com.backend.model.entity.Consultations;
import com.backend.model.entity.PaymentRecords;
import com.backend.model.entity.Appointments;
import com.backend.service.ConsultationsService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

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
                consultation.setDoctorId(userId);
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
