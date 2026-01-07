package com.deyoch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deyoch.entity.DeyochUser;
import com.deyoch.mapper.DeyochUserMapper;
import com.deyoch.service.UserInfoConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户信息转换器实现类
 * 实现批量用户ID到用户名转换，优化数据库查询性能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoConverterImpl implements UserInfoConverter {

    private final DeyochUserMapper deyochUserMapper;
    
    /**
     * 未知用户的默认名称
     */
    private static final String UNKNOWN_USER = "未知用户";

    @Override
    public Map<Long, String> convertUserIdsToNames(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }

        try {
            // 过滤掉null值
            Set<Long> validUserIds = userIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            if (validUserIds.isEmpty()) {
                return new HashMap<>();
            }

            log.debug("开始转换用户ID到用户名，用户ID列表: {}", validUserIds);

            // 批量查询用户信息 - 使用LambdaQueryWrapper替代过时的selectBatchIds
            List<DeyochUser> users = deyochUserMapper.selectList(
                new LambdaQueryWrapper<DeyochUser>()
                    .in(DeyochUser::getId, validUserIds)
            );
            
            log.debug("从数据库查询到{}个用户，原始用户ID数量: {}", users.size(), validUserIds.size());
            
            // 构建用户ID到用户名的映射
            Map<Long, String> userIdToNameMap = users.stream()
                .collect(Collectors.toMap(
                    DeyochUser::getId,
                    user -> user.getUsername() != null ? user.getUsername() : UNKNOWN_USER,
                    (existing, replacement) -> existing // 处理重复键的情况
                ));

            // 为不存在的用户ID添加默认值
            for (Long userId : validUserIds) {
                if (!userIdToNameMap.containsKey(userId)) {
                    log.warn("用户ID {} 在数据库中不存在，设置为未知用户", userId);
                    userIdToNameMap.put(userId, UNKNOWN_USER);
                }
            }

            log.debug("转换完成，最终映射: {}", userIdToNameMap);

            return userIdToNameMap;
        } catch (Exception e) {
            log.error("批量转换用户ID到用户名时发生错误，用户ID列表: {}", userIds, e);
            // 发生异常时，返回所有用户ID都映射为未知用户的Map
            return userIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                    userId -> userId,
                    userId -> UNKNOWN_USER
                ));
        }
    }

    @Override
    public <T> void populateUserNames(List<T> entities, 
                                     Function<T, Set<Long>> userIdExtractor,
                                     BiConsumer<T, Map<Long, String>> userNameSetter) {
        if (entities == null || entities.isEmpty()) {
            log.debug("实体列表为空，跳过用户名填充");
            return;
        }

        try {
            log.debug("开始为{}个实体填充用户名", entities.size());
            
            // 收集所有唯一的用户ID
            Set<Long> allUserIds = entities.stream()
                .map(userIdExtractor)
                .filter(Objects::nonNull)
                .flatMap(Set::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            log.debug("收集到{}个唯一用户ID: {}", allUserIds.size(), allUserIds);

            if (allUserIds.isEmpty()) {
                log.debug("没有找到需要转换的用户ID，跳过填充");
                return;
            }

            // 批量转换用户ID到用户名
            Map<Long, String> userIdToNameMap = convertUserIdsToNames(allUserIds);

            // 为每个实体设置用户名
            for (T entity : entities) {
                userNameSetter.accept(entity, userIdToNameMap);
            }

            log.debug("成功为{}个实体填充了用户名信息", entities.size());
        } catch (Exception e) {
            log.error("为实体列表填充用户名时发生错误", e);
            // 发生异常时，为每个实体设置空的映射
            Map<Long, String> emptyMap = new HashMap<>();
            for (T entity : entities) {
                try {
                    userNameSetter.accept(entity, emptyMap);
                } catch (Exception ex) {
                    log.warn("为单个实体设置用户名时发生错误", ex);
                }
            }
        }
    }

    @Override
    public <T> void populateUserNames(T entity,
                                     Function<T, Set<Long>> userIdExtractor,
                                     BiConsumer<T, Map<Long, String>> userNameSetter) {
        if (entity == null) {
            return;
        }

        try {
            // 提取用户ID
            Set<Long> userIds = userIdExtractor.apply(entity);
            if (userIds == null || userIds.isEmpty()) {
                return;
            }

            // 转换用户ID到用户名
            Map<Long, String> userIdToNameMap = convertUserIdsToNames(userIds);

            // 设置用户名
            userNameSetter.accept(entity, userIdToNameMap);

            log.debug("为单个实体填充了用户名信息");
        } catch (Exception e) {
            log.error("为单个实体填充用户名时发生错误", e);
            // 发生异常时，设置空的映射
            try {
                userNameSetter.accept(entity, new HashMap<>());
            } catch (Exception ex) {
                log.warn("为实体设置空用户名映射时发生错误", ex);
            }
        }
    }
}