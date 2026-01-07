package com.deyoch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deyoch.entity.DeyochRole;
import com.deyoch.entity.DeyochRolePermission;
import com.deyoch.mapper.DeyochRoleMapper;
import com.deyoch.mapper.DeyochRolePermissionMapper;
import com.deyoch.result.Result;
import com.deyoch.result.ResultCode;
import com.deyoch.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色管理服务实现类
 * 继承ServiceImpl获得MyBatis Plus的基础CRUD能力
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<DeyochRoleMapper, DeyochRole> implements RoleService {

    private final DeyochRolePermissionMapper deyochRolePermissionMapper;

    @Override
    public Result<List<DeyochRole>> getRoleList() {
        try {
            List<DeyochRole> roleList = list();
            return Result.success(roleList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取角色列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochRole> getRoleById(Long id) {
        try {
            DeyochRole role = getById(id);
            if (role == null) {
                return Result.error(ResultCode.ROLE_NOT_FOUND, "角色不存在");
            }
            return Result.success(role);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取角色详情失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochRole> createRole(DeyochRole role) {
        try {
            // 检查角色名称是否已存在
            LambdaQueryWrapper<DeyochRole> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochRole::getRoleName, role.getRoleName());
            if (getOne(queryWrapper) != null) {
                return Result.error(ResultCode.ROLE_EXISTS, "角色名称已存在");
            }

            // 检查角色编码是否已存在
            queryWrapper.clear();
            queryWrapper.eq(DeyochRole::getRoleCode, role.getRoleCode());
            if (getOne(queryWrapper) != null) {
                return Result.error(ResultCode.ROLE_EXISTS, "角色编码已存在");
            }

            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            role.setCreatedAt(now);
            role.setUpdatedAt(now);

            // 创建角色
            save(role);
            return Result.success(role);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "创建角色失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochRole> updateRole(DeyochRole role) {
        try {
            // 检查角色是否存在
            DeyochRole existingRole = getById(role.getId());
            if (existingRole == null) {
                return Result.error(ResultCode.ROLE_NOT_FOUND, "角色不存在");
            }

            // 检查角色名称是否已被其他角色使用
            LambdaQueryWrapper<DeyochRole> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochRole::getRoleName, role.getRoleName());
            queryWrapper.ne(DeyochRole::getId, role.getId());
            if (getOne(queryWrapper) != null) {
                return Result.error(ResultCode.ROLE_EXISTS, "角色名称已存在");
            }

            // 检查角色编码是否已被其他角色使用
            queryWrapper.clear();
            queryWrapper.eq(DeyochRole::getRoleCode, role.getRoleCode());
            queryWrapper.ne(DeyochRole::getId, role.getId());
            if (getOne(queryWrapper) != null) {
                return Result.error(ResultCode.ROLE_EXISTS, "角色编码已存在");
            }

            // 设置更新时间
            role.setUpdatedAt(LocalDateTime.now());

            // 更新角色
            updateById(role);
            return Result.success(role);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新角色失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteRole(Long id) {
        try {
            // 检查角色是否存在
            DeyochRole role = getById(id);
            if (role == null) {
                return Result.error(ResultCode.ROLE_NOT_FOUND, "角色不存在");
            }

            // 删除角色与权限的关联关系
            LambdaQueryWrapper<DeyochRolePermission> permWrapper = new LambdaQueryWrapper<>();
            permWrapper.eq(DeyochRolePermission::getRoleId, id);
            deyochRolePermissionMapper.delete(permWrapper);

            // 删除角色
            removeById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "删除角色失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> assignPermissions(Long roleId, List<Long> permIds) {
        try {
            // 检查角色是否存在
            if (getById(roleId) == null) {
                return Result.error(ResultCode.ROLE_NOT_FOUND, "角色不存在");
            }

            // 删除原有的权限关联
            LambdaQueryWrapper<DeyochRolePermission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochRolePermission::getRoleId, roleId);
            deyochRolePermissionMapper.delete(queryWrapper);

            // 添加新的权限关联
            if (permIds != null && !permIds.isEmpty()) {
                List<DeyochRolePermission> rolePermList = new ArrayList<>();
                for (Long permId : permIds) {
                    DeyochRolePermission rolePerm = new DeyochRolePermission();
                    rolePerm.setRoleId(roleId);
                    rolePerm.setPermId(permId);
                    deyochRolePermissionMapper.insert(rolePerm);
                }
            }

            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "分配权限失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<Long>> getRolePermIds(Long roleId) {
        try {
            // 检查角色是否存在
            if (getById(roleId) == null) {
                return Result.error(ResultCode.ROLE_NOT_FOUND, "角色不存在");
            }

            // 查询角色拥有的权限ID列表
            LambdaQueryWrapper<DeyochRolePermission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochRolePermission::getRoleId, roleId);
            List<DeyochRolePermission> rolePermList = deyochRolePermissionMapper.selectList(queryWrapper);

            // 转换为权限ID列表
            List<Long> permIds = new ArrayList<>();
            for (DeyochRolePermission rolePerm : rolePermList) {
                permIds.add(rolePerm.getPermId());
            }

            return Result.success(permIds);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取角色权限失败：" + e.getMessage());
        }
    }
}