package com.backend.controller;

import com.backend.common.ResultUtils;
import com.backend.model.entity.Schedules;
import com.backend.service.SchedulesService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *  排班管理控制器。
 *
 * @author 佳尔宇柔
 */
@RestController
@RequestMapping("/schedules")
public class SchedulesController {

    @Resource
    private SchedulesService schedulesService;

    /**
     * 获取医生排班（包含医生信息）
     *
     * @param deptId 科室ID
     * @param doctorId 医生ID
     * @param date 日期
     * @return 排班列表（包含医生信息）
     */
    @GetMapping
    public Object getDoctorSchedules(
            @RequestParam(required = false) Integer deptId,
            @RequestParam(required = false) Integer doctorId,
            @RequestParam(required = false) String date) {
        List<?> schedulesList = schedulesService.getSchedulesWithDoctor(deptId, doctorId, date);
        return ResultUtils.success(schedulesList);
    }

    /**
     * 添加排班
     *
     * @param schedules 排班信息
     * @return 操作结果
     */
    @PostMapping
    public Object addSchedule(@RequestBody Schedules schedules) {
        boolean result = schedulesService.addSchedule(schedules);
        return ResultUtils.success(result);
    }

    /**
     * 更新排班信息
     *
     * @param schedules 排班信息
     * @return 操作结果
     */
    @PutMapping
    public Object updateSchedule(@RequestBody Schedules schedules) {
        boolean result = schedulesService.updateSchedule(schedules);
        return ResultUtils.success(result);
    }

    /**
     * 删除排班
     *
     * @param scheduleId 排班ID
     * @return 操作结果
     */
    @DeleteMapping("/{scheduleId}")
    public Object deleteSchedule(@PathVariable Long scheduleId) {
        boolean result = schedulesService.deleteSchedule(scheduleId);
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取排班信息
     *
     * @param scheduleId 排班ID
     * @return 排班信息
     */
    @GetMapping("/{scheduleId}")
    public Object getScheduleById(@PathVariable Long scheduleId) {
        Schedules schedule = schedulesService.getScheduleById(scheduleId);
        return ResultUtils.success(schedule);
    }

}