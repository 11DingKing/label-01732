package com.cardmanager.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cardmanager.common.Constants;
import com.cardmanager.common.PageResult;
import com.cardmanager.dto.CardGenerateDTO;
import com.cardmanager.dto.CardQueryDTO;
import com.cardmanager.entity.CardBatch;
import com.cardmanager.entity.CardInfo;
import com.cardmanager.exception.BusinessException;
import com.cardmanager.mapper.CardBatchMapper;
import com.cardmanager.mapper.CardInfoMapper;
import com.cardmanager.security.UserContext;
import com.cardmanager.service.CardService;
import com.cardmanager.util.BusinessLogger;
import com.cardmanager.util.CardNumberGenerator;
import com.cardmanager.vo.CardBatchVO;
import com.cardmanager.vo.CardVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 卡密服务实现
 */
@Service
public class CardServiceImpl implements CardService {

    private static final Logger log = LoggerFactory.getLogger(CardServiceImpl.class);

    @Autowired
    private CardInfoMapper cardInfoMapper;

    @Autowired
    private CardBatchMapper cardBatchMapper;

    /**
     * 批量插入的批次大小（每批1000条，避免SQL过长）
     */
    private static final int BATCH_SIZE = 1000;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateCards(CardGenerateDTO dto) {
        int count = dto.getCount();
        
        // 生成批次号（严格遵循 YYYYMMDDHHmmss 格式，14位）
        String batchNumber = CardNumberGenerator.generateBatchNumber();
        
        // 获取当前用户信息
        Long operatorId = UserContext.getUserId();
        String operatorName = UserContext.getRealName();
        
        // 批量生成卡号（预生成）
        Set<String> preGeneratedNumbers = CardNumberGenerator.generateCardNumbers(count * 2);
        
        // 查询数据库中已存在的卡号
        Set<String> existingNumbers = new HashSet<>();
        if (!preGeneratedNumbers.isEmpty()) {
            existingNumbers = cardInfoMapper.selectExistingCardNumbers(preGeneratedNumbers);
        }
        
        // 过滤掉已存在的卡号
        Set<String> finalExistingNumbers = existingNumbers;
        List<String> availableNumbers = preGeneratedNumbers.stream()
                .filter(num -> !finalExistingNumbers.contains(num))
                .limit(count)
                .collect(Collectors.toList());
        
        // 如果可用卡号不足，继续生成
        int maxRetry = 100;
        int retry = 0;
        while (availableNumbers.size() < count && retry < maxRetry) {
            String newNumber = CardNumberGenerator.generateCardNumber();
            if (!cardInfoMapper.existsByCardNumber(newNumber) && !availableNumbers.contains(newNumber)) {
                availableNumbers.add(newNumber);
            }
            retry++;
        }
        
        if (availableNumbers.size() < count) {
            throw new BusinessException("卡号生成失败，可用卡号不足，请重试");
        }
        
        // 批量创建卡密
        List<CardInfo> cardList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < count; i++) {
            CardInfo card = new CardInfo();
            card.setCardNumber(availableNumbers.get(i));
            card.setCardPassword(CardNumberGenerator.generatePassword());
            card.setBatchNumber(batchNumber);
            card.setStatus(Constants.CardStatus.UNUSED);
            card.setCreateOperatorId(operatorId);
            card.setCreateOperatorName(operatorName);
            card.setCreateTime(now);
            card.setIsDeleted(0);
            cardList.add(card);
        }
        
