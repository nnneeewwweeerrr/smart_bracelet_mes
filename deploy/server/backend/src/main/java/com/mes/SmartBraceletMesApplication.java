package com.mes;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jRepositoriesAutoConfiguration;

@SpringBootApplication(exclude = {
    Neo4jAutoConfiguration.class,
    Neo4jDataAutoConfiguration.class,
    Neo4jRepositoriesAutoConfiguration.class
})
@MapperScan("com.mes.mapper")
public class SmartBraceletMesApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartBraceletMesApplication.class, args);
    }
}
