package com.cardmanager.controller;

import com.cardmanager.service.VerifyService;
import com.cardmanager.vo.PublicCardVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 公开查询接口测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("公开查询接口测试")
class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VerifyService verifyService;

    @Test
    @DisplayName("GET /api/public/query - 查询成功")
    void queryCardStatus_ValidCard_ReturnsStatus() throws Exception {
        // 准备
        PublicCardVO vo = new PublicCardVO();
        vo.setCardNumber("123456789");
        vo.setStatusName("未使用");
        vo.setCreateTime(LocalDateTime.now());

        when(verifyService.queryCardStatus(anyString(), anyString())).thenReturn(vo);

        // 执行 & 验证
        mockMvc.perform(get("/api/public/query")
                .param("cardNumber", "123456789")
                .param("cardPassword", "ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.cardNumber").value("123456789"))
                .andExpect(jsonPath("$.data.statusName").value("未使用"));
    }

    @Test
    @DisplayName("GET /api/public/query - 缺少卡号参数")
    void queryCardStatus_MissingCardNumber_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/public/query")
                .param("cardPassword", "ABC123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/public/query - 缺少密码参数")
    void queryCardStatus_MissingPassword_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/public/query")
                .param("cardNumber", "123456789"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/health - 健康检查")
    void health_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("OK"));
    }
}
