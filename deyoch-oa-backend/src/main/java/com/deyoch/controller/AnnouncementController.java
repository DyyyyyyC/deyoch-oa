package com.deyoch.controller;

import com.deyoch.entity.DeyochAnnouncement;
import com.deyoch.result.Result;
import com.deyoch.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告管理控制器
 * 处理公告相关的HTTP请求
 */
@RestController
@RequestMapping("announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 获取公告列表
     * @return 公告列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('announcement:list')")
    public Result<List<DeyochAnnouncement>> getAnnouncementList() {
        return announcementService.getAnnouncementList();
    }

    /**
     * 根据ID获取公告详情
     * @param id 公告ID
     * @return 公告详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('announcement:detail')")
    public Result<DeyochAnnouncement> getAnnouncementById(@PathVariable Long id) {
        return announcementService.getAnnouncementById(id);
    }

    /**
     * 创建公告
     * @param announcement 公告信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('announcement:add')")
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
    @PreAuthorize("hasAuthority('announcement:update')")
    public Result<DeyochAnnouncement> updateAnnouncement(@PathVariable Long id, @RequestBody DeyochAnnouncement announcement) {
        announcement.setId(id);
        return announcementService.updateAnnouncement(announcement);
    }

    /**
     * 删除公告
     * @param id 公告ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('announcement:delete')")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        return announcementService.deleteAnnouncement(id);
    }

    /**
     * 发布公告
     * @param id 公告ID
     * @return 发布结果
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('announcement:publish')")
    public Result<Void> publishAnnouncement(@PathVariable Long id) {
        return announcementService.publishAnnouncement(id);
    }

    /**
     * 撤销公告
     * @param id 公告ID
     * @return 撤销结果
     */
    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasAuthority('announcement:revoke')")
    public Result<Void> revokeAnnouncement(@PathVariable Long id) {
        return announcementService.revokeAnnouncement(id);
    }
}