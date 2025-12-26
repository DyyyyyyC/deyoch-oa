package com.deyoch.controller;

import com.deyoch.entity.DeyochDept;
import com.deyoch.result.Result;
import com.deyoch.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 * 处理部门相关的HTTP请求
 */
@RestController
@RequestMapping("dept")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    /**
     * 获取部门列表
     * @return 部门列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('dept:list')")
    public Result<List<DeyochDept>> getDeptList() {
        return deptService.getDeptList();
    }

    /**
     * 获取部门树
     * @return 部门树
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('dept:tree')")
    public Result<List<DeyochDept>> getDeptTree() {
        return deptService.getDeptTree();
    }

    /**
     * 根据ID获取部门详情
     * @param id 部门ID
     * @return 部门详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('dept:detail')")
    public Result<DeyochDept> getDeptById(@PathVariable Long id) {
        return deptService.getDeptById(id);
    }

    /**
     * 创建部门
     * @param dept 部门信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('dept:add')")
    public Result<DeyochDept> createDept(@RequestBody DeyochDept dept) {
        return deptService.createDept(dept);
    }

    /**
     * 更新部门信息
     * @param id 部门ID
     * @param dept 部门信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('dept:update')")
    public Result<DeyochDept> updateDept(@PathVariable Long id, @RequestBody DeyochDept dept) {
        dept.setId(id);
        return deptService.updateDept(dept);
    }

    /**
     * 删除部门
     * @param id 部门ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('dept:delete')")
    public Result<Void> deleteDept(@PathVariable Long id) {
        return deptService.deleteDept(id);
    }

    /**
     * 更新部门状态
     * @param id 部门ID
     * @param status 部门状态
     * @return 更新结果
     */
    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('dept:update-status')")
    public Result<Void> updateDeptStatus(@PathVariable Long id, @RequestParam Long status) {
        return deptService.updateDeptStatus(id, status);
    }
}