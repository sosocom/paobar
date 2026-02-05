package com.sosocom;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 吉他谱管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.sosocom.mapper")
@EnableAsync  // 启用异步支持
public class PaoBarApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaoBarApplication.class, args);
        System.out.println("=================================");
        System.out.println("吉他谱管理系统启动成功！");
        System.out.println("API地址: http://localhost:8080");
        System.out.println("=================================");
    }
}
