package com.backend.service;

import com.backend.model.entity.Notices;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 *  系统公告服务接口。
 *
 * @author 佳尔宇柔
 */
public interface NoticesService extends IService<Notices> {

    /**
     * 获取公告列表
     *
     * @return 公告列表
     */
    List<Notices> getNoticesList();

}
