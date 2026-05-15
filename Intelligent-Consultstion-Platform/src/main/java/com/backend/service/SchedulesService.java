package com.backend.service;

import com.backend.model.entity.Schedules;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 *  排班服务接口。
 *
 * @author 佳尔宇柔
 */
public interface SchedulesService extends IService<Schedules> {

    /**
     * 获取医生排班
     *
     * @param doctorId 医生ID
     * @param date 日期
     * @return 排班列表
     */
    List<Schedules> getDoctorSchedules(Integer doctorId, String date);

    /**
     * 添加排班
     *
     * @param schedules 排班信息
     * @return 是否成功
     */
    boolean addSchedule(Schedules schedules);

    /**
     * 更新排班信息
     *
     * @param schedules 排班信息
     * @return 是否成功
     */
    boolean updateSchedule(Schedules schedules);

    /**
     * 删除排班
     *
     * @param scheduleId 排班ID
     * @return 是否成功
     */
    boolean deleteSchedule(Long scheduleId);

    /**
     * 根据ID获取排班信息
     *
     * @param scheduleId 排班ID
     * @return 排班信息
     */
    Schedules getScheduleById(Long scheduleId);

}
