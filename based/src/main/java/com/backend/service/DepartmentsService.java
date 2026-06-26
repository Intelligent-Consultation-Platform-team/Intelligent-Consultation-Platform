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

    /**
     * 添加科室
     *
     * @param departments 科室信息
     * @return 是否成功
     */
    boolean addDepartment(Departments departments);

    /**
     * 更新科室
     *
     * @param departments 科室信息
     * @return 是否成功
     */
    boolean updateDepartment(Departments departments);

    /**
     * 删除科室
     *
     * @param deptId 科室ID
     * @return 是否成功
     */
    boolean deleteDepartment(Long deptId);

    /**
     * 根据ID获取科室
     *
     * @param deptId 科室ID
     * @return 科室信息
     */
    Departments getDepartmentById(Long deptId);

}
