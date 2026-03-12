package com.cardmanager.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 卡号生成器单元测试
 */
@DisplayName("卡号生成器测试")
class CardNumberGeneratorTest {

    @Test
    @DisplayName("生成的卡号应为9位数字")
    void generateCardNumber_ShouldReturn9DigitNumber() {
        String cardNumber = CardNumberGenerator.generateCardNumber();
        
        assertNotNull(cardNumber);
        assertEquals(9, cardNumber.length());
        assertTrue(Pattern.matches("\\d{9}", cardNumber));
    }

    @Test
    @DisplayName("生成的卡号应在有效范围内")
    void generateCardNumber_ShouldBeInValidRange() {
        String cardNumber = CardNumberGenerator.generateCardNumber();
        long number = Long.parseLong(cardNumber);
        
        assertTrue(number >= 100000000L);
        assertTrue(number < 1000000000L);
    }

    @RepeatedTest(100)
    @DisplayName("批量生成的卡号应无重复")
    void generateCardNumbers_ShouldBeUnique() {
        Set<String> cardNumbers = CardNumberGenerator.generateCardNumbers(100);
        
        assertNotNull(cardNumbers);
        assertEquals(100, cardNumbers.size()); // 无重复则集合大小等于请求数量
    }

    @Test
    @DisplayName("生成的密码应为6位字母数字")
    void generatePassword_ShouldReturn6CharAlphanumeric() {
        String password = CardNumberGenerator.generatePassword();
        
        assertNotNull(password);
        assertEquals(6, password.length());
        assertTrue(Pattern.matches("[A-Z0-9]{6}", password));
    }

    @Test
    @DisplayName("生成的批次号应符合时间格式")
    void generateBatchNumber_ShouldMatchTimeFormat() {
        String batchNumber = CardNumberGenerator.generateBatchNumber();
        
        assertNotNull(batchNumber);
        assertEquals(14, batchNumber.length());
        assertTrue(Pattern.matches("\\d{14}", batchNumber));
    }

    @Test
    @DisplayName("批量生成应处理数量为0的情况")
    void generateCardNumbers_ShouldHandleZeroCount() {
        Set<String> cardNumbers = CardNumberGenerator.generateCardNumbers(0);
        
        assertNotNull(cardNumbers);
        assertTrue(cardNumbers.isEmpty());
    }

    @Test
    @DisplayName("批量生成大量卡号性能测试")
    void generateCardNumbers_PerformanceTest() {
        long startTime = System.currentTimeMillis();
        Set<String> cardNumbers = CardNumberGenerator.generateCardNumbers(10000);
        long endTime = System.currentTimeMillis();
        
        assertNotNull(cardNumbers);
        assertTrue(cardNumbers.size() >= 9000); // 允许少量重复
        assertTrue(endTime - startTime < 5000); // 应在5秒内完成
    }
}
