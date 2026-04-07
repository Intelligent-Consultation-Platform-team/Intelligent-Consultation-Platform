package com.backend.service.impl;

import com.backend.mapper.RechargeRecordsMapper;
import com.backend.mapper.PaymentRecordsMapper;
import com.backend.model.entity.RechargeRecords;
import com.backend.model.entity.PaymentRecords;
import com.backend.service.PatientAccountService;
import com.mybatisflex.core.query.QueryWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 *  患者账户服务实现类。
 *
 * @author 佳尔宇柔
 */
@Service
public class PatientAccountServiceImpl implements PatientAccountService {

    @Autowired
    private RechargeRecordsMapper rechargeRecordsMapper;

    @Autowired
    private PaymentRecordsMapper paymentRecordsMapper;

    @Override
    public RechargeRecords recharge(Integer patientId, BigDecimal amount, String paymentMethod) {
        RechargeRecords rechargeRecords = RechargeRecords.builder()
                .patientId(patientId)
                .amount(amount)
                .rechargeDate(new Timestamp(System.currentTimeMillis()))
                .paymentMethod(paymentMethod)
                .status("success")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
        rechargeRecordsMapper.insert(rechargeRecords);
        return rechargeRecords;
    }

    @Override
    public PaymentRecords payment(Integer patientId, Integer consultationId, BigDecimal amount, String paymentMethod) {
        PaymentRecords paymentRecords = PaymentRecords.builder()
                .patientId(patientId)
                .consultationId(consultationId)
                .amount(amount)
                .paymentDate(new Timestamp(System.currentTimeMillis()))
                .paymentMethod(paymentMethod)
                .status("paid")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        paymentRecordsMapper.insert(paymentRecords);
        return paymentRecords;
    }

    @Override
    public BigDecimal getBalance(Integer patientId) {
        // 这里需要从患者表中获取余额，暂时返回一个默认值
        return new BigDecimal(1000);
    }

}
