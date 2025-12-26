package com.deyoch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deyoch.entity.DeyochUser;
import com.deyoch.mapper.DeyochUserMapper;
import com.deyoch.result.Result;
import com.deyoch.result.ResultCode;
import com.deyoch.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 * 实现用户管理相关的业务逻辑
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final DeyochUserMapper deyochUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Result<List<DeyochUser>> getUserList() {
        try {
            List<DeyochUser> userList = deyochUserMapper.selectList(null);
            return Result.success(userList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取用户列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochUser> getUserById(Long id) {
        try {
            DeyochUser user = deyochUserMapper.selectById(id);
            if (user == null) {
                return Result.error(ResultCode.USER_NOT_FOUND, "用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取用户详情失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochUser> createUser(DeyochUser user) {
        try {
            // 检查用户名是否已存在
            LambdaQueryWrapper<DeyochUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochUser::getUsername, user.getUsername());
            DeyochUser existingUser = deyochUserMapper.selectOne(queryWrapper);
            if (existingUser != null) {
                return Result.error(ResultCode.USERNAME_EXIST, "用户名已存在");
            }

            // 密码加密
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // 设置创建时间和更新时间
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            // 默认状态为启用
            if (user.getStatus() == null) {
                user.setStatus(1L);
            }

            // 创建用户
            deyochUserMapper.insert(user);
            
            // 隐藏密码
            user.setPassword(null);
            
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "创建用户失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochUser> updateUser(DeyochUser user) {
        try {
            // 检查用户是否存在
            DeyochUser existingUser = deyochUserMapper.selectById(user.getId());
            if (existingUser == null) {
                return Result.error(ResultCode.USER_NOT_FOUND, "用户不存在");
            }

            // 如果更新了用户名，检查是否已存在
            if (!existingUser.getUsername().equals(user.getUsername())) {
                LambdaQueryWrapper<DeyochUser> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(DeyochUser::getUsername, user.getUsername());
                queryWrapper.ne(DeyochUser::getId, user.getId());
                if (deyochUserMapper.selectOne(queryWrapper) != null) {
                    return Result.error(ResultCode.USERNAME_EXIST, "用户名已存在");
                }
            }

            // 如果更新了密码，进行加密
            if (user.getPassword() != null && !user.getPassword().equals(existingUser.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                // 密码未变更，使用原密码
                user.setPassword(existingUser.getPassword());
            }

            // 设置更新时间
            user.setUpdatedAt(LocalDateTime.now());

            // 更新用户
            deyochUserMapper.updateById(user);
            
            // 隐藏密码
            user.setPassword(null);
            
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新用户失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteUser(Long id) {
        try {
            // 检查用户是否存在
            DeyochUser existingUser = deyochUserMapper.selectById(id);
            if (existingUser == null) {
                return Result.error(ResultCode.USER_NOT_FOUND, "用户不存在");
            }

            // 不能删除自己
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                if (existingUser.getUsername().equals(userDetails.getUsername())) {
                    return Result.error(ResultCode.OPERATION_NOT_ALLOWED, "不能删除当前登录用户");
                }
            }

            // 删除用户
            deyochUserMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "删除用户失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> updateUserStatus(Long id, Long status) {
        try {
            // 检查用户是否存在
            DeyochUser existingUser = deyochUserMapper.selectById(id);
            if (existingUser == null) {
                return Result.error(ResultCode.USER_NOT_FOUND, "用户不存在");
            }

            // 检查状态值是否合法
            if (status != 0 && status != 1) {
                return Result.error(ResultCode.PARAM_ERROR, "状态值不合法，只能是0或1");
            }

            // 不能禁用自己
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                if (existingUser.getUsername().equals(userDetails.getUsername()) && status == 0) {
                    return Result.error(ResultCode.OPERATION_NOT_ALLOWED, "不能禁用当前登录用户");
                }
            }

            // 更新用户状态
            DeyochUser user = new DeyochUser();
            user.setId(id);
            user.setStatus(status);
            user.setUpdatedAt(LocalDateTime.now());
            deyochUserMapper.updateById(user);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新用户状态失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochUser> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return Result.error(ResultCode.UNAUTHORIZED, "未登录");
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            // 查询用户信息
            LambdaQueryWrapper<DeyochUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochUser::getUsername, username);
            DeyochUser user = deyochUserMapper.selectOne(queryWrapper);

            if (user == null) {
                return Result.error(ResultCode.USER_NOT_FOUND, "用户不存在");
            }

            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取当前用户失败：" + e.getMessage());
        }
    }
}