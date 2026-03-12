package com.cardmanager.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 卡密查询DTO
 */
@Data
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
}
