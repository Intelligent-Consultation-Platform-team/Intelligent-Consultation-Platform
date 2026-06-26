package com.backend.utils;

import cn.hutool.crypto.digest.BCrypt;

public class GeneratePassword {
    public static void main(String[] args) {
        String password = "123456";
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(10));
        System.out.println("Hashed password: " + hashed);
        System.out.println("Verify: " + BCrypt.checkpw(password, hashed));
    }
}
