<template>
  <view class="mine-page">
    <view class="banner-area" :style="{ paddingTop: statusBarPadding }">
      <view class="banner-bg"></view>
      <view class="banner-decoration"></view>
      <view class="banner-content">
        <view class="user-info">
          <view class="avatar-wrap">
            <image class="user-avatar" src="/static/头像.jpg" mode="aspectFill"></image>
          </view>
          <view class="user-detail">
            <text class="user-name">{{ userInfo.realName || '未登录' }}</text>
            <text class="user-role">{{ roleText }}</text>
          </view>
        </view>
        <view class="stats-row">
          <view class="stat-item" @click="goTo('/pages/mine/todo')">
            <text class="stat-value">{{ todoCount }}</text>
            <text class="stat-label">待办任务</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item" @click="goTo('/pages/mine/notifications')">
            <text class="stat-value">{{ notificationCount }}</text>
            <text class="stat-label">消息通知</text>
            <u-badge v-if="notificationCount > 0" :count="notificationCount > 99 ? '99+' : notificationCount" type="error" class="stat-badge"></u-badge>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item" @click="goTo('/pages/mine/log')">
            <text class="stat-value">{{ todayCount }}</text>
            <text class="stat-label">今日操作</text>
          </view>
        </view>
      </view>
    </view>

    <view class="main-content">
      <view class="perf-card">
        <view class="card-header">
          <text class="card-title">个人绩效</text>
          <text class="card-sub">今日数据</text>
        </view>
        <view class="perf-grid">
          <view class="perf-item">
            <view class="perf-icon output-icon">
              <text class="icon-text">产</text>
            </view>
            <text class="perf-value">{{ performance.todayOutput }}</text>
            <text class="perf-unit">件</text>
            <text class="perf-label">今日产量</text>
          </view>
          <view class="perf-item">
            <view class="perf-icon rate-icon">
              <text class="icon-text">完</text>
            </view>
            <text class="perf-value">{{ performance.completionRate }}</text>
            <text class="perf-unit">%</text>
            <text class="perf-label">完成率</text>
          </view>
          <view class="perf-item">
            <view class="perf-icon yield-icon">
              <text class="icon-text">良</text>
            </view>
            <text class="perf-value">{{ performance.yieldRate }}</text>
            <text class="perf-unit">%</text>
            <text class="perf-label">良品率</text>
          </view>
          <view class="perf-item">
            <view class="perf-icon rank-icon">
              <text class="icon-text">排</text>
            </view>
            <text class="perf-value">第{{ performance.departmentRank }}</text>
            <text class="perf-unit">名</text>
            <text class="perf-label">部门排名</text>
          </view>
        </view>
      </view>

      <view class="menu-section">
        <view class="menu-item" @click="goTo('/pages/mine/log')">
          <view class="menu-icon log-icon">
            <text class="menu-icon-text">记</text>
          </view>
          <text class="menu-title">操作记录</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goTo('/pages/mine/profile')">
          <view class="menu-icon profile-icon">
            <text class="menu-icon-text">人</text>
          </view>
          <text class="menu-title">个人信息</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goTo('/pages/mine/notifications')">
          <view class="menu-icon notify-icon">
            <text class="menu-icon-text">信</text>
          </view>
          <text class="menu-title">消息通知</text>
          <u-badge v-if="notificationCount > 0" :count="notificationCount > 99 ? '99+' : notificationCount" type="error" class="menu-badge"></u-badge>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="goTo('/pages/mine/todo')">
          <view class="menu-icon todo-icon">
            <text class="menu-icon-text">事</text>
          </view>
          <text class="menu-title">待办任务</text>
          <u-badge v-if="todoCount > 0" :count="todoCount" type="error" class="menu-badge"></u-badge>
          <text class="menu-arrow">›</text>
        </view>
      </view>

      <view class="menu-section">
        <view class="menu-item" @click="goTo('/pages/mine/settings')">
          <view class="menu-icon settings-icon">
            <text class="menu-icon-text">设</text>
          </view>
          <text class="menu-title">系统设置</text>
          <text class="menu-arrow">›</text>
        </view>
        <view class="menu-item" @click="showAbout">
          <view class="menu-icon about-icon">
            <text class="menu-icon-text">关</text>
          </view>
          <text class="menu-title">关于系统</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>

      <view class="logout-section">
        <view class="logout-btn" @click="handleLogout">
          <text class="logout-text">退出登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getUser, isLoggedIn, logout } from '@/utils/auth'
