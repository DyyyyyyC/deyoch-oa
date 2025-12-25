package com.deyoch.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成、解析和验证JWT Token
 * 
 * @author deyoch-oa
 * @date 2025-12-25
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * JWT签名密钥
     */
    @Value("${jwt.secret:deyoch-oa-secret-key-2025}")
    private String secret;

    /**
     * Access Token过期时间（毫秒）
     */
    @Value("${jwt.expire:86400000}")
    private long expire;

    /**
     * Refresh Token过期时间（毫秒）
     */
    @Value("${jwt.refresh-expire:604800000}")
    private long refreshExpire;

    /**
     * 获取签名密钥
     * 
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成Access Token
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param roleId 角色ID
     * @return Access Token
     */
    public String generateAccessToken(Long userId, String username, Long roleId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roleId", roleId);
        claims.put("type", "access");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expire);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成Refresh Token
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return Refresh Token
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpire);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成Token对
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param roleId 角色ID
     * @return Token对 [accessToken, refreshToken]
     */
    public String[] generateTokenPair(Long userId, String username, Long roleId) {
        String accessToken = generateAccessToken(userId, username, roleId);
        String refreshToken = generateRefreshToken(userId, username);
        return new String[]{accessToken, refreshToken};
    }

    /**
     * 解析Token获取Claims
     * 
     * @param token JWT Token
     * @return Claims对象，如果解析失败返回null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.warn("JWT Token解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID，解析失败返回null
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return null;
    }

    /**
     * 从Token中获取用户名
     * 
     * @param token JWT Token
     * @return 用户名，解析失败返回null
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return claims.getSubject();
    }

    /**
     * 从Token中获取角色ID
     * 
     * @param token JWT Token
     * @return 角色ID，解析失败返回null
     */
    public Long getRoleId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object roleId = claims.get("roleId");
        if (roleId instanceof Number) {
            return ((Number) roleId).longValue();
        }
        return null;
    }

    /**
     * 获取Token类型
     * 
     * @param token JWT Token
     * @return Token类型(access/refresh)，解析失败返回null
     */
    public String getTokenType(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return (String) claims.get("type");
    }

    /**
     * 验证Token是否过期
     * 
     * @param token JWT Token
     * @return 未过期返回true，已过期返回false
     */
    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return true;
        }
        return claims.getExpiration().before(new Date());
    }

    /**
     * 验证Token是否有效
     * 
     * @param token JWT Token
     * @return 有效返回true，无效返回false
     */
    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        // 检查是否过期
        if (claims.getExpiration().before(new Date())) {
            return false;
        }
        return true;
    }

    /**
     * 验证是否为Access Token
     * 
     * @param token JWT Token
     * @return 是Access Token返回true
     */
    public boolean isAccessToken(String token) {
        String type = getTokenType(token);
        return "access".equals(type);
    }

    /**
     * 验证是否为Refresh Token
     * 
     * @param token JWT Token
     * @return 是Refresh Token返回true
     */
    public boolean isRefreshToken(String token) {
        String type = getTokenType(token);
        return "refresh".equals(type);
    }

    /**
     * 获取Access Token过期时间（秒）
     * 
     * @return 过期时间（秒）
     */
    public long getAccessTokenExpireSeconds() {
        return expire / 1000;
    }

    /**
     * 获取Refresh Token过期时间（秒）
     * 
     * @return 过期时间（秒）
     */
    public long getRefreshTokenExpireSeconds() {
        return refreshExpire / 1000;
    }
}