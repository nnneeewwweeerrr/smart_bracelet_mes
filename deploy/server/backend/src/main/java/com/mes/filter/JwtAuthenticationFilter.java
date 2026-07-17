package com.mes.filter;

import com.mes.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        String requestURI = request.getRequestURI();

        if (isPublicUrl(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        if (!StringUtils.hasText(token)) {
            sendErrorResponse(response, 401, "未登录，请先登录");
            return;
        }

        try {
            if (!jwtUtil.validateToken(token)) {
                sendErrorResponse(response, 401, "Token无效");
                return;
            }

            if (jwtUtil.isTokenExpired(token)) {
                sendErrorResponse(response, 401, "Token已过期，请重新登录");
                return;
            }

            Claims claims = jwtUtil.parseToken(token);
            String username = claims.getSubject();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            sendErrorResponse(response, 401, "Token验证失败，请重新登录");
        }
    }

    private boolean isPublicUrl(String requestURI) {
        return requestURI.startsWith("/api/auth/login") ||
               requestURI.startsWith("/api/auth/register") ||
               requestURI.startsWith("/api/auth/init-password") ||
               requestURI.startsWith("/api/auth/init-data") ||
               requestURI.startsWith("/api/user/register") ||
               requestURI.startsWith("/api/mini/") ||
               requestURI.startsWith("/api/big-screen/") ||
               requestURI.startsWith("/api/trace/") ||
               requestURI.startsWith("/api/sop/") ||
               requestURI.startsWith("/api/sn-trace/") ||
               requestURI.startsWith("/api/ai/") ||
               requestURI.startsWith("/api/document/") ||
               requestURI.startsWith("/api/kg/") ||
               requestURI.startsWith("/api/qrcode/") ||
               requestURI.startsWith("/api/exception-handle/") ||
               requestURI.startsWith("/api/dashboard/") ||
               requestURI.startsWith("/api/process-flow/") ||
               requestURI.startsWith("/api/demo/") ||
               requestURI.startsWith("/api/fix/") ||
               requestURI.startsWith("/swagger-ui/") ||
               requestURI.startsWith("/v3/api-docs/") ||
               requestURI.startsWith("/ws/");
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", status);
        errorResponse.put("message", message);
        errorResponse.put("data", null);
        
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}