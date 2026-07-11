<template>
  <view class="production-page">
    <DynamicBackground theme="theme-warm" />
    <view class="page-content">
    <view class="page-header">
      <view class="header-bg"></view>
      <view class="page-title-wrap">
        <text class="page-title">生产报工</text>
        <text class="page-desc">工单管理、工序报工、SOP查看</text>
      </view>
    </view>

    <view class="func-grid">
      <view class="func-card card-order" @click="goTo('/pages/production/workorder')">
        <view class="card-icon">
          <text class="icon-text">单</text>
        </view>
        <view class="card-info">
          <text class="card-title">我的工单</text>
          <text class="card-desc">查看分配给我的工单</text>
        </view>
        <view class="card-arrow">›</view>
      </view>

      <view class="func-card card-report" @click="goTo('/pages/production/report')">
        <view class="card-icon">
          <text class="icon-text">工</text>
        </view>
        <view class="card-info">
          <text class="card-title">工序报工</text>
          <text class="card-desc">扫码或选择工单报工</text>
        </view>
        <view class="card-arrow">›</view>
      </view>

      <view class="func-card card-sop" @click="goTo('/pages/production/sop')">
        <view class="card-icon">
          <text class="icon-text">S</text>
        </view>
        <view class="card-info">
          <text class="card-title">SOP查看</text>
          <text class="card-desc">查看标准作业指导书</text>
        </view>
        <view class="card-arrow">›</view>
      </view>
    </view>

    <view class="today-section">
      <view class="section-header">
        <text class="section-title">今日报工</text>
        <text class="section-more" @click="goTo('/pages/production/report')">查看全部</text>
      </view>
      <view class="stat-card">
        <view class="stat-item">
          <view class="stat-icon-wrap stat-green">
            <text class="stat-icon">✓</text>
          </view>
          <text class="stat-value">{{ todayReport.goodQuantity || 0 }}</text>
          <text class="stat-label">合格数量</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <view class="stat-icon-wrap stat-red">
            <text class="stat-icon">✕</text>
          </view>
          <text class="stat-value error">{{ todayReport.badQuantity || 0 }}</text>
          <text class="stat-label">不合格数量</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <view class="stat-icon-wrap stat-blue">
            <text class="stat-icon">✎</text>
          </view>
          <text class="stat-value">{{ todayReport.orderCount || 0 }}</text>
          <text class="stat-label">报工次数</text>
        </view>
      </view>
    </view>
    </view>
  </view>
</template>

<script>
import { isLoggedIn } from '@/utils/auth'
import { getWorkOrderList } from '@/api/production'
import DynamicBackground from '@/components/DynamicBackground'

export default {
  components: {
    DynamicBackground
  },
  data() {
    return {
      todayReport: {
        goodQuantity: 0,
        badQuantity: 0,
        orderCount: 0
      }
    }
  },
  onShow() {
    if (!isLoggedIn()) {
      uni.reLaunch({ url: '/pages/login/index' })
      return
    }
    this.loadTodayReport()
  },
  methods: {
    goTo(url) {
      uni.navigateTo({ url })
    },
    async loadTodayReport() {
      try {
        const res = await getWorkOrderList({ status: 1, page: 1, size: 100 })
        const orders = (res && res.records) || res || []
        this.todayReport.orderCount = orders.length
      } catch {
        // ignore
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.production-page {
  min-height: 100vh;
  background: transparent;
  padding: 0;
  padding-bottom: $tabbar-height;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
}

.page-header {
  padding: 40rpx 32rpx 32rpx;
  background: linear-gradient(135deg, #FAAD14 0%, #FFC53D 40%, #FFD666 80%, #FFE082 100%);
  border-radius: 0 0 48rpx 48rpx;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -120rpx;
    right: -80rpx;
    width: 300rpx;
    height: 300rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    animation: float 6s ease-in-out infinite;
  }
}

.header-bg {
  position: absolute;
  top: -80rpx;
  right: -80rpx;
  width: 240rpx;
  height: 240rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
}

.page-title-wrap {
  position: relative;
  z-index: 1;
}

.page-title {
  font-size: 44rpx;
  font-weight: 700;
  color: #ffffff;
  display: block;
  margin-bottom: 8rpx;
  letter-spacing: 2rpx;
}

.page-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  display: block;
}

.func-grid {
  padding: 32rpx;
  margin-top: -24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.func-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #FFFDF8 100%);
  border-radius: 24rpx;
  padding: 28rpx 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  border: 1rpx solid rgba(255, 255, 255, 0.8);

  &:active {
    transform: scale(0.99);
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
}

.card-order::before { background: linear-gradient(180deg, #3B7CFF, #5B9AFF); }
.card-report::before { background: linear-gradient(180deg, #FAAD14, #FFC53D); }
.card-sop::before { background: linear-gradient(180deg, #52C41A, #73D13D); }

.card-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.card-order .card-icon { background: rgba(64, 158, 255, 0.1); }
.card-report .card-icon { background: rgba(250, 173, 20, 0.1); }
.card-sop .card-icon { background: rgba(103, 194, 58, 0.1); }

.icon-text {
  font-size: 36rpx;
  font-weight: 700;
}

.card-order .icon-text { color: #3B7CFF; }
.card-report .icon-text { color: #FAAD14; }
.card-sop .icon-text { color: #52C41A; }

.card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.card-desc {
  font-size: 24rpx;
  color: #8C8C8C;
}

.card-arrow {
  font-size: 36rpx;
  color: #D9D9D9;
  margin-left: 16rpx;
  flex-shrink: 0;
}

.today-section {
  margin: 0 32rpx 32rpx;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: flex;
  align-items: center;
  gap: 12rpx;

  &::before {
    content: '';
    width: 8rpx;
    height: 8rpx;
    border-radius: 50%;
    background: #FAAD14;
  }
}

.section-more {
  font-size: 24rpx;
  color: #8C8C8C;
}

.stat-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #FFFDF8 100%);
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  display: flex;
  justify-content: space-around;
  align-items: center;
  border: 1rpx solid rgba(255, 255, 255, 0.8);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stat-icon-wrap {
  width: 56rpx;
  height: 56rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}

.stat-green { background: rgba(103, 194, 58, 0.1); }
.stat-red { background: rgba(245, 108, 108, 0.1); }
.stat-blue { background: rgba(64, 158, 255, 0.1); }

.stat-icon {
  font-size: 28rpx;
  font-weight: 600;
}

.stat-green .stat-icon { color: #52C41A; }
.stat-red .stat-icon { color: #FF4D4F; }
.stat-blue .stat-icon { color: #3B7CFF; }

.stat-value {
  font-size: 44rpx;
  font-weight: 700;
  color: #1F1F1F;
  display: block;
  margin-bottom: 6rpx;
  line-height: 1;

  &.error {
    color: #FF4D4F;
  }
}

.stat-label {
  font-size: 24rpx;
  color: #8C8C8C;
  display: block;
}

.stat-divider {
  width: 1rpx;
  height: 80rpx;
  background: #F0F2F5;
}
</style>
