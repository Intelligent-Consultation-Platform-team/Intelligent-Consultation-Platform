package com.backend.service;

import com.backend.model.dto.AppointmentDTO;
import com.backend.model.entity.Appointments;
import com.mybatisflex.core.service.IService;

import java.util.List;
import java.util.Map;

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
     * 获取患者预约列表（包含详细信息）
     *
     * @param patientId 患者ID
     * @return 预约列表
     */
    List<AppointmentDTO> getPatientAppointments(Integer patientId);

    /**
     * 分页查询所有预约（支持按患者姓名模糊搜索、按状态筛选）
     *
     * @param current      当前页
     * @param size         每页大小
     * @param patientName  患者姓名（可选，模糊搜索）
     * @param status       状态（可选）
     * @return 包含分页信息和数据列表
     */
    Map<String, Object> getAppointmentsPage(Integer current, Integer size, String patientName, String status);

    /**
     * 取消预约
     *
     * @param appointmentId 预约ID
     */
    void cancelAppointment(Integer appointmentId);

    /**
     * 患者到院签到，将预约状态改为 confirmed
     *
     * @param appointmentId 预约ID
     */
    void confirmAppointment(Integer appointmentId);

    /**
     * 医生完成就诊，将预约状态改为 completed
     *
     * @param appointmentId 预约ID
     */
    void completeAppointment(Integer appointmentId);

    /**
     * 医生获取自己的出诊列表（包含患者信息）
     *
     * @param doctorUserId 医生的用户ID
     * @return 出诊列表
     */
    List<Map<String, Object>> getDoctorAppointments(Integer doctorUserId);

}
