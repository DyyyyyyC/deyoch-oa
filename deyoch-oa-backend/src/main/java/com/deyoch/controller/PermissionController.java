package com.deyoch.controller;

import com.deyoch.entity.DeyochPermission;
import com.deyoch.common.result.Result;
import com.deyoch.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;
import java.util.Map;

/**
 * 权限管理控制器
 * 处理权限相关的HTTP请求
 */
@RestController
@RequestMapping("permission")
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "权限相关接口")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 获取权限树
     * @return 权限树
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('sys:perm:manage')")
    @Operation(summary = "获取权限树", description = "获取权限的树形结构")
    public Result<List<DeyochPermission>> getPermissionTree() {
        return permissionService.getPermissionTree();
    }

    /**
     * 获取权限列表（扁平结构）
     * @return 权限列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:perm:manage')")
    @Operation(summary = "获取权限列表", description = "获取权限的扁平列表结构")
    public Result<Map<String, Object>> getPermissionList(
            @RequestParam(required = false) @Parameter(description = "权限名称") String permName,
            @RequestParam(required = false) @Parameter(description = "权限代码") String permCode,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") Integer size
    ) {
        return permissionService.getPermissionList(permName, permCode, page, size);
    }

    /**
     * 根据ID获取权限详情
     * @param id 权限ID
     * @return 权限详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:perm:manage')")
    @Operation(summary = "根据ID获取权限详情", description = "根据权限ID获取权限的详细信息")
    public Result<DeyochPermission> getPermissionById(@PathVariable @Parameter(description = "权限ID") Long id) {
        return permissionService.getPermissionById(id);
    }

    /**
     * 创建权限
     * @param permission 权限信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('sys:perm:manage')")
    @Operation(summary = "创建权限", description = "创建新的权限")
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
    @PreAuthorize("hasAuthority('sys:perm:manage')")
    @Operation(summary = "更新权限信息", description = "根据权限ID更新权限信息")
    public Result<DeyochPermission> updatePermission(@PathVariable @Parameter(description = "权限ID") Long id, @RequestBody DeyochPermission permission) {
        permission.setId(id);
        return permissionService.updatePermission(permission);
    }

    /**
     * 删除权限
     * @param id 权限ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:perm:manage')")
    @Operation(summary = "删除权限", description = "根据权限ID删除权限")
    public Result<Void> deletePermission(@PathVariable @Parameter(description = "权限ID") Long id) {
        return permissionService.deletePermission(id);
    }

    /**
     * 根据用户ID获取用户拥有的权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID获取权限列表", description = "根据用户ID获取用户拥有的权限列表")
    public Result<List<DeyochPermission>> getUserPermissions(@PathVariable @Parameter(description = "用户ID") Long userId) {
        return permissionService.getUserPermissions(userId);
    }

    /**
     * 根据用户ID获取用户拥有的权限编码列表
     * @param userId 用户ID
     * @return 权限编码列表
     */
    @GetMapping("/user/{userId}/codes")
    @Operation(summary = "获取用户权限编码列表", description = "根据用户ID获取用户拥有的权限编码列表")
    public Result<List<String>> getUserPermCodes(@PathVariable @Parameter(description = "用户ID") Long userId) {
        return permissionService.getUserPermCodes(userId);
    }
}