<template>
  <view class="quality-page">
    <DynamicBackground theme="theme-success" />
    <view class="page-content">
      <view class="page-header">
        <view class="page-title-wrap">
          <text class="page-title">质检录入</text>
          <text class="page-desc">IQC来料检验、质检结果录入</text>
        </view>
      </view>

      <view class="func-list">
        <view class="func-item" @click="goTo('/pages/quality/iqc')">
          <view class="func-icon icon-iqc">
            <text class="func-icon-text">检</text>
          </view>
          <view class="func-info">
            <text class="func-title">IQC检验录入</text>
            <text class="func-desc">来料检验结果录入</text>
          </view>
          <text class="func-arrow">›</text>
        </view>

        <view class="func-item" @click="goTo('/pages/quality/task')">
          <view class="func-icon icon-task">
            <text class="func-icon-text">任</text>
          </view>
          <view class="func-info">
            <text class="func-title">质检任务</text>
            <text class="func-desc">查看我的质检任务</text>
          </view>
          <text class="func-arrow">›</text>
        </view>

        <view class="func-item" @click="goTo('/pages/quality/test-dashboard')">
          <view class="func-icon icon-dashboard">
            <text class="func-icon-text">📊</text>
          </view>
          <view class="func-info">
            <text class="func-title">测试数据看板</text>
            <text class="func-desc">功能测试实时数据</text>
          </view>
          <text class="func-arrow">›</text>
        </view>
      </view>

      <view class="stat-section">
        <view class="section-header">
          <text class="section-title">质检统计</text>
          <text class="section-more" @click="goTo('/pages/quality/task')">查看详情</text>
        </view>
        <view class="stat-row">
          <view class="stat-item">
            <text class="stat-value">{{ stats.total || 0 }}</text>
            <text class="stat-label">总任务</text>
          </view>
          <view class="stat-item">
            <text class="stat-value success">{{ stats.passed || 0 }}</text>
            <text class="stat-label">合格</text>
          </view>
          <view class="stat-item">
            <text class="stat-value error">{{ stats.failed || 0 }}</text>
            <text class="stat-label">不合格</text>
          </view>
          <view class="stat-item">
            <text class="stat-value warn">{{ stats.pending || 0 }}</text>
            <text class="stat-label">待处理</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { isLoggedIn } from '@/utils/auth'
import { getQualityStatistics } from '@/api/quality'
import DynamicBackground from '@/components/DynamicBackground'

export default {
  components: {
    DynamicBackground
  },
  data() {
    return {
      stats: { total: 0, passed: 0, failed: 0, pending: 0 }
    }
  },
  onShow() {
    if (!isLoggedIn()) {
      uni.reLaunch({ url: '/pages/login/index' })
      return
    }
    this.loadStats()
  },
  methods: {
    goTo(url) {
      uni.navigateTo({ url })
    },
    async loadStats() {
      try {
        this.stats = await getQualityStatistics() || this.stats
      } catch {
        // ignore
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.quality-page {
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

.func-list {
  padding: 0 $spacing-lg;
  margin-bottom: $spacing-lg;
}

.func-item {
  display: flex;
  align-items: center;
  background: linear-gradient(145deg, #FFFFFF 0%, #FFF7F7 100%);
  border-radius: $radius-card;
  padding: $spacing-lg $spacing-xl;
  margin-bottom: $spacing-md;
  box-shadow: 0 4rpx 16rpx rgba(245, 108, 108, 0.06);
  border-left: 8rpx solid;
  transition: transform $animation-base, box-shadow $animation-base;

  &:nth-child(1) {
    border-left-color: $error-color;
    .func-icon { background: rgba(255,77,79,0.1); }
    .func-icon-text { color: $error-color; }
  }

  &:nth-child(2) {
    border-left-color: $brand-primary;
    .func-icon { background: rgba(59,124,255,0.1); }
    .func-icon-text { color: $brand-primary; }
  }

  &:nth-child(3) {
    border-left-color: $success-color;
    .func-icon { background: rgba(103,194,58,0.1); }
    .func-icon-text { color: $success-color; }
  }

  &:active {
    transform: scale(0.99);
    box-shadow: 0 2rpx 8rpx rgba(245, 108, 108, 0.08);
  }
}

.func-icon {
  width: 84rpx;
  height: 84rpx;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: $spacing-lg;
}

.func-icon-text {
  font-size: $font-size-xl;
  font-weight: 600;
}

.func-info {
  flex: 1;
}

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

.func-arrow {
  font-size: $font-size-xxl;
  color: $text-tertiary;
  font-weight: 300;
}

.stat-section {
  background: linear-gradient(145deg, #FFFFFF 0%, #FFF7F7 100%);
  border-radius: $radius-card;
  padding: $spacing-xl;
  box-shadow: 0 8rpx 32rpx rgba(245, 108, 108, 0.08), 0 2rpx 8rpx rgba(245, 108, 108, 0.04);
  border: 1rpx solid rgba(245, 108, 108, 0.1);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-xl;
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

.stat-row {
  display: flex;
  justify-content: space-around;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  padding: 0 $spacing-sm;

  &::after {
    content: '';
    position: absolute;
    right: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 1rpx;
    height: 50rpx;
    background: $border-light;
  }

  &:last-child::after {
    display: none;
  }
}

.stat-value {
  font-size: $font-size-display;
  font-weight: 700;
  color: $text-primary;
  display: block;
  margin-bottom: $spacing-xs;
  line-height: $line-height-tight;

  &.success { color: $success-color; }
  &.error { color: $error-color; }
  &.warn { color: $warning-color; }
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-tertiary;
  display: block;
}
</style>
