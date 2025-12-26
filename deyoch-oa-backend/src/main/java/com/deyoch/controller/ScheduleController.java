package com.deyoch.controller;

import com.deyoch.entity.DeyochSchedule;
import com.deyoch.result.Result;
import com.deyoch.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 日程管理控制器
 * 处理日程相关的HTTP请求
 */
@RestController
@RequestMapping("schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * 获取日程列表
     * @return 日程列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('schedule:list')")
    public Result<List<DeyochSchedule>> getScheduleList() {
        return scheduleService.getScheduleList();
    }

    /**
     * 根据用户ID获取日程列表
     * @param userId 用户ID
     * @return 日程列表
     */
    @GetMapping("/list/{userId}")
    @PreAuthorize("hasAuthority('schedule:list')")
    public Result<List<DeyochSchedule>> getScheduleListByUserId(@PathVariable Long userId) {
        return scheduleService.getScheduleListByUserId(userId);
    }

    /**
     * 根据ID获取日程详情
     * @param id 日程ID
     * @return 日程详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('schedule:detail')")
    public Result<DeyochSchedule> getScheduleById(@PathVariable Long id) {
        return scheduleService.getScheduleById(id);
    }

    /**
     * 创建日程
     * @param schedule 日程信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('schedule:add')")
    public Result<DeyochSchedule> createSchedule(@RequestBody DeyochSchedule schedule) {
        return scheduleService.createSchedule(schedule);
    }

    /**
     * 更新日程信息
     * @param id 日程ID
     * @param schedule 日程信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('schedule:update')")
    public Result<DeyochSchedule> updateSchedule(@PathVariable Long id, @RequestBody DeyochSchedule schedule) {
        schedule.setId(id);
        return scheduleService.updateSchedule(schedule);
    }

    /**
     * 删除日程
     * @param id 日程ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('schedule:delete')")
    public Result<Void> deleteSchedule(@PathVariable Long id) {
        return scheduleService.deleteSchedule(id);
    }

    /**
     * 更新日程状态
     * @param id 日程ID
     * @param status 日程状态
     * @return 更新结果
     */
    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('schedule:update-status')")
    public Result<Void> updateScheduleStatus(@PathVariable Long id, @RequestParam Long status) {
        return scheduleService.updateScheduleStatus(id, status);
    }
}