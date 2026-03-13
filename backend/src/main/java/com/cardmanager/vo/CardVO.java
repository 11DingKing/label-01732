package com.cardmanager.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 卡密VO
 */
public class CardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 卡号
     */
    @ExcelProperty("卡号")
    private String cardNumber;

    /**
     * 密码
     */
    @ExcelProperty("密码")
    private String cardPassword;

    /**
     * 批次号
     */
    @ExcelProperty("批次号")
    private String batchNumber;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 状态名称
     */
    @ExcelProperty("状态")
    private String statusName;

    /**
     * 生成时间
     */
    @ExcelProperty("生成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 核销时间
     */
    @ExcelProperty("核销时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime useTime;

    /**
     * 核销操作员
     */
    @ExcelProperty("核销操作员")
    private String useOperatorName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUseTime() {
        return useTime;
    }

    public void setUseTime(LocalDateTime useTime) {
        this.useTime = useTime;
    }

    public String getUseOperatorName() {
        return useOperatorName;
    }

    public void setUseOperatorName(String useOperatorName) {
        this.useOperatorName = useOperatorName;
    }
}
