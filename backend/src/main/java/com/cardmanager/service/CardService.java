package com.cardmanager.service;

import com.cardmanager.common.PageResult;
import com.cardmanager.dto.CardGenerateDTO;
import com.cardmanager.dto.CardQueryDTO;
import com.cardmanager.dto.CardRecycleDTO;
import com.cardmanager.vo.CardBatchVO;
import com.cardmanager.vo.CardVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 卡密服务接口
 */
public interface CardService {

    /**
     * 一键发卡
     */
    String generateCards(CardGenerateDTO dto);

    /**
     * 查询卡密列表
     */
    PageResult<CardVO> listCards(CardQueryDTO dto);

    /**
     * 导出卡密
     */
    void exportCards(CardQueryDTO dto, HttpServletResponse response);

    /**
     * 按批次回收卡密
     */
    int recycleBatch(String batchNumber);

    /**
     * 单独回收卡密
     */
    void recycleSingle(String cardNumber);

    /**
     * 查询批次列表
     */
    PageResult<CardBatchVO> listBatches(Integer pageNum, Integer pageSize);

    /**
     * 获取所有批次号列表（下拉选择用）
     */
    List<String> listAllBatchNumbers();
}
