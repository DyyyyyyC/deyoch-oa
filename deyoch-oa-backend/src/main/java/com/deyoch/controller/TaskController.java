package com.deyoch.controller;

import com.deyoch.entity.DeyochTask;
import com.deyoch.result.Result;
import com.deyoch.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务管理控制器
 * 处理任务相关的HTTP请求
 */
@RestController
@RequestMapping("task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * 获取任务列表
     * @return 任务列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('task:list')")
    public Result<List<DeyochTask>> getTaskList() {
        return taskService.getTaskList();
    }

    /**
     * 根据ID获取任务详情
     * @param id 任务ID
     * @return 任务详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('task:detail')")
    public Result<DeyochTask> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    /**
     * 创建任务
     * @param task 任务信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('task:add')")
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
    @PreAuthorize("hasAuthority('task:update')")
    public Result<DeyochTask> updateTask(@PathVariable Long id, @RequestBody DeyochTask task) {
        task.setId(id);
        return taskService.updateTask(task);
    }

    /**
     * 删除任务
     * @param id 任务ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('task:delete')")
    public Result<Void> deleteTask(@PathVariable Long id) {
        return taskService.deleteTask(id);
    }

    /**
     * 分配任务
     * @param id 任务ID
     * @param assignee 被分配人
     * @return 分配结果
     */
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('task:assign')")
    public Result<Void> assignTask(@PathVariable Long id, @RequestParam String assignee) {
        return taskService.assignTask(id, assignee);
    }

    /**
     * 更新任务状态
     * @param id 任务ID
     * @param status 任务状态
     * @return 更新结果
     */
    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('task:update-status')")
    public Result<Void> updateTaskStatus(@PathVariable Long id, @RequestParam Long status) {
        return taskService.updateTaskStatus(id, status);
    }

    /**
     * 根据任务状态获取任务列表
     * @param status 任务状态
     * @return 任务列表
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('task:list-by-status')")
    public Result<List<DeyochTask>> getTasksByStatus(@PathVariable Long status) {
        return taskService.getTasksByStatus(status);
    }

    /**
     * 根据任务优先级获取任务列表
     * @param priority 任务优先级
     * @return 任务列表
     */
    @GetMapping("/priority/{priority}")
    @PreAuthorize("hasAuthority('task:list-by-priority')")
    public Result<List<DeyochTask>> getTasksByPriority(@PathVariable Long priority) {
        return taskService.getTasksByPriority(priority);
    }
}