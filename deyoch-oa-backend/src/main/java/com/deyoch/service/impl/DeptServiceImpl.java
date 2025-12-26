package com.deyoch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deyoch.entity.DeyochDept;
import com.deyoch.mapper.DeyochDeptMapper;
import com.deyoch.result.Result;
import com.deyoch.result.ResultCode;
import com.deyoch.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门管理服务实现类
 * 实现部门管理相关的业务逻辑
 */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeyochDeptMapper deyochDeptMapper;

    @Override
    public Result<List<DeyochDept>> getDeptList() {
        try {
            // 查询所有部门，按排序字段升序排列
            LambdaQueryWrapper<DeyochDept> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByAsc(DeyochDept::getSort);
            List<DeyochDept> deptList = deyochDeptMapper.selectList(queryWrapper);
            return Result.success(deptList);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取部门列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<DeyochDept>> getDeptTree() {
        try {
            // 查询所有部门
            List<DeyochDept> deptList = deyochDeptMapper.selectList(null);
            
            // 构建部门树
            List<DeyochDept> deptTree = buildDeptTree(deptList);
            
            return Result.success(deptTree);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取部门树失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochDept> getDeptById(Long id) {
        try {
            DeyochDept dept = deyochDeptMapper.selectById(id);
            if (dept == null) {
                return Result.error(ResultCode.DEPT_NOT_FOUND, "部门不存在");
            }
            return Result.success(dept);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "获取部门详情失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochDept> createDept(DeyochDept dept) {
        try {
            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            dept.setCreatedAt(now);
            dept.setUpdatedAt(now);
            // 默认状态为启用
            dept.setStatus(1L);
            // 创建部门
            deyochDeptMapper.insert(dept);
            return Result.success(dept);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "创建部门失败：" + e.getMessage());
        }
    }

    @Override
    public Result<DeyochDept> updateDept(DeyochDept dept) {
        try {
            // 检查部门是否存在
            DeyochDept existingDept = deyochDeptMapper.selectById(dept.getId());
            if (existingDept == null) {
                return Result.error(ResultCode.DEPT_NOT_FOUND, "部门不存在");
            }
            // 设置更新时间
            dept.setUpdatedAt(LocalDateTime.now());
            // 更新部门
            deyochDeptMapper.updateById(dept);
            return Result.success(dept);
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新部门失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteDept(Long id) {
        try {
            // 检查部门是否存在
            DeyochDept dept = deyochDeptMapper.selectById(id);
            if (dept == null) {
                return Result.error(ResultCode.DEPT_NOT_FOUND, "部门不存在");
            }
            // 检查是否有子部门
            LambdaQueryWrapper<DeyochDept> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeyochDept::getParentId, id);
            if (deyochDeptMapper.selectCount(queryWrapper) > 0) {
                return Result.error(ResultCode.DEPT_HAS_CHILDREN, "该部门存在子部门，无法删除");
            }
            // 删除部门
            deyochDeptMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "删除部门失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> updateDeptStatus(Long id, Long status) {
        try {
            // 检查部门是否存在
            DeyochDept dept = deyochDeptMapper.selectById(id);
            if (dept == null) {
                return Result.error(ResultCode.DEPT_NOT_FOUND, "部门不存在");
            }
            // 检查状态值是否合法
            if (status != 0 && status != 1) {
                return Result.error(ResultCode.PARAM_ERROR, "状态值不合法，只能是0或1");
            }
            // 更新状态
            dept.setStatus(status);
            // 设置更新时间
            dept.setUpdatedAt(LocalDateTime.now());
            // 更新部门
            deyochDeptMapper.updateById(dept);
            return Result.success();
        } catch (Exception e) {
            return Result.error(ResultCode.SYSTEM_ERROR, "更新部门状态失败：" + e.getMessage());
        }
    }

    /**
     * 构建部门树
     * @param deptList 部门列表
     * @return 部门树
     */
    private List<DeyochDept> buildDeptTree(List<DeyochDept> deptList) {
        // 将部门列表转换为Map，便于根据ID查找
        Map<Long, DeyochDept> deptMap = deptList.stream()
                .collect(Collectors.toMap(DeyochDept::getId, dept -> dept));

        // 创建部门树列表
        List<DeyochDept> deptTree = new ArrayList<>();

        // 遍历部门列表，构建树结构
        for (DeyochDept dept : deptList) {
            Long parentId = dept.getParentId();
            if (parentId == null || parentId == 0L) {
                // 根部门，直接添加到树中
                deptTree.add(dept);
            } else {
                // 子部门，添加到父部门的children中
                // 注意：由于DeyochDept实体类中没有children字段，暂时不构建树形结构
                // 后续可以扩展DeyochDept类，添加children字段
            }
        }

        // 暂时返回按排序字段升序排列的部门列表
        return deptList.stream()
                .sorted((d1, d2) -> Long.compare(d1.getSort(), d2.getSort()))
                .collect(Collectors.toList());
    }
}