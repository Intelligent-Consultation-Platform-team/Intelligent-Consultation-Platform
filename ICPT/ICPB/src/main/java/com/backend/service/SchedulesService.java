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

}
