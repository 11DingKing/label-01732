package com.cardmanager.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cardmanager.common.Constants;
import com.cardmanager.common.PageResult;
import com.cardmanager.dto.CardVerifyDTO;
import com.cardmanager.dto.VerifyQueryDTO;
import com.cardmanager.entity.CardInfo;
import com.cardmanager.exception.BusinessException;
import com.cardmanager.mapper.CardBatchMapper;
import com.cardmanager.mapper.CardInfoMapper;
import com.cardmanager.security.UserContext;
import com.cardmanager.service.VerifyService;
import com.cardmanager.util.BusinessLogger;
import com.cardmanager.vo.CardVO;
import com.cardmanager.vo.PublicCardVO;
import com.cardmanager.vo.VerifyHistoryExportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 核销服务实现
 */
@Slf4j
@Service
public class VerifyServiceImpl implements VerifyService {

    @Autowired
    private CardInfoMapper cardInfoMapper;

    @Autowired
    private CardBatchMapper cardBatchMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyCard(CardVerifyDTO dto) {
        // 查询卡密
        CardInfo card = cardInfoMapper.selectByCardNumber(dto.getCardNumber());
        if (card == null) {
            throw new BusinessException("卡密不存在");
        }

        // 验证密码
        if (!card.getCardPassword().equals(dto.getCardPassword())) {
            throw new BusinessException("卡号或密码错误");
        }

        // 检查状态
        if (card.getStatus() == Constants.CardStatus.USED) {
            throw new BusinessException("卡密已被核销");
        }
        if (card.getStatus() == Constants.CardStatus.RECYCLED) {
            throw new BusinessException("卡密已被回收");
        }

        // 获取当前用户信息
        Long operatorId = UserContext.getUserId();
        String operatorName = UserContext.getRealName();

        // 更新卡密状态
        card.setStatus(Constants.CardStatus.USED);
        card.setUseTime(LocalDateTime.now());
        card.setUseOperatorId(operatorId);
        card.setUseOperatorName(operatorName);
        cardInfoMapper.updateById(card);

        // 更新批次已核销数量
        cardBatchMapper.incrementUsedCount(card.getBatchNumber());

        // 记录业务日志
        BusinessLogger.logCardVerify(dto.getCardNumber(), card.getBatchNumber(), operatorId, operatorName);
        log.info("卡密核销成功: cardNumber={}, operator={}", dto.getCardNumber(), operatorName);
    }

    @Override
    public PageResult<CardVO> listVerifyHistory(VerifyQueryDTO dto) {
        Page<CardInfo> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        LambdaQueryWrapper<CardInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CardInfo::getStatus, Constants.CardStatus.USED)
               .eq(StrUtil.isNotBlank(dto.getCardNumber()), CardInfo::getCardNumber, dto.getCardNumber())
               .eq(StrUtil.isNotBlank(dto.getBatchNumber()), CardInfo::getBatchNumber, dto.getBatchNumber());

        // 核销日期范围查询
        if (StrUtil.isNotBlank(dto.getStartDate())) {
            LocalDateTime startTime = DateUtil.parseLocalDateTime(dto.getStartDate() + " 00:00:00");
            wrapper.ge(CardInfo::getUseTime, startTime);
        }
        if (StrUtil.isNotBlank(dto.getEndDate())) {
            LocalDateTime endTime = DateUtil.parseLocalDateTime(dto.getEndDate() + " 23:59:59");
            wrapper.le(CardInfo::getUseTime, endTime);
        }

        wrapper.orderByDesc(CardInfo::getUseTime);

        Page<CardInfo> result = cardInfoMapper.selectPage(page, wrapper);

        List<CardVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), voList, result.getCurrent(), result.getSize());
    }

    @Override
    public void exportVerifyHistory(VerifyQueryDTO dto, HttpServletResponse response) {
        // 构建查询条件（与listVerifyHistory相同）
        LambdaQueryWrapper<CardInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CardInfo::getStatus, Constants.CardStatus.USED)
               .eq(StrUtil.isNotBlank(dto.getCardNumber()), CardInfo::getCardNumber, dto.getCardNumber())
               .eq(StrUtil.isNotBlank(dto.getBatchNumber()), CardInfo::getBatchNumber, dto.getBatchNumber());

        // 核销日期范围查询
        if (StrUtil.isNotBlank(dto.getStartDate())) {
            LocalDateTime startTime = DateUtil.parseLocalDateTime(dto.getStartDate() + " 00:00:00");
            wrapper.ge(CardInfo::getUseTime, startTime);
        }
        if (StrUtil.isNotBlank(dto.getEndDate())) {
            LocalDateTime endTime = DateUtil.parseLocalDateTime(dto.getEndDate() + " 23:59:59");
            wrapper.le(CardInfo::getUseTime, endTime);
        }

        wrapper.orderByDesc(CardInfo::getUseTime);

        // 查询所有核销记录（不分页）
        List<CardInfo> records = cardInfoMapper.selectList(wrapper);

        // 转换为导出VO
        List<VerifyHistoryExportVO> exportList = records.stream()
                .map(this::convertToExportVO)
                .collect(Collectors.toList());

        // 导出Excel
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("核销记录_" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss"), StandardCharsets.UTF_8.name());
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), VerifyHistoryExportVO.class)
                    .sheet("核销记录")
                    .doWrite(exportList);

            // 记录业务日志
            BusinessLogger.logExport("核销记录", exportList.size(), UserContext.getUserId(), UserContext.getRealName());
            log.info("导出核销记录成功: count={}", exportList.size());
        } catch (IOException e) {
            log.error("导出核销记录失败", e);
            BusinessLogger.logError("核销管理", "导出", e.getMessage(), e);
            throw new BusinessException("导出失败");
        }
    }

    @Override
    public PublicCardVO queryCardStatus(String cardNumber, String cardPassword) {
        // 查询卡密
        CardInfo card = cardInfoMapper.selectByCardNumber(cardNumber);
        if (card == null) {
            log.warn("公开查询失败: 卡密不存在, cardNumber={}", cardNumber);
            throw new BusinessException("卡密不存在");
        }

        // 验证密码
        if (!card.getCardPassword().equals(cardPassword)) {
            log.warn("公开查询失败: 密码错误, cardNumber={}", cardPassword);
            throw new BusinessException("卡号或密码错误");
        }

        // 构建响应
        PublicCardVO vo = new PublicCardVO();
        vo.setCardNumber(card.getCardNumber());
        vo.setStatusName(Constants.CardStatus.getStatusName(card.getStatus()));
        vo.setCreateTime(card.getCreateTime());
        vo.setUseTime(card.getUseTime());

        log.debug("公开查询成功: cardNumber={}, status={}", cardNumber, vo.getStatusName());
        return vo;
    }

    /**
     * 转换为 CardVO
     */
    private CardVO convertToVO(CardInfo entity) {
        CardVO vo = new CardVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setStatusName(Constants.CardStatus.getStatusName(entity.getStatus()));
        return vo;
    }

    /**
     * 转换为导出VO
     */
    private VerifyHistoryExportVO convertToExportVO(CardInfo entity) {
        VerifyHistoryExportVO vo = new VerifyHistoryExportVO();
        vo.setCardNumber(entity.getCardNumber());
        vo.setCardPassword(entity.getCardPassword());
        vo.setBatchNumber(entity.getBatchNumber());
        vo.setUseTime(entity.getUseTime());
        vo.setUseOperatorName(entity.getUseOperatorName());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}
