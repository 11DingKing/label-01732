package com.cardmanager.service.impl;

import com.cardmanager.common.Constants;
import com.cardmanager.dto.LoginDTO;
import com.cardmanager.entity.SysUser;
import com.cardmanager.exception.BusinessException;
import com.cardmanager.mapper.SysUserMapper;
import com.cardmanager.security.JwtUtil;
import com.cardmanager.security.UserContext;
import com.cardmanager.service.AuthService;
import com.cardmanager.util.PasswordEncoder;
import com.cardmanager.vo.LoginVO;
import com.cardmanager.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 查询用户
        SysUser user = sysUserMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            log.warn("登录失败: 用户不存在, username={}", dto.getUsername());
            throw new BusinessException("账号或密码错误");
        }

        // 验证密码
        if (!PasswordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("登录失败: 密码错误, username={}", dto.getUsername());
            throw new BusinessException("账号或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == Constants.UserStatus.DISABLED) {
            log.warn("登录失败: 用户已禁用, username={}", dto.getUsername());
            throw new BusinessException("用户已被禁用");
        }

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 构建响应
        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setRoleName(Constants.UserRole.getRoleName(user.getRole()));
        vo.setToken(token);

        log.info("用户登录成功: username={}, role={}", user.getUsername(), user.getRole());
        return vo;
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setRoleName(Constants.UserRole.getRoleName(user.getRole()));
        vo.setStatus(user.getStatus());
        vo.setStatusName(Constants.UserStatus.getStatusName(user.getStatus()));
        vo.setCreateTime(user.getCreateTime());

        return vo;
    }
}
