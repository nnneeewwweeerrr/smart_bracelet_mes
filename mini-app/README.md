# 智能手环MES系统 - 小程序端（车间现场端）

> UniApp + Vue 3 + uView Plus
>
> 车间现场专用：扫码报工、扫码追溯、异常上报、IQC检验、AI 助手

## 技术栈

- **跨端框架**: UniApp
- **前端**: Vue 3 Composition API
- **UI 组件**: uView Plus 2.x
- **构建工具**: Vite 5
- **HTTP**: uni.request 封装
- **运行目标**: H5、微信小程序、APP（语音播报依赖 Plus 环境）

## 项目结构

```
mini-app/
├── src/
│   ├── main.js                            # 入口文件
│   ├── App.vue                            # 根组件
│   ├── manifest.json                      # UniApp 应用配置
│   ├── pages.json                         # 页面路由配置（30+ 页面）
│   ├── uni.scss                           # 全局 SCSS 变量
│   ├── api/                               # 接口请求封装
│   │   ├── request.js                     # 请求封装 + 21 个 API 别名映射
│   │   ├── auth.js                        # 登录认证
│   │   ├── user.js                        # 用户/任务
│   │   ├── ai.js                          # AI 助手接口
│   │   ├── production.js                  # 生产/工单
│   │   ├── quality.js                     # 质检
│   │   ├── exception.js                   # 异常
│   │   └── stock.js                       # 库存
│   ├── components/                        # 通用组件
│   │   └── DynamicBackground.vue          # 动态深色背景
│   ├── pages/                             # 页面（30+ 页面）
│   │   ├── login/                         # 登录
│   │   ├── workbench/                     # 工作台（含 AI 助手入口）
│   │   │   ├── index.vue                  # 工作台首页
│   │   │   ├── apps.vue                   # 应用中心
│   │   │   └── stats.vue                  # 数据统计
│   │   ├── ai/                            # AI 助手
│   │   │   └── index.vue                  # AI 对话主页
│   │   ├── scan/                          # 扫码
│   │   │   └── index.vue                  # 扫码中心
│   │   ├── stock/                         # 仓储
│   │   │   ├── index.vue                  # 仓储首页
│   │   │   ├── material.vue               # 物料查询
│   │   │   ├── scan-in.vue                # 扫码入库
│   │   │   ├── scan-out.vue               # 扫码出库
│   │   │   ├── check.vue                  # 库存盘点
│   │   │   └── record.vue                 # 仓储记录
│   │   ├── production/                    # 生产
│   │   │   ├── index.vue                  # 生产首页
│   │   │   ├── workorder.vue              # 工单列表
│   │   │   ├── detail.vue                 # 工单详情
│   │   │   ├── report.vue                 # 生产报工
│   │   │   ├── sop.vue                    # SOP 作业指导
│   │   │   ├── trace.vue                  # 生产追溯
│   │   │   ├── sn-trace.vue               # SN 追溯
│   │   │   └── repair.vue                 # 不良品维修
│   │   ├── quality/                       # 质量
│   │   │   ├── index.vue                  # 质检首页
│   │   │   ├── iqc.vue                    # IQC 来料检验
│   │   │   ├── task.vue                   # 质检任务
│   │   │   ├── test-dashboard.vue         # 测试数据看板
│   │   │   └── test-input.vue             # 功能测试录入
│   │   ├── exception/                     # 异常
│   │   │   ├── index.vue                  # 异常首页
│   │   │   ├── create.vue                 # 异常上报
│   │   │   └── pending.vue                # 待处理异常
│   │   └── mine/                          # 我的
│   │       ├── index.vue                  # 我的首页
│   │       ├── profile.vue                # 个人信息
│   │       ├── todo.vue                   # 待办任务
│   │       ├── log.vue                    # 操作记录
│   │       ├── notifications.vue          # 消息通知
│   │       └── settings.vue               # 设置
│   ├── utils/                             # 工具类
│   │   ├── auth.js                        # 登录态管理
│   │   ├── request.js                     # 请求封装
│   │   └── scan.js                        # 扫码工具
│   └── static/                            # 静态资源（tabbar 图标/logo 等）
├── tests/                                 # 测试文件
├── index.html                             # H5 入口
├── vite.config.js                         # Vite 配置
└── package.json                           # 依赖管理
```

## 启动方式

### 安装依赖

```bash
cd mini-app
npm install
```

### H5 端开发（推荐浏览器查看）

```bash
npm run dev:h5
# 访问地址: http://localhost:5175
```

### 微信小程序端开发

```bash
npm run dev:mp-weixin
# 在 HBuilderX 中打开 dist/dev/mp-weixin 即可预览
```

### APP 端开发

```bash
npm run dev:app
# 需安装 HBuilderX
```

### 构建生产版本

```bash
# H5
npm run build:h5

# 微信小程序
npm run build:mp-weixin
```

## AI 生产助手

### 入口

工作台首页 → "AI 生产助手" 卡片（蓝色渐变 + 脉冲动画）

### 能力

- **文字输入**：自然语言提问（如"焊接温度多少？"）
- **语音输入**：长按麦克风按钮录音，调用 uni.getRecorderManager 录音
- **语音播报**：AI 回答支持语音播放（依赖 Plus.speech 引擎，需 APP 端）
- **扫码联动**：扫描工单 / SN / 物料码，自动带入场景信息提问
- **引用来源**：AI 回答标注知识库来源，可展开查看知识片段
- **快捷操作**：根据回答内容推荐跳转业务页面

### 演示问题

- 焊接工序的推荐温度是多少？
- 屏幕贴合出现气泡应如何处理？
- 如何进行生产报工操作？
- 防水测试的标准是什么？
- 今天的工单进度如何？

## 适配说明

- **H5 端**：扫码调用摄像头，语音播报降级为无声
- **微信小程序端**：扫码调用 wx.scanCode，语音播报调用 wx.createInnerAudioContext
- **APP 端**：扫码调用 barcode，语音播报调用 plus.speech.startSpeak

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 超级管理员 |
| worker001 | 123456 | 车间工人 |
