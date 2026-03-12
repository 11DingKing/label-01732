package com.cardmanager.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 发卡请求DTO
 */
@Data
public class CardGenerateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 发卡数量
     */
    @NotNull(message = "发卡数量不能为空")
    @Min(value = 1, message = "发卡数量最少为1张")
    @Max(value = 10000, message = "发卡数量最多为10000张")
    private Integer count;
}
