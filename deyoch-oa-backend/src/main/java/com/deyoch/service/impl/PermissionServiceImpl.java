package com.deyoch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deyoch.entity.DeyochPermission;
import com.deyoch.entity.DeyochRolePermission;
import com.deyoch.entity.DeyochUser;
import com.deyoch.mapper.DeyochPermissionMapper;
import com.deyoch.mapper.DeyochRolePermissionMapper;
import com.deyoch.mapper.DeyochUserMapper;
import com.deyoch.result.Result;
import com.deyoch.result.ResultCode;
import com.deyoch.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限管理服务实现类
 * 实现权限相关的业务逻辑
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final DeyochPermissionMapper deyochPermissionMapper;
    private final DeyochRolePermissionMapper deyochRolePermissionMapper;
    private final DeyochUserMapper deyochUserMapper;

    @Override
    public Result<List<DeyochPermission>> getPermissionTree() {
        try {
            // 获取所有权限列表
            List<DeyochPermission> permList = deyochPermissionMapper.selectList(null);
            
            // 构建权限树
            List<DeyochPermission> permTree = buildPermissionTree(permList);
            
            return Result.success(permTree);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取权限树失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<DeyochPermission>> getPermissionList() {
        try {
            // 查询所有权限，按创建时间倒序排列
            LambdaQueryWrapper<DeyochPermission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(DeyochPermission::getCreatedAt);
            List<DeyochPermission> permList = deyochPermissionMapper.selectList(queryWrapper);
            return Result.success(permList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取权限列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochPermission> getPermissionById(Long id) {
        try {
            DeyochPermission permission = deyochPermissionMapper.selectById(id);
            if (permission == null) {
                return Result.error(ResultCode.PERMISSION_NOT_FOUND, "权限不存在");
            }
            return Result.success(permission);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取权限详情失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochPermission> createPermission(DeyochPermission permission) {
        try {
            // 检查权限名称是否已存在
            LambdaQueryWrapper<DeyochPermission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochPermission::getPermName, permission.getPermName());
            if (deyochPermissionMapper.selectOne(queryWrapper) != null) {
                return Result.error(ResultCode.PARAM_ERROR, "权限名称已存在");
            }

            // 检查权限编码是否已存在
            queryWrapper.clear();
            queryWrapper.eq(DeyochPermission::getPermCode, permission.getPermCode());
            if (deyochPermissionMapper.selectOne(queryWrapper) != null) {
                return Result.error(ResultCode.PARAM_ERROR, "权限编码已存在");
            }

            // 设置默认值
            if (permission.getParentId() == null) {
                permission.setParentId(0L);
            }
            if (permission.getSort() == null) {
                permission.setSort(0L);
            }
            if (permission.getStatus() == null) {
                permission.setStatus(1L);
            }

            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            permission.setCreatedAt(now);
            permission.setUpdatedAt(now);

            // 创建权限
            deyochPermissionMapper.insert(permission);
            return Result.success(permission);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "创建权限失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochPermission> updatePermission(DeyochPermission permission) {
        try {
            // 检查权限是否存在
            DeyochPermission existingPerm = deyochPermissionMapper.selectById(permission.getId());
            if (existingPerm == null) {
                return Result.error(ResultCode.PERMISSION_NOT_FOUND, "权限不存在");
            }

            // 检查权限名称是否已被其他权限使用
            LambdaQueryWrapper<DeyochPermission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochPermission::getPermName, permission.getPermName());
            queryWrapper.ne(DeyochPermission::getId, permission.getId());
            if (deyochPermissionMapper.selectOne(queryWrapper) != null) {
                return Result.error(ResultCode.PARAM_ERROR, "权限名称已存在");
            }

            // 检查权限编码是否已被其他权限使用
            queryWrapper.clear();
            queryWrapper.eq(DeyochPermission::getPermCode, permission.getPermCode());
            queryWrapper.ne(DeyochPermission::getId, permission.getId());
            if (deyochPermissionMapper.selectOne(queryWrapper) != null) {
                return Result.error(ResultCode.PARAM_ERROR, "权限编码已存在");
            }

            // 检查是否是自己的父权限
            if (permission.getId().equals(permission.getParentId())) {
                return Result.error(ResultCode.PARAM_ERROR, "不能将自己设为父权限");
            }

            // 设置更新时间
            permission.setUpdatedAt(LocalDateTime.now());

            // 更新权限
            deyochPermissionMapper.updateById(permission);
            return Result.success(permission);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新权限失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deletePermission(Long id) {
        try {
            // 检查权限是否存在
            DeyochPermission permission = deyochPermissionMapper.selectById(id);
            if (permission == null) {
                return Result.error(ResultCode.PERMISSION_NOT_FOUND, "权限不存在");
            }

            // 检查是否有子权限
            LambdaQueryWrapper<DeyochPermission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochPermission::getParentId, id);
            if (deyochPermissionMapper.selectOne(queryWrapper) != null) {
                return Result.error(ResultCode.OPERATION_NOT_ALLOWED, "请先删除子权限");
            }

            // 删除权限与角色的关联关系
            LambdaQueryWrapper<DeyochRolePermission> rolePermWrapper = new LambdaQueryWrapper<>();
            rolePermWrapper.eq(DeyochRolePermission::getPermId, id);
            deyochRolePermissionMapper.delete(rolePermWrapper);

            // 删除权限
            deyochPermissionMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "删除权限失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<DeyochPermission>> getUserPermissions(Long userId) {
        try {
            // 1. 根据用户ID获取用户信息
            DeyochUser user = deyochUserMapper.selectById(userId);
            if (user == null) {
                return Result.error(ResultCode.USER_NOT_FOUND, "用户不存在");
            }
            
            // 2. 根据角色ID获取角色拥有的权限ID列表
            LambdaQueryWrapper<DeyochRolePermission> rolePermWrapper = new LambdaQueryWrapper<>();
            rolePermWrapper.eq(DeyochRolePermission::getRoleId, user.getRoleId());
            List<DeyochRolePermission> rolePermList = deyochRolePermissionMapper.selectList(rolePermWrapper);
            
            if (rolePermList.isEmpty()) {
                return Result.success(new ArrayList<>());
            }
            
            // 3. 提取权限ID列表
            List<Long> permIds = rolePermList.stream()
                    .map(DeyochRolePermission::getPermId)
                    .collect(Collectors.toList());
            
            // 4. 根据权限ID列表获取权限详情
            List<DeyochPermission> permList = deyochPermissionMapper.selectBatchIds(permIds);
            
            // 5. 构建权限树
            List<DeyochPermission> permTree = buildPermissionTree(permList);
            
            return Result.success(permTree);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取用户权限失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<String>> getUserPermCodes(Long userId) {
        try {
            // 1. 根据用户ID获取用户信息
            DeyochUser user = deyochUserMapper.selectById(userId);
            if (user == null) {
                return Result.error(ResultCode.USER_NOT_FOUND, "用户不存在");
            }
            
            System.out.println("获取用户权限编码：userId=" + userId + ", roleId=" + user.getRoleId());
            
            // 2. 根据角色ID获取角色拥有的权限ID列表
            LambdaQueryWrapper<DeyochRolePermission> rolePermWrapper = new LambdaQueryWrapper<>();
            rolePermWrapper.eq(DeyochRolePermission::getRoleId, user.getRoleId());
            List<DeyochRolePermission> rolePermList = deyochRolePermissionMapper.selectList(rolePermWrapper);
            
            System.out.println("角色权限关联列表：" + rolePermList);
            
            if (rolePermList.isEmpty()) {
                return Result.success(new ArrayList<>());
            }
            
            // 3. 提取权限ID列表
            List<Long> permIds = rolePermList.stream()
                    .map(DeyochRolePermission::getPermId)
                    .collect(Collectors.toList());
            
            System.out.println("权限ID列表：" + permIds);
            
            // 4. 根据权限ID列表获取权限详情
            List<DeyochPermission> permList = deyochPermissionMapper.selectBatchIds(permIds);
            
            System.out.println("权限详情列表：" + permList);
            
            // 5. 提取权限编码列表
            List<String> permCodes = permList.stream()
                    .map(DeyochPermission::getPermCode)
                    .collect(Collectors.toList());
            
            System.out.println("返回的权限编码列表：" + permCodes);
            
            return Result.success(permCodes);
        } catch (Exception e) {
            System.out.println("获取用户权限编码失败：" + e.getMessage());
            e.printStackTrace();
            return Result.error(ResultCode.SYSTEM_ERROR, "获取用户权限编码失败：" + e.getMessage());
        }
    }

    /**
     * 构建权限树
     * @param permList 权限列表
     * @return 权限树
     */
    private List<DeyochPermission> buildPermissionTree(List<DeyochPermission> permList) {
        // 将权限列表转换为Map，便于根据ID查找
        Map<Long, DeyochPermission> permMap = permList.stream()
                .collect(Collectors.toMap(DeyochPermission::getId, perm -> perm));

        // 创建权限树列表
        List<DeyochPermission> permTree = new ArrayList<>();

        // 遍历权限列表，构建树结构
        for (DeyochPermission perm : permList) {
            Long parentId = perm.getParentId();
            if (parentId == null || parentId == 0L) {
                // 根权限，直接添加到树中
                permTree.add(perm);
            } else {
                // 子权限，添加到父权限的children中
                DeyochPermission parentPerm = permMap.get(parentId);
                if (parentPerm != null) {
                    // 由于DeyochPermission实体类中没有children字段，暂时不构建树形结构
                    // 后续可以扩展DeyochPermission类，添加children字段
                }
            }
        }

        // 暂时返回扁平列表，后续改为返回树形结构
        return permList;
    }
}