package com.cardmanager.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 卡密回收DTO
 */
@Data
public class CardRecycleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 批次号（按批次回收时使用）
     */
    private String batchNumber;

    /**
     * 卡号（单独回收时使用）
     */
    private String cardNumber;
}
