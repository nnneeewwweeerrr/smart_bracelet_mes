# 智能手环MES系统

> 智能手环生产制造执行系统(MES)，包含网页管理端与小程序现场端，覆盖订单、采购、仓储、生产、质检、异常全流程数字化管理

## 项目简介

本系统是一款面向智能手环制造企业的生产制造执行系统（MES），旨在实现生产全流程的数字化管理，提升生产效率和产品质量。

## 技术架构

- **后端**: SpringBoot 2.7.15 + MyBatis-Plus 3.5.3 + Spring Security + JWT + MySQL 8.0
- **前端管理端**: Vue3 + Vite 5 + Element Plus + Pinia + ECharts 5
- **小程序端**: UniApp
- **代码规范**: Lombok、雪花ID、单据号自动生成、AOP操作日志

## 功能模块

### 业务功能

| 模块 | 说明 | 状态 |
|------|------|------|
| 订单管理 | 客户订单的创建、审批、派工、状态跟踪 | ✅ 已完成 |
| 采购管理 | 采购计划、采购订单、供应商管理、到货登记 | ✅ 已完成 |
| 仓储管理 | 原材料入库、成品出库、库存查询、库存流水 | ✅ 已完成 |
| 基础档案 | 客户/供应商/物料/产品/设备档案管理 | ✅ 已完成 |
| 系统管理 | 用户管理、角色权限、数据字典、操作日志 | ✅ 已完成 |
| 生产管理 | 生产计划、工单管理、生产报工、工艺路线、SOP | ✅ 已完成 |
| 质量管理 | IQC来料检验、IPQC工序检验、OQC成品检验、检验报告 | ✅ 已完成 |
| 异常处理 | 异常上报、等级判定、处理闭环、批次追溯 | ✅ 已完成 |
| 设备管理 | 设备台账、维护保养、设备状态监控 | ✅ 已完成 |
| 小程序端 | 现场扫码报工、质检、异常上报 | ⏳ 待开发 |

### 可视化大屏（答辩演示）

| 大屏名称 | 说明 | 访问路径 | 状态 |
|----------|------|----------|------|
| 数字化车间大屏 | 产线实时运行状态：实时产量、产线良率、设备状态、在制分布、TOP不良 | `/big-screen` | ✅ 已完成 |
| 订单全链路追溯大屏 | 订单从接收到交付的完整生产链路追溯，7个核心环节可视化 | `/trace-screen` | ✅ 已完成 |

## 项目结构

