package com.deyoch.service.impl;

import com.deyoch.entity.DeyochProcess;
import com.deyoch.mapper.DeyochProcessMapper;
import com.deyoch.service.DeyochProcessService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 流程服务实现类
 * 实现流程相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Service
public class DeyochProcessServiceImpl extends ServiceImpl<DeyochProcessMapper, DeyochProcess> implements DeyochProcessService {

    /**
     * 根据流程标识查询流程信息
     * 
     * @param processKey 流程标识
     * @return 查询到的流程对象，未找到返回null
     */
    @Override
    public DeyochProcess findByProcessKey(String processKey) {
        return lambdaQuery()
                .eq(DeyochProcess::getProcessKey, processKey)
                .one();
    }

    /**
     * 检查流程标识是否存在
     * 
     * @param processKey 流程标识
     * @return 存在返回true，不存在返回false
     */
    @Override
    public boolean existsByProcessKey(String processKey) {
        return lambdaQuery()
                .eq(DeyochProcess::getProcessKey, processKey)
                .count() > 0;
    }

    /**
     * 查询所有启用的流程列表
     * 
     * @return 流程列表
     */
    @Override
    public List<DeyochProcess> findAllEnabled() {
        return lambdaQuery()
                .eq(DeyochProcess::getStatus, 1)
                .orderByAsc(DeyochProcess::getId)
                .list();
    }
}