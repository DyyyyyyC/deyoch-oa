package com.deyoch.service;

import com.deyoch.entity.DeyochPermission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 权限服务接口
 * 提供权限相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
public interface DeyochPermissionService extends IService<DeyochPermission> {
    
    /**
     * 根据权限编码查询权限信息
     * 
     * @param permCode 权限编码
     * @return 查询到的权限对象，未找到返回null
     */
    DeyochPermission findByPermCode(String permCode);
    
    /**
     * 检查权限编码是否存在
     * 
     * @param permCode 权限编码
     * @return 存在返回true，不存在返回false
     */
    boolean existsByPermCode(String permCode);
    
    /**
     * 查询所有菜单类型的权限（用于前端菜单渲染）
     * 
     * @return 菜单权限列表
     */
    List<DeyochPermission> findAllMenuPermissions();
    
    /**
     * 根据父级ID查询子权限列表
     * 
     * @param parentId 父级ID
     * @return 子权限列表
     */
    List<DeyochPermission> findByParentId(Long parentId);
    
    /**
     * 根据角色ID查询该角色拥有的所有权限
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<DeyochPermission> findByRoleId(Long roleId);
    
    /**
     * 查询权限树形结构
     * 
     * @return 树形权限列表
     */
    List<DeyochPermission> findPermissionTree();
}