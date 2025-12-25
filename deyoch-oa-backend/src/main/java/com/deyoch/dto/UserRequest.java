package com.deyoch.dto;

import lombok.Data;

/**
 * 用户创建/更新请求DTO
 * 用于用户CRUD接口的请求参数
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Data
public class UserRequest {
    
    /**
     * 用户ID（更新时必传）
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 部门ID
     */
    private Long deptId;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Long status = 1L;
}