package com.guitar;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 吉他谱管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.guitar.mapper")
public class GuitarTabApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuitarTabApplication.class, args);
        System.out.println("=================================");
        System.out.println("吉他谱管理系统启动成功！");
        System.out.println("API地址: http://localhost:8080");
        System.out.println("=================================");
    }
}
