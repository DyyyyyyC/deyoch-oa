package com.deyoch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deyoch.entity.DeyochUser;
import com.deyoch.common.result.PageResult;
import com.deyoch.common.result.Result;

/**
 * 用户服务接口
 * 定义用户管理相关的业务逻辑方法
 */
public interface UserService extends IService<DeyochUser> {

    /**
     * 获取用户列表（分页）
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词
     * @return 分页用户列表
     */
    Result<PageResult<DeyochUser>> getUserList(Integer page, Integer size, String keyword);

    /**
     * 根据ID获取用户详情
     * @param id 用户ID
     * @return 用户详情
     */
    Result<DeyochUser> getUserById(Long id);

    /**
     * 创建用户
     * @param user 用户信息
     * @return 创建结果
     */
    Result<DeyochUser> createUser(DeyochUser user);

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新结果
     */
    Result<DeyochUser> updateUser(DeyochUser user);

    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除结果
     */
    Result<Void> deleteUser(Long id);

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 用户状态（1-启用，0-禁用）
     * @return 更新结果
     */
    Result<Void> updateUserStatus(Long id, Integer status);

    /**
     * 获取当前登录用户信息
     * @return 当前用户信息
     */
    Result<DeyochUser> getCurrentUser();
    
    /**
     * 更新当前用户信息
     * @param user 用户信息
     * @return 更新结果
     */
    Result<DeyochUser> updateCurrentUser(DeyochUser user);
    
    /**
     * 修改当前用户密码
     * @param currentPassword 当前密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    Result<Void> changePassword(String currentPassword, String newPassword);
    
    /**
     * 根据用户ID获取用户名
     * @param userId 用户ID
     * @return 用户名
     */
    String getUsernameById(Long userId);
}