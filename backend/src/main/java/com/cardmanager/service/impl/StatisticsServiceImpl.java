package com.cardmanager.service.impl;

import com.cardmanager.common.Constants;
import com.cardmanager.mapper.CardBatchMapper;
import com.cardmanager.mapper.CardInfoMapper;
import com.cardmanager.mapper.SysUserMapper;
import com.cardmanager.service.StatisticsService;
import com.cardmanager.vo.DashboardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 统计服务实现
 */
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private CardInfoMapper cardInfoMapper;

    @Autowired
    private CardBatchMapper cardBatchMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public DashboardVO getDashboardData() {
        DashboardVO vo = new DashboardVO();

        // 卡密统计
        vo.setTotalCards(cardInfoMapper.countTotal());
        vo.setUnusedCards(cardInfoMapper.countByStatus(Constants.CardStatus.UNUSED));
        vo.setUsedCards(cardInfoMapper.countByStatus(Constants.CardStatus.USED));
        vo.setRecycledCards(cardInfoMapper.countByStatus(Constants.CardStatus.RECYCLED));

        // 今日统计
        vo.setTodayGeneratedCards(cardInfoMapper.countTodayGenerated());
        vo.setTodayUsedCards(cardInfoMapper.countTodayUsed());

        // 其他统计
        vo.setTotalBatches(cardBatchMapper.countTotal());
        vo.setTotalUsers(sysUserMapper.countTotal());

        return vo;
    }
}
