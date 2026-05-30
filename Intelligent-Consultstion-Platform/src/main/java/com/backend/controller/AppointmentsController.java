package com.backend.controller;

import com.backend.common.BaseResponse;
import com.backend.common.ResultUtils;
import com.backend.common.UserContext;
import com.backend.model.dto.AppointmentDTO;
import com.backend.model.entity.Appointments;
import com.backend.service.AppointmentsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  预约挂号控制器。
 *
 * @author 佳尔宇柔
 */
@RestController
@RequestMapping("/appointments")
public class AppointmentsController {

    @Resource
    private AppointmentsService appointmentsService;

    /**
     * 创建预约
     *
     * @param appointments 预约信息
     * @return 预约结果
     */
    @PostMapping
    public Object createAppointment(@RequestBody Appointments appointments) {
        Appointments result = appointmentsService.createAppointment(appointments);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询所有预约（供管理员/医生使用，支持按患者姓名模糊搜索、按状态筛选）
     */
    @GetMapping("/page")
    public BaseResponse<Map<String, Object>> getAppointmentsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String status) {
        Map<String, Object> result = appointmentsService.getAppointmentsPage(current, size, patientName, status);
        return ResultUtils.success(result);
    }

    /**
     * 获取患者预约列表
     *
     * @param patientId 患者ID
     * @return 预约列表
     */
    @GetMapping("/patient")
    public Object getPatientAppointments(@RequestParam Integer patientId) {
        List<AppointmentDTO> appointmentsList = appointmentsService.getPatientAppointments(patientId);
        return ResultUtils.success(appointmentsList);
    }

    /**
     * 取消预约
     */
    @PutMapping("/cancel/{appointmentId}")
    public BaseResponse<Void> cancelAppointment(@PathVariable Integer appointmentId) {
        appointmentsService.cancelAppointment(appointmentId);
        return ResultUtils.success(null, "预约已取消");
    }

}