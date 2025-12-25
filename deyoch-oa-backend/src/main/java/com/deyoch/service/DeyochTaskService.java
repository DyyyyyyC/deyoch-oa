package com.deyoch.service;

import com.deyoch.entity.DeyochTask;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 任务服务接口
 * 提供任务相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
public interface DeyochTaskService extends IService<DeyochTask> {
    
    /**
     * 根据负责人查询任务列表
     * 
     * @param assignee 负责人
     * @return 任务列表
     */
    List<DeyochTask> findByAssignee(String assignee);
    
    /**
     * 根据状态查询任务列表
     * 
     * @param status 状态：0-未开始，1-进行中，2-已完成，3-已取消
     * @return 任务列表
     */
    List<DeyochTask> findByStatus(Long status);
    
    /**
     * 根据优先级查询任务列表
     * 
     * @param priority 优先级：1-低，2-中，3-高
     * @return 任务列表
     */
    List<DeyochTask> findByPriority(Long priority);
    
    /**
     * 查询用户待办任务
     * 
     * @param username 用户名
     * @return 待办任务列表
     */
    List<DeyochTask> findTodoTasks(String username);
    
    /**
     * 查询用户已完成任务
     * 
     * @param username 用户名
     * @return 已完成任务列表
     */
    List<DeyochTask> findCompletedTasks(String username);
}