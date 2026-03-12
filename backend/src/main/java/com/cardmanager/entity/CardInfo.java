package com.cardmanager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 卡密信息实体
 */
@Data
@TableName("card_info")
public class CardInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 卡号（9位唯一数字）
     */
    private String cardNumber;

    /**
     * 密码（6位随机字符）
     */
    private String cardPassword;

    /**
     * 批次号
     */
    private String batchNumber;

    /**
     * 状态：0-未使用 1-已核销 2-已回收
     */
    private Integer status;

    /**
     * 创建操作员ID
     */
    private Long createOperatorId;

    /**
     * 创建操作员姓名
     */
    private String createOperatorName;

    /**
     * 生成时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 核销时间
     */
    private LocalDateTime useTime;

    /**
     * 核销操作员ID
     */
    private Long useOperatorId;

    /**
     * 核销操作员姓名
     */
    private String useOperatorName;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}
