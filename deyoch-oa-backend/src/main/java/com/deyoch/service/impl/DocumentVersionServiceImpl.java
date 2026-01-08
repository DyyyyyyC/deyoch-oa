package com.deyoch.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deyoch.dto.DocumentVersionDto;
import com.deyoch.entity.DeyochDocumentVersion;
import com.deyoch.mapper.DocumentVersionMapper;
import com.deyoch.common.result.Result;
import com.deyoch.common.result.ResultCode;
import com.deyoch.service.DocumentVersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文档版本管理服务实现类
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Slf4j
@Service
public class DocumentVersionServiceImpl extends ServiceImpl<DocumentVersionMapper, DeyochDocumentVersion> 
        implements DocumentVersionService {
    
    @Autowired
    private DocumentVersionMapper documentVersionMapper;
    
    /**
     * 版本号正则表达式（支持 x.y.z 格式）
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?$");
    
    @Override
    public Result<DeyochDocumentVersion> createVersion(Long documentId, String version, String fileName, 
                                                      String filePath, Long fileSize, String md5Hash, 
                                                      String changeLog, Long createdBy) {
        try {
            // 参数校验
            if (documentId == null) {
                return Result.error(ResultCode.PARAM_IS_NULL, "文档ID不能为空");
            }
            if (!StringUtils.hasText(version)) {
                return Result.error(ResultCode.PARAM_IS_NULL, "版本号不能为空");
            }
            if (!StringUtils.hasText(fileName)) {
                return Result.error(ResultCode.PARAM_IS_NULL, "文件名不能为空");
            }
            if (!StringUtils.hasText(filePath)) {
                return Result.error(ResultCode.PARAM_IS_NULL, "文件路径不能为空");
            }
            if (createdBy == null) {
                return Result.error(ResultCode.PARAM_IS_NULL, "创建者ID不能为空");
            }
            
            // 检查版本是否已存在
            if (versionExists(documentId, version)) {
                return Result.error(ResultCode.DATA_DUPLICATE, "版本号已存在: " + version);
            }
            
            // 创建版本记录
            DeyochDocumentVersion versionEntity = new DeyochDocumentVersion();
            versionEntity.setDocumentId(documentId);
            versionEntity.setVersion(version);
            versionEntity.setFileName(fileName);
            versionEntity.setFilePath(filePath);
            versionEntity.setFileSize(fileSize);
            versionEntity.setMd5Hash(md5Hash);
            versionEntity.setChangeLog(changeLog);
            versionEntity.setCreatedBy(createdBy);
            versionEntity.setCreatedAt(LocalDateTime.now());
            
            // 保存到数据库
            boolean success = save(versionEntity);
            if (success) {
                log.info("创建文档版本成功 - 文档ID: {}, 版本: {}", documentId, version);
                return Result.success(versionEntity);
            } else {
                return Result.error(ResultCode.DATABASE_ERROR, "创建文档版本失败");
            }
            
        } catch (Exception e) {
            log.error("创建文档版本失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "创建文档版本失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<List<DocumentVersionDto>> getVersionHistory(Long documentId) {
        try {
            if (documentId == null) {
                return Result.error(ResultCode.PARAM_IS_NULL, "文档ID不能为空");
            }
            
            List<DocumentVersionDto> versions = documentVersionMapper.selectVersionsByDocumentId(documentId);
            
            // 设置格式化的文件大小
            versions.forEach(version -> {
                if (version.getFileSize() != null) {
                    version.setFileSizeFormatted(DocumentVersionDto.formatFileSize(version.getFileSize()));
                }
            });
            
            return Result.success(versions);
            
        } catch (Exception e) {
            log.error("获取文档版本历史失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "获取文档版本历史失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<DocumentVersionDto> getVersionInfo(Long documentId, String version) {
        try {
            if (documentId == null) {
                return Result.error(ResultCode.PARAM_IS_NULL, "文档ID不能为空");
            }
            if (!StringUtils.hasText(version)) {
                return Result.error(ResultCode.PARAM_IS_NULL, "版本号不能为空");
            }
            
            DocumentVersionDto versionInfo = documentVersionMapper
                    .selectVersionByDocumentIdAndVersion(documentId, version);
            
            if (versionInfo == null) {
                return Result.error(ResultCode.DATA_NOT_FOUND, "版本不存在");
            }
            
            // 设置格式化的文件大小
            if (versionInfo.getFileSize() != null) {
                versionInfo.setFileSizeFormatted(DocumentVersionDto.formatFileSize(versionInfo.getFileSize()));
            }
            
            return Result.success(versionInfo);
            
        } catch (Exception e) {
            log.error("获取版本信息失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "获取版本信息失败: " + e.getMessage());
        }
    }
    
    @Override
    public String generateNextVersion(String currentVersion) {
        if (!StringUtils.hasText(currentVersion)) {
            return "1.0";
        }
        
        try {
            Matcher matcher = VERSION_PATTERN.matcher(currentVersion.trim());
            if (matcher.matches()) {
                int major = Integer.parseInt(matcher.group(1));
                int minor = Integer.parseInt(matcher.group(2));
                String patch = matcher.group(3);
                
                if (patch != null) {
                    // x.y.z 格式，递增 patch 版本
                    int patchNum = Integer.parseInt(patch);
                    return String.format("%d.%d.%d", major, minor, patchNum + 1);
                } else {
                    // x.y 格式，递增 minor 版本
                    return String.format("%d.%d", major, minor + 1);
                }
            } else {
                // 无法解析的版本号，默认递增策略
                log.warn("无法解析版本号格式: {}, 使用默认策略", currentVersion);
                return currentVersion + ".1";
            }
        } catch (Exception e) {
            log.error("生成下一版本号失败: {}", currentVersion, e);
            return "1.0";
        }
    }
    
    @Override
    public boolean versionExists(Long documentId, String version) {
        try {
            return documentVersionMapper.existsVersion(documentId, version);
        } catch (Exception e) {
            log.error("检查版本是否存在失败", e);
            return false;
        }
    }
    
    @Override
    public Result<Void> deleteVersion(Long documentId, String version) {
        try {
            if (documentId == null) {
                return Result.error(ResultCode.PARAM_IS_NULL, "文档ID不能为空");
            }
            if (!StringUtils.hasText(version)) {
                return Result.error(ResultCode.PARAM_IS_NULL, "版本号不能为空");
            }
            
            // 检查版本是否存在
            if (!versionExists(documentId, version)) {
                return Result.error(ResultCode.DATA_NOT_FOUND, "版本不存在");
            }
            
            // 删除版本记录
            boolean success = remove(lambdaQuery()
                    .eq(DeyochDocumentVersion::getDocumentId, documentId)
                    .eq(DeyochDocumentVersion::getVersion, version));
            
            if (success) {
                log.info("删除文档版本成功 - 文档ID: {}, 版本: {}", documentId, version);
                return Result.success();
            } else {
                return Result.error(ResultCode.DATABASE_ERROR, "删除文档版本失败");
            }
            
        } catch (Exception e) {
            log.error("删除文档版本失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "删除文档版本失败: " + e.getMessage());
        }
    }
}