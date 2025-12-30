package com.deyoch.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 * 定义系统中所有的响应状态码和消息
 * 便于统一管理和维护，避免硬编码
 *
 * @author deyoch-oa
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    
    // ========== 成功状态 ==========
    /**
     * 操作成功
     * 用于所有成功的操作
     */
    SUCCESS(200, "操作成功"),
    
    // ========== 客户端错误 4xx ==========
    /**
     * 请求参数错误
     * 用于前端传参不合法的情况
     */
    BAD_REQUEST(400, "请求参数错误"),
    
    /**
     * 未登录或登录已过期
     * 用于需要登录但用户未登录的情况
     */
    UNAUTHORIZED(401, "未登录或登录已过期"),
    
    /**
     * 没有操作权限
     * 用于用户已登录但没有权限操作的情况
     */
    FORBIDDEN(403, "没有操作权限"),
    
    /**
     * 请求的资源不存在
     * 用于查询不存在的资源，如用户ID不存在
     */
    NOT_FOUND(404, "请求的资源不存在"),
    
    /**
     * 请求方法不允许
     * 用于使用了不支持的HTTP方法
     */
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    
    // ========== 服务器错误 5xx ==========
    /**
     * 服务器内部错误
     * 用于服务器端未知错误
     */
    INTERNAL_ERROR(500, "服务器内部错误"),
    
    /**
     * 服务不可用
     * 用于服务器维护或过载的情况
     */
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    // ========== 业务错误 1xxx ==========
    
    // 用户相关错误 1001-1099
    
    /**
     * 用户不存在
     * 用于根据ID或用户名查询用户，但用户不存在
     */
    USER_NOT_FOUND(1001, "用户不存在"),
    
    /**
     * 用户名或密码错误
     * 用于登录时用户名或密码不正确
     */
    USER_PASSWORD_ERROR(1002, "用户名或密码错误"),
    
    /**
     * 用户已被禁用
     * 用于用户状态为禁用时尝试登录
     */
    USER_DISABLED(1003, "用户已被禁用"),
    
    /**
     * 用户已存在
     * 用于创建用户时，用户名已重复
     */
    USER_EXISTS(1004, "用户已存在"),
    
    /**
     * 用户未登录
     * 用于需要登录但用户未登录的情况（与UNAUTHORIZED类似）
     */
    USER_NOT_LOGIN(1005, "用户未登录"),
    
    /**
     * 登录已过期
     * 用于Token过期，需要重新登录
     */
    USER_TOKEN_EXPIRED(1006, "登录已过期，请重新登录"),
    
    /**
     * 无效的Token
     * 用于Token格式不正确或被篡改
     */
    USER_TOKEN_INVALID(1007, "无效的Token"),
    
    // 角色相关错误 1101-1199
    
    /**
     * 角色不存在
     * 用于根据ID查询角色，但角色不存在
     */
    ROLE_NOT_FOUND(1101, "角色不存在"),
    
    /**
     * 角色已存在
     * 用于创建角色时，角色名已重复
     */
    ROLE_EXISTS(1102, "角色已存在"),
    
    /**
     * 角色已被禁用
     * 用于角色状态为禁用时
     */
    ROLE_DISABLED(1103, "角色已被禁用"),
    
    // 权限相关错误 1201-1299
    
    /**
     * 没有操作权限
     * 用于用户没有权限访问某个接口
     */
    PERMISSION_DENIED(1201, "没有操作权限"),
    
    /**
     * 权限不存在
     * 用于根据ID查询权限，但权限不存在
     */
    PERMISSION_NOT_FOUND(1202, "权限不存在"),
    
    // 参数相关错误 1301-1399
    
    /**
     * 参数为空
     * 用于必填参数为空的情况
     */
    PARAM_IS_NULL(1301, "参数为空"),
    
    /**
     * 参数类型错误
     * 用于参数类型不匹配，如期望String但收到Number
     */
    PARAM_TYPE_ERROR(1302, "参数类型错误"),
    
    /**
     * 参数校验失败
     * 用于参数校验不通过，如邮箱格式不正确
     */
    PARAM_VALID_ERROR(1303, "参数校验失败"),
    
    // 数据库相关错误 1401-1499
    
    /**
     * 数据库操作失败
     * 用于数据库操作失败，如插入失败、更新失败
     */
    DATABASE_ERROR(1401, "数据库操作失败"),
    
    /**
     * 数据不存在
     * 用于查询的数据不存在
     */
    DATA_NOT_FOUND(1402, "数据不存在"),
    
    /**
     * 数据重复
     * 用于唯一性约束冲突，如用户名重复
     */
    DATA_DUPLICATE(1403, "数据重复"),
    
    // 公告相关错误 1501-1599
    
    /**
     * 公告不存在
     * 用于根据ID查询公告，但公告不存在
     */
    ANNOUNCEMENT_NOT_FOUND(1501, "公告不存在"),
    
    // 任务相关错误 1601-1699
    
    /**
     * 任务不存在
     * 用于根据ID查询任务，但任务不存在
     */
    TASK_NOT_FOUND(1601, "任务不存在"),
    
    // 日程相关错误 1701-1799
    
    /**
     * 日程不存在
     * 用于根据ID查询日程，但日程不存在
     */
    SCHEDULE_NOT_FOUND(1701, "日程不存在"),
    
    // 部门相关错误 1801-1899
    
    /**
     * 部门不存在
     * 用于根据ID查询部门，但部门不存在
     */
    DEPT_NOT_FOUND(1801, "部门不存在"),
    
    /**
     * 部门存在子部门
     * 用于删除部门时，部门存在子部门
     */
    DEPT_HAS_CHILDREN(1802, "部门存在子部门，无法删除"),
    
    // 文档相关错误 1901-1999
    
    /**
     * 文档不存在
     * 用于根据ID查询文档，但文档不存在
     */
    DOCUMENT_NOT_FOUND(1901, "文档不存在"),
    
    // 流程相关错误 2001-2099
    
    /**
     * 流程不存在
     * 用于根据ID查询流程，但流程不存在
     */
    PROCESS_NOT_FOUND(2001, "流程不存在"),
    
    /**
     * 流程实例不存在
     * 用于根据ID查询流程实例，但流程实例不存在
     */
    PROCESS_INSTANCE_NOT_FOUND(2002, "流程实例不存在"),
    
    /**
     * 系统错误
     * 用于系统内部未知错误
     */
    SYSTEM_ERROR(500, "系统错误"),
    
    /**
     * 用户名已存在
     * 用于创建或修改用户时，用户名已被使用
     */
    USERNAME_EXIST(1008, "用户名已存在"),
    
    /**
     * 参数错误
     * 用于参数不合法的情况
     */
    PARAM_ERROR(400, "参数错误"),
    
    /**
     * 操作不允许
     * 用于禁止执行的操作，如删除自己
     */
    OPERATION_NOT_ALLOWED(403, "操作不允许");
    
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
     * 用于根据code查找对应的枚举
     *
     * @param code 状态码
     * @return 对应的枚举，如果未找到返回null
     */
    public static ResultCode getByCode(Integer code) {
        // 遍历所有枚举值，查找匹配的code
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