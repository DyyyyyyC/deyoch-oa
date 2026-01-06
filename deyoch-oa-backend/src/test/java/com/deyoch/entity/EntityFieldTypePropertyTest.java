package com.deyoch.entity;

import net.jqwik.api.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * 实体字段类型一致性属性测试
 * 功能：entity-field-fixes，属性1：实体字段类型一致性
 * 验证：需求 1.1, 1.2, 1.3, 1.5
 */
public class EntityFieldTypePropertyTest {

    /**
     * 需要验证的实体类列表
     */
    private static final List<Class<?>> ENTITY_CLASSES = Arrays.asList(
        DeyochUser.class,
        DeyochTask.class,
        DeyochAnnouncement.class,
        DeyochDocument.class,
        DeyochSchedule.class,
        DeyochDept.class,
        DeyochPermission.class,
        DeyochProcess.class,
        DeyochProcessInstance.class
    );

    /**
     * 应该是Integer类型的字段名列表
     */
    private static final List<String> INTEGER_FIELDS = Arrays.asList(
        "status",    // 所有实体的status字段应该是Integer
        "priority",  // DeyochTask的priority字段应该是Integer
        "sort"       // DeyochDept和DeyochPermission的sort字段应该是Integer
    );

    @Property(tries = 100)
    @Label("对于任何实体类和对应的数据库表，实体类中的字段类型应该与数据库列类型正确映射")
    void entityFieldTypesShouldMatchDatabaseSchema(@ForAll("entityClasses") Class<?> entityClass) {
        // 获取实体类的所有字段
        Field[] fields = entityClass.getDeclaredFields();
        
        for (Field field : fields) {
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();
            
            // 验证应该是Integer类型的字段
            if (INTEGER_FIELDS.contains(fieldName)) {
                assertThat(fieldType)
                    .as("字段 %s 在实体类 %s 中应该是 Integer 类型，但实际是 %s", 
                        fieldName, entityClass.getSimpleName(), fieldType.getSimpleName())
                    .isEqualTo(Integer.class);
            }
            
            // 验证ID字段应该是Long类型
            if ("id".equals(fieldName) || fieldName.endsWith("Id")) {
                assertThat(fieldType)
                    .as("ID字段 %s 在实体类 %s 中应该是 Long 类型，但实际是 %s", 
                        fieldName, entityClass.getSimpleName(), fieldType.getSimpleName())
                    .isEqualTo(Long.class);
            }
        }
    }

    @Property(tries = 100)
    @Label("所有实体类的status字段都应该是Integer类型")
    void allStatusFieldsShouldBeInteger(@ForAll("entityClasses") Class<?> entityClass) {
        try {
            Field statusField = entityClass.getDeclaredField("status");
            assertThat(statusField.getType())
                .as("实体类 %s 的 status 字段应该是 Integer 类型", entityClass.getSimpleName())
                .isEqualTo(Integer.class);
        } catch (NoSuchFieldException e) {
            // 如果实体类没有status字段，跳过验证（如DeyochRole）
            // 这是正常情况，不需要报错
        }
    }

    @Property(tries = 100)
    @Label("DeyochDept和DeyochPermission的sort字段应该是Integer类型")
    void sortFieldsShouldBeInteger(@ForAll("sortEntityClasses") Class<?> entityClass) {
        try {
            Field sortField = entityClass.getDeclaredField("sort");
            assertThat(sortField.getType())
                .as("实体类 %s 的 sort 字段应该是 Integer 类型", entityClass.getSimpleName())
                .isEqualTo(Integer.class);
        } catch (NoSuchFieldException e) {
            fail("实体类 %s 应该有 sort 字段", entityClass.getSimpleName());
        }
    }

    /**
     * 提供所有实体类的生成器
     */
    @Provide
    Arbitrary<Class<?>> entityClasses() {
        return Arbitraries.of(ENTITY_CLASSES);
    }

    /**
     * 提供有sort字段的实体类生成器
     */
    @Provide
    Arbitrary<Class<?>> sortEntityClasses() {
        return Arbitraries.of(DeyochDept.class, DeyochPermission.class);
    }
}