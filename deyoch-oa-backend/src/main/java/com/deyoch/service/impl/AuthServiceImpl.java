package com.deyoch.service.impl;

import com.deyoch.dto.LoginRequest;
import com.deyoch.dto.LoginResponse;
import com.deyoch.entity.DeyochUser;
import com.deyoch.service.AuthService;
import com.deyoch.service.DeyochUserService;
import com.deyoch.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类
 * 实现用户认证相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final DeyochUserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求参数
     * @return 登录结果（成功返回Token信息，失败返回null）
     */
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 1. 验证用户名
        DeyochUser user = userService.findByUsername(loginRequest.getUsername());
        if (user == null) {
            log.warn("登录失败：用户不存在 - {}", loginRequest.getUsername());
            return null;
        }

        // 2. 验证密码
        if (!userService.verifyPassword(loginRequest.getUsername(), loginRequest.getPassword())) {
            log.warn("登录失败：密码错误 - {}", loginRequest.getUsername());
            return null;
        }

        // 3. 检查用户状态
        if (user.getStatus() != 1L) {
            log.warn("登录失败：用户已禁用 - {}", loginRequest.getUsername());
            return null;
        }

        // 4. 生成Token
        String[] tokens = jwtUtil.generateTokenPair(user.getId(), user.getUsername(), user.getRoleId());

        // 5. 构建响应
        return buildLoginResponse(user, tokens);
    }

    /**
     * 用户注册
     * 
     * @param loginRequest 注册请求参数
     * @return 注册成功返回true，用户名已存在返回false
     */
    @Override
    public boolean register(LoginRequest loginRequest) {
        // 1. 检查用户名是否已存在
        if (userService.existsByUsername(loginRequest.getUsername())) {
            log.warn("注册失败：用户名已存在 - {}", loginRequest.getUsername());
            return false;
        }

        // 2. 创建用户
        DeyochUser user = new DeyochUser();
        user.setUsername(loginRequest.getUsername());
        user.setPassword(loginRequest.getPassword());
        user.setNickname(loginRequest.getUsername());

        boolean success = userService.createUser(user, loginRequest.getPassword());
        if (success) {
            log.info("注册成功 - {}", loginRequest.getUsername());
        }
        return success;
    }

    /**
     * 刷新Token
     * 
     * @param refreshToken 刷新Token
     * @return 新的Token对，失败返回null
     */
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // 1. 验证Refresh Token
        if (!jwtUtil.validateToken(refreshToken)) {
            log.warn("Token刷新失败：Refresh Token无效或已过期");
            return null;
        }

        // 2. 验证Token类型
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            log.warn("Token刷新失败：无效的Token类型");
            return null;
        }

        // 3. 获取用户信息
        Long userId = jwtUtil.getUserId(refreshToken);
        String username = jwtUtil.getUsername(refreshToken);
        if (userId == null || username == null) {
            log.warn("Token刷新失败：Token信息无效");
            return null;
        }

        // 4. 查询用户
        DeyochUser user = userService.findByIdWithDetails(userId);
        if (user == null) {
            log.warn("Token刷新失败：用户不存在 - userId={}", userId);
            return null;
        }

        // 5. 生成新的Token
        String[] tokens = jwtUtil.generateTokenPair(user.getId(), user.getUsername(), user.getRoleId());
        log.info("Token刷新成功 - {}", username);

        return buildLoginResponse(user, tokens);
    }

    /**
     * 构建登录响应
     * 
     * @param user 用户信息
     * @param tokens Token数组 [accessToken, refreshToken]
     * @return 登录响应对象
     */
    private LoginResponse buildLoginResponse(DeyochUser user, String[] tokens) {
        LoginResponse response = new LoginResponse();
        response.setAccessToken(tokens[0]);
        response.setRefreshToken(tokens[1]);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtUtil.getAccessTokenExpireSeconds());
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setRoleCode(String.valueOf(user.getRoleId()));
        response.setDeptId(user.getDeptId());
        return response;
    }
}