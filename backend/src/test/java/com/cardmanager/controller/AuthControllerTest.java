package com.cardmanager.controller;

import com.cardmanager.dto.LoginDTO;
import com.cardmanager.service.AuthService;
import com.cardmanager.vo.LoginVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证控制器集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("认证接口测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/login - 登录成功")
    void login_ValidCredentials_ReturnsToken() throws Exception {
        // 准备
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");

        LoginVO loginVO = new LoginVO();
        loginVO.setId(1L);
        loginVO.setUsername("admin");
        loginVO.setRealName("管理员");
        loginVO.setRole("admin");
        loginVO.setRoleName("管理员");
        loginVO.setToken("test-jwt-token");

        when(authService.login(any(LoginDTO.class))).thenReturn(loginVO);

        // 执行 & 验证
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 缺少用户名")
    void login_MissingUsername_ReturnsBadRequest() throws Exception {
        // 准备
        LoginDTO dto = new LoginDTO();
        dto.setPassword("admin123");

        // 执行 & 验证
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - 缺少密码")
    void login_MissingPassword_ReturnsBadRequest() throws Exception {
        // 准备
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");

        // 执行 & 验证
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
