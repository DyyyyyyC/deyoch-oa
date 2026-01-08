package com.deyoch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deyoch.entity.DeyochMessageTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 消息模板数据访问层
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Mapper
public interface MessageTemplateMapper extends BaseMapper<DeyochMessageTemplate> {
    
    /**
     * 根据模板类型查询消息模板
     * 
     * @param type 模板类型
     * @return 消息模板
     */
    DeyochMessageTemplate selectByType(@Param("type") String type);
}