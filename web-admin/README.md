# 智能手环MES系统 - 网页管理端

> Vue 3 + Vite 5 + Element Plus + Pinia + ECharts 5
>
> 40+ 页面，深色科技风格，覆盖生产管理全场景

## 技术栈

- **语言**: JavaScript / Vue 3 SFC
- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite 5
- **UI 组件**: Element Plus
- **状态管理**: Pinia
- **可视化**: ECharts 5
- **HTTP**: Axios
- **路由**: Vue Router 4

## 项目结构

```
web-admin/
├── public/                                # 静态资源
│   └── favicon.svg
├── src/
│   ├── main.js                            # 入口文件
│   ├── App.vue                            # 根组件
│   ├── api/                               # 接口请求封装（30+ 模块）
│   │   ├── request.js                     # Axios 实例 + 拦截器
│   │   ├── auth.js                        # 登录/注册
│   │   ├── ai.js                          # AI 问答/统计
│   │   ├── knowledge.js                   # 知识图谱/知识库
│   │   ├── order.js                       # 订单管理
│   │   ├── orderStat.js                   # 订单统计
│   │   ├── purchase.js                    # 采购管理
│   │   ├── purchaseStat.js                # 采购统计
│   │   ├── supplier.js                    # 供应商
│   │   ├── customer.js                    # 客户
│   │   ├── material.js                    # 物料
│   │   ├── product.js                     # 产品
│   │   ├── equipment.js                   # 设备
│   │   ├── equipmentMaintain.js           # 设备保养
│   │   ├── warehouse.js                   # 仓库
│   │   ├── stock.js                       # 库存
│   │   ├── stockIn.js                     # 入库
│   │   ├── stockOut.js                    # 出库
│   │   ├── stockFlow.js                   # 库存流水
│   │   ├── productionPlan.js              # 生产计划
│   │   ├── workOrder.js                   # 工单
│   │   ├── productionReport.js            # 报工
│   │   ├── process.js                     # 工艺
│   │   ├── sop.js                         # SOP
│   │   ├── qualityTask.js                 # 质检任务
│   │   ├── qualityResult.js               # 质检结果
│   │   ├── exception.js                   # 异常
│   │   ├── operationLog.js                # 操作日志
│   │   ├── dict.js                        # 字典
│   │   ├── bigScreen.js                   # 车间大屏
│   │   ├── trace.js                       # 订单追溯
│   │   └── snTrace.js                     # SN 追溯
│   ├── assets/
│   │   └── styles/
│   │       └── global.css                 # 全局样式
│   ├── components/                        # 通用组件
│   │   ├── BaseTable.vue                  # 通用表格
│   │   ├── SearchForm.vue                 # 通用搜索表单
│   │   ├── DictSelect.vue                 # 数据字典下拉
│   │   └── AiAssistant.vue                # AI 助手悬浮组件
│   ├── config/
│   │   └── roleMenu.js                    # 角色菜单配置
│   ├── router/
│   │   └── index.js                       # 路由表 + 路由守卫
│   ├── stores/
│   │   └── user.js                        # 用户信息 & Token
│   ├── utils/
│   │   └── request.js                     # 请求工具
│   └── views/                             # 页面（40+ 页面）
│       ├── login/                         # 登录页
│       ├── layout/                        # 后台布局
│       ├── dashboard/                     # 工作台
│       ├── big-screen/                    # 数字化车间大屏（全屏）
│       ├── trace-screen/                  # 订单全链路追溯大屏
│       ├── ai-stats/                      # AI 问答统计
│       ├── knowledge-base/                # AI 知识库管理
│       ├── knowledge-graph/               # 知识图谱可视化
│       ├── order/                         # 订单管理
│       ├── order-statistics/              # 订单统计
│       ├── purchase/                      # 采购管理
│       ├── purchase-statistics/           # 采购统计
│       ├── supplier/                      # 供应商
│       ├── customer/                      # 客户
│       ├── material/                      # 物料
│       ├── product/                       # 产品
│       ├── equipment/                     # 设备台账
│       ├── equipment-monitor/             # 设备监控仪表盘
│       ├── equipment-fault/               # 设备故障记录
│       ├── equipment-maintain/            # 设备维保记录
│       ├── stock/                         # 库存查询
│       ├── stock-in/                      # 入库
│       ├── stock-out/                     # 出库
│       ├── stock-flow/                    # 库存流水
│       ├── stock-take/                    # 库存盘点
│       ├── stock-warning/                 # 库存预警
│       ├── stock-statistics/              # 库存统计
│       ├── work-order/                    # 工单管理
│       ├── production/                    # 生产报工
│       ├── production-plan/               # 生产计划
│       ├── production-progress/           # 生产进度
│       ├── production-statistics/         # 生产统计
│       ├── process-config/                # 工艺配置
│       ├── sop-board/                     # SOP 看板
│       ├── quality/                       # 质检任务
│       ├── quality-record/                # 质检记录
│       ├── quality-statistics/            # 质量统计
│       ├── unqualified/                   # 不合格品
│       ├── exception-workbench/           # 异常处理
│       ├── sn-trace/                      # SN 全生命周期追溯
│       ├── operation-log/                 # 操作日志
│       └── system/                        # 系统管理
├── index.html                             # HTML 模板
├── vite.config.js                         # Vite 配置（代理 /api → 8080）
└── package.json                           # 依赖管理
```

## 启动方式

### 安装依赖

```bash
cd web-admin
npm install
```

### 开发模式

```bash
npm run dev
# 访问地址: http://localhost:5173
```

### 构建生产版本

```bash
npm run build
```

### 预览生产包

```bash
npm run preview
```

## 核心页面入口

| 页面 | 路径 | 说明 |
|------|------|------|
| 登录 | /login | 用户登录 |
| 工作台 | /dashboard | 首页数据看板 |
| 数字化车间大屏 | /big-screen | 全屏数据可视化 |
| 订单追溯大屏 | /trace-screen | 全屏订单链路追溯 |
| AI 知识图谱 | /knowledge-graph | 知识网络可视化 |
| AI 知识库 | /knowledge-base | SOP 文档管理 |
| AI 问答统计 | /ai-stats | 提问趋势 / 高频问题 |
| SN 追溯 | /sn-trace | 一片一码全生命周期 |

## AI 助手组件

- **位置**：右下角悬浮按钮（`<AiAssistant />`）
- **触发方式**：业务页面"问AI"按钮 + 全局悬浮按钮 + window 事件 `ask-ai`
- **能力**：多轮对话 / 引用来源 / 上下文感知 / 业务数据问答

## 开发规范

- 页面文件位于 `views/` 下，按业务模块分子目录
- 通用组件 `components/`，禁止页面内重复造轮子
- 接口封装 `api/`，按业务模块拆分文件
- 使用 Pinia 做全局状态，组件内尽量用 `ref` / `reactive`
- 所有页面统一使用 Element Plus 组件 + 深色科技风格
- 提交前执行 `npm run build` 确保无编译错误

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 超级管理员（全部菜单可见） |
