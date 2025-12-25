package com.deyoch.mapper;

import com.deyoch.entity.DeyochProcess;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程表 Mapper接口
 * 继承BaseMapper可以获得基础的CRUD方法
 */
@Mapper
public interface DeyochProcessMapper extends BaseMapper<DeyochProcess> {
    
}