package com.deyoch.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * 显示字段注解正确性测试
 * 功能：entity-field-fixes，属性5：显示字段注解正确性
 * 验证：需求 3.5
 */
@DisplayName("显示字段注解正确性测试")
public class DisplayFieldAnnotationTest {

    @Test
    @DisplayName("DeyochAnnouncement的publisherName字段应该有@TableField(exist = false)注解")
    void announcementPublisherNameShouldHaveTableFieldAnnotation() throws NoSuchFieldException {
        Field publisherNameField = DeyochAnnouncement.class.getDeclaredField("publisherName");
        
        // 验证字段有@TableField注解
        assertThat(publisherNameField.isAnnotationPresent(TableField.class))
            .as("DeyochAnnouncement的publisherName字段应该有@TableField注解")
            .isTrue();
        
        // 验证注解的exist属性为false
        TableField annotation = publisherNameField.getAnnotation(TableField.class);
        assertThat(annotation.exist())
            .as("DeyochAnnouncement的publisherName字段的@TableField注解的exist属性应该为false")
            .isFalse();
    }

    @Test
    @DisplayName("DeyochDocument的uploaderName字段应该有@TableField(exist = false)注解")
    void documentUploaderNameShouldHaveTableFieldAnnotation() throws NoSuchFieldException {
        Field uploaderNameField = DeyochDocument.class.getDeclaredField("uploaderName");
        
        // 验证字段有@TableField注解
        assertThat(uploaderNameField.isAnnotationPresent(TableField.class))
            .as("DeyochDocument的uploaderName字段应该有@TableField注解")
            .isTrue();
        
        // 验证注解的exist属性为false
        TableField annotation = uploaderNameField.getAnnotation(TableField.class);
        assertThat(annotation.exist())
            .as("DeyochDocument的uploaderName字段的@TableField注解的exist属性应该为false")
            .isFalse();
    }

    @Test
    @DisplayName("DeyochSchedule的creatorName字段应该有@TableField(exist = false)注解")
    void scheduleCreatorNameShouldHaveTableFieldAnnotation() throws NoSuchFieldException {
        Field creatorNameField = DeyochSchedule.class.getDeclaredField("creatorName");
        
        // 验证字段有@TableField注解
        assertThat(creatorNameField.isAnnotationPresent(TableField.class))
            .as("DeyochSchedule的creatorName字段应该有@TableField注解")
            .isTrue();
        
        // 验证注解的exist属性为false
        TableField annotation = creatorNameField.getAnnotation(TableField.class);
        assertThat(annotation.exist())
            .as("DeyochSchedule的creatorName字段的@TableField注解的exist属性应该为false")
            .isFalse();
    }

    @Test
    @DisplayName("DeyochProcessInstance的creatorName字段应该有@TableField(exist = false)注解")
    void processInstanceCreatorNameShouldHaveTableFieldAnnotation() throws NoSuchFieldException {
        Field creatorNameField = DeyochProcessInstance.class.getDeclaredField("creatorName");
        
        // 验证字段有@TableField注解
        assertThat(creatorNameField.isAnnotationPresent(TableField.class))
            .as("DeyochProcessInstance的creatorName字段应该有@TableField注解")
            .isTrue();
        
        // 验证注解的exist属性为false
        TableField annotation = creatorNameField.getAnnotation(TableField.class);
        assertThat(annotation.exist())
            .as("DeyochProcessInstance的creatorName字段的@TableField注解的exist属性应该为false")
            .isFalse();
    }

    @Test
    @DisplayName("DeyochTask的现有显示字段应该有@TableField(exist = false)注解")
    void taskDisplayFieldsShouldHaveTableFieldAnnotation() throws NoSuchFieldException {
        List<String> displayFields = Arrays.asList("creatorName", "assigneeName");
        
        for (String fieldName : displayFields) {
            Field field = DeyochTask.class.getDeclaredField(fieldName);
            
            // 验证字段有@TableField注解
            assertThat(field.isAnnotationPresent(TableField.class))
                .as("DeyochTask的%s字段应该有@TableField注解", fieldName)
                .isTrue();
            
            // 验证注解的exist属性为false
            TableField annotation = field.getAnnotation(TableField.class);
            assertThat(annotation.exist())
                .as("DeyochTask的%s字段的@TableField注解的exist属性应该为false", fieldName)
                .isFalse();
        }
    }

    @Test
    @DisplayName("所有新增的显示字段都应该是String类型")
    void allDisplayFieldsShouldBeStringType() throws NoSuchFieldException {
        // 定义所有显示字段及其所属实体类
        Object[][] displayFields = {
            {DeyochAnnouncement.class, "publisherName"},
            {DeyochDocument.class, "uploaderName"},
            {DeyochSchedule.class, "creatorName"},
            {DeyochProcessInstance.class, "creatorName"},
            {DeyochTask.class, "creatorName"},
            {DeyochTask.class, "assigneeName"}
        };
        
        for (Object[] fieldInfo : displayFields) {
            Class<?> entityClass = (Class<?>) fieldInfo[0];
            String fieldName = (String) fieldInfo[1];
            
            Field field = entityClass.getDeclaredField(fieldName);
            assertThat(field.getType())
                .as("%s的%s字段应该是String类型", entityClass.getSimpleName(), fieldName)
                .isEqualTo(String.class);
        }
    }
}