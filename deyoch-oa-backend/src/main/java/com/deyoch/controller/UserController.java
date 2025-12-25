package com.deyoch.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deyoch.common.PageResult;
import com.deyoch.common.Result;
import com.deyoch.common.ResultCode;
import com.deyoch.dto.UserRequest;
import com.deyoch.entity.DeyochUser;
import com.deyoch.service.DeyochUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 * 处理用户CRUD等用户管理相关接口
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final DeyochUserService userService;
    
    /**
     * 分页查询用户列表
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param username 用户名（模糊查询，可选）
     * @param nickname 昵称（模糊查询，可选）
     * @param status 状态（可选）
     * @param deptId 部门ID（可选）
     * @return 分页用户列表
     */
    @GetMapping("/page")
    public Result<PageResult<DeyochUser>> getUserPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) Long status,
            @RequestParam(required = false) Long deptId) {
        
        Page<DeyochUser> pageResult = userService.findPageByCondition(
                page, size, username, nickname, status, deptId);
        
        PageResult<DeyochUser> result = PageResult.of(
                pageResult.getCurrent(),
                pageResult.getSize(),
                pageResult.getTotal(),
                pageResult.getRecords()
        );
        
        return Result.success(result);
    }
    
    /**
     * 根据ID获取用户信息
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result<DeyochUser> getUserById(@PathVariable Long id) {
        DeyochUser user = userService.findByIdWithDetails(id);
        
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        
        // 不返回密码
        user.setPassword(null);
        return Result.success(user);
    }
    
    /**
     * 获取所有用户列表
     * 
     * @return 用户列表
     */
    @GetMapping("/list")
    public Result<List<DeyochUser>> getAllUsers() {
        List<DeyochUser> users = userService.list();
        
        // 不返回密码
        users.forEach(user -> user.setPassword(null));
        
        return Result.success(users);
    }
    
    /**
     * 创建新用户
     * 
     * @param userRequest 用户创建请求
     * @return 创建结果
     */
    @PostMapping
    public Result<String> createUser(@RequestBody UserRequest userRequest) {
        // 检查用户名是否已存在
        if (userService.existsByUsername(userRequest.getUsername())) {
            return Result.error(ResultCode.USER_EXISTS, "用户名已存在");
        }
        
        // 创建用户
        DeyochUser user = new DeyochUser();
        user.setUsername(userRequest.getUsername());
        user.setPassword(userRequest.getPassword());
        user.setNickname(userRequest.getNickname());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());
        user.setAvatar(userRequest.getAvatar());
        user.setDeptId(userRequest.getDeptId());
        user.setRoleId(userRequest.getRoleId());
        user.setStatus(userRequest.getStatus());
        
        boolean success = userService.createUser(user, userRequest.getPassword());
        
        if (success) {
            return Result.success("创建成功");
        } else {
            return Result.error(ResultCode.INTERNAL_ERROR, "创建失败");
        }
    }
    
    /**
     * 更新用户信息
     * 
     * @param id 用户ID
     * @param userRequest 用户更新请求
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<String> updateUser(@PathVariable Long id, @RequestBody UserRequest userRequest) {
        // 检查用户是否存在
        DeyochUser existingUser = userService.findByIdWithDetails(id);
        if (existingUser == null) {
            return Result.error(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
        
        // 更新用户信息（不更新密码）
        existingUser.setNickname(userRequest.getNickname());
        existingUser.setEmail(userRequest.getEmail());
        existingUser.setPhone(userRequest.getPhone());
        existingUser.setAvatar(userRequest.getAvatar());
        existingUser.setDeptId(userRequest.getDeptId());
        existingUser.setRoleId(userRequest.getRoleId());
        existingUser.setStatus(userRequest.getStatus());
        
        boolean success = userService.updateUser(existingUser);
        
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.error(ResultCode.INTERNAL_ERROR, "更新失败");
        }
    }
    
    /**
     * 更新用户状态
     * 
     * @param id 用户ID
     * @param status 新状态：0-禁用，1-启用
     * @return 更新结果
     */
    @PatchMapping("/{id}/status")
    public Result<String> updateUserStatus(@PathVariable Long id, @RequestParam Long status) {
        if (status != 0 && status != 1) {
            return Result.error(ResultCode.BAD_REQUEST, "状态值无效");
        }
        
        boolean success = userService.updateUserStatus(id, status);
        
        if (success) {
            return Result.success(status == 1 ? "用户已启用" : "用户已禁用");
        } else {
            return Result.error(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
    }
    
    /**
     * 重置用户密码
     * 
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 重置结果
     */
    @PatchMapping("/{id}/password")
    public Result<String> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        boolean success = userService.resetPassword(id, newPassword);
        
        if (success) {
            return Result.success("密码重置成功");
        } else {
            return Result.error(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
    }
    
    /**
     * 删除用户（逻辑删除）
     * 
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        boolean success = userService.deleteUser(id);
        
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error(ResultCode.USER_NOT_FOUND, "用户不存在");
        }
    }
    
    /**
     * 批量删除用户（逻辑删除）
     * 
     * @param ids 用户ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result<String> deleteUsers(@RequestBody List<Long> ids) {
        boolean success = userService.deleteUsers(ids);
        
        if (success) {
            return Result.success("批量删除成功");
        } else {
            return Result.error(ResultCode.INTERNAL_ERROR, "批量删除失败");
        }
    }
    
    /**
     * 检查用户名是否可用
     * 
     * @param username 用户名
     * @return 可用返回true
     */
    @GetMapping("/check/username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return Result.success(!exists);
    }
    
    /**
     * 检查邮箱是否可用
     * 
     * @param email 邮箱
     * @return 可用返回true
     */
    @GetMapping("/check/email")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return Result.success(!exists);
    }
}