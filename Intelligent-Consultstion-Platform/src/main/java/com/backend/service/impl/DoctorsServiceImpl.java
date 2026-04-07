package com.backend.service.impl;

import com.backend.mapper.DoctorsMapper;
import com.backend.model.entity.Doctors;
import com.backend.service.DoctorsService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 *  医生服务实现类。
 *
 * @author 佳尔宇柔
 */
@Service
public class DoctorsServiceImpl extends ServiceImpl<DoctorsMapper, Doctors> implements DoctorsService {

    @Override
    public List<Doctors> getDoctorsList(Integer deptId, String status) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (deptId != null) {
            queryWrapper.eq("dept_id", deptId);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        return list(queryWrapper);
    }

}
