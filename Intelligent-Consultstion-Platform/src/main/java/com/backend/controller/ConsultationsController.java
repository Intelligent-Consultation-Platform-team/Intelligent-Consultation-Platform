package com.backend.controller;

import com.backend.common.ResultUtils;
import com.backend.model.entity.Consultations;
import com.backend.service.ConsultationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *  就诊记录控制器。
 *
 * @author 佳尔宇柔
 */
@RestController
@RequestMapping("/api/consultations")
public class ConsultationsController {

    @Autowired
    private ConsultationsService consultationsService;

    /**
     * 获取就诊记录
     *
     * @param patientId 患者ID
     * @param doctorId 医生ID
     * @return 就诊记录列表
     */
    @GetMapping
    public Object getConsultations(
            @RequestParam(required = false) Integer patientId,
            @RequestParam(required = false) Integer doctorId) {
        List<Consultations> consultationsList = consultationsService.getConsultations(patientId, doctorId);
        return ResultUtils.success(consultationsList);
    }

}
