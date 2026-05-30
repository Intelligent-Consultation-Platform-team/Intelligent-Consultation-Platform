package com.backend.service.impl;

import com.backend.exception.BusinessException;
import com.backend.exception.ErrorCode;
import com.backend.mapper.PatientsMapper;
import com.backend.mapper.RechargeRecordsMapper;
import com.backend.mapper.PaymentRecordsMapper;
import com.backend.mapper.ConsultationsMapper;
import com.backend.model.entity.Patients;
import com.backend.model.entity.RechargeRecords;
import com.backend.model.entity.PaymentRecords;
import com.backend.service.PatientAccountService;
import com.mybatisflex.core.query.QueryWrapper;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;

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
                .patientId(patientId)
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

        PaymentRecords record = PaymentRecords.builder()
                .patientId(patientId)
                .consultationId(consultationId)
                .amount(amount)
                .paymentDate(new Timestamp(System.currentTimeMillis()))
                .paymentMethod(paymentMethod)
                .status("paid")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        paymentRecordsMapper.insert(record);

        patient.setBalance(patient.getBalance().subtract(amount));
        patient.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        patientsMapper.update(patient);

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

    private Patients getPatientByUserId(Integer userId) {
        if (userId == null) return null;
        QueryWrapper queryWrapper = QueryWrapper.create().eq("user_id", userId);
        return patientsMapper.selectOneByQuery(queryWrapper);
    }

}
