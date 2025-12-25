package com.deyoch.service;

import com.deyoch.entity.DeyochSchedule;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程服务接口
 * 提供日程相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
public interface DeyochScheduleService extends IService<DeyochSchedule> {
    
    /**
     * 根据用户ID查询日程列表
     * 
     * @param userId 用户ID
     * @return 日程列表
     */
    List<DeyochSchedule> findByUserId(Long userId);
    
    /**
     * 查询用户在指定时间范围内的日程
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日程列表
     */
    List<DeyochSchedule> findByUserIdAndTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查询用户今天的日程
     * 
     * @param userId 用户ID
     * @return 日程列表
     */
    List<DeyochSchedule> findTodayByUserId(Long userId);
    
    /**
     * 查询用户本周的日程
     * 
     * @param userId 用户ID
     * @return 日程列表
     */
    List<DeyochSchedule> findThisWeekByUserId(Long userId);
}