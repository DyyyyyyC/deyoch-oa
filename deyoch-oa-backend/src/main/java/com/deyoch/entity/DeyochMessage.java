package com.deyoch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息实体类
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Data
@TableName("deyoch_message")
public class DeyochMessage {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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
     * 优先级：1-普通，2-重要，3-紧急
     */
    private Integer priority;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
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
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}