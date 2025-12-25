package com.deyoch.mapper;

import com.deyoch.entity.DeyochDept;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门表 Mapper接口
 * 继承BaseMapper可以获得基础的CRUD方法
 */
@Mapper
public interface DeyochDeptMapper extends BaseMapper<DeyochDept> {
    
}