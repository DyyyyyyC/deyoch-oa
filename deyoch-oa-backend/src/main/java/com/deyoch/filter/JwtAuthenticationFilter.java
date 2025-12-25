package com.deyoch.filter;

import com.deyoch.common.Result;
import com.deyoch.common.ResultCode;
import com.deyoch.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * 从请求头中提取JWT Token并验证，将用户信息放入SecurityContext
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /**
     * JWT Token请求头名称
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Token前缀
     */
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        // 1. 从请求头提取Token
        String token = extractToken(request);
        
        // 2. 如果没有Token，放行（让后续的Security判断是否需要认证）
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 3. 验证Token
        if (!jwtUtil.validateToken(token)) {
            writeUnauthorizedResponse(response, "Token无效或已过期");
            return;
        }
        
        // 4. 验证是否为Access Token
        if (!jwtUtil.isAccessToken(token)) {
            writeUnauthorizedResponse(response, "无效的Token类型");
            return;
        }
        
        // 5. 获取用户信息
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        Long roleId = jwtUtil.getRoleId(token);
        
        if (userId == null || username == null) {
            writeUnauthorizedResponse(response, "Token信息无效");
            return;
        }
        
        // 6. 创建Authentication对象并放入SecurityContext
        // 将userId作为principal，角色作为authority
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + (roleId != null ? roleId : 0));
        
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userId, null, Collections.singletonList(authority));
        
        // 将用户名也放入attributes，方便后续使用
        request.setAttribute("username", username);
        request.setAttribute("userId", userId);
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        log.debug("JWT认证成功: userId={}, username={}", userId, username);
        
        // 7. 放行请求
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取JWT Token
     * 
     * @param request HTTP请求
     * @return JWT Token，如果没有返回null
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 写入未授权响应
     * 
     * @param response HTTP响应
     * @param message 错误消息
     * @throws IOException IO异常
     */
    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        Result<String> errorResult = Result.error(ResultCode.UNAUTHORIZED, message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResult));
    }

    /**
     * 判断请求是否需要Token认证
     * 公开接口不需要认证
     * 
     * @param request HTTP请求
     * @return 需要认证返回true
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 公开接口不需要JWT认证
        return path.startsWith("/auth/") && !path.contains("/refresh");
    }
}