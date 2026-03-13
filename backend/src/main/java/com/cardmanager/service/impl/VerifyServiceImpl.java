package com.cardmanager.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.cardmanager.listener.CardVerifyExcelListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cardmanager.common.Constants;
import com.cardmanager.common.PageResult;
import com.cardmanager.dto.CardVerifyDTO;
import com.cardmanager.dto.BatchVerifyResultDTO;
import com.cardmanager.dto.VerifyQueryDTO;
import com.cardmanager.entity.CardInfo;
import com.cardmanager.exception.BusinessException;
import com.cardmanager.mapper.CardBatchMapper;
import com.cardmanager.mapper.CardInfoMapper;
import com.cardmanager.security.UserContext;
import com.cardmanager.service.VerifyService;
import com.cardmanager.util.BusinessLogger;
import com.cardmanager.vo.BatchVerifyFailExportVO;
import com.cardmanager.vo.CardVO;
import com.cardmanager.vo.CardVerifyExcelVO;
import com.cardmanager.vo.PublicCardVO;
import com.cardmanager.vo.VerifyHistoryExportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchVerifyResultDTO batchVerifyCard(MultipartFile file) throws IOException {
        // 使用自定义监听器读取Excel文件（包含基础校验）
        CardVerifyExcelListener listener = new CardVerifyExcelListener();
        EasyExcel.read(file.getInputStream(), CardVerifyExcelVO.class, listener).sheet().doRead();
        
        List<CardVerifyExcelVO> validList = listener.getValidList();
        List<CardVerifyExcelListener.ErrorRecord> formatErrors = listener.getErrorRecords();
        
        int totalCount = validList.size() + formatErrors.size();
        if (totalCount == 0) {
            throw new BusinessException("Excel文件中没有有效的卡密数据");
        }

        List<String> successCards = new ArrayList<>();
        List<BatchVerifyResultDTO.FailRecord> failRecords = new ArrayList<>();
        Set<String> processedCardNumbers = new HashSet<>();

        Long operatorId = UserContext.getUserId();
        String operatorName = UserContext.getRealName();

        // 添加格式错误到失败记录
        for (CardVerifyExcelListener.ErrorRecord error : formatErrors) {
            failRecords.add(new BatchVerifyResultDTO.FailRecord(
                    error.getRowNum(), error.getCardNumber(), error.getErrorMessage()));
        }

        // 处理有效数据
        for (CardVerifyExcelVO cardVO : validList) {
            int rowNum = cardVO.getRowNum();
            String cardNumber = cardVO.getCardNumber().trim();

            // 检查重复卡号
            if (processedCardNumbers.contains(cardNumber)) {
                failRecords.add(new BatchVerifyResultDTO.FailRecord(rowNum, cardNumber, "Excel中存在重复卡号"));
                continue;
            }
            processedCardNumbers.add(cardNumber);

            // 执行核销逻辑
            try {
                verifySingleCard(cardVO, operatorId, operatorName);
                successCards.add(cardNumber);
                log.info("批量核销成功: rowNum={}, cardNumber={}", rowNum, cardNumber);
            } catch (BusinessException e) {
                failRecords.add(new BatchVerifyResultDTO.FailRecord(rowNum, cardNumber, e.getMessage()));
                log.warn("批量核销失败: rowNum={}, cardNumber={}, error={}", rowNum, cardNumber, e.getMessage());
            } catch (Exception e) {
                failRecords.add(new BatchVerifyResultDTO.FailRecord(rowNum, cardNumber, "系统错误：" + e.getMessage()));
                log.error("批量核销系统错误: rowNum={}, cardNumber={}", rowNum, cardNumber, e);
            }
        }

        BatchVerifyResultDTO result = BatchVerifyResultDTO.builder()
                .totalCount(totalCount)
                .successCount(successCards.size())
                .failCount(failRecords.size())
                .successCards(successCards)
                .failRecords(failRecords)
                .build();

        // 记录业务日志
        BusinessLogger.logBatchCardVerify(successCards.size(), failRecords.size(), operatorId, operatorName);
        log.info("批量核销完成: total={}, success={}, fail={}", result.getTotalCount(), result.getSuccessCount(), result.getFailCount());

        return result;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("批量核销模板", StandardCharsets.UTF_8.name());
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        // 创建模板数据
        List<CardVerifyExcelVO> templateData = new ArrayList<>();
        CardVerifyExcelVO example = new CardVerifyExcelVO();
        example.setCardNumber("123456789");
        example.setCardPassword("ABC123");
        templateData.add(example);

        // 写入Excel
        EasyExcel.write(response.getOutputStream(), CardVerifyExcelVO.class)
                .sheet("核销模板")
                .doWrite(templateData);
    }

    @Override
    public void exportFailRecords(BatchVerifyResultDTO result, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("核销失败记录_" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss"), StandardCharsets.UTF_8.name());
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        // 转换为导出VO
        List<BatchVerifyFailExportVO> exportList = result.getFailRecords().stream()
                .map(this::convertToFailExportVO)
                .collect(Collectors.toList());

        EasyExcel.write(response.getOutputStream(), BatchVerifyFailExportVO.class)
                .sheet("失败记录")
                .doWrite(exportList);
    }

    /**
     * 核销单张卡密
     */
    private void verifySingleCard(CardVerifyExcelVO cardVO, Long operatorId, String operatorName) {
        String cardNumber = cardVO.getCardNumber().trim();
        String cardPassword = cardVO.getCardPassword().trim();

        // 查询卡密
        CardInfo card = cardInfoMapper.selectByCardNumber(cardNumber);
        if (card == null) {
            throw new BusinessException("卡密不存在");
        }

        // 验证密码
        if (!card.getCardPassword().equals(cardPassword)) {
            throw new BusinessException("卡号或密码错误");
        }

        // 检查状态
        if (card.getStatus() == Constants.CardStatus.USED) {
            throw new BusinessException("卡密已被核销");
        }
        if (card.getStatus() == Constants.CardStatus.RECYCLED) {
            throw new BusinessException("卡密已被回收");
        }

        // 更新卡密状态
        card.setStatus(Constants.CardStatus.USED);
        card.setUseTime(LocalDateTime.now());
        card.setUseOperatorId(operatorId);
        card.setUseOperatorName(operatorName);
        cardInfoMapper.updateById(card);

        // 更新批次已核销数量
        cardBatchMapper.incrementUsedCount(card.getBatchNumber());

        BusinessLogger.logCardVerify(cardNumber, card.getBatchNumber(), operatorId, operatorName);
    }

    /**
     * 转换为失败记录导出VO
     */
    private BatchVerifyFailExportVO convertToFailExportVO(BatchVerifyResultDTO.FailRecord record) {
        BatchVerifyFailExportVO vo = new BatchVerifyFailExportVO();
        vo.setRowNum(record.getRowNum());
        vo.setCardNumber(record.getCardNumber());
        vo.setErrorMessage(record.getErrorMessage());
        return vo;
    }
}
