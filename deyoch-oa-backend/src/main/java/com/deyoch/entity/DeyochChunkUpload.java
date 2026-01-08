package com.deyoch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分片上传信息实体类
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Data
@TableName("deyoch_chunk_upload")
public class DeyochChunkUpload {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 上传ID（UUID）
     */
    private String uploadId;
    
    /**
     * 原始文件名
     */
    private String fileName;
    
    /**
     * 文件总大小
     */
    private Long fileSize;
    
    /**
     * 分片大小
     */
    private Integer chunkSize;
    
    /**
     * 总分片数
     */
    private Integer totalChunks;
    
    /**
     * 已上传分片数
     */
    private Integer uploadedChunks;
    
    /**
     * 文件MD5哈希值
     */
    private String md5Hash;
    
    /**
     * 上传用户ID
     */
    private Long userId;
    
    /**
     * 状态：0-上传中，1-已完成，2-已取消
     */
    private Integer status;
    
    /**
     * 最终文件路径
     */
    private String finalPath;
    
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
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
}