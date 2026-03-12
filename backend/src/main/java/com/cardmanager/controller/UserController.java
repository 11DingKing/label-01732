package com.cardmanager.controller;

import com.cardmanager.annotation.OperationLog;
import com.cardmanager.common.Constants;
import com.cardmanager.common.PageResult;
import com.cardmanager.common.Result;
import com.cardmanager.dto.UserDTO;
import com.cardmanager.dto.UserQueryDTO;
import com.cardmanager.exception.BusinessException;
import com.cardmanager.security.UserContext;
import com.cardmanager.service.UserService;
import com.cardmanager.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 查询用户列表
     */
    @GetMapping("/list")
    public Result<PageResult<UserVO>> listUsers(@Validated UserQueryDTO dto) {
        // 权限检查：只有管理员可以查看用户列表
        checkAdminPermission();
        PageResult<UserVO> result = userService.listUsers(dto);
        return Result.success(result);
    }

    /**
     * 添加用户
     */
    @PostMapping("/add")
    @OperationLog(module = Constants.LogModule.USER, operation = Constants.LogOperation.ADD)
    public Result<Void> addUser(@RequestBody @Validated UserDTO dto) {
        checkAdminPermission();
        userService.addUser(dto);
        return Result.success("添加成功", null);
    }

    /**
     * 修改用户
     */
    @PutMapping("/update")
    @OperationLog(module = Constants.LogModule.USER, operation = Constants.LogOperation.UPDATE)
    public Result<Void> updateUser(@RequestBody @Validated UserDTO dto) {
        checkAdminPermission();
        userService.updateUser(dto);
        return Result.success("修改成功", null);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete/{id}")
    @OperationLog(module = Constants.LogModule.USER, operation = Constants.LogOperation.DELETE)
    public Result<Void> deleteUser(@PathVariable Long id) {
        checkAdminPermission();
        userService.deleteUser(id);
        return Result.success("删除成功", null);
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        checkAdminPermission();
        UserVO vo = userService.getUserById(id);
        return Result.success(vo);
    }

    /**
     * 检查管理员权限
     */
    private void checkAdminPermission() {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(403, "无权限执行此操作");
        }
    }
}
