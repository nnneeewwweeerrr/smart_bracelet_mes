<template>
  <view class="scan-page">
    <DynamicBackground theme="theme-primary" />
    <view class="page-content">
    <view class="scan-header">
    <view class="scan-title-wrap">
      <text class="scan-title">扫码操作</text>
      <text class="scan-desc">请选择扫码类型，扫描条码或二维码</text>
    </view>
  </view>

    <view class="scan-actions">
      <view class="scan-card" @click="handleScan('stock/in')">
        <view class="card-icon icon-in">
          <text class="card-icon-text">入</text>
        </view>
        <view class="card-info">
          <text class="card-title">扫码入库</text>
          <text class="card-desc">扫描物料条码进行入库操作</text>
        </view>
        <text class="card-arrow">›</text>
      </view>

      <view class="scan-card" @click="handleScan('stock/out')">
        <view class="card-icon icon-out">
          <text class="card-icon-text">出</text>
        </view>
        <view class="card-info">
          <text class="card-title">扫码出库</text>
          <text class="card-desc">扫描物料条码进行出库操作</text>
        </view>
        <text class="card-arrow">›</text>
      </view>

      <view class="scan-card" @click="handleScan('production/report')">
        <view class="card-icon icon-report">
          <text class="card-icon-text">工</text>
        </view>
        <view class="card-info">
          <text class="card-title">扫码报工</text>
          <text class="card-desc">扫描工单条码进行生产报工</text>
        </view>
        <text class="card-arrow">›</text>
      </view>
    </view>

    <view class="scan-history" v-if="scanHistory.length > 0">
      <view class="section-header">
        <text class="section-title">扫码记录</text>
        <text class="section-more" @click="clearHistory">清空</text>
      </view>
      <view class="history-list">
        <view class="history-item" v-for="(item, index) in scanHistory" :key="index">
          <text class="history-type">{{ item.typeName }}</text>
          <text class="history-code">{{ item.code }}</text>
          <text class="history-time">{{ item.time }}</text>
        </view>
      </view>
    </view>
    </view>
  </view>
</template>

<script>
import { scanCode, navigateByScanResult } from '@/utils/scan'
import { isLoggedIn } from '@/utils/auth'
import DynamicBackground from '@/components/DynamicBackground'

