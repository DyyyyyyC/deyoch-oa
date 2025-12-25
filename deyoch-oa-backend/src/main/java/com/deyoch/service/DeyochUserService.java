package com.deyoch.service;

import com.deyoch.entity.DeyochUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 用户服务接口
 * 提供用户相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
public interface DeyochUserService extends IService<DeyochUser> {
    
    // ==================== 查询方法 ====================
    
    /**
     * 根据用户名查询用户信息
     * 
     * @param username 用户名
     * @return 查询到的用户对象，未找到返回null
     */
    DeyochUser findByUsername(String username);
    
    /**
     * 根据邮箱查询用户信息
     * 
     * @param email 邮箱地址
     * @return 查询到的用户对象，未找到返回null
     */
    DeyochUser findByEmail(String email);
    
    /**
     * 根据用户ID查询用户信息（包含角色和部门信息）
     * 
     * @param userId 用户ID
     * @return 查询到的用户对象，未找到返回null
     */
    DeyochUser findByIdWithDetails(Long userId);
    
    /**
     * 分页查询用户列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Page<DeyochUser> findPage(int page, int size);
    
    /**
     * 条件分页查询用户列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名（模糊查询）
     * @param nickname 昵称（模糊查询）
     * @param status 状态
     * @param deptId 部门ID
     * @return 分页结果
     */
    Page<DeyochUser> findPageByCondition(int page, int size, String username, String nickname, Long status, Long deptId);
    
    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 存在返回true，不存在返回false
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱地址
     * @return 存在返回true，不存在返回false
     */
    boolean existsByEmail(String email);
    
    /**
     * 根据用户ID查询角色ID
     * 
     * @param userId 用户ID
     * @return 角色ID，未找到返回null
     */
    Long findRoleIdByUserId(Long userId);
    
    /**
     * 根据用户ID查询角色编码
     * 
     * @param userId 用户ID
     * @return 角色编码，未找到返回null
     */
    String findRoleCodeByUserId(Long userId);
    
    /**
     * 根据用户ID查询部门ID
     * 
     * @param userId 用户ID
     * @return 部门ID，未找到返回null
     */
    Long findDeptIdByUserId(Long userId);
    
    // ==================== 新增方法 ====================
    
    /**
     * 创建新用户（密码加密）
     * 
     * @param user 用户对象
     * @param rawPassword 原始密码（未加密）
     * @return 创建成功返回true，用户名已存在返回false
     */
    boolean createUser(DeyochUser user, String rawPassword);
    
    /**
     * 批量创建用户
     * 
     * @param users 用户列表
     * @return 创建成功返回true
     */
    boolean createUsers(java.util.List<DeyochUser> users);
    
    // ==================== 修改方法 ====================
    
    /**
     * 更新用户信息
     * 
     * @param user 用户对象（包含ID）
     * @return 更新成功返回true，用户不存在返回false
     */
    boolean updateUser(DeyochUser user);
    
    /**
     * 更新用户状态
     * 
     * @param userId 用户ID
     * @param status 新状态：0-禁用，1-启用
     * @return 更新成功返回true，用户不存在返回false
     */
    boolean updateUserStatus(Long userId, Long status);
    
    /**
     * 重置用户密码
     * 
     * @param userId 用户ID
     * @param newPassword 新密码（未加密）
     * @return 重置成功返回true，用户不存在返回false
     */
    boolean resetPassword(Long userId, String newPassword);
    
    /**
     * 修改用户密码
     * 
     * @param userId 用户ID
     * @param oldPassword 旧密码（未加密）
     * @param newPassword 新密码（未加密）
     * @return 修改成功返回true，密码错误返回false
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 更新用户角色
     * 
     * @param userId 用户ID
     * @param roleId 新角色ID
     * @return 更新成功返回true
     */
    boolean updateUserRole(Long userId, Long roleId);
    
    /**
     * 更新用户部门
     * 
     * @param userId 用户ID
     * @param deptId 新部门ID
     * @return 更新成功返回true
     */
    boolean updateUserDept(Long userId, Long deptId);
    
    // ==================== 删除方法 ====================
    
    /**
     * 根据ID删除用户（逻辑删除）
     * 
     * @param userId 用户ID
     * @return 删除成功返回true，用户不存在返回false
     */
    boolean deleteUser(Long userId);
    
    /**
     * 批量删除用户（逻辑删除）
     * 
     * @param userIds 用户ID列表
     * @return 删除成功返回true
     */
    boolean deleteUsers(java.util.List<Long> userIds);
    
    /**
     * 彻底删除用户（物理删除）
     * 
     * @param userId 用户ID
     * @return 删除成功返回true，用户不存在返回false
     */
    boolean removeUserCompletely(Long userId);
    
    // ==================== 验证方法 ====================
    
    /**
     * 验证用户密码
     * 
     * @param username 用户名
     * @param rawPassword 原始密码（未加密）
     * @return 验证通过返回true，密码错误返回false
     */
    boolean verifyPassword(String username, String rawPassword);
}