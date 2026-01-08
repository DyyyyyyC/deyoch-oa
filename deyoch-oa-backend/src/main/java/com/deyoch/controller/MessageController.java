package com.deyoch.controller;

import com.deyoch.common.result.PageResult;
import com.deyoch.common.result.Result;
import com.deyoch.dto.MessageDto;
import com.deyoch.service.MessageService;
import com.deyoch.websocket.MessageWebSocketHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息管理控制器
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Slf4j
@RestController
@RequestMapping("/message")
@Tag(name = "消息管理", description = "消息通知相关接口")
public class MessageController {
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private MessageWebSocketHandler webSocketHandler;
    
    /**
     * 发送消息
     */
    @PostMapping("/send")
    @Operation(summary = "发送消息", description = "发送单条消息给指定用户")
    public Result<Void> sendMessage(@RequestBody MessageDto message) {
        log.info("发送消息 - 接收者: {}, 标题: {}", message.getReceiverId(), message.getTitle());
        return messageService.sendMessage(message);
    }
    
    /**
     * 批量发送消息
     */
    @PostMapping("/send/batch")
    @Operation(summary = "批量发送消息", description = "批量发送消息给多个用户")
    public Result<Void> sendBatchMessage(
            @Parameter(description = "接收者ID列表", required = true) @RequestParam List<Long> receiverIds,
            @RequestBody MessageDto message) {
        
        log.info("批量发送消息 - 接收者数量: {}, 标题: {}", receiverIds.size(), message.getTitle());
        return messageService.sendBatchMessage(receiverIds, message);
    }
    
    /**
     * 分页查询用户消息
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询用户消息", description = "获取用户的消息列表，支持按类型和已读状态筛选")
    public Result<PageResult<MessageDto>> getUserMessages(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小，默认20") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "消息类型") @RequestParam(required = false) Integer type,
            @Parameter(description = "是否已读") @RequestParam(required = false) Integer isRead) {
        
        log.info("查询用户消息 - 用户ID: {}, page: {}, size: {}, type: {}, isRead: {}", 
                userId, page, size, type, isRead);
        return messageService.getUserMessages(userId, page, size, type, isRead);
    }
    
    /**
     * 标记消息为已读
     */
    @PutMapping("/{messageId}/read")
    @Operation(summary = "标记消息为已读", description = "将指定消息标记为已读状态")
    public Result<Void> markMessageAsRead(
            @Parameter(description = "消息ID", required = true) @PathVariable Long messageId,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        
        log.info("标记消息已读 - 消息ID: {}, 用户ID: {}", messageId, userId);
        return messageService.markMessageAsRead(messageId, userId);
    }
    
    /**
     * 批量标记消息为已读
     */
    @PutMapping("/read/batch")
    @Operation(summary = "批量标记消息为已读", description = "批量将消息标记为已读状态")
    public Result<Void> markMessagesAsRead(
            @Parameter(description = "消息ID列表", required = true) @RequestParam List<Long> messageIds,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        
        log.info("批量标记消息已读 - 用户ID: {}, 消息数量: {}", userId, messageIds.size());
        return messageService.markMessagesAsRead(messageIds, userId);
    }
    
    /**
     * 删除消息
     */
    @DeleteMapping("/{messageId}")
    @Operation(summary = "删除消息", description = "删除指定的消息")
    public Result<Void> deleteMessage(
            @Parameter(description = "消息ID", required = true) @PathVariable Long messageId,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        
        log.info("删除消息 - 消息ID: {}, 用户ID: {}", messageId, userId);
        return messageService.deleteMessage(messageId, userId);
    }
    
    /**
     * 获取用户未读消息数量
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读消息数量", description = "获取用户的未读消息总数")
    public Result<Long> getUnreadMessageCount(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        
        return messageService.getUnreadMessageCount(userId);
    }
    
    /**
     * 获取用户未读消息数量（按类型分组）
     */
    @GetMapping("/unread/count/by-type")
    @Operation(summary = "获取各类型未读消息数量", description = "获取用户各类型消息的未读数量")
    public Result<Map<Integer, Long>> getUnreadMessageCountByType(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        
        return messageService.getUnreadMessageCountByType(userId);
    }
    
    /**
     * 清空用户所有消息
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空所有消息", description = "清空用户的所有消息")
    public Result<Void> clearAllMessages(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        
        log.info("清空所有消息 - 用户ID: {}", userId);
        return messageService.clearAllMessages(userId);
    }
    
    /**
     * 获取WebSocket连接状态
     */
    @GetMapping("/websocket/status")
    @Operation(summary = "获取WebSocket状态", description = "获取WebSocket连接状态信息")
    public Result<Map<String, Object>> getWebSocketStatus() {
        Map<String, Object> status = Map.of(
                "onlineUserCount", webSocketHandler.getOnlineUserCount(),
                "timestamp", System.currentTimeMillis()
        );
        return Result.success(status);
    }
    
    /**
     * 检查用户是否在线
     */
    @GetMapping("/websocket/online")
    @Operation(summary = "检查用户是否在线", description = "检查指定用户是否在线")
    public Result<Boolean> isUserOnline(
            @Parameter(description = "用户ID", required = true) @RequestParam String userId) {
        
        boolean online = webSocketHandler.isUserOnline(userId);
        return Result.success(online);
    }
}