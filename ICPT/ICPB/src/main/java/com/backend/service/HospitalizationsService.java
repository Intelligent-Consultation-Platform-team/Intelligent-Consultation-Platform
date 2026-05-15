package com.backend.service;

import com.backend.model.entity.Hospitalizations;
import com.mybatisflex.core.service.IService;

/**
 *  住院管理服务接口。
 *
 * @author 佳尔宇柔
 */
public interface HospitalizationsService extends IService<Hospitalizations> {

    /**
     * 住院登记
     *
     * @param hospitalizations 住院信息
     * @return 住院登记结果
     */
    Hospitalizations createHospitalization(Hospitalizations hospitalizations);

}
