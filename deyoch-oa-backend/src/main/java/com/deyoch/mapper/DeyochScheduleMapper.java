package com.deyoch.mapper;

import com.deyoch.entity.DeyochSchedule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 日程表 Mapper接口
 * 继承BaseMapper可以获得基础的CRUD方法
 */
@Mapper
public interface DeyochScheduleMapper extends BaseMapper<DeyochSchedule> {
    
}