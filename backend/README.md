# 智能手环MES系统 - 后端服务

## 技术栈

- **语言**: Java 11
- **框架**: SpringBoot 2.7.x
- **ORM**: MyBatis
- **数据库**: MySQL 8.0+
- **构建工具**: Maven

## 项目结构

```
backend/
├── src/main/java/com/mes/
│   ├── controller/     # 控制层
│   ├── service/        # 业务层接口
│   │   └── impl/       # 业务层实现类
│   ├── mapper/         # 数据访问层
│   ├── entity/         # 数据库实体类
│   ├── dto/            # 请求入参对象
│   ├── vo/             # 返回视图对象
│   ├── config/         # 配置类
│   ├── common/         # 通用类
│   └── utils/          # 工具类
├── src/main/resources/
│   ├── application.yml # 应用配置
│   └── mapper/         # MyBatis映射文件
├── src/test/           # 测试目录
└── pom.xml             # Maven配置
```

## 启动方式

### 开发环境

```bash
cd backend
mvn spring-boot:run
```

### 打包部署

```bash
mvn clean package
java -jar target/smart-bracelet-mes-1.0.0.jar
```

## 配置说明

修改 `src/main/resources/application.yml` 配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_bracelet_mes?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: password
```

## 模块说明

| 模块 | 说明 |
|------|------|
| OrderController | 订单管理接口 |
| PurchaseController | 采购管理接口 |
| StockController | 仓储管理接口 |
| ProductionController | 生产管理接口 |
| QualityController | 质量管理接口 |
| EquipmentController | 设备管理接口 |
| SystemController | 系统管理接口 |