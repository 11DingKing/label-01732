package com.cardmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量核销结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchVerifyResultDTO {

    /**
     * 总条数
     */
    private Integer totalCount;

    /**
     * 成功条数
     */
    private Integer successCount;

    /**
     * 失败条数
     */
    private Integer failCount;

    /**
     * 成功的卡号列表
     */
    private List<String> successCards;

    /**
     * 失败记录列表
     */
    private List<FailRecord> failRecords;

    /**
     * 失败记录
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FailRecord {
        private Integer rowNum;
        private String cardNumber;
        private String errorMessage;
    }
}
