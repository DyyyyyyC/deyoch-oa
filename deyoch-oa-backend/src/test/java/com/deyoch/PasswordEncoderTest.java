package com.deyoch;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密测试类
 * 用于生成BCrypt加密后的密码，供数据库初始化使用
 */
public class PasswordEncoderTest {

    /**
     * 生成BCrypt加密后的密码
     */
    @Test
    public void testGeneratePassword() {
        // 创建BCrypt密码编码器
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // 要加密的明文密码
        String rawPassword = "123456";
        
        // 生成加密后的密码
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // 输出加密结果
        System.out.println("明文密码: " + rawPassword);
        System.out.println("加密后密码: " + encodedPassword);
        
        // 验证密码是否匹配
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        System.out.println("密码匹配: " + matches);
    }
}