import { getOperationLogCount, getPersonalPerformance, getNotificationCount } from '@/api/user'
import { getWorkOrderList } from '@/api/production'
import { getQualityTaskList } from '@/api/quality'
import { getExceptionList } from '@/api/exception'

export default {
  data() {
    return {
      userInfo: {},
      todoCount: 0,
      logCount: 0,
      todayCount: 0,
      notificationCount: 0,
      statusBarPadding: '88rpx',
      performance: {
        todayOutput: 156,
        completionRate: 92,
        yieldRate: 98.5,
        departmentRank: 3,
        departmentTotal: 25,
        rankTrend: 2
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
  onLoad() {
    const sysInfo = uni.getSystemInfoSync()
    const statusBarHeight = sysInfo.statusBarHeight || 44
    this.statusBarPadding = `calc(${statusBarHeight}px + 40rpx)`
  },
  onShow() {
    if (!isLoggedIn()) {
      uni.reLaunch({ url: '/pages/login/index' })
      return
    }
    this.userInfo = getUser() || {}
    this.loadStats()
    uni.$on('notificationUpdate', this.loadStats)
    uni.$on('todoUpdate', this.loadStats)
  },
  onHide() {
    uni.$off('notificationUpdate', this.loadStats)
    uni.$off('todoUpdate', this.loadStats)
  },
  methods: {
    async loadStats() {
      let todoCount = 0
      try {
        const res = await getWorkOrderList({ status: 1, page: 1, size: 50 })
        const orders = (res && res.records) || res || []
        todoCount += orders.length
      } catch { /* ignore */ }
      try {
        const res = await getQualityTaskList({ status: 0, page: 1, size: 50 })
        const tasks = (res && res.records) || res || []
        todoCount += tasks.length
      } catch { /* ignore */ }
      try {
        const res = await getExceptionList({ status: 0, page: 1, size: 50 })
        const exceptions = (res && res.records) || res || []
        todoCount += exceptions.length
      } catch { /* ignore */ }
      this.todoCount = todoCount
      try {
        const res = await getOperationLogCount()
        this.logCount = res.total || 0
        this.todayCount = res.today || 0
      } catch { /* ignore */ }
      try {
        const res = await getPersonalPerformance()
        this.performance = {
          todayOutput: res.todayOutput || 0,
          completionRate: res.todayPassRate != null ? Number(res.todayPassRate).toFixed(1) : '0.0',
          yieldRate: res.todayPassRate != null ? Number(res.todayPassRate).toFixed(1) : '0.0',
          departmentRank: res.departmentRank || '-',
          departmentTotal: res.departmentTotal || '-',
          rankTrend: res.rankTrend || 0
        }
      } catch { /* ignore */ }
      try {
        const res = await getNotificationCount()
        this.notificationCount = res.unreadCount || 0
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
  background: #F5F7FA;
}

.banner-area {
  position: relative;
  padding-top: calc(env(safe-area-inset-top) + 40rpx);
  padding-left: 36rpx;
  padding-right: 36rpx;
  padding-bottom: 40rpx;
  overflow: hidden;
}

.banner-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #3B7CFF 0%, #5B9AFF 30%, #7CB7FF 60%, #9DC8FF 100%);
}

.banner-decoration {
  position: absolute;
  top: -100rpx;
  right: -80rpx;
  width: 350rpx;
  height: 350rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.banner-content {
  position: relative;
  z-index: 1;
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 32rpx;
}

.avatar-wrap {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.5);
  overflow: hidden;
}

.user-avatar {
  width: 100%;
  height: 100%;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 40rpx;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 8rpx;
}

.user-role {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.2);
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
  align-self: flex-start;
}

.stats-row {
  display: flex;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 32rpx;
  padding: 24rpx;
  backdrop-filter: blur(10px);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.stat-divider {
  width: 2rpx;
  height: 50rpx;
  background: rgba(255, 255, 255, 0.3);
}

.stat-value {
  font-size: 44rpx;
  font-weight: 700;
  color: #ffffff;
}

.stat-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 4rpx;
}

.stat-badge {
  position: absolute;
  top: -8rpx;
  right: 20rpx;
}

.main-content {
  padding: 0 32rpx;
  margin-top: -20rpx;
}

.perf-card {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.06), 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.card-sub {
  font-size: 24rpx;
  color: #999;
}

.perf-grid {
  display: flex;
  gap: 20rpx;
}

.perf-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 12rpx;
  background: #FAFBFC;
  border-radius: 16rpx;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.97);
  }
}

.perf-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}

