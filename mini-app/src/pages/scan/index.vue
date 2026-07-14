<template>
  <view class="scan-page">
    <view class="page-content">
      <view class="scan-header">
        <view class="scan-title-wrap">
          <text class="scan-title">扫码操作</text>
          <text class="scan-desc">扫码查信息 → 扫码看规范 → 扫码报异常</text>
        </view>
        <view class="quick-scan-btn" @click="handleQuickScan">
          <view class="scan-circle">
            <text class="scan-icon">扫</text>
          </view>
          <text class="quick-scan-text">快捷扫码</text>
        </view>
      </view>

      <view class="feature-section">
        <view class="section-header">
          <text class="section-title">核心功能</text>
        </view>
        
        <view class="feature-grid">
          <view class="feature-card" @click="goTrace">
            <view class="feature-icon-wrap blue">
              <text class="feature-icon">溯</text>
            </view>
            <text class="feature-title">SN码追溯</text>
            <text class="feature-desc">扫描成品SN码，查看完整产品履历</text>
            <view class="feature-badge">一物一码</view>
          </view>
          
          <view class="feature-card" @click="goSop">
            <view class="feature-icon-wrap green">
              <text class="feature-icon">规</text>
            </view>
            <text class="feature-title">工位SOP</text>
            <text class="feature-desc">扫工位码，查看电子作业指导书</text>
            <view class="feature-badge">标准作业</view>
          </view>
          
          <view class="feature-card" @click="goException">
            <view class="feature-icon-wrap orange">
              <text class="feature-icon">异</text>
            </view>
            <text class="feature-title">异常快报</text>
            <text class="feature-desc">拍照+语音，快速上报现场异常</text>
            <view class="feature-badge">实时响应</view>
          </view>
        </view>
      </view>

      <view class="scan-actions">
        <view class="section-header">
          <text class="section-title">仓储操作</text>
        </view>
        
        <view class="action-list">
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
        </view>
      </view>

      <view class="scan-actions">
        <view class="section-header">
          <text class="section-title">生产操作</text>
        </view>
        
        <view class="action-list">
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

          <view class="scan-card" @click="handleScan('quality/check')">
            <view class="card-icon icon-check">
              <text class="card-icon-text">检</text>
            </view>
            <view class="card-info">
              <text class="card-title">质检扫码</text>
              <text class="card-desc">扫描产品条码进行质量检验</text>
            </view>
            <text class="card-arrow">›</text>
          </view>
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
import { scanCode, navigateByScanResult, parseScanResult, getCodeTypeName } from '@/utils/scan'
import { isLoggedIn } from '@/utils/auth'

export default {
  components: {},
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
    async handleQuickScan() {
      try {
        const result = await scanCode()
        const parsed = parseScanResult(result)
        const typeName = parsed ? getCodeTypeName(parsed.type) : '扫码'
        
        this.scanHistory.unshift({
          type: parsed?.type || 'quick',
          typeName: typeName,
          code: result,
          time: this.formatTime(new Date())
        })
        if (this.scanHistory.length > 20) {
          this.scanHistory.pop()
        }
        navigateByScanResult(result)
      } catch {
      }
    },
    async handleScan(type) {
      try {
        const result = await scanCode()
        const parsed = parseScanResult(result)
        const typeName = parsed ? getCodeTypeName(parsed.type) : '扫码'
        
        this.scanHistory.unshift({
          type: parsed?.type || type,
          typeName: typeName,
          code: result,
          time: this.formatTime(new Date())
        })
        if (this.scanHistory.length > 20) {
          this.scanHistory.pop()
        }
        navigateByScanResult(result)
      } catch {
      }
    },
    goTrace() {
      uni.navigateTo({ url: '/pages/production/trace' })
    },
    goSop() {
      uni.navigateTo({ url: '/pages/production/sop' })
    },
    goException() {
      uni.navigateTo({ url: '/pages/exception/create' })
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
    box-shadow: 0 0 0 20rpx rgba(59, 124, 255, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(59, 124, 255, 0);
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20rpx); }
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
  background: #F5F7FA;
  padding: 20rpx 24rpx;
  padding-bottom: 180rpx;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
}

.scan-header {
  padding: 40rpx 30rpx;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  text-align: center;
  position: relative;
  overflow: hidden;
  border: 1rpx solid rgba(255, 255, 255, 0.8);
}

.scan-title-wrap {
  position: relative;
  z-index: 1;
  margin-bottom: 30rpx;
}

.scan-title {
  font-size: 38rpx;
  font-weight: 700;
  color: #1F1F1F;
  display: block;
  margin-bottom: 12rpx;
}

.scan-desc {
  font-size: 24rpx;
  color: #3B7CFF;
  display: block;
}

