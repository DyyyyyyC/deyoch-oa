package com.deyoch.service.impl;

import com.deyoch.entity.DeyochPermission;
import com.deyoch.entity.DeyochRolePermission;
import com.deyoch.mapper.DeyochPermissionMapper;
import com.deyoch.mapper.DeyochRolePermissionMapper;
import com.deyoch.service.DeyochPermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限服务实现类
 * 实现权限相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Service
public class DeyochPermissionServiceImpl extends ServiceImpl<DeyochPermissionMapper, DeyochPermission> implements DeyochPermissionService {

    private final DeyochRolePermissionMapper rolePermissionMapper;

    public DeyochPermissionServiceImpl(DeyochRolePermissionMapper rolePermissionMapper) {
        this.rolePermissionMapper = rolePermissionMapper;
    }

    /**
     * 根据权限编码查询权限信息
     * 
     * @param permCode 权限编码
     * @return 查询到的权限对象，未找到返回null
     */
    @Override
    public DeyochPermission findByPermCode(String permCode) {
        return lambdaQuery()
                .eq(DeyochPermission::getPermCode, permCode)
                .one();
    }

    /**
     * 检查权限编码是否存在
     * 
     * @param permCode 权限编码
     * @return 存在返回true，不存在返回false
     */
    @Override
    public boolean existsByPermCode(String permCode) {
        return lambdaQuery()
                .eq(DeyochPermission::getPermCode, permCode)
                .count() > 0;
    }

    /**
     * 查询所有菜单类型的权限（用于前端菜单渲染）
     * 
     * @return 菜单权限列表
     */
    @Override
    public List<DeyochPermission> findAllMenuPermissions() {
        return lambdaQuery()
                .eq(DeyochPermission::getPermType, "menu")
                .eq(DeyochPermission::getStatus, 1)
                .orderByAsc(DeyochPermission::getSort)
                .list();
    }

    /**
     * 根据父级ID查询子权限列表
     * 
     * @param parentId 父级ID
     * @return 子权限列表
     */
    @Override
    public List<DeyochPermission> findByParentId(Long parentId) {
        return lambdaQuery()
                .eq(DeyochPermission::getParentId, parentId)
                .eq(DeyochPermission::getStatus, 1)
                .orderByAsc(DeyochPermission::getSort)
                .list();
    }

    /**
     * 根据角色ID查询该角色拥有的所有权限
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Override
    public List<DeyochPermission> findByRoleId(Long roleId) {
        // 查询角色权限关联表
        List<DeyochRolePermission> rolePermissions = rolePermissionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeyochRolePermission>()
                        .eq(DeyochRolePermission::getRoleId, roleId)
        );
        
        if (rolePermissions.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 提取权限ID
        List<Long> permIds = new ArrayList<>();
        for (DeyochRolePermission rp : rolePermissions) {
            permIds.add(rp.getPermId());
        }
        
        // 查询权限详情
        return lambdaQuery()
                .in(DeyochPermission::getId, permIds)
                .eq(DeyochPermission::getStatus, 1)
                .list();
    }

    /**
     * 查询权限树形结构
     * 
     * @return 树形权限列表
     */
    @Override
    public List<DeyochPermission> findPermissionTree() {
        return lambdaQuery()
                .eq(DeyochPermission::getStatus, 1)
                .orderByAsc(DeyochPermission::getSort)
                .list();
    }
}