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
        <u-search v-model="searchKey" placeholder="搜索任务号/检验员" @search="doSearch" @custom="doSearch" @clear="doSearch" shape="round"></u-search>
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
        <text class="stat-number">{{ formatNumber(summary.total) }}</text>
        <text class="stat-name">测试总数</text>
      </view>
      <view class="stat-card">
        <text class="stat-icon-wrap green">✓</text>
        <text class="stat-number success">{{ formatNumber(summary.passCount) }}</text>
        <text class="stat-name">通过数</text>
      </view>
      <view class="stat-card">
        <text class="stat-icon-wrap red">✗</text>
        <text class="stat-number error">{{ formatNumber(summary.failCount) }}</text>
        <text class="stat-name">失败数</text>
      </view>
    </view>

    <view class="test-section" v-if="currentFilter === 0 || currentFilter === 1">
      <view class="section-header">
        <text class="section-title">心率传感器</text>
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
              <text class="metric-icon">📏</text>
              <text class="metric-label">测试范围</text>
              <text class="metric-value">暂无</text>
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
        <text class="section-title">蓝牙连接</text>
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
              <text class="metric-icon">📋</text>
              <text class="metric-label">协议版本</text>
              <text class="metric-value">暂无</text>
            </view>
            <view class="metric-card">
              <text class="metric-icon">📡</text>
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
        <text class="section-title">电池充放电</text>
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
              <text class="metric-icon">📐</text>
              <text class="metric-label">标称容量</text>
              <text class="metric-value">暂无</text>
            </view>
            <view class="metric-card">
              <text class="metric-icon">✅</text>
              <text class="metric-label">实测容量</text>
              <text class="metric-value highlight">{{ battery.actualCapacity }}mAh</text>
            </view>
            <view class="metric-card">
              <text class="metric-icon">⚡</text>
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
        <text class="section-title">防水测试</text>
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
              <text class="metric-icon">🏷️</text>
              <text class="metric-label">防水等级</text>
              <text class="metric-value">{{ waterproof.level ? 'IP' + waterproof.level : '暂无' }}</text>
            </view>
            <view class="metric-card">
              <text class="metric-icon">🌊</text>
              <text class="metric-label">测试深度</text>
              <text class="metric-value highlight">{{ waterproof.depth }}m</text>
            </view>
            <view class="metric-card">
              <text class="metric-icon">⏰</text>
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
        <text class="section-title">今日测试趋势</text>
        <text class="section-sub">每小时通过率变化</text>
      </view>
      <view class="trend-chart">
        <template v-if="trendData.length > 0">
          <view class="chart-header-info">
            <view class="chart-info-item">
              <view class="info-dot green"></view>
              <text class="info-text">优良 95%以上</text>
            </view>
            <view class="chart-info-item">
              <view class="info-dot yellow"></view>
              <text class="info-text">正常 (90-95%)</text>
            </view>
            <view class="chart-info-item">
              <view class="info-dot red"></view>
              <text class="info-text">需关注 90%以下</text>
            </view>
          </view>
          <view class="chart-container">
            <view class="chart-y-axis">
              <text class="y-label" v-for="(label, idx) in ['100%', '75%', '50%', '25%', '0%']" :key="idx">{{ label }}</text>
            </view>
            <view class="chart-main">
              <view class="chart-grid">
                <view class="grid-line" v-for="i in 4" :key="i"></view>
              </view>

              <view class="chart-content">
                <svg class="chart-svg" :viewBox="`0 0 ${chartWidth} ${chartHeight}`" preserveAspectRatio="none">
                  <defs>
                    <linearGradient id="areaGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                      <stop offset="0%" stop-color="rgba(82, 196, 26, 0.5)" />
                      <stop offset="30%" stop-color="rgba(82, 196, 26, 0.3)" />
                      <stop offset="60%" stop-color="rgba(115, 209, 61, 0.15)" />
                      <stop offset="100%" stop-color="rgba(115, 209, 61, 0.03)" />
                    </linearGradient>
                    <linearGradient id="lineGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                      <stop offset="0%" stop-color="#52C41A" />
                      <stop offset="50%" stop-color="#73D13D" />
                      <stop offset="100%" stop-color="#95DE64" />
                    </linearGradient>
                    <filter id="glow">
                      <feGaussianBlur stdDeviation="4" result="coloredBlur"/>
                      <feMerge>
                        <feMergeNode in="coloredBlur"/>
                        <feMergeNode in="SourceGraphic"/>
                      </feMerge>
                    </filter>
                    <filter id="pointGlow">
                      <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
                      <feMerge>
                        <feMergeNode in="coloredBlur"/>
                        <feMergeNode in="SourceGraphic"/>
                      </feMerge>
                    </filter>
                  </defs>
                  <path :d="smoothAreaPath" fill="url(#areaGradient)" opacity="0.8" />
                  <path :d="smoothLinePath" fill="none" stroke="url(#lineGradient)" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" filter="url(#glow)" />
                  <path :d="smoothLinePath" fill="none" stroke="#52C41A" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
                  <g v-for="(point, idx) in trendData" :key="idx">
                    <circle
                      :cx="getPointX(idx)"
                      :cy="getPointY(point.rate)"
                      r="8"
                      :fill="getPointColor(point.rate)"
                      stroke="#ffffff"
                      stroke-width="3"
                      filter="url(#pointGlow)"
                      class="data-point"
                    />
                    <circle
                      :cx="getPointX(idx)"
                      :cy="getPointY(point.rate)"
                      r="4"
                      fill="#ffffff"
                    />
                    <rect
                      :x="getPointX(idx) - 20"
                      :y="getPointY(point.rate) - 35"
                      width="40"
                      height="24"
                      rx="12"
                      fill="#ffffff"
                      stroke="rgba(82, 196, 26, 0.2)"
                      stroke-width="1"
                    />
                    <text
                      :x="getPointX(idx)"
                      :y="getPointY(point.rate) - 20"
                      text-anchor="middle"
                      fill="#52C41A"
                      font-size="11"
                      font-weight="700"
                    >{{ point.rate }}%</text>
                  </g>
                </svg>
              </view>
              <view class="chart-x-labels">
                <view class="x-label-wrap" v-for="(item, idx) in trendData" :key="idx">
                  <view class="x-tick"></view>
                  <text class="x-label">{{ item.time }}</text>
                </view>
              </view>
            </view>
          </view>
          <view class="chart-summary">
            <view class="summary-item">
              <text class="summary-label">最高通过率</text>
              <text class="summary-value highlight">{{ maxRate }}%</text>
            </view>
            <view class="summary-item">
              <text class="summary-label">最低通过率</text>
              <text class="summary-value" :class="minRate < 95 ? 'warning' : ''">{{ minRate }}%</text>
            </view>
            <view class="summary-item">
              <text class="summary-label">平均通过率</text>
              <text class="summary-value">{{ avgRate }}%</text>
            </view>
          </view>
        </template>
        <view v-else class="chart-empty">
          <text class="chart-empty-text">暂无趋势数据</text>
        </view>
      </view>
    </view>

    <view class="alarm-section" v-if="currentFilter === 0">
      <view class="section-header">
        <text class="section-title">测试异常告警</text>
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
        <text class="section-title">搜索结果</text>
        <text class="section-count">找到 {{ searchResults.length }} 条记录</text>
      </view>
      <view class="record-list">
        <view class="record-card" v-for="item in searchResults" :key="item.id">
          <view class="record-header">
            <text class="record-sn">{{ item.taskNo }}</text>
            <u-tag :text="item.statusText" :type="item.statusType" size="mini"></u-tag>
          </view>
          <view class="record-body">
            <view class="record-row">
              <text class="record-label">任务类型</text>
              <text class="record-value">{{ item.taskTypeName }}</text>
            </view>
            <view class="record-row">
              <text class="record-label">检验员</text>
              <text class="record-value">{{ item.inspectorName || '-' }}</text>
            </view>
            <view class="record-row">
              <text class="record-label">创建时间</text>
              <text class="record-value">{{ item.createTime }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    </view>
  </view>
</template>

<script>
import { get } from '@/utils/request'
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
        passRate: 0,
        accuracy: 0,
        responseTime: 0,
        todayPass: 0,
        todayFail: 0,
        total: 0
      },
      bluetooth: {
        passRate: 0,
        range: 0,
        connectTime: 0,
        todayPass: 0,
        todayFail: 0,
        total: 0
      },
      battery: {
        passRate: 0,
        actualCapacity: 0,
        chargeTime: 0,
        todayPass: 0,
        todayFail: 0,
        total: 0
      },
      waterproof: {
        passRate: 0,
        level: 0,
        depth: 0,
        duration: 0,
        todayPass: 0,
        todayFail: 0,
        total: 0
      },
      trendData: [],
      alarmList: [],
      testRecords: [],
      searchResults: [],
      chartWidth: 600,
      chartHeight: 280
    }
  },
  computed: {
    filteredAlarms() {
      if (!this.searchKey) return this.alarmList
      const key = this.searchKey.toLowerCase()
      return this.alarmList.filter(item =>
        (item.title && item.title.toLowerCase().includes(key)) ||
        (item.detail && item.detail.toLowerCase().includes(key))
      )
    },
    linePath() {
      if (this.trendData.length === 0) return ''
      const points = this.trendData.map((item, idx) => {
        return `${this.getPointX(idx)},${this.getPointY(item.rate)}`
      })
      return `M ${points.join(' L ')}`
    },
    smoothLinePath() {
      if (this.trendData.length < 2) return this.linePath()
      const points = this.trendData.map((item, idx) => ({
        x: this.getPointX(idx),
        y: this.getPointY(item.rate)
      }))
      let path = `M ${points[0].x},${points[0].y}`
      for (let i = 0; i < points.length - 1; i++) {
        const current = points[i]
        const next = points[i + 1]
        const cpx1 = current.x + (next.x - current.x) / 3
        const cpy1 = current.y
        const cpx2 = current.x + 2 * (next.x - current.x) / 3
        const cpy2 = next.y
        path += ` C ${cpx1},${cpy1} ${cpx2},${cpy2} ${next.x},${next.y}`
      }
      return path
    },
    smoothAreaPath() {
      if (this.trendData.length < 2) return this.areaPath()
      const points = this.trendData.map((item, idx) => ({
        x: this.getPointX(idx),
        y: this.getPointY(item.rate)
      }))
      let path = `M ${points[0].x},${this.chartHeight}`
      path += ` L ${points[0].x},${points[0].y}`
      for (let i = 0; i < points.length - 1; i++) {
        const current = points[i]
        const next = points[i + 1]
        const cpx1 = current.x + (next.x - current.x) / 3
        const cpy1 = current.y
        const cpx2 = current.x + 2 * (next.x - current.x) / 3
        const cpy2 = next.y
        path += ` C ${cpx1},${cpy1} ${cpx2},${cpy2} ${next.x},${next.y}`
      }
      path += ` L ${points[points.length - 1].x},${this.chartHeight} Z`
      return path
    },
    areaPath() {
      if (this.trendData.length === 0) return ''
      const points = this.trendData.map((item, idx) => {
        return `${this.getPointX(idx)},${this.getPointY(item.rate)}`
      })
      const startX = this.getPointX(0)
      const endX = this.getPointX(this.trendData.length - 1)
      return `M ${startX},${this.chartHeight} L ${points.join(' L ')} L ${endX},${this.chartHeight} Z`
    },
    maxRate() {
      if (this.trendData.length === 0) return 0
      return Math.max(...this.trendData.map(item => item.rate))
    },
    minRate() {
      if (this.trendData.length === 0) return 0
      return Math.min(...this.trendData.map(item => item.rate))
    },
    avgRate() {
      if (this.trendData.length === 0) return 0
      const sum = this.trendData.reduce((acc, item) => acc + item.rate, 0)
      return (sum / this.trendData.length).toFixed(1)
    }
  },
  onShow() {
    this.loadStatistics()
    this.loadTestRecords()
  },
  onReady() {
    this.initChartSize()
  },
  methods: {
    async loadStatistics() {
      try {
        const res = await get('/mini/quality/statistics')
        if (res) {
          const total = Number(res.total) || 0
          const passed = Number(res.passed) || 0
          const failed = Number(res.failed) || 0
          this.summary.total = total
          this.summary.passCount = passed
          this.summary.failCount = failed
          this.summary.passRate = total > 0 ? Number(((passed / total) * 100).toFixed(1)) : 0
        }
      } catch (e) {
        // 接口异常时保持0值
      }
    },
    async loadTestRecords() {
      try {
        const res = await get('/quality-task/list', { pageNum: 1, pageSize: 50 })
        const records = (res && res.records) || res || []
        this.testRecords = records.map(item => ({
          ...item,
          statusText: this.getStatusText(item.status),
          statusType: this.getStatusType(item.status),
          taskTypeName: this.getTypeName(item.taskType)
        }))
      } catch (e) {
        this.testRecords = []
      }
    },
    getStatusText(s) {
      const m = { 0: '待处理', 1: '处理中', 2: '已完成' }
      return m[s] || '未知'
    },
    getStatusType(s) {
      const m = { 0: 'info', 1: 'primary', 2: 'success' }
      return m[s] || 'info'
    },
    getTypeName(t) {
      const m = { incoming: '来料检验', process: '过程检验', finished: '成品检验' }
      return m[t] || t || '未知'
    },
    formatNumber(num) {
      const n = Number(num) || 0
      return n.toLocaleString()
    },
    initChartSize() {
      const sysInfo = uni.getSystemInfoSync()
      this.chartWidth = sysInfo.windowWidth - 140
      this.chartHeight = 280
    },
    getPointX(index) {
      const total = this.trendData.length
      if (total <= 1) return this.chartWidth / 2
      const padding = 30
      const availableWidth = this.chartWidth - padding * 2
      return padding + (index / (total - 1)) * availableWidth
    },
    getPointY(rate) {
      const padding = 20
      const availableHeight = this.chartHeight - padding * 2
      return this.chartHeight - padding - (rate / 100) * availableHeight
    },
    getPointColor(rate) {
      if (rate >= 95) return '#52C41A'
      if (rate >= 90) return '#FAAD14'
      return '#FF4D4F'
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
        (item.taskNo && item.taskNo.toLowerCase().includes(key)) ||
        (item.inspectorName && item.inspectorName.toLowerCase().includes(key)) ||
        (item.taskTypeName && item.taskTypeName.toLowerCase().includes(key))
      )
    },
    getRingRotation(percent) {
      return (Number(percent) / 100) * 360
    }
  }
}
</script>

