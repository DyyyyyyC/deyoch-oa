package com.deyoch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deyoch.entity.DeyochProcess;
import com.deyoch.mapper.DeyochProcessMapper;
import com.deyoch.result.Result;
import com.deyoch.result.ResultCode;
import com.deyoch.service.ProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程管理服务实现类
 * 实现流程管理相关的业务逻辑
 */
@Service
@RequiredArgsConstructor
public class ProcessServiceImpl implements ProcessService {

    private final DeyochProcessMapper deyochProcessMapper;

    @Override
    public Result<List<DeyochProcess>> getProcessList() {
        // 查询所有流程，按创建时间倒序排列
        LambdaQueryWrapper<DeyochProcess> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(DeyochProcess::getCreatedAt);
        List<DeyochProcess> processList = deyochProcessMapper.selectList(queryWrapper);
        return Result.success(processList);
    }

    @Override
    public Result<DeyochProcess> getProcessById(Long id) {
        DeyochProcess process = deyochProcessMapper.selectById(id);
        if (process == null) {
            return Result.error(ResultCode.PROCESS_NOT_FOUND, "流程不存在");
        }
        return Result.success(process);
    }

    @Override
    public Result<DeyochProcess> createProcess(DeyochProcess process) {
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        process.setCreatedAt(now);
        process.setUpdatedAt(now);
        // 默认状态为启用
        process.setStatus(1L);
        // 创建流程
        deyochProcessMapper.insert(process);
        return Result.success(process);
    }

    @Override
    public Result<DeyochProcess> updateProcess(DeyochProcess process) {
        // 检查流程是否存在
        DeyochProcess existingProcess = deyochProcessMapper.selectById(process.getId());
        if (existingProcess == null) {
            return Result.error(ResultCode.PROCESS_NOT_FOUND, "流程不存在");
        }
        // 设置更新时间
        process.setUpdatedAt(LocalDateTime.now());
        // 更新流程
        deyochProcessMapper.updateById(process);
        return Result.success(process);
    }

    @Override
    public Result<Void> deleteProcess(Long id) {
        // 检查流程是否存在
        DeyochProcess process = deyochProcessMapper.selectById(id);
        if (process == null) {
            return Result.error(ResultCode.PROCESS_NOT_FOUND, "流程不存在");
        }
        // 删除流程
        deyochProcessMapper.deleteById(id);
        return Result.success();
    }
}
