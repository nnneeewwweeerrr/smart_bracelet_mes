# 智能手环MES系统 - 后端服务

> SpringBoot 2.7 + MyBatis-Plus + Spring Security + JWT + MySQL 8.0
>
> 覆盖34张表、40+ 接口，集成 AI 生产问答助手与 SN 全生命周期追溯

## 技术栈

- **语言**: Java 17
- **框架**: SpringBoot 2.7.15
- **ORM**: MyBatis-Plus 3.5.3
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8.0
- **构建工具**: Maven
- **AI 模块**: 知识图谱（内存模拟 Neo4j）+ RAG 文档检索 + 大模型适配器

## 项目结构

```
backend/
├── src/main/java/com/mes/
│   ├── SmartBraceletMesApplication.java  # 启动类
│   ├── annotation/                       # 自定义注解（@OperationLog 操作日志）
│   ├── aspect/                           # AOP 切面
│   │   └── OperationLogAspect.java       # 操作日志切面
│   ├── common/                           # 公共基类
│   │   ├── Result.java                   # 统一返回结果包装
│   │   └── PageResult.java               # 分页结果包装
│   ├── config/                           # 配置类
│   │   ├── SecurityConfig.java           # Spring Security 安全配置
│   │   ├── CorsConfig.java               # 跨域配置
│   │   ├── MybatisPlusConfig.java        # MyBatis-Plus 分页插件
│   │   ├── CustomUserDetailsService.java # 用户认证服务（含状态校验）
│   │   ├── Neo4jConfig.java              # 知识图谱配置（默认内存模拟）
│   │   ├── AiConfig.java                 # AI 模块配置
│   │   └── DataInitializer.java          # 启动初始化（用户/SOP文档预置）
│   ├── controller/                       # 控制层（40+ 控制器）
│   │   ├── AuthController.java           # 登录鉴权
│   │   ├── UserRegisterController.java   # 用户注册
│   │   ├── SystemController.java         # 系统管理
│   │   ├── DictController.java           # 数据字典
│   │   ├── OrderController.java          # 订单管理
│   │   ├── PurchaseController.java       # 采购管理
│   │   ├── SupplierController.java       # 供应商
│   │   ├── CustomerController.java       # 客户
│   │   ├── MaterialController.java       # 物料
│   │   ├── ProductController.java        # 产品
│   │   ├── EquipmentController.java      # 设备
│   │   ├── EquipmentMaintainController.java # 设备保养
│   │   ├── OperationLogController.java   # 操作日志
│   │   ├── WarehouseController.java      # 仓库
│   │   ├── StockController.java          # 库存
│   │   ├── StockInController.java        # 入库
│   │   ├── StockOutController.java       # 出库
│   │   ├── StockFlowController.java      # 库存流水
│   │   ├── ProductionPlanController.java # 生产计划
│   │   ├── WorkOrderController.java      # 工单
│   │   ├── ProductionReportController.java # 报工
│   │   ├── ProcessController.java        # 工艺
│   │   ├── SopController.java            # SOP 看板
│   │   ├── QualityTaskController.java    # 质检任务
│   │   ├── QualityResultController.java  # 质检结果
│   │   ├── ExceptionRecordController.java # 异常
│   │   ├── BigScreenController.java      # 车间大屏
│   │   ├── OrderTraceController.java     # 订单追溯
│   │   ├── SnTraceController.java        # SN 追溯
│   │   ├── MiniAppController.java        # 小程序专用
│   │   ├── DocumentController.java       # AI 文档管理
│   │   ├── KnowledgeGraphController.java # 知识图谱
│   │   └── AiChatController.java         # AI 问答主接口
│   ├── ai/                               # AI 模块
│   │   ├── AiModelAdapter.java           # 大模型适配器接口
│   │   ├── ChatMessage.java              # 对话消息结构
│   │   └── impl/
│   │       └── GenericAiAdapter.java     # 通用大模型实现
│   ├── kg/                               # 知识图谱模块
│   │   ├── entity/                       # 节点实体（工序/不良/原因/方案/物料/设备）
│   │   ├── repository/                   # 节点 Repository
│   │   └── service/                      # 知识图谱服务
│   ├── service/                          # 业务逻辑层接口
│   │   └── impl/                         # 业务逻辑层实现
│   ├── mapper/                           # 数据访问层（MyBatis-Plus Mapper）
│   ├── entity/                           # 实体类（34 张表）
│   ├── dto/                              # 数据传输对象
│   │   ├── LoginDTO.java
│   │   └── RegisterDTO.java
│   ├── vo/                               # 视图对象
│   │   ├── LoginVO.java
│   │   └── StockFlowVO.java
│   ├── filter/                           # 过滤器
│   │   └── JwtAuthenticationFilter.java  # JWT 鉴权
│   ├── exception/                        # 异常处理
│   │   ├── BusinessException.java
│   │   └── GlobalExceptionHandler.java
│   ├── utils/                            # 工具类
│   │   ├── JwtUtil.java                  # JWT 生成与解析
│   │   ├── SnowflakeIdGenerator.java     # 雪花 ID
│   │   ├── BarcodeUtil.java              # 单据号生成
│   │   ├── DateUtil.java                 # 日期工具
│   │   └── BCryptGenerator.java          # BCrypt 密码
│   └── generator/                        # 代码生成器
├── src/main/resources/
│   ├── application.yml                   # 应用配置
│   └── mapper/                           # MyBatis XML
├── src/test/                             # 测试
│   ├── ResultTest.java
│   └── UtilTest.java
└── pom.xml                               # Maven 依赖
```

