package com.backend.controller;

import com.backend.common.BaseResponse;
import com.backend.common.ResultUtils;
import com.backend.model.entity.Consultations;
import com.backend.service.ConsultationsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  就诊记录控制器。
 *
 * @author 佳尔宇柔
 */
@RestController
@RequestMapping("/consultation")
public class ConsultationsController {

    @Resource
    private ConsultationsService consultationsService;

    /**
     * 获取就诊记录列表
     *
     * @param patientId 患者ID（可选）
     * @param doctorId 医生ID（可选）
     * @return 就诊记录列表
     */
    @GetMapping
    public BaseResponse<List<Consultations>> getConsultations(
            @RequestParam(required = false) Integer patientId,
            @RequestParam(required = false) Integer doctorId) {
        List<Consultations> list = consultationsService.getConsultations(patientId, doctorId);
        return ResultUtils.success(list);
    }

    /**
     * 获取单个就诊记录详情
     *
     * @param consultationId 就诊记录ID
     * @return 就诊记录
     */
    @GetMapping("/{consultationId}")
    public BaseResponse<Map<String, Object>> getConsultationById(@PathVariable Integer consultationId) {
        Map<String, Object> data = consultationsService.getConsultationDetail(consultationId);
        return ResultUtils.success(data);
    }

    /**
     * 医生获取自己的就诊列表
     */
    @GetMapping("/doctor")
    public BaseResponse<List<Map<String, Object>>> getDoctorConsultations() {
        List<Map<String, Object>> list = consultationsService.getDoctorConsultations();
        return ResultUtils.success(list);
    }

    /**
     * 医生填写病历（问诊）
     */
    @PostMapping
    public BaseResponse<Map<String, Object>> createConsultation(@RequestBody Consultations consultation) {
        Consultations result = consultationsService.createConsultation(consultation);
        Map<String, Object> data = new HashMap<>();
        data.put("consultationId", result.getConsultationId());
        data.put("appointmentId", result.getAppointmentId());
        return ResultUtils.success(data, "问诊记录已保存");
    }

    /**
     * 保存诊断信息
     */
    @PutMapping("/{consultationId}")
    public BaseResponse<Void> updateConsultation(
            @PathVariable Integer consultationId,
            @RequestBody Map<String, Object> body) {
        java.math.BigDecimal amount = null;
        if (body.get("amount") != null) {
            amount = new java.math.BigDecimal(body.get("amount").toString());
        }
        consultationsService.updateConsultation(consultationId,
                (String) body.get("diagnosis"),
                (String) body.get("treatment"),
                (String) body.get("prescription"),
                amount);
        return ResultUtils.success(null, "诊断已保存");
    }

    /**
     * 完成就诊
     */
    @PutMapping("/{consultationId}/complete")
    public BaseResponse<Void> completeConsultation(
            @PathVariable Integer consultationId,
            @RequestBody(required = false) Map<String, Object> body) {
        java.math.BigDecimal amount = null;
        if (body != null && body.get("amount") != null) {
            amount = new java.math.BigDecimal(body.get("amount").toString());
        }
        consultationsService.completeConsultation(consultationId, amount);
        return ResultUtils.success(null, "就诊已完成");
    }

}
