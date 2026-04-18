package com.backend.controller;

import com.backend.common.ResultUtils;
import com.backend.model.entity.Notices;
import com.backend.service.NoticesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *  系统公告控制器。
 *
 * @author 佳尔宇柔
 */
@RestController
@RequestMapping("/notices")
public class NoticesController {

    @Autowired
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

}
