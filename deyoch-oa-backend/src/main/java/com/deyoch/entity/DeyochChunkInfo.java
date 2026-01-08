package com.deyoch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分片详情实体类
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Data
@TableName("deyoch_chunk_info")
public class DeyochChunkInfo {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 上传ID
     */
    private String uploadId;
    
    /**
     * 分片索引
     */
    private Integer chunkIndex;
    
    /**
     * 分片大小
     */
    private Integer chunkSize;
    
    /**
     * 分片文件路径
     */
    private String chunkPath;
    
    /**
     * 分片MD5哈希值
     */
    private String md5Hash;
    
    /**
     * 状态：0-失败，1-成功
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}