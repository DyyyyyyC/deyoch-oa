package com.deyoch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deyoch.dto.LoginRequestDto;
import com.deyoch.dto.LoginResponseDto;
import com.deyoch.entity.DeyochUser;
import com.deyoch.mapper.DeyochUserMapper;
import com.deyoch.result.Result;
import com.deyoch.result.ResultCode;
import com.deyoch.service.AuthService;
import com.deyoch.service.PermissionService;
import com.deyoch.utils.JwtUtil;
import java.util.List;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务实现类
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final DeyochUserMapper deyochUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;

    @Override
    public Result<LoginResponseDto> login(LoginRequestDto loginRequestDto) {
        // 1. 检查用户名和密码 - 使用LambdaQueryWrapper链式调用
        DeyochUser user = deyochUserMapper.selectOne(
                new LambdaQueryWrapper<DeyochUser>()
                        .eq(DeyochUser::getUsername, loginRequestDto.getUsername())
        );

        // 2. 验证用户是否存在
        if (user == null) {
            return Result.error(ResultCode.USER_PASSWORD_ERROR, "用户名或密码错误");
        }

        // 3. 验证密码
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            return Result.error(ResultCode.USER_PASSWORD_ERROR, "用户名或密码错误");
        }

        // 4. 检查用户状态
        if (user.getStatus() == 0) {
            return Result.error(ResultCode.USER_DISABLED, "用户已被禁用");
        }

        // 5. 生成Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        String token = jwtUtil.generateToken(claims);

        // 6. 获取用户权限信息
        Result<List<String>> permCodesResult = permissionService.getUserPermCodes(user.getId());
        List<String> permissions = new ArrayList<>();
        if (permCodesResult.getCode() == 200 && permCodesResult.getData() != null) {
            permissions = permCodesResult.getData();
        }

        // 7. 构建角色列表（当前用户是一对一角色关系）
        List<String> roles = new ArrayList<>();
        // 可以根据roleId查询角色名称，这里简化处理
        roles.add("ROLE_USER");

        // 8. 构建响应
        LoginResponseDto response = new LoginResponseDto(
                token,
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                user.getDeptId(),
                user.getRoleId(),
                roles,
                permissions
        );

        return Result.success(response);
    }

    @Override
    public Result<Void> logout() {
        // JWT是无状态的，登出主要在客户端完成
        // 客户端需要删除本地存储的token和用户信息
        // 这里返回成功响应即可
        return Result.success();
    }

}