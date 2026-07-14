<template>
  <view class="exception-page">
    <DynamicBackground theme="theme-error" />
    <view class="page-content">
    <view class="page-header">
      <view class="page-title-wrap">
        <text class="page-title">异常管理</text>
        <text class="page-desc">异常上报、待处理异常</text>
      </view>
    </view>

    <view class="func-grid">
      <view class="func-card" @click="goTo('/pages/exception/create')">
        <view class="func-icon icon-report">
          <text class="func-icon-text">报</text>
        </view>
        <text class="func-title">异常上报</text>
        <text class="func-desc">上报生产异常</text>
      </view>

      <view class="func-card" @click="goTo('/pages/exception/pending')">
        <view class="func-icon icon-pending">
          <text class="func-icon-text">待</text>
        </view>
        <text class="func-title">待处理异常</text>
        <text class="func-desc">查看待处理异常</text>
        <u-badge v-if="pendingCount > 0" :count="pendingCount" :offset="[6, -6]"></u-badge>
      </view>
    </view>

    <view class="recent-section">
      <view class="section-header">
        <text class="section-title">最近上报</text>
        <text class="section-more" @click="goTo('/pages/exception/pending')">查看全部</text>
      </view>
      <view v-if="recentList.length === 0">
        <u-empty mode="data" text="暂无异常记录" iconSize="80"></u-empty>
      </view>
      <view class="recent-list" v-else>
        <view class="recent-card" v-for="item in recentList" :key="item.id">
        <view class="recent-header">
          <text class="recent-no">{{ item.exceptionNo }}</text>
          <u-tag :text="item.statusText" :type="item.statusType" size="mini"></u-tag>
        </view>
        <text class="recent-type">{{ item.exceptionTypeName }}</text>
        <text class="recent-desc">{{ item.description }}</text>
        <text class="recent-time">{{ item.reportTime }}</text>
        </view>
      </view>
    </view>
    </view>
  </view>
</template>

<script>
import { isLoggedIn } from '@/utils/auth'
import { getExceptionList } from '@/api/exception'
import DynamicBackground from '@/components/DynamicBackground'

export default {
  components: {
    DynamicBackground
  },
  data() {
    return {
      pendingCount: 0,
      recentList: []
    }
  },
  onShow() {
    if (!isLoggedIn()) {
      uni.reLaunch({ url: '/pages/login/index' })
      return
    }
    this.loadData()
  },
  methods: {
    goTo(url) {
      uni.navigateTo({ url })
    },
    async loadData() {
      try {
        const res = await getExceptionList({ page: 1, size: 10 })
        const records = (res && res.records) || res || []
        this.recentList = records.map(item => ({
          ...item,
          statusText: this.getStatusText(item.status),
          statusType: this.getStatusType(item.status),
          exceptionTypeName: this.getTypeName(item.exceptionType)
        }))
        this.pendingCount = records.filter(r => r.status === 0).length
      } catch {
        this.recentList = []
      }
    },
    getStatusText(s) {
      const m = { 0: '待处理', 1: '处理中', 2: '已解决' }
      return m[s] || '未知'
    },
    getStatusType(s) {
      const m = { 0: 'error', 1: 'warning', 2: 'success' }
      return m[s] || 'info'
    },
    getTypeName(t) {
      const m = { equipment: '设备故障', material: '物料短缺', quality: '质量问题', other: '其他异常' }
      return m[t] || t || '未知'
    }
  }
}
</script>

<style lang="scss" scoped>
.exception-page {
  min-height: 100vh;
  background: #F5F7FA;
  padding: $spacing-lg;
  padding-bottom: $tabbar-height;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
}

.page-header {
  padding: $spacing-xl $spacing-lg $spacing-lg;
  background: linear-gradient(145deg, #FFFFFF 0%, #FFF7F7 100%);
  border-radius: $radius-card;
  box-shadow: 0 8rpx 32rpx rgba(245, 108, 108, 0.08), 0 2rpx 8rpx rgba(245, 108, 108, 0.04);
  margin-bottom: $spacing-lg;
  position: relative;
  overflow: hidden;
  border: 1rpx solid rgba(245, 108, 108, 0.1);

  &::before {
    content: '';
    position: absolute;
    top: -100rpx;
    right: -80rpx;
    width: 280rpx;
    height: 280rpx;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(245, 108, 108, 0.08) 0%, transparent 70%);
    animation: float 6s ease-in-out infinite;
  }
}

.page-title-wrap {
  position: relative;
  z-index: 1;
}

.page-title {
  font-size: $font-size-xxl;
  font-weight: 700;
  color: $text-primary;
  display: block;
  margin-bottom: $spacing-xs;
}

.page-desc {
  font-size: $font-size-base;
  color: $text-secondary;
  display: block;
}

.func-grid {
  display: flex;
  margin: 0 -$spacing-sm;
  margin-bottom: $spacing-lg;
}

.func-card {
  width: calc(50% - #{$spacing-md});
  margin: 0 $spacing-sm;
  background: linear-gradient(145deg, #FFFFFF 0%, #FFF7F7 100%);
  border-radius: $radius-card;
  padding: $spacing-xl;
  box-shadow: 0 8rpx 32rpx rgba(245, 108, 108, 0.08), 0 2rpx 8rpx rgba(245, 108, 108, 0.04);
  position: relative;
  overflow: hidden;
  transition: transform $animation-base, box-shadow $animation-base;
  border: 1rpx solid rgba(245, 108, 108, 0.1);

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 16rpx rgba(245, 108, 108, 0.1);
  }

  &:nth-child(1)::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 8rpx;
    background: linear-gradient(90deg, #FF4D4F 0%, #FF7875 50%, #FF9E9E 100%);
    border-radius: 8rpx 8rpx 0 0;
  }

  &:nth-child(2)::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 8rpx;
    background: linear-gradient(90deg, #FAAD14 0%, #FFC53D 50%, #FFE082 100%);
    border-radius: 8rpx 8rpx 0 0;
  }
}

.func-icon {
  width: 84rpx;
  height: 84rpx;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: $spacing-md;
}

.icon-report { background: rgba(255,77,79,0.1); }
.icon-pending { background: rgba(250,173,20,0.1); }

.func-icon-text {
  font-size: $font-size-xl;
  font-weight: 600;
}

.icon-report .func-icon-text { color: $error-color; }
.icon-pending .func-icon-text { color: $warning-color; }

.func-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  display: block;
  margin-bottom: $spacing-xs;
}

.func-desc {
  font-size: $font-size-sm;
  color: $text-tertiary;
  display: block;
  line-height: $line-height-normal;
}

.recent-section {
  background: $bg-card;
  border-radius: $radius-card;
  padding: $spacing-xl;
  box-shadow: $shadow-card;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
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
    background: $error-color;
    border-radius: 2rpx;
  }
}

.section-more {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.recent-card {
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-light;
  border-radius: $radius-sm;
  transition: background $animation-fast;

  &:active {
    background: $bg-hover;
  }

  &:last-child {
    border-bottom: none;
  }
}

.recent-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.recent-no {
  font-size: $font-size-base;
  color: $brand-primary;
  font-weight: 600;
}

.recent-type {
  font-size: $font-size-sm;
  color: $warning-color;
  display: block;
  margin-bottom: $spacing-xs;
  font-weight: 500;
}

.recent-desc {
  font-size: $font-size-base;
  color: $text-primary;
  display: block;
  margin-bottom: $spacing-xs;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: $line-height-normal;
}

.recent-time {
  font-size: $font-size-xs;
  color: $text-tertiary;
  display: block;
}
</style>
