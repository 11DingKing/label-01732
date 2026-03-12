package com.cardmanager.config;

import com.cardmanager.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 频率限制拦截器
 * 用于防止公开接口被滥用
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Value("${rate-limit.public-query:10}")
    private int maxRequestsPerMinute;

    // 存储每个IP的请求计数和时间戳
    private final Map<String, RateLimitInfo> ipRequestMap = new ConcurrentHashMap<>();

    // 清理过期数据的间隔
    private static final long CLEANUP_INTERVAL_MS = 60000;
    private long lastCleanupTime = System.currentTimeMillis();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientIp = getClientIp(request);
        long currentTime = System.currentTimeMillis();

        // 定期清理过期数据
        if (currentTime - lastCleanupTime > CLEANUP_INTERVAL_MS) {
            cleanup(currentTime);
            lastCleanupTime = currentTime;
        }

        RateLimitInfo rateLimitInfo = ipRequestMap.computeIfAbsent(clientIp, 
            k -> new RateLimitInfo(currentTime));

        // 如果超过1分钟，重置计数
        if (currentTime - rateLimitInfo.getStartTime() > TimeUnit.MINUTES.toMillis(1)) {
            rateLimitInfo.reset(currentTime);
        }

        // 检查是否超过限制
        if (rateLimitInfo.getCount().incrementAndGet() > maxRequestsPerMinute) {
            log.warn("频率限制触发: IP={}, 当前请求数={}", clientIp, rateLimitInfo.getCount().get());
            throw new BusinessException(429, "请求过于频繁，请稍后再试");
        }

        return true;
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个代理IP的情况，取第一个非unknown的IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 清理过期的IP记录
     */
    private void cleanup(long currentTime) {
        ipRequestMap.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().getStartTime() > TimeUnit.MINUTES.toMillis(5));
    }

    /**
     * 频率限制信息
     */
    private static class RateLimitInfo {
        private volatile long startTime;
        private final AtomicInteger count;

        public RateLimitInfo(long startTime) {
            this.startTime = startTime;
            this.count = new AtomicInteger(0);
        }

        public long getStartTime() {
            return startTime;
        }

        public AtomicInteger getCount() {
            return count;
        }

        public void reset(long newStartTime) {
            this.startTime = newStartTime;
            this.count.set(0);
        }
    }
}
