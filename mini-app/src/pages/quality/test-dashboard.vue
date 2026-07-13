<template>
  <view class="test-dashboard-page">
    <DynamicBackground theme="theme-success" />
    <view class="page-content">
    <view class="page-header">
      <view class="page-title-wrap">
        <text class="page-title">功能测试数据看板</text>
        <text class="page-desc">智能手环功能测试实时数据</text>
      </view>
    </view>

    <view class="search-section">
      <view class="search-bar">
        <u-search v-model="searchKey" placeholder="搜索SN码/工单号" @search="doSearch" @custom="doSearch" @clear="doSearch" shape="round"></u-search>
      </view>
      <view class="filter-tabs">
        <u-tabs :list="filterList" :current="currentFilter" @click="onFilterChange" :activeStyle="{ color: '#52C41A', fontWeight: 'bold' }" :inactiveStyle="{ color: '#8C8C8C' }" lineColor="#52C41A" :lineWidth="40"></u-tabs>
      </view>
    </view>

    <view class="stats-grid">
      <view class="stat-card main">
        <view class="stat-ring">
          <view class="ring-bg"></view>
          <view class="ring-progress" :style="{ transform: `rotate(${getRingRotation(summary.passRate)}deg)` }"></view>
          <view class="ring-center">
            <text class="ring-value">{{ summary.passRate }}</text>
            <text class="ring-unit">%</text>
            <text class="ring-label">综合通过率</text>
          </view>
        </view>
      </view>
      <view class="stat-card">
        <text class="stat-icon-wrap blue">📊</text>
        <text class="stat-number">{{ summary.total.toLocaleString() }}</text>
        <text class="stat-name">测试总数</text>
      </view>
      <view class="stat-card">
        <text class="stat-icon-wrap green">✅</text>
        <text class="stat-number success">{{ summary.passCount.toLocaleString() }}</text>
        <text class="stat-name">通过数</text>
      </view>
      <view class="stat-card">
        <text class="stat-icon-wrap red">❌</text>
        <text class="stat-number error">{{ summary.failCount.toLocaleString() }}</text>
        <text class="stat-name">失败数</text>
      </view>
    </view>

    <view class="test-section" v-if="currentFilter === 0 || currentFilter === 1">
      <view class="section-header">
        <text class="section-title">❤️ 心率传感器</text>
        <text class="section-rate" :class="heartRate.passRate >= 95 ? 'rate-good' : 'rate-warning'">{{ heartRate.passRate }}%</text>
      </view>
      <view class="test-card">
        <view class="card-header heart">
          <view class="card-icon">❤️</view>
          <view class="card-title-wrap">
            <text class="card-title">心率传感器测试</text>
            <text class="card-sub">实时监测心率数据</text>
          </view>
          <view class="card-rate-badge" :class="heartRate.passRate >= 95 ? 'badge-good' : 'badge-warning'">
            <text class="badge-value">{{ heartRate.passRate }}%</text>
          </view>
        </view>
        <view class="card-body">
          <view class="progress-bar-row">
            <view class="progress-bar-wrap">
              <view class="progress-track">
                <view class="progress-fill heart" :style="{ width: heartRate.passRate + '%' }"></view>
              </view>
              <text class="progress-label">通过率</text>
            </view>
          </view>
          <view class="metrics-grid">
            <view class="metric-card">
              <text class="metric-icon">📊</text>
              <text class="metric-label">测试范围</text>
              <text class="metric-value">60-180bpm</text>
            </view>
            <view class="metric-card">
              <text class="metric-icon">🎯</text>
              <text class="metric-label">准确率</text>
              <text class="metric-value highlight">{{ heartRate.accuracy }}%</text>
            </view>
            <view class="metric-card">
              <text class="metric-icon">⚡</text>
              <text class="metric-label">响应时间</text>
              <text class="metric-value">{{ heartRate.responseTime }}ms</text>
            </view>
          </view>
          <view class="data-row">
            <view class="data-item">
              <text class="data-label">今日通过</text>
              <text class="data-value pass">{{ heartRate.todayPass }}</text>
            </view>
            <view class="data-item">
              <text class="data-label">今日失败</text>
              <text class="data-value fail">{{ heartRate.todayFail }}</text>
            </view>
            <view class="data-item">
              <text class="data-label">累计测试</text>
              <text class="data-value">{{ heartRate.total }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="test-section" v-if="currentFilter === 0 || currentFilter === 2">
      <view class="section-header">
        <text class="section-title">🔵 蓝牙连接</text>
        <text class="section-rate" :class="bluetooth.passRate >= 95 ? 'rate-good' : 'rate-warning'">{{ bluetooth.passRate }}%</text>
      </view>
      <view class="test-card">
        <view class="card-header blue">
          <view class="card-icon">🔵</view>
          <view class="card-title-wrap">
            <text class="card-title">蓝牙连接测试</text>
            <text class="card-sub">BLE 5.0协议通信</text>
          </view>
          <view class="card-rate-badge" :class="bluetooth.passRate >= 95 ? 'badge-good' : 'badge-warning'">
            <text class="badge-value">{{ bluetooth.passRate }}%</text>
          </view>
        </view>
        <view class="card-body">
          <view class="progress-bar-row">
            <view class="progress-bar-wrap">
              <view class="progress-track">
                <view class="progress-fill blue" :style="{ width: bluetooth.passRate + '%' }"></view>
              </view>
              <text class="progress-label">通过率</text>
            </view>
          </view>
          <view class="metrics-grid">
            <view class="metric-card">
              <text class="metric-icon">📶</text>
              <text class="metric-label">协议版本</text>
              <text class="metric-value">BLE 5.0</text>
            </view>
            <view class="metric-card">
              <text class="metric-icon">📏</text>
              <text class="metric-label">连接距离</text>
              <text class="metric-value highlight">{{ bluetooth.range }}m</text>
            </view>
            <view class="metric-card">
              <text class="metric-icon">⏱️</text>
              <text class="metric-label">平均耗时</text>
              <text class="metric-value">{{ bluetooth.connectTime }}s</text>
            </view>
          </view>
          <view class="data-row">
            <view class="data-item">
              <text class="data-label">今日通过</text>
              <text class="data-value pass">{{ bluetooth.todayPass }}</text>
            </view>
            <view class="data-item">
              <text class="data-label">今日失败</text>
              <text class="data-value fail">{{ bluetooth.todayFail }}</text>
            </view>
            <view class="data-item">
              <text class="data-label">累计测试</text>
              <text class="data-value">{{ bluetooth.total }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="test-section" v-if="currentFilter === 0 || currentFilter === 3">
      <view class="section-header">
        <text class="section-title">🔋 电池充放电</text>
        <text class="section-rate" :class="battery.passRate >= 95 ? 'rate-good' : 'rate-warning'">{{ battery.passRate }}%</text>
      </view>
      <view class="test-card">
        <view class="card-header orange">
          <view class="card-icon">🔋</view>
          <view class="card-title-wrap">
            <text class="card-title">电池充放电测试</text>
            <text class="card-sub">容量与续航检测</text>
          </view>
          <view class="card-rate-badge" :class="battery.passRate >= 95 ? 'badge-good' : 'badge-warning'">
            <text class="badge-value">{{ battery.passRate }}%</text>
          </view>
        </view>
        <view class="card-body">
          <view class="progress-bar-row">
            <view class="progress-bar-wrap">
              <view class="progress-track">
                <view class="progress-fill orange" :style="{ width: battery.passRate + '%' }"></view>
              </view>
              <text class="progress-label">通过率</text>
            </view>
          </view>
          <view class="metrics-grid">
            <view class="metric-card">
              <text class="metric-icon">📦</text>
              <text class="metric-label">标称容量</text>
              <text class="metric-value">380mAh</text>
            </view>
            <view class="metric-card">
              <text class="metric-icon">⚡</text>
              <text class="metric-label">实测容量</text>
              <text class="metric-value highlight">{{ battery.actualCapacity }}mAh</text>
            </view>
            <view class="metric-card">
              <text class="metric-icon">🔌</text>
              <text class="metric-label">充电时间</text>
              <text class="metric-value">{{ battery.chargeTime }}h</text>
            </view>
          </view>
          <view class="data-row">
            <view class="data-item">
              <text class="data-label">今日通过</text>
              <text class="data-value pass">{{ battery.todayPass }}</text>
            </view>
            <view class="data-item">
              <text class="data-label">今日失败</text>
              <text class="data-value fail">{{ battery.todayFail }}</text>
            </view>
            <view class="data-item">
              <text class="data-label">累计测试</text>
              <text class="data-value">{{ battery.total }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="test-section" v-if="currentFilter === 0 || currentFilter === 4">
      <view class="section-header">
        <text class="section-title">💧 防水测试</text>
        <text class="section-rate" :class="waterproof.passRate >= 95 ? 'rate-good' : 'rate-warning'">{{ waterproof.passRate }}%</text>
      </view>
      <view class="test-card">
        <view class="card-header cyan">
          <view class="card-icon">💧</view>
          <view class="card-title-wrap">
            <text class="card-title">防水性能测试</text>
            <text class="card-sub">IP68防水等级验证</text>
          </view>
          <view class="card-rate-badge" :class="waterproof.passRate >= 95 ? 'badge-good' : 'badge-warning'">
            <text class="badge-value">{{ waterproof.passRate }}%</text>
          </view>
        </view>
        <view class="card-body">
          <view class="progress-bar-row">
            <view class="progress-bar-wrap">
              <view class="progress-track">
                <view class="progress-fill cyan" :style="{ width: waterproof.passRate + '%' }"></view>
              </view>
              <text class="progress-label">通过率</text>
            </view>
          </view>
          <view class="metrics-grid">
            <view class="metric-card">
              <text class="metric-icon">🏊</text>
              <text class="metric-label">防水等级</text>
              <text class="metric-value">IP{{ waterproof.level }}</text>
            </view>
            <view class="metric-card">
              <text class="metric-icon">📏</text>
              <text class="metric-label">测试深度</text>
              <text class="metric-value highlight">{{ waterproof.depth }}m</text>
            </view>
            <view class="metric-card">
              <text class="metric-icon">⏱️</text>
              <text class="metric-label">测试时长</text>
              <text class="metric-value">{{ waterproof.duration }}h</text>
            </view>
          </view>
          <view class="data-row">
            <view class="data-item">
              <text class="data-label">今日通过</text>
              <text class="data-value pass">{{ waterproof.todayPass }}</text>
            </view>
            <view class="data-item">
              <text class="data-label">今日失败</text>
              <text class="data-value fail">{{ waterproof.todayFail }}</text>
            </view>
            <view class="data-item">
              <text class="data-label">累计测试</text>
              <text class="data-value">{{ waterproof.total }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="trend-section" v-if="currentFilter === 0">
      <view class="section-header">
        <text class="section-title">📈 今日测试趋势</text>
        <text class="section-sub">每小时通过率变化</text>
      </view>
      <view class="trend-chart">
        <view class="chart-grid">
          <view class="grid-line" v-for="i in 4" :key="i"></view>
        </view>
        <view class="chart-area">
          <view class="chart-line-fill" :style="{ clipPath: getLineClipPath() }"></view>
          <view class="chart-lines">
            <view 
              v-for="(point, idx) in trendData" 
              :key="'line-' + idx" 
              class="chart-line-segment"
              :style="getLineStyle(idx)"
            ></view>
          </view>
          <view 
            v-for="(point, idx) in trendData" 
            :key="idx" 
            class="chart-point"
            :style="{ 
              left: getPointX(idx) + '%', 
              bottom: point.rate + '%' 
            }"
          >
            <view class="point-dot" :class="point.rate >= 95 ? 'dot-good' : point.rate >= 90 ? 'dot-warning' : 'dot-error'"></view>
            <text class="point-value">{{ point.rate }}%</text>
          </view>
        </view>
        <view class="chart-labels">
          <text v-for="(item, idx) in trendData" :key="idx" class="chart-label">{{ item.time }}</text>
        </view>
      </view>
    </view>

    <view class="alarm-section" v-if="currentFilter === 0">
      <view class="section-header">
        <text class="section-title">⚠️ 测试异常告警</text>
      </view>
      <view class="alarm-list" v-if="filteredAlarms.length > 0">
        <view class="alarm-item" v-for="item in filteredAlarms" :key="item.id">
          <view class="alarm-indicator" :class="item.level"></view>
          <view class="alarm-content">
            <view class="alarm-header">
              <text class="alarm-title">{{ item.title }}</text>
              <text class="alarm-time">{{ item.time }}</text>
            </view>
            <text class="alarm-detail">{{ item.detail }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty-alarm">
        <text class="empty-text">暂无测试异常</text>
      </view>
    </view>

    <view class="test-records-section" v-if="searchKey && searchResults.length > 0">
      <view class="section-header">
        <text class="section-title">🔍 搜索结果</text>
        <text class="section-count">找到 {{ searchResults.length }} 条记录</text>
      </view>
      <view class="record-list">
        <view class="record-card" v-for="item in searchResults" :key="item.id">
          <view class="record-header">
            <text class="record-sn">{{ item.snCode }}</text>
            <u-tag :text="item.statusText" :type="item.statusTag" size="mini"></u-tag>
          </view>
          <view class="record-body">
            <view class="record-row">
              <text class="record-label">产品型号</text>
              <text class="record-value">{{ item.productModel }}</text>
            </view>
            <view class="record-row">
              <text class="record-label">测试工序</text>
              <text class="record-value">{{ item.processName }}</text>
            </view>
            <view class="record-row">
              <text class="record-label">测试时间</text>
              <text class="record-value">{{ item.testTime }}</text>
            </view>
            <view class="record-test-results">
              <view class="test-result" :class="item.heartRate ? 'pass' : 'fail'">
                <text class="result-icon">❤️</text>
                <text class="result-text">{{ item.heartRate ? '通过' : '失败' }}</text>
              </view>
              <view class="test-result" :class="item.bluetooth ? 'pass' : 'fail'">
                <text class="result-icon">🔵</text>
                <text class="result-text">{{ item.bluetooth ? '通过' : '失败' }}</text>
              </view>
              <view class="test-result" :class="item.battery ? 'pass' : 'fail'">
                <text class="result-icon">🔋</text>
                <text class="result-text">{{ item.battery ? '通过' : '失败' }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
    </view>
  </view>
</template>

<script>
import DynamicBackground from '@/components/DynamicBackground'

export default {
  components: {
    DynamicBackground
  },
  data() {
    return {
      searchKey: '',
      currentFilter: 0,
      filterList: [
        { name: '全部', value: 'all' },
        { name: '心率', value: 'heartRate' },
        { name: '蓝牙', value: 'bluetooth' },
        { name: '电池', value: 'battery' },
        { name: '防水', value: 'waterproof' }
      ],
      summary: {
        total: 0,
        passRate: 0,
        passCount: 0,
        failCount: 0
      },
      heartRate: {
        passRate: 98,
        accuracy: 99.2,
        responseTime: 120,
        todayPass: 485,
        todayFail: 10,
        total: 12580
      },
      bluetooth: {
        passRate: 96,
        range: 10,
        connectTime: 2.3,
        todayPass: 472,
        todayFail: 18,
        total: 12450
      },
      battery: {
        passRate: 97,
        actualCapacity: 375,
        chargeTime: 1.5,
        todayPass: 478,
        todayFail: 15,
        total: 12380
      },
      waterproof: {
        passRate: 95,
        level: 68,
        depth: 2,
        duration: 1.5,
        todayPass: 465,
        todayFail: 23,
        total: 12200
      },
      trendData: [
        { time: '08:00', rate: 92 },
        { time: '09:00', rate: 95 },
        { time: '10:00', rate: 98 },
        { time: '11:00', rate: 96 },
        { time: '12:00', rate: 94 },
        { time: '13:00', rate: 97 },
        { time: '14:00', rate: 99 },
        { time: '15:00', rate: 98 },
        { time: '16:00', rate: 96 },
        { time: '17:00', rate: 97 }
      ],
      alarmList: [
        { id: 1, level: 'level-error', title: '心率传感器异常', detail: 'SN20240115002心率测试多次失败，建议复检', time: '16:30' },
        { id: 2, level: 'level-warning', title: '蓝牙连接超时', detail: 'BATCH20240115批次蓝牙连接时间超过3秒', time: '15:20' },
        { id: 3, level: 'level-info', title: '电池容量偏低', detail: 'SN20240115018实测容量368mAh，接近下限', time: '14:15' },
        { id: 4, level: 'level-error', title: '防水测试失败', detail: 'SN20240115025防水测试IP68不达标', time: '13:45' }
      ],
      testRecords: [
        { id: 1, snCode: 'SN20240115001', productModel: '智能运动手环A1', processName: '功能测试', testTime: '2024-01-15 16:30', heartRate: true, bluetooth: true, battery: true, waterproof: true, statusText: '全部通过', statusTag: 'success' },
        { id: 2, snCode: 'SN20240115002', productModel: '智能运动手环A1', processName: '功能测试', testTime: '2024-01-15 16:25', heartRate: false, bluetooth: true, battery: true, waterproof: true, statusText: '心率失败', statusTag: 'error' },
        { id: 3, snCode: 'SN20240115003', productModel: '智能运动手环A1', processName: '功能测试', testTime: '2024-01-15 16:20', heartRate: true, bluetooth: true, battery: true, waterproof: true, statusText: '全部通过', statusTag: 'success' },
        { id: 4, snCode: 'SN20240115004', productModel: '智能运动手环A1', processName: '功能测试', testTime: '2024-01-15 16:15', heartRate: true, bluetooth: false, battery: true, waterproof: true, statusText: '蓝牙失败', statusTag: 'error' },
        { id: 5, snCode: 'SN20240115005', productModel: '智能运动手环A1', processName: '功能测试', testTime: '2024-01-15 16:10', heartRate: true, bluetooth: true, battery: true, waterproof: true, statusText: '全部通过', statusTag: 'success' }
      ],
      searchResults: []
    }
  },
  computed: {
    filteredAlarms() {
      if (!this.searchKey) return this.alarmList
      const key = this.searchKey.toLowerCase()
      return this.alarmList.filter(item => 
        item.title.toLowerCase().includes(key) || 
        item.detail.toLowerCase().includes(key)
      )
    }
  },
  onLoad() {
    this.calcSummary()
  },
  methods: {
    calcSummary() {
      const total = this.heartRate.total + this.bluetooth.total + this.battery.total + this.waterproof.total
      const passCount = Math.round(this.heartRate.total * this.heartRate.passRate / 100) +
                        Math.round(this.bluetooth.total * this.bluetooth.passRate / 100) +
                        Math.round(this.battery.total * this.battery.passRate / 100) +
                        Math.round(this.waterproof.total * this.waterproof.passRate / 100)
      
      this.summary.total = total
      this.summary.passCount = passCount
      this.summary.failCount = total - passCount
      this.summary.passRate = total > 0 ? ((passCount / total) * 100).toFixed(1) : 0
    },
    onFilterChange({ index }) {
      this.currentFilter = index
    },
    doSearch() {
      if (!this.searchKey.trim()) {
        this.searchResults = []
        return
      }
      const key = this.searchKey.toLowerCase()
      this.searchResults = this.testRecords.filter(item => 
        item.snCode.toLowerCase().includes(key) ||
        item.productModel.toLowerCase().includes(key) ||
        item.processName.toLowerCase().includes(key)
      )
    },
    getRingRotation(percent) {
      return (percent / 100) * 360
    },
    getPointX(index) {
      const total = this.trendData.length
      return ((index + 1) / (total + 1)) * 100
    },
    getLineClipPath() {
      if (this.trendData.length === 0) return ''
      const total = this.trendData.length
      let points = []
      points.push(`0% 100%`)
      for (let i = 0; i < total; i++) {
        const x = ((i + 1) / (total + 1)) * 100
        const y = 100 - this.trendData[i].rate
        points.push(`${x}% ${y}%`)
      }
      points.push(`100% 100%`)
      return `polygon(${points.join(', ')})`
    },
    getLineStyle(idx) {
      if (idx >= this.trendData.length - 1) return {}
      const startX = this.getPointX(idx)
      const startY = this.trendData[idx].rate
      const endX = this.getPointX(idx + 1)
      const endY = this.trendData[idx + 1].rate
      const length = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2))
      const angle = Math.atan2(endY - startY, endX - startX) * 180 / Math.PI
      return {
        width: `${length}%`,
        transform: `rotate(${angle}deg)`,
        left: `${startX}%`,
        bottom: `${startY}%`
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.test-dashboard-page {
  min-height: 100vh;
  background: transparent;
  padding-bottom: $tabbar-height;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
}

.page-header {
  padding: 40rpx 32rpx 32rpx;
  background: linear-gradient(135deg, #52C41A 0%, #73D13D 40%, #95DE64 80%, #A8E6CF 100%);
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

.search-section {
  padding: 24rpx;
}

.search-bar {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 8rpx 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
  margin-bottom: 16rpx;
}

.filter-tabs {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 8rpx 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
  padding: 0 24rpx;
  margin-top: -32rpx;
}

.stat-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  padding: 28rpx 24rpx;
  box-shadow: 0 12rpx 40rpx rgba(0, 0, 0, 0.08), 0 0 0 1rpx rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
  }

  &.main {
    padding: 36rpx 28rpx;
    background: linear-gradient(145deg, #FFFFFF 0%, #F0FFF0 100%);
  }
}

.stat-ring {
  position: relative;
  width: 150rpx;
  height: 150rpx;
}

.ring-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #E8F5E9 0%, #F1FFF1 100%);
}

.ring-progress {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: conic-gradient(#52C41A 0deg, #73D13D 50%, #95DE64 100%, transparent 100%);
  transition: transform 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.ring-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 108rpx;
  height: 108rpx;
  background: #ffffff;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.ring-value {
  font-size: 36rpx;
  font-weight: 700;
  color: #52C41A;
  line-height: 1;
  text-shadow: 0 2rpx 4rpx rgba(103, 194, 58, 0.2);
}

.ring-unit {
  font-size: 22rpx;
  color: #8C8C8C;
}

.ring-label {
  font-size: 18rpx;
  color: #8C8C8C;
  margin-top: 4rpx;
}

.stat-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
  font-size: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

  &.blue { background: linear-gradient(135deg, rgba(64, 158, 255, 0.2) 0%, rgba(64, 158, 255, 0.05) 100%); }
  &.green { background: linear-gradient(135deg, rgba(103, 194, 58, 0.2) 0%, rgba(103, 194, 58, 0.05) 100%); }
  &.red { background: linear-gradient(135deg, rgba(245, 108, 108, 0.2) 0%, rgba(245, 108, 108, 0.05) 100%); }
}

.stat-number {
  font-size: 40rpx;
  font-weight: 700;
  color: #1F1F1F;
  display: block;
  margin-bottom: 6rpx;
  letter-spacing: -1rpx;

  &.success { 
    color: #52C41A; 
    text-shadow: 0 2rpx 8rpx rgba(103, 194, 58, 0.3);
  }
  &.error { 
    color: #FF4D4F; 
    text-shadow: 0 2rpx 8rpx rgba(245, 108, 108, 0.3);
  }
}

.stat-name {
  font-size: 24rpx;
  color: #8C8C8C;
  font-weight: 500;
}

.test-section {
  padding: 0 24rpx;
  margin-bottom: 24rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.section-sub {
  font-size: 24rpx;
  color: #8C8C8C;
}

.section-count {
  font-size: 24rpx;
  color: #52C41A;
}

.section-rate {
  font-size: 36rpx;
  font-weight: 700;

  &.rate-good { color: #52C41A; }
  &.rate-warning { color: #FAAD14; }
}

.test-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  overflow: hidden;
  box-shadow: 0 12rpx 48rpx rgba(0, 0, 0, 0.08), 0 0 0 1rpx rgba(255, 255, 255, 0.9);
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:active {
    transform: scale(0.99);
    box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.12);
  }
}

.card-header {
  padding: 28rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  border-left: 8rpx solid transparent;

  &.heart { 
    background: linear-gradient(135deg, rgba(245, 108, 108, 0.12) 0%, rgba(245, 108, 108, 0.03) 100%); 
    border-left-color: #FF4D4F; 
  }
  &.blue { 
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.12) 0%, rgba(64, 158, 255, 0.03) 100%); 
    border-left-color: #3B7CFF; 
  }
  &.orange { 
    background: linear-gradient(135deg, rgba(250, 173, 20, 0.12) 0%, rgba(250, 173, 20, 0.03) 100%); 
    border-left-color: #FAAD14; 
  }
  &.cyan { 
    background: linear-gradient(135deg, rgba(19, 194, 194, 0.12) 0%, rgba(19, 194, 194, 0.03) 100%); 
    border-left-color: #13C2C2; 
  }
}

.card-icon {
  font-size: 52rpx;
  filter: drop-shadow(0 4rpx 8rpx rgba(0, 0, 0, 0.1));
}

.card-title-wrap {
  flex: 1;
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
  letter-spacing: 0.5rpx;
}

.card-sub {
  font-size: 24rpx;
  color: #8C8C8C;
  display: block;
  margin-top: 6rpx;
}

.card-rate-badge {
  padding: 10rpx 24rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);

  &.badge-good { 
    background: linear-gradient(135deg, rgba(103, 194, 58, 0.18) 0%, rgba(103, 194, 58, 0.08) 100%); 
    .badge-value { color: #52C41A; } 
  }
  &.badge-warning { 
    background: linear-gradient(135deg, rgba(250, 173, 20, 0.18) 0%, rgba(250, 173, 20, 0.08) 100%); 
    .badge-value { color: #FAAD14; } 
  }
}

.badge-value {
  font-size: 30rpx;
  font-weight: 700;
}

.card-body {
  padding: 28rpx;
}

.progress-bar-row {
  margin-bottom: 28rpx;
}

.progress-bar-wrap {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.progress-track {
  flex: 1;
  height: 18rpx;
  background: linear-gradient(90deg, #F0F0F0 0%, #F5F5F5 100%);
  border-radius: 9rpx;
  overflow: hidden;
  box-shadow: inset 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.progress-fill {
  height: 100%;
  border-radius: 9rpx;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 20rpx;
    height: 100%;
    background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.4) 100%);
  }

  &.heart { 
    background: linear-gradient(90deg, #FF4D4F 0%, #FF7875 50%, #FFA39E 100%); 
    box-shadow: 0 0 16rpx rgba(245, 108, 108, 0.4);
  }
  &.blue { 
    background: linear-gradient(90deg, #3B7CFF 0%, #69B1FF 50%, #9DCFFF 100%); 
    box-shadow: 0 0 16rpx rgba(64, 158, 255, 0.4);
  }
  &.orange { 
    background: linear-gradient(90deg, #FAAD14 0%, #FFC53D 50%, #FFD666 100%); 
    box-shadow: 0 0 16rpx rgba(250, 173, 20, 0.4);
  }
  &.cyan { 
    background: linear-gradient(90deg, #13C2C2 0%, #36CFC9 50%, #5CDBD3 100%); 
    box-shadow: 0 0 16rpx rgba(19, 194, 194, 0.4);
  }
}

.progress-label {
  font-size: 26rpx;
  color: #8C8C8C;
  flex-shrink: 0;
  font-weight: 500;
}

.metrics-grid {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 28rpx;
}

.metric-card {
  flex: 1;
  text-align: center;
  padding: 24rpx 16rpx;
  background: linear-gradient(145deg, #F8FAFF 0%, #FFFFFF 100%);
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(255, 255, 255, 0.6);
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:active {
    transform: scale(0.97);
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
  }
}

.metric-icon {
  font-size: 36rpx;
  display: block;
  margin-bottom: 12rpx;
  filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.08));
}

.metric-label {
  font-size: 24rpx;
  color: #8C8C8C;
  display: block;
  margin-bottom: 6rpx;
}

.metric-value {
  font-size: 32rpx;
  font-weight: 700;
  color: #1F1F1F;
  display: block;
  letter-spacing: -0.5rpx;

  &.highlight { 
    color: #52C41A; 
    text-shadow: 0 2rpx 8rpx rgba(103, 194, 58, 0.3);
  }
}

.data-row {
  display: flex;
  justify-content: space-around;
  padding-top: 24rpx;
  border-top: 1rpx solid rgba(0, 0, 0, 0.06);
}

.data-item {
  text-align: center;
  padding: 0 16rpx;
}

.data-label {
  font-size: 24rpx;
  color: #8C8C8C;
  display: block;
  margin-bottom: 10rpx;
  font-weight: 500;
}

.data-value {
  font-size: 36rpx;
  font-weight: 700;
  color: #1F1F1F;
  display: block;
  letter-spacing: -1rpx;

  &.pass { 
    color: #52C41A; 
    text-shadow: 0 2rpx 8rpx rgba(103, 194, 58, 0.3);
  }
  &.fail { 
    color: #FF4D4F; 
    text-shadow: 0 2rpx 8rpx rgba(245, 108, 108, 0.3);
  }
}

.trend-section {
  padding: 0 24rpx;
  margin-bottom: 24rpx;
}

.trend-chart {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  padding: 32rpx;
  box-shadow: 0 12rpx 48rpx rgba(0, 0, 0, 0.08), 0 0 0 1rpx rgba(255, 255, 255, 0.8);
  position: relative;
  height: 320rpx;
}

.chart-grid {
  position: absolute;
  top: 32rpx;
  left: 32rpx;
  right: 32rpx;
  bottom: 50rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.grid-line {
  height: 1rpx;
  background: linear-gradient(90deg, transparent 0%, rgba(103, 194, 58, 0.1) 50%, transparent 100%);
}

.chart-area {
  position: absolute;
  top: 32rpx;
  left: 32rpx;
  right: 32rpx;
  bottom: 50rpx;
  overflow: hidden;
}

.chart-line-fill {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, rgba(103, 194, 58, 0.35) 0%, rgba(103, 194, 58, 0.08) 60%, rgba(103, 194, 58, 0.02) 100%);
  transition: clip-path 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.chart-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.chart-line-segment {
  position: absolute;
  height: 4rpx;
  background: linear-gradient(90deg, #52C41A 0%, #73D13D 100%);
  border-radius: 2rpx;
  transform-origin: left center;
  box-shadow: 0 0 12rpx rgba(103, 194, 58, 0.5);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.chart-point {
  position: absolute;
  transform: translate(-50%, 50%);
  z-index: 10;
}

.point-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  border: 4rpx solid #ffffff;
  box-shadow: 0 4rpx 16rpx rgba(103, 194, 58, 0.4), 0 0 0 4rpx rgba(103, 194, 58, 0.15);
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:active {
    transform: scale(1.3);
  }

  &.dot-good { 
    background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%); 
    box-shadow: 0 4rpx 16rpx rgba(103, 194, 58, 0.4), 0 0 0 4rpx rgba(103, 194, 58, 0.15);
  }
  &.dot-warning { 
    background: linear-gradient(135deg, #FAAD14 0%, #FFC53D 100%); 
    box-shadow: 0 4rpx 16rpx rgba(250, 173, 20, 0.4), 0 0 0 4rpx rgba(250, 173, 20, 0.15);
  }
  &.dot-error { 
    background: linear-gradient(135deg, #FF4D4F 0%, #FF7875 100%); 
    box-shadow: 0 4rpx 16rpx rgba(245, 108, 108, 0.4), 0 0 0 4rpx rgba(245, 108, 108, 0.15);
  }
}

.point-value {
  position: absolute;
  top: -48rpx;
  left: 50%;
  transform: translateX(-50%);
  font-size: 22rpx;
  font-weight: 700;
  color: #52C41A;
  white-space: nowrap;
  background: rgba(255, 255, 255, 0.95);
  padding: 6rpx 12rpx;
  border-radius: 12rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.chart-labels {
  position: absolute;
  bottom: 0;
  left: 32rpx;
  right: 32rpx;
  display: flex;
  justify-content: space-between;
}

.chart-label {
  font-size: 22rpx;
  color: #8C8C8C;
  font-weight: 500;
}

.alarm-section {
  padding: 0 24rpx;
  margin-bottom: 24rpx;
}

.alarm-list {
  background: linear-gradient(145deg, #FFFFFF 0%, #FFF9F9 100%);
  border-radius: 28rpx;
  padding: 24rpx;
  box-shadow: 0 12rpx 48rpx rgba(0, 0, 0, 0.08), 0 0 0 1rpx rgba(255, 255, 255, 0.9);
}

.alarm-item {
  display: flex;
  padding: 24rpx 0;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.06);
  transition: background 0.3s ease;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: rgba(0, 0, 0, 0.02);
    border-radius: 16rpx;
    margin: 0 -24rpx;
    padding-left: 24rpx;
    padding-right: 24rpx;
  }
}

.alarm-indicator {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  margin-top: 8rpx;
  flex-shrink: 0;
  box-shadow: 0 0 12rpx currentColor;

  &.level-error { 
    background: #FF4D4F; 
    color: #FF4D4F;
  }
  &.level-warning { 
    background: #FAAD14; 
    color: #FAAD14;
  }
  &.level-info { 
    background: #3B7CFF; 
    color: #3B7CFF;
  }
}

.alarm-content {
  flex: 1;
}

.alarm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.alarm-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.alarm-time {
  font-size: 24rpx;
  color: #BFBFBF;
  font-weight: 500;
}

.alarm-detail {
  font-size: 26rpx;
  color: #8C8C8C;
  line-height: 1.6;
}

.empty-alarm {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  padding: 80rpx 60rpx;
  text-align: center;
  box-shadow: 0 12rpx 48rpx rgba(0, 0, 0, 0.08), 0 0 0 1rpx rgba(255, 255, 255, 0.9);
}

.empty-text {
  font-size: 28rpx;
  color: #8C8C8C;
  font-weight: 500;
}

.test-records-section {
  padding: 0 24rpx;
  margin-bottom: 24rpx;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.record-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  padding: 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 0 0 1rpx rgba(255, 255, 255, 0.9);
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:active {
    transform: scale(0.99);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  }
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.record-sn {
  font-size: 30rpx;
  font-weight: 700;
  color: #3B7CFF;
  letter-spacing: -0.5rpx;
}

.record-body {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.record-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.record-label {
  font-size: 26rpx;
  color: #8C8C8C;
  font-weight: 500;
}

.record-value {
  font-size: 26rpx;
  color: #1F1F1F;
  font-weight: 600;
}

.record-test-results {
  display: flex;
  gap: 20rpx;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid rgba(0, 0, 0, 0.06);
}

.test-result {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 10rpx 20rpx;
  border-radius: 24rpx;

  &.pass {
    background: linear-gradient(135deg, rgba(103, 194, 58, 0.15) 0%, rgba(103, 194, 58, 0.05) 100%);
    .result-text { color: #52C41A; }
  }

  &.fail {
    background: linear-gradient(135deg, rgba(245, 108, 108, 0.15) 0%, rgba(245, 108, 108, 0.05) 100%);
    .result-text { color: #FF4D4F; }
  }
}

.result-icon {
  font-size: 28rpx;
}

.result-text {
  font-size: 24rpx;
  font-weight: 600;
}
</style>