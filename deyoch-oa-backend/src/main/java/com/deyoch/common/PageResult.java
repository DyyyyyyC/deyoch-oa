package com.deyoch.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装类
 * 用于分页查询的返回结果
 * 
 * @param <T> 分页数据的类型
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Data
@NoArgsConstructor
public class PageResult<T> {
    
    /**
     * 当前页码
     */
    private Long current;
    
    /**
     * 每页大小
     */
    private Long size;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 总页数
     */
    private Long pages;
    
    /**
     * 当前页的数据列表
     */
    private List<T> records;
    
    /**
     * 创建一个分页结果
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @param total 总记录数
     * @param records 当前页数据列表
     * @param <T> 数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(Long current, Long size, Long total, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setCurrent(current);
        result.setSize(size);
        result.setTotal(total);
        result.setRecords(records);
        // 计算总页数
        result.setPages((total + size - 1) / size);
        return result;
    }
    
    /**
     * 创建一个空分页结果
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @param <T> 数据类型
     * @return 空分页结果
     */
    public static <T> PageResult<T> empty(Long current, Long size) {
        PageResult<T> result = new PageResult<>();
        result.setCurrent(current);
        result.setSize(size);
        result.setTotal(0L);
        result.setPages(0L);
        result.setRecords(List.of());
        return result;
    }
    
    /**
     * 判断是否有上一页
     * 
     * @return 有上一页返回true
     */
    public boolean hasPrevious() {
        return current != null && current > 1;
    }
    
    /**
     * 判断是否有下一页
     * 
     * @return 有下一页返回true
     */
    public boolean hasNext() {
        return current != null && pages != null && current < pages;
    }
}