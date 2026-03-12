package com.cardmanager.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码加密工具单元测试
 */
@DisplayName("密码加密工具测试")
class PasswordEncoderTest {

    @Test
    @DisplayName("加密后的密码应与原始密码不同")
    void encode_ShouldReturnDifferentFromRaw() {
        String rawPassword = "123456";
        String encoded = PasswordEncoder.encode(rawPassword);
        
        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
    }

    @Test
    @DisplayName("相同密码加密两次应产生不同的结果")
    void encode_SamPasswordShouldProduceDifferentHash() {
        String rawPassword = "admin123";
        String encoded1 = PasswordEncoder.encode(rawPassword);
        String encoded2 = PasswordEncoder.encode(rawPassword);
        
        assertNotEquals(encoded1, encoded2);
    }

    @Test
    @DisplayName("正确密码验证应返回true")
    void matches_CorrectPasswordShouldReturnTrue() {
        String rawPassword = "testPassword";
        String encoded = PasswordEncoder.encode(rawPassword);
        
        assertTrue(PasswordEncoder.matches(rawPassword, encoded));
    }

    @Test
    @DisplayName("错误密码验证应返回false")
    void matches_WrongPasswordShouldReturnFalse() {
        String rawPassword = "correctPassword";
        String encoded = PasswordEncoder.encode(rawPassword);
        
        assertFalse(PasswordEncoder.matches("wrongPassword", encoded));
    }

    @Test
    @DisplayName("空密码加密不应抛出异常")
    void encode_EmptyPasswordShouldNotThrow() {
        assertDoesNotThrow(() -> PasswordEncoder.encode(""));
    }

    @Test
    @DisplayName("特殊字符密码加密验证")
    void encode_SpecialCharactersShouldWork() {
        String rawPassword = "P@ssw0rd!#$%^&*()";
        String encoded = PasswordEncoder.encode(rawPassword);
        
        assertTrue(PasswordEncoder.matches(rawPassword, encoded));
    }
}
