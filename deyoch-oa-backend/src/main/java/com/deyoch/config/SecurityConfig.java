package com.deyoch.config;

import com.deyoch.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置类
 * 配置安全策略、JWT过滤器和公开接口
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置安全过滤器链
     * 
     * @param http HttpSecurity配置
     * @return 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（因为使用Token认证）
            .csrf(csrf -> csrf.disable())
            
            // 配置会话管理
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 放行公开接口
                .requestMatchers("/auth/**").permitAll()           // 认证相关接口（登录、注册等）
                .requestMatchers("/user/check/**").permitAll()     // 用户检查接口
                .requestMatchers("/error").permitAll()             // 错误接口
                .requestMatchers("/actuator/**").permitAll()       // 健康检查（如果需要）
                .requestMatchers("/swagger-ui/**").permitAll()     // Swagger UI（如果使用）
                .requestMatchers("/v3/api-docs/**").permitAll()    // OpenAPI文档（如果使用）
                
                // 其他接口需要认证
                .anyRequest().authenticated()
            )
            
            // 添加JWT过滤器（在UsernamePasswordAuthenticationFilter之前）
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 配置异常处理
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(401);
                    response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"timestamp\":" + System.currentTimeMillis() + "}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(403);
                    response.getWriter().write("{\"code\":403,\"message\":\"没有操作权限\",\"timestamp\":" + System.currentTimeMillis() + "}");
                })
            );
        
        return http.build();
    }
}