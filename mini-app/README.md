# 智能手环MES系统 - 小程序端

## 技术栈

- **框架**: UniApp
- **语言**: JavaScript
- **UI组件**: uni-ui
- **构建工具**: HBuilderX

## 项目结构

```
mini-app/
├── src/
│   ├── pages/          # 小程序页面
│   │   ├── workbench/  # 工作台
│   │   ├── scan/       # 扫码功能
│   │   ├── stock/      # 仓储操作
│   │   ├── production/ # 生产报工
│   │   ├── quality/    # 质检录入
│   │   ├── exception/  # 异常上报
│   │   └── mine/       # 个人中心
│   ├── components/     # 通用组件
│   ├── api/            # 接口封装
│   ├── utils/          # 工具方法
│   └── static/         # 静态资源
├── package.json        # 依赖配置
├── pages.json          # 页面路由配置
└── manifest.json       # 小程序配置
```

## 启动方式

### 安装依赖

```bash
cd mini-app
npm install
```

### H5开发模式

```bash
npm run dev:h5
```

### 微信小程序开发

```bash
npm run dev:mp-weixin
```

### 构建H5版本

```bash
npm run build:h5
```

### 构建微信小程序版本

```bash
npm run build:mp-weixin
```

## 模块说明

| 模块 | 说明 |
|------|------|
| workbench | 工作台、任务列表、快捷操作 |
| scan | 扫码入库、扫码出库、扫码报工 |
| stock | 库存查询、物料盘点、库存预警 |
| production | 生产报工、工序确认、产量录入 |
| quality | 质检任务、质检录入、不合格处理 |
| exception | 异常上报、异常处理、异常查询 |
| mine | 个人信息、操作记录、设置 |