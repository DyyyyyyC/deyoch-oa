package com.deyoch.service;

import com.deyoch.entity.DeyochTask;
import com.deyoch.result.Result;

import java.util.List;

/**
 * 任务管理服务接口
 * 定义任务管理相关的业务逻辑方法
 */
public interface TaskService {

    /**
     * 获取任务列表
     * @return 任务列表
     */
    Result<List<DeyochTask>> getTaskList();

    /**
     * 根据ID获取任务详情
     * @param id 任务ID
     * @return 任务详情
     */
    Result<DeyochTask> getTaskById(Long id);

    /**
     * 创建任务
     * @param task 任务信息
     * @return 创建结果
     */
    Result<DeyochTask> createTask(DeyochTask task);

    /**
     * 更新任务信息
     * @param task 任务信息
     * @return 更新结果
     */
    Result<DeyochTask> updateTask(DeyochTask task);

    /**
     * 删除任务
     * @param id 任务ID
     * @return 删除结果
     */
    Result<Void> deleteTask(Long id);

    /**
     * 分配任务
     * @param id 任务ID
     * @param assigneeId 被分配人ID
     * @return 分配结果
     */
    Result<Void> assignTask(Long id, Long assigneeId);

    /**
     * 更新任务状态
     * @param id 任务ID
     * @param status 任务状态
     * @return 更新结果
     */
    Result<Void> updateTaskStatus(Long id, Long status);

    /**
     * 根据任务状态获取任务列表
     * @param status 任务状态
     * @return 任务列表
     */
    Result<List<DeyochTask>> getTasksByStatus(Long status);

    /**
     * 根据任务优先级获取任务列表
     * @param priority 任务优先级
     * @return 任务列表
     */
    Result<List<DeyochTask>> getTasksByPriority(Long priority);
}
