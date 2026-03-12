package com.cardmanager.service;

import com.cardmanager.vo.DashboardVO;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取仪表盘数据
     */
    DashboardVO getDashboardData();
}
