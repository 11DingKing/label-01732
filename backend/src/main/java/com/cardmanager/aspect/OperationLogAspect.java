package com.cardmanager.aspect;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cardmanager.annotation.OperationLog;
import com.cardmanager.common.Constants;
import com.cardmanager.mapper.OperationLogMapper;
import com.cardmanager.security.UserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Pointcut("@annotation(com.cardmanager.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 执行方法
        Object result = null;
        String resultStr = "成功";
        try {
            result = point.proceed();
        } catch (Exception e) {
            resultStr = "失败: " + e.getMessage();
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            
            // 保存日志
            try {
                saveLog(point, resultStr, costTime);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }
        
        return result;
    }

    /**
     * 保存操作日志
     */
    private void saveLog(ProceedingJoinPoint point, String result, long costTime) {
        // 获取注解信息
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);
        
        if (annotation == null) {
            return;
        }

        // 创建日志实体
        com.cardmanager.entity.OperationLog operationLog = new com.cardmanager.entity.OperationLog();
        operationLog.setModule(annotation.module());
        operationLog.setOperation(annotation.operation());
        operationLog.setMethod(point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName());
        
        // 获取请求参数（限制长度，并脱敏敏感信息）
        Object[] args = point.getArgs();
        String params = "";
        String loginUsername = null;  // 用于登录场景
        if (args != null && args.length > 0) {
            try {
                params = JSONUtil.toJsonStr(args);
                
                // 如果是登录操作，提取 username 用于审计日志
                if (Constants.LogOperation.LOGIN.equals(annotation.operation())) {
                    loginUsername = extractUsernameFromArgs(args);
                }
                
                // 脱敏敏感信息（密码等）
                params = maskSensitiveData(params);
                if (params.length() > 2000) {
                    params = params.substring(0, 2000) + "...";
                }
            } catch (Exception e) {
                params = "参数序列化失败";
            }
        }
        operationLog.setParams(params);
        operationLog.setResult(result);
        
        // 获取当前用户信息
        Long userId = UserContext.getUserId();
        String realName = UserContext.getRealName();
        
        // 【登录失败审计日志】特殊处理：登录场景下用户可能还未认证
        // 此时从请求参数中提取 username 作为操作者标识
        if (Constants.LogOperation.LOGIN.equals(annotation.operation()) && realName == null) {
            realName = loginUsername != null ? loginUsername + "(登录尝试)" : "匿名用户";
        }
        
        operationLog.setOperatorId(userId);
        operationLog.setOperatorName(realName);
        
        // 获取IP地址
        String ip = getIpAddress();
        operationLog.setIp(ip);
        
        operationLog.setCostTime(costTime);
        operationLog.setCreateTime(LocalDateTime.now());
        
        // 保存日志
        operationLogMapper.insert(operationLog);
        
        log.info("操作日志: module={}, operation={}, operator={}, result={}, costTime={}ms", 
                annotation.module(), annotation.operation(), realName, result, costTime);
    }
    
    /**
     * 从参数中提取用户名（用于登录审计日志）
     */
    private String extractUsernameFromArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        try {
            for (Object arg : args) {
                if (arg == null) continue;
                String json = JSONUtil.toJsonStr(arg);
                JSONObject jsonObj = JSONUtil.parseObj(json);
                if (jsonObj.containsKey("username")) {
                    return jsonObj.getStr("username");
                }
            }
        } catch (Exception e) {
            log.debug("提取用户名失败", e);
        }
        return null;
    }

    /**
     * 获取IP地址
     */
    private String getIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return "unknown";
            }
            HttpServletRequest request = attributes.getRequest();
            
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
            
            // 多个代理的情况，取第一个IP
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            
            return ip;
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 脱敏敏感数据
     */
    private String maskSensitiveData(String params) {
        if (params == null || params.isEmpty()) {
            return params;
        }
        // 替换常见的敏感字段
        return params
            .replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"******\"")
            .replaceAll("\"cardPassword\"\\s*:\\s*\"[^\"]*\"", "\"cardPassword\":\"******\"")
            .replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"******\"");
    }
}
