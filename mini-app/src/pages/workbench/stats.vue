<template>
  <view class="stats-page">
    <DynamicBackground theme="theme-primary" />
    <view class="page-content">
    <view class="summary-cards">
      <view class="summary-card card-output">
        <view class="card-bg"></view>
        <view class="card-content">
          <text class="summary-label">今日产量</text>
          <view class="summary-value-row">
            <text class="summary-value">{{ dashboard.todayOutput }}</text>
            <text class="summary-unit">件</text>
          </view>
          <view class="summary-trend-wrap">
            <text :class="['summary-trend', dashboard.outputTrend >= 0 ? 'trend-up' : 'trend-down']">
              {{ dashboard.outputTrend >= 0 ? '↑' : '↓' }}{{ Math.abs(dashboard.outputTrend) }}%
            </text>
            <text class="trend-label">较昨日</text>
          </view>
        </view>
      </view>
      <view class="summary-card card-rate">
        <view class="card-bg"></view>
        <view class="card-content">
          <text class="summary-label">今日合格率</text>
          <view class="summary-value-row">
            <text class="summary-value">{{ dashboard.todayPassRate }}</text>
            <text class="summary-unit">%</text>
          </view>
          <view class="summary-trend-wrap">
            <text :class="['summary-trend', dashboard.rateTrend >= 0 ? 'trend-up' : 'trend-down']">
              {{ dashboard.rateTrend >= 0 ? '↑' : '↓' }}{{ Math.abs(dashboard.rateTrend) }}%
            </text>
            <text class="trend-label">较昨日</text>
          </view>
        </view>
      </view>
    </view>

    <view class="chart-section">
      <view class="section-header">
        <text class="section-title">本周产量趋势</text>
        <text class="section-sub">共 {{ weekTotalOutput }} 件</text>
      </view>
      <view class="chart-container">
        <view class="chart-grid">
          <view class="grid-line" v-for="i in 4" :key="i"></view>
        </view>
        <view class="chart-bars">
          <view class="bar-item" v-for="(val, idx) in dashboard.weekOutput" :key="idx">
            <text class="bar-value-top">{{ val }}</text>
            <view class="bar-wrapper">
              <view class="bar-fill" :style="{ height: barHeight(val) }">
                <view class="bar-glow"></view>
              </view>
            </view>
            <text class="bar-label">{{ dashboard.weekLabels[idx] }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="chart-section">
      <view class="section-header">
        <text class="section-title">本周合格率趋势</text>
        <text class="section-sub">平均 {{ weekAvgRate }}%</text>
      </view>
      <view class="chart-container">
        <view class="chart-grid rate-grid">
          <view class="grid-line" v-for="i in 4" :key="i"></view>
        </view>
        <view class="chart-bars">
          <view class="bar-item" v-for="(val, idx) in dashboard.weekPassRate" :key="idx">
            <text class="bar-value-top rate-value">{{ val }}%</text>
            <view class="bar-wrapper rate-bar-wrapper">
              <view class="bar-fill rate-fill" :style="{ height: rateBarHeight(val) }">
                <view class="bar-glow"></view>
              </view>
            </view>
            <text class="bar-label">{{ dashboard.weekLabels[idx] }}</text>
          </view>
        </view>
        <view class="target-line" :style="{ bottom: rateBarHeight(98) }">
          <text class="target-text">目标 98%</text>
        </view>
      </view>
    </view>

    <view class="process-section">
      <view class="section-header">
        <text class="section-title">各工序产量</text>
      </view>
      <view class="process-list">
        <view class="process-row" v-for="item in dashboard.processOutput" :key="item.processName">
          <view class="process-left">
            <text class="process-name">{{ item.processName }}</text>
            <view class="process-bar-bg">
              <view class="process-bar-fill" :style="{ width: processWidth(item.output) }"></view>
            </view>
          </view>
          <view class="process-right">
            <text class="process-output">{{ item.output }}件</text>
            <text class="process-rate" :class="item.passRate >= 98 ? 'rate-good' : item.passRate > 0 ? 'rate-warn' : ''">
              {{ item.passRate > 0 ? item.passRate + '%' : '-' }}
            </text>
          </view>
        </view>
      </view>
    </view>
    </view>
  </view>
</template>

<script>
import { getDashboard } from '@/api/production'
import DynamicBackground from '@/components/DynamicBackground'

export default {
  components: {
    DynamicBackground
  },
  data() {
    return {
      dashboard: {
        todayOutput: 320,
        todayPassRate: '98.5',
        outputTrend: 5,
        rateTrend: 0.3,
        weekOutput: [280, 310, 295, 340, 320, 0, 0],
        weekPassRate: [97.2, 98.1, 97.8, 98.5, 98.5, 0, 0],
        weekLabels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        processOutput: [
          { processName: 'SMT贴片', output: 150, passRate: 98.7 },
          { processName: '焊接组装', output: 148, passRate: 100 },
          { processName: '固件烧录', output: 148, passRate: 100 },
          { processName: '功能测试', output: 145, passRate: 97.3 },
          { processName: '外观检查', output: 145, passRate: 100 },
          { processName: '包装入库', output: 0, passRate: 0 }
        ]
      }
    }
  },
  computed: {
    weekTotalOutput() {
      return this.dashboard.weekOutput.reduce((sum, val) => sum + val, 0)
    },
    weekAvgRate() {
      const validRates = this.dashboard.weekPassRate.filter(val => val > 0)
      if (validRates.length === 0) return '0.0'
      return (validRates.reduce((sum, val) => sum + val, 0) / validRates.length).toFixed(1)
    }
  },
  onLoad() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        const res = await getDashboard()
        this.dashboard = {
          todayOutput: res.todayOutput || 320,
          todayPassRate: res.todayPassRate != null ? Number(res.todayPassRate).toFixed(1) : '98.5',
          outputTrend: res.outputTrend || 5,
          rateTrend: res.rateTrend || 0.3,
          weekOutput: res.weekOutput || [280, 310, 295, 340, 320, 0, 0],
          weekPassRate: res.weekPassRate || [97.2, 98.1, 97.8, 98.5, 98.5, 0, 0],
          weekLabels: res.weekLabels || ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
          processOutput: res.processOutput || [
            { processName: 'SMT贴片', output: 150, passRate: 98.7 },
            { processName: '焊接组装', output: 148, passRate: 100 },
            { processName: '固件烧录', output: 148, passRate: 100 },
            { processName: '功能测试', output: 145, passRate: 97.3 },
            { processName: '外观检查', output: 145, passRate: 100 },
            { processName: '包装入库', output: 0, passRate: 0 }
          ]
        }
      } catch {
        // keep default mock data
      }
    },
    barHeight(val) {
      const max = Math.max(...this.dashboard.weekOutput, 1)
      return Math.max((val / max) * 100, 0) + '%'
    },
    rateBarHeight(val) {
      if (!val) return '0%'
      return Math.max(((val - 90) / 10) * 100, 0) + '%'
    },
    processWidth(val) {
      const max = Math.max(...this.dashboard.processOutput.map(p => p.output), 1)
      return Math.max((val / max) * 100, 0) + '%'
    }
  }
}
</script>

