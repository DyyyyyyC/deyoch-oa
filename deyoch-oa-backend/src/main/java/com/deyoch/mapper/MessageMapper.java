package com.deyoch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deyoch.dto.MessageDto;
import com.deyoch.entity.DeyochMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 消息数据访问层
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Mapper
public interface MessageMapper extends BaseMapper<DeyochMessage> {
    
    /**
     * 分页查询用户消息
     * 
     * @param page 分页对象
     * @param userId 用户ID
     * @param type 消息类型
     * @param isRead 是否已读
     * @return 消息分页数据
     */
    IPage<MessageDto> selectUserMessages(Page<MessageDto> page, 
                                        @Param("userId") Long userId,
                                        @Param("type") Integer type,
                                        @Param("isRead") Integer isRead);
    
    /**
     * 获取用户未读消息数量
     * 
     * @param userId 用户ID
     * @return 未读消息数量
     */
    Long selectUnreadMessageCount(@Param("userId") Long userId);
    
    /**
     * 获取用户未读消息数量（按类型分组）
     * 
     * @param userId 用户ID
     * @return 各类型未读消息数量
     */
    List<Map<String, Object>> selectUnreadMessageCountByType(@Param("userId") Long userId);
    
    /**
     * 批量标记消息为已读
     * 
     * @param messageIds 消息ID列表
     * @param userId 用户ID
     * @return 更新数量
     */
    int batchMarkAsRead(@Param("messageIds") List<Long> messageIds, @Param("userId") Long userId);
    
    /**
     * 根据消息ID和用户ID查询消息
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 消息对象
     */
    MessageDto selectMessageByIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);
}