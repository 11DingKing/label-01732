package com.cardmanager.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 批量核销Excel导入DTO
 */
@Data
public class CardBatchVerifyExcelDTO {

    @ExcelProperty(value = "卡号", index = 0)
    private String cardNumber;

    @ExcelProperty(value = "密码", index = 1)
    private String cardPassword;
}
