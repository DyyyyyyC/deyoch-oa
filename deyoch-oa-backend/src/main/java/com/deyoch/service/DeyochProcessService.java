package com.deyoch.service;

import com.deyoch.entity.DeyochProcess;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 流程服务接口
 * 提供流程相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
public interface DeyochProcessService extends IService<DeyochProcess> {
    
    /**
     * 根据流程标识查询流程信息
     * 
     * @param processKey 流程标识
     * @return 查询到的流程对象，未找到返回null
     */
    DeyochProcess findByProcessKey(String processKey);
    
    /**
     * 检查流程标识是否存在
     * 
     * @param processKey 流程标识
     * @return 存在返回true，不存在返回false
     */
    boolean existsByProcessKey(String processKey);
    
    /**
     * 查询所有启用的流程列表
     * 
     * @return 流程列表
     */
    List<DeyochProcess> findAllEnabled();
}