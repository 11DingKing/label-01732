package com.cardmanager.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量核销结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchVerifyResultVO {

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
    private List<FailRecordVO> failRecords;

    /**
     * 失败记录VO
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FailRecordVO {
        private Integer rowNum;
        private String cardNumber;
        private String errorMessage;
    }
}
