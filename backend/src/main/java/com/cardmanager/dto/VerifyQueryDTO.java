package com.cardmanager.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 核销查询DTO
 */
@Data
public class VerifyQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 卡号
     */
    private String cardNumber;

    /**
     * 批次号
     */
    private String batchNumber;

    /**
     * 核销开始日期
     */
    private String startDate;

    /**
     * 核销结束日期
     */
    private String endDate;

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
}
