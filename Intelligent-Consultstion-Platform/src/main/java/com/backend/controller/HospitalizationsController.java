package com.backend.controller;

import com.backend.common.ResultUtils;
import com.backend.model.entity.Hospitalizations;
import com.backend.service.HospitalizationsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  住院管理控制器。
 *
 * @author 佳尔宇柔
 */
@RestController
@RequestMapping("/hospitalizations")
public class HospitalizationsController {

    @Resource
    private HospitalizationsService hospitalizationsService;

    /**
     * 住院登记
     *
     * @param hospitalizations 住院信息
     * @return 住院登记结果
     */
    @PostMapping
    public Object createHospitalization(@RequestBody Hospitalizations hospitalizations) {
        Hospitalizations result = hospitalizationsService.createHospitalization(hospitalizations);
        return ResultUtils.success(result);
    }

}