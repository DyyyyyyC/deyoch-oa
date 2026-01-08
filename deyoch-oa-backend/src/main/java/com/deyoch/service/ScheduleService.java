package com.deyoch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deyoch.entity.DeyochSchedule;
import com.deyoch.common.result.Result;

import java.util.List;

/**
 * 日程管理服务接口
 * 定义日程管理相关的业务逻辑方法
 */
public interface ScheduleService extends IService<DeyochSchedule> {

    /**
     * 获取日程列表
     * @return 日程列表
     */
    Result<List<DeyochSchedule>> getScheduleList();

    /**
     * 根据用户ID获取日程列表
     * @param userId 用户ID
     * @return 日程列表
     */
    Result<List<DeyochSchedule>> getScheduleListByUserId(Long userId);

    /**
     * 根据ID获取日程详情
     * @param id 日程ID
     * @return 日程详情
     */
    Result<DeyochSchedule> getScheduleById(Long id);

    /**
     * 创建日程
     * @param schedule 日程信息
     * @return 创建结果
     */
    Result<DeyochSchedule> createSchedule(DeyochSchedule schedule);

    /**
     * 更新日程信息
     * @param schedule 日程信息
     * @return 更新结果
     */
    Result<DeyochSchedule> updateSchedule(DeyochSchedule schedule);

    /**
     * 删除日程
     * @param id 日程ID
     * @return 删除结果
     */
    Result<Void> deleteSchedule(Long id);

    /**
     * 更新日程状态
     * @param id 日程ID
     * @param status 日程状态
     * @return 更新结果
     */
    Result<Void> updateScheduleStatus(Long id, Integer status);
}