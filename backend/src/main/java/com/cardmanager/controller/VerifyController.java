package com.cardmanager.controller;

import com.cardmanager.annotation.OperationLog;
import com.cardmanager.common.Constants;
import com.cardmanager.common.PageResult;
import com.cardmanager.common.Result;
import com.cardmanager.dto.CardVerifyDTO;
import com.cardmanager.dto.VerifyQueryDTO;
import com.cardmanager.service.VerifyService;
import com.cardmanager.vo.CardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 核销管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/verify")
public class VerifyController {

    @Autowired
    private VerifyService verifyService;

    /**
     * 核销卡密
     */
    @PostMapping("/use")
    @OperationLog(module = Constants.LogModule.VERIFY, operation = Constants.LogOperation.VERIFY)
    public Result<Void> verifyCard(@RequestBody @Validated CardVerifyDTO dto) {
        verifyService.verifyCard(dto);
        return Result.success("核销成功", null);
    }

    /**
     * 查询核销记录
     */
    @GetMapping("/history")
    public Result<PageResult<CardVO>> listVerifyHistory(@Validated VerifyQueryDTO dto) {
        PageResult<CardVO> result = verifyService.listVerifyHistory(dto);
        return Result.success(result);
    }

    /**
     * 导出核销记录
     */
    @GetMapping("/export")
    @OperationLog(module = Constants.LogModule.VERIFY, operation = Constants.LogOperation.EXPORT)
    public void exportVerifyHistory(@Validated VerifyQueryDTO dto, HttpServletResponse response) {
        verifyService.exportVerifyHistory(dto, response);
    }
}
