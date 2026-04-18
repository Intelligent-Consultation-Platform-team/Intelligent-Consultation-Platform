package com.backend.controller;

import com.backend.common.ResultUtils;
import com.backend.model.entity.Departments;
import com.backend.service.DepartmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *  科室管理控制器。
 *
 * @author 佳尔宇柔
 */
@RestController
@RequestMapping("/departments")
public class DepartmentsController {

    @Autowired
    private DepartmentsService departmentsService;

    /**
     * 获取科室列表
     *
     * @return 科室列表
     */
    @GetMapping
    public Object getDepartmentsList() {
        List<Departments> departmentsList = departmentsService.getDepartmentsList();
        return ResultUtils.success(departmentsList);
    }

}
