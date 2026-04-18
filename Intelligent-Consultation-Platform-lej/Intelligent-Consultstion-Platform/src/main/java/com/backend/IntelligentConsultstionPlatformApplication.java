package com.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.backend.mapper")
public class IntelligentConsultstionPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelligentConsultstionPlatformApplication.class, args);
    }

}
