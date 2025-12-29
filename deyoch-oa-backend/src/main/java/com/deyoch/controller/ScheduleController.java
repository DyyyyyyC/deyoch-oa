package com.deyoch.controller;

import com.deyoch.entity.DeyochSchedule;
import com.deyoch.result.Result;
import com.deyoch.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

/**
 * 日程管理控制器
 * 处理日程相关的HTTP请求
 */
@RestController
@RequestMapping("schedule")
@RequiredArgsConstructor
@Tag(name = "日程管理", description = "日程相关接口")
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * 获取日程列表
     * @return 日程列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('schedule:list')")
    @Operation(summary = "获取日程列表", description = "获取所有日程的列表")
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
    @Operation(summary = "根据用户ID获取日程列表", description = "根据用户ID获取该用户的所有日程")
    public Result<List<DeyochSchedule>> getScheduleListByUserId(@PathVariable @Parameter(description = "用户ID") Long userId) {
        return scheduleService.getScheduleListByUserId(userId);
    }

    /**
     * 根据ID获取日程详情
     * @param id 日程ID
     * @return 日程详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('schedule:detail')")
    @Operation(summary = "根据ID获取日程详情", description = "根据日程ID获取日程的详细信息")
    public Result<DeyochSchedule> getScheduleById(@PathVariable @Parameter(description = "日程ID") Long id) {
        return scheduleService.getScheduleById(id);
    }

    /**
     * 创建日程
     * @param schedule 日程信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('schedule:add')")
    @Operation(summary = "创建日程", description = "创建新的日程")
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
    @Operation(summary = "更新日程信息", description = "根据日程ID更新日程信息")
    public Result<DeyochSchedule> updateSchedule(@PathVariable @Parameter(description = "日程ID") Long id, @RequestBody DeyochSchedule schedule) {
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
    @Operation(summary = "删除日程", description = "根据日程ID删除日程")
    public Result<Void> deleteSchedule(@PathVariable @Parameter(description = "日程ID") Long id) {
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
    @Operation(summary = "更新日程状态", description = "根据日程ID更新日程状态")
    public Result<Void> updateScheduleStatus(@PathVariable @Parameter(description = "日程ID") Long id, @RequestParam @Parameter(description = "日程状态") Long status) {
        return scheduleService.updateScheduleStatus(id, status);
    }
}