package com.deyoch.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分片上传数据传输对象
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Data
public class ChunkUploadDto {
    
    /**
     * 上传ID
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
     * 上传进度（百分比）
     */
    private Double progress;
    
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
     * 状态描述
     */
    private String statusText;
    
    /**
     * 最终文件路径
     */
    private String finalPath;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
    
    /**
     * 计算上传进度
     */
    public void calculateProgress() {
        if (totalChunks != null && totalChunks > 0 && uploadedChunks != null) {
            this.progress = (double) uploadedChunks / totalChunks * 100;
        } else {
            this.progress = 0.0;
        }
    }
    
    /**
     * 设置状态描述
     */
    public void setStatusText() {
        if (status == null) {
            this.statusText = "未知";
            return;
        }
        
        switch (status) {
            case 0:
                this.statusText = "上传中";
                break;
            case 1:
                this.statusText = "已完成";
                break;
            case 2:
                this.statusText = "已取消";
                break;
            default:
                this.statusText = "未知";
        }
    }
}