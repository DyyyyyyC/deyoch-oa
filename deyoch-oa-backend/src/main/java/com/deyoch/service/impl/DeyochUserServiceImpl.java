package com.deyoch.service.impl;

import com.deyoch.entity.DeyochUser;
import com.deyoch.mapper.DeyochUserMapper;
import com.deyoch.service.DeyochUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 * 实现用户相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Service
public class DeyochUserServiceImpl extends ServiceImpl<DeyochUserMapper, DeyochUser> implements DeyochUserService {

    /**
     * 密码加密工具（实际项目中应使用BCrypt或MD5）
     */
    private static final String DEFAULT_PASSWORD = "123456"; // 默认密码

    // ==================== 查询方法实现 ====================

    /**
     * 根据用户名查询用户信息
     * 
     * @param username 用户名
     * @return 查询到的用户对象，未找到返回null
     */
    @Override
    public DeyochUser findByUsername(String username) {
        return lambdaQuery()
                .eq(DeyochUser::getUsername, username)
                .one();
    }

    /**
     * 根据邮箱查询用户信息
     * 
     * @param email 邮箱地址
     * @return 查询到的用户对象，未找到返回null
     */
    @Override
    public DeyochUser findByEmail(String email) {
        return lambdaQuery()
                .eq(DeyochUser::getEmail, email)
                .one();
    }

    /**
     * 根据用户ID查询用户信息（包含角色和部门信息）
     * 
     * @param userId 用户ID
     * @return 查询到的用户对象，未找到返回null
     */
    @Override
    public DeyochUser findByIdWithDetails(Long userId) {
        return lambdaQuery()
                .eq(DeyochUser::getId, userId)
                .one();
    }

    /**
     * 分页查询用户列表
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    @Override
    public Page<DeyochUser> findPage(int page, int size) {
        Page<DeyochUser> pageParam = new Page<>(page, size);
        return lambdaQuery()
                .orderByDesc(DeyochUser::getCreatedAt)
                .page(pageParam);
    }

    /**
     * 条件分页查询用户列表
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @param username 用户名（模糊查询）
     * @param nickname 昵称（模糊查询）
     * @param status 状态
     * @param deptId 部门ID
     * @return 分页结果
     */
    @Override
    public Page<DeyochUser> findPageByCondition(int page, int size, String username, String nickname, Long status, Long deptId) {
        Page<DeyochUser> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<DeyochUser> queryWrapper = new LambdaQueryWrapper<DeyochUser>();
        queryWrapper.like(StringUtils.hasText(username), DeyochUser::getUsername, username)
                   .like(StringUtils.hasText(nickname), DeyochUser::getNickname, nickname)
                   .eq(status != null, DeyochUser::getStatus, status)
                   .eq(deptId != null, DeyochUser::getDeptId, deptId)
                   .orderByDesc(DeyochUser::getCreatedAt);
        
        return this.page(pageParam, queryWrapper);
    }

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 存在返回true，不存在返回false
     */
    @Override
    public boolean existsByUsername(String username) {
        return lambdaQuery()
                .eq(DeyochUser::getUsername, username)
                .count() > 0;
    }

    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱地址
     * @return 存在返回true，不存在返回false
     */
    @Override
    public boolean existsByEmail(String email) {
        return lambdaQuery()
                .eq(DeyochUser::getEmail, email)
                .count() > 0;
    }

    /**
     * 根据用户ID查询角色ID
     * 
     * @param userId 用户ID
     * @return 角色ID，未找到返回null
     */
    @Override
    public Long findRoleIdByUserId(Long userId) {
        DeyochUser user = lambdaQuery()
                .eq(DeyochUser::getId, userId)
                .one();
        return user != null ? user.getRoleId() : null;
    }

    /**
     * 根据用户ID查询角色编码
     * 
     * @param userId 用户ID
     * @return 角色编码，未找到返回null
     */
    @Override
    public String findRoleCodeByUserId(Long userId) {
        DeyochUser user = lambdaQuery()
                .eq(DeyochUser::getId, userId)
                .one();
        // 注意：这里需要关联查询角色表获取角色编码
        // 后续可以在Service层通过RoleService获取
        return user != null ? String.valueOf(user.getRoleId()) : null;
    }

    /**
     * 根据用户ID查询部门ID
     * 
     * @param userId 用户ID
     * @return 部门ID，未找到返回null
     */
    @Override
    public Long findDeptIdByUserId(Long userId) {
        DeyochUser user = lambdaQuery()
                .eq(DeyochUser::getId, userId)
                .one();
        return user != null ? user.getDeptId() : null;
    }

    // ==================== 新增方法实现 ====================

