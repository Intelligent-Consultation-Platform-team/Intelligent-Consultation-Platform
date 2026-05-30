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
    public BaseResponse<Consultations> getConsultationById(@PathVariable Integer consultationId) {
        Consultations consultation = consultationsService.getById(consultationId);
        return ResultUtils.success(consultation);
    }

    /**
     * 医生填写病历（问诊）
     * 会自动更新预约状态为 processing
     *
     * @param consultation 就诊信息（symptoms, diagnosis, treatment, prescription等）
     * @return 问诊记录ID
     */
    @PostMapping
    public BaseResponse<Map<String, Object>> createConsultation(@RequestBody Consultations consultation) {
        Consultations result = consultationsService.createConsultation(consultation);
        Map<String, Object> data = new HashMap<>();
        data.put("consultationId", result.getConsultationId());
        data.put("appointmentId", result.getAppointmentId());
        return ResultUtils.success(data, "问诊记录已保存");
    }

}
