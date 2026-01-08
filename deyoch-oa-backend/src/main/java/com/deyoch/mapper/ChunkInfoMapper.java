package com.deyoch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deyoch.entity.DeyochChunkInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分片信息数据访问层
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Mapper
public interface ChunkInfoMapper extends BaseMapper<DeyochChunkInfo> {
    
    /**
     * 根据上传ID查询所有分片信息
     * 
     * @param uploadId 上传ID
     * @return 分片信息列表
     */
    List<DeyochChunkInfo> selectChunksByUploadId(@Param("uploadId") String uploadId);
    
    /**
     * 检查分片是否存在
     * 
     * @param uploadId 上传ID
     * @param chunkIndex 分片索引
     * @return 是否存在
     */
    boolean existsChunk(@Param("uploadId") String uploadId, @Param("chunkIndex") Integer chunkIndex);
    
    /**
     * 获取已上传的分片数量
     * 
     * @param uploadId 上传ID
     * @return 已上传分片数量
     */
    int countUploadedChunks(@Param("uploadId") String uploadId);
    
    /**
     * 根据上传ID删除所有分片信息
     * 
     * @param uploadId 上传ID
     * @return 删除数量
     */
    int deleteChunksByUploadId(@Param("uploadId") String uploadId);
}