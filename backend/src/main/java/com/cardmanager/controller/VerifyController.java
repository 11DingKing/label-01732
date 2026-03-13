package com.cardmanager.controller;

import com.cardmanager.annotation.OperationLog;
import com.cardmanager.common.Constants;
import com.cardmanager.common.PageResult;
import com.cardmanager.common.Result;
import com.cardmanager.dto.BatchVerifyResultDTO;
import com.cardmanager.dto.CardVerifyDTO;
import com.cardmanager.dto.VerifyQueryDTO;
import com.cardmanager.service.VerifyService;
import com.cardmanager.vo.BatchVerifyResultVO;
import com.cardmanager.vo.CardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * 批量核销卡密
     */
    @PostMapping("/batch")
    @OperationLog(module = Constants.LogModule.VERIFY, operation = Constants.LogOperation.VERIFY)
    public Result<BatchVerifyResultVO> batchVerifyCard(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.error("请选择上传的文件");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            return Result.error("请上传Excel文件");
        }
        BatchVerifyResultDTO resultDTO = verifyService.batchVerifyCard(file);
        BatchVerifyResultVO resultVO = convertToVO(resultDTO);
        return Result.success("批量核销完成", resultVO);
    }

    /**
     * 下载批量核销模板
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        verifyService.downloadTemplate(response);
    }

    /**
     * 导出失败记录
     */
    @PostMapping("/export-fail")
    @OperationLog(module = Constants.LogModule.VERIFY, operation = Constants.LogOperation.EXPORT)
    public void exportFailRecords(@RequestBody BatchVerifyResultVO resultVO, HttpServletResponse response) throws IOException {
        BatchVerifyResultDTO resultDTO = convertToDTO(resultVO);
        verifyService.exportFailRecords(resultDTO, response);
    }

    /**
     * 转换DTO到VO
     */
    private BatchVerifyResultVO convertToVO(BatchVerifyResultDTO dto) {
        BatchVerifyResultVO vo = new BatchVerifyResultVO();
        BeanUtils.copyProperties(dto, vo);
        if (dto.getFailRecords() != null) {
            List<BatchVerifyResultVO.FailRecordVO> failRecordVOS = dto.getFailRecords().stream()
                    .map(this::convertFailRecordToVO)
                    .collect(Collectors.toList());
            vo.setFailRecords(failRecordVOS);
        }
        return vo;
    }

    /**
     * 转换失败记录到VO
     */
    private BatchVerifyResultVO.FailRecordVO convertFailRecordToVO(BatchVerifyResultDTO.FailRecord record) {
        BatchVerifyResultVO.FailRecordVO vo = new BatchVerifyResultVO.FailRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    /**
     * 转换VO到DTO
     */
    private BatchVerifyResultDTO convertToDTO(BatchVerifyResultVO vo) {
        BatchVerifyResultDTO dto = new BatchVerifyResultDTO();
        BeanUtils.copyProperties(vo, dto);
        if (vo.getFailRecords() != null) {
            List<BatchVerifyResultDTO.FailRecord> failRecords = vo.getFailRecords().stream()
                    .map(this::convertFailRecordToDTO)
                    .collect(Collectors.toList());
            dto.setFailRecords(failRecords);
        }
        return dto;
    }

    /**
     * 转换失败记录到DTO
     */
    private BatchVerifyResultDTO.FailRecord convertFailRecordToDTO(BatchVerifyResultVO.FailRecordVO vo) {
        return new BatchVerifyResultDTO.FailRecord(vo.getRowNum(), vo.getCardNumber(), vo.getErrorMessage());
    }
}
