package com.cardmanager.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 卡密核销Excel导入VO
 */
@Data
public class CardVerifyExcelVO {

    @ExcelProperty(index = 0, value = "卡号")
    private String cardNumber;

    @ExcelProperty(index = 1, value = "密码")
    private String cardPassword;

    /**
     * 行号（用于错误提示）
     */
    private Integer rowNum;
}
