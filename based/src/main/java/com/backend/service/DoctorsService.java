package com.backend.service;

import com.backend.model.dto.DoctorAddRequest;
import com.backend.model.dto.DoctorDTO;
import com.backend.model.dto.DoctorUpdateRequest;
import com.backend.model.entity.Doctors;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 医生服务接口。
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
     * 获取医生列表（包含用户信息）
     *
     * @param deptId 科室ID
     * @param status 状态
     * @return 医生列表（包含用户信息）
     */
    List<DoctorDTO> getDoctorsWithUserInfo(Integer deptId, String status);

    /**
     * 添加医生（同时创建用户账户）
     *
     * @param request 添加医生请求
     * @return 是否成功
     */
    boolean addDoctor(DoctorAddRequest request);

    /**
     * 更新医生信息（同时更新用户信息）
     *
     * @param request 更新医生请求
     * @return 是否成功
     */
    boolean updateDoctor(DoctorUpdateRequest request);

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
