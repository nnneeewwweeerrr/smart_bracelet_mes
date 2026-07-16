<template>
  <view class="workbench-page">
    <scroll-view class="page-body" scroll-y>
      <view class="page-header" :style="{ paddingTop: statusBarPadding }">
        <view class="header-bg"></view>
        <view class="header-wave"></view>
        <view class="header-content">
          <view class="header-top">
            <view class="location-tag">
              <text class="location-icon">🏭</text>
              <text class="location-text">生产车间</text>
            </view>
            <view class="avatar-wrap" @click="goTo('/pages/mine/index')">
              <image class="user-avatar" src="/static/头像.jpg" mode="aspectFill"></image>
            </view>
          </view>
          <view class="header-title-area">
            <text class="header-title">智能手环MES</text>
            <text class="header-subtitle">{{ greetingText }}，{{ userInfo.realName || '操作员' }}</text>
          </view>
          <view class="stats-row">
            <view class="stat-card">
              <view class="stat-icon-wrap blue">
                <text class="stat-icon">📊</text>
              </view>
              <view class="stat-info">
                <text class="stat-value">{{ dashboard.todayOutput }}</text>
                <text class="stat-label">今日产量</text>
              </view>
            </view>
            <view class="stat-card">
              <view class="stat-icon-wrap green">
                <text class="stat-icon">✅</text>
              </view>
              <view class="stat-info">
                <text class="stat-value">{{ dashboard.todayPassRate }}%</text>
                <text class="stat-label">合格率</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      <view class="notice-section">
        <view class="notice-card">
          <view class="notice-icon">📢</view>
          <view class="notice-body">
            <view class="notice-header">
              <text class="notice-title">通知公告</text>
              <text class="notice-time">{{ currentDate }}</text>
            </view>
            <text class="notice-desc">今日任务：完成生产计划80%，注意保持5S标准，及时上报异常</text>
          </view>
        </view>
      </view>

      <view class="quick-section">
        <view class="section-header">
          <text class="section-title">快捷操作</text>
          <text class="section-more" @click="goTo('/pages/workbench/apps')">全部 ›</text>
        </view>
        <view class="quick-grid">
          <view class="quick-item" @click="goTo('/pages/scan/index')">
            <view class="quick-icon scan">
              <text class="icon-text">📷</text>
            </view>
            <text class="quick-label">扫码报工</text>
            <u-badge v-if="scanCount > 0" :count="scanCount" type="error" class="item-badge"></u-badge>
          </view>
          <view class="quick-item" @click="goTo('/pages/stock/index')">
            <view class="quick-icon stock">
              <text class="icon-text">📦</text>
            </view>
            <text class="quick-label">仓储管理</text>
            <u-badge v-if="stockWarningCount > 0" :count="stockWarningCount" type="error" class="item-badge"></u-badge>
          </view>
          <view class="quick-item" @click="goTo('/pages/production/index')">
            <view class="quick-icon prod">
              <text class="icon-text">🏭</text>
            </view>
            <text class="quick-label">生产报工</text>
          </view>
          <view class="quick-item" @click="goTo('/pages/quality/index')">
            <view class="quick-icon quality">
              <text class="icon-text">✅</text>
            </view>
            <text class="quick-label">质检录入</text>
          </view>
          <view class="quick-item" @click="goTo('/pages/exception/index')">
            <view class="quick-icon exception">
              <text class="icon-text">⚠️</text>
            </view>
            <text class="quick-label">异常上报</text>
            <u-badge v-if="pendingExceptionCount > 0" :count="pendingExceptionCount" type="error" class="item-badge"></u-badge>
          </view>
          <view class="quick-item" @click="goTo('/pages/quality/test-input')">
            <view class="quick-icon test">
              <text class="icon-text">🔬</text>
            </view>
            <text class="quick-label">功能测试</text>
          </view>
          <view class="quick-item" @click="goTo('/pages/production/trace')">
            <view class="quick-icon trace">
              <text class="icon-text">🔍</text>
            </view>
            <text class="quick-label">批次追溯</text>
          </view>
          <view class="quick-item" @click="goTo('/pages/production/repair')">
            <view class="quick-icon repair">
              <text class="icon-text">🔧</text>
            </view>
            <text class="quick-label">不良品维修</text>
          </view>
        </view>
      </view>

      <view class="ai-section" @click="goTo('/pages/ai/index')">
        <view class="ai-bg"></view>
        <view class="ai-content">
          <view class="ai-icon-wrap">
            <text class="ai-icon">🤖</text>
          </view>
          <view class="ai-info">
            <text class="ai-title">AI生产助手</text>
            <text class="ai-desc">智能问答 · 语音交互 · 扫码联动</text>
          </view>
          <view class="ai-arrow">
            <text class="arrow-text">立即提问</text>
            <text class="arrow-icon">›</text>
          </view>
        </view>
      </view>

      <view class="task-section">
        <view class="section-header">
          <text class="section-title">我的任务</text>
          <text class="section-more" @click="goTo('/pages/mine/todo')">查看全部 ›</text>
        </view>
        <view v-if="tasks.length === 0" class="empty-state">
          <text class="empty-icon">📋</text>
          <text class="empty-text">暂无任务</text>
        </view>
        <view v-else class="task-list">
          <view class="task-item" v-for="item in tasks" :key="item.id" @click="goTaskDetail(item)">
            <view class="task-status" :class="item.statusType"></view>
            <view class="task-content">
              <view class="task-top">
                <text class="task-name">{{ item.workNo || item.taskNo }}</text>
                <u-tag :text="item.statusText" :type="item.statusType" size="mini" plain class="task-tag"></u-tag>
              </view>
              <text class="task-desc">{{ item.description || '暂无描述' }}</text>
            </view>
            <text class="task-arrow">›</text>
          </view>
        </view>
      </view>

      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script>
