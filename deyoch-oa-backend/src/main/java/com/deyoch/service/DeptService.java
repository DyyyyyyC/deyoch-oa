package com.deyoch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.deyoch.entity.DeyochDept;
import com.deyoch.result.Result;

import java.util.List;

/**
 * 部门管理服务接口
 * 定义部门管理相关的业务逻辑方法
 */
public interface DeptService extends IService<DeyochDept> {

    /**
     * 获取部门列表
     * @return 部门列表
     */
    Result<List<DeyochDept>> getDeptList();

    /**
     * 获取部门树
     * @return 部门树
     */
    Result<List<DeyochDept>> getDeptTree();

    /**
     * 根据ID获取部门详情
     * @param id 部门ID
     * @return 部门详情
     */
    Result<DeyochDept> getDeptById(Long id);

    /**
     * 创建部门
     * @param dept 部门信息
     * @return 创建结果
     */
    Result<DeyochDept> createDept(DeyochDept dept);

    /**
     * 更新部门信息
     * @param dept 部门信息
     * @return 更新结果
     */
    Result<DeyochDept> updateDept(DeyochDept dept);

    /**
     * 删除部门
     * @param id 部门ID
     * @return 删除结果
     */
    Result<Void> deleteDept(Long id);

    /**
     * 更新部门状态
     * @param id 部门ID
     * @param status 部门状态
     * @return 更新结果
     */
    Result<Void> updateDeptStatus(Long id, Integer status);
}