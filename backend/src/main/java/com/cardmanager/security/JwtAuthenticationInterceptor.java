package com.cardmanager.security;

import com.cardmanager.entity.SysUser;
import com.cardmanager.exception.BusinessException;
import com.cardmanager.mapper.SysUserMapper;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 认证拦截器
 */
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行OPTIONS请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 获取 Token
        String authHeader = request.getHeader(header);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(prefix)) {
            throw new BusinessException(401, "请先登录");
        }

        String token = authHeader.substring(prefix.length());

        try {
            // 验证 Token
            if (!jwtUtil.validateToken(token)) {
                throw new BusinessException(401, "登录已过期，请重新登录");
            }

            // 获取用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            // 验证用户是否存在且有效
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null || user.getIsDeleted() == 1) {
                throw new BusinessException(401, "用户不存在");
            }
            if (user.getStatus() == 1) {
                throw new BusinessException(401, "用户已被禁用");
            }

            // 设置用户上下文
            UserContext.setUser(new UserContext.UserInfo(userId, username, user.getRealName(), role));

            return true;
        } catch (ExpiredJwtException e) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token验证失败", e);
            throw new BusinessException(401, "认证失败");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清除用户上下文
        UserContext.clear();
    }
}
