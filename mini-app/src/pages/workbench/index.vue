<template>
  <view class="workbench-page">
    <DynamicBackground theme="theme-primary" />
    <view class="page-content">
    <view class="header-panel">
      <view class="header-top">
      <view class="header-left">
        <text class="header-title">智能手环MES</text>
        <text class="user-greeting">{{ greetingText }}，{{ userInfo.realName || '用户' }}</text>
      </view>
      <view class="avatar-wrap">
        <u-avatar :text="avatarText" size="52" bg-color="#ffffff" color="#3B7CFF"></u-avatar>
      </view>
    </view>
    </view>

    <view class="content-area">
      <view class="notice-card">
        <view class="notice-line"></view>
        <view class="notice-content">
          <view class="notice-header">
            <text class="notice-title">通知公告</text>
            <text class="notice-tag">温度 17/26° 阴转晴</text>
          </view>
          <text class="notice-desc">今天请优先处理未完成工单，生产现场保持5S标准，及时上报异常。</text>
        </view>
      </view>

      <view class="dashboard-cards">
        <view class="data-card card-output" @click="goTo('/pages/workbench/stats')">
          <view class="card-deco"></view>
          <view class="card-content">
            <view class="card-top">
              <view class="card-icon-wrap output-icon">
                <text class="icon-text">产</text>
              </view>
              <text class="card-label">今日产量</text>
            </view>
            <view class="card-body">
              <text class="card-value">{{ dashboard.todayOutput }}</text>
              <text class="card-unit">件</text>
            </view>
            <view class="card-trend">
              <text :class="['trend-text', dashboard.outputTrend >= 0 ? 'trend-up' : 'trend-down']">
                {{ dashboard.outputTrend >= 0 ? '↑' : '↓' }}{{ Math.abs(dashboard.outputTrend) }}%
              </text>
              <text class="trend-label">较昨日</text>
            </view>
          </view>
        </view>
        <view class="data-card card-rate" @click="goTo('/pages/workbench/stats')">
          <view class="card-deco"></view>
          <view class="card-content">
            <view class="card-top">
              <view class="card-icon-wrap rate-icon">
                <text class="icon-text">合</text>
              </view>
              <text class="card-label">今日合格率</text>
            </view>
            <view class="card-body">
              <text class="card-value">{{ dashboard.todayPassRate }}</text>
              <text class="card-unit">%</text>
            </view>
            <view class="card-trend">
              <text :class="['trend-text', dashboard.rateTrend >= 0 ? 'trend-up' : 'trend-down']">
                {{ dashboard.rateTrend >= 0 ? '↑' : '↓' }}{{ Math.abs(dashboard.rateTrend) }}%
              </text>
              <text class="trend-label">较昨日</text>
            </view>
          </view>
        </view>
      </view>

      <!-- AI生产助手入口 -->
      <view class="ai-entry-card" @click="goTo('/pages/ai/index')">
        <view class="ai-card-bg"></view>
        <view class="ai-card-content">
          <view class="ai-card-left">
            <view class="ai-avatar">
              <text class="ai-avatar-icon">AI</text>
              <view class="ai-pulse"></view>
            </view>
            <view class="ai-card-text">
              <text class="ai-card-title">AI生产助手</text>
              <text class="ai-card-desc">智能问答 · 语音交互 · 扫码联动</text>
            </view>
          </view>
          <view class="ai-card-right">
            <text class="ai-card-arrow">立即提问 ›</text>
          </view>
        </view>
      </view>

      <view class="quick-actions">
        <view class="section-header">
          <text class="section-title">快捷操作</text>
          <text class="section-more" @click="goTo('/pages/workbench/apps')">更多</text>
        </view>
        <view class="action-grid">
          <view class="action-item" @click="goTo('/pages/scan/index')">
            <view class="action-icon scan-icon">
              <text class="action-icon-text">扫</text>
            </view>
            <text class="action-text">扫码报工</text>
          </view>
          <view class="action-item" @click="goTo('/pages/stock/index')">
            <view class="action-icon stock-icon">
              <text class="action-icon-text">仓</text>
              <u-badge v-if="stockWarningCount > 0" :count="stockWarningCount" :offset="[4, -4]" type="error"></u-badge>
            </view>
            <text class="action-text">仓储管理</text>
          </view>
          <view class="action-item" @click="goTo('/pages/production/index')">
            <view class="action-icon prod-icon">
              <text class="action-icon-text">工</text>
            </view>
            <text class="action-text">生产报工</text>
          </view>
          <view class="action-item" @click="goTo('/pages/quality/index')">
            <view class="action-icon quality-icon">
              <text class="action-icon-text">检</text>
            </view>
            <text class="action-text">质检录入</text>
          </view>
          <view class="action-item" @click="goTo('/pages/exception/index')">
            <view class="action-icon exception-icon">
              <text class="action-icon-text">异</text>
              <u-badge v-if="pendingExceptionCount > 0" :count="pendingExceptionCount" :offset="[4, -4]" type="error"></u-badge>
            </view>
            <text class="action-text">异常上报</text>
          </view>
          <view class="action-item" @click="goTo('/pages/quality/test-input')">
            <view class="action-icon test-icon">
              <text class="action-icon-text">测</text>
            </view>
            <text class="action-text">功能测试</text>
          </view>
          <view class="action-item" @click="goTo('/pages/production/repair')">
            <view class="action-icon repair-icon">
              <text class="action-icon-text">修</text>
            </view>
            <text class="action-text">不良品维修</text>
          </view>
        </view>
      </view>

      <view class="task-section">
        <view class="section-header">
          <text class="section-title">我的任务</text>
          <text class="section-more" @click="goTo('/pages/mine/todo')">查看全部</text>
        </view>
        <view v-if="tasks.length === 0" class="empty-tips">
          <u-empty mode="data" text="暂无任务" iconSize="100"></u-empty>
        </view>
        <view v-else class="task-list">
          <view class="task-card" v-for="item in tasks" :key="item.id">
            <view class="task-header">
              <text class="task-name">{{ item.workNo || item.taskNo }}</text>
              <u-tag :text="item.statusText" :type="item.statusType" size="mini" plain></u-tag>
            </view>
            <text class="task-desc">{{ item.description || '暂无描述' }}</text>
          </view>
        </view>
      </view>
    </view>
    </view>
  </view>
