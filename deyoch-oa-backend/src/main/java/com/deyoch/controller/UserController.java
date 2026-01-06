package com.deyoch.controller;

import com.deyoch.entity.DeyochUser;
import com.deyoch.result.Result;
import com.deyoch.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

/**
 * 用户管理控制器
 * 处理用户相关的HTTP请求
 */
@RestController
@RequestMapping("user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    private final UserService userService;

    /**
     * 获取用户列表
     * @return 用户列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:user:manage')")
    @Operation(summary = "获取用户列表", description = "获取所有用户的列表")
    public Result<List<DeyochUser>> getUserList() {
        return userService.getUserList();
    }

    /**
     * 根据ID获取用户详情
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:manage')")
    @Operation(summary = "根据ID获取用户详情", description = "根据用户ID获取用户的详细信息")
    public Result<DeyochUser> getUserById(@PathVariable @Parameter(description = "用户ID") Long id) {
        return userService.getUserById(id);
    }

    /**
     * 创建用户
     * @param user 用户信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('sys:user:manage')")
    @Operation(summary = "创建用户", description = "创建新的用户")
    public Result<DeyochUser> createUser(@RequestBody DeyochUser user) {
        return userService.createUser(user);
    }

    /**
     * 更新用户信息
     * @param id 用户ID
     * @param user 用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:manage')")
    @Operation(summary = "更新用户信息", description = "根据用户ID更新用户信息")
    public Result<DeyochUser> updateUser(@PathVariable @Parameter(description = "用户ID") Long id, @RequestBody DeyochUser user) {
        user.setId(id);
        return userService.updateUser(user);
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:manage')")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    public Result<Void> deleteUser(@PathVariable @Parameter(description = "用户ID") Long id) {
        return userService.deleteUser(id);
    }

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 用户状态（1-启用，0-禁用）
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('sys:user:manage')")
    @Operation(summary = "更新用户状态", description = "根据用户ID更新用户状态")
    public Result<Void> updateUserStatus(@PathVariable @Parameter(description = "用户ID") Long id, @RequestParam @Parameter(description = "用户状态（1-启用，0-禁用）") Integer status) {
        return userService.updateUserStatus(id, status);
    }

    /**
     * 获取当前登录用户信息
     * @return 当前用户信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前登录用户信息", description = "获取当前登录用户的详细信息")
    public Result<DeyochUser> getCurrentUser() {
        return userService.getCurrentUser();
    }
}