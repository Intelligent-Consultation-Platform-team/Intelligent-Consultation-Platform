package com.backend.service;

import com.backend.model.entity.Departments;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 *  科室服务接口。
 *
 * @author 佳尔宇柔
 */
public interface DepartmentsService extends IService<Departments> {

    /**
     * 获取科室列表
     *
     * @return 科室列表
     */
    List<Departments> getDepartmentsList();

}
