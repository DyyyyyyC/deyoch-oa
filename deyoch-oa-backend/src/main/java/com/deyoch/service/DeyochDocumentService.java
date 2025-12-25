package com.deyoch.service;

import com.deyoch.entity.DeyochDocument;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 文档服务接口
 * 提供文档相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
public interface DeyochDocumentService extends IService<DeyochDocument> {
    
    /**
     * 根据部门ID查询文档列表
     * 
     * @param deptId 部门ID
     * @return 文档列表
     */
    List<DeyochDocument> findByDeptId(Long deptId);
    
    /**
     * 根据上传人查询文档列表
     * 
     * @param uploader 上传人
     * @return 文档列表
     */
    List<DeyochDocument> findByUploader(String uploader);
    
    /**
     * 根据文件类型查询文档列表
     * 
     * @param fileType 文件类型
     * @return 文档列表
     */
    List<DeyochDocument> findByFileType(String fileType);
    
    /**
     * 查询所有启用的文档列表
     * 
     * @return 文档列表
     */
    List<DeyochDocument> findAllEnabled();
}