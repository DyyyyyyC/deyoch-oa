package com.deyoch.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文档版本数据传输对象
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Data
public class DocumentVersionDto {
    
    /**
     * 版本记录ID
     */
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
     * 文件大小（格式化）
     */
    private String fileSizeFormatted;
    
    /**
     * 文件MD5哈希值
     */
    private String md5Hash;
    
    /**
     * 变更日志
     */
    private String changeLog;
    
    /**
     * 创建者ID
     */
    private Long createdBy;
    
    /**
     * 创建者姓名
     */
    private String creatorName;
    
    /**
     * 是否当前版本
     */
    private Boolean isCurrent;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 格式化文件大小
     * 
     * @param size 文件大小（字节）
     * @return 格式化后的大小
     */
    public static String formatFileSize(Long size) {
        if (size == null || size <= 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double fileSize = size.doubleValue();
        
        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", fileSize, units[unitIndex]);
    }
    
    /**
     * 设置文件大小并自动格式化
     * 
     * @param fileSize 文件大小
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
        this.fileSizeFormatted = formatFileSize(fileSize);
    }
}