# 智能手环MES系统 - 网页管理端

## 技术栈

- **语言**: JavaScript/TypeScript
- **框架**: Vue 3
- **构建工具**: Vite
- **UI组件**: Element Plus
- **状态管理**: Pinia

## 项目结构

```
web-admin/
├── src/
│   ├── views/          # 页面文件
│   │   ├── dashboard/ # 首页看板
│   │   ├── order/     # 订单管理
│   │   ├── purchase/  # 采购管理
│   │   ├── stock/     # 仓储管理
│   │   ├── production/# 生产管理
│   │   ├── quality/   # 质量管理
│   │   ├── equipment/ # 设备管理
│   │   └── system/    # 系统管理
│   ├── components/     # 通用业务组件
│   ├── api/            # 接口请求封装
│   ├── router/         # 路由配置
│   ├── store/          # 全局状态管理
│   ├── utils/          # 工具方法
│   └── assets/         # 静态资源
├── package.json        # 依赖配置
├── vite.config.js      # Vite配置
└── index.html          # 入口HTML
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
```

### 构建生产版本

```bash
npm run build
```

## 模块说明

| 模块 | 说明 |
|------|------|
| dashboard | 数据看板、统计概览 |
| order | 订单列表、订单详情、订单审批 |
| purchase | 采购计划、采购订单、供应商管理 |
| stock | 库存查询、入库管理、出库管理 |
| production | 生产计划、工单管理、生产进度 |
| quality | 质检任务、质检结果、质量报表 |
| equipment | 设备台账、维护保养、设备状态 |
| system | 用户管理、角色权限、数据字典 |