package com.backend.controller;

import com.backend.common.ResultUtils;
import com.backend.model.entity.Notices;
import com.backend.service.NoticesService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *  系统公告控制器。
 *
 * @author 佳尔宇柔
 */
@RestController
@RequestMapping("/notices")
public class NoticesController {

    @Resource
    private NoticesService noticesService;

    /**
     * 获取公告列表
     *
     * @return 公告列表
     */
    @GetMapping
    public Object getNoticesList() {
        List<Notices> noticesList = noticesService.getNoticesList();
        return ResultUtils.success(noticesList);
    }

    /**
     * 添加公告
     *
     * @param notices 公告信息
     * @return 操作结果
     */
    @PostMapping
    public Object addNotice(@RequestBody Notices notices) {
        boolean result = noticesService.addNotice(notices);
        return ResultUtils.success(result);
    }

    /**
     * 更新公告信息
     *
     * @param notices 公告信息
     * @return 操作结果
     */
    @PutMapping
    public Object updateNotice(@RequestBody Notices notices) {
        boolean result = noticesService.updateNotice(notices);
        return ResultUtils.success(result);
    }

    /**
     * 删除公告
     *
     * @param noticeId 公告ID
     * @return 操作结果
     */
    @DeleteMapping("/{noticeId}")
    public Object deleteNotice(@PathVariable Long noticeId) {
        boolean result = noticesService.deleteNotice(noticeId);
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取公告信息
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    @GetMapping("/{noticeId}")
    public Object getNoticeById(@PathVariable Long noticeId) {
        Notices notice = noticesService.getNoticeById(noticeId);
        return ResultUtils.success(notice);
    }

}