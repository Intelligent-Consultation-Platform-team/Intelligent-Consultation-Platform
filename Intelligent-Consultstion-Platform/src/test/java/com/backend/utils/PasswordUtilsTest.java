package com.backend.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码工具类单元测试。
 * <p>
 * 测试功能：用户注册/登录中的 BCrypt 加密与校验。
 * 对应生产类：{@link PasswordUtils}
 */
class PasswordUtilsTest {

    /** 验证同一明文密码两次加密结果不同（BCrypt 随机盐） */
    @Test
    void encrypt_samePassword_producesDifferentHashes() {
        String hash1 = PasswordUtils.encrypt("123456");
        String hash2 = PasswordUtils.encrypt("123456");

        assertNotEquals(hash1, hash2);
    }

    /** 验证正确密码能通过校验 */
    @Test
    void verify_correctPassword_returnsTrue() {
        String hash = PasswordUtils.encrypt("123456");

        assertTrue(PasswordUtils.verify("123456", hash));
    }

    /** 验证错误密码校验失败 */
    @Test
    void verify_wrongPassword_returnsFalse() {
        String hash = PasswordUtils.encrypt("123456");

        assertFalse(PasswordUtils.verify("wrong", hash));
    }

    /** 验证空密码校验失败 */
    @Test
    void verify_emptyPassword_returnsFalse() {
        String hash = PasswordUtils.encrypt("123456");

        assertFalse(PasswordUtils.verify("", hash));
    }
}
