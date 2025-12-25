package com.deyoch.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一响应结果类
 * 用于规范所有接口的返回格式
 * 
 * @param <T> 响应数据的类型
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Data
public class Result<T> {
    
    /**
     * 响应状态码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 响应时间（友好格式）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * 创建一个成功响应（无数据）
     * 
     * @return 成功响应结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }
    
    /**
     * 创建一个成功响应（有数据）
     * 
     * @param data 响应数据
     * @return 成功响应结果
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
     * 创建一个成功响应（自定义消息）
     * 
     * @param message 成功消息
     * @param data 响应数据
     * @return 成功响应结果
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }
    
    /**
     * 创建一个失败响应
     * 
     * @param resultCode 状态码枚举
     * @return 失败响应结果
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        result.setTimestamp(LocalDateTime.now());
        return result;
    }
    
    /**
     * 创建一个失败响应（自定义消息）
     * 
     * @param code 状态码
     * @param message 错误消息
     * @return 失败响应结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }
    
    /**
     * 创建一个失败响应（使用ResultCode和自定义消息）
     * 
     * @param resultCode 状态码枚举
     * @param message 自定义错误消息
     * @return 失败响应结果
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(message);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }
    
    /**
     * 判断是否为成功响应
     * 
     * @return 是成功响应返回true
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
}