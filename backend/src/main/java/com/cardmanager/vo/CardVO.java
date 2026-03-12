package com.cardmanager.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 卡密VO
 */
@Data
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
}