</template>

<script>
import { getUser, isLoggedIn } from '@/utils/auth'
import { getWorkerTasks } from '@/api/user'
import { getDashboard } from '@/api/production'
import { getStockWarning } from '@/api/stock'
import { getExceptionList } from '@/api/exception'
import DynamicBackground from '@/components/DynamicBackground'

export default {
  components: {
    DynamicBackground
  },
  data() {
    return {
      userInfo: {},
      tasks: [],
      pendingExceptionCount: 0,
      stockWarningCount: 0,
      dashboard: {
        todayOutput: 0,
        todayPassRate: '0.0',
        outputTrend: 0,
        rateTrend: 0
      }
    }
  },
  computed: {
    avatarText() {
      return (this.userInfo.realName || '?').substring(0, 1)
    },
    greetingText() {
      const h = new Date().getHours()
      if (h < 6) return '夜深了'
      if (h < 12) return '上午好'
      if (h < 14) return '中午好'
      if (h < 18) return '下午好'
      return '晚上好'
    }
  },
  onShow() {
    if (!isLoggedIn()) {
      uni.reLaunch({ url: '/pages/login/index' })
      return
    }
    this.userInfo = getUser() || {}
    this.loadTasks()
    this.loadDashboard()
    this.loadWarningCounts()
  },
  methods: {
    async loadDashboard() {
      try {
        const res = await getDashboard()
        this.dashboard = {
          todayOutput: res.todayOutput || 0,
          todayPassRate: res.todayPassRate != null ? Number(res.todayPassRate).toFixed(1) : '0.0',
          outputTrend: res.outputTrend || 0,
          rateTrend: res.rateTrend || 0
        }
      } catch {
        // keep default
      }
    },
    async loadTasks() {
      try {
        const res = await getWorkerTasks()
        this.tasks = (res || []).map(item => ({
          ...item,
          statusText: item.status === 1 ? '进行中' : item.status === 2 ? '已完成' : '待处理',
          statusType: item.status === 1 ? 'primary' : item.status === 2 ? 'success' : 'info'
        }))
      } catch {
        this.tasks = []
      }
    },
    async loadWarningCounts() {
      try {
        const warnings = await getStockWarning()
        this.stockWarningCount = (warnings || []).length
      } catch { /* ignore */ }
      try {
        const res = await getExceptionList({ status: 0, page: 1, size: 1 })
        const records = (res && res.records) || res || []
        this.pendingExceptionCount = Array.isArray(records) ? records.length : 0
      } catch { /* ignore */ }
    },
    goTo(url) {
      const tabPages = ['/pages/workbench/index', '/pages/scan/index', '/pages/mine/index']
      if (tabPages.some(p => url.indexOf(p) > -1)) {
        uni.switchTab({ url })
      } else {
        uni.navigateTo({ url })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.workbench-page {
  min-height: 100vh;
  background: transparent;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
}

.header-panel {
  padding: $spacing-md $spacing-lg $spacing-lg;
  background: linear-gradient(135deg, #3B7CFF 0%, #5B9AFF 30%, #7CB7FF 60%, #9DC8FF 100%);
  border-radius: 0 0 $radius-xl $radius-xl;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -150rpx;
    right: -120rpx;
    width: 400rpx;
    height: 400rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.12);
    animation: float 6s ease-in-out infinite;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -80rpx;
    left: -80rpx;
    width: 240rpx;
    height: 240rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.08);
    animation: float 8s ease-in-out infinite reverse;
  }

  .header-decoration {
    position: absolute;
    top: 20rpx;
    left: 20rpx;
    width: 80rpx;
    height: 80rpx;
    border: 2rpx solid rgba(255, 255, 255, 0.1);
    border-radius: 16rpx;
    transform: rotate(15deg);
  }
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.header-title {
  font-size: $font-size-xl;
  font-weight: 700;
  color: #ffffff;
  display: block;
}

.user-greeting {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.65);
  margin-top: $spacing-xs;
}

.avatar-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: $radius-md;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid rgba(255, 255, 255, 0.2);
}

.content-area {
  padding: $spacing-lg;
  margin-top: -40rpx;
  position: relative;
  z-index: 2;
}

.notice-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: $radius-card;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  display: flex;
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;
  border: 1rpx solid rgba(255, 255, 255, 0.8);
}

