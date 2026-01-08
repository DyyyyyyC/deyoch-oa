package com.deyoch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.deyoch.entity.DeyochDocument;
import com.deyoch.dto.DocumentVersionDto;
import com.deyoch.mapper.DeyochDocumentMapper;
import com.deyoch.common.result.PageResult;
import com.deyoch.common.result.Result;
import com.deyoch.common.result.ResultCode;
import com.deyoch.service.DocumentService;
import com.deyoch.service.DocumentVersionService;
import com.deyoch.service.UserInfoConverter;
import com.deyoch.utils.JwtUtil;
import com.deyoch.utils.UserContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 文档管理服务实现类
 * 实现文档管理相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl extends ServiceImpl<DeyochDocumentMapper, DeyochDocument> implements DocumentService {

    private final JwtUtil jwtUtil;
    private final UserInfoConverter userInfoConverter;
    private final DocumentVersionService documentVersionService;

    // 文档上传路径（从配置文件中获取）
    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public Result<PageResult<DeyochDocument>> getDocumentList(Integer page, Integer size, String keyword) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<DeyochDocument> queryWrapper = new LambdaQueryWrapper<>();
            
            // 添加关键词搜索条件（搜索文件名）
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.like(DeyochDocument::getFileName, keyword);
            }
            
            // 按创建时间倒序排列
            queryWrapper.orderByDesc(DeyochDocument::getCreatedAt);
            
            // 创建分页对象
            Page<DeyochDocument> pageObj = new Page<>(page, size);
            
            // 分页查询文档
            IPage<DeyochDocument> documentPage = page(pageObj, queryWrapper);
            List<DeyochDocument> documentList = documentPage.getRecords();
            
            // 使用UserInfoConverter填充上传者用户名
            userInfoConverter.<DeyochDocument>populateUserNames(
                documentList,
                // 用户ID提取器：从文档中提取userId
                document -> document.getUserId() != null ? 
                    Collections.singleton(document.getUserId()) : Collections.emptySet(),
                // 用户名设置器：将用户名设置到uploaderName字段
                (document, userIdToNameMap) -> {
                    if (document.getUserId() != null) {
                        String uploaderName = userIdToNameMap.get(document.getUserId());
                        document.setUploaderName(uploaderName);
                    }
                }
            );
            
            // 构建分页结果
            PageResult<DeyochDocument> pageResult = PageResult.of(
                documentPage.getCurrent(),
                documentPage.getSize(),
                documentPage.getTotal(),
                documentList
            );
            
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取文档列表失败，请稍后重试");
        }
    }

    @Override
    public Result<DeyochDocument> getDocumentById(Long id) {
        try {
            DeyochDocument document = getById(id);
            if (document == null) {
                return Result.error(ResultCode.DOCUMENT_NOT_FOUND, "文档不存在");
            }
            
            // 使用UserInfoConverter填充上传者用户名
            userInfoConverter.<DeyochDocument>populateUserNames(
                document,
                // 用户ID提取器：从文档中提取userId
                doc -> doc.getUserId() != null ? 
                    Collections.singleton(doc.getUserId()) : Collections.emptySet(),
                // 用户名设置器：将用户名设置到uploaderName字段
                (doc, userIdToNameMap) -> {
                    if (doc.getUserId() != null) {
                        String uploaderName = userIdToNameMap.get(doc.getUserId());
                        doc.setUploaderName(uploaderName);
                    }
                }
            );
            
            return Result.success(document);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取文档详情失败，请稍后重试");
        }
    }

    @Override
    public Result<List<DeyochDocument>> getDocumentsByDeptId(Long deptId) {
        try {
            // 根据部门ID查询文档，按创建时间倒序排列
            LambdaQueryWrapper<DeyochDocument> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochDocument::getDeptId, deptId);
            queryWrapper.orderByDesc(DeyochDocument::getCreatedAt);
            List<DeyochDocument> documentList = list(queryWrapper);
            
            // 使用UserInfoConverter填充上传者用户名
            userInfoConverter.<DeyochDocument>populateUserNames(
                documentList,
                // 用户ID提取器：从文档中提取userId
                document -> document.getUserId() != null ? 
                    Collections.singleton(document.getUserId()) : Collections.emptySet(),
                // 用户名设置器：将用户名设置到uploaderName字段
                (document, userIdToNameMap) -> {
                    if (document.getUserId() != null) {
                        String uploaderName = userIdToNameMap.get(document.getUserId());
                        document.setUploaderName(uploaderName);
                    }
                }
            );
            
            return Result.success(documentList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取部门文档列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochDocument> createDocument(DeyochDocument document) {
        try {
            // 从JWT token中获取用户ID
            Long userId = UserContextUtil.getUserIdFromToken(jwtUtil);
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "未登录或无效的令牌，无法创建文档");
            }
            
            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            document.setCreatedAt(now);
            document.setUpdatedAt(now);
            // 设置上传人ID
            document.setUserId(userId);
            // 默认状态为启用
            document.setStatus(1);
            // 创建文档
            save(document);
            return Result.success(document);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "创建文档失败，请稍后重试");
        }
    }

    @Override
    public Result<DeyochDocument> updateDocument(DeyochDocument document) {
        try {
            // 检查文档是否存在
            DeyochDocument existingDocument = getById(document.getId());
            if (existingDocument == null) {
                return Result.error(ResultCode.DOCUMENT_NOT_FOUND, "文档不存在");
            }
            // 设置更新时间
            document.setUpdatedAt(LocalDateTime.now());
            // 更新文档
            updateById(document);
            return Result.success(document);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新文档失败，请稍后重试");
        }
    }

    @Override
    public Result<Void> deleteDocument(Long id) {
        try {
            // 检查文档是否存在
            DeyochDocument document = getById(id);
            if (document == null) {
                return Result.error(ResultCode.DOCUMENT_NOT_FOUND, "文档不存在");
            }
            // 删除文档文件
            File file = new File(document.getFilePath());
            if (file.exists()) {
                file.delete();
            }
            // 删除文档记录
            removeById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "删除文档失败，请稍后重试");
        }
    }

    @Override
    public Result<Void> updateDocumentStatus(Long id, Integer status) {
        try {
            // 检查文档是否存在
            DeyochDocument document = getById(id);
            if (document == null) {
                return Result.error(ResultCode.DOCUMENT_NOT_FOUND, "文档不存在");
            }
            // 检查状态值是否合法
            if (status != 0 && status != 1) {
                return Result.error(ResultCode.PARAM_ERROR, "状态值不合法，只能是0或1");
            }
            // 更新状态
            document.setStatus(status);
            // 设置更新时间
            document.setUpdatedAt(LocalDateTime.now());
            // 更新文档
            updateById(document);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新文档状态失败，请稍后重试");
        }
    }

    @Override
    public Result<DeyochDocument> uploadDocument(MultipartFile file, DeyochDocument document) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return Result.error(ResultCode.PARAM_ERROR, "上传文件不能为空");
            }
            
            // 从JWT token中获取用户ID
            Long userId = UserContextUtil.getUserIdFromToken(jwtUtil);
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "未登录或无效的令牌，无法上传文档");
            }
            
            // 创建上传目录
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtWithDot = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + fileExtWithDot;
            
            // 保存文件
            File destFile = new File(uploadPath + File.separator + fileName);
            file.transferTo(destFile);
            
            // 处理文件类型，只存储文件扩展名而不是完整的MIME类型
            String fileExt = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            String fileType = fileExt.toLowerCase(); // 存储小写的扩展名，如 "docx", "pdf" 等
            
            // 设置文档信息
            document.setFileName(originalFilename);
            document.setFilePath(destFile.getAbsolutePath());
            document.setFileSize(file.getSize());
            document.setFileType(fileType);
            document.setUserId(userId);
            document.setCreatedAt(LocalDateTime.now());
            document.setUpdatedAt(LocalDateTime.now());
            document.setStatus(1);
            
            // 保存文档记录
            save(document);
            
            return Result.success(document);
        } catch (IOException e) {
            // 文件操作异常，返回用户友好的错误信息
            return Result.error(ResultCode.SYSTEM_ERROR, "文件上传失败，请检查文件是否损坏或重试");
        } catch (Exception e) {
            // 其他异常（如数据库异常），返回通用错误信息，不暴露具体错误
            return Result.error(ResultCode.SYSTEM_ERROR, "上传文档失败，请稍后重试");
        }
    }


    @Override
    public ResponseEntity<Resource> downloadDocument(Long id) {
        try {
            // 检查文档是否存在
            DeyochDocument document = getById(id);
            if (document == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 检查文件是否存在
            File file = new File(document.getFilePath());
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            // 创建文件资源
            Resource resource = new FileSystemResource(file);
            
            // 设置响应头
            String encodedFileName = URLEncoder.encode(document.getFileName(), StandardCharsets.UTF_8);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== 工作台专用方法实现 ====================

    public Result<List<DeyochDocument>> getRecentDocuments(Integer size) {
        try {
            // 获取当前用户ID
            Long userId = UserContextUtil.getUserIdFromToken(jwtUtil);
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "未登录或无效的令牌");
            }

            // 构建查询条件：获取最近上传或修改的文档
            LambdaQueryWrapper<DeyochDocument> queryWrapper = new LambdaQueryWrapper<>();
            // 可以查询当前用户上传的文档，或者查询所有文档（根据业务需求）
            // 这里查询所有启用状态的文档，按更新时间倒序排列
            queryWrapper.eq(DeyochDocument::getStatus, 1); // 只查询启用状态的文档
            queryWrapper.orderByDesc(DeyochDocument::getUpdatedAt);
            queryWrapper.last("LIMIT " + size);
            
            List<DeyochDocument> documentList = list(queryWrapper);
            
            // 填充用户名信息
            userInfoConverter.<DeyochDocument>populateUserNames(
                documentList,
                document -> document.getUserId() != null ? 
                    Collections.singleton(document.getUserId()) : Collections.emptySet(),
                (document, userIdToNameMap) -> {
                    if (document.getUserId() != null) {
                        String uploaderName = userIdToNameMap.get(document.getUserId());
                        document.setUploaderName(uploaderName);
                    }
                }
            );
            
            return Result.success(documentList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取最近文档失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteVersion(Long documentId, String version) {
        try {
            // 委托给DocumentVersionService处理
            return documentVersionService.deleteVersion(documentId, version);
        } catch (Exception e) {
            log.error("删除文档版本失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "删除文档版本失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> compareVersions(Long documentId, String version1, String version2) {
        try {
            // 这里可以实现版本比较逻辑
            // 目前返回一个简单的提示信息
            return Result.success("版本比较功能暂未实现，请使用专业的文档比较工具");
        } catch (Exception e) {
            log.error("比较文档版本失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "比较文档版本失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> revertToVersion(Long documentId, String version) {
        try {
            // 这里可以实现版本回退逻辑
            // 目前返回一个简单的提示信息
            log.info("文档版本回退请求 - 文档ID: {}, 目标版本: {}", documentId, version);
            return Result.success();
        } catch (Exception e) {
            log.error("回退文档版本失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "回退文档版本失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Resource> downloadDocumentVersion(Long documentId, String version) {
        try {
            // 这里可以实现版本文档下载逻辑
            // 目前返回404
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public Result<List<DocumentVersionDto>> getVersionHistory(Long documentId) {
        try {
            // 委托给DocumentVersionService处理
            return documentVersionService.getVersionHistory(documentId);
        } catch (Exception e) {
            log.error("获取文档版本历史失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "获取文档版本历史失败: " + e.getMessage());
        }
    }

    @Override
    public Result<DeyochDocument> uploadNewVersion(Long documentId, MultipartFile file, String changeLog) {
        try {
            // 这里可以实现新版本上传逻辑
            // 目前返回一个简单的提示
            log.info("上传新版本文档请求 - 文档ID: {}, 变更日志: {}", documentId, changeLog);
            return Result.error(ResultCode.SYSTEM_ERROR, "新版本上传功能暂未实现");
        } catch (Exception e) {
            log.error("上传新版本文档失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "上传新版本文档失败: " + e.getMessage());
        }
    }
}
