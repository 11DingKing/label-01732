package com.cardmanager.util;

import cn.hutool.core.util.StrUtil;

/**
 * XSS 过滤工具类
 */
public class XssUtil {

    private XssUtil() {
    }

    /**
     * 清理 XSS 攻击字符
     */
    public static String clean(String value) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        
        // 替换常见的 XSS 攻击字符
        value = value.replaceAll("<", "&lt;")
                     .replaceAll(">", "&gt;")
                     .replaceAll("\\(", "&#40;")
                     .replaceAll("\\)", "&#41;")
                     .replaceAll("'", "&#39;")
                     .replaceAll("\"", "&quot;");
        
        // 过滤 javascript 协议
        value = value.replaceAll("(?i)javascript:", "");
        value = value.replaceAll("(?i)vbscript:", "");
        
        // 过滤事件处理器
        value = value.replaceAll("(?i)on\\w+\\s*=", "");
        
        return value;
    }

    /**
     * 检查字符串是否包含 XSS 攻击特征
     */
    public static boolean hasXssRisk(String value) {
        if (StrUtil.isBlank(value)) {
            return false;
        }
        
        String lowerValue = value.toLowerCase();
        return lowerValue.contains("<script") ||
               lowerValue.contains("javascript:") ||
               lowerValue.contains("vbscript:") ||
               lowerValue.contains("onclick") ||
               lowerValue.contains("onerror") ||
               lowerValue.contains("onload") ||
               lowerValue.contains("onmouseover");
    }
}
