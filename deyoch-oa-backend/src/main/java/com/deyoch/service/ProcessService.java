package com.deyoch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deyoch.entity.DeyochProcess;
import com.deyoch.common.result.Result;

import java.util.List;

/**
 * 流程管理服务接口
 * 定义流程管理相关的业务逻辑方法
 */
public interface ProcessService extends IService<DeyochProcess> {

    /**
     * 获取流程列表
     * @return 流程列表
     */
    Result<List<DeyochProcess>> getProcessList();

    /**
     * 根据ID获取流程详情
     * @param id 流程ID
     * @return 流程详情
     */
    Result<DeyochProcess> getProcessById(Long id);

    /**
     * 创建流程
     * @param process 流程信息
     * @return 创建结果
     */
    Result<DeyochProcess> createProcess(DeyochProcess process);

    /**
     * 更新流程
     * @param process 流程信息
     * @return 更新结果
     */
    Result<DeyochProcess> updateProcess(DeyochProcess process);

    /**
     * 删除流程
     * @param id 流程ID
     * @return 删除结果
     */
    Result<Void> deleteProcess(Long id);
}
