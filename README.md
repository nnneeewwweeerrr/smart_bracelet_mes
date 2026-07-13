# 智能手环MES系统

> 面向智能手环制造企业的生产制造执行系统（MES），包含**Web管理端**与**小程序现场端**，覆盖订单、采购、仓储、生产、质检、设备、SN全生命周期追溯、AI生产助手等全流程数字化管理。

## 项目简介

本系统面向智能手环制造企业，覆盖**销售订单 → 采购 → 仓储 → 生产 → 质检 → 出货**完整价值链，并集成**SN全生命周期追溯**与**AI生产问答助手**两大亮点功能，适合作为专业实训答辩演示项目。

## 技术架构

- **后端**: SpringBoot 2.7.15 + MyBatis-Plus 3.5.3 + Spring Security + JWT + MySQL 8.0
- **前端管理端**: Vue3 + Vite 5 + Element Plus + Pinia + ECharts 5
- **小程序端**: UniApp + Vue3 Composition API + uView Plus
- **AI模块**: 内存模拟知识图谱（ConcurrentHashMap）+ RAG双路检索 + 大模型适配器架构
- **通用规范**: Lombok、雪花ID、单据号自动生成、AOP操作日志

## 项目亮点

| 亮点 | 说明 |
|------|------|
| 🚀 **AI生产问答助手** | 知识图谱 + RAG文档检索 + 业务数据问答，Web/小程序全端覆盖 |
| 🔍 **SN全生命周期追溯** | 一片一码，向上追物料批次、向下追客户订单，支持正反向追溯 |
| 📊 **车间数据大屏** | 数字化车间大屏 + 订单全链路追溯大屏，深色科技风格可视化 |
| 🛡️ **关键工序防错** | 工艺参数阈值 + 物料BOM校验 + SOP电子作业指导书 |
| 📱 **车间现场小程序** | 扫码报工/扫码追溯/异常上报/AI助手，覆盖车间现场全部场景 |

## 功能模块

### 一、业务功能（11大模块）

| 模块 | 说明 | 状态 |
|------|------|------|
| 订单管理 | 客户订单的创建、审批、派工、状态跟踪 | ✅ 已完成 |
| 采购管理 | 采购计划、采购订单、供应商管理、到货登记 | ✅ 已完成 |
| 仓储管理 | 原材料入库、成品出库、库存查询、库存流水、库存盘点、库存预警 | ✅ 已完成 |
| 基础档案 | 客户/供应商/物料/产品/设备档案管理 | ✅ 已完成 |
| 系统管理 | 用户管理、角色权限、数据字典、操作日志 | ✅ 已完成 |
| 生产管理 | 生产计划、工单管理、生产报工、工艺路线、SOP电子看板 | ✅ 已完成 |
| 质量管理 | IQC来料检验、IPQC工序检验、OQC成品检验、检验报告 | ✅ 已完成 |
| 异常处理 | 异常上报、等级判定、处理闭环、批次追溯 | ✅ 已完成 |
| 设备管理 | 设备台账、维护保养、设备状态监控、故障记录 | ✅ 已完成 |
| SN追溯 | 产品SN唯一码全生命周期正向/反向追溯 | ✅ 已完成 |
| 现场小程序 | 扫码报工/质检/异常上报/AI助手 | ✅ 已完成 |

### 二、可视化大屏（答辩演示）

| 大屏名称 | 说明 | 访问路径 | 状态 |
|----------|------|----------|------|
| 数字化车间大屏 | 产线实时运行状态：实时产量、产线良率、设备状态、在制分布、TOP不良 | `/big-screen` | ✅ 已完成 |
| 订单全链路追溯大屏 | 订单从接收到交付的完整生产链路追溯，7个核心环节可视化 | `/trace-screen` | ✅ 已完成 |

### 三、AI生产问答助手（创新亮点）

