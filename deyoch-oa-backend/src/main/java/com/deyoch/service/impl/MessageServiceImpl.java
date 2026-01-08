package com.deyoch.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deyoch.common.result.PageResult;
import com.deyoch.common.result.Result;
import com.deyoch.common.result.ResultCode;
import com.deyoch.dto.MessageDto;
import com.deyoch.entity.DeyochMessage;
import com.deyoch.entity.DeyochMessageTemplate;
import com.deyoch.mapper.MessageMapper;
import com.deyoch.mapper.MessageTemplateMapper;
import com.deyoch.service.MessageService;
import com.deyoch.websocket.MessageWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息服务实现类
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {
    
    @Autowired
    private MessageMapper messageMapper;
    
    @Autowired
    private MessageTemplateMapper messageTemplateMapper;
    
    @Autowired
    private MessageWebSocketHandler webSocketHandler;
    
    /**
     * 模板变量替换的正则表达式
     */
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    
    @Override
    @Transactional
    public Result<Void> sendMessage(MessageDto message) {
        try {
            // 参数校验
            if (message.getReceiverId() == null) {
                return Result.error(ResultCode.PARAM_IS_NULL, "接收者ID不能为空");
            }
            if (!StringUtils.hasText(message.getTitle())) {
                return Result.error(ResultCode.PARAM_IS_NULL, "消息标题不能为空");
            }
            
            // 设置默认值
            if (message.getType() == null) {
                message.setType(1); // 默认系统消息
            }
            if (message.getPriority() == null) {
                message.setPriority(1); // 默认普通优先级
            }
            
            // 转换为实体对象
            DeyochMessage entity = new DeyochMessage();
            BeanUtils.copyProperties(message, entity);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            
            // 保存到数据库
            int result = messageMapper.insert(entity);
            if (result > 0) {
                // 设置消息ID
                message.setId(entity.getId());
                
                // 实时推送消息
                webSocketHandler.sendMessageToUser(message.getReceiverId().toString(), message);
                
                log.info("发送消息成功 - 接收者: {}, 标题: {}", message.getReceiverId(), message.getTitle());
                return Result.success();
            } else {
                return Result.error(ResultCode.DATABASE_ERROR, "发送消息失败");
            }
            
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "发送消息失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<Void> sendBatchMessage(List<Long> receiverIds, MessageDto message) {
        try {
            if (receiverIds == null || receiverIds.isEmpty()) {
                return Result.error(ResultCode.PARAM_IS_NULL, "接收者列表不能为空");
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (Long receiverId : receiverIds) {
                MessageDto userMessage = message.copy();
                userMessage.setReceiverId(receiverId);
                
                Result<Void> result = sendMessage(userMessage);
                if (ResultCode.isSuccess(result.getCode())) {
                    successCount++;
                } else {
                    failCount++;
                    log.warn("向用户 {} 发送消息失败: {}", receiverId, result.getMessage());
                }
            }
            
            log.info("批量发送消息完成 - 成功: {}, 失败: {}", successCount, failCount);
            
            if (failCount == 0) {
                return Result.success();
            } else if (successCount > 0) {
                return Result.success("部分发送成功，成功: " + successCount + ", 失败: " + failCount, null);
            } else {
                return Result.error(ResultCode.DATABASE_ERROR, "批量发送消息全部失败");
            }
            
        } catch (Exception e) {
            log.error("批量发送消息失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "批量发送消息失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Void> sendMessageByTemplate(String templateType, Long receiverId, Map<String, Object> variables) {
        try {
            // 查询消息模板
            DeyochMessageTemplate template = messageTemplateMapper.selectByType(templateType);
            if (template == null) {
                return Result.error(ResultCode.DATA_NOT_FOUND, "消息模板不存在: " + templateType);
            }
            
            if (template.getStatus() != 1) {
                return Result.error(ResultCode.DATA_NOT_FOUND, "消息模板已禁用: " + templateType);
            }
            
            // 替换模板变量
            String title = replaceTemplateVariables(template.getTitleTemplate(), variables);
            String content = replaceTemplateVariables(template.getContentTemplate(), variables);
            
            // 创建消息对象
            MessageDto message = new MessageDto();
            message.setTitle(title);
            message.setContent(content);
            message.setReceiverId(receiverId);
            
            // 根据模板类型设置消息类型
            message.setType(getMessageTypeByTemplate(templateType));
            message.setPriority(1); // 默认普通优先级
            
            // 发送消息
            return sendMessage(message);
            
        } catch (Exception e) {
            log.error("根据模板发送消息失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "根据模板发送消息失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<PageResult<MessageDto>> getUserMessages(Long userId, Integer page, Integer size, 
                                                         Integer type, Integer isRead) {
        try {
            // 参数校验
            if (userId == null) {
                return Result.error(ResultCode.PARAM_IS_NULL, "用户ID不能为空");
            }
            if (page == null || page < 1) {
                page = 1;
            }
            if (size == null || size < 1 || size > 100) {
                size = 20;
            }
            
            // 创建分页对象
            Page<MessageDto> pageObj = new Page<>(page, size);
            
            // 执行分页查询
            IPage<MessageDto> result = messageMapper.selectUserMessages(pageObj, userId, type, isRead);
            
            // 构建返回结果
            PageResult<MessageDto> pageResult = new PageResult<>();
            pageResult.setRecords(result.getRecords());
            pageResult.setTotal(result.getTotal());
            pageResult.setSize(result.getSize());
            pageResult.setCurrent(result.getCurrent());
            pageResult.setPages(result.getPages());
            
            return Result.success(pageResult);
            
        } catch (Exception e) {
            log.error("查询用户消息失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "查询用户消息失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<Void> markMessageAsRead(Long messageId, Long userId) {
        try {
            if (messageId == null || userId == null) {
                return Result.error(ResultCode.PARAM_IS_NULL, "消息ID和用户ID不能为空");
            }
            
            // 查询消息是否存在且属于该用户
            MessageDto message = messageMapper.selectMessageByIdAndUserId(messageId, userId);
            if (message == null) {
                return Result.error(ResultCode.DATA_NOT_FOUND, "消息不存在或无权限");
            }
            
            if (message.getIsRead() == 1) {
                return Result.success("消息已经是已读状态", null);
            }
            
            // 更新消息状态
            DeyochMessage entity = new DeyochMessage();
            entity.setId(messageId);
            entity.setIsRead(1);
            entity.setReadTime(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            
            int result = messageMapper.updateById(entity);
            if (result > 0) {
                log.info("标记消息已读成功 - 消息ID: {}, 用户ID: {}", messageId, userId);
                return Result.success();
            } else {
                return Result.error(ResultCode.DATABASE_ERROR, "标记消息已读失败");
            }
            
        } catch (Exception e) {
            log.error("标记消息已读失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "标记消息已读失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<Void> markMessagesAsRead(List<Long> messageIds, Long userId) {
        try {
            if (messageIds == null || messageIds.isEmpty() || userId == null) {
                return Result.error(ResultCode.PARAM_IS_NULL, "消息ID列表和用户ID不能为空");
            }
            
            int result = messageMapper.batchMarkAsRead(messageIds, userId);
            
            log.info("批量标记消息已读 - 用户ID: {}, 消息数量: {}, 更新数量: {}", userId, messageIds.size(), result);
            return Result.success("成功标记 " + result + " 条消息为已读", null);
            
        } catch (Exception e) {
            log.error("批量标记消息已读失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "批量标记消息已读失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<Void> deleteMessage(Long messageId, Long userId) {
        try {
            if (messageId == null || userId == null) {
                return Result.error(ResultCode.PARAM_IS_NULL, "消息ID和用户ID不能为空");
            }
            
            // 查询消息是否存在且属于该用户
            MessageDto message = messageMapper.selectMessageByIdAndUserId(messageId, userId);
            if (message == null) {
                return Result.error(ResultCode.DATA_NOT_FOUND, "消息不存在或无权限");
            }
            
            // 删除消息
            int result = messageMapper.deleteById(messageId);
            if (result > 0) {
                log.info("删除消息成功 - 消息ID: {}, 用户ID: {}", messageId, userId);
                return Result.success();
            } else {
                return Result.error(ResultCode.DATABASE_ERROR, "删除消息失败");
            }
            
        } catch (Exception e) {
            log.error("删除消息失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "删除消息失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Long> getUnreadMessageCount(Long userId) {
        try {
            if (userId == null) {
                return Result.error(ResultCode.PARAM_IS_NULL, "用户ID不能为空");
            }
            
            Long count = messageMapper.selectUnreadMessageCount(userId);
            return Result.success(count != null ? count : 0L);
            
        } catch (Exception e) {
            log.error("获取未读消息数量失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "获取未读消息数量失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Map<Integer, Long>> getUnreadMessageCountByType(Long userId) {
        try {
            if (userId == null) {
                return Result.error(ResultCode.PARAM_IS_NULL, "用户ID不能为空");
            }
            
            List<Map<String, Object>> results = messageMapper.selectUnreadMessageCountByType(userId);
            
            Map<Integer, Long> countMap = new HashMap<>();
            for (Map<String, Object> result : results) {
                Integer type = (Integer) result.get("type");
                Long count = ((Number) result.get("count")).longValue();
                countMap.put(type, count);
            }
            
            return Result.success(countMap);
            
        } catch (Exception e) {
            log.error("获取各类型未读消息数量失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "获取各类型未读消息数量失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<Void> clearAllMessages(Long userId) {
        try {
            if (userId == null) {
                return Result.error(ResultCode.PARAM_IS_NULL, "用户ID不能为空");
            }
            
            // 删除用户所有消息
            messageMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DeyochMessage>()
                    .eq("receiver_id", userId));
            
            log.info("清空用户所有消息成功 - 用户ID: {}", userId);
            return Result.success();
            
        } catch (Exception e) {
            log.error("清空用户所有消息失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "清空用户所有消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 替换模板变量
     * 
     * @param template 模板字符串
     * @param variables 变量Map
     * @return 替换后的字符串
     */
    private String replaceTemplateVariables(String template, Map<String, Object> variables) {
        if (!StringUtils.hasText(template) || variables == null || variables.isEmpty()) {
            return template;
        }
        
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = variables.get(variableName);
            String replacement = value != null ? value.toString() : "{" + variableName + "}";
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * 根据模板类型获取消息类型
     * 
     * @param templateType 模板类型
     * @return 消息类型
     */
    private Integer getMessageTypeByTemplate(String templateType) {
        switch (templateType) {
            case "task_assign":
            case "task_update":
                return 3; // 任务通知
            case "process_approve":
            case "process_complete":
                return 2; // 审批通知
            case "announcement_publish":
                return 4; // 公告通知
            default:
                return 1; // 系统消息
        }
    }
}