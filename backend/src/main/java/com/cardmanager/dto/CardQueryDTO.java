package com.cardmanager.dto;

import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 卡密查询DTO
 */
public class CardQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 批次号
     */
    private String batchNumber;

    /**
     * 卡号
     */
    private String cardNumber;

    /**
     * 状态：0-未使用 1-已核销 2-已回收
     */
    private Integer status;

    /**
     * 当前页码
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小最小为1")
    private Integer pageSize = 10;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
