package com.cardmanager.controller;

import com.cardmanager.common.Result;
import com.cardmanager.service.StatisticsService;
import com.cardmanager.vo.DashboardVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private static final Logger log = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取仪表盘数据
     */
    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboardData() {
        DashboardVO vo = statisticsService.getDashboardData();
        return Result.success(vo);
    }
}
