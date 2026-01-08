package com.deyoch.common.result;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 统一响应结果类
 * 用于规范所有接口的返回格式
 *
 * @param <T> 响应数据的类型
 * @author deyoch-oa
 */
@Data
public class Result<T> {
    
    /**
     * 响应状态码
     * 200表示成功，其他值表示错误
     */
    private Integer code;
    
    /**
     * 响应消息
     * 成功或错误的提示信息
     */
    private String message;
    
    /**
     * 响应数据
     * 可以是任意类型：用户、分页、列表等
     */
    private T data;
    
    /**
     * 响应时间
     * 记录接口返回的时间，便于排查问题
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    // ========== 成功响应方法 ==========
    
    /**
     * 创建成功响应（无数据）
     * 用途：用于不需要返回数据的操作，如"删除成功"
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setTimestamp(LocalDateTime.now());
        return result;
    }
    
    /**
     * 创建成功响应（有数据）
     * 用途：用于需要返回数据的操作，如查询用户、查询列表
     * 
     * @param data 返回的数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }
    
    /**
     * 创建成功响应（自定义消息）
     * 用途：用于需要自定义成功提示的操作
     * 
     * @param message 成功消息，如"创建成功"、"更新成功"
     * @param data 返回的数据
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }
    
    // ========== 失败响应方法 ==========
    
    /**
     * 创建失败响应（使用ResultCode枚举）
     * 用途：用于标准的错误情况，如用户不存在、权限不足
     * 
     * @param resultCode 状态码枚举，包含code和message
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        result.setTimestamp(LocalDateTime.now());
        return result;
    }
    
    /**
     * 创建失败响应（自定义状态码和消息）
     * 用途：用于特殊的错误情况，需要自定义错误信息
     * 
     * @param code 状态码
     * @param message 错误消息
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }
    
    /**
     * 创建失败响应（使用ResultCode枚举和自定义消息）
     * 用途：用于需要覆盖默认错误消息的情况
     * 
     * @param resultCode 状态码枚举
     * @param message 自定义错误消息（会覆盖枚举中的默认消息）
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(message);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }
}