        // 【性能优化】使用批量插入，分批处理避免SQL过长
        int totalInserted = 0;
        for (int i = 0; i < cardList.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, cardList.size());
            List<CardInfo> batchList = cardList.subList(i, endIndex);
            totalInserted += cardInfoMapper.batchInsert(batchList);
        }
        log.info("批量插入卡密完成: 预期={}, 实际={}", count, totalInserted);
        
        // 创建批次记录
        CardBatch batch = new CardBatch();
        batch.setBatchNumber(batchNumber);
        batch.setTotalCount(count);
        batch.setUsedCount(0);
        batch.setRecycledCount(0);
        batch.setOperatorId(operatorId);
        batch.setOperatorName(operatorName);
        batch.setCreateTime(now);
        batch.setIsDeleted(0);
        cardBatchMapper.insert(batch);
        
        // 记录业务日志
        BusinessLogger.logCardGenerate(batchNumber, count, operatorId, operatorName);
        log.info("发卡成功: batchNumber={}, count={}, operator={}", batchNumber, count, operatorName);
        return batchNumber;
    }

    @Override
    public PageResult<CardVO> listCards(CardQueryDTO dto) {
        Page<CardInfo> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        
        LambdaQueryWrapper<CardInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(dto.getBatchNumber()), CardInfo::getBatchNumber, dto.getBatchNumber())
               .eq(StrUtil.isNotBlank(dto.getCardNumber()), CardInfo::getCardNumber, dto.getCardNumber())
               .eq(dto.getStatus() != null, CardInfo::getStatus, dto.getStatus())
               .orderByDesc(CardInfo::getCreateTime);
        
        Page<CardInfo> result = cardInfoMapper.selectPage(page, wrapper);
        
        List<CardVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return PageResult.of(result.getTotal(), voList, result.getCurrent(), result.getSize());
    }

    @Override
    public void exportCards(CardQueryDTO dto, HttpServletResponse response) {
        // 查询数据（不分页）
        LambdaQueryWrapper<CardInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(dto.getBatchNumber()), CardInfo::getBatchNumber, dto.getBatchNumber())
               .eq(StrUtil.isNotBlank(dto.getCardNumber()), CardInfo::getCardNumber, dto.getCardNumber())
               .eq(dto.getStatus() != null, CardInfo::getStatus, dto.getStatus())
               .orderByDesc(CardInfo::getCreateTime);
        
        List<CardInfo> list = cardInfoMapper.selectList(wrapper);
        List<CardVO> voList = list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 导出 Excel
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("卡密列表_" + System.currentTimeMillis(), "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            EasyExcel.write(response.getOutputStream(), CardVO.class)
                    .sheet("卡密列表")
                    .doWrite(voList);
            
            // 记录业务日志
            BusinessLogger.logExport("卡密列表", voList.size(), UserContext.getUserId(), UserContext.getRealName());
            log.info("导出卡密成功: count={}", voList.size());
        } catch (IOException e) {
            log.error("导出卡密失败", e);
            BusinessLogger.logError("发卡管理", "导出", e.getMessage(), e);
            throw new BusinessException("导出失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int recycleBatch(String batchNumber) {
        // 检查批次是否存在
        CardBatch batch = cardBatchMapper.selectByBatchNumber(batchNumber);
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }
        
        // 批量更新卡密状态为已回收（只回收未使用的）
        // 注意：只修改状态，不设置 isDeleted，确保已回收的卡密仍可被查询
        // "回收"的含义是：状态变更为"已回收"，不可再被核销，但仍可在管理界面查询
        LambdaUpdateWrapper<CardInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CardInfo::getBatchNumber, batchNumber)
               .eq(CardInfo::getStatus, Constants.CardStatus.UNUSED)
               .eq(CardInfo::getIsDeleted, 0)
               .set(CardInfo::getStatus, Constants.CardStatus.RECYCLED);
        
        int count = cardInfoMapper.update(null, wrapper);
        
        // 更新批次的回收数量
        cardBatchMapper.updateRecycledCount(batchNumber);
        
        // 记录业务日志
        BusinessLogger.logBatchRecycle(batchNumber, count, UserContext.getUserId(), UserContext.getRealName());
        log.info("批次回收成功: batchNumber={}, count={}", batchNumber, count);
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recycleSingle(String cardNumber) {
        // 查询卡密
        CardInfo card = cardInfoMapper.selectByCardNumber(cardNumber);
        if (card == null) {
            throw new BusinessException("卡密不存在");
        }
        
        // 检查状态
        if (card.getStatus() == Constants.CardStatus.USED) {
            throw new BusinessException("已核销的卡密不能回收");
        }
        if (card.getStatus() == Constants.CardStatus.RECYCLED) {
            throw new BusinessException("卡密已被回收");
        }
        
        // 更新状态为已回收
        // 注意：只修改状态，不设置 isDeleted，确保已回收的卡密仍可被查询
        card.setStatus(Constants.CardStatus.RECYCLED);
        cardInfoMapper.updateById(card);
        
        // 更新批次的回收数量
        cardBatchMapper.updateRecycledCount(card.getBatchNumber());
        
        // 记录业务日志
        BusinessLogger.logCardRecycle(cardNumber, card.getBatchNumber(), UserContext.getUserId(), UserContext.getRealName());
        log.info("单张回收成功: cardNumber={}", cardNumber);
    }

    @Override
    public PageResult<CardBatchVO> listBatches(Integer pageNum, Integer pageSize) {
        Page<CardBatch> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<CardBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(CardBatch::getCreateTime);
        
        Page<CardBatch> result = cardBatchMapper.selectPage(page, wrapper);
        
        List<CardBatchVO> voList = result.getRecords().stream()
                .map(this::convertToBatchVO)
                .collect(Collectors.toList());
        
        return PageResult.of(result.getTotal(), voList, result.getCurrent(), result.getSize());
    }

    @Override
    public List<String> listAllBatchNumbers() {
        LambdaQueryWrapper<CardBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(CardBatch::getBatchNumber)
               .orderByDesc(CardBatch::getCreateTime);
        
        return cardBatchMapper.selectList(wrapper).stream()
                .map(CardBatch::getBatchNumber)
                .collect(Collectors.toList());
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
     * 转换为 CardBatchVO
     */
    private CardBatchVO convertToBatchVO(CardBatch entity) {
        CardBatchVO vo = new CardBatchVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setUnusedCount(entity.getTotalCount() - entity.getUsedCount() - entity.getRecycledCount());
        return vo;
    }
}
