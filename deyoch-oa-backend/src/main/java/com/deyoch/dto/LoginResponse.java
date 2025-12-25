package com.deyoch.dto;

import lombok.Data;

/**
 * 登录响应DTO
 * 用于返回登录成功后的用户信息
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Data
public class LoginResponse {
    
    /**
     * 访问Token
     */
    private String accessToken;
    
    /**
     * 刷新Token
     */
    private String refreshToken;
    
    /**
     * Token类型
     */
    private String tokenType = "Bearer";
    
    /**
     * Token过期时间（秒）
     */
    private Long expiresIn;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 角色编码
     */
    private String roleCode;
    
    /**
     * 部门ID
     */
    private Long deptId;
}