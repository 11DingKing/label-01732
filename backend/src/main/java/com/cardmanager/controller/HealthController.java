package com.cardmanager.controller;

import com.cardmanager.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("OK");
    }
}
