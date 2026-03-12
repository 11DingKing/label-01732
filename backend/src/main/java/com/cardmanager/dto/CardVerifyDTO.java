package com.cardmanager.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 卡密核销DTO
 */
@Data
public class CardVerifyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 卡号
     */
    @NotBlank(message = "卡号不能为空")
    private String cardNumber;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String cardPassword;
}
