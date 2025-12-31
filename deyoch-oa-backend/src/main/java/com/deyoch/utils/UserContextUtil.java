package com.deyoch.utils;

import com.deyoch.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 用户上下文工具类
 * 提供获取当前用户信息的静态方法，支持从SecurityContext或JWT Token中获取
 * 统一处理用户信息获取逻辑，避免重复代码
 * 
 * @author deyoch-oa
 */
public class UserContextUtil {

    /**
     * 从SecurityContext中获取当前登录用户的用户名
     * @return 当前登录用户的用户名，未登录返回null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        return null;
    }
    
    /**
     * 从当前请求中获取JWT token
     * @return 当前请求的JWT token，不存在返回null
     */
    public static String getCurrentToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        
        HttpServletRequest request = attributes.getRequest();
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    /**
     * 从JWT token中获取用户名
     * @param jwtUtil JWT工具类实例
     * @return 用户名，无效token返回null
     */
    public static String getUsernameFromToken(JwtUtil jwtUtil) {
        String token = getCurrentToken();
        if (StringUtils.hasText(token)) {
            try {
                return jwtUtil.getUsernameFromToken(token);
            } catch (Exception e) {
                // 忽略解析错误，返回null
                return null;
            }
        }
        return null;
    }
    
    /**
     * 从JWT token中获取用户ID
     * @param jwtUtil JWT工具类实例
     * @return 用户ID，无效token返回null
     */
    public static Long getUserIdFromToken(JwtUtil jwtUtil) {
        String token = getCurrentToken();
        if (StringUtils.hasText(token)) {
            try {
                return jwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                // 忽略解析错误，返回null
                return null;
            }
        }
        return null;
    }
    
    /**
     * 获取当前登录用户的信息，优先从JWT token获取，其次从SecurityContext获取
     * @param jwtUtil JWT工具类实例
     * @return 用户信息对象，包含用户名和用户ID
     */
    public static UserInfo getCurrentUserInfo(JwtUtil jwtUtil) {
        // 优先从JWT token获取
        String token = getCurrentToken();
        if (StringUtils.hasText(token)) {
            try {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);
                if (userId != null && StringUtils.hasText(username)) {
                    return new UserInfo(userId, username);
                }
            } catch (Exception e) {
                // 忽略解析错误，尝试从SecurityContext获取
            }
        }
        
        // 从SecurityContext获取
        String username = getCurrentUsername();
        if (StringUtils.hasText(username)) {
            return new UserInfo(null, username);
        }
        
        return null;
    }
    
    /**
     * 用户信息内部类
     * 用于封装用户的基本信息
     */
    public static class UserInfo {
        private Long userId;
        private String username;
        
        public UserInfo(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
    }
}