```
SmartBraceletMES/
├── backend/                              # 【后端】SpringBoot 项目（Java 17 + SpringBoot 2.7.x）
│   ├── src/main/java/com/mes/
│   │   ├── SmartBraceletMesApplication.java   # 启动类，整个后端的入口
│   │   ├── annotation/                   # 自定义注解（如 @OperationLog 操作日志注解）
│   │   ├── aspect/                       # AOP 切面（如 OperationLogAspect 操作日志切面）
│   │   ├── common/                       # 公共基类
│   │   │   ├── Result.java               # 统一返回结果包装（code / message / data）
│   │   │   └── PageResult.java           # 分页结果包装
│   │   ├── config/                       # 配置类
│   │   │   ├── SecurityConfig.java       # Spring Security 安全配置（权限拦截规则）
│   │   │   ├── CorsConfig.java           # 跨域配置（前后端分离必配）
│   │   │   ├── MybatisPlusConfig.java    # MyBatis-Plus 配置（分页插件）
│   │   │   ├── CustomUserDetailsService.java  # 用户认证服务（从数据库查用户）
│   │   │   └── DataInitializer.java      # 启动时初始化默认密码
│   │   ├── controller/                   # 控制层（接收请求、返回响应）
│   │   │   ├── AuthController.java       # 登录鉴权接口（/api/auth）
│   │   │   ├── SystemController.java     # 系统管理：用户/角色/权限（/api/system）
│   │   │   ├── DictController.java       # 数据字典接口（/api/dict）
│   │   │   ├── OrderController.java      # 订单管理接口（/api/order）
│   │   │   ├── PurchaseController.java   # 采购管理接口（/api/purchase）
│   │   │   ├── SupplierController.java   # 供应商管理接口（/api/supplier）
│   │   │   ├── CustomerController.java   # 客户档案接口（/api/customer）
│   │   │   ├── MaterialController.java   # 物料档案接口（/api/material）
│   │   │   ├── ProductController.java    # 产品档案接口（/api/product）
│   │   │   ├── EquipmentController.java        # 设备档案接口（/api/equipment）
│   │   │   ├── EquipmentMaintainController.java # 设备保养接口（/api/equipment-maintain）
│   │   │   ├── OperationLogController.java      # 操作日志接口（/api/operation-log）
│   │   │   ├── WarehouseController.java         # 仓库管理接口（/api/warehouse）
│   │   │   ├── StockController.java      # 库存查询接口（/api/stock）
│   │   │   ├── StockInController.java    # 入库记录接口（/api/stock-in）
│   │   │   ├── StockOutController.java   # 出库记录接口（/api/stock-out）
│   │   │   ├── StockFlowController.java  # 库存流水接口（/api/stock-flow）
│   │   │   ├── ProductionPlanController.java   # 生产计划接口（/api/production-plan）
│   │   │   ├── WorkOrderController.java        # 工单管理接口（/api/work-order）
│   │   │   ├── ProductionReportController.java # 生产报工接口（/api/production-report）
│   │   │   ├── ProcessController.java          # 工艺管理接口（/api/process）
│   │   │   ├── QualityTaskController.java      # 质检任务接口（/api/quality-task）
│   │   │   ├── QualityResultController.java    # 质检结果接口（/api/quality-result）
│   │   │   ├── ExceptionRecordController.java  # 异常记录接口（/api/exception）
│   │   │   ├── BigScreenController.java        # 数字化车间大屏接口（/api/big-screen）
│   │   │   └── OrderTraceController.java       # 订单全链路追溯接口（/api/trace）
│   │   ├── service/                      # 业务逻辑层接口
│   │   │   └── impl/                     # 业务逻辑层实现
│   │   ├── mapper/                       # 数据访问层（MyBatis Mapper 接口）
│   │   ├── entity/                       # 实体类（与数据库表一一对应）
│   │   │   ├── SysUser.java              # 系统用户
│   │   │   ├── SysRole.java              # 角色
│   │   │   ├── SysPermission.java        # 权限
│   │   │   ├── SysDict.java              # 数据字典
│   │   │   ├── OperationLog.java         # 操作日志
│   │   │   ├── OrderCustomer.java        # 客户订单
│   │   │   ├── PurchaseOrder.java        # 采购单
│   │   │   ├── SupplierInfo.java         # 供应商
│   │   │   ├── CustomerInfo.java         # 客户
│   │   │   ├── MaterialInfo.java         # 物料
│   │   │   ├── ProductInfo.java          # 产品
│   │   │   ├── EquipmentInfo.java         # 设备
│   │   │   ├── EquipmentMaintain.java     # 设备保养记录
│   │   │   ├── WarehouseInfo.java         # 仓库
│   │   │   ├── StockInfo.java            # 库存
│   │   │   ├── StockIn.java              # 入库单
│   │   │   ├── StockOut.java             # 出库单
│   │   │   ├── ProductionPlan.java       # 生产计划
│   │   │   ├── WorkOrder.java            # 工单
│   │   │   ├── ProductionReport.java     # 报工记录
│   │   │   ├── ProcessInfo.java          # 工序
│   │   │   ├── QualityTask.java          # 质检任务
│   │   │   ├── QualityResult.java        # 质检结果
│   │   │   └── ExceptionRecord.java      # 异常记录
│   │   ├── dto/                          # 数据传输对象（请求参数封装）
│   │   ├── vo/                           # 视图对象（返回前端的自定义结构）
│   │   ├── filter/                       # 过滤器（如 JwtAuthenticationFilter）
│   │   ├── exception/                    # 异常处理
│   │   │   ├── BusinessException.java    # 业务异常类
│   │   │   └── GlobalExceptionHandler.java  # 全局异常处理器
│   │   ├── utils/                        # 工具类
│   │   │   ├── JwtUtil.java              # JWT Token 生成与解析
│   │   │   ├── SnowflakeIdGenerator.java # 雪花 ID 生成器
│   │   │   ├── BarcodeUtil.java          # 单据号生成（订单/采购/入库等）
│   │   │   ├── DateUtil.java             # 日期工具
│   │   │   └── BCryptGenerator.java      # BCrypt 密码生成
│   │   └── generator/                    # 代码生成器（MyBatis-Plus Generator）
│   ├── src/main/resources/
│   │   ├── application.yml               # 应用配置文件（数据库、端口、JWT 密钥等）
│   │   └── mapper/                       # MyBatis XML 映射文件（目前用 MP 注解，可选）
│   └── pom.xml                           # Maven 依赖管理
│
├── web-admin/                            # 【前端】网页管理端（Vue3 + Vite + Element Plus）
│   ├── src/
│   │   ├── main.js                       # 入口文件（挂载 Vue、注册插件）
│   │   ├── App.vue                       # 根组件
│   │   ├── api/                          # 接口请求封装（每个模块一个文件）
│   │   │   ├── request.js                # Axios 实例封装（拦截器、统一错误处理）
│   │   │   ├── auth.js                   # 登录 / 用户信息
│   │   │   ├── order.js                  # 订单管理
│   │   │   ├── purchase.js               # 采购管理
│   │   │   ├── supplier.js               # 供应商
│   │   │   ├── customer.js               # 客户档案
│   │   │   ├── material.js               # 物料档案
│   │   │   ├── product.js                # 产品档案
│   │   │   ├── equipment.js              # 设备档案
│   │   │   ├── warehouse.js              # 仓库
│   │   │   ├── stock.js                  # 库存查询
│   │   │   ├── stockIn.js                # 入库记录
│   │   │   ├── stockOut.js               # 出库记录
│   │   │   ├── stockFlow.js              # 库存流水
│   │   │   ├── productionPlan.js         # 生产计划
│   │   │   ├── workOrder.js              # 工单管理
│   │   │   ├── productionReport.js       # 生产报工
│   │   │   ├── process.js                # 工艺管理
│   │   │   ├── qualityTask.js            # 质检任务
│   │   │   ├── qualityResult.js          # 质检结果
│   │   │   ├── exception.js              # 异常记录
│   │   │   ├── equipmentMaintain.js      # 设备保养
│   │   │   ├── operationLog.js           # 操作日志
│   │   │   ├── dict.js                   # 数据字典
│   │   │   ├── bigScreen.js              # 数字化车间大屏
│   │   │   └── trace.js                  # 订单全链路追溯
│   │   ├── router/                       # 路由配置
│   │   │   └── index.js                  # 路由表 + 路由守卫（未登录跳登录页）
│   │   ├── stores/                       # Pinia 状态管理
│   │   │   └── user.js                   # 用户信息 & Token 存储
│   │   ├── components/                   # 通用组件
│   │   │   ├── BaseTable.vue             # 通用表格（分页、加载状态）
│   │   │   ├── SearchForm.vue            # 通用搜索表单
│   │   │   └── DictSelect.vue            # 数据字典下拉选择组件
│   │   └── views/                        # 页面视图
│   │       ├── login/                    # 登录页
│   │       ├── layout/                   # 后台整体布局（侧边栏 + 顶部 + 内容区）
│   │       ├── dashboard/                # 首页 / 工作台
│   │       ├── big-screen/               # 数字化车间大屏（全屏，独立路由）
│   │       ├── trace-screen/             # 订单全链路追溯大屏（全屏，独立路由）
│   │       ├── order/                    # 订单管理页
│   │       ├── purchase/                 # 采购管理页
│   │       ├── supplier/                 # 供应商管理页
│   │       ├── customer/                 # 客户档案页
│   │       ├── material/                 # 物料档案页（含扫码查物料）
│   │       ├── product/                  # 产品档案页
│   │       ├── equipment/                # 设备档案页
│   │       ├── equipment-monitor/         # 设备状态监控仪表盘
│   │       ├── stock/                     # 库存查询页
│   │       ├── stock-in/                 # 入库记录页
│   │       ├── stock-out/                # 出库记录页
│   │       ├── stock-flow/               # 库存流水页
│   │       ├── work-order/               # 生产工单管理页
│   │       ├── production-progress/      # 生产进度监控页
│   │       ├── process-config/           # 工艺配置页
│   │       ├── quality-record/           # 质检记录页
│   │       ├── exception-workbench/      # 异常处理工作台
│   │       └── operation-log/            # 操作日志查看页
│   ├── public/                           # 静态资源
│   ├── index.html                        # HTML 模板
│   ├── vite.config.js                    # Vite 配置（代理 /api → 后端 8080）
│   └── package.json                      # 前端依赖管理
│
├── mini-app/                             # 【前端】小程序端（uni-app）—— 待开发
│
├── docs/                                 # 【文档】项目全生命周期文档
│   ├── 01-需求文档/                      # 需求规格说明书
│   ├── 02-设计文档/                      # 详细设计 / 数据库设计 / 接口设计
│   ├── 03-部署文档/                      # 部署手册
│   ├── 04-用户文档/                      # 用户操作手册
│   └── 05-项目管理/                      # 开发排期 / 团队开发规范
│
├── sql/                                  # 【数据库】脚本
│   ├── schema.sql                        # 建表脚本（25 张业务表）
│   ├── init_data.sql                     # 初始化数据（管理员、角色、字典、基础数据）
│   ├── sample_data.sql                   # 业务示例数据（每表10条以上，用于演示）
│   └── supplement_data.sql               # 补充数据（补全数据不足的表）
│
├── .gitignore                            # Git 忽略配置
├── LICENSE                               # MIT 开源协议
└── README.md                             # 项目说明文档
```

