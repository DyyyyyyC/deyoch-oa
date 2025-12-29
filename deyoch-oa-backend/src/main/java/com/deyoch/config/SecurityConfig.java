package com.deyoch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置类
 * 负责配置系统的认证和授权规则
 * Spring Boot 3.x 推荐使用 SecurityFilterChain 配置
 * 
 * @author deyoch-oa
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * 配置密码编码器
     * 用于密码的加密和验证
     * BCryptPasswordEncoder 是 Spring 推荐的密码加密算法
     * 特点：
     * 1. 自动生成随机盐值
     * 2. 算法强度可配置（默认10）
     * 3. 单向加密，不可逆
     * 
     * @return PasswordEncoder 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 创建 BCrypt 密码编码器，强度为12（推荐值，平衡安全性和性能）
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置安全过滤器链
     * 定义系统的认证和授权规则
     * 
     * @param http HttpSecurity 配置对象
     * @param jwtAuthenticationFilter JWT认证过滤器
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        // 1. 配置 CSRF 保护
        // 前后端分离项目，CSRF 保护可禁用（前端需自行处理）
        http.csrf(AbstractHttpConfigurer::disable)
                // 2. 配置请求授权规则
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // 登录接口允许匿名访问
                                .requestMatchers("/auth/login").permitAll()
                                // Swagger相关路径允许匿名访问
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                // 后续可添加更多不需要认证的接口
                                // .requestMatchers("/public/**").permitAll()
                                // 其他所有请求都需要认证
                                .anyRequest().authenticated()
                )
                // 3. 添加JWT认证过滤器
                // 将JWT过滤器添加到UsernamePasswordAuthenticationFilter之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 返回配置好的安全过滤器链
        return http.build();
    }

    // 后续可以添加的配置：
    // 1. JWT 认证过滤器
    // 2. 自定义认证成功/失败处理
    // 3. 自定义授权失败处理
    // 4. 会话管理配置
    // 5. 跨域配置（如果前端和后端不在同一域名下）
    // 6. 记住我功能
    // 7. 验证码配置
}
