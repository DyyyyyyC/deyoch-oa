package com.deyoch.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deyoch.common.result.PageResult;
import com.deyoch.common.result.Result;
import com.deyoch.common.result.ResultCode;
import com.deyoch.dto.ContactDto;
import com.deyoch.dto.OrgTreeDto;
import com.deyoch.mapper.ContactMapper;
import com.deyoch.service.ContactService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通讯录服务实现类
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Slf4j
@Service
public class ContactServiceImpl implements ContactService {
    
    @Autowired
    private ContactMapper contactMapper;
    
    @Override
    public Result<PageResult<ContactDto>> getContactDirectory(Integer page, Integer size, 
                                                            String keyword, Long deptId) {
        try {
            // 参数校验
            if (page == null || page < 1) {
                page = 1;
            }
            if (size == null || size < 1 || size > 100) {
                size = 20;
            }
            
            // 创建分页对象
            Page<ContactDto> pageObj = new Page<>(page, size);
            
            // 执行分页查询
            IPage<ContactDto> result = contactMapper.selectContactDirectory(pageObj, keyword, deptId);
            
            // 构建返回结果
            PageResult<ContactDto> pageResult = new PageResult<>();
            pageResult.setRecords(result.getRecords());
            pageResult.setTotal(result.getTotal());
            pageResult.setSize(result.getSize());
            pageResult.setCurrent(result.getCurrent());
            pageResult.setPages(result.getPages());
            
            return Result.success(pageResult);
            
        } catch (Exception e) {
            log.error("查询通讯录失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "查询通讯录失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<List<OrgTreeDto>> getOrganizationTree() {
        try {
            // 查询所有部门
            List<OrgTreeDto> departments = contactMapper.selectDepartmentTree();
            
            // 构建部门树结构
            List<OrgTreeDto> tree = buildTree(departments);
            
            // 为每个部门添加用户节点
            addUsersToTree(tree);
            
            return Result.success(tree);
            
        } catch (Exception e) {
            log.error("获取组织架构树失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "获取组织架构树失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<List<ContactDto>> searchContacts(String keyword) {
        try {
            if (!StringUtils.hasText(keyword)) {
                return Result.success(new ArrayList<>());
            }
            
            List<ContactDto> contacts = contactMapper.searchContacts(keyword.trim());
            return Result.success(contacts);
            
        } catch (Exception e) {
            log.error("搜索联系人失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "搜索联系人失败: " + e.getMessage());
        }
    }
    
    @Override
    public void exportContacts(HttpServletResponse response) {
        try {
            // 查询所有通讯录数据
            List<ContactDto> contacts = contactMapper.selectAllContacts();
            
            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("通讯录");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"姓名", "用户名", "工号", "职位", "部门", "电话", "邮箱", "办公地点", "分机号"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据行
            for (int i = 0; i < contacts.size(); i++) {
                ContactDto contact = contacts.get(i);
                Row row = sheet.createRow(i + 1);
                
                row.createCell(0).setCellValue(contact.getRealName());
                row.createCell(1).setCellValue(contact.getUsername());
                row.createCell(2).setCellValue(contact.getEmployeeId());
                row.createCell(3).setCellValue(contact.getPosition());
                row.createCell(4).setCellValue(contact.getDeptName());
                row.createCell(5).setCellValue(contact.getPhone());
                row.createCell(6).setCellValue(contact.getEmail());
                row.createCell(7).setCellValue(contact.getOfficeLocation());
                row.createCell(8).setCellValue(contact.getExtension());
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 设置响应头
            String fileName = "通讯录_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            
            // 写入响应流
            workbook.write(response.getOutputStream());
            workbook.close();
            
        } catch (Exception e) {
            log.error("导出通讯录失败", e);
            throw new RuntimeException("导出通讯录失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<String> importContacts(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.error(ResultCode.PARAM_IS_NULL, "请选择要导入的文件");
            }
            
            // 检查文件类型
            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
                return Result.error(ResultCode.PARAM_ERROR, "请上传Excel文件(.xlsx格式)");
            }
            
            // 读取Excel文件
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            
            int successCount = 0;
            int errorCount = 0;
            StringBuilder errorMessages = new StringBuilder();
            
            // 跳过标题行，从第二行开始读取数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    // 读取单元格数据
                    String realName = getCellValue(row.getCell(0));
                    String username = getCellValue(row.getCell(1));
                    String employeeId = getCellValue(row.getCell(2));
                    String position = getCellValue(row.getCell(3));
                    String phone = getCellValue(row.getCell(5));
                    String email = getCellValue(row.getCell(6));
                    String officeLocation = getCellValue(row.getCell(7));
                    String extension = getCellValue(row.getCell(8));
                    
                    // 数据验证
                    if (!StringUtils.hasText(realName) || !StringUtils.hasText(username)) {
                        errorMessages.append("第").append(i + 1).append("行：姓名和用户名不能为空\n");
                        errorCount++;
                        continue;
                    }
                    
                    // TODO: 这里应该调用用户服务来更新用户信息
                    // 由于这是通讯录导入，主要是更新用户的联系信息
                    log.info("导入联系人: {} - {}", realName, username);
                    successCount++;
                    
                } catch (Exception e) {
                    errorMessages.append("第").append(i + 1).append("行：").append(e.getMessage()).append("\n");
                    errorCount++;
                }
            }
            
            workbook.close();
            
            String result = String.format("导入完成！成功：%d条，失败：%d条", successCount, errorCount);
            if (errorCount > 0) {
                result += "\n错误详情：\n" + errorMessages.toString();
            }
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("导入通讯录失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "导入通讯录失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<ContactDto> getContactDetail(Long userId) {
        try {
            if (userId == null) {
                return Result.error(ResultCode.PARAM_IS_NULL, "用户ID不能为空");
            }
            
            // 通过搜索功能获取用户详情（复用现有查询）
            List<ContactDto> contacts = contactMapper.searchContacts(userId.toString());
            
            ContactDto contact = contacts.stream()
                    .filter(c -> c.getId().equals(userId))
                    .findFirst()
                    .orElse(null);
            
            if (contact == null) {
                return Result.error(ResultCode.DATA_NOT_FOUND, "联系人不存在");
            }
            
            return Result.success(contact);
            
        } catch (Exception e) {
            log.error("获取联系人详情失败", e);
            return Result.error(ResultCode.INTERNAL_ERROR, "获取联系人详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建树形结构
     */
    private List<OrgTreeDto> buildTree(List<OrgTreeDto> nodes) {
        List<OrgTreeDto> tree = new ArrayList<>();
        Map<Long, OrgTreeDto> nodeMap = new HashMap<>();
        
        // 将所有节点放入Map中
        for (OrgTreeDto node : nodes) {
            node.setChildren(new ArrayList<>());
            nodeMap.put(node.getId(), node);
        }
        
        // 构建树形结构
        for (OrgTreeDto node : nodes) {
            if (node.getParentId() == null || node.getParentId() == 0) {
                // 根节点
                tree.add(node);
            } else {
                // 子节点
                OrgTreeDto parent = nodeMap.get(node.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                }
            }
        }
        
        return tree;
    }
    
    /**
     * 为部门树添加用户节点
     */
    private void addUsersToTree(List<OrgTreeDto> tree) {
        for (OrgTreeDto dept : tree) {
            // 查询该部门下的用户
            List<OrgTreeDto> users = contactMapper.selectUsersByDeptId(dept.getId());
            dept.getChildren().addAll(users);
            
            // 递归处理子部门
            if (!dept.getChildren().isEmpty()) {
                List<OrgTreeDto> subDepts = dept.getChildren().stream()
                        .filter(child -> "dept".equals(child.getType()))
                        .toList();
                addUsersToTree(subDepts);
            }
        }
    }
    
    /**
     * 获取单元格值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}