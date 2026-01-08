package com.deyoch.controller;

import com.deyoch.common.result.PageResult;
import com.deyoch.common.result.Result;
import com.deyoch.dto.ContactDto;
import com.deyoch.dto.OrgTreeDto;
import com.deyoch.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 通讯录管理控制器
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Slf4j
@RestController
@RequestMapping("/contact")
@Tag(name = "通讯录管理", description = "通讯录相关接口")
public class ContactController {
    
    @Autowired
    private ContactService contactService;
    
    /**
     * 分页查询通讯录
     */
    @GetMapping("/directory")
    @Operation(summary = "分页查询通讯录", description = "支持按关键词和部门筛选")
    public Result<PageResult<ContactDto>> getContactDirectory(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小，默认20") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId) {
        
        log.info("查询通讯录 - page: {}, size: {}, keyword: {}, deptId: {}", page, size, keyword, deptId);
        return contactService.getContactDirectory(page, size, keyword, deptId);
    }
    
    /**
     * 获取组织架构树
     */
    @GetMapping("/org-tree")
    @Operation(summary = "获取组织架构树", description = "返回完整的组织架构树形结构")
    public Result<List<OrgTreeDto>> getOrganizationTree() {
        log.info("获取组织架构树");
        return contactService.getOrganizationTree();
    }
    
    /**
     * 搜索联系人
     */
    @GetMapping("/search")
    @Operation(summary = "搜索联系人", description = "根据关键词快速搜索联系人")
    public Result<List<ContactDto>> searchContacts(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword) {
        
        log.info("搜索联系人 - keyword: {}", keyword);
        return contactService.searchContacts(keyword);
    }
    
    /**
     * 获取联系人详情
     */
    @GetMapping("/{userId}")
    @Operation(summary = "获取联系人详情", description = "根据用户ID获取详细联系信息")
    public Result<ContactDto> getContactDetail(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        
        log.info("获取联系人详情 - userId: {}", userId);
        return contactService.getContactDetail(userId);
    }
    
    /**
     * 导出通讯录
     */
    @GetMapping("/export")
    @Operation(summary = "导出通讯录", description = "导出通讯录到Excel文件")
    public void exportContacts(HttpServletResponse response) {
        log.info("导出通讯录");
        contactService.exportContacts(response);
    }
    
    /**
     * 导入通讯录
     */
    @PostMapping("/import")
    @Operation(summary = "导入通讯录", description = "从Excel文件导入通讯录数据")
    public Result<String> importContacts(
            @Parameter(description = "Excel文件", required = true) @RequestParam("file") MultipartFile file) {
        
        log.info("导入通讯录 - fileName: {}", file.getOriginalFilename());
        return contactService.importContacts(file);
    }
}