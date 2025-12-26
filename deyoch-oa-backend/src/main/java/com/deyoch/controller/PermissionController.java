package com.deyoch.controller;

import com.deyoch.entity.DeyochPermission;
import com.deyoch.result.Result;
import com.deyoch.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 * 处理权限相关的HTTP请求
 */
@RestController
@RequestMapping("permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 获取权限树
     * @return 权限树
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('permission:tree')")
    public Result<List<DeyochPermission>> getPermissionTree() {
        return permissionService.getPermissionTree();
    }

    /**
     * 获取权限列表（扁平结构）
     * @return 权限列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('permission:list')")
    public Result<List<DeyochPermission>> getPermissionList() {
        return permissionService.getPermissionList();
    }

    /**
     * 根据ID获取权限详情
     * @param id 权限ID
     * @return 权限详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:detail')")
    public Result<DeyochPermission> getPermissionById(@PathVariable Long id) {
        return permissionService.getPermissionById(id);
    }

    /**
     * 创建权限
     * @param permission 权限信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('permission:add')")
    public Result<DeyochPermission> createPermission(@RequestBody DeyochPermission permission) {
        return permissionService.createPermission(permission);
    }

    /**
     * 更新权限信息
     * @param id 权限ID
     * @param permission 权限信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:update')")
    public Result<DeyochPermission> updatePermission(@PathVariable Long id, @RequestBody DeyochPermission permission) {
        permission.setId(id);
        return permissionService.updatePermission(permission);
    }

    /**
     * 删除权限
     * @param id 权限ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:delete')")
    public Result<Void> deletePermission(@PathVariable Long id) {
        return permissionService.deletePermission(id);
    }

    /**
     * 根据用户ID获取用户拥有的权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<DeyochPermission>> getUserPermissions(@PathVariable Long userId) {
        return permissionService.getUserPermissions(userId);
    }

    /**
     * 根据用户ID获取用户拥有的权限编码列表
     * @param userId 用户ID
     * @return 权限编码列表
     */
    @GetMapping("/user/{userId}/codes")
    public Result<List<String>> getUserPermCodes(@PathVariable Long userId) {
        return permissionService.getUserPermCodes(userId);
    }
}