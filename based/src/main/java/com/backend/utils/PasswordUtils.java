package com.backend.utils;

import cn.hutool.crypto.digest.BCrypt;

public class PasswordUtils {

    private static final int SALT_LENGTH = 10;

    public static String encrypt(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(SALT_LENGTH));
    }

    public static boolean verify(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}