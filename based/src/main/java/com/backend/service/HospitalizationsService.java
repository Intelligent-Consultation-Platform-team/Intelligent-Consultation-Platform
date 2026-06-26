package com.backend.service;

import com.backend.model.entity.Hospitalizations;
import com.mybatisflex.core.service.IService;

import java.util.Map;

/**
 * 住院管理服务接口。
 *
 * @author 佳尔宇柔
 */
public interface HospitalizationsService extends IService<Hospitalizations> {

    /**
     * 住院登记
     */
    Hospitalizations createHospitalization(Hospitalizations hospitalizations);

    /**
     * 分页查询住院列表
     */
    Map<String, Object> getPage(Integer current, Integer size, String patientName, String status);

    /**
     * 出院
     */
    void discharge(Integer hospitalizationId);

    /**
     * 检查患者是否正在住院
     */
    boolean isPatientAdmitted(Integer patientId);

}
