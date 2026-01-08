package com.deyoch.service;

import com.deyoch.dto.ChunkUploadDto;
import com.deyoch.common.result.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * 分片上传服务接口
 * 
 * @author deyoch
 * @since 2026-01-08
 */
public interface ChunkUploadService {
    
    /**
     * 初始化分片上传
     * 
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param totalChunks 总分片数
     * @param md5Hash 文件MD5哈希值
     * @param userId 用户ID
     * @return 上传信息
     */
    Result<ChunkUploadDto> initChunkUpload(String fileName, Long fileSize, 
                                          Integer totalChunks, String md5Hash, Long userId);
    
    /**
     * 上传分片
     * 
     * @param uploadId 上传ID
     * @param chunkIndex 分片索引
     * @param chunk 分片文件
     * @param chunkMd5 分片MD5哈希值
     * @return 上传结果
     */
    Result<Void> uploadChunk(String uploadId, Integer chunkIndex, 
                            MultipartFile chunk, String chunkMd5);
    
    /**
     * 合并分片
     * 
     * @param uploadId 上传ID
     * @return 合并结果，包含最终文件路径
     */
    Result<String> mergeChunks(String uploadId);
    
    /**
     * 获取上传进度
     * 
     * @param uploadId 上传ID
     * @return 上传进度信息
     */
    Result<ChunkUploadDto> getUploadProgress(String uploadId);
    
    /**
     * 取消上传
     * 
     * @param uploadId 上传ID
     * @return 取消结果
     */
    Result<Void> cancelUpload(String uploadId);
    
    /**
     * 检查分片是否已上传
     * 
     * @param uploadId 上传ID
     * @param chunkIndex 分片索引
     * @return 是否已上传
     */
    Result<Boolean> checkChunkExists(String uploadId, Integer chunkIndex);
    
    /**
     * 清理过期的上传记录
     * 
     * @return 清理数量
     */
    Result<Integer> cleanExpiredUploads();
}