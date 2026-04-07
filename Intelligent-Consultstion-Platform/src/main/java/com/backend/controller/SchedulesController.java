package com.backend.controller;

import com.backend.common.ResultUtils;
import com.backend.model.entity.Schedules;
import com.backend.service.SchedulesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *  排班管理控制器。
 *
 * @author 佳尔宇柔
 */
@RestController
@RequestMapping("/api/schedules")
public class SchedulesController {

    @Autowired
    private SchedulesService schedulesService;

    /**
     * 获取医生排班
     *
     * @param doctorId 医生ID
     * @param date 日期
     * @return 排班列表
     */
    @GetMapping
    public Object getDoctorSchedules(
            @RequestParam(required = false) Integer doctorId,
            @RequestParam(required = false) String date) {
        List<Schedules> schedulesList = schedulesService.getDoctorSchedules(doctorId, date);
        return ResultUtils.success(schedulesList);
    }

}