| 子模块 | 说明 | 状态 |
|--------|------|------|
| 智能问答引擎 | 意图识别 + 业务数据查询 + RAG双路检索 + 大模型对话 | ✅ 已完成 |
| 知识图谱模块 | 45节点46关系，覆盖工序/不良/原因/方案/物料/设备，支持多路径推理 | ✅ 已完成 |
| 知识库管理 | SOP文档上传/语义切片/关键词提取/相似度检索 | ✅ 已完成 |
| 知识图谱可视化 | ECharts力导向图渲染，点击节点查看详情 | ✅ 已完成 |
| 问答统计 | 提问趋势、高频问题TOP10、解决率 | ✅ 已完成 |
| 业务页面问AI | 工单/工艺/异常页面一键带入上下文提问 | ✅ 已完成 |
| 小程序AI助手 | 文字/语音输入、语音播报、扫码联动、引用来源 | ✅ 已完成 |

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
│   │   │   ├── Neo4jConfig.java          # Neo4j 知识图谱配置（默认内存模拟）
│   │   │   ├── AiConfig.java             # AI 模块配置
│   │   │   └── DataInitializer.java      # 启动时初始化默认数据 + SOP文档预置
│   │   ├── controller/                   # 控制层（接收请求、返回响应）
│   │   │   ├── AuthController.java       # 登录鉴权接口（/api/auth）
│   │   │   ├── UserRegisterController.java  # 用户注册接口（/api/user）
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
│   │   │   ├── SopController.java              # SOP电子看板接口（/api/sop）
│   │   │   ├── QualityTaskController.java      # 质检任务接口（/api/quality-task）
│   │   │   ├── QualityResultController.java    # 质检结果接口（/api/quality-result）
│   │   │   ├── ExceptionRecordController.java  # 异常记录接口（/api/exception）
│   │   │   ├── BigScreenController.java        # 数字化车间大屏接口（/api/big-screen）
│   │   │   ├── OrderTraceController.java       # 订单全链路追溯接口（/api/trace）
│   │   │   ├── SnTraceController.java          # SN追溯接口（/api/sn-trace）
│   │   │   ├── MiniAppController.java          # 小程序专用接口（/api/mini）
│   │   │   ├── DocumentController.java         # AI文档管理（/api/document）
│   │   │   ├── KnowledgeGraphController.java   # 知识图谱（/api/kg）
│   │   │   └── AiChatController.java           # AI问答主接口（/api/ai）
│   │   ├── ai/                           # AI 模块
│   │   │   ├── AiModelAdapter.java       # AI 大模型适配器接口
│   │   │   ├── ChatMessage.java          # 对话消息结构
│   │   │   └── impl/
│   │   │       └── GenericAiAdapter.java # 通用大模型实现（关键词匹配+Prompt工程）
│   │   ├── kg/                           # 知识图谱模块
│   │   │   ├── entity/                   # 图谱节点实体（工序/不良/原因/方案/物料/设备）
│   │   │   ├── repository/               # 节点 Repository
│   │   │   └── service/                  # 知识图谱服务（多路径推理+图数据接口）
│   │   ├── service/                      # 业务逻辑层接口
│   │   │   └── impl/                     # 业务逻辑层实现
│   │   ├── mapper/                       # 数据访问层（MyBatis-Plus Mapper）
│   │   ├── entity/                       # 实体类（与数据库表一一对应，34张表）
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
│   │   │   ├── ProcessSop.java           # SOP电子作业指导书
│   │   │   ├── ProcessParamStandard.java # 工序工艺参数标准
│   │   │   ├── ProcessMaterialBom.java   # 工序物料BOM
│   │   │   ├── QualityTask.java          # 质检任务
│   │   │   ├── QualityResult.java        # 质检结果
│   │   │   ├── ExceptionRecord.java      # 异常记录
│   │   │   ├── ProductSn.java            # 产品SN唯一码
│   │   │   ├── SnMaterialTrace.java      # SN物料追溯
│   │   │   ├── SnProcessTrace.java       # SN生产过程追溯
│   │   │   ├── SnQualityTrace.java       # SN质检追溯
│   │   │   ├── AiChatHistory.java        # AI对话历史
│   │   │   ├── AiDocument.java           # AI文档
│   │   │   └── AiDocumentChunk.java      # AI文档切片
│   │   ├── dto/                          # 数据传输对象（请求参数封装）
│   │   │   ├── LoginDTO.java
│   │   │   └── RegisterDTO.java
│   │   ├── vo/                           # 视图对象（返回前端的自定义结构）
│   │   │   ├── LoginVO.java
│   │   │   └── StockFlowVO.java
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
│   │   ├── api/                          # 接口请求封装（30+ 个 API 模块）
│   │   │   ├── request.js                # Axios 实例封装（拦截器、统一错误处理）
│   │   │   ├── auth.js                   # 登录 / 注册
│   │   │   ├── ai.js                     # AI 问答 / 统计
│   │   │   ├── knowledge.js              # 知识图谱 / 知识库
│   │   │   ├── order.js                  # 订单管理
│   │   │   ├── purchase.js               # 采购管理
│   │   │   ├── supplier.js               # 供应商
│   │   │   ├── customer.js               # 客户档案
│   │   │   ├── material.js               # 物料档案
│   │   │   ├── product.js                # 产品档案
│   │   │   ├── equipment.js              # 设备档案
│   │   │   ├── equipmentMaintain.js      # 设备保养
│   │   │   ├── warehouse.js              # 仓库
│   │   │   ├── stock.js                  # 库存查询
│   │   │   ├── stockIn.js                # 入库记录
│   │   │   ├── stockOut.js               # 出库记录
│   │   │   ├── stockFlow.js              # 库存流水
│   │   │   ├── productionPlan.js         # 生产计划
│   │   │   ├── workOrder.js              # 工单管理
│   │   │   ├── productionReport.js       # 生产报工
│   │   │   ├── process.js                # 工艺管理
│   │   │   ├── sop.js                    # SOP 电子看板
│   │   │   ├── qualityTask.js            # 质检任务
│   │   │   ├── qualityResult.js          # 质检结果
│   │   │   ├── exception.js              # 异常记录
│   │   │   ├── operationLog.js           # 操作日志
│   │   │   ├── dict.js                   # 数据字典
│   │   │   ├── bigScreen.js              # 数字化车间大屏
│   │   │   ├── trace.js                  # 订单全链路追溯
│   │   │   └── snTrace.js                # SN 追溯
│   │   ├── router/                       # 路由配置
│   │   │   └── index.js                  # 路由表 + 路由守卫
│   │   ├── stores/                       # Pinia 状态管理
│   │   │   └── user.js                   # 用户信息 & Token 存储
│   │   ├── components/                   # 通用组件
│   │   │   ├── BaseTable.vue             # 通用表格（分页、加载状态）
│   │   │   ├── SearchForm.vue            # 通用搜索表单
│   │   │   ├── DictSelect.vue            # 数据字典下拉选择组件
│   │   │   └── AiAssistant.vue           # AI 助手悬浮组件（全页面可唤起）
│   │   ├── config/
│   │   │   └── roleMenu.js               # 角色菜单配置
│   │   └── views/                        # 页面视图（40+ 页面）
│   │       ├── login/                    # 登录页
│   │       ├── layout/                   # 后台整体布局（侧边栏 + 顶部 + 内容区）
│   │       ├── dashboard/                # 首页 / 工作台
│   │       ├── big-screen/               # 数字化车间大屏（全屏）
│   │       ├── trace-screen/             # 订单全链路追溯大屏（全屏）
│   │       ├── ai-stats/                 # AI 问答统计页面
│   │       ├── knowledge-base/           # AI 知识库管理
│   │       ├── knowledge-graph/          # 知识图谱可视化
│   │       ├── order/ order-statistics/  # 订单管理 / 订单统计
│   │       ├── purchase/ purchase-statistics/  # 采购管理 / 采购统计
│   │       ├── supplier/                 # 供应商管理
│   │       ├── customer/                 # 客户档案
│   │       ├── material/                 # 物料档案
│   │       ├── product/                  # 产品档案
│   │       ├── equipment/ equipment-monitor/ equipment-fault/ equipment-maintain/  # 设备管理
│   │       ├── stock/ stock-in/ stock-out/ stock-flow/ stock-take/ stock-warning/ stock-statistics/  # 仓储
│   │       ├── work-order/ production/ production-plan/ production-progress/ production-statistics/  # 生产
│   │       ├── process-config/ sop-board/  # 工艺 / SOP
│   │       ├── quality/ quality-record/ quality-statistics/ unqualified/  # 质量
│   │       ├── exception-workbench/      # 异常处理工作台
│   │       ├── sn-trace/                 # SN 全生命周期追溯
│   │       ├── operation-log/            # 操作日志
│   │       └── system/                   # 系统管理
│   ├── public/                           # 静态资源
│   ├── index.html                        # HTML 模板
│   ├── vite.config.js                    # Vite 配置（代理 /api → 后端 8080）
│   └── package.json                      # 前端依赖管理
│
├── mini-app/                             # 【前端】小程序端（UniApp + Vue3）
│   ├── src/
│   │   ├── main.js                       # 入口文件
│   │   ├── App.vue                       # 根组件
│   │   ├── manifest.json                 # 小程序配置
│   │   ├── pages.json                    # 页面路由配置（30+ 页面）
│   │   ├── uni.scss                      # 全局样式变量
│   │   ├── api/                          # 接口请求封装
│   │   │   ├── request.js                # 请求封装（含 Mock 兜底）
│   │   │   ├── auth.js                   # 登录认证
│   │   │   ├── user.js                   # 用户/任务
│   │   │   ├── ai.js                     # AI 助手接口
│   │   │   ├── production.js             # 生产/工单
│   │   │   ├── quality.js                # 质检
│   │   │   ├── exception.js              # 异常
│   │   │   └── stock.js                  # 库存
│   │   ├── components/                   # 通用组件
│   │   │   └── DynamicBackground.vue     # 动态深色背景
│   │   ├── pages/                        # 页面（30+ 页面）
│   │   │   ├── login/                    # 登录
│   │   │   ├── workbench/                # 工作台（含 AI 助手入口）
│   │   │   ├── ai/                       # AI 助手对话主页
│   │   │   ├── scan/                     # 扫码中心
│   │   │   ├── stock/                    # 仓储（入库/出库/盘点/记录/物料）
│   │   │   ├── production/               # 生产（工单/报工/SOP/追溯/SN追溯/维修/详情）
│   │   │   ├── quality/                  # 质量（首页/IQC/任务/测试看板/测试录入）
│   │   │   ├── exception/                # 异常（首页/上报/待处理）
│   │   │   └── mine/                     # 我的（首页/待办/操作记录/通知/设置/个人信息）
│   │   ├── utils/                        # 工具类
│   │   │   ├── auth.js                   # 登录态管理
│   │   │   ├── request.js                # 请求封装
│   │   │   └── scan.js                   # 扫码工具
│   │   └── static/                       # 静态资源（tab图标/logo等）
│   ├── tests/                            # 测试文件
│   ├── index.html                        # H5 入口
│   ├── vite.config.js                    # Vite 配置
│   └── package.json                      # 小程序依赖管理
│
├── docs/                                 # 【文档】项目全生命周期文档
│   ├── 01-需求文档/                      # 需求规格说明书
│   ├── 02-设计文档/                      # 详细设计 / 数据库设计 / 接口设计
│   ├── 03-部署文档/                      # 部署手册
│   ├── 04-用户文档/                      # 用户操作手册 / AI生产助手演示操作说明
│   └── 05-项目管理/                      # 开发排期 / 团队开发规范
│
├── sql/                                  # 【数据库】脚本
│   ├── schema.sql                        # 主建表脚本（33 张业务表）
│   ├── init_data.sql                     # 初始化数据（管理员、角色、字典、基础数据）
│   ├── sample_data.sql                   # 业务示例数据（每表10条以上，用于演示）
│   ├── supplement_data.sql               # 补充数据
│   ├── sop_tables.sql                    # SOP 工艺模块表
│   ├── sn_trace_tables.sql               # SN 追溯模块表（含示例 SN 码）
│   └── sn_trace_supplement.sql           # SN 追溯补充数据
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
| worker001 | 123456 | 车间工人 |

