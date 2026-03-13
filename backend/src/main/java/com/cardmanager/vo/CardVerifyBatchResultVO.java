package com.cardmanager.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 批量核销结果VO
 */
public class CardVerifyBatchResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总数量
     */
    private Integer totalCount;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 成功卡号列表
     */
    private List<String> successCards;

    /**
     * 失败详情列表
     */
    private List<FailDetail> failDetails;

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public List<String> getSuccessCards() {
        return successCards;
    }

    public void setSuccessCards(List<String> successCards) {
        this.successCards = successCards;
    }

    public List<FailDetail> getFailDetails() {
        return failDetails;
    }

    public void setFailDetails(List<FailDetail> failDetails) {
        this.failDetails = failDetails;
    }

    public static class FailDetail implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 卡号
         */
        private String cardNumber;

        /**
         * 行号
         */
        private Integer rowNum;

        /**
         * 失败原因
         */
        private String reason;

        public FailDetail(String cardNumber, Integer rowNum, String reason) {
            this.cardNumber = cardNumber;
            this.rowNum = rowNum;
            this.reason = reason;
        }

        public String getCardNumber() {
            return cardNumber;
        }

        public void setCardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
        }

        public Integer getRowNum() {
            return rowNum;
        }

        public void setRowNum(Integer rowNum) {
            this.rowNum = rowNum;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
