package com.deyoch.controller;

import com.deyoch.service.UserInfoConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * 测试控制器
 * 用于测试UserInfoConverter功能
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "测试接口", description = "用于测试系统功能")
public class TestController {

    private final UserInfoConverter userInfoConverter;

    /**
     * 测试用户ID转换
     */
    @GetMapping("/user-convert")
    @Operation(summary = "测试用户ID转换", description = "测试UserInfoConverter的用户ID到用户名转换功能")
    public Map<String, Object> testUserConvert(@RequestParam String userIds) {
        try {
            // 解析用户ID
            Set<Long> userIdSet = Set.of(userIds.split(","))
                .stream()
                .map(String::trim)
                .map(Long::parseLong)
                .collect(java.util.stream.Collectors.toSet());
            
            log.info("测试用户ID转换，输入用户ID: {}", userIdSet);
            
            // 调用转换方法
            Map<Long, String> result = userInfoConverter.convertUserIdsToNames(userIdSet);
            
            log.info("转换结果: {}", result);
            
            return Map.of(
                "success", true,
                "inputUserIds", userIdSet,
                "result", result,
                "message", "转换成功"
            );
        } catch (Exception e) {
            log.error("测试用户ID转换失败", e);
            return Map.of(
                "success", false,
                "error", e.getMessage(),
                "message", "转换失败"
            );
        }
    }
}