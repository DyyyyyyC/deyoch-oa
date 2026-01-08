package com.deyoch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deyoch.dto.DocumentVersionDto;
import com.deyoch.entity.DeyochDocumentVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档版本数据访问层
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Mapper
public interface DocumentVersionMapper extends BaseMapper<DeyochDocumentVersion> {
    
    /**
     * 根据文档ID查询版本历史
     * 
     * @param documentId 文档ID
     * @return 版本历史列表
     */
    List<DocumentVersionDto> selectVersionsByDocumentId(@Param("documentId") Long documentId);
    
    /**
     * 根据文档ID和版本号查询版本信息
     * 
     * @param documentId 文档ID
     * @param version 版本号
     * @return 版本信息
     */
    DocumentVersionDto selectVersionByDocumentIdAndVersion(@Param("documentId") Long documentId, 
                                                          @Param("version") String version);
    
    /**
     * 获取文档的最新版本号
     * 
     * @param documentId 文档ID
     * @return 最新版本号
     */
    String selectLatestVersionByDocumentId(@Param("documentId") Long documentId);
    
    /**
     * 检查版本是否存在
     * 
     * @param documentId 文档ID
     * @param version 版本号
     * @return 是否存在
     */
    boolean existsVersion(@Param("documentId") Long documentId, @Param("version") String version);
}