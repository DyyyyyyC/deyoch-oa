package com.deyoch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deyoch.dto.ChunkUploadDto;
import com.deyoch.entity.DeyochChunkUpload;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分片上传数据访问层
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Mapper
public interface ChunkUploadMapper extends BaseMapper<DeyochChunkUpload> {
    
    /**
     * 根据上传ID查询上传信息
     * 
     * @param uploadId 上传ID
     * @return 上传信息
     */
    ChunkUploadDto selectUploadInfoByUploadId(@Param("uploadId") String uploadId);
    
    /**
     * 更新已上传分片数
     * 
     * @param uploadId 上传ID
     * @param uploadedChunks 已上传分片数
     * @return 更新数量
     */
    int updateUploadedChunks(@Param("uploadId") String uploadId, 
                            @Param("uploadedChunks") Integer uploadedChunks);
    
    /**
     * 更新上传状态和最终路径
     * 
     * @param uploadId 上传ID
     * @param status 状态
     * @param finalPath 最终文件路径
     * @return 更新数量
     */
    int updateStatusAndFinalPath(@Param("uploadId") String uploadId, 
                                @Param("status") Integer status, 
                                @Param("finalPath") String finalPath);
    
    /**
     * 查询过期的上传记录
     * 
     * @param expireTime 过期时间
     * @return 过期的上传ID列表
     */
    List<String> selectExpiredUploadIds(@Param("expireTime") LocalDateTime expireTime);
    
    /**
     * 删除过期的上传记录
     * 
     * @param expireTime 过期时间
     * @return 删除数量
     */
    int deleteExpiredUploads(@Param("expireTime") LocalDateTime expireTime);
}