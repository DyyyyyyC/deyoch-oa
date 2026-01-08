package com.deyoch.service;

import com.deyoch.common.result.PageResult;
import com.deyoch.common.result.Result;
import com.deyoch.dto.MessageDto;
import com.deyoch.entity.DeyochMessage;

import java.util.List;
import java.util.Map;

/**
 * 消息服务接口
 * 
 * @author deyoch
 * @since 2026-01-08
 */
public interface MessageService {
    
    /**
     * 发送消息
     * 
     * @param message 消息对象
     * @return 发送结果
     */
    Result<Void> sendMessage(MessageDto message);
    
    /**
     * 批量发送消息
     * 
     * @param receiverIds 接收者ID列表
     * @param message 消息对象
     * @return 发送结果
     */
    Result<Void> sendBatchMessage(List<Long> receiverIds, MessageDto message);
    
    /**
     * 根据模板发送消息
     * 
     * @param templateType 模板类型
     * @param receiverId 接收者ID
     * @param variables 模板变量
     * @return 发送结果
     */
    Result<Void> sendMessageByTemplate(String templateType, Long receiverId, Map<String, Object> variables);
    
    /**
     * 分页查询用户消息
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @param type 消息类型
     * @param isRead 是否已读
     * @return 消息分页结果
     */
    Result<PageResult<MessageDto>> getUserMessages(Long userId, Integer page, Integer size, 
                                                  Integer type, Integer isRead);
    
    /**
     * 标记消息为已读
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Void> markMessageAsRead(Long messageId, Long userId);
    
    /**
     * 批量标记消息为已读
     * 
     * @param messageIds 消息ID列表
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Void> markMessagesAsRead(List<Long> messageIds, Long userId);
    
    /**
     * 删除消息
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Void> deleteMessage(Long messageId, Long userId);
    
    /**
     * 获取用户未读消息数量
     * 
     * @param userId 用户ID
     * @return 未读消息数量
     */
    Result<Long> getUnreadMessageCount(Long userId);
    
    /**
     * 获取用户未读消息数量（按类型分组）
     * 
     * @param userId 用户ID
     * @return 各类型未读消息数量
     */
    Result<Map<Integer, Long>> getUnreadMessageCountByType(Long userId);
    
    /**
     * 清空用户所有消息
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Void> clearAllMessages(Long userId);
}