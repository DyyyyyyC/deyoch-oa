package com.deyoch.config;

import com.deyoch.entity.DeyochUser;
import com.deyoch.mapper.DeyochUserMapper;
import com.deyoch.result.Result;
import com.deyoch.service.PermissionService;
import com.deyoch.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * JWT认证过滤器
 * 实现OncePerRequestFilter接口，确保每个请求只执行一次
 * 核心功能：验证请求头中的JWT Token，并将用户信息设置到SecurityContext
 * 
 * @author deyoch-oa
 */
@Component
@AllArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    
    private final JwtUtil jwtUtil;
    private final DeyochUserMapper deyochUserMapper;
    private final PermissionService permissionService;

    /**
     * 过滤请求，验证JWT Token
     * 核心逻辑：
     * 1. 从请求头获取Token
     * 2. 验证Token有效性
     * 3. 获取用户信息
     * 4. 从数据库获取用户权限
     * 5. 设置认证信息到SecurityContext
     * 6. 继续执行过滤器链
     * 
     * @param request HttpServletRequest 请求对象
     * @param response HttpServletResponse 响应对象
     * @param filterChain FilterChain 过滤器链
     * @throws ServletException 服务异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. 从请求头中获取JWT Token
            // 默认格式：Authorization: Bearer {token}
            String token = extractTokenFromRequest(request);

            // 2. 验证Token是否有效
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // 3. 解析Token，获取Claims
                Claims claims = jwtUtil.parseToken(token);

                // 4. 从Claims中获取用户信息
                String username = claims.getSubject();

                // 5. 从数据库获取用户详情
                LambdaQueryWrapper<DeyochUser> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(DeyochUser::getUsername, username);
                DeyochUser user = deyochUserMapper.selectOne(queryWrapper);

                if (user != null) {
                    // 6. 获取用户权限编码列表
                    Result<List<String>> permCodesResult = permissionService.getUserPermCodes(user.getId());
                    List<String> permCodes = new ArrayList<>();
                    if (permCodesResult.getCode() == 200 && permCodesResult.getData() != null) {
                        permCodes = permCodesResult.getData();
                    }

                    // 7. 将权限编码转换为GrantedAuthority对象列表
                    List<GrantedAuthority> authorities = permCodes.stream()
                            .map(permCode -> new SimpleGrantedAuthority(permCode))
                            .collect(Collectors.toList());

                    // 8. 创建UserDetails对象
                    // UserDetails是Spring Security的核心接口，用于表示用户信息
                    UserDetails userDetails = new User(
                            username,  // 用户名
                            "",  // 密码（JWT认证不需要密码）
                            authorities  // 权限列表
                    );

                    // 9. 创建认证对象
                    // UsernamePasswordAuthenticationToken是Spring Security的认证对象
                    // 参数1：用户详情，参数2：凭证（JWT不需要），参数3：权限列表
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,  // 认证主体
                            null,  // 凭证（密码），JWT认证不需要
                            userDetails.getAuthorities()  // 权限列表
                    );

                    // 10. 设置认证信息到SecurityContext
                    // SecurityContext是Spring Security的核心上下文，用于存储认证信息
                    // 后续的@PreAuthorize等注解会从这里获取认证信息
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            // 11. 处理Token验证异常
            // 可以记录日志，或者返回401响应
            log.error("JWT认证失败: {}", e);
        }

        // 12. 继续执行过滤器链
        // 无论Token是否有效，都必须继续执行后续过滤器
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取JWT Token
     * 默认从Authorization头中获取，格式为：Bearer {token}
     * 
     * @param request HttpServletRequest 请求对象
     * @return JWT Token，如果没有则返回null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // 获取Authorization请求头
        String authHeader = request.getHeader("Authorization");
        
        // 检查Authorization头是否存在，且以"Bearer "开头
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            // 提取Bearer后面的Token部分
            return authHeader.substring(7);
        }
        return null;
    }
}