import { getUser, isLoggedIn } from '@/utils/auth'
import { getWorkerTasks } from '@/api/user'
import { getDashboard } from '@/api/production'
import { getStockWarning } from '@/api/stock'
import { getExceptionList } from '@/api/exception'

export default {
  data() {
    return {
      userInfo: {},
      tasks: [],
      pendingExceptionCount: 0,
      stockWarningCount: 0,
      scanCount: 0,
      statusBarPadding: '',
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
    },
    currentDate() {
      const now = new Date()
      const month = now.getMonth() + 1
      const day = now.getDate()
      const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      return `${month}月${day}日 ${weekDays[now.getDay()]}`
    }
  },
  onLoad() {
    const sysInfo = uni.getSystemInfoSync()
    const statusBarHeight = sysInfo.statusBarHeight || 44
    this.statusBarPadding = `${statusBarHeight}px`
    this.bodyPadding = `calc(${statusBarHeight}px + 30rpx)`
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
      } catch (e) {
        console.error('获取工作台数据失败:', e)
      }
    },
    async loadTasks() {
      try {
        const res = await getWorkerTasks()
        const list = Array.isArray(res) ? res : []
        this.tasks = list.slice(0, 3).map(item => ({
          ...item,
          statusText: item.status === 1 ? '进行中' : item.status === 2 ? '已完成' : '待处理',
          statusType: item.status === 1 ? 'primary' : item.status === 2 ? 'success' : 'warning'
        }))
      } catch (e) {
        console.error('获取任务失败:', e)
        this.tasks = []
      }
    },
    async loadWarningCounts() {
      try {
        const warnings = await getStockWarning()
        this.stockWarningCount = Array.isArray(warnings) ? warnings.length : 0
      } catch (e) { console.error('获取库存预警失败:', e) }
      try {
        const res = await getExceptionList({ status: 0, page: 1, size: 1 })
        const records = (res && res.records) || res || []
        this.pendingExceptionCount = Array.isArray(records) ? records.length : 0
      } catch (e) { console.error('获取异常信息失败:', e) }
    },
    goTaskDetail(item) {
      if (item.workNo) {
        this.goTo(`/pages/production/detail?workOrderId=${encodeURIComponent(item.workNo)}`)
      } else {
        this.goTo('/pages/mine/todo')
      }
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
  background: #F0F2F5;
}

.page-header {
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, #3B7CFF 0%, #5B9AFF 40%, #7CB7FF 100%);
}

.header-wave {
  position: absolute;
  bottom: -2rpx;
  left: 0;
  right: 0;
  height: 60rpx;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 120'%3E%3Cpath fill='%23F0F2F5' fill-opacity='1' d='M0,64L60,69.3C120,75,240,85,360,80C480,75,600,53,720,48C840,43,960,53,1080,58.7C1200,64,1320,64,1380,64L1440,64L1440,120L1380,120C1320,120,1200,120,1080,120C960,120,840,120,720,120C600,120,480,120,360,120C240,120,120,120,60,120L0,120Z'%3E%3C/path%3E%3C/svg%3E") no-repeat;
  background-size: 100% 100%;
}

.header-content {
  position: relative;
  z-index: 1;
  padding: 20rpx 32rpx 40rpx;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.location-tag {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
  padding: 10rpx 24rpx;
  border-radius: 24rpx;
  backdrop-filter: blur(10px);
}

.location-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.location-text {
  font-size: 26rpx;
  color: #ffffff;
  font-weight: 500;
}

.avatar-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3rpx solid rgba(255, 255, 255, 0.4);
  overflow: hidden;
}

