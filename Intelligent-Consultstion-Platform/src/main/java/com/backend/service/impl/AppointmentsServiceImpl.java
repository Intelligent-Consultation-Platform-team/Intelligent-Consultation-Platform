package com.backend.service.impl;

import com.backend.common.UserContext;
import com.backend.exception.BusinessException;
import com.backend.exception.ErrorCode;
import com.backend.mapper.AppointmentsMapper;
import com.backend.model.entity.Appointments;
import com.backend.service.AppointmentsService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 *  预约挂号服务实现类。
 *
 * @author 佳尔宇柔
 */
@Service
public class AppointmentsServiceImpl extends ServiceImpl<AppointmentsMapper, Appointments> implements AppointmentsService {

    @Override
    public Appointments createAppointment(Appointments appointments) {
        Integer userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        if (appointments.getDoctorId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "医生ID不能为空");
        }
        
        if (appointments.getScheduleId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排班ID不能为空");
        }
        
        if (appointments.getAppointmentDate() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "预约日期不能为空");
        }
        
        appointments.setPatientId(userId);
        appointments.setStatus("pending");
        save(appointments);
        return appointments;
    }

    @Override
    public List<Appointments> getPatientAppointments(Integer patientId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("patient_id", patientId);
        return list(queryWrapper);
    }

}