.notice-line {
  width: 6rpx;
  background: linear-gradient(180deg, $brand-primary 0%, $brand-primary-light 100%);
  border-radius: 3rpx;
  margin-right: $spacing-md;
  flex-shrink: 0;
}

.notice-content {
  flex: 1;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.notice-title {
  font-size: $font-size-md;
  font-weight: 600;
  color: $text-primary;
  display: flex;
  align-items: center;
  gap: $spacing-xs;

  &::before {
    content: '';
    width: 8rpx;
    height: 8rpx;
    border-radius: 50%;
    background: $brand-primary;
  }
}

.notice-tag {
  font-size: $font-size-xs;
  color: $text-tertiary;
  background: $bg-hover;
  padding: $spacing-xs $spacing-sm;
  border-radius: $radius-xs;
}

.notice-desc {
  font-size: $font-size-sm;
  line-height: $line-height-relaxed;
  color: $text-secondary;
}

.dashboard-cards {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.data-card {
  flex: 1;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: $radius-card;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  padding: $spacing-lg $spacing-md;
  position: relative;
  overflow: hidden;
  border: 1rpx solid rgba(255, 255, 255, 0.8);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 6rpx;
    border-radius: 6rpx 6rpx 0 0;
  }

  &.card-output::before {
    background: linear-gradient(90deg, #3B7CFF 0%, #5B9AFF 50%, #7CB7FF 100%);
  }

  &.card-rate::before {
    background: linear-gradient(90deg, #52C41A 0%, #73D13D 50%, #95DE64 100%);
  }

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  }

  .card-deco {
    position: absolute;
    top: -40rpx;
    right: -40rpx;
    width: 140rpx;
    height: 140rpx;
    border-radius: 50%;
  }

  .card-output .card-deco {
    background: radial-gradient(circle, rgba(59, 124, 255, 0.12) 0%, transparent 70%);
  }

  .card-rate .card-deco {
    background: radial-gradient(circle, rgba(103, 194, 58, 0.12) 0%, transparent 70%);
  }
}

.card-content {
  position: relative;
  z-index: 1;
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-md;
}

.card-icon-wrap {
  width: 56rpx;
  height: 56rpx;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
}

.output-icon { background: $brand-primary-light-10; }
.rate-icon { background: $success-light-10; }

.icon-text {
  font-size: $font-size-md;
  font-weight: 600;
}

.output-icon .icon-text { color: $brand-primary; }
.rate-icon .icon-text { color: $success-color; }

.card-label {
  font-size: $font-size-sm;
  color: $text-tertiary;
  font-weight: 500;
}

.card-body {
  display: flex;
  align-items: flex-end;
  gap: $spacing-xs;
  margin-bottom: $spacing-sm;
}

.card-value {
  font-size: $font-size-display;
  font-weight: 700;
  color: $text-primary;
  line-height: $line-height-tight;
}

.card-unit {
  font-size: $font-size-sm;
  color: $text-tertiary;
  margin-bottom: 4rpx;
}

.card-trend {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.trend-text {
  font-size: $font-size-sm;
  font-weight: 600;
}

.trend-up { color: $success-color; }
.trend-down { color: $error-color; }

.trend-label {
  font-size: $font-size-xs;
  color: $text-placeholder;
}

/* AI助手入口卡片 */
.ai-entry-card {
  position: relative;
  border-radius: $radius-card;
  overflow: hidden;
  margin-bottom: $spacing-lg;
  cursor: pointer;
}
.ai-card-bg {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  opacity: 0.9;
}
.ai-card-content {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;
}
.ai-card-left {
  display: flex;
  align-items: center;
}
.ai-avatar {
  position: relative;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}
.ai-avatar-icon {
  font-size: 28rpx;
  font-weight: bold;
  color: #fff;
}
.ai-pulse {
  position: absolute;
  top: -4rpx; left: -4rpx; right: -4rpx; bottom: -4rpx;
  border-radius: 50%;
  border: 2rpx solid rgba(255,255,255,0.4);
  animation: aiPulse 2s ease-out infinite;
}
@keyframes aiPulse {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.3); opacity: 0; }
}
.ai-card-text {
  display: flex;
  flex-direction: column;
}
.ai-card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
}
.ai-card-desc {
  font-size: 24rpx;
  color: rgba(255,255,255,0.8);
  margin-top: 4rpx;
}
.ai-card-right {
  display: flex;
  align-items: center;
}
.ai-card-arrow {
  font-size: 26rpx;
  color: rgba(255,255,255,0.9);
  font-weight: 500;
}

