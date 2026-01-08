package com.deyoch.dto;

import lombok.Data;
import java.util.List;

/**
 * 组织架构树数据传输对象
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Data
public class OrgTreeDto {
    
    /**
     * 节点ID
     */
    private Long id;
    
    /**
     * 节点名称
     */
    private String name;
    
    /**
     * 节点类型：dept-部门，user-用户
     */
    private String type;
    
    /**
     * 父节点ID
     */
    private Long parentId;
    
    /**
     * 部门编码（仅部门节点有效）
     */
    private String deptCode;
    
    /**
     * 负责人ID（仅部门节点有效）
     */
    private Long leaderId;
    
    /**
     * 负责人姓名（仅部门节点有效）
     */
    private String leaderName;
    
    /**
     * 用户职位（仅用户节点有效）
     */
    private String position;
    
    /**
     * 用户电话（仅用户节点有效）
     */
    private String phone;
    
    /**
     * 用户邮箱（仅用户节点有效）
     */
    private String email;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 子节点列表
     */
    private List<OrgTreeDto> children;
}