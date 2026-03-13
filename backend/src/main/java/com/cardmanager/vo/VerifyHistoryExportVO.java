package com.cardmanager.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 核销记录导出VO
 */
public class VerifyHistoryExportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 卡号
     */
    @ExcelProperty("卡号")
    @ColumnWidth(15)
    private String cardNumber;

    /**
     * 密码
     */
    @ExcelProperty("密码")
    @ColumnWidth(12)
    private String cardPassword;

    /**
     * 批次号
     */
    @ExcelProperty("批次号")
    @ColumnWidth(18)
    private String batchNumber;

    /**
     * 核销时间
     */
    @ExcelProperty("核销时间")
    @ColumnWidth(20)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime useTime;

    /**
     * 核销操作员
     */
    @ExcelProperty("核销操作员")
    @ColumnWidth(15)
    private String useOperatorName;

    /**
     * 生成时间
     */
    @ExcelProperty("生成时间")
    @ColumnWidth(20)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
