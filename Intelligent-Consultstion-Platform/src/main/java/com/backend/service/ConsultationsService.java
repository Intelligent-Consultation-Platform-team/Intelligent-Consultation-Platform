package com.backend.service;

import com.backend.model.entity.Consultations;
import com.backend.model.entity.PaymentRecords;
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

    /**
     * 医生填写病历并开处方（问诊）
     * 自动生成待缴费单（unpaid），并更新预约状态为 processing
     *
     * @param consultation 就诊信息
     * @return 就诊记录
     */
    Consultations createConsultation(Consultations consultation);

}
