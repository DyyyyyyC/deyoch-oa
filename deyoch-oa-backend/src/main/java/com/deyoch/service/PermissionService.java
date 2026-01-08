package com.deyoch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deyoch.entity.DeyochPermission;
import com.deyoch.common.result.Result;

import java.util.List;

/**
 * 权限管理服务接口
 * 定义权限相关的业务逻辑方法
 */
public interface PermissionService extends IService<DeyochPermission> {

    /**
     * 获取权限列表（树形结构）
     * @return 权限列表
     */
    Result<List<DeyochPermission>> getPermissionTree();

    /**
     * 获取权限列表（扁平结构）
     * @return 权限列表
     */
    Result<List<DeyochPermission>> getPermissionList();

    /**
     * 根据ID获取权限详情
     * @param id 权限ID
     * @return 权限详情
     */
    Result<DeyochPermission> getPermissionById(Long id);

    /**
     * 创建权限
     * @param permission 权限信息
     * @return 创建结果
     */
    Result<DeyochPermission> createPermission(DeyochPermission permission);

    /**
     * 更新权限信息
     * @param permission 权限信息
     * @return 更新结果
     */
    Result<DeyochPermission> updatePermission(DeyochPermission permission);

    /**
     * 删除权限
     * @param id 权限ID
     * @return 删除结果
     */
    Result<Void> deletePermission(Long id);

    /**
     * 根据用户ID获取用户拥有的权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    Result<List<DeyochPermission>> getUserPermissions(Long userId);

    /**
     * 根据用户ID获取用户拥有的权限编码列表
     * @param userId 用户ID
     * @return 权限编码列表
     */
    Result<List<String>> getUserPermCodes(Long userId);
}