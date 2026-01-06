package com.deyoch.service;

import com.deyoch.entity.DeyochUser;
import com.deyoch.mapper.DeyochUserMapper;
import com.deyoch.service.impl.UserInfoConverterImpl;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.*;

/**
 * 用户信息回退处理属性测试
 * 功能：entity-field-fixes，属性4：用户信息回退处理
 * 验证：需求 2.5
 */
public class UserInfoConverterPropertyTest {

    @Mock
    private DeyochUserMapper deyochUserMapper;
    
    private UserInfoConverter userInfoConverter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userInfoConverter = new UserInfoConverterImpl(deyochUserMapper);
    }

    @Property(tries = 100)
    @Label("对于任何不存在的用户ID，服务层应该使用'未知用户'作为用户名的回退值")
    void shouldReturnUnknownUserForNonExistentUserIds(@ForAll("nonExistentUserIds") Set<Long> userIds) {
        // 模拟数据库查询返回空列表（用户不存在）
        when(deyochUserMapper.selectBatchIds(any())).thenReturn(new ArrayList<>());
        
        // 执行转换
        Map<Long, String> result = userInfoConverter.convertUserIdsToNames(userIds);
        
        // 验证所有用户ID都映射为"未知用户"
        for (Long userId : userIds) {
            if (userId != null) {
                assertThat(result.get(userId))
                    .as("不存在的用户ID %d 应该映射为'未知用户'", userId)
                    .isEqualTo("未知用户");
            }
        }
    }

    @Property(tries = 100)
    @Label("对于任何包含null值的用户ID集合，应该正确处理并忽略null值")
    void shouldHandleNullUserIdsGracefully(@ForAll("userIdsWithNulls") Set<Long> userIds) {
        // 模拟数据库查询
        when(deyochUserMapper.selectBatchIds(any())).thenReturn(new ArrayList<>());
        
        // 执行转换
        Map<Long, String> result = userInfoConverter.convertUserIdsToNames(userIds);
        
        // 验证结果不包含null键
        assertThat(result.keySet())
            .as("结果映射不应该包含null键")
            .doesNotContainNull();
        
        // 验证所有非null的用户ID都有对应的映射
        long nonNullCount = userIds.stream().filter(Objects::nonNull).count();
        assertThat(result.size())
            .as("结果映射的大小应该等于非null用户ID的数量")
            .isEqualTo((int) nonNullCount);
    }

    @Property(tries = 100)
    @Label("对于空的或null的用户ID集合，应该返回空的映射")
    void shouldReturnEmptyMapForEmptyOrNullInput(@ForAll("emptyOrNullUserIds") Set<Long> userIds) {
        // 执行转换
        Map<Long, String> result = userInfoConverter.convertUserIdsToNames(userIds);
        
        // 验证返回空映射
        assertThat(result)
            .as("空的或null的用户ID集合应该返回空映射")
            .isEmpty();
    }

    @Property(tries = 100)
    @Label("对于部分存在的用户ID，应该正确处理存在和不存在的用户")
    void shouldHandleMixedExistentAndNonExistentUsers(@ForAll("mixedUserIds") Set<Long> userIds) {
        // 模拟部分用户存在
        List<DeyochUser> existingUsers = new ArrayList<>();
        Set<Long> existingUserIds = new HashSet<>();
        
        // 假设ID为偶数的用户存在
        for (Long userId : userIds) {
            if (userId != null && userId % 2 == 0) {
                DeyochUser user = new DeyochUser();
                user.setId(userId);
                user.setUsername("user" + userId);
                existingUsers.add(user);
                existingUserIds.add(userId);
            }
        }
        
        when(deyochUserMapper.selectBatchIds(any())).thenReturn(existingUsers);
        
        // 执行转换
        Map<Long, String> result = userInfoConverter.convertUserIdsToNames(userIds);
        
        // 验证存在的用户有正确的用户名
        for (Long userId : existingUserIds) {
            assertThat(result.get(userId))
                .as("存在的用户ID %d 应该有正确的用户名", userId)
                .isEqualTo("user" + userId);
        }
        
        // 验证不存在的用户映射为"未知用户"
        for (Long userId : userIds) {
            if (userId != null && !existingUserIds.contains(userId)) {
                assertThat(result.get(userId))
                    .as("不存在的用户ID %d 应该映射为'未知用户'", userId)
                    .isEqualTo("未知用户");
            }
        }
    }

    /**
     * 生成不存在的用户ID集合
     */
    @Provide
    Arbitrary<Set<Long>> nonExistentUserIds() {
        return Arbitraries.longs().between(1000L, 9999L)
            .set().ofMinSize(1).ofMaxSize(10);
    }

    /**
     * 生成包含null值的用户ID集合
     */
    @Provide
    Arbitrary<Set<Long>> userIdsWithNulls() {
        return Arbitraries.oneOf(
            Arbitraries.longs().between(1L, 100L),
            Arbitraries.just((Long) null)
        ).set().ofMinSize(1).ofMaxSize(5);
    }

    /**
     * 生成空的或null的用户ID集合
     */
    @Provide
    Arbitrary<Set<Long>> emptyOrNullUserIds() {
        return Arbitraries.oneOf(
            Arbitraries.just((Set<Long>) null),
            Arbitraries.just(new HashSet<Long>())
        );
    }

    /**
     * 生成混合的用户ID集合
     */
    @Provide
    Arbitrary<Set<Long>> mixedUserIds() {
        return Arbitraries.longs().between(1L, 20L)
            .set().ofMinSize(2).ofMaxSize(8);
    }
}