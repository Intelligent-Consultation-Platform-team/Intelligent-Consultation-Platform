package com.backend.service.impl;

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
