package com.backend.controller;

import com.backend.common.ResultUtils;
import com.backend.model.entity.Doctors;
import com.backend.service.DoctorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *  医生管理控制器。
 *
 * @author 佳尔宇柔
 */
@RestController
@RequestMapping("/api/doctors")
public class DoctorsController {

    @Autowired
    private DoctorsService doctorsService;

    /**
     * 获取医生列表
     *
     * @param deptId 科室ID
     * @param status 状态
     * @return 医生列表
     */
    @GetMapping
    public Object getDoctorsList(
            @RequestParam(required = false) Integer deptId,
            @RequestParam(required = false) String status) {
        List<Doctors> doctorsList = doctorsService.getDoctorsList(deptId, status);
        return ResultUtils.success(doctorsList);
    }

}
