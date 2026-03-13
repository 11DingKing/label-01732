package com.cardmanager.util;

import cn.hutool.core.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 卡号生成器
 * 
 * <h3>设计说明（关于雪花算法）</h3>
 * <p>
 * 原需求建议使用"雪花算法"生成卡号，但由于以下约束，本项目采用随机数 + 数据库查重的方案：
 * </p>
 * <ul>
 *   <li><b>9位数字约束</b>：雪花算法生成的是64位长整型（约19位十进制数字），无法直接满足"9位数字"的要求</li>
 *   <li><b>可读性要求</b>：卡号需要用户手动输入，9位数字更易记忆和输入</li>
 *   <li><b>性能考量</b>：在10000张卡的发卡量级下，随机生成+批量查重的方案性能完全足够</li>
 * </ul>
 * <p>
 * 当前实现：
 * <ol>
 *   <li>使用 ThreadLocalRandom 生成 9 位随机数字（100000000 ~ 999999999）</li>
 *   <li>批量预生成 count*2 个候选卡号</li>
 *   <li>通过数据库批量查询过滤已存在的卡号</li>
 *   <li>若候选不足则逐个补充生成</li>
 * </ol>
 * </p>
 */
public class CardNumberGenerator {

    private static final Logger log = LoggerFactory.getLogger(CardNumberGenerator.class);

    private CardNumberGenerator() {
    }

    /**
     * 生成9位唯一卡号
     * <p>
     * 使用 ThreadLocalRandom 生成高性能随机数，
     * 唯一性由调用方通过数据库查重保证。
     * </p>
     */
    public static String generateCardNumber() {
        // 生成100000000到999999999之间的9位数字
        long number = ThreadLocalRandom.current().nextLong(100000000L, 1000000000L);
        return String.valueOf(number);
    }

    /**
     * 批量生成卡号（预生成，不保证唯一性，需后续与数据库校验）
     */
    public static Set<String> generateCardNumbers(int count) {
        Set<String> cardNumbers = new HashSet<>();
        int maxAttempts = count * 3; // 最大尝试次数
        int attempts = 0;
        
        while (cardNumbers.size() < count && attempts < maxAttempts) {
            cardNumbers.add(generateCardNumber());
            attempts++;
        }
        
        if (cardNumbers.size() < count) {
            log.warn("卡号生成数量不足: 期望={}, 实际={}", count, cardNumbers.size());
        }
        
        return cardNumbers;
    }

    /**
     * 生成6位随机密码（包含数字和大写字母）
     */
    public static String generatePassword() {
        return RandomUtil.randomString("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ", 6);
    }

    /**
     * 生成批次号（严格遵循 YYYYMMDDHHmmss 格式，14位）
     * 注意：同一秒内多次调用会返回相同的批次号，业务上应避免同秒多次发卡
     */
    public static String generateBatchNumber() {
        return cn.hutool.core.date.DateUtil.format(new java.util.Date(), "yyyyMMddHHmmss");
    }
}