## 启动方式

### 开发环境

```bash
cd backend
mvn spring-boot:run
# 访问地址: http://localhost:8080
# 启动时自动：
#   - 初始化数据库表（DataInitializer）
#   - 预置 SOP 文档至 AI 知识库
#   - 初始化 45 节点 46 关系的知识图谱
#   - 创建默认账号（admin/123456）
```

### 打包部署

```bash
mvn clean package
java -jar target/smart-bracelet-mes-1.0.0.jar
```

### 单元测试

```bash
mvn test
```

## 配置说明

修改 `src/main/resources/application.yml`：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_bracelet_mes?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: password

ai:
  knowledge-graph:
    enabled: false  # true 启用 Neo4j，false 使用内存模拟

jwt:
  secret: your-secret-key
  expire-hours: 24
```

## 模块说明

| 控制器 | 路径 | 说明 |
|--------|------|------|
| AuthController | /api/auth | 登录鉴权 |
| UserRegisterController | /api/user | 用户注册 |
| SystemController | /api/system | 用户/角色/权限/字典 |
| OrderController | /api/order | 客户订单 |
| PurchaseController | /api/purchase | 采购订单 |
| StockController / StockIn / StockOut | /api/stock-* | 库存/入库/出库 |
| StockFlowController | /api/stock-flow | 库存流水 |
| WorkOrderController | /api/work-order | 工单 |
| ProductionReportController | /api/production-report | 生产报工 |
| ProcessController / SopController | /api/process /api/sop | 工艺/SOP |
| QualityTask / QualityResult | /api/quality-* | 质检 |
| ExceptionRecordController | /api/exception | 异常 |
| EquipmentController / EquipmentMaintain | /api/equipment* | 设备 |
| BigScreenController | /api/big-screen | 车间大屏 |
| OrderTraceController | /api/trace | 订单追溯 |
| SnTraceController | /api/sn-trace | SN 追溯 |
| MiniAppController | /api/mini | 小程序专用 |
| DocumentController | /api/document | AI 文档管理 |
| KnowledgeGraphController | /api/kg | 知识图谱 |
| AiChatController | /api/ai | AI 问答主接口 |

## AI 助手接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/ai/chat | POST | 智能对话（支持上下文） |
| /api/ai/feedback | POST | 无用答案反馈 |
| /api/ai/stats | GET | 问答统计（趋势/TOP10/解决率） |
| /api/ai/history/recent | GET | 最近对话历史 |
| /api/ai/voice/recognize | POST | 语音识别（小程序端） |
| /api/document/upload | POST | 知识库文档上传 |
| /api/document/list | GET | 文档列表 |
| /api/kg/graph-data | GET | 知识图谱可视化数据 |
| /api/kg/multi-path/{defectName} | GET | 不良品多路径推理 |

## 知识图谱

- **节点类型**：工序(ProcessNode)、不良(DefectNode)、原因(CauseNode)、方案(SolutionNode)、物料(MaterialNode)、设备(EquipmentNode)
- **关系类型**：BELONGS_TO、CAUSES、LEADS_TO、USES、REQUIRES、DEPENDS_ON
- **演示数据**：45 节点 / 46 关系，覆盖 SMT 贴片、焊接组装、固件烧录、功能测试、外观检查、包装入库 6 道工序
- **核心查询**：多路径关联推理（如"虚焊→焊接温度偏低→返工方案"）、单工序物料清单、设备依赖关系

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 超级管理员 |
| order_clerk | 123456 | 订单员 |
| purchase_clerk | 123456 | 采购员 |
| warehouse_keeper | 123456 | 仓库管理员 |
| production_manager | 123456 | 生产主管 |
| quality_inspector | 123456 | 质检员 |
| worker001 | 123456 | 车间工人 |
