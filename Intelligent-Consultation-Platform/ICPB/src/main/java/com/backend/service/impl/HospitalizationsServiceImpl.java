package com.backend.service.impl;

import com.backend.mapper.HospitalizationsMapper;
import com.backend.model.entity.Hospitalizations;
import com.backend.service.HospitalizationsService;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

/**
 *  住院管理服务实现类。
 *
 * @author 佳尔宇柔
 */
@Service
public class HospitalizationsServiceImpl extends ServiceImpl<HospitalizationsMapper, Hospitalizations> implements HospitalizationsService {

    @Override
    public Hospitalizations createHospitalization(Hospitalizations hospitalizations) {
        hospitalizations.setStatus("admitted");
        save(hospitalizations);
        return hospitalizations;
    }

}
