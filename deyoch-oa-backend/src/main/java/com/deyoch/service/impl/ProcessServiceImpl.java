package com.deyoch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deyoch.entity.DeyochProcess;
import com.deyoch.mapper.DeyochProcessMapper;
import com.deyoch.common.result.Result;
import com.deyoch.common.result.ResultCode;
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
public class ProcessServiceImpl extends ServiceImpl<DeyochProcessMapper, DeyochProcess> implements ProcessService {

    @Override
    public Result<List<DeyochProcess>> getProcessList() {
        // 查询所有流程，按创建时间倒序排列
        LambdaQueryWrapper<DeyochProcess> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(DeyochProcess::getCreatedAt);
        List<DeyochProcess> processList = list(queryWrapper);
        return Result.success(processList);
    }

    @Override
    public Result<DeyochProcess> getProcessById(Long id) {
        DeyochProcess process = getById(id);
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
        process.setStatus(1);
        // 创建流程
        save(process);
        return Result.success(process);
    }

    @Override
    public Result<DeyochProcess> updateProcess(DeyochProcess process) {
        // 检查流程是否存在
        DeyochProcess existingProcess = getById(process.getId());
        if (existingProcess == null) {
            return Result.error(ResultCode.PROCESS_NOT_FOUND, "流程不存在");
        }
        // 设置更新时间
        process.setUpdatedAt(LocalDateTime.now());
        // 更新流程
        updateById(process);
        return Result.success(process);
    }

    @Override
    public Result<Void> deleteProcess(Long id) {
        // 检查流程是否存在
        DeyochProcess process = getById(id);
        if (process == null) {
            return Result.error(ResultCode.PROCESS_NOT_FOUND, "流程不存在");
        }
        // 删除流程
        removeById(id);
        return Result.success();
    }
}
