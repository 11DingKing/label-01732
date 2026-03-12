package com.cardmanager.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 核销记录导出VO
 */
@Data
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
}
