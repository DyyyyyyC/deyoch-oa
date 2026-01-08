package com.deyoch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deyoch.entity.DeyochSchedule;
import com.deyoch.mapper.DeyochScheduleMapper;
import com.deyoch.common.result.Result;
import com.deyoch.common.result.ResultCode;
import com.deyoch.service.ScheduleService;
import com.deyoch.service.UserInfoConverter;
import com.deyoch.utils.JwtUtil;
import com.deyoch.utils.UserContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 日程管理服务实现类
 * 继承ServiceImpl获得MyBatis Plus的基础CRUD能力
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl extends ServiceImpl<DeyochScheduleMapper, DeyochSchedule> implements ScheduleService {

    private final JwtUtil jwtUtil;
    private final UserInfoConverter userInfoConverter;

    @Override
    public Result<List<DeyochSchedule>> getScheduleList() {
        try {
            // 查询所有日程，按开始时间升序排列
            LambdaQueryWrapper<DeyochSchedule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByAsc(DeyochSchedule::getStartTime);
            List<DeyochSchedule> scheduleList = list(queryWrapper);
            
            // 使用UserInfoConverter填充创建者用户名
            userInfoConverter.<DeyochSchedule>populateUserNames(
                scheduleList,
                // 用户ID提取器：从日程中提取userId
                schedule -> schedule.getUserId() != null ? 
                    Collections.singleton(schedule.getUserId()) : Collections.emptySet(),
                // 用户名设置器：将用户名设置到creatorName字段
                (schedule, userIdToNameMap) -> {
                    if (schedule.getUserId() != null) {
                        String creatorName = userIdToNameMap.get(schedule.getUserId());
                        schedule.setCreatorName(creatorName);
                    }
                }
            );
            
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
            List<DeyochSchedule> scheduleList = list(queryWrapper);
            
            // 使用UserInfoConverter填充创建者用户名
            userInfoConverter.<DeyochSchedule>populateUserNames(
                scheduleList,
                // 用户ID提取器：从日程中提取userId
                schedule -> schedule.getUserId() != null ? 
                    Collections.singleton(schedule.getUserId()) : Collections.emptySet(),
                // 用户名设置器：将用户名设置到creatorName字段
                (schedule, userIdToNameMap) -> {
                    if (schedule.getUserId() != null) {
                        String creatorName = userIdToNameMap.get(schedule.getUserId());
                        schedule.setCreatorName(creatorName);
                    }
                }
            );
            
            return Result.success(scheduleList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取用户日程列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochSchedule> getScheduleById(Long id) {
        try {
            DeyochSchedule schedule = getById(id);
            if (schedule == null) {
                return Result.error(ResultCode.SCHEDULE_NOT_FOUND, "日程不存在");
            }
            
            // 使用UserInfoConverter填充创建者用户名
            userInfoConverter.<DeyochSchedule>populateUserNames(
                schedule,
                // 用户ID提取器：从日程中提取userId
                sch -> sch.getUserId() != null ? 
                    Collections.singleton(sch.getUserId()) : Collections.emptySet(),
                // 用户名设置器：将用户名设置到creatorName字段
                (sch, userIdToNameMap) -> {
                    if (sch.getUserId() != null) {
                        String creatorName = userIdToNameMap.get(sch.getUserId());
                        sch.setCreatorName(creatorName);
                    }
                }
            );
            
            return Result.success(schedule);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取日程详情失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochSchedule> createSchedule(DeyochSchedule schedule) {
        try {
            // 获取当前用户信息，优先从Token中获取，其次从SecurityContext获取
            UserContextUtil.UserInfo userInfo = UserContextUtil.getCurrentUserInfo(jwtUtil);
            if (userInfo == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "未登录或无效的令牌，无法创建日程");
            }
            
            Long userId = userInfo.getUserId();
            if (userId == null) {
                // 如果用户ID为null，这里可以记录日志或者抛出更详细的错误
                log.error("用户信息中缺失用户ID，无法创建日程");
                return Result.error(ResultCode.UNAUTHORIZED, "用户信息不完整，无法创建日程");
            }
            
            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            schedule.setCreatedAt(now);
            schedule.setUpdatedAt(now);
            // 设置用户ID
            schedule.setUserId(userId);
            // 默认状态为待开始
            schedule.setStatus(0);
            // 创建日程
            save(schedule);
            return Result.success(schedule);
        } catch (Exception e) {
            log.error("创建日程失败：", e);
            // 不再暴露详细错误信息给前端
            return Result.error(ResultCode.SYSTEM_ERROR, "创建日程失败，请稍后重试");
        }
    }

    @Override
    public Result<DeyochSchedule> updateSchedule(DeyochSchedule schedule) {
        try {
            // 检查日程是否存在
            DeyochSchedule existingSchedule = getById(schedule.getId());
            if (existingSchedule == null) {
                return Result.error(ResultCode.SCHEDULE_NOT_FOUND, "日程不存在");
            }
            // 设置更新时间
            schedule.setUpdatedAt(LocalDateTime.now());
            // 更新日程
            updateById(schedule);
            return Result.success(schedule);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新日程失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteSchedule(Long id) {
        try {
            // 检查日程是否存在
            DeyochSchedule schedule = getById(id);
            if (schedule == null) {
                return Result.error(ResultCode.SCHEDULE_NOT_FOUND, "日程不存在");
            }
            // 删除日程
            removeById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "删除日程失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> updateScheduleStatus(Long id, Integer status) {
        try {
            // 检查日程是否存在
            DeyochSchedule schedule = getById(id);
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
            updateById(schedule);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新日程状态失败：" + e.getMessage());
        }
    }

    // ==================== 工作台专用方法实现 ====================

    public Result<List<DeyochSchedule>> getScheduleByDate(String date) {
        try {
            // 获取当前用户ID
            Long userId = UserContextUtil.getUserIdFromToken(jwtUtil);
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "未登录或无效的令牌");
            }

            // 构建查询条件：指定日期的日程
            LambdaQueryWrapper<DeyochSchedule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochSchedule::getUserId, userId);
            // 使用DATE函数匹配日期部分
            queryWrapper.apply("DATE(start_time) = {0}", date);
            queryWrapper.orderByAsc(DeyochSchedule::getStartTime);
            
            List<DeyochSchedule> scheduleList = list(queryWrapper);
            
            // 填充用户名信息
            userInfoConverter.<DeyochSchedule>populateUserNames(
                scheduleList,
                schedule -> schedule.getUserId() != null ? 
                    Collections.singleton(schedule.getUserId()) : Collections.emptySet(),
                (schedule, userIdToNameMap) -> {
                    if (schedule.getUserId() != null) {
                        String creatorName = userIdToNameMap.get(schedule.getUserId());
                        schedule.setCreatorName(creatorName);
                    }
                }
            );
            
            return Result.success(scheduleList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取指定日期日程失败：" + e.getMessage());
        }
    }

    public Result<List<DeyochSchedule>> getScheduleByDateRange(String startDate, String endDate) {
        try {
            // 获取当前用户ID
            Long userId = UserContextUtil.getUserIdFromToken(jwtUtil);
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "未登录或无效的令牌");
            }

            // 构建查询条件：日期范围内的日程
            LambdaQueryWrapper<DeyochSchedule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochSchedule::getUserId, userId);
            // 使用DATE函数匹配日期范围
            queryWrapper.apply("DATE(start_time) >= {0}", startDate);
            queryWrapper.apply("DATE(start_time) <= {0}", endDate);
            queryWrapper.orderByAsc(DeyochSchedule::getStartTime);
            
            List<DeyochSchedule> scheduleList = list(queryWrapper);
            
            // 填充用户名信息
            userInfoConverter.<DeyochSchedule>populateUserNames(
                scheduleList,
                schedule -> schedule.getUserId() != null ? 
                    Collections.singleton(schedule.getUserId()) : Collections.emptySet(),
                (schedule, userIdToNameMap) -> {
                    if (schedule.getUserId() != null) {
                        String creatorName = userIdToNameMap.get(schedule.getUserId());
                        schedule.setCreatorName(creatorName);
                    }
                }
            );
            
            return Result.success(scheduleList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取日期范围日程失败：" + e.getMessage());
        }
    }

    public Result<List<DeyochSchedule>> getUpcomingSchedules(Integer days) {
        try {
            // 获取当前用户ID
            Long userId = UserContextUtil.getUserIdFromToken(jwtUtil);
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "未登录或无效的令牌");
            }

            // 计算未来几天的日期范围
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime futureDate = now.plusDays(days);

            // 构建查询条件：未来几天的日程
            LambdaQueryWrapper<DeyochSchedule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochSchedule::getUserId, userId);
            queryWrapper.ge(DeyochSchedule::getStartTime, now);
            queryWrapper.le(DeyochSchedule::getStartTime, futureDate);
            queryWrapper.orderByAsc(DeyochSchedule::getStartTime);
            
            List<DeyochSchedule> scheduleList = list(queryWrapper);
            
            // 填充用户名信息
            userInfoConverter.<DeyochSchedule>populateUserNames(
                scheduleList,
                schedule -> schedule.getUserId() != null ? 
                    Collections.singleton(schedule.getUserId()) : Collections.emptySet(),
                (schedule, userIdToNameMap) -> {
                    if (schedule.getUserId() != null) {
                        String creatorName = userIdToNameMap.get(schedule.getUserId());
                        schedule.setCreatorName(creatorName);
                    }
                }
            );
            
            return Result.success(scheduleList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取即将到来的日程失败：" + e.getMessage());
        }
    }

    public Result<List<DeyochSchedule>> getSharedScheduleList() {
        try {
            // 获取当前用户ID
            Long userId = UserContextUtil.getUserIdFromToken(jwtUtil);
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "未登录或无效的令牌");
            }

            // 这里需要根据实际的分享机制来实现
            // 假设有一个分享表或者在schedule表中有shared_users字段
            // 目前先返回空列表，后续可以根据具体需求实现
            List<DeyochSchedule> sharedSchedules = Collections.emptyList();
            
            return Result.success(sharedSchedules);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取分享日程失败：" + e.getMessage());
        }
    }

    public Result<Void> shareSchedule(Long id, List<Long> sharedUserIds) {
        try {
            // 检查日程是否存在
            DeyochSchedule schedule = getById(id);
            if (schedule == null) {
                return Result.error(ResultCode.SCHEDULE_NOT_FOUND, "日程不存在");
            }

            // 获取当前用户ID，检查权限
            Long userId = UserContextUtil.getUserIdFromToken(jwtUtil);
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "未登录或无效的令牌");
            }

            // 检查是否是日程创建者
            if (!userId.equals(schedule.getUserId())) {
                return Result.error(ResultCode.FORBIDDEN, "只有日程创建者可以分享日程");
            }

            // 这里需要根据实际的分享机制来实现
            // 可能需要创建一个分享表来记录分享关系
            // 目前先简单返回成功，后续可以根据具体需求实现
            log.info("用户 {} 分享日程 {} 给用户 {}", userId, id, sharedUserIds);
            
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "分享日程失败：" + e.getMessage());
        }
    }
}