package com.backend.service;

import com.backend.model.entity.RechargeRecords;
import com.backend.model.entity.PaymentRecords;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    /**
     * 获取患者全流程数据
     */
    Map<String, Object> getJourney(Integer userId);

    /**
     * 获取患者交易记录
     */
    List<Map<String, Object>> getRecords(Integer userId);

    /**
     * 按姓名搜索患者
     *
     * @param name 患者姓名（模糊匹配）
     * @return 患者列表（含 patientId、姓名、身份证、电话等）
     */
    List<Map<String, Object>> searchPatients(String name);

}
