package com.cardmanager.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 批量核销失败导出VO
 */
@Data
public class BatchVerifyFailExportVO {

    @ExcelProperty("行号")
    private Integer rowNum;

    @ExcelProperty("卡号")
    private String cardNumber;

    @ExcelProperty("错误信息")
    private String errorMessage;
}
