package com.cardmanager.vo;

import java.io.Serializable;

/**
 * 仪表盘数据VO
 */
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

    public Long getTotalCards() {
        return totalCards;
    }

    public void setTotalCards(Long totalCards) {
        this.totalCards = totalCards;
    }

    public Long getUnusedCards() {
        return unusedCards;
    }

    public void setUnusedCards(Long unusedCards) {
        this.unusedCards = unusedCards;
    }

    public Long getUsedCards() {
        return usedCards;
    }

    public void setUsedCards(Long usedCards) {
        this.usedCards = usedCards;
    }

    public Long getRecycledCards() {
        return recycledCards;
    }

    public void setRecycledCards(Long recycledCards) {
        this.recycledCards = recycledCards;
    }

    public Long getTodayGeneratedCards() {
        return todayGeneratedCards;
    }

    public void setTodayGeneratedCards(Long todayGeneratedCards) {
        this.todayGeneratedCards = todayGeneratedCards;
    }

    public Long getTodayUsedCards() {
        return todayUsedCards;
    }

    public void setTodayUsedCards(Long todayUsedCards) {
        this.todayUsedCards = todayUsedCards;
    }

    public Long getTotalBatches() {
        return totalBatches;
    }

    public void setTotalBatches(Long totalBatches) {
        this.totalBatches = totalBatches;
    }

    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }
}