.output-icon { background: linear-gradient(135deg, rgba(59, 124, 255, 0.15) 0%, rgba(59, 124, 255, 0.05) 100%); }
.rate-icon { background: linear-gradient(135deg, rgba(250, 173, 20, 0.15) 0%, rgba(250, 173, 20, 0.05) 100%); }
.yield-icon { background: linear-gradient(135deg, rgba(82, 196, 26, 0.15) 0%, rgba(82, 196, 26, 0.05) 100%); }
.rank-icon { background: linear-gradient(135deg, rgba(147, 112, 219, 0.15) 0%, rgba(147, 112, 219, 0.05) 100%); }

.icon-text {
  font-size: 28rpx;
  font-weight: 600;
}

.output-icon .icon-text { color: #3B7CFF; }
.rate-icon .icon-text { color: #FAAD14; }
.yield-icon .icon-text { color: #52C41A; }
.rank-icon .icon-text { color: #9370DB; }

.perf-value {
  font-size: 32rpx;
  font-weight: 700;
  color: #333;
}

.perf-unit {
  font-size: 20rpx;
  color: #999;
  margin-left: 4rpx;
}

.perf-label {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
}

.menu-section {
  background: #ffffff;
  border-radius: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.06), 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 28rpx;
  border-bottom: 1rpx solid #F5F5F5;
  position: relative;
  transition: all 0.3s ease;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: #FAFBFC;
    transform: scale(0.95);
  }
}

.menu-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.log-icon { background: linear-gradient(135deg, #3B7CFF 0%, #5B9AFF 100%); }
.profile-icon { background: linear-gradient(135deg, #52C41A 0%, #389E0D 100%); }
.notify-icon { background: linear-gradient(135deg, #FAAD14 0%, #D48806 100%); }
.todo-icon { background: linear-gradient(135deg, #FF6B6B 0%, #FF4D4F 100%); }
.settings-icon { background: linear-gradient(135deg, #9370DB 0%, #722ED1 100%); }
.about-icon { background: linear-gradient(135deg, #13C2C2 0%, #08979C 100%); }

.menu-icon-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #ffffff;
}

.menu-title {
  flex: 1;
  font-size: 30rpx;
  color: #333;
}

.menu-arrow {
  font-size: 36rpx;
  color: #CCC;
}

.menu-badge {
  position: absolute;
  right: 80rpx;
  top: 20rpx;
}

.logout-section {
  padding: 40rpx 0 calc(env(safe-area-inset-bottom) + 100rpx);
}

.logout-btn {
  background: #ffffff;
  border-radius: 44rpx;
  padding: 28rpx;
  text-align: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.03);
  border: 2rpx solid #FFE0E0;

  &:active {
    background: #FFF5F5;
  }
}

.logout-text {
  font-size: 30rpx;
  color: #FF4D4F;
  font-weight: 500;
}
</style>
