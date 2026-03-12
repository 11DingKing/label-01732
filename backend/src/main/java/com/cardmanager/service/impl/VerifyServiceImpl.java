package com.cardmanager.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cardmanager.common.Constants;
import com.cardmanager.common.PageResult;
import com.cardmanager.dto.CardBatchVerifyExcelDTO;
import com.cardmanager.dto.CardVerifyDTO;
import com.cardmanager.dto.VerifyQueryDTO;
import com.cardmanager.entity.CardInfo;
import com.cardmanager.exception.BusinessException;
import com.cardmanager.mapper.CardBatchMapper;
import com.cardmanager.mapper.CardInfoMapper;
import com.cardmanager.security.UserContext;
import com.cardmanager.service.VerifyService;
import com.cardmanager.util.BusinessLogger;
import com.cardmanager.vo.CardBatchVerifyResultVO;
import com.cardmanager.vo.CardVO;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    /**
     * Excel读取监听器
     */
    private static class BatchVerifyExcelListener extends AnalysisEventListener<CardBatchVerifyExcelDTO> {
        private final List<CardBatchVerifyExcelDTO> dataList = new ArrayList<>();

        @Override
        public void invoke(CardBatchVerifyExcelDTO data, AnalysisContext context) {
            dataList.add(data);
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            // 所有数据解析完成
        }

        public List<CardBatchVerifyExcelDTO> getDataList() {
            return dataList;
        }
    }

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
    @Transactional(rollbackFor = Exception.class)
    public CardBatchVerifyResultVO batchVerifyCard(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 读取Excel文件
        List<CardBatchVerifyExcelDTO> excelData;
        try {
            BatchVerifyExcelListener listener = new BatchVerifyExcelListener();
            EasyExcel.read(file.getInputStream(), CardBatchVerifyExcelDTO.class, listener).sheet().doRead();
            excelData = listener.getDataList();
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException("读取Excel文件失败，请检查文件格式");
        }

        if (excelData.isEmpty()) {
            throw new BusinessException("Excel文件中没有数据");
        }

        // 获取当前用户信息
        Long operatorId = UserContext.getUserId();
        String operatorName = UserContext.getRealName();

        int totalCount = excelData.size();
        int successCount = 0;
        List<CardBatchVerifyResultVO.FailDetail> failDetails = new ArrayList<>();

        // 逐条处理核销
        for (int i = 0; i < excelData.size(); i++) {
            CardBatchVerifyExcelDTO item = excelData.get(i);
            int rowIndex = i + 2; // Excel行号从1开始，加上表头行

            try {
                // 验证数据格式
                if (StrUtil.isBlank(item.getCardNumber())) {
                    throw new BusinessException("卡号不能为空");
                }
                if (StrUtil.isBlank(item.getCardPassword())) {
                    throw new BusinessException("密码不能为空");
                }
                if (!item.getCardNumber().matches("^\\d{9}$")) {
                    throw new BusinessException("卡号格式错误，必须为9位数字");
                }
                if (!item.getCardPassword().matches("^[A-Z0-9]{6}$")) {
                    throw new BusinessException("密码格式错误，必须为6位字母数字");
                }

                // 查询卡密
                CardInfo card = cardInfoMapper.selectByCardNumber(item.getCardNumber());
                if (card == null) {
                    throw new BusinessException("卡密不存在");
                }

                // 验证密码
                if (!card.getCardPassword().equals(item.getCardPassword())) {
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

                // 记录业务日志
                BusinessLogger.logCardVerify(item.getCardNumber(), card.getBatchNumber(), operatorId, operatorName);
                successCount++;
                log.info("批量核销成功: cardNumber={}, operator={}", item.getCardNumber(), operatorName);

            } catch (BusinessException e) {
                failDetails.add(CardBatchVerifyResultVO.FailDetail.builder()
                        .rowIndex(rowIndex)
                        .cardNumber(item.getCardNumber())
                        .reason(e.getMessage())
                        .build());
                log.warn("批量核销失败: row={}, cardNumber={}, reason={}", rowIndex, item.getCardNumber(), e.getMessage());
            } catch (Exception e) {
                failDetails.add(CardBatchVerifyResultVO.FailDetail.builder()
                        .rowIndex(rowIndex)
                        .cardNumber(item.getCardNumber())
                        .reason("系统错误")
                        .build());
                log.error("批量核销系统错误: row={}, cardNumber={}", rowIndex, item.getCardNumber(), e);
            }
        }

        // 记录批量核销日志
        BusinessLogger.logBatchVerify(totalCount, successCount, failDetails.size(), operatorId, operatorName);
        log.info("批量核销完成: total={}, success={}, fail={}", totalCount, successCount, failDetails.size());

        return CardBatchVerifyResultVO.builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failCount(failDetails.size())
                .failDetails(failDetails)
                .build();
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