<style lang="scss" scoped>
.stats-page {
  min-height: 100vh;
  background: transparent;
  padding: 24rpx;
  padding-bottom: 40rpx;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
}

.summary-cards {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.summary-card {
  flex: 1;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  padding: 32rpx 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
  border: 1rpx solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 8rpx;
    border-radius: 8rpx 8rpx 0 0;
  }

  &.card-output::before {
    background: linear-gradient(90deg, #3B7CFF 0%, #5B9AFF 50%, #7CB7FF 100%);
  }

  &.card-rate::before {
    background: linear-gradient(90deg, #52C41A 0%, #73D13D 50%, #95DE64 100%);
  }

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  }
}

.card-bg {
  position: absolute;
  top: -80rpx;
  right: -80rpx;
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
}

.card-output .card-bg {
  background: radial-gradient(circle, rgba(59, 124, 255, 0.15) 0%, transparent 70%);
}

.card-rate .card-bg {
  background: radial-gradient(circle, rgba(103, 194, 58, 0.15) 0%, transparent 70%);
}

.card-content {
  position: relative;
  z-index: 1;
}

.summary-label {
  font-size: 24rpx;
  color: #8C8C8C;
  display: block;
  margin-bottom: 12rpx;
}

.summary-value-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 12rpx;
}

.summary-value {
  font-size: 56rpx;
  font-weight: 700;
  color: #1F1F1F;
  line-height: 1;
  letter-spacing: -2rpx;
}

