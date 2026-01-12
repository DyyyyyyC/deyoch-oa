package com.deyoch.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.deyoch.dto.MessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息WebSocket处理器
 * 负责处理WebSocket连接和消息推送
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Slf4j
@Component
public class MessageWebSocketHandler implements WebSocketHandler {
    
    /**
     * 存储用户会话，key为用户ID，value为WebSocket会话
     */
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    
    /**
     * JSON序列化工具
     */
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket连接建立，会话ID: {}", session.getId());
        log.info("连接URI: {}", session.getUri());
        log.info("远程地址: {}", session.getRemoteAddress());
        
        String userId = getUserIdFromSession(session);
        log.info("解析到的用户ID: {}", userId);
        
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("用户 {} 建立WebSocket连接成功，当前在线用户数: {}", userId, userSessions.size());
            
            // 发送连接成功消息
            sendMessage(session, createSystemMessage("连接成功", "WebSocket连接已建立"));
        } else {
            log.warn("无法获取用户ID，关闭连接。URI: {}", session.getUri());
            session.close();
        }
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String userId = getUserIdFromSession(session);
        log.debug("收到用户 {} 的消息: {}", userId, message.getPayload());
        
        // 处理心跳消息
        if ("ping".equals(message.getPayload())) {
            session.sendMessage(new TextMessage("pong"));
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String userId = getUserIdFromSession(session);
        log.error("用户 {} WebSocket传输错误", userId, exception);
        
        // 移除会话
        if (userId != null) {
            userSessions.remove(userId);
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
            log.info("用户 {} 断开WebSocket连接，当前在线用户数: {}", userId, userSessions.size());
        }
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * 向指定用户发送消息
     * 
     * @param userId 用户ID
     * @param message 消息对象
     */
    public void sendMessageToUser(String userId, MessageDto message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
                log.debug("向用户 {} 发送消息成功: {}", userId, message.getTitle());
            } catch (Exception e) {
                log.error("向用户 {} 发送消息失败", userId, e);
                // 如果发送失败，移除无效会话
                userSessions.remove(userId);
            }
        } else {
            log.debug("用户 {} 不在线，无法发送实时消息", userId);
        }
    }
    
    /**
     * 广播消息给所有在线用户
     * 
     * @param message 消息对象
     */
    public void broadcastMessage(MessageDto message) {
        userSessions.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    String jsonMessage = objectMapper.writeValueAsString(message);
                    session.sendMessage(new TextMessage(jsonMessage));
                } catch (Exception e) {
                    log.error("向用户 {} 广播消息失败", userId, e);
                    userSessions.remove(userId);
                }
            }
        });
        log.info("广播消息给 {} 个在线用户: {}", userSessions.size(), message.getTitle());
    }
    
    /**
     * 获取在线用户数量
     * 
     * @return 在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }
    
    /**
     * 检查用户是否在线
     * 
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(String userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }
    
    /**
     * 从WebSocket会话中获取用户ID
     * 
     * @param session WebSocket会话
     * @return 用户ID
     */
    private String getUserIdFromSession(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            log.debug("解析WebSocket URI: {}", uri);
            
            if (uri != null) {
                String query = uri.getQuery();
                log.debug("查询参数: {}", query);
                
                if (query != null) {
                    // 解析查询参数，格式：userId=123
                    String[] params = query.split("&");
                    for (String param : params) {
                        String[] keyValue = param.split("=");
                        if (keyValue.length == 2 && "userId".equals(keyValue[0])) {
                            log.debug("从URL参数中找到用户ID: {}", keyValue[1]);
                            return keyValue[1];
                        }
                    }
                }
            }
            
            // 如果URL中没有userId参数，尝试从session属性中获取
            Object userIdAttr = session.getAttributes().get("userId");
            if (userIdAttr != null) {
                log.debug("从session属性中找到用户ID: {}", userIdAttr);
                return userIdAttr.toString();
            }
            
            log.warn("无法从URI或session属性中获取用户ID");
            
        } catch (Exception e) {
            log.error("获取用户ID失败", e);
        }
        
        return null;
    }
    
    /**
     * 向指定会话发送消息
     * 
     * @param session WebSocket会话
     * @param message 消息对象
     */
    private void sendMessage(WebSocketSession session, MessageDto message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (IOException e) {
            log.error("发送消息失败", e);
        }
    }
    
    /**
     * 创建系统消息
     * 
     * @param title 消息标题
     * @param content 消息内容
     * @return 消息对象
     */
    private MessageDto createSystemMessage(String title, String content) {
        MessageDto message = new MessageDto();
        message.setTitle(title);
        message.setContent(content);
        message.setType(1); // 系统消息
        message.setPriority(1); // 普通优先级
        return message;
    }
}