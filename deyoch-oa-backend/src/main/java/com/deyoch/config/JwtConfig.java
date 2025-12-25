package com.deyoch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置类
 * 使用@ConfigurationProperties注解绑定配置文件中的jwt.*属性
 * 使用Lombok的@Data注解自动生成getter、setter等方法
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {
    
    /**
     * JWT签名密钥
     */
    private String secret;
    
    /**
     * 令牌过期时间，单位毫秒
     */
    private long expire;
    
    /**
     * 令牌在HTTP请求头中的名称
     */
    private String tokenHeader;
    
    /**
     * 令牌前缀
     */
    private String tokenPrefix;
}
