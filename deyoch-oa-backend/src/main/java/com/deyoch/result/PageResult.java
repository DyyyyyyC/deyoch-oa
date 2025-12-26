package com.deyoch.result;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页结果封装类
 * 用于分页查询的返回结果
 * 包含分页信息和数据列表
 *
 * @param <T> 分页数据的类型
 * @author deyoch-oa
 */
@Data
@NoArgsConstructor
public class PageResult<T> {
    
    /**
     * 当前页码
     * 从1开始计数
     */
    private Long current;
    
    /**
     * 每页大小
     * 每页显示的记录数
     */
    private Long size;
    
    /**
     * 总记录数
     * 满足条件的总记录数
     */
    private Long total;
    
    /**
     * 总页数
     * 计算公式：(total + size - 1) / size
     */
    private Long pages;
    
    /**
     * 当前页的数据列表
     * 这一页要显示的数据
     */
    private List<T> records;
    
    /**
     * 创建一个分页结果
     * 用于分页查询后，构建返回结果
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param total 总记录数
     * @param records 当前页数据列表
     * @return 分页结果
     */
    public static <T> PageResult<T> of(Long current, Long size, Long total, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setCurrent(current);
        result.setSize(size);
        result.setTotal(total);
        result.setRecords(records);
        
        // 计算总页数
        // 公式：(total + size - 1) / size
        // 例如：total=100, size=10 -> pages=10
        // 例如：total=101, size=10 -> pages=11
        result.setPages((total + size - 1) / size);
        
        return result;
    }
    
    /**
     * 创建一个空分页结果
     * 用于查询无结果时，返回空的分页对象
     *
     * @param current 当前页码
     * @param size 每页大小
     * @return 空分页结果
     */
    public static <T> PageResult<T> empty(Long current, Long size) {
        PageResult<T> result = new PageResult<>();
        result.setCurrent(current);
        result.setSize(size);
        result.setTotal(0L);
        result.setPages(0L);
        result.setRecords(new ArrayList<>());
        return result;
    }
    
    /**
     * 判断是否有上一页
     *
     * @return 有上一页返回true
     */
    public boolean hasPrevious() {
        // 当前页码大于1，说明有上一页
        return current != null && current > 1;
    }
    
    /**
     * 判断是否有下一页
     *
     * @return 有下一页返回true
     */
    public boolean hasNext() {
        // 当前页码小于总页数，说明有下一页
        return current != null && pages != null && current < pages;
    }
    
    /**
     * 获取偏移量
     * 用于数据库分页查询的LIMIT offset
     *
     * @return 偏移量
     */
    public Long getOffset() {
        // 计算公式：(current - 1) * size
        // 例如：current=1, size=10 -> offset=0
        // 例如：current=2, size=10 -> offset=10
        return (current - 1) * size;
    }
}