.summary-unit {
  font-size: 22rpx;
  color: #8C8C8C;
  margin-left: 6rpx;
}

.summary-trend-wrap {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.summary-trend {
  font-size: 24rpx;
  font-weight: 600;

  &.trend-up { color: #52C41A; }
  &.trend-down { color: #FF4D4F; }
}

.trend-label {
  font-size: 22rpx;
  color: #BFBFBF;
}

.chart-section {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  padding: 32rpx 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  position: relative;
  border: 1rpx solid rgba(255, 255, 255, 0.8);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28rpx;
}

.section-title {
  font-size: 32rpx;
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
    background: #3B7CFF;
  }
}

.section-sub {
  font-size: 24rpx;
  color: #8C8C8C;
}

.chart-container {
  width: 100%;
  height: 340rpx;
  position: relative;
}

.chart-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 40rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  pointer-events: none;

  .grid-line {
    height: 1rpx;
    background: rgba(64, 158, 255, 0.1);
  }
}

.rate-grid .grid-line {
  background: rgba(103, 194, 58, 0.1);
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 300rpx;
  padding-top: 10rpx;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.bar-value-top {
  font-size: 22rpx;
  font-weight: 600;
  color: #3B7CFF;
  margin-bottom: 8rpx;
  height: 28rpx;
}

.bar-value-top.rate-value {
  color: #52C41A;
}

.bar-wrapper {
  width: 44rpx;
  height: 220rpx;
  background: linear-gradient(180deg, rgba(64, 158, 255, 0.08) 0%, rgba(64, 158, 255, 0.03) 100%);
  border-radius: 12rpx 12rpx 0 0;
  display: flex;
  align-items: flex-end;
  overflow: hidden;
  position: relative;
}

.rate-bar-wrapper {
  background: linear-gradient(180deg, rgba(103, 194, 58, 0.08) 0%, rgba(103, 194, 58, 0.03) 100%);
}

.bar-fill {
  width: 100%;
  background: linear-gradient(180deg, #3B7CFF 0%, #6BA1FF 50%, #8EC5FC 100%);
  border-radius: 12rpx 12rpx 0 0;
  transition: height 0.5s ease;
  position: relative;
  overflow: hidden;
}

.bar-fill::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 50%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.4) 0%, transparent 100%);
}

.rate-fill {
  background: linear-gradient(180deg, #52C41A 0%, #73D13D 50%, #95DE64 100%);
}

.bar-glow {
  position: absolute;
  top: -10rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  height: 20rpx;
  background: radial-gradient(ellipse, rgba(255, 255, 255, 0.6) 0%, transparent 70%);
  pointer-events: none;
}

.bar-label {
  font-size: 22rpx;
  color: #8C8C8C;
  margin-top: 12rpx;
}

.target-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 2rpx;
  background: rgba(250, 173, 20, 0.6);
  z-index: 10;

  &::before {
    content: '';
    position: absolute;
    left: -8rpx;
    top: -4rpx;
    width: 8rpx;
    height: 10rpx;
    background: rgba(250, 173, 20, 0.6);
    border-radius: 4rpx 0 0 4rpx;
  }
}

.target-text {
  position: absolute;
  left: 8rpx;
  top: -32rpx;
  font-size: 20rpx;
  color: #FAAD14;
  white-space: nowrap;
}

.process-section {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  padding: 32rpx 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
}

.process-list {
  margin-top: 8rpx;
}

.process-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F5F7FA;

  &:last-child {
    margin-bottom: 0;
    border-bottom: none;
  }
}

.process-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.process-name {
  font-size: 28rpx;
  color: #1F1F1F;
  font-weight: 500;
}

.process-bar-bg {
  width: 100%;
  height: 16rpx;
  background: #F0F2F5;
  border-radius: 8rpx;
  overflow: hidden;
}

.process-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #3B7CFF 0%, #6BA1FF 100%);
  border-radius: 8rpx;
  transition: width 0.5s ease;
}

.process-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4rpx;
  margin-left: 20rpx;
  flex-shrink: 0;
}

.process-output {
  font-size: 26rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.process-rate {
  font-size: 24rpx;

  &.rate-good { color: #52C41A; }
  &.rate-warn { color: #FAAD14; }
}
</style>