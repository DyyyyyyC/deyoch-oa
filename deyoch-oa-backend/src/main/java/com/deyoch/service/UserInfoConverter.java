package com.deyoch.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 用户信息转换器接口
 * 负责批量转换用户ID到用户名，优化数据库查询性能
 */
public interface UserInfoConverter {

    /**
     * 批量转换用户ID到用户名
     * @param userIds 用户ID集合
     * @return 用户ID到用户名的映射，不存在的用户ID映射为"未知用户"
     */
    Map<Long, String> convertUserIdsToNames(Set<Long> userIds);

    /**
     * 为实体列表填充用户名信息
     * @param entities 实体列表
     * @param userIdExtractor 用户ID提取器，从实体中提取需要转换的用户ID
     * @param userNameSetter 用户名设置器，将转换后的用户名设置到实体中
     * @param <T> 实体类型
     */
    <T> void populateUserNames(List<T> entities, 
                              Function<T, Set<Long>> userIdExtractor,
                              BiConsumer<T, Map<Long, String>> userNameSetter);

    /**
     * 为单个实体填充用户名信息
     * @param entity 实体对象
     * @param userIdExtractor 用户ID提取器，从实体中提取需要转换的用户ID
     * @param userNameSetter 用户名设置器，将转换后的用户名设置到实体中
     * @param <T> 实体类型
     */
    <T> void populateUserNames(T entity,
                              Function<T, Set<Long>> userIdExtractor,
                              BiConsumer<T, Map<Long, String>> userNameSetter);
}