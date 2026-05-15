package com.backend.controller;

import com.backend.common.ResultUtils;
import com.backend.model.entity.Departments;
import com.backend.service.DepartmentsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *  科室管理控制器。
 *
 * @author 佳尔宇柔
 */
@RestController
@RequestMapping("/departments")
public class DepartmentsController {

    @Resource
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

    /**
     * 添加科室
     *
     * @param departments 科室信息
     * @return 操作结果
     */
    @PostMapping
    public Object addDepartment(@RequestBody Departments departments) {
        boolean result = departmentsService.addDepartment(departments);
        return ResultUtils.success(result);
    }

    /**
     * 更新科室
     *
     * @param departments 科室信息
     * @return 操作结果
     */
    @PutMapping
    public Object updateDepartment(@RequestBody Departments departments) {
        boolean result = departmentsService.updateDepartment(departments);
        return ResultUtils.success(result);
    }

    /**
     * 删除科室
     *
     * @param deptId 科室ID
     * @return 操作结果
     */
    @DeleteMapping("/{deptId}")
    public Object deleteDepartment(@PathVariable Long deptId) {
        boolean result = departmentsService.deleteDepartment(deptId);
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取科室
     *
     * @param deptId 科室ID
     * @return 科室信息
     */
    @GetMapping("/{deptId}")
    public Object getDepartmentById(@PathVariable Long deptId) {
        Departments department = departmentsService.getDepartmentById(deptId);
        return ResultUtils.success(department);
    }

}