export default {
  components: {
    DynamicBackground
  },
  data() {
    return {
      scanHistory: []
    }
  },
  onShow() {
    if (!isLoggedIn()) {
      uni.reLaunch({ url: '/pages/login/index' })
    }
  },
  methods: {
    async handleScan(type) {
      try {
        const result = await scanCode()
        const typeNameMap = {
          'stock/in': '入库',
          'stock/out': '出库',
          'production/report': '报工'
        }
        this.scanHistory.unshift({
          type,
          typeName: typeNameMap[type] || '扫码',
          code: result,
          time: this.formatTime(new Date())
        })
        if (this.scanHistory.length > 20) {
          this.scanHistory.pop()
        }
        navigateByScanResult(result)
      } catch {
        // cancelled or error
      }
    },
    formatTime(date) {
      const pad = n => String(n).padStart(2, '0')
      return `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
    },
    clearHistory() {
      uni.showModal({
        title: '确认清空',
        content: '确定要清空所有扫码记录吗？',
        success: (res) => {
          if (res.confirm) {
            this.scanHistory = []
            uni.showToast({ title: '已清空', icon: 'success' })
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@keyframes pulse-ring {
  0% {
    box-shadow: 0 0 0 0 rgba(59, 124, 255, 0.3);
  }
  70% {
    box-shadow: 0 0 0 16rpx rgba(59, 124, 255, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(59, 124, 255, 0);
  }
}

@keyframes slide-up {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.scan-page {
  min-height: 100vh;
  background: transparent;
  padding: $spacing-lg;
  padding-bottom: $tabbar-height;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
}

.scan-header {
  padding: $spacing-xl;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: $radius-card;
  margin-bottom: $spacing-lg;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  text-align: center;
  position: relative;
  overflow: hidden;
  border: 1rpx solid rgba(255, 255, 255, 0.8);

  &::before {
    content: '';
    position: absolute;
    top: -180rpx;
    right: -150rpx;
    width: 400rpx;
    height: 400rpx;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(59, 124, 255, 0.1) 0%, transparent 70%);
    animation: float 6s ease-in-out infinite;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -120rpx;
    left: -120rpx;
    width: 280rpx;
    height: 280rpx;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(103, 194, 58, 0.08) 0%, transparent 70%);
    animation: float 8s ease-in-out infinite reverse;
  }
}

.scan-title-wrap {
  position: relative;
  z-index: 1;
}

.scan-title {
  font-size: $font-size-display;
  font-weight: 700;
  color: $text-primary;
  display: block;
  margin-bottom: $spacing-sm;
}

.scan-desc {
  font-size: $font-size-base;
  color: $text-tertiary;
  display: block;
}

.scan-actions {
  margin-bottom: $spacing-lg;
}

.scan-card {
  display: flex;
  align-items: center;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: $radius-card;
  padding: $spacing-xl;
  margin-bottom: $spacing-lg;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
  transition: all $transition-normal;
  border: 1rpx solid rgba(255, 255, 255, 0.8);

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  }

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 8rpx;
    border-radius: 8rpx 0 0 8rpx;
  }

  &:nth-child(1)::before {
    background: linear-gradient(180deg, #52C41A 0%, #73D13D 50%, #95DE64 100%);
  }

  &:nth-child(2)::before {
    background: linear-gradient(180deg, #3B7CFF 0%, #5B9AFF 50%, #7CB7FF 100%);
  }

  &:nth-child(3)::before {
    background: linear-gradient(180deg, #FAAD14 0%, #FFC53D 50%, #FFE082 100%);
  }
}

.card-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: $spacing-lg;
  flex-shrink: 0;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border-radius: $radius-lg;
    border: 2rpx dashed rgba(59, 124, 255, 0.25);
    animation: pulse-ring 2.5s ease-out infinite;
  }
}

.icon-in { background: rgba(103, 194, 58, 0.1); }
.icon-out { background: rgba(64, 158, 255, 0.1); }
.icon-report { background: rgba(230, 162, 60, 0.1); }

.card-icon-text {
  font-size: 40rpx;
  font-weight: 600;
}

.icon-in .card-icon-text { color: $success-color; }
.icon-out .card-icon-text { color: $brand-primary; }
.icon-report .card-icon-text { color: $warning-color; }

.card-info {
  flex: 1;
}

.card-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  display: block;
  margin-bottom: $spacing-xs;
}

.card-desc {
  font-size: $font-size-sm;
  color: $text-tertiary;
  display: block;
}

.card-arrow {
  color: $text-placeholder;
  font-size: $font-size-lg;
}

.scan-history {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: $radius-card;
  padding: $spacing-xl;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
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

  &::before {
    content: '';
    width: 8rpx;
    height: 8rpx;
    border-radius: 50%;
    background: $brand-primary;
  }
}

.section-more {
  font-size: $font-size-sm;
  color: $brand-primary;
}

.history-list {
  counter-reset: list-item;
}

.history-item {
  display: flex;
  align-items: center;
  padding: $spacing-lg 0;
  border-bottom: 1rpx solid $border-light;
  position: relative;
  padding-left: 56rpx;
  animation: slide-up 0.3s ease-out;

  &:last-child {
    border-bottom: none;
  }

  &::before {
    content: counter(list-item);
    counter-increment: list-item;
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 36rpx;
    height: 36rpx;
    border-radius: 50%;
    background: $brand-primary-light-10;
    color: $brand-primary;
    font-size: $font-size-xs;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.history-type {
  font-size: $font-size-base;
  color: $brand-primary;
  font-weight: 500;
  margin-right: $spacing-md;
  flex-shrink: 0;
  padding: $spacing-xs $spacing-sm;
  background: $brand-primary-light-10;
  border-radius: $radius-xs;
}

.history-code {
  font-size: $font-size-base;
  color: $text-secondary;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: monospace;
}

.history-time {
  font-size: $font-size-xs;
  color: $text-placeholder;
  margin-left: $spacing-md;
  flex-shrink: 0;
}
</style>