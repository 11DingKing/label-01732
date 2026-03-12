package com.cardmanager.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器
 */
@RestController
public class HealthController {

    /**
     * 健康检查接口（无API前缀，避免被拦截器拦截）
     */
    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    /**
     * API健康检查接口
     */
    @GetMapping("/api/health")
    public String apiHealth() {
        return "OK";
    }
}
