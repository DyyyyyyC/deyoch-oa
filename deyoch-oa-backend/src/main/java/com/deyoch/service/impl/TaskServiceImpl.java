package com.deyoch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deyoch.entity.DeyochTask;
import com.deyoch.entity.DeyochUser;
import com.deyoch.mapper.DeyochTaskMapper;
import com.deyoch.mapper.DeyochUserMapper;
import com.deyoch.result.Result;
import com.deyoch.result.ResultCode;
import com.deyoch.service.TaskService;
import com.deyoch.service.UserService;
import com.deyoch.utils.JwtUtil;
import com.deyoch.utils.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 任务管理服务实现类
 * 实现任务管理相关的业务逻辑
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final DeyochTaskMapper deyochTaskMapper;
    private final DeyochUserMapper deyochUserMapper;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    public Result<List<DeyochTask>> getTaskList() {
        try {
            // 查询所有任务，按创建时间倒序排列
            LambdaQueryWrapper<DeyochTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(DeyochTask::getCreatedAt);
            List<DeyochTask> taskList = deyochTaskMapper.selectList(queryWrapper);
            
            // 批量获取用户信息，减少数据库查询次数
            if (!taskList.isEmpty()) {
                // 收集所有唯一的用户ID
                Set<Long> userIds = new HashSet<>();
                for (DeyochTask task : taskList) {
                    if (task.getCreatorId() != null) {
                        userIds.add(task.getCreatorId());
                    }
                    if (task.getAssigneeId() != null) {
                        userIds.add(task.getAssigneeId());
                    }
                }
                
                // 批量获取用户信息
                if (!userIds.isEmpty()) {
                    // 构建用户ID到用户名的映射
                    Map<Long, String> userIdToUsernameMap = new HashMap<>();
                    
                    // 查询所有相关用户
                    List<DeyochUser> users = deyochUserMapper.selectBatchIds(userIds);
                    for (DeyochUser user : users) {
                        userIdToUsernameMap.put(user.getId(), user.getUsername());
                    }
                    
                    // 为每个任务设置用户名
                    for (DeyochTask task : taskList) {
                        // 设置创建者用户名
                        if (task.getCreatorId() != null) {
                            String creatorName = userIdToUsernameMap.getOrDefault(task.getCreatorId(), "未知用户");
                            task.setCreatorName(creatorName);
                        }
                        
                        // 设置负责人用户名
                        if (task.getAssigneeId() != null) {
                            String assigneeName = userIdToUsernameMap.getOrDefault(task.getAssigneeId(), "未知用户");
                            task.setAssigneeName(assigneeName);
                        }
                    }
                }
            }
            
            return Result.success(taskList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取任务列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochTask> getTaskById(Long id) {
        try {
            DeyochTask task = deyochTaskMapper.selectById(id);
            if (task == null) {
                return Result.error(ResultCode.TASK_NOT_FOUND, "任务不存在");
            }
            return Result.success(task);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取任务详情失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochTask> createTask(DeyochTask task) {
        try {
            // 从JWT token中获取用户ID
            Long userId = UserContextUtil.getUserIdFromToken(jwtUtil);
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "未登录或无效的令牌，无法创建任务");
            }
            
            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            task.setCreatedAt(now);
            task.setUpdatedAt(now);
            // 设置创建人ID
            task.setCreatorId(userId);
            // 默认状态为待分配
            task.setStatus(0L);
            // 创建任务
            deyochTaskMapper.insert(task);
            return Result.success(task);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "创建任务失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochTask> updateTask(DeyochTask task) {
        try {
            // 检查任务是否存在
            DeyochTask existingTask = deyochTaskMapper.selectById(task.getId());
            if (existingTask == null) {
                return Result.error(ResultCode.TASK_NOT_FOUND, "任务不存在");
            }
            // 设置更新时间
            task.setUpdatedAt(LocalDateTime.now());
            // 更新任务
            deyochTaskMapper.updateById(task);
            return Result.success(task);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新任务失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteTask(Long id) {
        try {
            // 检查任务是否存在
            DeyochTask task = deyochTaskMapper.selectById(id);
            if (task == null) {
                return Result.error(ResultCode.TASK_NOT_FOUND, "任务不存在");
            }
            // 删除任务
            deyochTaskMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "删除任务失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> assignTask(Long id, Long assigneeId) {
        try {
            // 检查任务是否存在
            DeyochTask task = deyochTaskMapper.selectById(id);
            if (task == null) {
                return Result.error(ResultCode.TASK_NOT_FOUND, "任务不存在");
            }
            // 更新被分配人ID
            task.setAssigneeId(assigneeId);
            // 更新状态为已分配
            task.setStatus(1L);
            // 设置更新时间
            task.setUpdatedAt(LocalDateTime.now());
            // 更新任务
            deyochTaskMapper.updateById(task);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "分配任务失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> updateTaskStatus(Long id, Long status) {
        try {
            // 检查任务是否存在
            DeyochTask task = deyochTaskMapper.selectById(id);
            if (task == null) {
                return Result.error(ResultCode.TASK_NOT_FOUND, "任务不存在");
            }
            // 检查状态值是否合法
            if (status < 0 || status > 3) {
                return Result.error(ResultCode.PARAM_ERROR, "状态值不合法，只能是0-3");
            }
            // 更新状态
            task.setStatus(status);
            // 设置更新时间
            task.setUpdatedAt(LocalDateTime.now());
            // 更新任务
            deyochTaskMapper.updateById(task);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新任务状态失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<DeyochTask>> getTasksByStatus(Long status) {
        try {
            // 根据状态查询任务，按创建时间倒序排列
            LambdaQueryWrapper<DeyochTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochTask::getStatus, status);
            queryWrapper.orderByDesc(DeyochTask::getCreatedAt);
            List<DeyochTask> taskList = deyochTaskMapper.selectList(queryWrapper);
            return Result.success(taskList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取任务列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<DeyochTask>> getTasksByPriority(Long priority) {
        try {
            // 根据优先级查询任务，按创建时间倒序排列
            LambdaQueryWrapper<DeyochTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochTask::getPriority, priority);
            queryWrapper.orderByDesc(DeyochTask::getCreatedAt);
            List<DeyochTask> taskList = deyochTaskMapper.selectList(queryWrapper);
            return Result.success(taskList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取任务列表失败：" + e.getMessage());
        }
    }
}
