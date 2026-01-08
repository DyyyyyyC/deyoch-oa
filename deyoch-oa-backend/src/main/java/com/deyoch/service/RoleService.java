package com.deyoch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deyoch.entity.DeyochRole;
import com.deyoch.common.result.PageResult;
import com.deyoch.common.result.Result;

import java.util.List;

/**
 * 角色管理服务接口
 * 定义角色相关的业务逻辑方法
 */
public interface RoleService extends IService<DeyochRole> {

    /**
     * 获取角色列表（分页）
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词
     * @return 分页角色列表
     */
    Result<PageResult<DeyochRole>> getRoleList(Integer page, Integer size, String keyword);

    /**
     * 根据ID获取角色详情
     * @param id 角色ID
     * @return 角色详情
     */
    Result<DeyochRole> getRoleById(Long id);

    /**
     * 创建角色
     * @param role 角色信息
     * @return 创建结果
     */
    Result<DeyochRole> createRole(DeyochRole role);

    /**
     * 更新角色信息
     * @param role 角色信息
     * @return 更新结果
     */
    Result<DeyochRole> updateRole(DeyochRole role);

    /**
     * 删除角色
     * @param id 角色ID
     * @return 删除结果
     */
    Result<Void> deleteRole(Long id);

    /**
     * 为角色分配权限
     * @param roleId 角色ID
     * @param permIds 权限ID列表
     * @return 分配结果
     */
    Result<Void> assignPermissions(Long roleId, List<Long> permIds);

    /**
     * 获取角色已分配的权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    Result<List<Long>> getRolePermIds(Long roleId);
}