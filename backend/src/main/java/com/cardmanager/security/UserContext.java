package com.cardmanager.security;

import lombok.Data;

/**
 * 用户上下文（线程安全）
 */
public class UserContext {

    private static final ThreadLocal<UserInfo> USER_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户
     */
    public static void setUser(UserInfo userInfo) {
        USER_HOLDER.set(userInfo);
    }

    /**
     * 获取当前用户
     */
    public static UserInfo getUser() {
        return USER_HOLDER.get();
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        UserInfo user = getUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        UserInfo user = getUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前用户姓名
     */
    public static String getRealName() {
        UserInfo user = getUser();
        return user != null ? user.getRealName() : null;
    }

    /**
     * 获取当前用户角色
     */
    public static String getRole() {
        UserInfo user = getUser();
        return user != null ? user.getRole() : null;
    }

    /**
     * 判断是否为管理员
     */
    public static boolean isAdmin() {
        return "admin".equals(getRole());
    }

    /**
     * 清除当前用户
     */
    public static void clear() {
        USER_HOLDER.remove();
    }

    /**
     * 用户信息
     */
    @Data
    public static class UserInfo {
        private Long userId;
        private String username;
        private String realName;
        private String role;

        public UserInfo(Long userId, String username, String realName, String role) {
            this.userId = userId;
            this.username = username;
            this.realName = realName;
            this.role = role;
        }
    }
}
