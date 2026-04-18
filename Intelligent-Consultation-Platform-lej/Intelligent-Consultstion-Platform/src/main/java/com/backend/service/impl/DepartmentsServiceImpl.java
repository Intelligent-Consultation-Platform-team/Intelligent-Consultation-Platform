package com.backend.service.impl;

import com.backend.mapper.DepartmentsMapper;
import com.backend.model.entity.Departments;
import com.backend.service.DepartmentsService;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

import java.util.List;

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

}
