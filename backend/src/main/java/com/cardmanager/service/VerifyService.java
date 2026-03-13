package com.cardmanager.service;

import com.cardmanager.common.PageResult;
import com.cardmanager.dto.CardVerifyDTO;
import com.cardmanager.dto.BatchVerifyResultDTO;
import com.cardmanager.dto.VerifyQueryDTO;
import com.cardmanager.vo.CardVO;
import com.cardmanager.vo.PublicCardVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 核销服务接口
 */
public interface VerifyService {

    /**
     * 核销卡密
     */
    void verifyCard(CardVerifyDTO dto);

    /**
     * 批量核销卡密
     */
    BatchVerifyResultDTO batchVerifyCard(MultipartFile file) throws IOException;

    /**
     * 下载批量核销模板
     */
    void downloadTemplate(HttpServletResponse response) throws IOException;

    /**
     * 导出批量核销失败记录
     */
    void exportFailRecords(BatchVerifyResultDTO result, HttpServletResponse response) throws IOException;

    /**
     * 查询核销记录
     */
    PageResult<CardVO> listVerifyHistory(VerifyQueryDTO dto);

    /**
     * 导出核销记录
     */
    void exportVerifyHistory(VerifyQueryDTO dto, HttpServletResponse response);

    /**
     * 公开查询卡密状态
     */
    PublicCardVO queryCardStatus(String cardNumber, String cardPassword);
}
