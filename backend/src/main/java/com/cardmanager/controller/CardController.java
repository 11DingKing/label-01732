package com.cardmanager.controller;

import com.cardmanager.annotation.OperationLog;
import com.cardmanager.common.Constants;
import com.cardmanager.common.PageResult;
import com.cardmanager.common.Result;
import com.cardmanager.dto.CardGenerateDTO;
import com.cardmanager.dto.CardQueryDTO;
import com.cardmanager.exception.BusinessException;
import com.cardmanager.security.UserContext;
import com.cardmanager.service.CardService;
import com.cardmanager.vo.CardBatchVO;
import com.cardmanager.vo.CardVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 卡密管理控制器
 */
@RestController
@RequestMapping("/api/card")
public class CardController {

    private static final Logger log = LoggerFactory.getLogger(CardController.class);

    @Autowired
    private CardService cardService;

    /**
     * 一键发卡
     */
    @PostMapping("/generate")
    @OperationLog(module = Constants.LogModule.CARD, operation = Constants.LogOperation.GENERATE)
    public Result<String> generateCards(@RequestBody @Validated CardGenerateDTO dto) {
        String batchNumber = cardService.generateCards(dto);
        return Result.success("发卡成功", batchNumber);
    }

    /**
     * 查询卡密列表
     */
    @GetMapping("/list")
    public Result<PageResult<CardVO>> listCards(@Validated CardQueryDTO dto) {
        PageResult<CardVO> result = cardService.listCards(dto);
        return Result.success(result);
    }

    /**
     * 导出卡密
     */
    @GetMapping("/export")
    @OperationLog(module = Constants.LogModule.CARD, operation = Constants.LogOperation.EXPORT)
    public void exportCards(@Validated CardQueryDTO dto, HttpServletResponse response) {
        cardService.exportCards(dto, response);
    }

    /**
     * 按批次回收卡密
     */
    @PutMapping("/recycle/batch")
    @OperationLog(module = Constants.LogModule.CARD, operation = Constants.LogOperation.RECYCLE)
    public Result<Integer> recycleBatch(@RequestParam String batchNumber) {
        // 权限检查：只有管理员可以回收
        if (!UserContext.isAdmin()) {
            throw new BusinessException(403, "无权限执行此操作");
        }
        int count = cardService.recycleBatch(batchNumber);
        return Result.success("回收成功，共回收 " + count + " 张卡密", count);
    }

    /**
     * 单独回收卡密
     */
    @PutMapping("/recycle/single")
    @OperationLog(module = Constants.LogModule.CARD, operation = Constants.LogOperation.RECYCLE)
    public Result<Void> recycleSingle(@RequestParam String cardNumber) {
        // 权限检查：只有管理员可以回收
        if (!UserContext.isAdmin()) {
            throw new BusinessException(403, "无权限执行此操作");
        }
        cardService.recycleSingle(cardNumber);
        return Result.success("回收成功", null);
    }

    /**
     * 查询批次列表
     */
    @GetMapping("/batch/list")
    public Result<PageResult<CardBatchVO>> listBatches(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<CardBatchVO> result = cardService.listBatches(pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取所有批次号列表
     */
    @GetMapping("/batch/numbers")
    public Result<List<String>> listBatchNumbers() {
        List<String> batchNumbers = cardService.listAllBatchNumbers();
        return Result.success(batchNumbers);
    }
}
