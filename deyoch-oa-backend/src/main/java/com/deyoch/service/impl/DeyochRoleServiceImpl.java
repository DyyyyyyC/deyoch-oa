package com.deyoch.service.impl;

import com.deyoch.entity.DeyochRole;
import com.deyoch.entity.DeyochRolePermission;
import com.deyoch.mapper.DeyochRoleMapper;
import com.deyoch.mapper.DeyochRolePermissionMapper;
import com.deyoch.service.DeyochRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色服务实现类
 * 实现角色相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Service
public class DeyochRoleServiceImpl extends ServiceImpl<DeyochRoleMapper, DeyochRole> implements DeyochRoleService {

    private final DeyochRolePermissionMapper rolePermissionMapper;

    public DeyochRoleServiceImpl(DeyochRolePermissionMapper rolePermissionMapper) {
        this.rolePermissionMapper = rolePermissionMapper;
    }

    /**
     * 根据角色编码查询角色信息
     * 
     * @param roleCode 角色编码
     * @return 查询到的角色对象，未找到返回null
     */
    @Override
    public DeyochRole findByRoleCode(String roleCode) {
        return lambdaQuery()
                .eq(DeyochRole::getRoleCode, roleCode)
                .one();
    }

    /**
     * 检查角色编码是否存在
     * 
     * @param roleCode 角色编码
     * @return 存在返回true，不存在返回false
     */
    @Override
    public boolean existsByRoleCode(String roleCode) {
        return lambdaQuery()
                .eq(DeyochRole::getRoleCode, roleCode)
                .count() > 0;
    }

    /**
     * 根据角色ID查询该角色的所有权限ID
     * 
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @Override
    public List<Long> findPermissionIdsByRoleId(Long roleId) {
        List<DeyochRolePermission> rolePermissions = rolePermissionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeyochRolePermission>()
                        .eq(DeyochRolePermission::getRoleId, roleId)
        );
        
        List<Long> permissionIds = new ArrayList<>();
        for (DeyochRolePermission rp : rolePermissions) {
            permissionIds.add(rp.getPermId());
        }
        return permissionIds;
    }

    /**
     * 为角色分配权限
     * 
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 分配成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        // 先清除该角色的所有权限
        clearPermissions(roleId);
        
        // 批量插入新的权限关联
        for (Long permId : permissionIds) {
            DeyochRolePermission rp = new DeyochRolePermission();
            rp.setRoleId(roleId);
            rp.setPermId(permId);
            rolePermissionMapper.insert(rp);
        }
        
        return true;
    }

    /**
     * 清除角色的所有权限
     * 
     * @param roleId 角色ID
     * @return 清除成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearPermissions(Long roleId) {
        return rolePermissionMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeyochRolePermission>()
                        .eq(DeyochRolePermission::getRoleId, roleId)
        ) >= 0;
    }
}