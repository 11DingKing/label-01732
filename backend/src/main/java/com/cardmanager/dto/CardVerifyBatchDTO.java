package com.cardmanager.dto;

import com.alibaba.excel.annotation.ExcelProperty;

import java.io.Serializable;

/**
 * 批量卡密核销DTO - Excel导入使用
 */
public class CardVerifyBatchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 卡号
     */
    @ExcelProperty(value = "卡号", index = 0)
    private String cardNumber;

    /**
     * 密码
     */
    @ExcelProperty(value = "密码", index = 1)
    private String cardPassword;

    /**
     * 行号（用于标识Excel中的行）
     */
    private Integer rowNum;

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardPassword() {
        return cardPassword;
    }

    public void setCardPassword(String cardPassword) {
        this.cardPassword = cardPassword;
    }

    public Integer getRowNum() {
        return rowNum;
    }

    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }
}
