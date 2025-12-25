package com.deyoch.dto;

import lombok.Data;

/**
 * 登录请求DTO
 * 用于用户登录接口的请求参数
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Data
public class LoginRequest {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 验证码（可选）
     */
    private String captcha;
    
    /**
     * 记住我
     */
    private Boolean rememberMe = false;
}