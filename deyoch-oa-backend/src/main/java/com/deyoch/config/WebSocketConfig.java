package com.deyoch.config;

import com.deyoch.websocket.MessageWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Bean
    public MessageWebSocketHandler messageWebSocketHandler() {
        return new MessageWebSocketHandler();
    }
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册消息WebSocket处理器
        // WebSocket路径需要包含完整路径，因为context-path不会自动添加到WebSocket端点
        registry.addHandler(messageWebSocketHandler(), "/ws/message")
                .setAllowedOrigins("*") // 生产环境应该配置具体的域名
                .withSockJS(); // 启用SockJS支持
    }
}