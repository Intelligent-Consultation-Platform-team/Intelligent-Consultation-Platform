package com.backend.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT 工具类单元测试。
 * <p>
 * 测试功能：登录鉴权核心 — Token 的生成、解析、校验与过期处理。
 * 对应生产类：{@link JwtUtils}
 */
class JwtUtilsTest {

    private static final String TEST_SECRET = "TestSecretKeyForJWTUnitTests2024!";

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secretString", TEST_SECRET);
    }

    /** 验证生成的 Token 包含正确的 userId、username、role，且未过期 */
    @Test
    void generateToken_containsCorrectClaims() {
        String token = jwtUtils.generateToken(1001, "test_user", "patient");

        assertEquals("test_user", jwtUtils.extractUsername(token));
        assertEquals(1001, jwtUtils.extractUserId(token));
        assertEquals("patient", jwtUtils.extractRole(token));
        assertFalse(jwtUtils.isTokenExpired(token));
    }

    /** 验证用户名匹配且未过期时 validateToken 返回 true */
    @Test
    void validateToken_validToken_returnsTrue() {
        String token = jwtUtils.generateToken(1001, "test_user", "patient");

        assertTrue(jwtUtils.validateToken(token, "test_user"));
    }

    /** 验证 Token 中的用户名与传入用户名不一致时返回 false */
    @Test
    void validateToken_wrongUsername_returnsFalse() {
        String token = jwtUtils.generateToken(1001, "test_user", "patient");

        assertFalse(jwtUtils.validateToken(token, "other_user"));
    }

    /** 验证解析已过期 Token 时抛出 ExpiredJwtException */
    @Test
    void isTokenExpired_expiredToken_throwsExpiredJwtException() {
        String expiredToken = buildExpiredToken();

        assertThrows(io.jsonwebtoken.ExpiredJwtException.class,
                () -> jwtUtils.isTokenExpired(expiredToken));
    }

    /** 验证校验已过期 Token 时抛出 ExpiredJwtException */
    @Test
    void validateToken_expiredToken_throwsExpiredJwtException() {
        String expiredToken = buildExpiredToken();

        assertThrows(io.jsonwebtoken.ExpiredJwtException.class,
                () -> jwtUtils.validateToken(expiredToken, "test_user"));
    }

    private String buildExpiredToken() {
        byte[] keyBytes = TEST_SECRET.getBytes(StandardCharsets.UTF_8);
        return Jwts.builder()
                .setClaims(Map.of("userId", 1001, "role", "patient"))
                .setSubject("test_user")
                .setIssuedAt(new Date(System.currentTimeMillis() - 7200000))
                .setExpiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(Keys.hmacShaKeyFor(keyBytes), SignatureAlgorithm.HS256)
                .compact();
    }

    /** 验证被篡改的 Token 解析失败 */
    @Test
    void extractClaims_tamperedToken_throwsException() {
        String token = jwtUtils.generateToken(1001, "test_user", "patient");
        String tampered = token.substring(0, token.length() - 2) + "XX";

        assertThrows(Exception.class, () -> jwtUtils.extractClaims(tampered));
    }

    /** 验证使用错误密钥签名的 Token 无法被当前实例解析 */
    @Test
    void extractClaims_wrongSecret_throwsException() {
        JwtUtils otherUtils = new JwtUtils();
        ReflectionTestUtils.setField(otherUtils, "secretString", "AnotherSecretKeyForTestingOnly123456!");

        String token = otherUtils.generateToken(1001, "test_user", "patient");

        assertThrows(Exception.class, () -> jwtUtils.extractClaims(token));
    }

    /** 验证 Token 过期时间配置为 7200 秒（2 小时） */
    @Test
    void getExpirationTime_returnsSeconds() {
        assertEquals(7200L, jwtUtils.getExpirationTime());
    }
}
