package com.deyoch.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

/**
 * 优先级字段类型正确性单元测试
 * 功能：entity-field-fixes，属性2：优先级字段类型正确性
 * 验证：需求 1.4
 */
@DisplayName("优先级字段类型正确性单元测试")
public class TaskPriorityFieldTypeTest {

    @Test
    @DisplayName("DeyochTask实体的priority字段应该是Integer类型")
    void taskPriorityFieldShouldBeInteger() throws NoSuchFieldException {
        // 获取DeyochTask类的priority字段
        Field priorityField = DeyochTask.class.getDeclaredField("priority");
        
        // 验证字段类型是Integer
        assertThat(priorityField.getType())
            .as("DeyochTask的priority字段应该是Integer类型，但实际是%s", 
                priorityField.getType().getSimpleName())
            .isEqualTo(Integer.class);
    }

    @Test
    @DisplayName("DeyochTask实体可以正确设置和获取Integer类型的priority值")
    void taskCanSetAndGetIntegerPriorityValue() {
        // 创建DeyochTask实例
        DeyochTask task = new DeyochTask();
        
        // 测试设置不同的Integer优先级值
        Integer[] priorityValues = {1, 2, 3, null};
        
        for (Integer priority : priorityValues) {
            task.setPriority(priority);
            assertThat(task.getPriority())
                .as("设置priority为%s后，获取的值应该相同", priority)
                .isEqualTo(priority);
        }
    }

    @Test
    @DisplayName("DeyochTask实体的priority字段应该支持TINYINT数据库类型的值范围")
    void taskPriorityShouldSupportTinyintRange() {
        DeyochTask task = new DeyochTask();
        
        // TINYINT的有效范围是0-255（无符号）或-128到127（有符号）
        // 对于优先级，我们通常使用1-低，2-中，3-高
        Integer[] validPriorities = {1, 2, 3};
        
        for (Integer priority : validPriorities) {
            task.setPriority(priority);
            assertThat(task.getPriority())
                .as("优先级值%d应该在有效范围内", priority)
                .isEqualTo(priority)
                .isBetween(1, 3);
        }
    }

    @Test
    @DisplayName("验证DeyochTask实体的priority字段不是Long类型")
    void taskPriorityFieldShouldNotBeLong() throws NoSuchFieldException {
        Field priorityField = DeyochTask.class.getDeclaredField("priority");
        
        // 验证字段类型不是Long
        assertThat(priorityField.getType())
            .as("DeyochTask的priority字段不应该是Long类型")
            .isNotEqualTo(Long.class);
    }
}