.quick-actions {
  background: $bg-card;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  display: flex;
  align-items: center;
  gap: $spacing-sm;

  &::after {
    content: '';
    width: 40rpx;
    height: 4rpx;
    background: $brand-gradient;
    border-radius: 2rpx;
  }
}

.section-more {
  font-size: $font-size-sm;
  color: $brand-primary;
  display: flex;
  align-items: center;
  gap: $spacing-xs;

  &::after {
    content: '›';
    font-size: $font-size-lg;
  }
}

.action-grid {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-lg;
}

.action-item {
  width: calc(20% - 10rpx);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-md 0;
  border-radius: $radius-lg;
  transition: all $transition-fast;

  &:active {
    transform: scale(0.95);
    background: rgba(59, 124, 255, 0.05);
  }
}

.action-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: $spacing-sm;
  position: relative;
  transition: all $transition-fast;
}

.scan-icon { 
  background: linear-gradient(135deg, rgba(59, 124, 255, 0.1) 0%, rgba(107, 161, 255, 0.05) 100%);
  box-shadow: 0 6rpx 20rpx rgba(59, 124, 255, 0.12);
}
.stock-icon { 
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.1) 0%, rgba(115, 209, 61, 0.05) 100%);
  box-shadow: 0 6rpx 20rpx rgba(103, 194, 58, 0.12);
}
.prod-icon { 
  background: linear-gradient(135deg, rgba(250, 173, 20, 0.1) 0%, rgba(255, 197, 61, 0.05) 100%);
  box-shadow: 0 6rpx 20rpx rgba(250, 173, 20, 0.12);
}
.quality-icon { 
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.1) 0%, rgba(255, 120, 117, 0.05) 100%);
  box-shadow: 0 6rpx 20rpx rgba(245, 108, 108, 0.12);
}
.exception-icon { 
  background: linear-gradient(135deg, rgba(250, 173, 20, 0.1) 0%, rgba(255, 197, 61, 0.05) 100%);
  box-shadow: 0 6rpx 20rpx rgba(250, 173, 20, 0.12);
}
.test-icon { 
  background: linear-gradient(135deg, rgba(147, 112, 219, 0.1) 0%, rgba(168, 140, 224, 0.05) 100%);
  box-shadow: 0 6rpx 20rpx rgba(147, 112, 219, 0.12);
}
.repair-icon { 
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.1) 0%, rgba(255, 120, 117, 0.05) 100%);
  box-shadow: 0 6rpx 20rpx rgba(245, 108, 108, 0.12);
}

.action-icon-text {
  font-size: $font-size-xl;
  font-weight: 700;
}

.scan-icon .action-icon-text { color: #3B7CFF; }
.stock-icon .action-icon-text { color: #52C41A; }
.prod-icon .action-icon-text { color: #FAAD14; }
.quality-icon .action-icon-text { color: #FF4D4F; }
.exception-icon .action-icon-text { color: #FAAD14; }
.test-icon .action-icon-text { color: #9370DB; }
.repair-icon .action-icon-text { color: #F56C6C; }

.action-text {
  font-size: $font-size-sm;
  color: #4A5568;
  font-weight: 500;
  margin-top: 4rpx;
}

.task-section {
  background: $bg-card;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  padding: $spacing-lg;
}

.empty-tips {
  padding: $spacing-xxl 0;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.task-card {
  padding: $spacing-lg;
  border-radius: $radius-lg;
  background: $bg-hover;
  border: 1rpx solid $border-light;
  transition: all $transition-fast;

  &:active {
    background: $bg-active;
    transform: scale(0.99);
  }
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-sm;
}

.task-name {
  font-size: $font-size-md;
  font-weight: 600;
  color: $text-primary;
  max-width: 65%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-desc {
  font-size: $font-size-sm;
  color: $text-tertiary;
  line-height: $line-height-relaxed;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>