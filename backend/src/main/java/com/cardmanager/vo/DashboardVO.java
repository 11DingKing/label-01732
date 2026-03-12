package com.cardmanager.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 仪表盘数据VO
 */
@Data
public class DashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总发卡数
     */
    private Long totalCards;

    /**
     * 未使用数量
     */
    private Long unusedCards;

    /**
     * 已核销数量
     */
    private Long usedCards;

    /**
     * 已回收数量
     */
    private Long recycledCards;

    /**
     * 今日发卡数
     */
    private Long todayGeneratedCards;

    /**
     * 今日核销数
     */
    private Long todayUsedCards;

    /**
     * 总批次数
     */
    private Long totalBatches;

    /**
     * 用户总数
     */
    private Long totalUsers;
}
