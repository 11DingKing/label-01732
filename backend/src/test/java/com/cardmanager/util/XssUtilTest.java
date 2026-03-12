package com.cardmanager.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XSS 工具类单元测试
 */
@DisplayName("XSS 工具类测试")
class XssUtilTest {

    @Test
    @DisplayName("应该清理 script 标签")
    void clean_ShouldRemoveScriptTag() {
        String input = "<script>alert('xss')</script>";
        String result = XssUtil.clean(input);
        
        assertFalse(result.contains("<script"));
        assertFalse(result.contains("</script"));
    }

    @Test
    @DisplayName("应该清理 javascript 协议")
    void clean_ShouldRemoveJavascriptProtocol() {
        String input = "javascript:alert('xss')";
        String result = XssUtil.clean(input);
        
        assertFalse(result.toLowerCase().contains("javascript:"));
    }

    @Test
    @DisplayName("应该清理事件处理器")
    void clean_ShouldRemoveEventHandlers() {
        String input = "<img src='x' onerror='alert(1)'>";
        String result = XssUtil.clean(input);
        
        assertFalse(result.contains("onerror="));
    }

    @Test
    @DisplayName("空字符串应该返回空")
    void clean_EmptyStringShouldReturnEmpty() {
        assertNull(XssUtil.clean(null));
        assertEquals("", XssUtil.clean(""));
    }

    @Test
    @DisplayName("应该检测 XSS 风险")
    void hasXssRisk_ShouldDetectRisk() {
        assertTrue(XssUtil.hasXssRisk("<script>"));
        assertTrue(XssUtil.hasXssRisk("javascript:"));
        assertTrue(XssUtil.hasXssRisk("onclick="));
        assertTrue(XssUtil.hasXssRisk("onerror="));
        
        assertFalse(XssUtil.hasXssRisk("normal text"));
        assertFalse(XssUtil.hasXssRisk("hello world"));
        assertFalse(XssUtil.hasXssRisk(null));
    }
}