<style lang="scss" scoped>
.test-dashboard-page {
  min-height: 100vh;
  background: #F5F7FA;
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
  position: relative;
  z-index: 10;
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
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -30%;
    width: 400rpx;
    height: 400rpx;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(103, 194, 58, 0.15) 0%, transparent 70%);
  }
  
  &::after {
    content: '';
    position: absolute;
    bottom: -30%;
    left: -20%;
    width: 300rpx;
    height: 300rpx;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(59, 124, 255, 0.1) 0%, transparent 70%);
  }
}

.chart-header-info {
  display: flex;
  justify-content: center;
  gap: 32rpx;
  margin-bottom: 28rpx;
  position: relative;
  z-index: 10;
}

.chart-info-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.info-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  
  &.green { 
    background: #52C41A; 
    box-shadow: 0 0 12rpx rgba(82, 196, 26, 0.5);
  }
  &.yellow { 
    background: #FAAD14; 
    box-shadow: 0 0 12rpx rgba(250, 173, 20, 0.5);
  }
  &.red { 
    background: #FF4D4F; 
    box-shadow: 0 0 12rpx rgba(245, 108, 108, 0.5);
  }
}

.info-text {
  font-size: 24rpx;
  color: #8C8C8C;
  font-weight: 500;
}

.chart-container {
  position: relative;
  z-index: 10;
  display: flex;
  height: 400rpx;
}