### 数据库初始化

```bash
# 1. 创建数据库与主表
mysql -u root -p < sql/schema.sql

# 2. 导入初始数据（管理员、角色、字典、SOP/SN扩展表等）
mysql -u root -p < sql/init_data.sql

# 3. （可选）导入业务示例数据，用于演示和大屏展示
mysql -u root -p smart_bracelet_mes -e "source sql/sample_data.sql"
mysql -u root -p smart_bracelet_mes -e "source sql/supplement_data.sql"

# 4. 导入 SOP 工艺模块
mysql -u root -p smart_bracelet_mes -e "source sql/sop_tables.sql"

# 5. 导入 SN 追溯模块
mysql -u root -p smart_bracelet_mes -e "source sql/sn_trace_tables.sql"
mysql -u root -p smart_bracelet_mes -e "source sql/sn_trace_supplement.sql"
```

### 后端启动

```bash
cd backend
mvn spring-boot:run
# 访问地址: http://localhost:8080
# AI 知识库 SOP 文档将在启动时自动预置
```

### 前端管理端启动

```bash
cd web-admin
npm install
npm run dev
# 访问地址: http://localhost:5173
# 数字化车间大屏: http://localhost:5173/big-screen
# 订单全链路追溯大屏: http://localhost:5173/trace-screen
# AI 知识图谱可视化: http://localhost:5173/knowledge-graph
# AI 知识库管理: http://localhost:5173/knowledge-base
# AI 问答统计: http://localhost:5173/ai-stats
```

