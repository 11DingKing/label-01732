package com.cardmanager.controller;

import com.cardmanager.annotation.OperationLog;
import com.cardmanager.common.Constants;
import com.cardmanager.common.PageResult;
import com.cardmanager.common.Result;
import com.cardmanager.dto.CardVerifyDTO;
import com.cardmanager.dto.CardVerifyBatchDTO;
import com.cardmanager.dto.VerifyQueryDTO;
import com.cardmanager.service.VerifyService;
import com.cardmanager.vo.CardVO;
import com.cardmanager.vo.CardVerifyBatchResultVO;
import com.alibaba.excel.EasyExcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 核销管理控制器
 */
@RestController
@RequestMapping("/api/verify")
public class VerifyController {

    private static final Logger log = LoggerFactory.getLogger(VerifyController.class);

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
     * 批量核销卡密（Excel导入）
     */
    @PostMapping("/batch")
    @OperationLog(module = Constants.LogModule.VERIFY, operation = Constants.LogOperation.VERIFY_BATCH)
    public Result<CardVerifyBatchResultVO> verifyCardBatch(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择上传文件");
        }

        try {
            // 读取Excel文件
            List<CardVerifyBatchDTO> list = EasyExcel.read(file.getInputStream())
                    .head(CardVerifyBatchDTO.class)
                    .sheet()
                    .doReadSync();

            if (list.isEmpty()) {
                return Result.error("Excel文件中没有数据");
            }

            // 设置行号（从第2行开始，因为第1行是表头）
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setRowNum(i + 2);
            }

            // 执行批量核销
            CardVerifyBatchResultVO result = verifyService.verifyCardBatch(list);
            return Result.success("批量核销完成", result);

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            return Result.error("读取Excel文件失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("批量核销失败", e);
            return Result.error("批量核销失败：" + e.getMessage());
        }
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
