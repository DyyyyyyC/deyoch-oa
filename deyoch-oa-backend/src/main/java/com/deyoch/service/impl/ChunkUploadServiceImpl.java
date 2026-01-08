package com.deyoch.service.impl;

import com.deyoch.dto.ChunkUploadDto;
import com.deyoch.entity.DeyochChunkInfo;
import com.deyoch.entity.DeyochChunkUpload;
import com.deyoch.mapper.ChunkInfoMapper;
import com.deyoch.mapper.ChunkUploadMapper;
import com.deyoch.common.result.Result;
import com.deyoch.common.result.ResultCode;
import com.deyoch.service.ChunkUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 分片上传服务实现类
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Slf4j
@Service
public class ChunkUploadServiceImpl implements ChunkUploadService {
    
    @Autowired
    private ChunkUploadMapper chunkUploadMapper;
    
    @Autowired
    private ChunkInfoMapper chunkInfoMapper;
    
    /**
     * 文件上传根目录
     */
    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadPath;
    
    /**
     * 分片临时目录
     */
    @Value("${file.chunk.temp-path:/tmp/chunks}")
    private String chunkTempPath;
    
    /**
     * 上传过期时间（小时）
     */
    @Value("${file.chunk.expire-hours:24}")
    private Integer expireHours;
    
    /**
     * 默认分片大小（2MB）
     */
    private static final int DEFAULT_CHUNK_SIZE = 2 * 1024 * 1024;
    
