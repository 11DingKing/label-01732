package com.cardmanager.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 卡密信息实体
 */
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardPassword() {
        return cardPassword;
    }

    public void setCardPassword(String cardPassword) {
        this.cardPassword = cardPassword;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreateOperatorId() {
        return createOperatorId;
    }

    public void setCreateOperatorId(Long createOperatorId) {
        this.createOperatorId = createOperatorId;
    }

    public String getCreateOperatorName() {
        return createOperatorName;
    }

    public void setCreateOperatorName(String createOperatorName) {
        this.createOperatorName = createOperatorName;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUseTime() {
        return useTime;
    }

    public void setUseTime(LocalDateTime useTime) {
        this.useTime = useTime;
    }

    public Long getUseOperatorId() {
        return useOperatorId;
    }

    public void setUseOperatorId(Long useOperatorId) {
        this.useOperatorId = useOperatorId;
    }

    public String getUseOperatorName() {
        return useOperatorName;
    }

    public void setUseOperatorName(String useOperatorName) {
        this.useOperatorName = useOperatorName;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}
