package com.backend.controller;

import com.backend.common.BaseResponse;
import com.backend.common.ResultUtils;
import com.backend.common.UserContext;
import com.backend.exception.BusinessException;
import com.backend.exception.ErrorCode;
import com.backend.model.entity.PaymentRecords;
import com.backend.model.entity.RechargeRecords;
import com.backend.service.PatientAccountService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  患者账户控制器。
 *
 * @author 佳尔宇柔
 */
@RestController
@RequestMapping("/patient")
public class PatientAccountController {

    @Resource
    private PatientAccountService patientAccountService;

    /**
     * 获取患者余额
     *
     * @param patientId 患者ID
     * @return 余额
     */
    @GetMapping("/{patientId}/balance")
    public BaseResponse<Map<String, Object>> getBalance(@PathVariable Integer patientId) {
        BigDecimal balance = patientAccountService.getBalance(patientId);
        Map<String, Object> data = new HashMap<>();
        data.put("patientId", patientId);
        data.put("balance", balance);
        return ResultUtils.success(data);
    }

    /**
     * 充值
     *
     * @param patientId 患者ID
     * @param request 充值请求
     * @return 充值结果
     */
    @PostMapping("/{patientId}/recharge")
    public BaseResponse<Map<String, Object>> recharge(
            @PathVariable Integer patientId,
            @RequestBody RechargeRequest request) {
        RechargeRecords record = patientAccountService.recharge(
                patientId,
                request.getAmount(),
                request.getPaymentMethod()
        );
        BigDecimal balance = patientAccountService.getBalance(patientId);
        Map<String, Object> data = new HashMap<>();
        data.put("recordId", record.getRecordId());
        data.put("amount", record.getAmount());
        data.put("balance", balance);
        data.put("rechargeDate", record.getRechargeDate());
        return ResultUtils.success(data, "充值成功");
    }

    /**
     * 缴费
     *
     * @param patientId 患者ID
     * @param request 缴费请求
     * @return 缴费结果
     */
    @PostMapping("/{patientId}/payment")
    public BaseResponse<Map<String, Object>> payment(
            @PathVariable Integer patientId,
            @RequestBody PaymentRequest request) {
        PaymentRecords record = patientAccountService.payment(
                patientId,
                request.getConsultationId(),
                request.getAmount(),
                request.getPaymentMethod()
        );
        BigDecimal balance = patientAccountService.getBalance(patientId);
        Map<String, Object> data = new HashMap<>();
        data.put("paymentId", record.getPaymentId());
        data.put("amount", record.getAmount());
        data.put("balance", balance);
        data.put("paymentDate", record.getPaymentDate());
        return ResultUtils.success(data, "缴费成功");
    }

    /**
     * 获取患者全流程数据（预约 + 就诊 + 缴费）
     */
    @GetMapping("/journey")
    public BaseResponse<Map<String, Object>> getJourney() {
        Integer userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        Map<String, Object> data = patientAccountService.getJourney(userId);
        return ResultUtils.success(data);
    }

    /**
     * 获取患者交易记录（充值 + 缴费）
     */
    @GetMapping("/records")
    public BaseResponse<List<Map<String, Object>>> getRecords() {
        Integer userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        List<Map<String, Object>> data = patientAccountService.getRecords(userId);
        return ResultUtils.success(data);
    }

    // 充值请求类
    public static class RechargeRequest {
        private BigDecimal amount;
        private String paymentMethod;

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }

    // 缴费请求类
    public static class PaymentRequest {
        private Integer consultationId;
        private BigDecimal amount;
        private String paymentMethod;

        public Integer getConsultationId() { return consultationId; }
        public void setConsultationId(Integer consultationId) { this.consultationId = consultationId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }

}