### 小程序端启动

```bash
cd mini-app
npm install

# H5 端开发（推荐在浏览器查看）
npm run dev:h5
# 访问地址: http://localhost:5175

# 微信小程序端开发（需 HBuilderX）
npm run dev:mp-weixin
```

## 演示场景

### 场景一：Web 端 AI 智能问答
1. 打开 http://localhost:5173，使用 admin/123456 登录
2. 任意页面点击右下角 AI 助手悬浮按钮
3. 输入业务问题（如"焊接工序温度"、"工单进度"、"库存情况"）
4. 查看 AI 回答 + 引用来源

### 场景二：知识图谱可视化
1. 打开 http://localhost:5173/knowledge-graph
2. 查看 45 节点 / 46 关系的知识网络
3. 点击节点查看详情、关联关系

### 场景三：车间数据大屏
1. 打开 http://localhost:5173/big-screen
2. 查看实时产量、产线良率、设备状态
3. 打开 http://localhost:5173/trace-screen 查看订单全链路追溯

### 场景四：小程序扫码 + AI 提问
1. 打开 http://localhost:5175，登录后进入工作台
2. 点击"AI 生产助手"卡片进入对话页
3. 扫描工单码 / SN 码 / 物料码，自动带入场景提问
4. 体验语音输入、语音播报

## 文档目录

- [需求规格说明书](./docs/01-需求文档/需求规格说明书.md)
- [详细设计说明书](./docs/02-设计文档/详细设计说明书.md)
- [数据库设计说明书](./docs/02-设计文档/数据库设计说明书.md)
- [接口设计说明书](./docs/02-设计文档/接口设计说明书.md)
- [部署手册](./docs/03-部署文档/部署手册.md)
- [用户操作手册](./docs/04-用户文档/用户操作手册.md)
- [AI生产助手演示操作说明](./docs/04-用户文档/AI生产助手演示操作说明.md)
- [开发排期与分工](./docs/05-项目管理/开发排期与分工.md)
- [团队开发规范](./docs/05-项目管理/团队开发规范.md)

## 开源协议

MIT License
