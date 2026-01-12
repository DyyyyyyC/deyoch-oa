package com.deyoch.config;

import com.deyoch.websocket.MessageWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final MessageWebSocketHandler messageWebSocketHandler;
    
    public WebSocketConfig(MessageWebSocketHandler messageWebSocketHandler) {
        this.messageWebSocketHandler = messageWebSocketHandler;
    }
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("注册WebSocket处理器...");
        
        // 注册消息WebSocket处理器
        // 由于应用的context-path是/api，所以WebSocket端点会自动加上/api前缀
        // 实际访问路径：ws://localhost:8080/api/ws/message
        registry.addHandler(messageWebSocketHandler, "/ws/message")
                .setAllowedOriginPatterns("*"); // 允许所有来源
        
        log.info("WebSocket处理器注册完成");
        log.info("WebSocket端点: /ws/message");
        log.info("实际访问路径: ws://localhost:8080/api/ws/message");
    }
}