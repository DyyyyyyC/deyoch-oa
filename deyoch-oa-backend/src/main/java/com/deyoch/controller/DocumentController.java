package com.deyoch.controller;

import com.deyoch.entity.DeyochDocument;
import com.deyoch.result.Result;
import com.deyoch.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

/**
 * 文档管理控制器
 * 处理文档相关的HTTP请求
 */
@RestController
@RequestMapping("document")
@RequiredArgsConstructor
@Tag(name = "文档管理", description = "文档相关接口")
public class DocumentController {

    private final DocumentService documentService;

    /**
     * 获取文档列表
     * @return 文档列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('document:list')")
    @Operation(summary = "获取文档列表", description = "获取所有文档的列表")
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
    @Operation(summary = "根据ID获取文档详情", description = "根据文档ID获取文档的详细信息")
    public Result<DeyochDocument> getDocumentById(@PathVariable @Parameter(description = "文档ID") Long id) {
        return documentService.getDocumentById(id);
    }

    /**
     * 根据部门ID获取文档列表
     * @param deptId 部门ID
     * @return 文档列表
     */
    @GetMapping("/dept/{deptId}")
    @PreAuthorize("hasAuthority('document:list')")
    @Operation(summary = "根据部门ID获取文档列表", description = "根据部门ID获取该部门下的所有文档")
    public Result<List<DeyochDocument>> getDocumentsByDeptId(@PathVariable @Parameter(description = "部门ID") Long deptId) {
        return documentService.getDocumentsByDeptId(deptId);
    }

    /**
     * 创建文档
     * @param document 文档信息
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('document:add')")
    @Operation(summary = "创建文档", description = "创建新的文档")
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
    @Operation(summary = "更新文档信息", description = "根据文档ID更新文档信息")
    public Result<DeyochDocument> updateDocument(@PathVariable @Parameter(description = "文档ID") Long id, @RequestBody DeyochDocument document) {
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
    @Operation(summary = "删除文档", description = "根据文档ID删除文档")
    public Result<Void> deleteDocument(@PathVariable @Parameter(description = "文档ID") Long id) {
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
    @Operation(summary = "更新文档状态", description = "根据文档ID更新文档状态")
    public Result<Void> updateDocumentStatus(@PathVariable @Parameter(description = "文档ID") Long id, @RequestParam @Parameter(description = "文档状态") Long status) {
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
    @Operation(summary = "上传文档", description = "上传文档文件和信息")
    public Result<DeyochDocument> uploadDocument(@RequestParam("file") @Parameter(description = "上传的文件") MultipartFile file, @Parameter(description = "文档信息") DeyochDocument document) {
        return documentService.uploadDocument(file, document);
    }

    /**
     * 下载文档
     * @param id 文档ID
     * @return 下载结果
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('document:download')")
    @Operation(summary = "下载文档", description = "根据文档ID下载文档")
    public Result<String> downloadDocument(@PathVariable @Parameter(description = "文档ID") Long id) {
        return documentService.downloadDocument(id);
    }
}