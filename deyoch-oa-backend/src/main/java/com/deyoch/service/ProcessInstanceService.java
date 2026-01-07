package com.deyoch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deyoch.entity.DeyochProcessInstance;
import com.deyoch.result.Result;

import java.util.List;

/**
 * 流程实例管理服务接口
 * 定义流程实例管理相关的业务逻辑方法
 */
public interface ProcessInstanceService extends IService<DeyochProcessInstance> {

    /**
     * 获取流程实例列表
     * @return 流程实例列表
     */
    Result<List<DeyochProcessInstance>> getProcessInstanceList();

    /**
     * 根据ID获取流程实例详情
     * @param id 流程实例ID
     * @return 流程实例详情
     */
    Result<DeyochProcessInstance> getProcessInstanceById(Long id);

    /**
     * 创建流程实例
     * @param instance 流程实例信息
     * @return 创建结果
     */
    Result<DeyochProcessInstance> createProcessInstance(DeyochProcessInstance instance);

    /**
     * 更新流程实例
     * @param instance 流程实例信息
     * @return 更新结果
     */
    Result<DeyochProcessInstance> updateProcessInstance(DeyochProcessInstance instance);

    /**
     * 删除流程实例
     * @param id 流程实例ID
     * @return 删除结果
     */
    Result<Void> deleteProcessInstance(Long id);

    /**
     * 启动流程实例
     * @param id 流程实例ID
     * @return 启动结果
     */
    Result<Void> startProcessInstance(Long id);

    /**
     * 完成流程实例
     * @param id 流程实例ID
     * @return 完成结果
     */
    Result<Void> completeProcessInstance(Long id);
}