.quick-scan-btn {
  position: relative;
  z-index: 1;
  animation: slide-up 0.5s ease-out;
}

.scan-circle {
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #3B7CFF 0%, #5B9BFF 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  box-shadow: 0 12rpx 32rpx rgba(59, 124, 255, 0.4);
  position: relative;
  animation: pulse-ring 2s ease-out infinite;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.95);
    box-shadow: 0 6rpx 16rpx rgba(59, 124, 255, 0.3);
  }
}

.scan-icon {
  font-size: 48rpx;
  font-weight: 600;
  color: #ffffff;
}

.quick-scan-text {
  font-size: 24rpx;
  color: #8C8C8C;
  display: block;
  margin-top: 16rpx;
}

.feature-section {
  margin-bottom: 24rpx;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: flex;
  align-items: center;
  gap: 8rpx;

  &::before {
    content: '';
    width: 8rpx;
    height: 28rpx;
    background: linear-gradient(180deg, #3B7CFF 0%, #5B9BFF 100%);
    border-radius: 4rpx;
  }
}

.section-desc {
  font-size: 22rpx;
  color: #3B7CFF;
  margin-left: 12rpx;
  padding: 4rpx 12rpx;
  background: rgba(59, 124, 255, 0.1);
  border-radius: 8rpx;
}

.section-more {
  font-size: 24rpx;
  color: #3B7CFF;
}

.feature-grid {
  display: flex;
  gap: 16rpx;
}

.feature-card {
  flex: 1;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 24rpx;
  padding: 24rpx 16rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
  text-align: center;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.96);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  }
}

.feature-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12rpx;

  &.blue { background: linear-gradient(135deg, #3B7CFF 0%, #5B9BFF 100%); }
  &.green { background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%); }
  &.orange { background: linear-gradient(135deg, #FAAD14 0%, #FFC53D 100%); }
}

.feature-icon {
  font-size: 28rpx;
  font-weight: 600;
  color: #ffffff;
}

.feature-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
  margin-bottom: 8rpx;
}

.feature-desc {
  font-size: 20rpx;
  color: #8C8C8C;
  display: block;
  line-height: 1.4;
}

.feature-badge {
  font-size: 18rpx;
  color: #ffffff;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  display: inline-block;
  margin-top: 12rpx;
  background: rgba(0, 0, 0, 0.5);
}

.scan-actions {
  margin-bottom: 24rpx;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.scan-card {
  display: flex;
  align-items: center;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1rpx solid rgba(255, 255, 255, 0.8);

  &:active {
    transform: scale(0.98);
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  }

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6rpx;
    border-radius: 6rpx 0 0 6rpx;
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

  &:nth-child(4)::before {
    background: linear-gradient(180deg, #FF4D4F 0%, #FF7875 50%, #FFA39E 100%);
  }
}

.card-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border-radius: 16rpx;
    border: 2rpx dashed rgba(59, 124, 255, 0.2);
    animation: pulse-ring 2.5s ease-out infinite;
  }
}

.icon-in { background: rgba(82, 196, 26, 0.1); }
.icon-out { background: rgba(59, 124, 255, 0.1); }
.icon-report { background: rgba(250, 173, 20, 0.1); }
.icon-check { background: rgba(255, 77, 79, 0.1); }

.card-icon-text {
  font-size: 32rpx;
  font-weight: 600;
}

.icon-in .card-icon-text { color: #52C41A; }
.icon-out .card-icon-text { color: #3B7CFF; }
.icon-report .card-icon-text { color: #FAAD14; }
.icon-check .card-icon-text { color: #FF4D4F; }

.card-info {
  flex: 1;
}

.card-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
  margin-bottom: 6rpx;
}

.card-desc {
  font-size: 22rpx;
  color: #8C8C8C;
  display: block;
}

.card-arrow {
  color: #CCCCCC;
  font-size: 32rpx;
}

.scan-history {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
}

.history-list {
  counter-reset: list-item;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F0F0F0;
  position: relative;
  padding-left: 48rpx;
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
    width: 32rpx;
    height: 32rpx;
    border-radius: 50%;
    background: rgba(59, 124, 255, 0.1);
    color: #3B7CFF;
    font-size: 20rpx;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.history-type {
  font-size: 24rpx;
  color: #3B7CFF;
  font-weight: 500;
  margin-right: 16rpx;
  flex-shrink: 0;
  padding: 4rpx 12rpx;
  background: rgba(59, 124, 255, 0.1);
  border-radius: 8rpx;
}

.history-code {
  font-size: 24rpx;
  color: #4A4A4A;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: monospace;
}

.history-time {
  font-size: 22rpx;
  color: #BBBBBB;
  margin-left: 16rpx;
  flex-shrink: 0;
}
</style>