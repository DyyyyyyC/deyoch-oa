package com.deyoch.service;

import com.deyoch.common.result.PageResult;
import com.deyoch.common.result.Result;
import com.deyoch.dto.ContactDto;
import com.deyoch.dto.OrgTreeDto;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 通讯录服务接口
 * 
 * @author deyoch
 * @since 2026-01-08
 */
public interface ContactService {
    
    /**
     * 分页查询通讯录
     * 
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @param deptId 部门ID
     * @return 分页结果
     */
    Result<PageResult<ContactDto>> getContactDirectory(Integer page, Integer size, 
                                                     String keyword, Long deptId);
    
    /**
     * 获取组织架构树
     * 
     * @return 组织架构树
     */
    Result<List<OrgTreeDto>> getOrganizationTree();
    
    /**
     * 搜索联系人
     * 
     * @param keyword 搜索关键词
     * @return 联系人列表
     */
    Result<List<ContactDto>> searchContacts(String keyword);
    
    /**
     * 导出通讯录到Excel
     * 
     * @param response HTTP响应对象
     */
    void exportContacts(HttpServletResponse response);
    
    /**
     * 从Excel导入通讯录
     * 
     * @param file Excel文件
     * @return 导入结果
     */
    Result<String> importContacts(MultipartFile file);
    
    /**
     * 获取联系人详情
     * 
     * @param userId 用户ID
     * @return 联系人详情
     */
    Result<ContactDto> getContactDetail(Long userId);
}