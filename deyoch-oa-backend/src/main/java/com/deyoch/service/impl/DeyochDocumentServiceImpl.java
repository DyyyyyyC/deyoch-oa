package com.deyoch.service.impl;

import com.deyoch.entity.DeyochDocument;
import com.deyoch.mapper.DeyochDocumentMapper;
import com.deyoch.service.DeyochDocumentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文档服务实现类
 * 实现文档相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Service
public class DeyochDocumentServiceImpl extends ServiceImpl<DeyochDocumentMapper, DeyochDocument> implements DeyochDocumentService {

    /**
     * 根据部门ID查询文档列表
     * 
     * @param deptId 部门ID
     * @return 文档列表
     */
    @Override
    public List<DeyochDocument> findByDeptId(Long deptId) {
        return lambdaQuery()
                .eq(DeyochDocument::getDeptId, deptId)
                .eq(DeyochDocument::getStatus, 1)
                .orderByDesc(DeyochDocument::getCreatedAt)
                .list();
    }

    /**
     * 根据上传人查询文档列表
     * 
     * @param uploader 上传人
     * @return 文档列表
     */
    @Override
    public List<DeyochDocument> findByUploader(String uploader) {
        return lambdaQuery()
                .eq(DeyochDocument::getUploader, uploader)
                .eq(DeyochDocument::getStatus, 1)
                .orderByDesc(DeyochDocument::getCreatedAt)
                .list();
    }

    /**
     * 根据文件类型查询文档列表
     * 
     * @param fileType 文件类型
     * @return 文档列表
     */
    @Override
    public List<DeyochDocument> findByFileType(String fileType) {
        return lambdaQuery()
                .eq(DeyochDocument::getFileType, fileType)
                .eq(DeyochDocument::getStatus, 1)
                .orderByDesc(DeyochDocument::getCreatedAt)
                .list();
    }

    /**
     * 查询所有启用的文档列表
     * 
     * @return 文档列表
     */
    @Override
    public List<DeyochDocument> findAllEnabled() {
        return lambdaQuery()
                .eq(DeyochDocument::getStatus, 1)
                .orderByDesc(DeyochDocument::getCreatedAt)
                .list();
    }
}