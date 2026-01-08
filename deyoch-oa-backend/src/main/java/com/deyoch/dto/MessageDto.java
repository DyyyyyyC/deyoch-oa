package com.deyoch.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息数据传输对象
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Data
public class MessageDto {
    
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型：1-系统消息，2-审批通知，3-任务通知，4-公告通知
     */
    private Integer type;
    
    /**
     * 消息类型名称
     */
    private String typeName;
    
    /**
     * 优先级：1-普通，2-重要，3-紧急
     */
    private Integer priority;
    
    /**
     * 优先级名称
     */
    private String priorityName;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者姓名
     */
    private String senderName;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 接收者姓名
     */
    private String receiverName;
    
    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;
    
    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
    
    /**
     * 关联类型：task,process,announcement,document
     */
    private String relatedType;
    
    /**
     * 关联ID
     */
    private Long relatedId;
    
    /**
     * 关联对象标题
     */
    private String relatedTitle;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 复制消息对象（用于批量发送）
     */
    public MessageDto copy() {
        MessageDto copy = new MessageDto();
        copy.setTitle(this.title);
        copy.setContent(this.content);
        copy.setType(this.type);
        copy.setPriority(this.priority);
        copy.setSenderId(this.senderId);
        copy.setRelatedType(this.relatedType);
        copy.setRelatedId(this.relatedId);
        return copy;
    }
}