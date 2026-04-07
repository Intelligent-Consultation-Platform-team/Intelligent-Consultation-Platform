package com.backend.controller;

import com.backend.common.ResultUtils;
import com.backend.model.entity.Appointments;
import com.backend.service.AppointmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *  预约挂号控制器。
 *
 * @author 佳尔宇柔
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentsController {

    @Autowired
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
     * 获取患者预约列表
     *
     * @param patientId 患者ID
     * @return 预约列表
     */
    @GetMapping("/patient")
    public Object getPatientAppointments(@RequestParam Integer patientId) {
        List<Appointments> appointmentsList = appointmentsService.getPatientAppointments(patientId);
        return ResultUtils.success(appointmentsList);
    }

}
