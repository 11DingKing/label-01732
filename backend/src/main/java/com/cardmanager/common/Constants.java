package com.cardmanager.common;

/**
 * 常量定义
 */
public class Constants {

    private Constants() {
    }

    /**
     * 卡密状态
     */
    public static class CardStatus {
        public static final int UNUSED = 0;      // 未使用
        public static final int USED = 1;        // 已核销
        public static final int RECYCLED = 2;    // 已回收

        public static String getStatusName(int status) {
            switch (status) {
                case UNUSED:
                    return "未使用";
                case USED:
                    return "已核销";
                case RECYCLED:
                    return "已回收";
                default:
                    return "未知";
            }
        }
    }

    /**
     * 用户状态
     */
    public static class UserStatus {
        public static final int ENABLED = 0;     // 启用
        public static final int DISABLED = 1;    // 禁用

        public static String getStatusName(int status) {
            switch (status) {
                case ENABLED:
                    return "启用";
                case DISABLED:
                    return "禁用";
                default:
                    return "未知";
            }
        }
    }

    /**
     * 用户角色
     */
    public static class UserRole {
        public static final String ADMIN = "admin";        // 管理员
        public static final String OPERATOR = "operator";  // 操作员

        public static String getRoleName(String role) {
            if (ADMIN.equals(role)) {
                return "管理员";
            } else if (OPERATOR.equals(role)) {
                return "操作员";
            }
            return "未知";
        }
    }

    /**
     * 操作日志模块
     */
    public static class LogModule {
        public static final String AUTH = "认证管理";
        public static final String CARD = "发卡管理";
        public static final String VERIFY = "核销管理";
        public static final String USER = "用户管理";
    }

    /**
     * 操作日志类型
     */
    public static class LogOperation {
        public static final String LOGIN = "登录";
        public static final String LOGOUT = "登出";
        public static final String GENERATE = "发卡";
        public static final String RECYCLE = "回收";
        public static final String VERIFY = "核销";
        public static final String BATCH_VERIFY = "批量核销";
        public static final String ADD = "新增";
        public static final String UPDATE = "修改";
        public static final String DELETE = "删除";
        public static final String EXPORT = "导出";
    }
}
