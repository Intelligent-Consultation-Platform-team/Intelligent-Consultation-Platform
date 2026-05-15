package com.backend.service;

import com.backend.model.entity.Appointments;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 *  预约挂号服务接口。
 *
 * @author 佳尔宇柔
 */
public interface AppointmentsService extends IService<Appointments> {

    /**
     * 创建预约
     *
     * @param appointments 预约信息
     * @return 预约结果
     */
    Appointments createAppointment(Appointments appointments);

    /**
     * 获取患者预约列表
     *
     * @param patientId 患者ID
     * @return 预约列表
     */
    List<Appointments> getPatientAppointments(Integer patientId);

}
