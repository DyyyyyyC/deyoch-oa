package com.deyoch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deyoch.entity.DeyochDocument;
import com.deyoch.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档管理服务接口
 * 定义文档管理相关的业务逻辑方法
 */
public interface DocumentService extends IService<DeyochDocument> {

    /**
     * 获取文档列表
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词
     * @return 文档列表
     */
    Result<List<DeyochDocument>> getDocumentList(Integer page, Integer size, String keyword);

    /**
     * 根据ID获取文档详情
     * @param id 文档ID
     * @return 文档详情
     */
    Result<DeyochDocument> getDocumentById(Long id);

    /**
     * 根据部门ID获取文档列表
     * @param deptId 部门ID
     * @return 文档列表
     */
    Result<List<DeyochDocument>> getDocumentsByDeptId(Long deptId);

    /**
     * 创建文档
     * @param document 文档信息
     * @return 创建结果
     */
    Result<DeyochDocument> createDocument(DeyochDocument document);

    /**
     * 更新文档信息
     * @param document 文档信息
     * @return 更新结果
     */
    Result<DeyochDocument> updateDocument(DeyochDocument document);

    /**
     * 删除文档
     * @param id 文档ID
     * @return 删除结果
     */
    Result<Void> deleteDocument(Long id);

    /**
     * 更新文档状态
     * @param id 文档ID
     * @param status 文档状态
     * @return 更新结果
     */
    Result<Void> updateDocumentStatus(Long id, Integer status);

    /**
     * 上传文档
     * @param file 上传的文件
     * @param document 文档信息
     * @return 上传结果
     */
    Result<DeyochDocument> uploadDocument(MultipartFile file, DeyochDocument document);

    /**
     * 下载文档
     * @param id 文档ID
     * @return 下载结果
     */
    Result<String> downloadDocument(Long id);
}