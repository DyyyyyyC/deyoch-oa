package com.deyoch.service.impl;

import com.deyoch.entity.DeyochTask;
import com.deyoch.mapper.DeyochTaskMapper;
import com.deyoch.service.DeyochTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 任务服务实现类
 * 实现任务相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Service
public class DeyochTaskServiceImpl extends ServiceImpl<DeyochTaskMapper, DeyochTask> implements DeyochTaskService {

    /**
     * 根据负责人查询任务列表
     * 
     * @param assignee 负责人
     * @return 任务列表
     */
    @Override
    public List<DeyochTask> findByAssignee(String assignee) {
        return lambdaQuery()
                .eq(DeyochTask::getAssignee, assignee)
                .orderByDesc(DeyochTask::getCreatedAt)
                .list();
    }

    /**
     * 根据状态查询任务列表
     * 
     * @param status 状态：0-未开始，1-进行中，2-已完成，3-已取消
     * @return 任务列表
     */
    @Override
    public List<DeyochTask> findByStatus(Long status) {
        return lambdaQuery()
                .eq(DeyochTask::getStatus, status)
                .orderByDesc(DeyochTask::getCreatedAt)
                .list();
    }

    /**
     * 根据优先级查询任务列表
     * 
     * @param priority 优先级：1-低，2-中，3-高
     * @return 任务列表
     */
    @Override
    public List<DeyochTask> findByPriority(Long priority) {
        return lambdaQuery()
                .eq(DeyochTask::getPriority, priority)
                .orderByDesc(DeyochTask::getCreatedAt)
                .list();
    }

    /**
     * 查询用户待办任务
     * 
     * @param username 用户名
     * @return 待办任务列表
     */
    @Override
    public List<DeyochTask> findTodoTasks(String username) {
        return lambdaQuery()
                .eq(DeyochTask::getAssignee, username)
                .in(DeyochTask::getStatus, 0, 1) // 未开始或进行中
                .orderByAsc(DeyochTask::getPriority) // 按优先级排序，高优先级优先
                .list();
    }

    /**
     * 查询用户已完成任务
     * 
     * @param username 用户名
     * @return 已完成任务列表
     */
    @Override
    public List<DeyochTask> findCompletedTasks(String username) {
        return lambdaQuery()
                .eq(DeyochTask::getAssignee, username)
                .eq(DeyochTask::getStatus, 2) // 已完成
                .orderByDesc(DeyochTask::getEndTime)
                .list();
    }
}