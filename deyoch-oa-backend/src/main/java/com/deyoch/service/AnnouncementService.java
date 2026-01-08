package com.deyoch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deyoch.entity.DeyochAnnouncement;
import com.deyoch.common.result.PageResult;
import com.deyoch.common.result.Result;

/**
 * 公告管理服务接口
 * 定义公告管理相关的业务逻辑方法
 */
public interface AnnouncementService extends IService<DeyochAnnouncement> {

    /**
     * 获取公告列表（分页）
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词
     * @return 分页公告列表
     */
    Result<PageResult<DeyochAnnouncement>> getAnnouncementList(Integer page, Integer size, String keyword);

    /**
     * 根据ID获取公告详情
     * @param id 公告ID
     * @return 公告详情
     */
    Result<DeyochAnnouncement> getAnnouncementById(Long id);

    /**
     * 创建公告
     * @param announcement 公告信息
     * @return 创建结果
     */
    Result<DeyochAnnouncement> createAnnouncement(DeyochAnnouncement announcement);

    /**
     * 更新公告信息
     * @param announcement 公告信息
     * @return 更新结果
     */
    Result<DeyochAnnouncement> updateAnnouncement(DeyochAnnouncement announcement);

    /**
     * 删除公告
     * @param id 公告ID
     * @return 删除结果
     */
    Result<Void> deleteAnnouncement(Long id);

    /**
     * 发布公告
     * @param id 公告ID
     * @return 发布结果
     */
    Result<Void> publishAnnouncement(Long id);

    /**
     * 撤销公告
     * @param id 公告ID
     * @return 撤销结果
     */
    Result<Void> revokeAnnouncement(Long id);
}