package com.cardmanager.controller;

import com.cardmanager.common.Result;
import com.cardmanager.service.VerifyService;
import com.cardmanager.vo.PublicCardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

/**
 * 公开查询控制器（无需登录）
 */
@Slf4j
@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private VerifyService verifyService;

    /**
     * 查询卡密状态
     */
    @GetMapping("/query")
    public Result<PublicCardVO> queryCardStatus(
            @RequestParam @NotBlank(message = "卡号不能为空") String cardNumber,
            @RequestParam @NotBlank(message = "密码不能为空") String cardPassword) {
        PublicCardVO vo = verifyService.queryCardStatus(cardNumber, cardPassword);
        return Result.success(vo);
    }
}
