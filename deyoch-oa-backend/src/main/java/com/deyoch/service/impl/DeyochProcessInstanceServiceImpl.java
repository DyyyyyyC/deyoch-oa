package com.deyoch.service.impl;

import com.deyoch.entity.DeyochProcessInstance;
import com.deyoch.mapper.DeyochProcessInstanceMapper;
import com.deyoch.service.DeyochProcessInstanceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程实例服务实现类
 * 实现流程实例相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Service
public class DeyochProcessInstanceServiceImpl extends ServiceImpl<DeyochProcessInstanceMapper, DeyochProcessInstance> implements DeyochProcessInstanceService {

    /**
     * 根据流程ID查询流程实例列表
     * 
     * @param processId 流程ID
     * @return 流程实例列表
     */
    @Override
    public List<DeyochProcessInstance> findByProcessId(Long processId) {
        return lambdaQuery()
                .eq(DeyochProcessInstance::getProcessId, processId)
                .orderByDesc(DeyochProcessInstance::getCreatedAt)
                .list();
    }

    /**
     * 根据发起人查询流程实例列表
     * 
     * @param initiator 发起人
     * @return 流程实例列表
     */
    @Override
    public List<DeyochProcessInstance> findByInitiator(String initiator) {
        return lambdaQuery()
                .eq(DeyochProcessInstance::getInitiator, initiator)
                .orderByDesc(DeyochProcessInstance::getCreatedAt)
                .list();
    }

    /**
     * 根据状态查询流程实例列表
     * 
     * @param status 状态：0-运行中，1-已完成，2-已取消
     * @return 流程实例列表
     */
    @Override
    public List<DeyochProcessInstance> findByStatus(Long status) {
        return lambdaQuery()
                .eq(DeyochProcessInstance::getStatus, status)
                .orderByDesc(DeyochProcessInstance::getCreatedAt)
                .list();
    }

    /**
     * 取消流程实例
     * 
     * @param id 流程实例ID
     * @return 取消成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(Long id) {
        DeyochProcessInstance instance = lambdaQuery()
                .eq(DeyochProcessInstance::getId, id)
                .one();
        
        if (instance == null) {
            return false;
        }
        
        // 只有运行中的流程实例可以取消
        if (instance.getStatus() != 0) {
            return false;
        }
        
        instance.setStatus(2); // 已取消
        instance.setEndTime(LocalDateTime.now());
        return updateById(instance);
    }

    /**
     * 完成流程实例
     * 
     * @param id 流程实例ID
     * @return 完成成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean complete(Long id) {
        DeyochProcessInstance instance = lambdaQuery()
                .eq(DeyochProcessInstance::getId, id)
                .one();
        
        if (instance == null) {
            return false;
        }
        
        // 只有运行中的流程实例可以完成
        if (instance.getStatus() != 0) {
            return false;
        }
        
        instance.setStatus(1); // 已完成
        instance.setEndTime(LocalDateTime.now());
        return updateById(instance);
    }
}