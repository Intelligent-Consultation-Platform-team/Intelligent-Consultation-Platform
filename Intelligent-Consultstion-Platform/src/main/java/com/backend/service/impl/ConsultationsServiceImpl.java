package com.backend.service.impl;

import com.backend.mapper.ConsultationsMapper;
import com.backend.model.entity.Consultations;
import com.backend.service.ConsultationsService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 *  就诊记录服务实现类。
 *
 * @author 佳尔宇柔
 */
@Service
public class ConsultationsServiceImpl extends ServiceImpl<ConsultationsMapper, Consultations> implements ConsultationsService {

    @Override
    public List<Consultations> getConsultations(Integer patientId, Integer doctorId) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (patientId != null) {
            queryWrapper.eq("patient_id", patientId);
        }
        if (doctorId != null) {
            queryWrapper.eq("doctor_id", doctorId);
        }
        return list(queryWrapper);
    }

}
