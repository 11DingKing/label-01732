package com.cardmanager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 卡密批次实体
 */
@Data
@TableName("card_batch")
public class CardBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 批次号（YYYYMMDDHHmmss格式）
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
     * 操作员ID
     */
    private Long operatorId;

    /**
     * 操作员姓名
     */
    private String operatorName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}
