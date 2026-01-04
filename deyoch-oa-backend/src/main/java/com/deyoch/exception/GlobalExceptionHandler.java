package com.deyoch.exception;

import com.deyoch.result.Result;
import com.deyoch.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理类
 * 用于统一处理系统中的所有异常，返回统一的响应格式
 * 隐藏具体的错误信息，只返回友好的提示给前端，同时记录详细日志
 *
 * @author deyoch-oa
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有未捕获的异常
     *
     * @param e 异常对象
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // 记录详细日志，包括异常堆栈
        log.error("系统异常：", e);
        
        // 返回友好的错误提示，不暴露具体的异常信息
        return Result.error(ResultCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
    }

    /**
     * 处理数据访问异常（如SQL异常）
     *
     * @param e 数据访问异常
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(DataAccessException.class)
    public Result<Void> handleDataAccessException(DataAccessException e) {
        // 记录详细日志
        log.error("数据库操作异常：", e);
        
        // 检查是否为唯一性约束冲突
        if (e instanceof DuplicateKeyException) {
            return Result.error(ResultCode.DATA_DUPLICATE, "数据已存在，请检查后重试");
        }
        
        // 检查是否包含SQL异常
        Throwable cause = e.getCause();
        if (cause instanceof SQLException) {
            SQLException sqlException = (SQLException) cause;
            log.error("SQL异常：", sqlException);
            
            // 检查是否为字段为空错误
            if (sqlException.getMessage().contains("doesn't have a default value")) {
                return Result.error(ResultCode.DATABASE_ERROR, "系统数据异常，请联系管理员");
            }
        }
        
        // 其他数据库异常
        return Result.error(ResultCode.DATABASE_ERROR, "操作失败，请稍后重试");
    }

    /**
     * 处理参数验证异常
     * 用于@Validated或@Valid注解的参数验证失败
     *
     * @param e 参数验证异常
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 获取所有验证失败的字段和错误信息
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        // 记录日志
        log.warn("参数验证失败：{}", errorMessage);
        
        // 返回友好的错误提示
        return Result.error(ResultCode.PARAM_VALID_ERROR, errorMessage);
    }

    /**
     * 处理参数类型不匹配异常
     * 用于参数类型转换失败，如字符串转换为数字失败
     *
     * @param e 参数类型不匹配异常
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        // 记录日志
        log.warn("参数类型错误：{}，期望类型：{}", e.getName(), e.getRequiredType().getSimpleName());
        
        // 返回友好的错误提示
        return Result.error(ResultCode.PARAM_TYPE_ERROR, "参数类型错误，请检查后重试");
    }

    /**
     * 处理空指针异常
     *
     * @param e 空指针异常
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<Void> handleNullPointerException(NullPointerException e) {
        // 记录详细日志
        log.error("空指针异常：", e);
        
        // 返回友好的错误提示
        return Result.error(ResultCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
    }

    /**
     * 处理数组越界异常
     *
     * @param e 数组越界异常
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
    public Result<Void> handleArrayIndexOutOfBoundsException(ArrayIndexOutOfBoundsException e) {
        // 记录详细日志
        log.error("数组越界异常：", e);
        
        // 返回友好的错误提示
        return Result.error(ResultCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
    }
}
