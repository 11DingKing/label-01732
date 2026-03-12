package com.cardmanager.util;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码加密工具
 */
public class PasswordEncoder {

    private PasswordEncoder() {
    }

    /**
     * 加密密码
     */
    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
