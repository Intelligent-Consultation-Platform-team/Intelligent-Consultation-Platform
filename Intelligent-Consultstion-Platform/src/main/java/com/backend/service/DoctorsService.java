package com.backend.service;

import com.backend.model.entity.Doctors;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 *  医生服务接口。
 *
 * @author 佳尔宇柔
 */
public interface DoctorsService extends IService<Doctors> {

    /**
     * 获取医生列表
     *
     * @param deptId 科室ID
     * @param status 状态
     * @return 医生列表
     */
    List<Doctors> getDoctorsList(Integer deptId, String status);

    /**
     * 添加医生
     *
     * @param doctors 医生信息
     * @return 是否成功
     */
    boolean addDoctor(Doctors doctors);

    /**
     * 更新医生信息
     *
     * @param doctors 医生信息
     * @return 是否成功
     */
    boolean updateDoctor(Doctors doctors);

    /**
     * 删除医生
     *
     * @param doctorId 医生ID
     * @return 是否成功
     */
    boolean deleteDoctor(Long doctorId);

    /**
     * 根据ID获取医生信息
     *
     * @param doctorId 医生ID
     * @return 医生信息
     */
    Doctors getDoctorById(Long doctorId);

}
