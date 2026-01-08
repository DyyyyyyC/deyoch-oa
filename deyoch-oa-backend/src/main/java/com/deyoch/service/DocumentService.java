package com.deyoch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deyoch.entity.DeyochDocument;
import com.deyoch.dto.DocumentVersionDto;
import com.deyoch.common.result.PageResult;
import com.deyoch.common.result.Result;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档管理服务接口
 * 定义文档管理相关的业务逻辑方法
 */
public interface DocumentService extends IService<DeyochDocument> {

    /**
     * 获取文档列表
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词
     * @return 分页文档列表
     */
    Result<PageResult<DeyochDocument>> getDocumentList(Integer page, Integer size, String keyword);

    /**
     * 根据ID获取文档详情
     * @param id 文档ID
     * @return 文档详情
     */
    Result<DeyochDocument> getDocumentById(Long id);

    /**
     * 根据部门ID获取文档列表
     * @param deptId 部门ID
     * @return 文档列表
     */
    Result<List<DeyochDocument>> getDocumentsByDeptId(Long deptId);

    /**
     * 创建文档
     * @param document 文档信息
     * @return 创建结果
     */
    Result<DeyochDocument> createDocument(DeyochDocument document);

    /**
     * 更新文档信息
     * @param document 文档信息
     * @return 更新结果
     */
    Result<DeyochDocument> updateDocument(DeyochDocument document);

    /**
     * 删除文档
     * @param id 文档ID
     * @return 删除结果
     */
    Result<Void> deleteDocument(Long id);

    /**
     * 更新文档状态
     * @param id 文档ID
     * @param status 文档状态
     * @return 更新结果
     */
    Result<Void> updateDocumentStatus(Long id, Integer status);

    /**
     * 上传文档
     * @param file 上传的文件
     * @param document 文档信息
     * @return 上传结果
     */
    Result<DeyochDocument> uploadDocument(MultipartFile file, DeyochDocument document);

    /**
     * 下载文档
     * @param id 文档ID
     * @return 文件流响应
     */
    ResponseEntity<Resource> downloadDocument(Long id);

    // ========== 版本管理功能 ==========

    /**
     * 上传新版本文档
     * @param documentId 原文档ID
     * @param file 新版本文件
     * @param changeLog 变更日志
     * @return 新版本文档
     */
    Result<DeyochDocument> uploadNewVersion(Long documentId, MultipartFile file, String changeLog);

    /**
     * 获取文档版本历史
     * @param documentId 文档ID
     * @return 版本历史列表
     */
    Result<List<DocumentVersionDto>> getVersionHistory(Long documentId);

    /**
     * 下载指定版本的文档
     * @param documentId 文档ID
     * @param version 版本号
     * @return 文件流响应
     */
    ResponseEntity<Resource> downloadDocumentVersion(Long documentId, String version);

    /**
     * 回退到指定版本
     * @param documentId 文档ID
     * @param version 目标版本号
     * @return 操作结果
     */
    Result<Void> revertToVersion(Long documentId, String version);

    /**
     * 比较两个版本的差异
     * @param documentId 文档ID
     * @param version1 版本1
     * @param version2 版本2
     * @return 差异信息
     */
    Result<String> compareVersions(Long documentId, String version1, String version2);

    /**
     * 删除指定版本
     * @param documentId 文档ID
     * @param version 版本号
     * @return 操作结果
     */
    Result<Void> deleteVersion(Long documentId, String version);
}