package com.cardmanager.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 批次VO
 */
@Data
public class CardBatchVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 批次号
     */
    private String batchNumber;

    /**
     * 发卡数量
     */
    private Integer totalCount;

    /**
     * 已核销数量
     */
    private Integer usedCount;

    /**
     * 已回收数量
     */
    private Integer recycledCount;

    /**
     * 未使用数量
     */
    private Integer unusedCount;

    /**
     * 操作员姓名
     */
    private String operatorName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
