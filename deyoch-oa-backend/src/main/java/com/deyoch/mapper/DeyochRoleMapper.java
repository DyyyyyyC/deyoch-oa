package com.deyoch.mapper;

import com.deyoch.entity.DeyochRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色表 Mapper接口
 * 继承BaseMapper可以获得基础的CRUD方法
 */
@Mapper
public interface DeyochRoleMapper extends BaseMapper<DeyochRole> {
    
}