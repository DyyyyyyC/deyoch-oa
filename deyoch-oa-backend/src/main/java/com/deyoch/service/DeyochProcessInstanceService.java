package com.deyoch.service;

import com.deyoch.entity.DeyochProcessInstance;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 流程实例服务接口
 * 提供流程实例相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
public interface DeyochProcessInstanceService extends IService<DeyochProcessInstance> {
    
    /**
     * 根据流程ID查询流程实例列表
     * 
     * @param processId 流程ID
     * @return 流程实例列表
     */
    List<DeyochProcessInstance> findByProcessId(Long processId);
    
    /**
     * 根据发起人查询流程实例列表
     * 
     * @param initiator 发起人
     * @return 流程实例列表
     */
    List<DeyochProcessInstance> findByInitiator(String initiator);
    
    /**
     * 根据状态查询流程实例列表
     * 
     * @param status 状态：0-运行中，1-已完成，2-已取消
     * @return 流程实例列表
     */
    List<DeyochProcessInstance> findByStatus(Long status);
    
    /**
     * 取消流程实例
     * 
     * @param id 流程实例ID
     * @return 取消成功返回true，失败返回false
     */
    boolean cancel(Long id);
    
    /**
     * 完成流程实例
     * 
     * @param id 流程实例ID
     * @return 完成成功返回true，失败返回false
     */
    boolean complete(Long id);
}