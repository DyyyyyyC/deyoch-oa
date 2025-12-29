package com.deyoch.controller;

import com.deyoch.entity.DeyochTask;
import com.deyoch.result.Result;
import com.deyoch.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

/**
 * 任务管理控制器
 * 处理任务相关的HTTP请求
 */
@RestController
@RequestMapping("task")
@RequiredArgsConstructor
@Tag(name = "任务管理", description = "任务相关接口")
public class TaskController {

    private final TaskService taskService;

    /**
     * 获取任务列表
     * @return 任务列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('oa:task:manage')")
    @Operation(summary = "获取任务列表", description = "获取所有任务的列表")
    public Result<List<DeyochTask>> getTaskList() {
        return taskService.getTaskList();
    }

    /**
     * 根据ID获取任务详情
     * @param id 任务ID
     * @return 任务详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('oa:task:manage')")
    @Operation(summary = "根据ID获取任务详情", description = "根据任务ID获取任务的详细信息")
    public Result<DeyochTask> getTaskById(@PathVariable @Parameter(description = "任务ID") Long id) {
        return taskService.getTaskById(id);
    }

    /**
     * 创建任务
     * @param task 任务信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('oa:task:manage')")
    @Operation(summary = "创建任务", description = "创建新的任务")
    public Result<DeyochTask> createTask(@RequestBody DeyochTask task) {
        return taskService.createTask(task);
    }

    /**
     * 更新任务信息
     * @param id 任务ID
     * @param task 任务信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('oa:task:manage')")
    @Operation(summary = "更新任务信息", description = "根据任务ID更新任务信息")
    public Result<DeyochTask> updateTask(@PathVariable @Parameter(description = "任务ID") Long id, @RequestBody DeyochTask task) {
        task.setId(id);
        return taskService.updateTask(task);
    }

    /**
     * 删除任务
     * @param id 任务ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('oa:task:manage')")
    @Operation(summary = "删除任务", description = "根据任务ID删除任务")
    public Result<Void> deleteTask(@PathVariable @Parameter(description = "任务ID") Long id) {
        return taskService.deleteTask(id);
    }

    /**
     * 分配任务
     * @param id 任务ID
     * @param assignee 被分配人
     * @return 分配结果
     */
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('oa:task:manage')")
    @Operation(summary = "分配任务", description = "根据任务ID分配任务给指定人员")
    public Result<Void> assignTask(@PathVariable @Parameter(description = "任务ID") Long id, @RequestParam @Parameter(description = "被分配人") String assignee) {
        return taskService.assignTask(id, assignee);
    }

    /**
     * 更新任务状态
     * @param id 任务ID
     * @param status 任务状态
     * @return 更新结果
     */
    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('oa:task:manage')")
    @Operation(summary = "更新任务状态", description = "根据任务ID更新任务状态")
    public Result<Void> updateTaskStatus(@PathVariable @Parameter(description = "任务ID") Long id, @RequestParam @Parameter(description = "任务状态") Long status) {
        return taskService.updateTaskStatus(id, status);
    }

    /**
     * 根据任务状态获取任务列表
     * @param status 任务状态
     * @return 任务列表
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('oa:task:manage')")
    @Operation(summary = "根据状态获取任务列表", description = "根据任务状态获取任务列表")
    public Result<List<DeyochTask>> getTasksByStatus(@PathVariable @Parameter(description = "任务状态") Long status) {
        return taskService.getTasksByStatus(status);
    }

    /**
     * 根据任务优先级获取任务列表
     * @param priority 任务优先级
     * @return 任务列表
     */
    @GetMapping("/priority/{priority}")
    @PreAuthorize("hasAuthority('oa:task:manage')")
    @Operation(summary = "根据优先级获取任务列表", description = "根据任务优先级获取任务列表")
    public Result<List<DeyochTask>> getTasksByPriority(@PathVariable @Parameter(description = "任务优先级") Long priority) {
        return taskService.getTasksByPriority(priority);
    }
}