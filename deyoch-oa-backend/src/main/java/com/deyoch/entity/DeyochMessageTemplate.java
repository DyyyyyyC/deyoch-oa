package com.deyoch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息模板实体类
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Data
@TableName("deyoch_message_template")
public class DeyochMessageTemplate {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 模板名称
     */
    private String name;
    
    /**
     * 模板类型
     */
    private String type;
    
    /**
     * 标题模板
     */
    private String titleTemplate;
    
    /**
     * 内容模板
     */
    private String contentTemplate;
    
    /**
     * 模板变量说明（JSON格式）
     */
    private String variables;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
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