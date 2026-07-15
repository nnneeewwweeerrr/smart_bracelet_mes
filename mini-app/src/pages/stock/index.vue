<template>
  <view class="stock-page">
    <DynamicBackground theme="theme-primary" />
    <view class="page-content">
    <view class="page-header">
      <view class="header-bg"></view>
      <view class="page-title-wrap">
        <text class="page-title">仓储操作</text>
        <text class="page-desc">库存查询、扫码出入库、盘点</text>
      </view>
    </view>

    <view class="func-grid">
      <view class="func-card card-in" @click="goTo('/pages/stock/scan-in')">
        <view class="card-icon">
          <text class="icon-text">入</text>
        </view>
        <view class="card-info">
          <text class="card-title">扫码入库</text>
          <text class="card-desc">扫描物料条码完成入库</text>
        </view>
        <view class="card-arrow">›</view>
      </view>

      <view class="func-card card-out" @click="goTo('/pages/stock/scan-out')">
        <view class="card-icon">
          <text class="icon-text">出</text>
        </view>
        <view class="card-info">
          <text class="card-title">扫码出库</text>
          <text class="card-desc">扫描物料条码完成出库</text>
        </view>
        <view class="card-arrow">›</view>
      </view>

      <view class="func-card card-search" @click="goTo('/pages/stock/material')">
        <view class="card-icon">
          <text class="icon-text">查</text>
        </view>
        <view class="card-info">
          <text class="card-title">物料查询</text>
          <text class="card-desc">查询物料库存及详情</text>
        </view>
        <view class="card-arrow">›</view>
      </view>

      <view class="func-card card-check" @click="goTo('/pages/stock/check')">
        <view class="card-icon">
          <text class="icon-text">盘</text>
        </view>
        <view class="card-info">
          <text class="card-title">库存盘点</text>
          <text class="card-desc">盘点库存数量差异</text>
        </view>
        <view class="card-arrow">›</view>
      </view>

      <view class="func-card card-record" @click="goTo('/pages/stock/record')">
        <view class="card-icon">
          <text class="icon-text">记</text>
        </view>
        <view class="card-info">
          <text class="card-title">出入库记录</text>
          <text class="card-desc">查看个人操作记录</text>
        </view>
        <view class="card-arrow">›</view>
      </view>
    </view>

    <view class="warning-section" v-if="warnings.length > 0">
      <view class="section-header">
        <text class="section-title">库存预警</text>
        <text class="section-more" @click="goTo('/pages/stock/material')">查看全部</text>
      </view>
      <view class="warning-list">
        <view class="warning-item" v-for="item in warnings" :key="item.id">
          <view class="warning-left">
            <text class="warning-name">{{ item.materialName }}</text>
            <text class="warning-info">库存 {{ item.quantity }} / 安全库存 {{ item.safetyStock }}</text>
          </view>
          <u-tag text="预警" type="error" size="mini"></u-tag>
        </view>
      </view>
    </view>
    </view>
  </view>
</template>

<script>
import { isLoggedIn } from '@/utils/auth'
import { getStockWarning } from '@/api/stock'
import DynamicBackground from '@/components/DynamicBackground'

export default {
  components: {
    DynamicBackground
  },
  data() {
    return {
      warnings: []
    }
  },
  onShow() {
    if (!isLoggedIn()) {
      uni.reLaunch({ url: '/pages/login/index' })
      return
    }
    this.loadWarnings()
  },
  methods: {
    goTo(url) {
      uni.navigateTo({ url })
    },
    async loadWarnings() {
      try {
        this.warnings = await getStockWarning() || []
      } catch {
        this.warnings = []
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.stock-page {
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
  background: linear-gradient(135deg, #3B7CFF 0%, #5B9AFF 40%, #7CB7FF 80%, #9DC8FF 100%);
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
  background: rgba(255, 255, 255, 0.1);
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
  color: rgba(255, 255, 255, 0.7);
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
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
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

.card-in::before { background: linear-gradient(180deg, #52C41A, #73D13D); }
.card-out::before { background: linear-gradient(180deg, #3B7CFF, #5B9AFF); }
.card-search::before { background: linear-gradient(180deg, #FAAD14, #FFC53D); }
.card-check::before { background: linear-gradient(180deg, #FF4D4F, #FF7875); }
.card-record::before { background: linear-gradient(180deg, #646A73, #8D939E); }

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

.card-in .card-icon { background: rgba(103, 194, 58, 0.1); }
.card-out .card-icon { background: rgba(64, 158, 255, 0.1); }
.card-search .card-icon { background: rgba(250, 173, 20, 0.1); }
.card-check .card-icon { background: rgba(245, 108, 108, 0.1); }
.card-record .card-icon { background: rgba(100, 106, 115, 0.1); }

.icon-text {
  font-size: 36rpx;
  font-weight: 700;
}

.card-in .icon-text { color: #52C41A; }
.card-out .icon-text { color: #3B7CFF; }
.card-search .icon-text { color: #FAAD14; }
.card-check .icon-text { color: #FF4D4F; }
.card-record .icon-text { color: #646A73; }

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

.warning-section {
  margin: 0 32rpx 32rpx;
  background: linear-gradient(145deg, #FFFFFF 0%, #FFF7F7 100%);
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(245, 108, 108, 0.08), 0 2rpx 8rpx rgba(245, 108, 108, 0.04);
  border: 1rpx solid rgba(245, 108, 108, 0.1);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
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
    background: #FF4D4F;
  }
}

.section-more {
  font-size: 24rpx;
  color: #8C8C8C;
}

.warning-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.warning-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  background: #FFF7F7;
  border-radius: 16rpx;
  border-left: 4rpx solid #FF4D4F;
  transition: background 0.2s;

  &:active {
    background: #FFF0F0;
  }
}

.warning-left {
  flex: 1;
}

.warning-name {
  font-size: 28rpx;
  color: #1F1F1F;
  display: block;
  margin-bottom: 6rpx;
  font-weight: 500;
}

.warning-info {
  font-size: 24rpx;
  color: #FF4D4F;
  display: block;
}
</style>
