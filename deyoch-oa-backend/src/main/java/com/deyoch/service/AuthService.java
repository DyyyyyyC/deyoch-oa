package com.deyoch.service;

import com.deyoch.dto.LoginRequestDto;
import com.deyoch.dto.LoginResponseDto;
import com.deyoch.result.Result;

/**
 * 认证服务接口
 * 提供用户登录、注销等认证相关功能
 */
public interface AuthService {

    /**
     * 用户登录
     * @param loginRequestDto 登录请求参数
     * @return 登录结果，包含Token和用户信息
     */
    Result<LoginResponseDto> login(LoginRequestDto loginRequestDto);

}
