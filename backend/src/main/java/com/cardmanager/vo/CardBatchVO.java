package com.cardmanager.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 批次VO
 */
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(Integer usedCount) {
        this.usedCount = usedCount;
    }

    public Integer getRecycledCount() {
        return recycledCount;
    }

    public void setRecycledCount(Integer recycledCount) {
        this.recycledCount = recycledCount;
    }

    public Integer getUnusedCount() {
        return unusedCount;
    }

    public void setUnusedCount(Integer unusedCount) {
        this.unusedCount = unusedCount;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
