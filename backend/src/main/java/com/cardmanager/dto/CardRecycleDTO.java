package com.cardmanager.dto;

import java.io.Serializable;

/**
 * 卡密回收DTO
 */
public class CardRecycleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 批次号（按批次回收时使用）
     */
    private String batchNumber;

    /**
     * 卡号（单独回收时使用）
     */
    private String cardNumber;

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}