    /**
     * 创建新用户（密码加密）
     * 
     * @param user 用户对象
     * @param rawPassword 原始密码（未加密）
     * @return 创建成功返回true，用户名已存在返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(DeyochUser user, String rawPassword) {
        // 检查用户名是否已存在
        if (existsByUsername(user.getUsername())) {
            return false;
        }
        
        // 设置默认密码（如果未提供）
        if (!StringUtils.hasText(rawPassword)) {
            rawPassword = DEFAULT_PASSWORD;
        }
        
        // 加密密码（实际项目应使用BCrypt）
        user.setPassword(encryptPassword(rawPassword));
        
        // 设置默认值
        user.setStatus(1L); // 默认启用
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // 保存用户
        return save(user);
    }

    /**
     * 批量创建用户
     * 
     * @param users 用户列表
     * @return 创建成功返回true
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUsers(List<DeyochUser> users) {
        for (DeyochUser user : users) {
            // 检查用户名是否已存在
            if (existsByUsername(user.getUsername())) {
                continue; // 跳过已存在的用户
            }
            
            // 设置默认密码
            user.setPassword(encryptPassword(DEFAULT_PASSWORD));
            user.setStatus(1L);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
        }
        
        return saveBatch(users);
    }

    // ==================== 修改方法实现 ====================

    /**
     * 更新用户信息
     * 
     * @param user 用户对象（包含ID）
     * @return 更新成功返回true，用户不存在返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(DeyochUser user) {
        // 检查用户是否存在
        DeyochUser existingUser = lambdaQuery()
                .eq(DeyochUser::getId, user.getId())
                .one();
        
        if (existingUser == null) {
            return false;
        }
        
        // 更新时间
        user.setUpdatedAt(LocalDateTime.now());
        
        // 保留原密码（不更新密码字段）
        user.setPassword(existingUser.getPassword());
        
        return updateById(user);
    }

    /**
     * 更新用户状态
     * 
     * @param userId 用户ID
     * @param status 新状态：0-禁用，1-启用
     * @return 更新成功返回true，用户不存在返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserStatus(Long userId, Long status) {
        DeyochUser user = lambdaQuery()
                .eq(DeyochUser::getId, userId)
                .one();
        
        if (user == null) {
            return false;
        }
        
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        return updateById(user);
    }

    /**
     * 重置用户密码
     * 
     * @param userId 用户ID
     * @param newPassword 新密码（未加密）
     * @return 重置成功返回true，用户不存在返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long userId, String newPassword) {
        DeyochUser user = lambdaQuery()
                .eq(DeyochUser::getId, userId)
                .one();
        
        if (user == null) {
            return false;
        }
        
        user.setPassword(encryptPassword(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        return updateById(user);
    }

    /**
     * 修改用户密码
     * 
     * @param userId 用户ID
     * @param oldPassword 旧密码（未加密）
     * @param newPassword 新密码（未加密）
     * @return 修改成功返回true，密码错误返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        DeyochUser user = lambdaQuery()
                .eq(DeyochUser::getId, userId)
                .one();
        
        if (user == null) {
            return false;
        }
        
        // 验证旧密码
        if (!verifyPassword(user.getUsername(), oldPassword)) {
            return false;
        }
        
        // 更新新密码
        user.setPassword(encryptPassword(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        return updateById(user);
    }

    /**
     * 更新用户角色
     * 
     * @param userId 用户ID
     * @param roleId 新角色ID
     * @return 更新成功返回true
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserRole(Long userId, Long roleId) {
        DeyochUser user = lambdaQuery()
                .eq(DeyochUser::getId, userId)
                .one();
        
        if (user == null) {
            return false;
        }
        
        user.setRoleId(roleId);
        user.setUpdatedAt(LocalDateTime.now());
        return updateById(user);
    }

    /**
     * 更新用户部门
     * 
     * @param userId 用户ID
     * @param deptId 新部门ID
     * @return 更新成功返回true
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserDept(Long userId, Long deptId) {
        DeyochUser user = lambdaQuery()
                .eq(DeyochUser::getId, userId)
                .one();
        
        if (user == null) {
            return false;
        }
        
        user.setDeptId(deptId);
        user.setUpdatedAt(LocalDateTime.now());
        return updateById(user);
    }

    // ==================== 删除方法实现 ====================

    /**
     * 根据ID删除用户（逻辑删除）
     * 
     * @param userId 用户ID
     * @return 删除成功返回true，用户不存在返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        DeyochUser user = lambdaQuery()
                .eq(DeyochUser::getId, userId)
                .one();
        
        if (user == null) {
            return false;
        }
        
        // 逻辑删除：将状态设为0（禁用）
        user.setStatus(0L);
        user.setUpdatedAt(LocalDateTime.now());
        return updateById(user);
    }

    /**
     * 批量删除用户（逻辑删除）
     * 
     * @param userIds 用户ID列表
     * @return 删除成功返回true
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUsers(List<Long> userIds) {
        List<DeyochUser> users = lambdaQuery()
                .in(DeyochUser::getId, userIds)
                .list();
        
        if (users.isEmpty()) {
            return true;
        }
        
        for (DeyochUser user : users) {
            user.setStatus(0L);
            user.setUpdatedAt(LocalDateTime.now());
        }
        
        return updateBatchById(users);
    }

    /**
     * 彻底删除用户（物理删除）
     * 
     * @param userId 用户ID
     * @return 删除成功返回true，用户不存在返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeUserCompletely(Long userId) {
        return removeById(userId);
    }

    // ==================== 验证方法实现 ====================

    /**
     * 验证用户密码
     * 
     * @param username 用户名
     * @param rawPassword 原始密码（未加密）
     * @return 验证通过返回true，密码错误返回false
     */
    @Override
    public boolean verifyPassword(String username, String rawPassword) {
        DeyochUser user = findByUsername(username);
        
        if (user == null) {
            return false;
        }
        
        // 验证密码（实际项目应使用BCrypt加密验证）
        return user.getPassword().equals(encryptPassword(rawPassword));
    }

    // ==================== 私有方法 ====================

    /**
     * 密码加密（实际项目应使用BCrypt）
     * 
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    private String encryptPassword(String rawPassword) {
        // TODO: 实际项目应使用BCrypt加密
        // return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        return rawPassword; // 开发环境直接返回原文，生产环境必须加密
    }
}