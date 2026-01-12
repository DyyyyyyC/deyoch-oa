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
        // WebSocket连接也需要禁用CSRF
        http.csrf(AbstractHttpConfigurer::disable)
                // 2. 配置请求授权规则
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // 登录接口允许匿名访问
                                .requestMatchers("/auth/login").permitAll()
                                // Swagger相关路径允许匿名访问
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                // WebSocket端点允许匿名访问（两种路径都允许）
                                .requestMatchers("/ws/**").permitAll()
                                .requestMatchers("/api/ws/**").permitAll()
                                // WebSocket测试端点允许匿名访问
                                .requestMatchers("/api/websocket/**").permitAll()
                                // 后续可添加更多不需要认证的接口
                                // .requestMatchers("/public/**").permitAll()
                                // 其他所有请求都需要认证
                                .anyRequest().authenticated()
                )
                // 3. 禁用会话管理（对WebSocket很重要）
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
                )
                // 4. 添加JWT认证过滤器
                // 将JWT过滤器添加到UsernamePasswordAuthenticationFilter之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 返回配置好的安全过滤器链
        return http.build();
    }

}
