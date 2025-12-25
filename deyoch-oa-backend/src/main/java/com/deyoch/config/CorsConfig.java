package com.deyoch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 * 用于配置允许跨域请求的规则
 */
@Configuration
public class CorsConfig {

    /**
     * 创建CORS过滤器
     * @return CorsFilter 跨域过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        // 创建CORS配置对象
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许前端域名访问，这里配置为允许所有域名，生产环境建议配置具体域名
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOriginPattern("*"); // 允许所有域名，生产环境建议注释掉
        
        // 允许的请求方法
        config.addAllowedMethod("*");
        
        // 允许的请求头
        config.addAllowedHeader("*");
        
        // 允许发送Cookie和凭证
        config.setAllowCredentials(true);
        
        // 暴露的响应头，前端可以获取这些头信息
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Disposition");
        
        // 创建URL映射源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 所有URL都应用CORS配置
        source.registerCorsConfiguration("/**", config);
        
        // 创建并返回CORS过滤器
        return new CorsFilter(source);
    }
}
