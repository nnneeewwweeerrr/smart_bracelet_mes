package com.mes;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mes.mapper")
public class SmartBraceletMesApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartBraceletMesApplication.class, args);
    }
}
