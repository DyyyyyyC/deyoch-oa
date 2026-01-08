package com.deyoch.controller;

import com.deyoch.entity.DeyochProcess;
import com.deyoch.common.result.Result;
import com.deyoch.service.ProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

/**
 * 流程管理控制器
 * 处理流程定义相关的HTTP请求
 */
@RestController
@RequestMapping("process")
@RequiredArgsConstructor
@Tag(name = "流程管理", description = "流程定义相关接口")
public class ProcessController {

    private final ProcessService processService;

    /**
     * 获取流程列表
     * @return 流程列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('oa:process:manage')")
    @Operation(summary = "获取流程列表", description = "获取所有流程定义的列表")
    public Result<List<DeyochProcess>> getProcessList() {
        return processService.getProcessList();
    }

    /**
     * 根据ID获取流程详情
     * @param id 流程ID
     * @return 流程详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('oa:process:manage')")
    @Operation(summary = "根据ID获取流程详情", description = "根据流程ID获取流程定义的详细信息")
    public Result<DeyochProcess> getProcessById(@PathVariable @Parameter(description = "流程ID") Long id) {
        return processService.getProcessById(id);
    }

    /**
     * 创建流程
     * @param process 流程信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('oa:process:manage')")
    @Operation(summary = "创建流程", description = "创建新的流程定义")
    public Result<DeyochProcess> createProcess(@RequestBody DeyochProcess process) {
        return processService.createProcess(process);
    }

    /**
     * 更新流程
     * @param id 流程ID
     * @param process 流程信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('oa:process:manage')")
    @Operation(summary = "更新流程", description = "根据流程ID更新流程定义信息")
    public Result<DeyochProcess> updateProcess(@PathVariable @Parameter(description = "流程ID") Long id, @RequestBody DeyochProcess process) {
        process.setId(id);
        return processService.updateProcess(process);
    }

    /**
     * 删除流程
     * @param id 流程ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('oa:process:manage')")
    @Operation(summary = "删除流程", description = "根据流程ID删除流程定义")
    public Result<Void> deleteProcess(@PathVariable @Parameter(description = "流程ID") Long id) {
        return processService.deleteProcess(id);
    }
}
