package com.deyoch.service.impl;

import com.deyoch.entity.DeyochAnnouncement;
import com.deyoch.mapper.DeyochAnnouncementMapper;
import com.deyoch.service.DeyochAnnouncementService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务实现类
 * 实现公告相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Service
public class DeyochAnnouncementServiceImpl extends ServiceImpl<DeyochAnnouncementMapper, DeyochAnnouncement> implements DeyochAnnouncementService {

    /**
     * 查询所有已发布的公告列表
     * 
     * @return 公告列表
     */
    @Override
    public List<DeyochAnnouncement> findPublished() {
        return lambdaQuery()
                .eq(DeyochAnnouncement::getStatus, 1)
                .orderByDesc(DeyochAnnouncement::getPublishTime)
                .list();
    }

    /**
     * 根据发布人查询公告列表
     * 
     * @param publisher 发布人
     * @return 公告列表
     */
    @Override
    public List<DeyochAnnouncement> findByPublisher(String publisher) {
        return lambdaQuery()
                .eq(DeyochAnnouncement::getPublisher, publisher)
                .orderByDesc(DeyochAnnouncement::getPublishTime)
                .list();
    }

    /**
     * 查询所有草稿公告
     * 
     * @return 草稿公告列表
     */
    @Override
    public List<DeyochAnnouncement> findDrafts() {
        return lambdaQuery()
                .eq(DeyochAnnouncement::getStatus, 0)
                .orderByDesc(DeyochAnnouncement::getCreatedAt)
                .list();
    }

    /**
     * 发布公告
     * 
     * @param id 公告ID
     * @return 发布成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publish(Long id) {
        DeyochAnnouncement announcement = lambdaQuery()
                .eq(DeyochAnnouncement::getId, id)
                .one();
        
        if (announcement == null) {
            return false;
        }
        
        announcement.setStatus(1);
        announcement.setPublishTime(LocalDateTime.now());
        return updateById(announcement);
    }

    /**
     * 撤销公告
     * 
     * @param id 公告ID
     * @return 撤销成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revoke(Long id) {
        DeyochAnnouncement announcement = lambdaQuery()
                .eq(DeyochAnnouncement::getId, id)
                .one();
        
        if (announcement == null) {
            return false;
        }
        
        announcement.setStatus(2);
        return updateById(announcement);
    }
}