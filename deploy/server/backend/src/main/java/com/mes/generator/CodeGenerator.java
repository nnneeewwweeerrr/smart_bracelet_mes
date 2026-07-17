package com.mes.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

public class CodeGenerator {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/smart_bracelet_mes?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false";
        String username = "root";
        String password = "root";
        String outputDir = System.getProperty("user.dir") + "/src/main/java";
        String xmlOutputDir = System.getProperty("user.dir") + "/src/main/resources/mapper";

        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> builder
                        .author("MES Team")
                        .outputDir(outputDir)
                        .dateType(DateType.ONLY_DATE)
                        .disableOpenDir())
                .packageConfig(builder -> builder
                        .parent("com.mes")
                        .entity("entity")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .controller("controller")
                        .pathInfo(Collections.singletonMap(OutputFile.xml, xmlOutputDir)))
                .strategyConfig(builder -> builder
                        .addInclude(
                                "sys_user", "sys_role", "sys_permission", "sys_dict",
                                "customer_info", "product_info", "order_customer",
                                "supplier_info", "material_info", "purchase_order",
                                "purchase_order_item", "warehouse_info", "stock_info",
                                "stock_record", "bom_info", "bom_item",
                                "production_plan", "production_task", "production_report",
                                "quality_inspection", "equipment_info", "equipment_maintenance",
                                "exception_record"
                        )
                        .entityBuilder()
                        .enableLombok()
                        .enableTableFieldAnnotation()
                        .controllerBuilder()
                        .enableRestStyle()
                        .formatFileName("%sController")
                        .serviceBuilder()
                        .formatServiceFileName("%sService")
                        .formatServiceImplFileName("%sServiceImpl")
                        .mapperBuilder()
                        .enableMapperAnnotation()
                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
