package com.backend.service;

import com.backend.model.entity.Consultations;
import com.backend.model.entity.PaymentRecords;
import com.mybatisflex.core.service.IService;

import java.util.List;
import java.util.Map;

/**
 *  就诊记录服务接口。
 *
 * @author 佳尔宇柔
 */
public interface ConsultationsService extends IService<Consultations> {

    /**
     * 获取就诊记录
     */
    List<Consultations> getConsultations(Integer patientId, Integer doctorId);

    /**
     * 获取就诊详情（含缴费信息）
     */
    Map<String, Object> getConsultationDetail(Integer consultationId);

    /**
     * 医生获取自己的就诊列表（含患者姓名、预约状态）
     */
    List<Map<String, Object>> getDoctorConsultations();

    /**
     * 医生填写病历并开处方（问诊）
     */
    Consultations createConsultation(Consultations consultation);

    /**
     * 更新诊断信息
     */
    void updateConsultation(Integer consultationId, String diagnosis, String treatment, String prescription, java.math.BigDecimal amount);

    /**
     * 完成就诊
     */
    void completeConsultation(Integer consultationId, java.math.BigDecimal amount);

}
