package com.deyoch.service;

import com.deyoch.dto.LoginRequest;
import com.deyoch.dto.LoginResponse;

/**
 * 认证服务接口
 * 提供用户认证相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
public interface AuthService {
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求参数
     * @return 登录结果（成功返回Token信息，失败返回null）
     */
    LoginResponse login(LoginRequest loginRequest);
    
    /**
     * 用户注册
     * 
     * @param loginRequest 注册请求参数
     * @return 注册成功返回true，用户名已存在返回false
     */
    boolean register(LoginRequest loginRequest);
    
    /**
     * 刷新Token
     * 
     * @param refreshToken 刷新Token
     * @return 新的Token对，失败返回null
     */
    LoginResponse refreshToken(String refreshToken);
}