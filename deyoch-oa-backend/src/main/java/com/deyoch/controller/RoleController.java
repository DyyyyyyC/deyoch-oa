package com.deyoch.controller;

import com.deyoch.entity.DeyochRole;
import com.deyoch.result.Result;
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
     * 获取角色列表
     * @return 角色列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('role:list')")
    @Operation(summary = "获取角色列表", description = "获取所有角色的列表")
    public Result<List<DeyochRole>> getRoleList() {
        return roleService.getRoleList();
    }

    /**
     * 根据ID获取角色详情
     * @param id 角色ID
     * @return 角色详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:detail')")
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
    @PreAuthorize("hasAuthority('role:add')")
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
    @PreAuthorize("hasAuthority('role:update')")
    public Result<DeyochRole> updateRole(@PathVariable Long id, @RequestBody DeyochRole role) {
        role.setId(id);
        return roleService.updateRole(role);
    }

    /**
     * 删除角色
     * @param id 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public Result<Void> deleteRole(@PathVariable Long id) {
        return roleService.deleteRole(id);
    }

    /**
     * 为角色分配权限
     * @param roleId 角色ID
     * @param permIds 权限ID列表
     * @return 分配结果
     */
    @PostMapping("/{roleId}/assign-perms")
    @PreAuthorize("hasAuthority('role:assign-perm')")
    public Result<Void> assignPermissions(@PathVariable Long roleId, @RequestBody List<Long> permIds) {
        return roleService.assignPermissions(roleId, permIds);
    }

    /**
     * 获取角色已分配的权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @GetMapping("/{roleId}/perms")
    @PreAuthorize("hasAuthority('role:perm-list')")
    public Result<List<Long>> getRolePermIds(@PathVariable Long roleId) {
        return roleService.getRolePermIds(roleId);
    }
}