package com.cardmanager.service;

import com.cardmanager.common.Constants;
import com.cardmanager.dto.LoginDTO;
import com.cardmanager.entity.SysUser;
import com.cardmanager.exception.BusinessException;
import com.cardmanager.mapper.SysUserMapper;
import com.cardmanager.security.JwtUtil;
import com.cardmanager.service.impl.AuthServiceImpl;
import com.cardmanager.util.PasswordEncoder;
import com.cardmanager.vo.LoginVO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 认证服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("认证服务测试")
class AuthServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("登录成功 - 返回Token")
    void login_ValidCredentials_ReturnsToken() {
        // 准备
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword(PasswordEncoder.encode("admin123"));
        user.setRealName("管理员");
        user.setRole("admin");
        user.setStatus(Constants.UserStatus.ENABLED);

        when(sysUserMapper.selectByUsername("admin")).thenReturn(user);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("test-token");

        // 执行
        LoginVO result = authService.login(dto);

        // 验证
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals("管理员", result.getRealName());
        assertEquals("admin", result.getRole());
        assertEquals("test-token", result.getToken());
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void login_UserNotFound_ThrowsException() {
        // 准备
        LoginDTO dto = new LoginDTO();
        dto.setUsername("nonexistent");
        dto.setPassword("password");

        when(sysUserMapper.selectByUsername("nonexistent")).thenReturn(null);

        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> authService.login(dto));
        assertEquals("账号或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_WrongPassword_ThrowsException() {
        // 准备
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("wrongpassword");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword(PasswordEncoder.encode("admin123"));
        user.setStatus(Constants.UserStatus.ENABLED);

        when(sysUserMapper.selectByUsername("admin")).thenReturn(user);

        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> authService.login(dto));
        assertEquals("账号或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("登录失败 - 用户被禁用")
    void login_UserDisabled_ThrowsException() {
        // 准备
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword(PasswordEncoder.encode("admin123"));
        user.setStatus(Constants.UserStatus.DISABLED);

        when(sysUserMapper.selectByUsername("admin")).thenReturn(user);

        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> authService.login(dto));
        assertEquals("用户已被禁用", exception.getMessage());
    }
}
