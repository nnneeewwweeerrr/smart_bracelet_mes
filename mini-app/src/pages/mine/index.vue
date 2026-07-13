<template>
  <view class="mine-page">
    <DynamicBackground theme="theme-primary" />
    <view class="page-content">
    <view class="mine-header">
      <view class="avatar-wrap">
        <u-avatar :text="avatarText" size="52" bg-color="#ffffff" color="#3B7CFF"></u-avatar>
      </view>
      <view class="mine-info">
        <text class="mine-name">{{ userInfo.realName || '未登录' }}</text>
        <text class="mine-role">{{ roleText }}</text>
      </view>
    </view>

    <view class="mine-stats">
      <view class="stat-item" @click="goTo('/pages/mine/todo')">
        <text class="stat-value">{{ todoCount }}</text>
        <text class="stat-label">待办任务</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item" @click="goTo('/pages/mine/log')">
        <text class="stat-value">{{ logCount }}</text>
        <text class="stat-label">操作记录</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ todayCount }}</text>
        <text class="stat-label">今日操作</text>
      </view>
    </view>

    <view class="performance-card">
      <text class="section-title">个人绩效</text>
      <view class="performance-row">
        <view class="perf-item">
          <view class="perf-icon-wrap output-icon">
            <text class="perf-icon-text">产</text>
          </view>
          <view class="perf-detail">
            <view class="perf-value-row">
              <text class="perf-value">{{ performance.todayOutput }}</text>
              <text class="perf-unit">件</text>
            </view>
            <text class="perf-label">今日产量</text>
          </view>
        </view>
        <view class="perf-divider"></view>
        <view class="perf-item">
          <view class="perf-icon-wrap rank-icon">
            <text class="perf-icon-text">排</text>
          </view>
          <view class="perf-detail">
            <view class="perf-value-row">
              <text class="perf-value">第{{ performance.departmentRank }}</text>
              <text class="perf-unit">/ {{ performance.departmentTotal }}人</text>
            </view>
            <view class="perf-label-row">
              <text class="perf-label">部门排名</text>
              <text v-if="performance.rankTrend > 0" class="rank-trend trend-up">↑{{ performance.rankTrend }}</text>
              <text v-else-if="performance.rankTrend < 0" class="rank-trend trend-down">↓{{ Math.abs(performance.rankTrend) }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="mine-menu">
      <view class="menu-item" @click="goTo('/pages/mine/log')">
        <view class="menu-icon-wrap">
          <text class="menu-icon-text">记</text>
        </view>
        <text class="menu-title">操作记录</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goTo('/pages/mine/profile')">
        <view class="menu-icon-wrap">
          <text class="menu-icon-text">人</text>
        </view>
        <text class="menu-title">个人信息</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <view class="mine-menu">
      <view class="menu-item" @click="goTo('/pages/mine/settings')">
        <view class="menu-icon-wrap">
          <text class="menu-icon-text">设</text>
        </view>
        <text class="menu-title">系统设置</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="showAbout">
        <view class="menu-icon-wrap">
          <text class="menu-icon-text">关</text>
        </view>
        <text class="menu-title">关于系统</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <view class="logout-btn">
      <u-button type="error" plain @click="handleLogout" customStyle="border-radius: 48rpx; height: 88rpx;">
        退出登录
      </u-button>
    </view>
    </view>
  </view>
</template>

<script>
import { getUser, isLoggedIn, logout } from '@/utils/auth'
import { getWorkerTasks, getOperationLogCount, getPersonalPerformance } from '@/api/user'
import DynamicBackground from '@/components/DynamicBackground'

export default {
  components: {
    DynamicBackground
  },
  data() {
    return {
      userInfo: {},
      todoCount: 0,
      logCount: 0,
      todayCount: 0,
      performance: {
        todayOutput: 0,
        departmentRank: '-',
        departmentTotal: '-',
        rankTrend: 0
      }
    }
  },
  computed: {
    avatarText() {
      return (this.userInfo.realName || '?').substring(0, 1)
    },
    roleText() {
      const roleMap = { 1: '管理员', 2: '订单专员', 3: '采购专员', 4: '仓库管理员', 5: '生产主管', 6: '质检人员', 7: '一线工人' }
      return roleMap[this.userInfo.roleId] || '未知角色'
    }
  },
  onShow() {
    if (!isLoggedIn()) {
      uni.reLaunch({ url: '/pages/login/index' })
      return
    }
    this.userInfo = getUser() || {}
    this.loadStats()
  },
  methods: {
    async loadStats() {
      try {
        const tasks = await getWorkerTasks()
        this.todoCount = (tasks || []).filter(t => t.status === 0 || t.status === 1).length
      } catch { /* ignore */ }
      try {
        const res = await getOperationLogCount()
        this.logCount = res.total || 0
        this.todayCount = res.today || 0
      } catch { /* ignore */ }
      try {
        const res = await getPersonalPerformance()
        this.performance = {
          todayOutput: res.todayOutput || 0,
          departmentRank: res.departmentRank || '-',
          departmentTotal: res.departmentTotal || '-',
          rankTrend: res.rankTrend || 0
        }
      } catch { /* ignore */ }
    },
    goTo(url) {
      uni.navigateTo({ url })
    },
    showAbout() {
      uni.showModal({
        title: '智能手环MES系统',
        content: '版本: v1.0.0\n框架: UniApp + uView Plus\n用途: 生产制造执行系统小程序现场端',
        showCancel: false
      })
    },
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success(res) {
          if (res.confirm) {
            logout()
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.mine-page {
  min-height: 100vh;
  background: transparent;
  padding-bottom: 120rpx;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
}

.mine-header {
  background: linear-gradient(135deg, #3B7CFF 0%, #5B9AFF 40%, #7CB7FF 80%, #9DC8FF 100%);
  padding: 48rpx 32rpx 40rpx;
  display: flex;
  align-items: center;
  border-radius: 0 0 48rpx 48rpx;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -150rpx;
    right: -100rpx;
    width: 350rpx;
    height: 350rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.12);
    animation: float 6s ease-in-out infinite;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -60rpx;
    left: -60rpx;
    width: 200rpx;
    height: 200rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.08);
    animation: float 8s ease-in-out infinite reverse;
  }
}

.avatar-wrap {
  margin-right: 24rpx;
}

.mine-info {
  flex: 1;
}

.mine-name {
  font-size: 36rpx;
  font-weight: 700;
  color: #ffffff;
  display: block;
  margin-bottom: 8rpx;
}

.mine-role {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.75);
  display: inline-block;
  padding: 4rpx 16rpx;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 8rpx;
}

.mine-stats {
  display: flex;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  margin: -24rpx 24rpx 24rpx;
  border-radius: 24rpx;
  padding: 32rpx 0;
  box-shadow: 0 8rpx 32rpx rgba(59, 124, 255, 0.15), 0 2rpx 8rpx rgba(59, 124, 255, 0.08);
  position: relative;
  z-index: 2;
  border: 1rpx solid rgba(255, 255, 255, 0.8);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-divider {
  width: 1rpx;
  height: 60rpx;
  background: #F0F0F0;
  align-self: center;
}

.stat-value {
  font-size: 44rpx;
  font-weight: 700;
  color: #3B7CFF;
  display: block;
  margin-bottom: 8rpx;
  line-height: 1.2;
}

.stat-label {
  font-size: 24rpx;
  color: #8C8C8C;
  display: block;
}

.performance-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  margin: 0 24rpx 24rpx;
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
  margin-bottom: 24rpx;
  display: block;
}

.performance-row {
  display: flex;
  align-items: center;
}

.perf-item {
  flex: 1;
  display: flex;
  align-items: center;
}

.perf-divider {
  width: 1rpx;
  height: 80rpx;
  background: #F0F0F0;
  margin: 0 24rpx;
}

.perf-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;

  &.output-icon { background: #E8F0FF; }
  &.rank-icon { background: #FFFBE6; }
}

.perf-icon-text {
  font-size: 28rpx;
  font-weight: 700;
  display: block;
  line-height: 80rpx;
  text-align: center;

  .output-icon & { color: #3B7CFF; }
  .rank-icon & { color: #FAAD14; }
}

.perf-detail {
  display: flex;
  flex-direction: column;
}

.perf-value-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 6rpx;
}

.perf-value {
  font-size: 34rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.perf-unit {
  font-size: 24rpx;
  color: #8C8C8C;
  margin-left: 4rpx;
}

.perf-label {
  font-size: 24rpx;
  color: #8C8C8C;
}

.perf-label-row {
  display: flex;
  align-items: center;
}

.rank-trend {
  font-size: 22rpx;
  margin-left: 8rpx;
  font-weight: 500;

  &.trend-up { color: #52C41A; }
  &.trend-down { color: #FF4D4F; }
}

.mine-menu {
  margin: 0 24rpx 24rpx;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 30rpx 28rpx;
  border-bottom: 1rpx solid #F2F2F2;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: #F8F8F8;
  }
}

.menu-icon-wrap {
  width: 56rpx;
  height: 56rpx;
  border-radius: 14rpx;
  background: #E8F0FF;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.menu-icon-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #3B7CFF;
}

.menu-title {
  flex: 1;
  font-size: 28rpx;
  color: #1F1F1F;
}

.menu-arrow {
  font-size: 32rpx;
  color: #BFBFBF;
}

.logout-btn {
  margin: 40rpx 24rpx;
}
</style>
