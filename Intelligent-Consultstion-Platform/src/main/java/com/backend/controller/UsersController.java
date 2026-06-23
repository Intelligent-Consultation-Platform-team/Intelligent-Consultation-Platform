package com.backend.controller;

import com.backend.common.ResultUtils;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import com.backend.model.dto.ResetPasswordRequest;
import com.backend.model.entity.Users;
import com.backend.service.UsersService;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 *  控制层。
 *
 * @author 佳尔宇柔
 */
@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UsersService usersService;

    /**
     * 保存。
     *
     * @param users
     * @return 操作结果
     */
    @PostMapping("save")
    public Object save(@RequestBody Users users) {
        boolean result = usersService.save(users);
        return ResultUtils.success(result);
    }

    /**
     * 根据主键删除。
     *
     * @param id 主键
     * @return 操作结果
     */
    @DeleteMapping("remove/{id}")
    public Object remove(@PathVariable Integer id) {
        boolean result = usersService.deleteUser(id);
        return ResultUtils.success(result);
    }

    /**
     * 根据主键更新。
     *
     * @param users
     * @return 操作结果
     */
    @PutMapping("update")
    public Object update(@RequestBody Users users) {
        boolean result = usersService.updateById(users);
        return ResultUtils.success(result);
    }

    /**
     * 查询所有。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public Object list() {
        QueryWrapper qw = QueryWrapper.create().eq("status", "active");
        List<Users> result = usersService.list(qw);
        return ResultUtils.success(result);
    }

    /**
     * 根据主键获取。
     *
     * @param id 主键
     * @return 详情
     */
    @GetMapping("getInfo/{id}")
    public Object getInfo(@PathVariable Integer id) {
        Users result = usersService.getById(id);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询（支持关键词搜索）。
     *
     * @param page 分页对象
     * @param keyword 搜索关键词（匹配用户名、真实姓名、手机号）
     * @return 分页对象
     */
    @GetMapping("page")
    public Object page(Page<Users> page, @RequestParam(required = false) String keyword) {
        QueryWrapper queryWrapper = QueryWrapper.create().eq("status", "active");
        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword + "%";
            queryWrapper.where("(username like ? or real_name like ? or phone like ?)", kw, kw, kw);
        }
        Page<Users> result = usersService.page(page, queryWrapper);
        return ResultUtils.success(result);
    }

    /**
     * 重置用户密码为默认密码123456
     *
     * @param request 重置密码请求
     * @return 操作结果
     */
    @PostMapping("resetPassword")
    public Object resetPassword(@RequestBody ResetPasswordRequest request) {
        boolean result = usersService.resetPassword(request.getUserId());
        return ResultUtils.success(result);
    }

}