## 快速开始

### 环境要求

- **JDK**: 17+
- **Node.js**: 16+
- **MySQL**: 8.0+
- **Maven**: 3.6+

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 超级管理员 |
| order_clerk | 123456 | 订单员 |
| purchase_clerk | 123456 | 采购员 |
| warehouse_keeper | 123456 | 仓库管理员 |
| production_manager | 123456 | 生产主管 |
| quality_inspector | 123456 | 质检员 |

### 数据库初始化

```bash
# 1. 创建数据库
mysql -u root -p < sql/schema.sql

# 2. 导入初始数据（管理员、角色、字典等）
mysql -u root -p < sql/init_data.sql

# 3. （可选）导入业务示例数据，用于演示和大屏展示
mysql -u root -p smart_bracelet_mes -e "source sql/sample_data.sql"
mysql -u root -p smart_bracelet_mes -e "source sql/supplement_data.sql"
```

### 后端启动

```bash
cd backend
mvn spring-boot:run
# 访问地址: http://localhost:8080
```

### 前端管理端启动

```bash
cd web-admin
npm install
npm run dev
# 访问地址: http://localhost:5173
# 数字化车间大屏: http://localhost:5173/big-screen
# 订单全链路追溯大屏: http://localhost:5173/trace-screen
```

### 小程序端启动

```bash
cd mini-app
npm install
npm run dev:h5
```

## 文档目录

- [需求规格说明书](./docs/01-需求文档/需求规格说明书.md)
- [详细设计说明书](./docs/02-设计文档/详细设计说明书.md)
- [数据库设计说明书](./docs/02-设计文档/数据库设计说明书.md)
- [接口设计说明书](./docs/02-设计文档/接口设计说明书.md)
- [部署手册](./docs/03-部署文档/部署手册.md)
- [用户操作手册](./docs/04-用户文档/用户操作手册.md)
- [开发排期与分工](./docs/05-项目管理/开发排期与分工.md)

## 开源协议

MIT License