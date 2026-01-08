package com.deyoch.controller;

import com.deyoch.entity.DeyochRole;
import com.deyoch.common.result.PageResult;
import com.deyoch.common.result.Result;
import com.deyoch.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

/**
 * 角色管理控制器
 * 处理角色相关的HTTP请求
 */
@RestController
@RequestMapping("role")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色相关接口")
public class RoleController {

    private final RoleService roleService;

    /**
     * 获取角色列表（分页）
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词
     * @return 分页角色列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:role:manage')")
    @Operation(summary = "获取角色列表", description = "获取所有角色的分页列表")
    public Result<PageResult<DeyochRole>> getRoleList(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页数量") Integer size,
            @RequestParam(required = false) @Parameter(description = "搜索关键词") String keyword) {
        return roleService.getRoleList(page, size, keyword);
    }

    /**
     * 根据ID获取角色详情
     * @param id 角色ID
     * @return 角色详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:manage')")
    @Operation(summary = "根据ID获取角色详情", description = "根据角色ID获取角色的详细信息")
    public Result<DeyochRole> getRoleById(@PathVariable @Parameter(description = "角色ID") Long id) {
        return roleService.getRoleById(id);
    }

    /**
     * 创建角色
     * @param role 角色信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('sys:role:manage')")
    @Operation(summary = "创建角色", description = "创建新的角色")
    public Result<DeyochRole> createRole(@RequestBody DeyochRole role) {
        return roleService.createRole(role);
    }

    /**
     * 更新角色信息
     * @param id 角色ID
     * @param role 角色信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:manage')")
    @Operation(summary = "更新角色信息", description = "根据角色ID更新角色信息")
    public Result<DeyochRole> updateRole(@PathVariable @Parameter(description = "角色ID") Long id, @RequestBody DeyochRole role) {
        role.setId(id);
        return roleService.updateRole(role);
    }

    /**
     * 删除角色
     * @param id 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:manage')")
    @Operation(summary = "删除角色", description = "根据角色ID删除角色")
    public Result<Void> deleteRole(@PathVariable @Parameter(description = "角色ID") Long id) {
        return roleService.deleteRole(id);
    }

    /**
     * 为角色分配权限
     * @param roleId 角色ID
     * @param permIds 权限ID列表
     * @return 分配结果
     */
    @PostMapping("/{roleId}/assign-perms")
    @PreAuthorize("hasAuthority('sys:role:manage')")
    @Operation(summary = "为角色分配权限", description = "根据角色ID为角色分配权限")
    public Result<Void> assignPermissions(@PathVariable @Parameter(description = "角色ID") Long roleId, @RequestBody @Parameter(description = "权限ID列表") List<Long> permIds) {
        return roleService.assignPermissions(roleId, permIds);
    }

    /**
     * 获取角色已分配的权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @GetMapping("/{roleId}/perms")
    @PreAuthorize("hasAuthority('sys:role:manage')")
    @Operation(summary = "获取角色权限ID列表", description = "根据角色ID获取角色已分配的权限ID列表")
    public Result<List<Long>> getRolePermIds(@PathVariable @Parameter(description = "角色ID") Long roleId) {
        return roleService.getRolePermIds(roleId);
    }
}