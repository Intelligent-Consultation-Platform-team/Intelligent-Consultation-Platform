package com.backend.interceptor;

import com.backend.common.UserContext;
import com.backend.utils.JwtUtils;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = extractToken(request);
        
        if (token == null) {
            UserContext.clear();
            return true;
        }

        try {
            Integer userId = jwtUtils.extractUserId(token);
            String username = jwtUtils.extractUsername(token);
            String role = jwtUtils.extractRole(token);

            if (jwtUtils.isTokenExpired(token)) {
                UserContext.clear();
                return true;
            }

            UserContext.UserInfo userInfo = new UserContext.UserInfo(userId, username, role);
            UserContext.setUserInfo(userInfo);
            return true;
        } catch (JwtException e) {
            UserContext.clear();
            return true;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}