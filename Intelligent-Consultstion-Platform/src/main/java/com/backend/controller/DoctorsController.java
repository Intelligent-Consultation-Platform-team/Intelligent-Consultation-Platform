package com.backend.controller;

import com.backend.common.ResultUtils;
import com.backend.model.entity.Doctors;
import com.backend.service.DoctorsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *  医生管理控制器。
 *
 * @author 佳尔宇柔
 */
@RestController
@RequestMapping("/doctors")
public class DoctorsController {

    @Resource
    private DoctorsService doctorsService;

    /**
     * 获取医生列表（包含用户信息）
     *
     * @param deptId 科室ID
     * @param status 状态
     * @return 医生列表（包含用户信息）
     */
    @GetMapping
    public Object getDoctorsList(
            @RequestParam(required = false) Integer deptId,
            @RequestParam(required = false) String status) {
        List<?> doctorsList = doctorsService.getDoctorsWithUserInfo(deptId, status);
        return ResultUtils.success(doctorsList);
    }

    /**
     * 添加医生
     *
     * @param doctors 医生信息
     * @return 操作结果
     */
    @PostMapping
    public Object addDoctor(@RequestBody Doctors doctors) {
        boolean result = doctorsService.addDoctor(doctors);
        return ResultUtils.success(result);
    }

    /**
     * 更新医生信息
     *
     * @param doctors 医生信息
     * @return 操作结果
     */
    @PutMapping
    public Object updateDoctor(@RequestBody Doctors doctors) {
        boolean result = doctorsService.updateDoctor(doctors);
        return ResultUtils.success(result);
    }

    /**
     * 删除医生
     *
     * @param doctorId 医生ID
     * @return 操作结果
     */
    @DeleteMapping("/{doctorId}")
    public Object deleteDoctor(@PathVariable Long doctorId) {
        boolean result = doctorsService.deleteDoctor(doctorId);
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取医生信息
     *
     * @param doctorId 医生ID
     * @return 医生信息
     */
    @GetMapping("/{doctorId}")
    public Object getDoctorById(@PathVariable Long doctorId) {
        Doctors doctor = doctorsService.getDoctorById(doctorId);
        return ResultUtils.success(doctor);
    }

}