    @Override
    @Transactional
    public Result<ChunkUploadDto> initChunkUpload(String fileName, Long fileSize, 
                                                 Integer totalChunks, String md5Hash, Long userId) {
        try {
            // 参数校验
            if (!StringUtils.hasText(fileName)) {
                return Result.error(ResultCode.PARAM_IS_NULL, "文件名不能为空");
            }
            if (fileSize == null || fileSize <= 0) {
                return Result.error(ResultCode.PARAM_ERROR, "文件大小必须大于0");
            }
            if (totalChunks == null || totalChunks <= 0) {
                return Result.error(ResultCode.PARAM_ERROR, "分片数量必须大于0");
            }
            if (userId == null) {
                return Result.error(ResultCode.PARAM_IS_NULL, "用户ID不能为空");
            }
            
            // 生成上传ID
            String uploadId = UUID.randomUUID().toString().replace("-", "");
            
            // 计算分片大小
            int chunkSize = (int) Math.ceil((double) fileSize / totalChunks);
            if (chunkSize > DEFAULT_CHUNK_SIZE * 2) {
                // 如果分片过大，重新计算分片数量
                totalChunks = (int) Math.ceil((double) fileSize / DEFAULT_CHUNK_SIZE);
                chunkSize = DEFAULT_CHUNK_SIZE;
            }
            
            // 创建上传记录
            DeyochChunkUpload upload = new DeyochChunkUpload();
            upload.setUploadId(uploadId);
            upload.setFileName(fileName);
            upload.setFileSize(fileSize);
            upload.setChunkSize(chunkSize);
            upload.setTotalChunks(totalChunks);
            upload.setUploadedChunks(0);
            upload.setMd5Hash(md5Hash);
            upload.setUserId(userId);
            upload.setStatus(0); // 上传中
            upload.setCreatedAt(LocalDateTime.now());
            upload.setUpdatedAt(LocalDateTime.now());
            upload.setExpiresAt(LocalDateTime.now().plusHours(expireHours));
            
            // 保存到数据库
            chunkUploadMapper.insert(upload);
            
            // 创建临时目录
            createChunkTempDirectory(uploadId);
            
            // 构建返回结果
            ChunkUploadDto result = new ChunkUploadDto();
            result.setUploadId(uploadId);
            result.setFileName(fileName);
            result.setFileSize(fileSize);
            result.setChunkSize(chunkSize);
            result.setTotalChunks(totalChunks);
            result.setUploadedChunks(0);
            result.setMd5Hash(md5Hash);
            result.setUserId(userId);
            result.setStatus(0);
            result.setCreatedAt(upload.getCreatedAt());
            result.setExpiresAt(upload.getExpiresAt());
            result.calculateProgress();
            result.setStatusText();
            
            log.info("初始化分片上传成功 - 上传ID: {}, 文件: {}, 大小: {}, 分片数: {}", 
                    uploadId, fileName, fileSize, totalChunks);
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("初始化分片上传失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "初始化分片上传失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<Void> uploadChunk(String uploadId, Integer chunkIndex, 
                                   MultipartFile chunk, String chunkMd5) {
        try {
            // 参数校验
            if (!StringUtils.hasText(uploadId)) {
                return Result.error(ResultCode.PARAM_IS_NULL, "上传ID不能为空");
            }
            if (chunkIndex == null || chunkIndex < 0) {
                return Result.error(ResultCode.PARAM_ERROR, "分片索引无效");
            }
            if (chunk == null || chunk.isEmpty()) {
                return Result.error(ResultCode.PARAM_IS_NULL, "分片文件不能为空");
            }
            
            // 检查上传记录是否存在
            ChunkUploadDto uploadInfo = chunkUploadMapper.selectUploadInfoByUploadId(uploadId);
            if (uploadInfo == null) {
                return Result.error(ResultCode.DATA_NOT_FOUND, "上传记录不存在");
            }
            
            if (uploadInfo.getStatus() != 0) {
                return Result.error(ResultCode.OPERATION_NOT_ALLOWED, "上传已完成或已取消");
            }
            
            // 检查分片索引是否有效
            if (chunkIndex >= uploadInfo.getTotalChunks()) {
                return Result.error(ResultCode.PARAM_ERROR, "分片索引超出范围");
            }
            
            // 检查分片是否已上传
            if (chunkInfoMapper.existsChunk(uploadId, chunkIndex)) {
                return Result.success(); // 分片已存在，直接返回成功
            }
            
            // 验证分片MD5（如果提供）
            if (StringUtils.hasText(chunkMd5)) {
                String actualMd5 = DigestUtils.md5DigestAsHex(chunk.getInputStream());
                if (!chunkMd5.equalsIgnoreCase(actualMd5)) {
                    return Result.error(ResultCode.PARAM_VALID_ERROR, "分片MD5校验失败");
                }
            }
            
            // 保存分片文件
            String chunkPath = saveChunkFile(uploadId, chunkIndex, chunk);
            
            // 记录分片信息
            DeyochChunkInfo chunkInfo = new DeyochChunkInfo();
            chunkInfo.setUploadId(uploadId);
            chunkInfo.setChunkIndex(chunkIndex);
            chunkInfo.setChunkSize((int) chunk.getSize());
            chunkInfo.setChunkPath(chunkPath);
            chunkInfo.setMd5Hash(chunkMd5);
            chunkInfo.setStatus(1); // 成功
            chunkInfo.setCreatedAt(LocalDateTime.now());
            
            chunkInfoMapper.insert(chunkInfo);
            
            // 更新已上传分片数
            int uploadedChunks = chunkInfoMapper.countUploadedChunks(uploadId);
            chunkUploadMapper.updateUploadedChunks(uploadId, uploadedChunks);
            
            log.debug("分片上传成功 - 上传ID: {}, 分片索引: {}, 大小: {}", 
                    uploadId, chunkIndex, chunk.getSize());
            
            return Result.success();
            
        } catch (Exception e) {
            log.error("分片上传失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "分片上传失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<String> mergeChunks(String uploadId) {
        try {
            // 参数校验
            if (!StringUtils.hasText(uploadId)) {
                return Result.error(ResultCode.PARAM_IS_NULL, "上传ID不能为空");
            }
            
            // 检查上传记录
            ChunkUploadDto uploadInfo = chunkUploadMapper.selectUploadInfoByUploadId(uploadId);
            if (uploadInfo == null) {
                return Result.error(ResultCode.DATA_NOT_FOUND, "上传记录不存在");
            }
            
            if (uploadInfo.getStatus() != 0) {
                return Result.error(ResultCode.OPERATION_NOT_ALLOWED, "上传已完成或已取消");
            }
            
            // 检查所有分片是否已上传
            int uploadedChunks = chunkInfoMapper.countUploadedChunks(uploadId);
            if (uploadedChunks != uploadInfo.getTotalChunks()) {
                return Result.error(ResultCode.PARAM_VALID_ERROR, "分片上传不完整，已上传: " + uploadedChunks + 
                                  ", 总数: " + uploadInfo.getTotalChunks());
            }
            
            // 合并分片文件
            String finalPath = mergeChunkFiles(uploadId, uploadInfo);
            
            // 验证合并后的文件MD5（如果提供）
            if (StringUtils.hasText(uploadInfo.getMd5Hash())) {
                String actualMd5 = calculateFileMd5(finalPath);
                if (!uploadInfo.getMd5Hash().equalsIgnoreCase(actualMd5)) {
                    // 删除合并失败的文件
                    deleteFile(finalPath);
                    return Result.error(ResultCode.PARAM_VALID_ERROR, "文件MD5校验失败");
                }
            }
            
            // 更新上传状态
            chunkUploadMapper.updateStatusAndFinalPath(uploadId, 1, finalPath);
            
            // 清理临时分片文件
            cleanupChunkFiles(uploadId);
            
            log.info("分片合并成功 - 上传ID: {}, 最终路径: {}", uploadId, finalPath);
            
            return Result.success(finalPath);
            
        } catch (Exception e) {
            log.error("分片合并失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "分片合并失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<ChunkUploadDto> getUploadProgress(String uploadId) {
        try {
            if (!StringUtils.hasText(uploadId)) {
                return Result.error(ResultCode.PARAM_IS_NULL, "上传ID不能为空");
            }
            
            ChunkUploadDto uploadInfo = chunkUploadMapper.selectUploadInfoByUploadId(uploadId);
            if (uploadInfo == null) {
                return Result.error(ResultCode.DATA_NOT_FOUND, "上传记录不存在");
            }
            
            // 计算进度和设置状态描述
            uploadInfo.calculateProgress();
            uploadInfo.setStatusText();
            
            return Result.success(uploadInfo);
            
        } catch (Exception e) {
            log.error("获取上传进度失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "获取上传进度失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<Void> cancelUpload(String uploadId) {
        try {
            if (!StringUtils.hasText(uploadId)) {
                return Result.error(ResultCode.PARAM_IS_NULL, "上传ID不能为空");
            }
            
            // 更新状态为已取消
            chunkUploadMapper.updateStatusAndFinalPath(uploadId, 2, null);
            
            // 清理临时文件
            cleanupChunkFiles(uploadId);
            
            log.info("取消上传成功 - 上传ID: {}", uploadId);
            
            return Result.success();
            
        } catch (Exception e) {
            log.error("取消上传失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "取消上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Boolean> checkChunkExists(String uploadId, Integer chunkIndex) {
        try {
            if (!StringUtils.hasText(uploadId)) {
                return Result.error(ResultCode.PARAM_IS_NULL, "上传ID不能为空");
            }
            if (chunkIndex == null || chunkIndex < 0) {
                return Result.error(ResultCode.PARAM_ERROR, "分片索引无效");
            }
            
            boolean exists = chunkInfoMapper.existsChunk(uploadId, chunkIndex);
            return Result.success(exists);
            
        } catch (Exception e) {
            log.error("检查分片是否存在失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "检查分片是否存在失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<Integer> cleanExpiredUploads() {
        try {
            LocalDateTime expireTime = LocalDateTime.now();
            
            // 查询过期的上传记录
            List<String> expiredUploadIds = chunkUploadMapper.selectExpiredUploadIds(expireTime);
            
            // 清理临时文件
            for (String uploadId : expiredUploadIds) {
                cleanupChunkFiles(uploadId);
            }
            
            // 删除过期记录
            int deletedCount = chunkUploadMapper.deleteExpiredUploads(expireTime);
            
            log.info("清理过期上传记录完成 - 删除数量: {}", deletedCount);
            
            return Result.success(deletedCount);
            
        } catch (Exception e) {
            log.error("清理过期上传记录失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "清理过期上传记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建分片临时目录
     */
    private void createChunkTempDirectory(String uploadId) throws IOException {
        Path tempDir = Paths.get(chunkTempPath, uploadId);
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }
    }
    
    /**
     * 保存分片文件
     */
    private String saveChunkFile(String uploadId, Integer chunkIndex, MultipartFile chunk) throws IOException {
        Path tempDir = Paths.get(chunkTempPath, uploadId);
        String chunkFileName = String.format("chunk_%d", chunkIndex);
        Path chunkPath = tempDir.resolve(chunkFileName);
        
        chunk.transferTo(chunkPath.toFile());
        
        return chunkPath.toString();
    }
    
    /**
     * 合并分片文件
     */
    private String mergeChunkFiles(String uploadId, ChunkUploadDto uploadInfo) throws IOException {
        // 创建最终文件路径
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        String finalFileName = System.currentTimeMillis() + "_" + uploadInfo.getFileName();
        Path finalPath = uploadDir.resolve(finalFileName);
        
        // 获取所有分片信息
        List<DeyochChunkInfo> chunks = chunkInfoMapper.selectChunksByUploadId(uploadId);
        chunks.sort((a, b) -> Integer.compare(a.getChunkIndex(), b.getChunkIndex()));
        
        // 合并分片
        try (FileOutputStream fos = new FileOutputStream(finalPath.toFile());
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            
            for (DeyochChunkInfo chunkInfo : chunks) {
                Path chunkPath = Paths.get(chunkInfo.getChunkPath());
                if (Files.exists(chunkPath)) {
                    Files.copy(chunkPath, bos);
                } else {
                    throw new IOException("分片文件不存在: " + chunkPath);
                }
            }
        }
        
        return finalPath.toString();
    }
    
    /**
     * 清理分片文件
     */
    private void cleanupChunkFiles(String uploadId) {
        try {
            // 删除分片信息记录
            chunkInfoMapper.deleteChunksByUploadId(uploadId);
            
            // 删除临时目录
            Path tempDir = Paths.get(chunkTempPath, uploadId);
            if (Files.exists(tempDir)) {
                Files.walk(tempDir)
                     .sorted((a, b) -> b.compareTo(a)) // 先删除文件，再删除目录
                     .forEach(path -> {
                         try {
                             Files.deleteIfExists(path);
                         } catch (IOException e) {
                             log.warn("删除临时文件失败: {}", path, e);
                         }
                     });
            }
        } catch (Exception e) {
            log.warn("清理分片文件失败 - 上传ID: {}", uploadId, e);
        }
    }
    
    /**
     * 计算文件MD5
     */
    private String calculateFileMd5(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return DigestUtils.md5DigestAsHex(fis);
        }
    }
    
    /**
     * 删除文件
     */
    private void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            log.warn("删除文件失败: {}", filePath, e);
        }
    }
}