package com.backend.service.impl;

import com.backend.mapper.SchedulesMapper;
import com.backend.model.entity.Schedules;
import com.backend.service.SchedulesService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.sql.Timestamp;

/**
 *  排班服务实现类。
 *
 * @author 佳尔宇柔
 */
@Service
public class SchedulesServiceImpl extends ServiceImpl<SchedulesMapper, Schedules> implements SchedulesService {

    @Override
    public List<Schedules> getDoctorSchedules(Integer doctorId, String date) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (doctorId != null) {
            queryWrapper.eq("doctor_id", doctorId);
        }
        if (date != null) {
            // 计算日期对应的星期几（1-7）
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            int dayOfWeek = localDate.getDayOfWeek().getValue();
            queryWrapper.eq("day_of_week", dayOfWeek);
        }
        return list(queryWrapper);
    }

    @Override
    public boolean addSchedule(Schedules schedules) {
        return save(schedules);
    }

    @Override
    public boolean updateSchedule(Schedules schedules) {
        return updateById(schedules);
    }

    @Override
    public boolean deleteSchedule(Long scheduleId) {
        return removeById(scheduleId);
    }

    @Override
    public Schedules getScheduleById(Long scheduleId) {
        return getById(scheduleId);
    }

}
