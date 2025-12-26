package com.deyoch.controller;

import com.deyoch.entity.DeyochDocument;
import com.deyoch.result.Result;
import com.deyoch.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档管理控制器
 * 处理文档相关的HTTP请求
 */
@RestController
@RequestMapping("document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * 获取文档列表
     * @return 文档列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('document:list')")
    public Result<List<DeyochDocument>> getDocumentList() {
        return documentService.getDocumentList();
    }

    /**
     * 根据ID获取文档详情
     * @param id 文档ID
     * @return 文档详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('document:detail')")
    public Result<DeyochDocument> getDocumentById(@PathVariable Long id) {
        return documentService.getDocumentById(id);
    }

    /**
     * 根据部门ID获取文档列表
     * @param deptId 部门ID
     * @return 文档列表
     */
    @GetMapping("/dept/{deptId}")
    @PreAuthorize("hasAuthority('document:list')")
    public Result<List<DeyochDocument>> getDocumentsByDeptId(@PathVariable Long deptId) {
        return documentService.getDocumentsByDeptId(deptId);
    }

    /**
     * 创建文档
     * @param document 文档信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('document:add')")
    public Result<DeyochDocument> createDocument(@RequestBody DeyochDocument document) {
        return documentService.createDocument(document);
    }

    /**
     * 更新文档信息
     * @param id 文档ID
     * @param document 文档信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('document:update')")
    public Result<DeyochDocument> updateDocument(@PathVariable Long id, @RequestBody DeyochDocument document) {
        document.setId(id);
        return documentService.updateDocument(document);
    }

    /**
     * 删除文档
     * @param id 文档ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('document:delete')")
    public Result<Void> deleteDocument(@PathVariable Long id) {
        return documentService.deleteDocument(id);
    }

    /**
     * 更新文档状态
     * @param id 文档ID
     * @param status 文档状态
     * @return 更新结果
     */
    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('document:update-status')")
    public Result<Void> updateDocumentStatus(@PathVariable Long id, @RequestParam Long status) {
        return documentService.updateDocumentStatus(id, status);
    }

    /**
     * 上传文档
     * @param file 上传的文件
     * @param document 文档信息
     * @return 上传结果
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('document:upload')")
    public Result<DeyochDocument> uploadDocument(@RequestParam("file") MultipartFile file, DeyochDocument document) {
        return documentService.uploadDocument(file, document);
    }

    /**
     * 下载文档
     * @param id 文档ID
     * @return 下载结果
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('document:download')")
    public Result<String> downloadDocument(@PathVariable Long id) {
        return documentService.downloadDocument(id);
    }
}