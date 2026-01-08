package com.deyoch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文档版本历史实体类
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Data
@TableName("deyoch_document_version")
public class DeyochDocumentVersion {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 文档ID
     */
    private Long documentId;
    
    /**
     * 版本号
     */
    private String version;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件路径
     */
    private String filePath;
    
    /**
     * 文件大小
     */
    private Long fileSize;
    
    /**
     * 文件MD5哈希值
     */
    private String md5Hash;
    
    /**
     * 变更日志
     */
    private String changeLog;
    
    /**
     * 创建者
     */
    private Long createdBy;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}