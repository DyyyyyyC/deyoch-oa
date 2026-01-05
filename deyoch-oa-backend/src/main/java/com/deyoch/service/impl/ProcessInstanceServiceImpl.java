package com.deyoch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deyoch.entity.DeyochProcessInstance;
import com.deyoch.mapper.DeyochProcessInstanceMapper;
import com.deyoch.result.Result;
import com.deyoch.result.ResultCode;
import com.deyoch.service.ProcessInstanceService;
import com.deyoch.utils.JwtUtil;
import com.deyoch.utils.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程实例管理服务实现类
 * 实现流程实例管理相关的业务逻辑
 */
@Service
@RequiredArgsConstructor
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

    private final DeyochProcessInstanceMapper deyochProcessInstanceMapper;
    private final JwtUtil jwtUtil;

    @Override
    public Result<List<DeyochProcessInstance>> getProcessInstanceList() {
        try {
            // 查询所有流程实例，按创建时间倒序排列
            LambdaQueryWrapper<DeyochProcessInstance> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(DeyochProcessInstance::getCreatedAt);
            List<DeyochProcessInstance> instanceList = deyochProcessInstanceMapper.selectList(queryWrapper);
            return Result.success(instanceList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取流程实例列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochProcessInstance> getProcessInstanceById(Long id) {
        try {
            DeyochProcessInstance instance = deyochProcessInstanceMapper.selectById(id);
            if (instance == null) {
                return Result.error(ResultCode.PROCESS_INSTANCE_NOT_FOUND, "流程实例不存在");
            }
            return Result.success(instance);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取流程实例详情失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochProcessInstance> createProcessInstance(DeyochProcessInstance instance) {
        try {
            // 从JWT token中获取用户ID
            Long userId = UserContextUtil.getUserIdFromToken(jwtUtil);
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "未登录或无效的令牌，无法创建流程实例");
            }
            
            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            instance.setCreatedAt(now);
            instance.setUpdatedAt(now);
            // 设置发起人ID
            instance.setUserId(userId);
            // 默认状态为待启动
            instance.setStatus(0L);
            // 创建流程实例
            deyochProcessInstanceMapper.insert(instance);
            return Result.success(instance);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "创建流程实例失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochProcessInstance> updateProcessInstance(DeyochProcessInstance instance) {
        try {
            // 检查流程实例是否存在
            DeyochProcessInstance existingInstance = deyochProcessInstanceMapper.selectById(instance.getId());
            if (existingInstance == null) {
                return Result.error(ResultCode.PROCESS_INSTANCE_NOT_FOUND, "流程实例不存在");
            }
            // 设置更新时间
            instance.setUpdatedAt(LocalDateTime.now());
            // 更新流程实例
            deyochProcessInstanceMapper.updateById(instance);
            return Result.success(instance);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新流程实例失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteProcessInstance(Long id) {
        try {
            // 检查流程实例是否存在
            DeyochProcessInstance instance = deyochProcessInstanceMapper.selectById(id);
            if (instance == null) {
                return Result.error(ResultCode.PROCESS_INSTANCE_NOT_FOUND, "流程实例不存在");
            }
            // 删除流程实例
            deyochProcessInstanceMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "删除流程实例失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> startProcessInstance(Long id) {
        try {
            // 检查流程实例是否存在
            DeyochProcessInstance instance = deyochProcessInstanceMapper.selectById(id);
            if (instance == null) {
                return Result.error(ResultCode.PROCESS_INSTANCE_NOT_FOUND, "流程实例不存在");
            }
            // 设置状态为运行中
            instance.setStatus(1L);
            // 设置启动时间
            instance.setStartTime(LocalDateTime.now());
            // 设置更新时间
            instance.setUpdatedAt(LocalDateTime.now());
            // 更新流程实例
            deyochProcessInstanceMapper.updateById(instance);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "启动流程实例失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> completeProcessInstance(Long id) {
        try {
            // 检查流程实例是否存在
            DeyochProcessInstance instance = deyochProcessInstanceMapper.selectById(id);
            if (instance == null) {
                return Result.error(ResultCode.PROCESS_INSTANCE_NOT_FOUND, "流程实例不存在");
            }
            // 设置状态为已完成
            instance.setStatus(2L);
            // 设置结束时间
            instance.setEndTime(LocalDateTime.now());
            // 设置更新时间
            instance.setUpdatedAt(LocalDateTime.now());
            // 更新流程实例
            deyochProcessInstanceMapper.updateById(instance);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "完成流程实例失败：" + e.getMessage());
        }
    }
}
