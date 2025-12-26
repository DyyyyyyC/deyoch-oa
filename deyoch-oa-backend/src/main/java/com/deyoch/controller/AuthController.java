package com.deyoch.controller;

import com.deyoch.dto.LoginRequestDto;
import com.deyoch.dto.LoginResponseDto;
import com.deyoch.result.Result;
import com.deyoch.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "认证相关接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录接口
     * @param request 登录请求参数
     * @return 登录结果，包含Token和用户信息
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口，返回Token和用户信息")
    public Result<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

}
