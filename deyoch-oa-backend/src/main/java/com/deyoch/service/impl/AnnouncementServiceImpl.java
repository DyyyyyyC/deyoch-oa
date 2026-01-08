package com.deyoch.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deyoch.entity.DeyochAnnouncement;
import com.deyoch.mapper.DeyochAnnouncementMapper;
import com.deyoch.common.result.PageResult;
import com.deyoch.common.result.Result;
import com.deyoch.common.result.ResultCode;
import com.deyoch.service.AnnouncementService;
import com.deyoch.service.UserInfoConverter;
import com.deyoch.utils.JwtUtil;
import com.deyoch.utils.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

/**
 * 公告管理服务实现类
 * 继承ServiceImpl获得MyBatis Plus的基础CRUD能力
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<DeyochAnnouncementMapper, DeyochAnnouncement> implements AnnouncementService {

    private final JwtUtil jwtUtil;
    private final UserInfoConverter userInfoConverter;

    @Override
    public Result<PageResult<DeyochAnnouncement>> getAnnouncementList(Integer page, Integer size, String keyword) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<DeyochAnnouncement> queryWrapper = new LambdaQueryWrapper<>();
            
            // 添加关键词搜索条件（搜索公告标题或内容）
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                    .like(DeyochAnnouncement::getTitle, keyword)
                    .or()
                    .like(DeyochAnnouncement::getContent, keyword)
                );
            }
            
            // 按发布时间倒序排列
            queryWrapper.orderByDesc(DeyochAnnouncement::getPublishTime);
            
            // 创建分页对象
            Page<DeyochAnnouncement> pageObj = new Page<>(page, size);
            
            // 分页查询公告
            IPage<DeyochAnnouncement> announcementPage = page(pageObj, queryWrapper);
            List<DeyochAnnouncement> announcementList = announcementPage.getRecords();
            
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
            
            // 构建分页结果
            PageResult<DeyochAnnouncement> pageResult = PageResult.of(
                announcementPage.getCurrent(),
                announcementPage.getSize(),
                announcementPage.getTotal(),
                announcementList
            );
            
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取公告列表失败，请稍后重试");
        }
    }

    @Override
    public Result<DeyochAnnouncement> getAnnouncementById(Long id) {
        try {
            DeyochAnnouncement announcement = getById(id);
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
            save(announcement);
            return Result.success(announcement);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "创建公告失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochAnnouncement> updateAnnouncement(DeyochAnnouncement announcement) {
        try {
            // 检查公告是否存在
            DeyochAnnouncement existingAnnouncement = getById(announcement.getId());
            if (existingAnnouncement == null) {
                return Result.error(ResultCode.ANNOUNCEMENT_NOT_FOUND, "公告不存在");
            }
            // 设置更新时间
            announcement.setUpdatedAt(LocalDateTime.now());
            // 更新公告
            updateById(announcement);
            return Result.success(announcement);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新公告失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteAnnouncement(Long id) {
        try {
            // 检查公告是否存在
            DeyochAnnouncement announcement = getById(id);
            if (announcement == null) {
                return Result.error(ResultCode.ANNOUNCEMENT_NOT_FOUND, "公告不存在");
            }
            // 删除公告
            removeById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "删除公告失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> publishAnnouncement(Long id) {
        try {
            // 检查公告是否存在
            DeyochAnnouncement announcement = getById(id);
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
            updateById(announcement);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "发布公告失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> revokeAnnouncement(Long id) {
        try {
            // 检查公告是否存在
            DeyochAnnouncement announcement = getById(id);
            if (announcement == null) {
                return Result.error(ResultCode.ANNOUNCEMENT_NOT_FOUND, "公告不存在");
            }
            // 设置状态为已撤销
            announcement.setStatus(2);
            // 设置更新时间
            announcement.setUpdatedAt(LocalDateTime.now());
            // 更新公告
            updateById(announcement);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "撤销公告失败：" + e.getMessage());
        }
    }
}
