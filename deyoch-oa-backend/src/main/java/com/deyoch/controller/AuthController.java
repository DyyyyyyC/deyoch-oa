package com.deyoch.controller;

import com.deyoch.common.Result;
import com.deyoch.common.ResultCode;
import com.deyoch.dto.LoginRequest;
import com.deyoch.dto.LoginResponse;
import com.deyoch.entity.DeyochUser;
import com.deyoch.service.AuthService;
import com.deyoch.service.DeyochUserService;
import com.deyoch.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理用户登录、注册、Token刷新等认证相关接口
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final DeyochUserService userService;
    private final JwtUtil jwtUtil;
    
    /**
     * 用户登录接口
     * 
     * @param loginRequest 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        
        if (response != null) {
            return Result.success("登录成功", response);
        } else {
            return Result.error(ResultCode.USER_PASSWORD_ERROR, "用户名或密码错误");
        }
    }
    
    /**
     * 用户注册接口
     * 
     * @param loginRequest 注册请求参数
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody LoginRequest loginRequest) {
        boolean success = authService.register(loginRequest);
        
        if (success) {
            return Result.success("注册成功，请登录");
        } else {
            return Result.error(ResultCode.USER_EXISTS, "用户名已存在");
        }
    }
    
    /**
     * 获取当前用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/me")
    public Result<DeyochUser> getCurrentUser(@RequestParam Long userId) {
        DeyochUser user = userService.findByIdWithDetails(userId);
        
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        
        user.setPassword(null);
        return Result.success(user);
    }
    
    /**
     * 刷新Token接口
     * 
     * @param refreshToken 刷新Token
     * @return 新的Access Token
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        LoginResponse response = authService.refreshToken(refreshToken);
        
        if (response != null) {
            return Result.success("Token刷新成功", response);
        } else {
            return Result.error(ResultCode.UNAUTHORIZED, "Refresh Token无效或已过期");
        }
    }
    
    /**
     * 验证Token是否有效
     * 
     * @param token Access Token
     * @return 验证结果
     */
    @GetMapping("/validate")
    public Result<Boolean> validateToken(@RequestParam String token) {
        boolean valid = jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token);
        return Result.success(valid);
    }
    
    /**
     * 退出登录接口
     * 
     * @return 退出结果
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("退出成功");
    }
}