package com.deyoch.controller;

import com.deyoch.entity.DeyochProcessInstance;
import com.deyoch.common.result.Result;
import com.deyoch.service.ProcessInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

/**
 * 流程实例管理控制器
 * 处理流程实例相关的HTTP请求
 */
@RestController
@RequestMapping("process-instance")
@RequiredArgsConstructor
@Tag(name = "流程实例管理", description = "流程实例相关接口")
public class ProcessInstanceController {

    private final ProcessInstanceService processInstanceService;

    /**
     * 获取流程实例列表
     * @return 流程实例列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('oa:process:manage')")
    @Operation(summary = "获取流程实例列表", description = "获取所有流程实例的列表")
    public Result<List<DeyochProcessInstance>> getProcessInstanceList() {
        return processInstanceService.getProcessInstanceList();
    }

    /**
     * 根据ID获取流程实例详情
     * @param id 流程实例ID
     * @return 流程实例详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('oa:process:manage')")
    @Operation(summary = "根据ID获取流程实例详情", description = "根据流程实例ID获取流程实例的详细信息")
    public Result<DeyochProcessInstance> getProcessInstanceById(@PathVariable @Parameter(description = "流程实例ID") Long id) {
        return processInstanceService.getProcessInstanceById(id);
    }

    /**
     * 创建流程实例
     * @param instance 流程实例信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('oa:process:manage')")
    @Operation(summary = "创建流程实例", description = "创建新的流程实例")
    public Result<DeyochProcessInstance> createProcessInstance(@RequestBody DeyochProcessInstance instance) {
        return processInstanceService.createProcessInstance(instance);
    }

    /**
     * 更新流程实例
     * @param id 流程实例ID
     * @param instance 流程实例信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('oa:process:manage')")
    @Operation(summary = "更新流程实例", description = "根据流程实例ID更新流程实例信息")
    public Result<DeyochProcessInstance> updateProcessInstance(@PathVariable @Parameter(description = "流程实例ID") Long id, @RequestBody DeyochProcessInstance instance) {
        instance.setId(id);
        return processInstanceService.updateProcessInstance(instance);
    }

    /**
     * 删除流程实例
     * @param id 流程实例ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('oa:process:manage')")
    @Operation(summary = "删除流程实例", description = "根据流程实例ID删除流程实例")
    public Result<Void> deleteProcessInstance(@PathVariable @Parameter(description = "流程实例ID") Long id) {
        return processInstanceService.deleteProcessInstance(id);
    }

    /**
     * 启动流程实例
     * @param id 流程实例ID
     * @return 启动结果
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAuthority('oa:process:manage')")
    @Operation(summary = "启动流程实例", description = "根据流程实例ID启动流程实例")
    public Result<Void> startProcessInstance(@PathVariable @Parameter(description = "流程实例ID") Long id) {
        return processInstanceService.startProcessInstance(id);
    }

    /**
     * 完成流程实例
     * @param id 流程实例ID
     * @return 完成结果
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('oa:process:manage')")
    @Operation(summary = "完成流程实例", description = "根据流程实例ID完成流程实例")
    public Result<Void> completeProcessInstance(@PathVariable @Parameter(description = "流程实例ID") Long id) {
        return processInstanceService.completeProcessInstance(id);
    }
}
