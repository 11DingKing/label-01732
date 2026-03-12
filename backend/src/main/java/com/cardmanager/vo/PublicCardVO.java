package com.cardmanager.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公开查询卡密VO
 */
@Data
public class PublicCardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 卡号
     */
    private String cardNumber;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 生成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 核销时间（如已核销）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime useTime;
}
