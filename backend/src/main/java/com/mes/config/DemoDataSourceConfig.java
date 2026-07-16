package com.mes.config;

import org.springframework.context.annotation.Configuration;

/**
 * 演示数据库配置
 * 演示表通过 @TableName("mes_demo.表名") 跨库访问，无需独立数据源
 */
@Configuration
public class DemoDataSourceConfig {
}
