package com.deyoch.controller;

import com.deyoch.dto.ChunkUploadDto;
import com.deyoch.common.result.Result;
import com.deyoch.service.ChunkUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 分片上传控制器
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Slf4j
@RestController
@RequestMapping("/upload/chunk")
@Tag(name = "分片上传", description = "大文件分片上传相关接口")
public class ChunkUploadController {
    
    @Autowired
    private ChunkUploadService chunkUploadService;
    
    /**
     * 初始化分片上传
     */
    @PostMapping("/init")
    @Operation(summary = "初始化分片上传", description = "创建分片上传任务")
    public Result<ChunkUploadDto> initChunkUpload(
            @Parameter(description = "文件名", required = true) @RequestParam String fileName,
            @Parameter(description = "文件大小", required = true) @RequestParam Long fileSize,
            @Parameter(description = "总分片数", required = true) @RequestParam Integer totalChunks,
            @Parameter(description = "文件MD5哈希值") @RequestParam(required = false) String md5Hash,
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        
        log.info("初始化分片上传 - 文件: {}, 大小: {}, 分片数: {}, 用户: {}", 
                fileName, fileSize, totalChunks, userId);
        
        return chunkUploadService.initChunkUpload(fileName, fileSize, totalChunks, md5Hash, userId);
    }
    
    /**
     * 上传分片
     */
    @PostMapping("/upload")
    @Operation(summary = "上传分片", description = "上传单个分片文件")
    public Result<Void> uploadChunk(
            @Parameter(description = "上传ID", required = true) @RequestParam String uploadId,
            @Parameter(description = "分片索引", required = true) @RequestParam Integer chunkIndex,
            @Parameter(description = "分片文件", required = true) @RequestParam("chunk") MultipartFile chunk,
            @Parameter(description = "分片MD5哈希值") @RequestParam(required = false) String chunkMd5) {
        
        log.debug("上传分片 - 上传ID: {}, 分片索引: {}, 大小: {}", 
                uploadId, chunkIndex, chunk.getSize());
        
        return chunkUploadService.uploadChunk(uploadId, chunkIndex, chunk, chunkMd5);
    }
    
    /**
     * 合并分片
     */
    @PostMapping("/merge")
    @Operation(summary = "合并分片", description = "合并所有分片为完整文件")
    public Result<String> mergeChunks(
            @Parameter(description = "上传ID", required = true) @RequestParam String uploadId) {
        
        log.info("合并分片 - 上传ID: {}", uploadId);
        
        return chunkUploadService.mergeChunks(uploadId);
    }
    
    /**
     * 获取上传进度
     */
    @GetMapping("/progress")
    @Operation(summary = "获取上传进度", description = "查询分片上传进度")
    public Result<ChunkUploadDto> getUploadProgress(
            @Parameter(description = "上传ID", required = true) @RequestParam String uploadId) {
        
        return chunkUploadService.getUploadProgress(uploadId);
    }
    
    /**
     * 取消上传
     */
    @PostMapping("/cancel")
    @Operation(summary = "取消上传", description = "取消分片上传任务")
    public Result<Void> cancelUpload(
            @Parameter(description = "上传ID", required = true) @RequestParam String uploadId) {
        
        log.info("取消上传 - 上传ID: {}", uploadId);
        
        return chunkUploadService.cancelUpload(uploadId);
    }
    
    /**
     * 检查分片是否存在
     */
    @GetMapping("/check")
    @Operation(summary = "检查分片是否存在", description = "检查指定分片是否已上传")
    public Result<Boolean> checkChunkExists(
            @Parameter(description = "上传ID", required = true) @RequestParam String uploadId,
            @Parameter(description = "分片索引", required = true) @RequestParam Integer chunkIndex) {
        
        return chunkUploadService.checkChunkExists(uploadId, chunkIndex);
    }
    
    /**
     * 清理过期上传记录（管理员接口）
     */
    @PostMapping("/cleanup")
    @Operation(summary = "清理过期上传记录", description = "清理过期的分片上传记录和临时文件")
    public Result<Integer> cleanExpiredUploads() {
        log.info("开始清理过期上传记录");
        
        return chunkUploadService.cleanExpiredUploads();
    }
}