package com.deyoch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deyoch.entity.DeyochTask;
import com.deyoch.mapper.DeyochTaskMapper;
import com.deyoch.common.result.PageResult;
import com.deyoch.common.result.Result;
import com.deyoch.common.result.ResultCode;
import com.deyoch.service.TaskService;
import com.deyoch.service.UserInfoConverter;
import com.deyoch.utils.JwtUtil;
import com.deyoch.utils.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 任务管理服务实现类
 * 继承ServiceImpl获得MyBatis Plus的基础CRUD能力
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl extends ServiceImpl<DeyochTaskMapper, DeyochTask> implements TaskService {

    private final JwtUtil jwtUtil;
    private final UserInfoConverter userInfoConverter;

    /**
     * 为任务列表填充用户名信息的通用方法
     * 提取重复代码，提高代码复用性
     */
    private void populateTaskUserNames(List<DeyochTask> taskList) {
        userInfoConverter.<DeyochTask>populateUserNames(
            taskList,
            // 用户ID提取器：从任务中提取creatorId和assigneeId
            task -> {
                Set<Long> userIds = new HashSet<>();
                if (task.getCreatorId() != null) {
                    userIds.add(task.getCreatorId());
                }
                if (task.getAssigneeId() != null) {
                    userIds.add(task.getAssigneeId());
                }
                return userIds;
            },
            // 用户名设置器：将用户名设置到creatorName和assigneeName字段
            (task, userIdToNameMap) -> {
                if (task.getCreatorId() != null) {
                    String creatorName = userIdToNameMap.get(task.getCreatorId());
                    task.setCreatorName(creatorName);
                }
                if (task.getAssigneeId() != null) {
                    String assigneeName = userIdToNameMap.get(task.getAssigneeId());
                    task.setAssigneeName(assigneeName);
                }
            }
        );
    }

    @Override
    public Result<PageResult<DeyochTask>> getTaskList(Integer page, Integer size, String keyword) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<DeyochTask> queryWrapper = new LambdaQueryWrapper<>();
            
            // 添加关键词搜索条件（搜索任务标题或内容）
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                    .like(DeyochTask::getTitle, keyword)
                    .or()
                    .like(DeyochTask::getContent, keyword)
                );
            }
            
            // 按创建时间倒序排列
            queryWrapper.orderByDesc(DeyochTask::getCreatedAt);
            
            // 创建分页对象
            Page<DeyochTask> pageObj = new Page<>(page, size);
            
            // 分页查询任务
            IPage<DeyochTask> taskPage = page(pageObj, queryWrapper);
            List<DeyochTask> taskList = taskPage.getRecords();
            
            // 填充用户名信息
            populateTaskUserNames(taskList);
            
            // 构建分页结果
            PageResult<DeyochTask> pageResult = PageResult.of(
                taskPage.getCurrent(),
                taskPage.getSize(),
                taskPage.getTotal(),
                taskList
            );
            
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取任务列表失败，请稍后重试");
        }
    }

    @Override
    public Result<DeyochTask> getTaskById(Long id) {
        try {
            DeyochTask task = getById(id);
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
            task.setStatus(0);
            // 创建任务
            save(task);
            return Result.success(task);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "创建任务失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochTask> updateTask(DeyochTask task) {
        try {
            // 检查任务是否存在
            DeyochTask existingTask = getById(task.getId());
            if (existingTask == null) {
                return Result.error(ResultCode.TASK_NOT_FOUND, "任务不存在");
            }
            // 设置更新时间
            task.setUpdatedAt(LocalDateTime.now());
            // 更新任务
            updateById(task);
            return Result.success(task);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新任务失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteTask(Long id) {
        try {
            // 检查任务是否存在
            DeyochTask task = getById(id);
            if (task == null) {
                return Result.error(ResultCode.TASK_NOT_FOUND, "任务不存在");
            }
            // 删除任务
            removeById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "删除任务失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> assignTask(Long id, Long assigneeId) {
        try {
            // 检查任务是否存在
            DeyochTask task = getById(id);
            if (task == null) {
                return Result.error(ResultCode.TASK_NOT_FOUND, "任务不存在");
            }
            // 更新被分配人ID
            task.setAssigneeId(assigneeId);
            // 更新状态为已分配
            task.setStatus(1);
            // 设置更新时间
            task.setUpdatedAt(LocalDateTime.now());
            // 更新任务
            updateById(task);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "分配任务失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> updateTaskStatus(Long id, Integer status) {
        try {
            // 检查任务是否存在
            DeyochTask task = getById(id);
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
            updateById(task);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新任务状态失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<DeyochTask>> getTasksByStatus(Integer status) {
        try {
            // 根据状态查询任务，按创建时间倒序排列
            LambdaQueryWrapper<DeyochTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochTask::getStatus, status);
            queryWrapper.orderByDesc(DeyochTask::getCreatedAt);
            List<DeyochTask> taskList = list(queryWrapper);
            
            // 填充用户名信息
            populateTaskUserNames(taskList);
            
            return Result.success(taskList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取任务列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<DeyochTask>> getTasksByPriority(Integer priority) {
        try {
            // 根据优先级查询任务，按创建时间倒序排列
            LambdaQueryWrapper<DeyochTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochTask::getPriority, priority);
            queryWrapper.orderByDesc(DeyochTask::getCreatedAt);
            List<DeyochTask> taskList = list(queryWrapper);
            
            // 填充用户名信息
            populateTaskUserNames(taskList);
            
            return Result.success(taskList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取任务列表失败：" + e.getMessage());
        }
    }

    // ==================== 工作台专用方法实现 ====================

    public Result<List<DeyochTask>> getDashboardTasks(Integer limit) {
        try {
            // 获取当前用户ID
            Long userId = UserContextUtil.getUserIdFromToken(jwtUtil);
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "未登录或无效的令牌");
            }

            // 构建查询条件：优先显示高优先级和进行中的任务
            LambdaQueryWrapper<DeyochTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.and(wrapper -> wrapper
                .eq(DeyochTask::getCreatorId, userId)
                .or()
                .eq(DeyochTask::getAssigneeId, userId)
            );
            // 优先显示待办和进行中的任务
            queryWrapper.in(DeyochTask::getStatus, 0, 1, 2);
            // 按优先级降序，创建时间降序排列
            queryWrapper.orderByDesc(DeyochTask::getPriority)
                       .orderByDesc(DeyochTask::getCreatedAt);
            // 限制数量
            queryWrapper.last("LIMIT " + limit);
            
            List<DeyochTask> taskList = list(queryWrapper);
            
            // 填充用户名信息
            populateTaskUserNames(taskList);
            
            return Result.success(taskList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取工作台任务失败：" + e.getMessage());
        }
    }

    public Result<List<DeyochTask>> getMyPendingTasks(Integer limit) {
        try {
            // 获取当前用户ID
            Long userId = UserContextUtil.getUserIdFromToken(jwtUtil);
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "未登录或无效的令牌");
            }

            // 查询分配给当前用户的待办任务（状态为0或1）
            LambdaQueryWrapper<DeyochTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochTask::getAssigneeId, userId);
            queryWrapper.in(DeyochTask::getStatus, 0, 1); // 待分配、已分配
            queryWrapper.orderByDesc(DeyochTask::getPriority)
                       .orderByDesc(DeyochTask::getCreatedAt);
            queryWrapper.last("LIMIT " + limit);
            
            List<DeyochTask> taskList = list(queryWrapper);
            
            // 填充用户名信息
            populateTaskUserNames(taskList);
            
            return Result.success(taskList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取待办任务失败：" + e.getMessage());
        }
    }

    public Result<List<DeyochTask>> getMyReviewTasks(Integer limit) {
        try {
            // 获取当前用户ID
            Long userId = UserContextUtil.getUserIdFromToken(jwtUtil);
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "未登录或无效的令牌");
            }

            // 查询当前用户创建的需要审阅的任务（状态为2进行中）
            LambdaQueryWrapper<DeyochTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochTask::getCreatorId, userId);
            queryWrapper.eq(DeyochTask::getStatus, 2); // 进行中，需要审阅
            queryWrapper.orderByDesc(DeyochTask::getPriority)
                       .orderByDesc(DeyochTask::getUpdatedAt);
            queryWrapper.last("LIMIT " + limit);
            
            List<DeyochTask> taskList = list(queryWrapper);
            
            // 填充用户名信息
            populateTaskUserNames(taskList);
            
            return Result.success(taskList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取待阅任务失败：" + e.getMessage());
        }
    }

    public Result<Object> getTaskStats() {
        try {
            // 获取当前用户ID
            Long userId = UserContextUtil.getUserIdFromToken(jwtUtil);
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "未登录或无效的令牌");
            }

            // 统计各状态任务数量
            LambdaQueryWrapper<DeyochTask> baseWrapper = new LambdaQueryWrapper<>();
            baseWrapper.and(wrapper -> wrapper
                .eq(DeyochTask::getCreatorId, userId)
                .or()
                .eq(DeyochTask::getAssigneeId, userId)
            );

            // 待办任务数量（状态0,1）
            LambdaQueryWrapper<DeyochTask> pendingWrapper = baseWrapper.clone();
            pendingWrapper.in(DeyochTask::getStatus, 0, 1);
            long pendingCount = count(pendingWrapper);

            // 待阅任务数量（状态2）
            LambdaQueryWrapper<DeyochTask> reviewWrapper = baseWrapper.clone();
            reviewWrapper.eq(DeyochTask::getStatus, 2);
            long reviewCount = count(reviewWrapper);

            // 已完成任务数量（状态3）
            LambdaQueryWrapper<DeyochTask> completedWrapper = baseWrapper.clone();
            completedWrapper.eq(DeyochTask::getStatus, 3);
            long completedCount = count(completedWrapper);

            // 逾期任务数量（截止时间小于当前时间且未完成）
            LambdaQueryWrapper<DeyochTask> overdueWrapper = baseWrapper.clone();
            overdueWrapper.lt(DeyochTask::getEndTime, LocalDateTime.now());
            overdueWrapper.ne(DeyochTask::getStatus, 3);
            long overdueCount = count(overdueWrapper);

            // 构建统计结果
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("pending", pendingCount);
            stats.put("pendingReview", reviewCount);
            stats.put("completed", completedCount);
            stats.put("overdue", overdueCount);
            stats.put("total", pendingCount + reviewCount + completedCount);

            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取任务统计失败：" + e.getMessage());
        }
    }
}
