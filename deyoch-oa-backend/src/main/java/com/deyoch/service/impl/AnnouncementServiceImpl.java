package com.deyoch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deyoch.entity.DeyochAnnouncement;
import com.deyoch.mapper.DeyochAnnouncementMapper;
import com.deyoch.result.Result;
import com.deyoch.result.ResultCode;
import com.deyoch.service.AnnouncementService;
import com.deyoch.service.UserInfoConverter;
import com.deyoch.utils.JwtUtil;
import com.deyoch.utils.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

/**
 * 公告管理服务实现类
 * 实现公告管理相关的业务逻辑
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final DeyochAnnouncementMapper deyochAnnouncementMapper;
    private final JwtUtil jwtUtil;
    private final UserInfoConverter userInfoConverter;

    @Override
    public Result<List<DeyochAnnouncement>> getAnnouncementList() {
        try {
            // 查询所有公告，按发布时间倒序排列
            LambdaQueryWrapper<DeyochAnnouncement> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(DeyochAnnouncement::getPublishTime);
            List<DeyochAnnouncement> announcementList = deyochAnnouncementMapper.selectList(queryWrapper);
            
            // 使用UserInfoConverter填充发布者用户名
            userInfoConverter.<DeyochAnnouncement>populateUserNames(
                announcementList,
                // 用户ID提取器：从公告中提取userId
                announcement -> announcement.getUserId() != null ? 
                    Collections.singleton(announcement.getUserId()) : Collections.emptySet(),
                // 用户名设置器：将用户名设置到publisherName字段
                (announcement, userIdToNameMap) -> {
                    if (announcement.getUserId() != null) {
                        String publisherName = userIdToNameMap.get(announcement.getUserId());
                        announcement.setPublisherName(publisherName);
                    }
                }
            );
            
            return Result.success(announcementList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取公告列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochAnnouncement> getAnnouncementById(Long id) {
        try {
            DeyochAnnouncement announcement = deyochAnnouncementMapper.selectById(id);
            if (announcement == null) {
                return Result.error(ResultCode.ANNOUNCEMENT_NOT_FOUND, "公告不存在");
            }
            
            // 使用UserInfoConverter填充发布者用户名
            userInfoConverter.<DeyochAnnouncement>populateUserNames(
                announcement,
                // 用户ID提取器：从公告中提取userId
                ann -> ann.getUserId() != null ? 
                    Collections.singleton(ann.getUserId()) : Collections.emptySet(),
                // 用户名设置器：将用户名设置到publisherName字段
                (ann, userIdToNameMap) -> {
                    if (ann.getUserId() != null) {
                        String publisherName = userIdToNameMap.get(ann.getUserId());
                        ann.setPublisherName(publisherName);
                    }
                }
            );
            
            return Result.success(announcement);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取公告详情失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochAnnouncement> createAnnouncement(DeyochAnnouncement announcement) {
        try {
            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            announcement.setCreatedAt(now);
            announcement.setUpdatedAt(now);
            // 默认状态为未发布
            announcement.setStatus(0);
            // 设置创建人为当前登录用户ID
            Long currentUserId = UserContextUtil.getUserIdFromToken(jwtUtil);
            if (currentUserId != null) {
                announcement.setUserId(currentUserId);
            }
            // 创建公告
            deyochAnnouncementMapper.insert(announcement);
            return Result.success(announcement);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "创建公告失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochAnnouncement> updateAnnouncement(DeyochAnnouncement announcement) {
        try {
            // 检查公告是否存在
            DeyochAnnouncement existingAnnouncement = deyochAnnouncementMapper.selectById(announcement.getId());
            if (existingAnnouncement == null) {
                return Result.error(ResultCode.ANNOUNCEMENT_NOT_FOUND, "公告不存在");
            }
            // 设置更新时间
            announcement.setUpdatedAt(LocalDateTime.now());
            // 更新公告
            deyochAnnouncementMapper.updateById(announcement);
            return Result.success(announcement);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新公告失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteAnnouncement(Long id) {
        try {
            // 检查公告是否存在
            DeyochAnnouncement announcement = deyochAnnouncementMapper.selectById(id);
            if (announcement == null) {
                return Result.error(ResultCode.ANNOUNCEMENT_NOT_FOUND, "公告不存在");
            }
            // 删除公告
            deyochAnnouncementMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "删除公告失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> publishAnnouncement(Long id) {
        try {
            // 检查公告是否存在
            DeyochAnnouncement announcement = deyochAnnouncementMapper.selectById(id);
            if (announcement == null) {
                return Result.error(ResultCode.ANNOUNCEMENT_NOT_FOUND, "公告不存在");
            }
            // 设置状态为已发布
            announcement.setStatus(1);
            // 设置发布时间
            announcement.setPublishTime(LocalDateTime.now());
            // 设置更新时间
            announcement.setUpdatedAt(LocalDateTime.now());
            // 更新公告
            deyochAnnouncementMapper.updateById(announcement);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "发布公告失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> revokeAnnouncement(Long id) {
        try {
            // 检查公告是否存在
            DeyochAnnouncement announcement = deyochAnnouncementMapper.selectById(id);
            if (announcement == null) {
                return Result.error(ResultCode.ANNOUNCEMENT_NOT_FOUND, "公告不存在");
            }
            // 设置状态为已撤销
            announcement.setStatus(2);
            // 设置更新时间
            announcement.setUpdatedAt(LocalDateTime.now());
            // 更新公告
            deyochAnnouncementMapper.updateById(announcement);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "撤销公告失败：" + e.getMessage());
        }
    }
}