.user-avatar {
  width: 100%;
  height: 100%;
}

.header-title-area {
  margin-bottom: 28rpx;
}

.header-title {
  font-size: 44rpx;
  font-weight: 700;
  color: #ffffff;
  display: block;
  letter-spacing: 2rpx;
}

.header-subtitle {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 6rpx;
  display: block;
}

.stats-row {
  display: flex;
  gap: 20rpx;
}

.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20rpx;
  padding: 20rpx;
  backdrop-filter: blur(10px);
}

.stat-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;

  &.blue { background: rgba(255, 255, 255, 0.2); }
  &.green { background: rgba(82, 196, 26, 0.2); }
}

.stat-icon {
  font-size: 28rpx;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 36rpx;
  font-weight: 700;
  color: #ffffff;
  display: block;
}

.stat-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 2rpx;
  display: block;
}

.page-body {
  height: 100vh;
  padding: 0;
}

.notice-section {
  margin-bottom: 24rpx;
  padding: 0 32rpx;
}

.notice-card {
  display: flex;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.notice-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.notice-body {
  flex: 1;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.notice-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.notice-time {
  font-size: 22rpx;
  color: #999;
}

.notice-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

.quick-section {
  margin-bottom: 24rpx;
  padding: 0 32rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.section-more {
  font-size: 24rpx;
  color: #3B7CFF;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  padding: 20rpx 0;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: scale(0.96);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  }
}

.quick-icon {
  width: 100rpx;
  height: 100rpx;
  border-radius: 26rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.3) 0%, transparent 50%);
    pointer-events: none;
  }

  &:active {
    transform: scale(0.92);
  }

  &.scan { background: linear-gradient(135deg, #6B8FFF 0%, #3B7CFF 100%); }
  &.stock { background: linear-gradient(135deg, #52C41A 0%, #389E0D 100%); }
  &.prod { background: linear-gradient(135deg, #FAAD14 0%, #D48806 100%); }
  &.quality { background: linear-gradient(135deg, #FF6B6B 0%, #FF4D4F 100%); }
  &.exception { background: linear-gradient(135deg, #FAAD14 0%, #D48806 100%); }
  &.test { background: linear-gradient(135deg, #9370DB 0%, #722ED1 100%); }
  &.trace { background: linear-gradient(135deg, #13C2C2 0%, #08979C 100%); }
  &.repair { background: linear-gradient(135deg, #FF6B6B 0%, #FF4D4F 100%); }
}

.icon-text {
  font-size: 44rpx;
  position: relative;
  z-index: 1;
}

.quick-label {
  font-size: 26rpx;
  color: #333333;
  font-weight: 600;
  letter-spacing: 0.5rpx;
}

.item-badge {
  position: absolute;
  top: 8rpx;
  right: 12rpx;
}

.ai-section {
  position: relative;
  margin-bottom: 24rpx;
  border-radius: 24rpx;
  overflow: hidden;
  padding: 0 32rpx;
}

.ai-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #1e3a5f 0%, #2a5298 50%, #3B7CFF 100%);
}

.ai-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  padding: 32rpx;
}

.ai-icon-wrap {
  width: 88rpx;
  height: 88rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.ai-icon {
  font-size: 40rpx;
}

.ai-info {
  flex: 1;
}

.ai-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #ffffff;
  display: block;
}

.ai-desc {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 6rpx;
  display: block;
}

.ai-arrow {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
  padding: 14rpx 28rpx;
  border-radius: 30rpx;
}

.arrow-text {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 500;
}

.arrow-icon {
  font-size: 28rpx;
  color: #ffffff;
  margin-left: 6rpx;
}

.task-section {
  margin-bottom: 24rpx;
  padding: 0 32rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
  background: #ffffff;
  border-radius: 24rpx;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 16rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.task-list {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 8rpx 0;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.task-item {
  display: flex;
  align-items: center;
  padding: 24rpx 28rpx;
  border-bottom: 1rpx solid #F0F0F0;
  transition: all 0.3s ease;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: #FAFAFA;
  }
}

.task-status {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  flex-shrink: 0;

  &.primary { background: #3B7CFF; }
  &.success { background: #52C41A; }
  &.warning { background: #FAAD14; }
}

.task-content {
  flex: 1;
}

.task-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.task-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.task-tag {
  font-size: 20rpx;
}

.task-desc {
  font-size: 24rpx;
  color: #999;
  line-height: 1.5;
}

.task-arrow {
  font-size: 32rpx;
  color: #DDD;
  margin-left: 16rpx;
}

.bottom-space {
  height: 160rpx;
}
</style>
