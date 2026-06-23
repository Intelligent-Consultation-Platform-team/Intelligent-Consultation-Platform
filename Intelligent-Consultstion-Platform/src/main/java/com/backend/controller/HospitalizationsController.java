package com.backend.controller;

import com.backend.common.BaseResponse;
import com.backend.common.ResultUtils;
import com.backend.model.entity.Hospitalizations;
import com.backend.service.HospitalizationsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 住院管理控制器。
 *
 * @author 佳尔宇柔
 */
@RestController
@RequestMapping("/hospitalization")
public class HospitalizationsController {

    @Resource
    private HospitalizationsService hospitalizationsService;

    /**
     * 住院登记
     */
    @PostMapping
    public BaseResponse<Hospitalizations> createHospitalization(@RequestBody Hospitalizations hospitalizations) {
        Hospitalizations result = hospitalizationsService.createHospitalization(hospitalizations);
        return ResultUtils.success(result, "住院登记成功");
    }

    /**
     * 分页查询住院列表
     */
    @GetMapping
    public BaseResponse<Map<String, Object>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String status) {
        Map<String, Object> result = hospitalizationsService.getPage(current, size, patientName, status);
        return ResultUtils.success(result);
    }

    /**
     * 获取住院详情
     */
    @GetMapping("/{id}")
    public BaseResponse<Hospitalizations> getById(@PathVariable Integer id) {
        Hospitalizations h = hospitalizationsService.getById(id);
        return ResultUtils.success(h);
    }

    /**
     * 检查患者是否正在住院
     */
    @GetMapping("/patient/{patientId}/active")
    public BaseResponse<Map<String, Object>> checkPatientAdmitted(@PathVariable Integer patientId) {
        boolean admitted = hospitalizationsService.isPatientAdmitted(patientId);
        Map<String, Object> data = new HashMap<>();
        data.put("patientId", patientId);
        data.put("admitted", admitted);
        return ResultUtils.success(data);
    }

    /**
     * 出院
     */
    @PutMapping("/{id}/discharge")
    public BaseResponse<Void> discharge(@PathVariable Integer id) {
        hospitalizationsService.discharge(id);
        return ResultUtils.success(null, "出院成功");
    }

}
