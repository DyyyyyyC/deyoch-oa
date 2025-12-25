package com.deyoch.mapper;

import com.deyoch.entity.DeyochRolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色权限关联表 Mapper接口
 * 继承BaseMapper可以获得基础的CRUD方法
 */
@Mapper
public interface DeyochRolePermissionMapper extends BaseMapper<DeyochRolePermission> {
    
}