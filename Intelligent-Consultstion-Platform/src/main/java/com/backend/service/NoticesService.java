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

    /**
     * 添加公告
     *
     * @param notices 公告信息
     * @return 是否成功
     */
    boolean addNotice(Notices notices);

    /**
     * 更新公告信息
     *
     * @param notices 公告信息
     * @return 是否成功
     */
    boolean updateNotice(Notices notices);

    /**
     * 删除公告
     *
     * @param noticeId 公告ID
     * @return 是否成功
     */
    boolean deleteNotice(Long noticeId);

    /**
     * 根据ID获取公告信息
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    Notices getNoticeById(Long noticeId);

}
