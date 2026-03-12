package com.cardmanager.controller;

import com.cardmanager.annotation.OperationLog;
import com.cardmanager.common.Constants;
import com.cardmanager.common.Result;
import com.cardmanager.dto.LoginDTO;
import com.cardmanager.service.AuthService;
import com.cardmanager.vo.LoginVO;
import com.cardmanager.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @OperationLog(module = Constants.LogModule.AUTH, operation = Constants.LogOperation.LOGIN)
    public Result<LoginVO> login(@RequestBody @Validated LoginDTO dto) {
        LoginVO vo = authService.login(dto);
        return Result.success("登录成功", vo);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @OperationLog(module = Constants.LogModule.AUTH, operation = Constants.LogOperation.LOGOUT)
    public Result<Void> logout() {
        // JWT 无状态，前端清除 Token 即可
        return Result.success("登出成功", null);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> getCurrentUser() {
        UserVO vo = authService.getCurrentUser();
        return Result.success(vo);
    }
}
