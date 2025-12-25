package com.deyoch.service.impl;

import com.deyoch.entity.DeyochDept;
import com.deyoch.mapper.DeyochDeptMapper;
import com.deyoch.service.DeyochDeptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门服务实现类
 * 实现部门相关的业务操作方法
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Service
public class DeyochDeptServiceImpl extends ServiceImpl<DeyochDeptMapper, DeyochDept> implements DeyochDeptService {

    /**
     * 根据部门编码查询部门信息
     * 
     * @param deptCode 部门编码
     * @return 查询到的部门对象，未找到返回null
     */
    @Override
    public DeyochDept findByDeptCode(String deptCode) {
        return lambdaQuery()
                .eq(DeyochDept::getDeptCode, deptCode)
                .one();
    }

    /**
     * 检查部门编码是否存在
     * 
     * @param deptCode 部门编码
     * @return 存在返回true，不存在返回false
     */
    @Override
    public boolean existsByDeptCode(String deptCode) {
        return lambdaQuery()
                .eq(DeyochDept::getDeptCode, deptCode)
                .count() > 0;
    }

    /**
     * 根据父级ID查询子部门列表
     * 
     * @param parentId 父级ID
     * @return 子部门列表
     */
    @Override
    public List<DeyochDept> findByParentId(Long parentId) {
        return lambdaQuery()
                .eq(DeyochDept::getParentId, parentId)
                .eq(DeyochDept::getStatus, 1)
                .orderByAsc(DeyochDept::getSort)
                .list();
    }

    /**
     * 查询所有启用的部门列表
     * 
     * @return 部门列表
     */
    @Override
    public List<DeyochDept> findAllEnabled() {
        return lambdaQuery()
                .eq(DeyochDept::getStatus, 1)
                .orderByAsc(DeyochDept::getSort)
                .list();
    }

    /**
     * 查询部门树形结构
     * 
     * @return 树形部门列表
     */
    @Override
    public List<DeyochDept> findDeptTree() {
        return lambdaQuery()
                .eq(DeyochDept::getStatus, 1)
                .orderByAsc(DeyochDept::getSort)
                .list();
    }
}