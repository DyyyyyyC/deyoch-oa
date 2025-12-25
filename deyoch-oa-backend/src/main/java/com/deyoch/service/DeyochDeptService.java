package com.deyoch.service;

import com.deyoch.entity.DeyochDept;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 部门服务接口
 * 提供部门相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
public interface DeyochDeptService extends IService<DeyochDept> {
    
    /**
     * 根据部门编码查询部门信息
     * 
     * @param deptCode 部门编码
     * @return 查询到的部门对象，未找到返回null
     */
    DeyochDept findByDeptCode(String deptCode);
    
    /**
     * 检查部门编码是否存在
     * 
     * @param deptCode 部门编码
     * @return 存在返回true，不存在返回false
     */
    boolean existsByDeptCode(String deptCode);
    
    /**
     * 根据父级ID查询子部门列表
     * 
     * @param parentId 父级ID
     * @return 子部门列表
     */
    List<DeyochDept> findByParentId(Long parentId);
    
    /**
     * 查询所有启用的部门列表
     * 
     * @return 部门列表
     */
    List<DeyochDept> findAllEnabled();
    
    /**
     * 查询部门树形结构
     * 
     * @return 树形部门列表
     */
    List<DeyochDept> findDeptTree();
}