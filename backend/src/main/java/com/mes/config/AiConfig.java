package com.mes.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {

    private String apiKey;

    private String modelName;

    private String apiUrl;

    private Integer timeout = 30000;

    private Integer maxTokens = 2000;

    private Double temperature = 0.7;
}
