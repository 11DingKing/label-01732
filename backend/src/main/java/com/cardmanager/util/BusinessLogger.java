package com.cardmanager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * 业务日志工具类
 * 用于记录关键业务操作日志，便于审计和问题追踪
 */
public class BusinessLogger {

    private static final Logger BUSINESS_LOG = LoggerFactory.getLogger("BUSINESS");

    private BusinessLogger() {
    }

    /**
     * 记录发卡操作日志
     */
    public static void logCardGenerate(String batchNumber, int count, Long operatorId, String operatorName) {
        setMDC(operatorId, operatorName);
        BUSINESS_LOG.info("[发卡] 批次号={}, 数量={}, 操作员ID={}, 操作员={}", 
            batchNumber, count, operatorId, operatorName);
        clearMDC();
    }

    /**
     * 记录核销操作日志
     */
    public static void logCardVerify(String cardNumber, String batchNumber, Long operatorId, String operatorName) {
        setMDC(operatorId, operatorName);
        BUSINESS_LOG.info("[核销] 卡号={}, 批次号={}, 操作员ID={}, 操作员={}", 
            cardNumber, batchNumber, operatorId, operatorName);
        clearMDC();
    }

    /**
     * 记录批量核销操作日志
     */
    public static void logCardVerifyBatch(int count, Long operatorId, String operatorName) {
        setMDC(operatorId, operatorName);
        BUSINESS_LOG.info("[批量核销] 成功数量={}, 操作员ID={}, 操作员={}",
            count, operatorId, operatorName);
        clearMDC();
    }

    /**
     * 记录卡密回收操作日志
     */
    public static void logCardRecycle(String cardNumber, String batchNumber, Long operatorId, String operatorName) {
        setMDC(operatorId, operatorName);
        BUSINESS_LOG.info("[回收] 卡号={}, 批次号={}, 操作员ID={}, 操作员={}", 
            cardNumber, batchNumber, operatorId, operatorName);
        clearMDC();
    }

    /**
     * 记录批量回收操作日志
     */
    public static void logBatchRecycle(String batchNumber, int count, Long operatorId, String operatorName) {
        setMDC(operatorId, operatorName);
        BUSINESS_LOG.info("[批量回收] 批次号={}, 数量={}, 操作员ID={}, 操作员={}", 
            batchNumber, count, operatorId, operatorName);
        clearMDC();
    }

    /**
     * 记录用户登录日志
     */
    public static void logUserLogin(Long userId, String username, String ip, boolean success, String message) {
        BUSINESS_LOG.info("[登录] 用户ID={}, 用户名={}, IP={}, 结果={}, 信息={}", 
            userId, username, ip, success ? "成功" : "失败", message);
    }

    /**
     * 记录用户登出日志
     */
    public static void logUserLogout(Long userId, String username) {
        BUSINESS_LOG.info("[登出] 用户ID={}, 用户名={}", userId, username);
    }

    /**
     * 记录用户操作日志
     */
    public static void logUserOperation(String operation, Long targetUserId, String targetUsername, 
                                         Long operatorId, String operatorName) {
        setMDC(operatorId, operatorName);
        BUSINESS_LOG.info("[用户管理] 操作={}, 目标用户ID={}, 目标用户名={}, 操作员ID={}, 操作员={}", 
            operation, targetUserId, targetUsername, operatorId, operatorName);
        clearMDC();
    }

    /**
     * 记录导出操作日志
     */
    public static void logExport(String type, int count, Long operatorId, String operatorName) {
        setMDC(operatorId, operatorName);
        BUSINESS_LOG.info("[导出] 类型={}, 数量={}, 操作员ID={}, 操作员={}", 
            type, count, operatorId, operatorName);
        clearMDC();
    }

    /**
     * 记录公开查询日志
     */
    public static void logPublicQuery(String cardNumber, String ip, boolean success) {
        BUSINESS_LOG.info("[公开查询] 卡号={}, IP={}, 结果={}", 
            maskCardNumber(cardNumber), ip, success ? "成功" : "失败");
    }

    /**
     * 记录异常日志
     */
    public static void logError(String module, String operation, String message, Throwable e) {
        BUSINESS_LOG.error("[异常] 模块={}, 操作={}, 信息={}", module, operation, message, e);
    }

    /**
     * 卡号脱敏处理
     */
    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        return cardNumber.substring(0, 3) + "***" + cardNumber.substring(cardNumber.length() - 3);
    }

    /**
     * 设置MDC上下文
     */
    private static void setMDC(Long operatorId, String operatorName) {
        if (operatorId != null) {
            MDC.put("operatorId", String.valueOf(operatorId));
        }
        if (operatorName != null) {
            MDC.put("operatorName", operatorName);
        }
    }

    /**
     * 清除MDC上下文
     */
    private static void clearMDC() {
        MDC.remove("operatorId");
        MDC.remove("operatorName");
    }
}
