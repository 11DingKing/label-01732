package com.cardmanager.service;

import com.cardmanager.dto.LoginDTO;
import com.cardmanager.vo.LoginVO;
import com.cardmanager.vo.UserVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUser();
}
