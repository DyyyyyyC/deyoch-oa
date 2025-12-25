package com.deyoch.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 * 定义系统中所有的响应状态码和消息
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    
    // ========== 成功状态 ==========
    SUCCESS(200, "操作成功"),
    
    // ========== 客户端错误 4xx ==========
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "数据冲突"),
    VALIDATION_ERROR(422, "参数验证失败"),
    
    // ========== 服务器错误 5xx ==========
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    GATEWAY_ERROR(504, "网关错误"),
    
    // ========== 业务错误 1xxx ==========
    
    // 用户相关 1001-1099
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_EXISTS(1004, "用户已存在"),
    USER_NOT_LOGIN(1005, "用户未登录"),
    USER_TOKEN_EXPIRED(1006, "登录已过期，请重新登录"),
    USER_TOKEN_INVALID(1007, "无效的Token"),
    
    // 角色相关 1101-1199
    ROLE_NOT_FOUND(1101, "角色不存在"),
    ROLE_EXISTS(1102, "角色已存在"),
    ROLE_DISABLED(1103, "角色已被禁用"),
    
    // 权限相关 1201-1299
    PERMISSION_DENIED(1201, "没有操作权限"),
    PERMISSION_NOT_FOUND(1202, "权限不存在"),
    
    // 参数相关 1301-1399
    PARAM_IS_NULL(1301, "参数为空"),
    PARAM_TYPE_ERROR(1302, "参数类型错误"),
    PARAM_VALID_ERROR(1303, "参数校验失败"),
    
    // 数据库相关 1401-1499
    DATABASE_ERROR(1401, "数据库操作失败"),
    DATA_NOT_FOUND(1402, "数据不存在"),
    DATA_DUPLICATE(1403, "数据重复"),
    
    // 文件相关 1501-1599
    FILE_NOT_FOUND(1501, "文件不存在"),
    FILE_UPLOAD_ERROR(1502, "文件上传失败"),
    FILE_TOO_LARGE(1503, "文件过大"),
    FILE_TYPE_NOT_ALLOWED(1504, "文件类型不允许");
    
    /**
     * 状态码
     */
    private final Integer code;
    
    /**
     * 状态消息
     */
    private final String message;
    
    /**
     * 根据状态码获取枚举
     * 
     * @param code 状态码
     * @return 对应的枚举，如果未找到返回null
     */
    public static ResultCode getByCode(Integer code) {
        for (ResultCode resultCode : values()) {
            if (resultCode.getCode().equals(code)) {
                return resultCode;
            }
        }
        return null;
    }
    
    /**
     * 判断状态码是否表示成功
     * 
     * @param code 状态码
     * @return 成功返回true
     */
    public static boolean isSuccess(Integer code) {
        return SUCCESS.getCode().equals(code);
    }
}