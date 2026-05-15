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

}
