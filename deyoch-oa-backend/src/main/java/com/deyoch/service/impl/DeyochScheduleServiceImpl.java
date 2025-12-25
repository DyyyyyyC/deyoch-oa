package com.deyoch.service.impl;

import com.deyoch.entity.DeyochSchedule;
import com.deyoch.mapper.DeyochScheduleMapper;
import com.deyoch.service.DeyochScheduleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 日程服务实现类
 * 实现日程相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Service
public class DeyochScheduleServiceImpl extends ServiceImpl<DeyochScheduleMapper, DeyochSchedule> implements DeyochScheduleService {

    /**
     * 根据用户ID查询日程列表
     * 
     * @param userId 用户ID
     * @return 日程列表
     */
    @Override
    public List<DeyochSchedule> findByUserId(Long userId) {
        return lambdaQuery()
                .eq(DeyochSchedule::getUserId, userId)
                .eq(DeyochSchedule::getStatus, 1)
                .orderByAsc(DeyochSchedule::getStartTime)
                .list();
    }

    /**
     * 查询用户在指定时间范围内的日程
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日程列表
     */
    @Override
    public List<DeyochSchedule> findByUserIdAndTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return lambdaQuery()
                .eq(DeyochSchedule::getUserId, userId)
                .eq(DeyochSchedule::getStatus, 1)
                .ge(DeyochSchedule::getStartTime, startTime)
                .le(DeyochSchedule::getEndTime, endTime)
                .orderByAsc(DeyochSchedule::getStartTime)
                .list();
    }

    /**
     * 查询用户今天的日程
     * 
     * @param userId 用户ID
     * @return 日程列表
     */
    @Override
    public List<DeyochSchedule> findTodayByUserId(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);
        
        return findByUserIdAndTimeRange(userId, startOfDay, endOfDay);
    }

    /**
     * 查询用户本周的日程
     * 
     * @param userId 用户ID
     * @return 日程列表
     */
    @Override
    public List<DeyochSchedule> findThisWeekByUserId(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate sunday = monday.plusDays(6);
        
        LocalDateTime startOfWeek = LocalDateTime.of(monday, LocalTime.MIN);
        LocalDateTime endOfWeek = LocalDateTime.of(sunday, LocalTime.MAX);
        
        return findByUserIdAndTimeRange(userId, startOfWeek, endOfWeek);
    }
}