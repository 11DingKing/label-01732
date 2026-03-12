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
public class CardBatchVerifyResultVO {

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
     * 失败详情
     */
    private List<FailDetail> failDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailDetail {
        /**
         * 行号
         */
        private Integer rowIndex;

        /**
         * 卡号
         */
        private String cardNumber;

        /**
         * 失败原因
         */
        private String reason;
    }
}
