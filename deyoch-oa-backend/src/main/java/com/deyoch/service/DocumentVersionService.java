package com.deyoch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deyoch.dto.DocumentVersionDto;
import com.deyoch.entity.DeyochDocumentVersion;
import com.deyoch.common.result.Result;

import java.util.List;

/**
 * 文档版本管理服务接口
 * 
 * @author deyoch
 * @since 2026-01-08
 */
public interface DocumentVersionService extends IService<DeyochDocumentVersion> {
    
    /**
     * 创建新版本记录
     * 
     * @param documentId 文档ID
     * @param version 版本号
     * @param fileName 文件名
     * @param filePath 文件路径
     * @param fileSize 文件大小
     * @param md5Hash MD5哈希值
     * @param changeLog 变更日志
     * @param createdBy 创建者ID
     * @return 创建结果
     */
    Result<DeyochDocumentVersion> createVersion(Long documentId, String version, String fileName, 
                                               String filePath, Long fileSize, String md5Hash, 
                                               String changeLog, Long createdBy);
    
    /**
     * 获取文档版本历史
     * 
     * @param documentId 文档ID
     * @return 版本历史列表
     */
    Result<List<DocumentVersionDto>> getVersionHistory(Long documentId);
    
    /**
     * 获取指定版本信息
     * 
     * @param documentId 文档ID
     * @param version 版本号
     * @return 版本信息
     */
    Result<DocumentVersionDto> getVersionInfo(Long documentId, String version);
    
    /**
     * 生成下一个版本号
     * 
     * @param currentVersion 当前版本号
     * @return 下一个版本号
     */
    String generateNextVersion(String currentVersion);
    
    /**
     * 检查版本是否存在
     * 
     * @param documentId 文档ID
     * @param version 版本号
     * @return 是否存在
     */
    boolean versionExists(Long documentId, String version);
    
    /**
     * 删除版本记录
     * 
     * @param documentId 文档ID
     * @param version 版本号
     * @return 删除结果
     */
    Result<Void> deleteVersion(Long documentId, String version);
}