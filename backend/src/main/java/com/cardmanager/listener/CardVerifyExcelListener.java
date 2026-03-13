package com.cardmanager.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.cardmanager.vo.CardVerifyExcelVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 卡密核销Excel读取监听器
 */
@Slf4j
public class CardVerifyExcelListener extends AnalysisEventListener<CardVerifyExcelVO> {

    /**
     * 卡号正则（9位数字）
     */
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^\\d{9}$");

    /**
     * 密码正则（6位大写字母数字）
     */
    private static final Pattern CARD_PASSWORD_PATTERN = Pattern.compile("^[A-Z0-9]{6}$");

    /**
     * 最大导入数量
     */
    private static final int MAX_IMPORT_COUNT = 1000;

    private final List<CardVerifyExcelVO> validList = new ArrayList<>();
    private final List<ErrorRecord> errorRecords = new ArrayList<>();

    @Override
    public void invoke(CardVerifyExcelVO data, AnalysisContext context) {
        // 获取当前行号
        Integer rowNum = context.readRowHolder().getRowIndex() + 1;
        data.setRowNum(rowNum);

        // 跳过表头后的空行
        if (!StringUtils.hasText(data.getCardNumber()) && !StringUtils.hasText(data.getCardPassword())) {
            return;
        }

        // 基础校验
        String error = validateData(data);
        if (error != null) {
            errorRecords.add(new ErrorRecord(rowNum, data.getCardNumber(), error));
            return;
        }

        validList.add(data);

        // 限制最大导入数量
        if (validList.size() + errorRecords.size() > MAX_IMPORT_COUNT) {
            throw new RuntimeException("单次最多导入" + MAX_IMPORT_COUNT + "条数据");
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("Excel解析完成，有效数据: {}条，错误数据: {}条", validList.size(), errorRecords.size());
    }

    /**
     * 校验数据
     */
    private String validateData(CardVerifyExcelVO data) {
        // 卡号校验
        if (!StringUtils.hasText(data.getCardNumber())) {
            return "卡号不能为空";
        }
        if (!CARD_NUMBER_PATTERN.matcher(data.getCardNumber()).matches()) {
            return "卡号格式错误，必须为9位数字";
        }

        // 密码校验
        if (!StringUtils.hasText(data.getCardPassword())) {
            return "密码不能为空";
        }
        // 自动转大写
        data.setCardPassword(data.getCardPassword().toUpperCase());
        if (!CARD_PASSWORD_PATTERN.matcher(data.getCardPassword()).matches()) {
            return "密码格式错误，必须为6位字母或数字";
        }

        return null;
    }

    public List<CardVerifyExcelVO> getValidList() {
        return validList;
    }

    public List<ErrorRecord> getErrorRecords() {
        return errorRecords;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorRecord {
        private Integer rowNum;
        private String cardNumber;
        private String errorMessage;
    }
}
