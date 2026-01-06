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
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 批量查询优化属性测试
 * 功能：entity-field-fixes，属性6：批量查询优化
 * 验证：需求 4.1, 4.2
 */
public class BatchQueryOptimizationPropertyTest {

    @Mock
    private DeyochUserMapper deyochUserMapper;
    
    private UserInfoConverter userInfoConverter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userInfoConverter = new UserInfoConverterImpl(deyochUserMapper);
    }

    @Property(tries = 100)
    @Label("对于任何需要用户信息的列表查询，服务层应该使用批量查询而不是逐个查询用户信息")
    void shouldUseBatchQueryForUserInformation(@ForAll("entityLists") List<TestEntity> entities) {
        // 模拟数据库返回
        when(deyochUserMapper.selectBatchIds(any())).thenReturn(createMockUsers());
        
        // 执行批量填充用户名
        userInfoConverter.populateUserNames(
            entities,
            this::extractUserIds,
            this::setUserNames
        );
        
        if (!entities.isEmpty() && hasAnyUserIds(entities)) {
            // 验证只调用了一次批量查询
            verify(deyochUserMapper, times(1)).selectBatchIds(any());
            
            // 验证没有调用单个查询方法（如果存在的话）
            verify(deyochUserMapper, never()).selectById(any());
        } else {
            // 如果没有用户ID，应该不调用数据库
            verify(deyochUserMapper, never()).selectBatchIds(any());
        }
    }

    @Property(tries = 100)
    @Label("当多个实体引用相同用户时，服务实现应该创建一次用户ID到用户名的映射")
    void shouldCreateUserMappingOnceForDuplicateUsers(@ForAll("entitiesWithDuplicateUsers") List<TestEntity> entities) {
        // 模拟数据库返回
        when(deyochUserMapper.selectBatchIds(any())).thenReturn(createMockUsers());
        
        // 执行批量填充用户名
        userInfoConverter.populateUserNames(
            entities,
            this::extractUserIds,
            this::setUserNames
        );
        
        if (!entities.isEmpty() && hasAnyUserIds(entities)) {
            // 验证只调用了一次批量查询，即使有重复的用户ID
            verify(deyochUserMapper, times(1)).selectBatchIds(any());
            
            // 验证传递给数据库的用户ID集合是去重的
            verify(deyochUserMapper).selectBatchIds(argThat(userIds -> {
                Set<Long> uniqueIds = new HashSet<>((Collection<Long>) userIds);
                return uniqueIds.size() == ((Collection<Long>) userIds).size();
            }));
        }
    }

    @Property(tries = 100)
    @Label("当没有用户ID存在时，服务实现应该跳过用户信息查询")
    void shouldSkipQueryWhenNoUserIdsExist(@ForAll("entitiesWithoutUserIds") List<TestEntity> entities) {
        // 执行批量填充用户名
        userInfoConverter.populateUserNames(
            entities,
            this::extractUserIds,
            this::setUserNames
        );
        
        // 验证没有调用数据库查询
        verify(deyochUserMapper, never()).selectBatchIds(any());
        verify(deyochUserMapper, never()).selectById(any());
    }

    @Property(tries = 100)
    @Label("批量查询应该最小化数据库往返次数")
    void shouldMinimizeDatabaseRoundTrips(@ForAll("largeEntityList") List<TestEntity> entities) {
        // 模拟数据库返回
        when(deyochUserMapper.selectBatchIds(any())).thenReturn(createMockUsers());
        
        // 执行批量填充用户名
        userInfoConverter.populateUserNames(
            entities,
            this::extractUserIds,
            this::setUserNames
        );
        
        if (!entities.isEmpty() && hasAnyUserIds(entities)) {
            // 无论实体数量多少，都应该只有一次数据库查询
            verify(deyochUserMapper, times(1)).selectBatchIds(any());
            
            // 验证查询的用户ID数量不超过实体数量的两倍（考虑每个实体最多有两个用户ID）
            verify(deyochUserMapper).selectBatchIds(argThat(userIds -> {
                Collection<Long> ids = (Collection<Long>) userIds;
                return ids.size() <= entities.size() * 2;
            }));
        }
    }

    /**
     * 测试实体类
     */
    private static class TestEntity {
        private Long creatorId;
        private Long assigneeId;
        private String creatorName;
        private String assigneeName;
        
        public TestEntity(Long creatorId, Long assigneeId) {
            this.creatorId = creatorId;
            this.assigneeId = assigneeId;
        }
        
        // Getters and setters
        public Long getCreatorId() { return creatorId; }
        public Long getAssigneeId() { return assigneeId; }
        public String getCreatorName() { return creatorName; }
        public String getAssigneeName() { return assigneeName; }
        public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
        public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
    }

    /**
     * 从实体中提取用户ID
     */
    private Set<Long> extractUserIds(TestEntity entity) {
        Set<Long> userIds = new HashSet<>();
        if (entity.getCreatorId() != null) {
            userIds.add(entity.getCreatorId());
        }
        if (entity.getAssigneeId() != null) {
            userIds.add(entity.getAssigneeId());
        }
        return userIds;
    }

    /**
     * 为实体设置用户名
     */
    private void setUserNames(TestEntity entity, Map<Long, String> userIdToNameMap) {
        if (entity.getCreatorId() != null) {
            entity.setCreatorName(userIdToNameMap.getOrDefault(entity.getCreatorId(), "未知用户"));
        }
        if (entity.getAssigneeId() != null) {
            entity.setAssigneeName(userIdToNameMap.getOrDefault(entity.getAssigneeId(), "未知用户"));
        }
    }

    /**
     * 检查实体列表是否包含任何用户ID
     */
    private boolean hasAnyUserIds(List<TestEntity> entities) {
        return entities.stream().anyMatch(entity -> 
            entity.getCreatorId() != null || entity.getAssigneeId() != null);
    }

    /**
     * 创建模拟用户数据
     */
    private List<DeyochUser> createMockUsers() {
        List<DeyochUser> users = new ArrayList<>();
        for (long i = 1; i <= 10; i++) {
            DeyochUser user = new DeyochUser();
            user.setId(i);
            user.setUsername("user" + i);
            users.add(user);
        }
        return users;
    }

    /**
     * 生成实体列表
     */
    @Provide
    Arbitrary<List<TestEntity>> entityLists() {
        return Arbitraries.longs().between(1L, 10L).optional()
            .tuple2()
            .map(tuple -> new TestEntity(tuple.get1().orElse(null), tuple.get2().orElse(null)))
            .list().ofMinSize(0).ofMaxSize(20);
    }

    /**
     * 生成包含重复用户的实体列表
     */
    @Provide
    Arbitrary<List<TestEntity>> entitiesWithDuplicateUsers() {
        return Arbitraries.longs().between(1L, 5L)
            .tuple2()
            .map(tuple -> new TestEntity(tuple.get1(), tuple.get2()))
            .list().ofMinSize(3).ofMaxSize(10);
    }

    /**
     * 生成不包含用户ID的实体列表
     */
    @Provide
    Arbitrary<List<TestEntity>> entitiesWithoutUserIds() {
        return Arbitraries.just(new TestEntity(null, null))
            .list().ofMinSize(1).ofMaxSize(5);
    }

    /**
     * 生成大型实体列表
     */
    @Provide
    Arbitrary<List<TestEntity>> largeEntityList() {
        return Arbitraries.longs().between(1L, 20L).optional()
            .tuple2()
            .map(tuple -> new TestEntity(tuple.get1().orElse(null), tuple.get2().orElse(null)))
            .list().ofMinSize(50).ofMaxSize(100);
    }
}