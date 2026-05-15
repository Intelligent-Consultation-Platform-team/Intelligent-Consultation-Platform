package com.backend.service;

import com.backend.model.entity.RechargeRecords;
import com.backend.model.entity.PaymentRecords;

import java.math.BigDecimal;

/**
 *  患者账户服务接口。
 *
 * @author 佳尔宇柔
 */
public interface PatientAccountService {

    /**
     * 充值
     *
     * @param patientId 患者ID
     * @param amount 充值金额
     * @param paymentMethod 支付方式
     * @return 充值记录
     */
    RechargeRecords recharge(Integer patientId, BigDecimal amount, String paymentMethod);

    /**
     * 缴费
     *
     * @param patientId 患者ID
     * @param consultationId 就诊记录ID
     * @param amount 缴费金额
     * @param paymentMethod 支付方式
     * @return 缴费记录
     */
    PaymentRecords payment(Integer patientId, Integer consultationId, BigDecimal amount, String paymentMethod);

    /**
     * 获取患者余额
     *
     * @param patientId 患者ID
     * @return 余额
     */
    BigDecimal getBalance(Integer patientId);

}
