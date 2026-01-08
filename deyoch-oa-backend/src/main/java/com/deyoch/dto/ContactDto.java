package com.deyoch.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通讯录数据传输对象
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Data
public class ContactDto {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 电话
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 职位
     */
    private String position;
    
    /**
     * 办公地点
     */
    private String officeLocation;
    
    /**
     * 分机号
     */
    private String extension;
    
    /**
     * 工号
     */
    private String employeeId;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 部门名称
     */
    private String deptName;
    
    /**
     * 部门ID
     */
    private Long deptId;
    
    /**
     * 父部门ID
     */
    private Long deptParentId;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}