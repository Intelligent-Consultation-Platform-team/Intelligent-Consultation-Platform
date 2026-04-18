package com.backend.controller;

import com.backend.common.ResultUtils;
import com.backend.model.entity.RechargeRecords;
import com.backend.model.entity.PaymentRecords;
import com.backend.service.PatientAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 *  患者账户控制器。
 *
 * @author 佳尔宇柔
 */
@RestController
@RequestMapping("")
public class PatientAccountController {

    @Autowired
    private PatientAccountService patientAccountService;

    /**
     * 充值
     *
     * @param request 充值请求
     * @return 充值结果
     */
    @PostMapping("/recharge")
    public Object recharge(@RequestBody RechargeRequest request) {
        RechargeRecords rechargeRecords = patientAccountService.recharge(
                request.getPatientId(),
                request.getAmount(),
                request.getPaymentMethod()
        );
        BigDecimal balance = patientAccountService.getBalance(request.getPatientId());
        Map<String, Object> result = new HashMap<>();
        result.put("recordId", rechargeRecords.getRecordId());
        result.put("amount", rechargeRecords.getAmount());
        result.put("balance", balance);
        result.put("rechargeDate", rechargeRecords.getRechargeDate());
        return ResultUtils.success(result);
    }

    /**
     * 缴费
     *
     * @param request 缴费请求
     * @return 缴费结果
     */
    @PostMapping("/payment")
    public Object payment(@RequestBody PaymentRequest request) {
        PaymentRecords paymentRecords = patientAccountService.payment(
                request.getPatientId(),
                request.getConsultationId(),
                request.getAmount(),
                request.getPaymentMethod()
        );
        BigDecimal balance = patientAccountService.getBalance(request.getPatientId());
        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", paymentRecords.getPaymentId());
        result.put("amount", paymentRecords.getAmount());
        result.put("balance", balance);
        result.put("paymentDate", paymentRecords.getPaymentDate());
        return ResultUtils.success(result);
    }

    // 充值请求类
    public static class RechargeRequest {
        private Integer patientId;
        private BigDecimal amount;
        private String paymentMethod;

        public Integer getPatientId() {
            return patientId;
        }

        public void setPatientId(Integer patientId) {
            this.patientId = patientId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }
    }

    // 缴费请求类
    public static class PaymentRequest {
        private Integer patientId;
        private Integer consultationId;
        private BigDecimal amount;
        private String paymentMethod;

        public Integer getPatientId() {
            return patientId;
        }

        public void setPatientId(Integer patientId) {
            this.patientId = patientId;
        }

        public Integer getConsultationId() {
            return consultationId;
        }

        public void setConsultationId(Integer consultationId) {
            this.consultationId = consultationId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }
    }

}
