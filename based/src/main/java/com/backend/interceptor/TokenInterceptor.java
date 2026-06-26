package com.backend.interceptor;

import com.backend.common.UserContext;
import com.backend.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = extractToken(request);

        // 没有 token，直接返回 401
        if (token == null) {
            UserContext.clear();
            sendUnauthorized(response, "请先登录");
            return false;
        }

        try {
            Integer userId = jwtUtils.extractUserId(token);
            String username = jwtUtils.extractUsername(token);
            String role = jwtUtils.extractRole(token);

            if (jwtUtils.isTokenExpired(token)) {
                UserContext.clear();
                sendUnauthorized(response, "登录已过期，请重新登录");
                return false;
            }

            UserContext.UserInfo userInfo = new UserContext.UserInfo(userId, username, role);
            UserContext.setUserInfo(userInfo);
            return true;
        } catch (JwtException e) {
            UserContext.clear();
            sendUnauthorized(response, "token无效，请重新登录");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("message", message);
        body.put("data", null);
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
