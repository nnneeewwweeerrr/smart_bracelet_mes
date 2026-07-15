<template>
  <view class="apps-page">
    <view class="page-header">
      <view class="header-bg"></view>
      <view class="page-title-wrap">
        <text class="page-title">应用中心</text>
        <text class="page-desc">全部功能入口</text>
      </view>
    </view>

    <scroll-view scroll-y class="apps-scroll">
      <view class="apps-list">
        <view class="app-card" :class="'card-' + index" v-for="(app, index) in appList" :key="index" @click="goTo(app.url)">
          <view class="app-icon" :class="'icon-' + index">
            <text class="app-icon-text">{{ app.icon }}</text>
          </view>
          <view class="app-info">
            <text class="app-name">{{ app.name }}</text>
            <text class="app-desc">{{ app.desc }}</text>
          </view>
          <view class="app-arrow">›</view>
        </view>
      </view>

      <view class="footer-space"></view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  name: 'AppsPage',
  data() {
    return {
      appList: [
        { icon: '扫', name: '扫码报工', desc: '扫码入库、出库、报工', url: '/pages/scan/index', iconBg: 'linear-gradient(135deg, #667EEA, #764BA2)' },
        { icon: '仓', name: '仓储管理', desc: '出入库、库存查询、盘点', url: '/pages/stock/index', iconBg: 'linear-gradient(135deg, #11998E, #38EF7D)' },
        { icon: '工', name: '生产报工', desc: '工单管理、工序报工', url: '/pages/production/index', iconBg: 'linear-gradient(135deg, #F093FB, #F5576C)' },
        { icon: '检', name: '质检录入', desc: 'IQC检验、质检任务', url: '/pages/quality/index', iconBg: 'linear-gradient(135deg, #4FACFE, #00F2FE)' },
        { icon: '异', name: '异常上报', desc: '生产异常上报处理', url: '/pages/exception/index', iconBg: 'linear-gradient(135deg, #FA709A, #FEE140)' },
        { icon: '统', name: '生产统计', desc: '产量趋势、合格率统计', url: '/pages/workbench/stats', iconBg: 'linear-gradient(135deg, #A8EDEA, #FED6E3)' }
      ]
    }
  },
  methods: {
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
.apps-page {
  min-height: 100vh;
  background: #F5F7FA;
  display: flex;
  flex-direction: column;
  position: relative;
}

.page-header {
  position: relative;
  padding: 70rpx 40rpx 50rpx;
  background: linear-gradient(135deg, #4F46E5 0%, #7C3AED 30%, #A78BFA 60%, #C4B5FD 100%);
  border-radius: 0 0 50rpx 50rpx;
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

  .page-title-wrap {
    position: relative;
    z-index: 1;

    .page-title {
      display: block;
      font-size: 52rpx;
      font-weight: 700;
      color: #FFFFFF;
      margin-bottom: 8rpx;
    }

    .page-desc {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.8);
    }
  }
}

.apps-scroll {
  flex: 1;
  padding: 32rpx;
  margin-top: -24rpx;
}

.apps-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.app-card {
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

.card-0::before { background: linear-gradient(180deg, #52C41A, #73D13D); }
.card-1::before { background: linear-gradient(180deg, #3B7CFF, #5B9AFF); }
.card-2::before { background: linear-gradient(180deg, #FAAD14, #FFC53D); }
.card-3::before { background: linear-gradient(180deg, #FF4D4F, #FF7875); }
.card-4::before { background: linear-gradient(180deg, #722ED1, #9254DE); }
.card-5::before { background: linear-gradient(180deg, #13C2C2, #36CFCC); }

.app-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.icon-0 { background: rgba(103, 194, 58, 0.1); }
.icon-1 { background: rgba(64, 158, 255, 0.1); }
.icon-2 { background: rgba(250, 173, 20, 0.1); }
.icon-3 { background: rgba(245, 108, 108, 0.1); }
.icon-4 { background: rgba(114, 46, 209, 0.1); }
.icon-5 { background: rgba(19, 194, 194, 0.1); }

.app-icon-text {
  font-size: 36rpx;
  font-weight: 700;
}

.icon-0 .app-icon-text { color: #52C41A; }
.icon-1 .app-icon-text { color: #3B7CFF; }
.icon-2 .app-icon-text { color: #FAAD14; }
.icon-3 .app-icon-text { color: #FF4D4F; }
.icon-4 .app-icon-text { color: #722ED1; }
.icon-5 .app-icon-text { color: #13C2C2; }

.app-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.app-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.app-desc {
  font-size: 24rpx;
  color: #8C8C8C;
}

.app-arrow {
  font-size: 36rpx;
  color: #D9D9D9;
  margin-left: 16rpx;
  flex-shrink: 0;
}

.footer-space {
  height: 60rpx;
}
</style>