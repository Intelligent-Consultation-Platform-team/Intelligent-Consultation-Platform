package com.backend.service;

import com.backend.model.entity.Consultations;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 *  就诊记录服务接口。
 *
 * @author 佳尔宇柔
 */
public interface ConsultationsService extends IService<Consultations> {

    /**
     * 获取就诊记录
     *
     * @param patientId 患者ID
     * @param doctorId 医生ID
     * @return 就诊记录列表
     */
    List<Consultations> getConsultations(Integer patientId, Integer doctorId);

}
