package com.deyoch.service;

import com.deyoch.entity.DeyochAnnouncement;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 公告服务接口
 * 提供公告相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
public interface DeyochAnnouncementService extends IService<DeyochAnnouncement> {
    
    /**
     * 查询所有已发布的公告列表
     * 
     * @return 公告列表
     */
    List<DeyochAnnouncement> findPublished();
    
    /**
     * 根据发布人查询公告列表
     * 
     * @param publisher 发布人
     * @return 公告列表
     */
    List<DeyochAnnouncement> findByPublisher(String publisher);
    
    /**
     * 查询所有草稿公告
     * 
     * @return 草稿公告列表
     */
    List<DeyochAnnouncement> findDrafts();
    
    /**
     * 发布公告
     * 
     * @param id 公告ID
     * @return 发布成功返回true，失败返回false
     */
    boolean publish(Long id);
    
    /**
     * 撤销公告
     * 
     * @param id 公告ID
     * @return 撤销成功返回true，失败返回false
     */
    boolean revoke(Long id);
}