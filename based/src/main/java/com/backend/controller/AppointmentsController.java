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
@RequestMapping("/appointment")
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
    public BaseResponse<Map<String, Object>> createAppointment(@RequestBody Appointments appointments) {
        Appointments result = appointmentsService.createAppointment(appointments);
        Map<String, Object> data = new HashMap<>();
        data.put("appointmentId", result.getAppointmentId());
        data.put("status", result.getStatus());
        return ResultUtils.success(data, "预约成功");
    }

    /**
     * 获取患者预约列表
     *
     * @param patientId 患者ID
     * @return 预约列表
     */
    @GetMapping("/patient/{patientId}")
    public BaseResponse<List<AppointmentDTO>> getPatientAppointments(@PathVariable Integer patientId) {
        List<AppointmentDTO> list = appointmentsService.getPatientAppointments(patientId);
        return ResultUtils.success(list);
    }

    /**
     * 医生获取自己的出诊列表
     */
    @GetMapping("/doctor")
    public BaseResponse<List<Map<String, Object>>> getDoctorAppointments() {
        Integer userId = UserContext.getUserId();
        List<Map<String, Object>> list = appointmentsService.getDoctorAppointments(userId);
        return ResultUtils.success(list);
    }

    /**
     * 分页查询所有预约（管理员/医生用）
     */
    @GetMapping
    public BaseResponse<Map<String, Object>> getAppointmentsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String status) {
        Map<String, Object> result = appointmentsService.getAppointmentsPage(current, size, patientName, status);
        return ResultUtils.success(result);
    }

    /**
     * 获取单个预约详情
     */
    @GetMapping("/{appointmentId}")
    public BaseResponse<Appointments> getAppointmentById(@PathVariable Integer appointmentId) {
        Appointments appointment = appointmentsService.getById(appointmentId);
        return ResultUtils.success(appointment);
    }

    /**
     * 医生接诊（状态 pending/confirmed -> processing）
     */
    @PutMapping("/{appointmentId}/process")
    public BaseResponse<Void> processAppointment(@PathVariable Integer appointmentId) {
        appointmentsService.processAppointment(appointmentId);
        return ResultUtils.success(null, "已接诊");
    }

    /**
     * 取消预约
     */
    @PutMapping("/{appointmentId}/cancel")
    public BaseResponse<Void> cancelAppointment(@PathVariable Integer appointmentId) {
        appointmentsService.cancelAppointment(appointmentId);
        return ResultUtils.success(null, "预约已取消");
    }

    /**
     * 患者到院签到（状态 pending -> confirmed）
     */
    @PutMapping("/{appointmentId}/confirm")
    public BaseResponse<Void> confirmAppointment(@PathVariable Integer appointmentId) {
        appointmentsService.confirmAppointment(appointmentId);
        return ResultUtils.success(null, "签到成功");
    }

    /**
     * 医生完成就诊（状态 processing -> completed）
     */
    @PutMapping("/{appointmentId}/complete")
    public BaseResponse<Void> completeAppointment(@PathVariable Integer appointmentId) {
        appointmentsService.completeAppointment(appointmentId);
        return ResultUtils.success(null, "就诊已完成");
    }

}