.chart-y-axis {
  width: 80rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding-top: 20rpx;
  padding-bottom: 40rpx;
}

.y-label {
  font-size: 22rpx;
  color: #8C8C8C;
  font-weight: 500;
  text-align: right;
  padding-right: 12rpx;
}

.chart-main {
  flex: 1;
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
}

.grid-line {
  height: 1rpx;
  background: linear-gradient(90deg, transparent 0%, rgba(103, 194, 58, 0.1) 50%, transparent 100%);
}



.chart-content {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 40rpx;
}

.chart-svg {
  width: 100%;
  height: 100%;
}

.chart-x-labels {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
}

.x-label-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.x-tick {
  width: 1rpx;
  height: 12rpx;
  background: rgba(103, 194, 58, 0.2);
  margin-bottom: 8rpx;
}

.x-label {
  font-size: 22rpx;
  color: #8C8C8C;
  font-weight: 500;
}

.chart-summary {
  display: flex;
  justify-content: space-around;
  margin-top: 28rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 10;
}

.summary-item {
  text-align: center;
  padding: 0 20rpx;
}

.summary-label {
  font-size: 24rpx;
  color: #8C8C8C;
  font-weight: 500;
  display: block;
  margin-bottom: 10rpx;
}

.summary-value {
  font-size: 36rpx;
  font-weight: 700;
  color: #52C41A;
  display: block;
  letter-spacing: -1rpx;
  text-shadow: 0 2rpx 8rpx rgba(82, 196, 26, 0.3);
  
  &.highlight {
    color: #52C41A;
    font-size: 40rpx;
  }
  
  &.warning {
    color: #FAAD14;
    text-shadow: 0 2rpx 8rpx rgba(250, 173, 20, 0.3);
  }
}

.data-point {
  cursor: pointer;
  transition: transform 0.2s ease;

  &:hover {
    transform: scale(1.2);
  }
}

.chart-empty {
  padding: 80rpx 40rpx;
  text-align: center;
  position: relative;
  z-index: 10;
}

.chart-empty-text {
  font-size: 28rpx;
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

@keyframes float {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.1;
  }
  50% {
    transform: translateY(-20rpx) scale(1.1);
    opacity: 0.15;
  }
}
</style>