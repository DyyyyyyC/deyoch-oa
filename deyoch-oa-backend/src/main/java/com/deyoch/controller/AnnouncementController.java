package com.deyoch.controller;

import com.deyoch.entity.DeyochAnnouncement;
import com.deyoch.common.result.PageResult;
import com.deyoch.common.result.Result;
import com.deyoch.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

/**
 * 公告管理控制器
 * 处理公告相关的HTTP请求
 */
@RestController
@RequestMapping("announcement")
@RequiredArgsConstructor
@Tag(name = "公告管理", description = "公告相关接口")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 获取公告列表（分页）
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词
     * @return 分页公告列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('oa:announcement:manage')")
    @Operation(summary = "获取公告列表", description = "获取所有公告的分页列表")
    public Result<PageResult<DeyochAnnouncement>> getAnnouncementList(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页数量") Integer size,
            @RequestParam(required = false) @Parameter(description = "搜索关键词") String keyword) {
        return announcementService.getAnnouncementList(page, size, keyword);
    }

    /**
     * 根据ID获取公告详情
     * @param id 公告ID
     * @return 公告详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('oa:announcement:manage')")
    @Operation(summary = "根据ID获取公告详情", description = "根据公告ID获取公告的详细信息")
    public Result<DeyochAnnouncement> getAnnouncementById(@PathVariable @Parameter(description = "公告ID") Long id) {
        return announcementService.getAnnouncementById(id);
    }

    /**
     * 创建公告
     * @param announcement 公告信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('oa:announcement:manage')")
    @Operation(summary = "创建公告", description = "创建新的公告")
    public Result<DeyochAnnouncement> createAnnouncement(@RequestBody DeyochAnnouncement announcement) {
        return announcementService.createAnnouncement(announcement);
    }

    /**
     * 更新公告信息
     * @param id 公告ID
     * @param announcement 公告信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('oa:announcement:manage')")
    @Operation(summary = "更新公告信息", description = "根据公告ID更新公告信息")
    public Result<DeyochAnnouncement> updateAnnouncement(@PathVariable @Parameter(description = "公告ID") Long id, @RequestBody DeyochAnnouncement announcement) {
        announcement.setId(id);
        return announcementService.updateAnnouncement(announcement);
    }

    /**
     * 删除公告
     * @param id 公告ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('oa:announcement:manage')")
    @Operation(summary = "删除公告", description = "根据公告ID删除公告")
    public Result<Void> deleteAnnouncement(@PathVariable @Parameter(description = "公告ID") Long id) {
        return announcementService.deleteAnnouncement(id);
    }

    /**
     * 发布公告
     * @param id 公告ID
     * @return 发布结果
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('oa:announcement:manage')")
    @Operation(summary = "发布公告", description = "根据公告ID发布公告")
    public Result<Void> publishAnnouncement(@PathVariable @Parameter(description = "公告ID") Long id) {
        return announcementService.publishAnnouncement(id);
    }

    /**
     * 撤销公告
     * @param id 公告ID
     * @return 撤销结果
     */
    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasAuthority('oa:announcement:manage')")
    @Operation(summary = "撤销公告", description = "根据公告ID撤销公告")
    public Result<Void> revokeAnnouncement(@PathVariable @Parameter(description = "公告ID") Long id) {
        return announcementService.revokeAnnouncement(id);
    }
}