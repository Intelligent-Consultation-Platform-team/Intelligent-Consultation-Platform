package com.backend.service.impl;

import com.backend.mapper.NoticesMapper;
import com.backend.model.entity.Notices;
import com.backend.service.NoticesService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 *  系统公告服务实现类。
 *
 * @author 佳尔宇柔
 */
@Service
public class NoticesServiceImpl extends ServiceImpl<NoticesMapper, Notices> implements NoticesService {

    @Override
    public List<Notices> getNoticesList() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("status", "active");
        return list(queryWrapper);
    }

}
