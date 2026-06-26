package com.backend.service.impl;

import com.backend.mapper.DepartmentsMapper;
import com.backend.model.entity.Departments;
import com.backend.service.DepartmentsService;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.sql.Timestamp;

/**
 *  科室服务实现类。
 *
 * @author 佳尔宇柔
 */
@Service
public class DepartmentsServiceImpl extends ServiceImpl<DepartmentsMapper, Departments> implements DepartmentsService {

    @Override
    public List<Departments> getDepartmentsList() {
        return list();
    }

    @Override
    public boolean addDepartment(Departments departments) {
        departments.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        departments.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        return save(departments);
    }

    @Override
    public boolean updateDepartment(Departments departments) {
        departments.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        return updateById(departments);
    }

    @Override
    public boolean deleteDepartment(Long deptId) {
        return removeById(deptId);
    }

    @Override
    public Departments getDepartmentById(Long deptId) {
        return getById(deptId);
    }

}
