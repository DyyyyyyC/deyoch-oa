package com.deyoch.service;

import com.deyoch.entity.DeyochRole;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 角色服务接口
 * 提供角色相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
public interface DeyochRoleService extends IService<DeyochRole> {
    
    /**
     * 根据角色编码查询角色信息
     * 
     * @param roleCode 角色编码
     * @return 查询到的角色对象，未找到返回null
     */
    DeyochRole findByRoleCode(String roleCode);
    
    /**
     * 检查角色编码是否存在
     * 
     * @param roleCode 角色编码
     * @return 存在返回true，不存在返回false
     */
    boolean existsByRoleCode(String roleCode);
    
    /**
     * 根据角色ID查询该角色的所有权限ID
     * 
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    java.util.List<Long> findPermissionIdsByRoleId(Long roleId);
    
    /**
     * 为角色分配权限
     * 
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 分配成功返回true，失败返回false
     */
    boolean assignPermissions(Long roleId, java.util.List<Long> permissionIds);
    
    /**
     * 清除角色的所有权限
     * 
     * @param roleId 角色ID
     * @return 清除成功返回true，失败返回false
     */
    boolean clearPermissions(Long roleId);
}