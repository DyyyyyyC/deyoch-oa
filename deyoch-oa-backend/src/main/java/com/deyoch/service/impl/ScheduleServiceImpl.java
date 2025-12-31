package com.deyoch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deyoch.entity.DeyochSchedule;
import com.deyoch.mapper.DeyochScheduleMapper;
import com.deyoch.result.Result;
import com.deyoch.result.ResultCode;
import com.deyoch.service.ScheduleService;
import com.deyoch.utils.JwtUtil;
import com.deyoch.utils.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程管理服务实现类
 * 实现日程管理相关的业务逻辑
 */
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final DeyochScheduleMapper deyochScheduleMapper;
    private final JwtUtil jwtUtil;

    @Override
    public Result<List<DeyochSchedule>> getScheduleList() {
        try {
            // 查询所有日程，按开始时间升序排列
            LambdaQueryWrapper<DeyochSchedule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByAsc(DeyochSchedule::getStartTime);
            List<DeyochSchedule> scheduleList = deyochScheduleMapper.selectList(queryWrapper);
            return Result.success(scheduleList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取日程列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<DeyochSchedule>> getScheduleListByUserId(Long userId) {
        try {
            // 根据用户ID查询日程，按开始时间升序排列
            LambdaQueryWrapper<DeyochSchedule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochSchedule::getUserId, userId);
            queryWrapper.orderByAsc(DeyochSchedule::getStartTime);
            List<DeyochSchedule> scheduleList = deyochScheduleMapper.selectList(queryWrapper);
            return Result.success(scheduleList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取用户日程列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochSchedule> getScheduleById(Long id) {
        try {
            DeyochSchedule schedule = deyochScheduleMapper.selectById(id);
            if (schedule == null) {
                return Result.error(ResultCode.SCHEDULE_NOT_FOUND, "日程不存在");
            }
            return Result.success(schedule);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取日程详情失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochSchedule> createSchedule(DeyochSchedule schedule) {
        try {
            // 从JWT token中直接获取用户ID，无需查询数据库
            Long userId = UserContextUtil.getUserIdFromToken(jwtUtil);
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "未登录或无效的令牌，无法创建日程");
            }
            
            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            schedule.setCreatedAt(now);
            schedule.setUpdatedAt(now);
            // 设置用户ID
            schedule.setUserId(userId);
            // 默认状态为待开始
            schedule.setStatus(0L);
            // 创建日程
            deyochScheduleMapper.insert(schedule);
            return Result.success(schedule);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "创建日程失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochSchedule> updateSchedule(DeyochSchedule schedule) {
        try {
            // 检查日程是否存在
            DeyochSchedule existingSchedule = deyochScheduleMapper.selectById(schedule.getId());
            if (existingSchedule == null) {
                return Result.error(ResultCode.SCHEDULE_NOT_FOUND, "日程不存在");
            }
            // 设置更新时间
            schedule.setUpdatedAt(LocalDateTime.now());
            // 更新日程
            deyochScheduleMapper.updateById(schedule);
            return Result.success(schedule);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新日程失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteSchedule(Long id) {
        try {
            // 检查日程是否存在
            DeyochSchedule schedule = deyochScheduleMapper.selectById(id);
            if (schedule == null) {
                return Result.error(ResultCode.SCHEDULE_NOT_FOUND, "日程不存在");
            }
            // 删除日程
            deyochScheduleMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "删除日程失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> updateScheduleStatus(Long id, Long status) {
        try {
            // 检查日程是否存在
            DeyochSchedule schedule = deyochScheduleMapper.selectById(id);
            if (schedule == null) {
                return Result.error(ResultCode.SCHEDULE_NOT_FOUND, "日程不存在");
            }
            // 检查状态值是否合法
            if (status < 0 || status > 2) {
                return Result.error(ResultCode.PARAM_ERROR, "状态值不合法，只能是0-2");
            }
            // 更新状态
            schedule.setStatus(status);
            // 设置更新时间
            schedule.setUpdatedAt(LocalDateTime.now());
            // 更新日程
            deyochScheduleMapper.updateById(schedule);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新日程状态失败：" + e.getMessage());
        }
    }
}