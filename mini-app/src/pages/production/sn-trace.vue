﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <view class="sn-trace-page">
    <DynamicBackground theme="theme-primary" fixed></DynamicBackground>
    <view class="page-content">
    <!-- 顶部查询 -->
    <view class="search-section">
      <view class="search-card">
        <view class="search-title">
          <text class="title-icon">🔍</text>
          <text class="title-text">SN全生命周期追溯</text>
        </view>
        <view class="mode-tabs">
          <view 
            class="mode-tab" 
            :class="{ active: traceMode === 'reverse' }"
            @tap="traceMode = 'reverse'"
          >
            逆向追溯(SN→全链路)
          </view>
          <view 
            class="mode-tab" 
            :class="{ active: traceMode === 'forward' }"
            @tap="traceMode = 'forward'"
          >
            正向追溯(批次→成品)
          </view>
        </view>
        <view class="input-row">
          <input 
            class="search-input" 
            v-model="searchCode" 
            :placeholder="traceMode === 'reverse' ? '请输入成品SN码' : '请输入物料批次号'"
            confirm-type="search"
            @confirm="doSearch"
          />
          <view class="scan-btn" @tap="handleScan">
            <text>📷</text>
          </view>
        </view>
        <view class="quick-list">
          <text class="quick-label">快速查询：</text>
          <text 
            class="quick-item"
            v-for="item in quickList"
            :key="item.code"
            @tap="quickSearch(item)"
          >
            {{ item.label }}
          </text>
        </view>
        <view class="search-btn" @tap="doSearch">
          <text>{{ loading ? '查询中...' : '立即追溯' }}</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-if="!hasSearched" class="empty-section">
      <view class="empty-icon">🌐</view>
      <view class="empty-text">输入SN码或扫码开始追溯</view>
      <view class="empty-sub">支持双向追溯，快速定位产品信息</view>
    </view>

    <!-- 逆向追溯结果 -->
    <view v-else-if="traceMode === 'reverse'" class="result-section">
      <!-- 未找到 -->
      <view v-if="!reverseData?.exists" class="not-found-card">
        <view class="nf-icon">❓</view>
        <view class="nf-title">未找到SN码信息</view>
        <view class="nf-desc">{{ reverseData?.message }}</view>
      </view>

      <template v-else>
        <!-- SN基本信息 -->
        <view class="info-card">
          <view class="card-header">
            <text class="card-icon">⌚</text>
            <text class="card-title">产品SN档案</text>
            <text v-if="reverseData.stats?.hasException" class="status-badge danger">⚠ 异常</text>
            <text v-else class="status-badge success">✓ 正常</text>
          </view>
          <view class="sn-main">
            <text class="sn-code">{{ reverseData.snInfo.snCode }}</text>
            <text class="sn-name">{{ reverseData.snInfo.productName }}</text>
          </view>
          <view class="info-grid">
            <view class="info-item">
              <text class="info-label">产品编码</text>
              <text class="info-value">{{ reverseData.snInfo.productCode }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">所属工单</text>
              <text class="info-value">{{ reverseData.snInfo.workOrderNo }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">关联订单</text>
              <text class="info-value">{{ reverseData.snInfo.orderNo || '-' }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">客户名称</text>
              <text class="info-value">{{ reverseData.snInfo.customerName || '-' }}</text>
            </view>
          </view>
        </view>

        <!-- 统计 -->
        <view class="stats-row">
          <view class="stat-item">
            <text class="stat-num">{{ reverseData.stats?.materialCount || 0 }}</text>
            <text class="stat-label">物料批次</text>
          </view>
          <view class="stat-item">
            <text class="stat-num">{{ reverseData.stats?.processCount || 0 }}</text>
            <text class="stat-label">加工工序</text>
          </view>
          <view class="stat-item">
            <text class="stat-num success">{{ reverseData.stats?.qualityPassCount || 0 }}</text>
            <text class="stat-label">合格项</text>
          </view>
          <view class="stat-item">
            <text class="stat-num danger">{{ reverseData.stats?.qualityFailCount || 0 }}</text>
            <text class="stat-label">不合格</text>
          </view>
        </view>

        <!-- 物料明细 -->
        <view class="info-card">
          <view class="card-header">
            <text class="card-icon">🧩</text>
            <text class="card-title">物料明细</text>
          </view>
          <view class="mat-list">
            <view class="mat-item" v-for="(mat, i) in reverseData.materialList" :key="i">
              <view class="mat-left">
                <text class="mat-name">{{ mat.materialName }}</text>
                <text class="mat-code">{{ mat.materialCode }}</text>
              </view>
              <view class="mat-right">
                <text class="batch-tag">{{ mat.batchNo }}</text>
                <text class="mat-op">操作工：{{ mat.operatorName }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 生产过程 -->
        <view class="info-card">
          <view class="card-header">
            <text class="card-icon">⚙️</text>
            <text class="card-title">生产过程</text>
          </view>
          <view class="process-timeline">
            <view 
              class="proc-item" 
              v-for="(proc, i) in reverseData.processList" 
              :key="i"
              :class="{ 'is-exception': proc.status === 2 }"
            >
              <view class="proc-dot">
                <text v-if="proc.status === 1">✓</text>
                <text v-else-if="proc.status === 2">!</text>
                <text v-else>○</text>
              </view>
              <view class="proc-content">
                <view class="proc-top">
                  <text class="proc-name">{{ proc.processName }}</text>
                  <text 
                    class="proc-status"
                    :class="'status-' + (proc.status === 1 ? 'success' : proc.status === 2 ? 'danger' : 'info')"
                  >
                    {{ proc.status === 1 ? '已完成' : proc.status === 2 ? '异常' : '进行中' }}
                  </text>
                </view>
                <text class="proc-info">👤 {{ proc.workerName }} · 🔧 {{ proc.equipmentName }}</text>
                <text class="proc-info">🕐 {{ formatTime(proc.startTime) }} ~ {{ formatTime(proc.endTime) }}</text>
                <text v-if="proc.remark" class="proc-remark">💬 {{ proc.remark }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 质检记录 -->
        <view class="info-card">
          <view class="card-header">
            <text class="card-icon">📋</text>
            <text class="card-title">质检记录</text>
          </view>
          <view class="qc-list">
            <view 
              class="qc-item" 
              v-for="(q, i) in reverseData.qualityList" 
              :key="i"
              :class="{ 'qc-fail': q.result === 0 }"
            >
              <view class="qc-top">
                <text class="qc-type qc-type-{{ q.taskType }}">{{ q.taskType }}</text>
                <text class="qc-item-name">{{ q.checkItem }}</text>
                <text class="qc-result" :class="q.result === 1 ? 'pass' : 'fail'">
                  {{ q.result === 1 ? '合格' : '不合格' }}
                </text>
              </view>
              <view class="qc-detail">
                <text>标准：{{ q.standardValue }}</text>
                <text>实际：{{ q.actualValue }}</text>
              </view>
              <view class="qc-footer">
                <text>检验员：{{ q.inspectorName }}</text>
                <text>{{ formatTime(q.checkTime) }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 归属信息 -->
        <view class="info-card">
          <view class="card-header">
            <text class="card-icon">🏢</text>
            <text class="card-title">归属信息</text>
          </view>
          <view class="owner-list">
            <view class="owner-item">
              <text class="owner-label">客户订单</text>
              <text class="owner-value">{{ reverseData.snInfo.orderNo || '-' }}</text>
            </view>
            <view class="owner-item">
              <text class="owner-label">客户名称</text>
              <text class="owner-value">{{ reverseData.snInfo.customerName || '-' }}</text>
            </view>
            <view class="owner-item">
              <text class="owner-label">生产工单</text>
              <text class="owner-value">{{ reverseData.snInfo.workOrderNo }}</text>
            </view>
            <view class="owner-item">
              <text class="owner-label">产品型号</text>
              <text class="owner-value">{{ reverseData.snInfo.productName }}</text>
            </view>
          </view>
        </view>
      </template>
    </view>

    <!-- 正向追溯结果 -->
    <view v-else class="result-section">
      <view v-if="!forwardData?.exists" class="not-found-card">
        <view class="nf-icon">❓</view>
        <view class="nf-title">未找到该批次物料</view>
        <view class="nf-desc">{{ forwardData?.message }}</view>
      </view>

      <template v-else>
        <view class="info-card">
          <view class="card-header">
            <text class="card-icon">📦</text>
            <text class="card-title">物料批次信息</text>
          </view>
          <view class="sn-main">
            <text class="sn-code">{{ forwardData.batchInfo.batchNo }}</text>
            <text class="sn-name">{{ forwardData.batchInfo.materialName }}</text>
          </view>
          <view class="info-grid">
            <view class="info-item">
              <text class="info-label">物料编码</text>
              <text class="info-value">{{ forwardData.batchInfo.materialCode }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">涉及成品数</text>
              <text class="info-value highlight">{{ forwardData.batchInfo.totalSnCount }} 个SN</text>
            </view>
          </view>
        </view>

        <view class="info-card" v-for="(group, gi) in forwardData.workOrderGroups" :key="gi">
          <view class="card-header">
            <text class="card-icon">📋</text>
            <text class="card-title">{{ group.workOrderNo }}</text>
          </view>
          <view class="group-sub">{{ group.productName }} · {{ group.snCount }} 个成品</view>
          <view class="sn-tag-list">
            <text 
              class="sn-tag"
              v-for="sn in group.snCodes"
              :key="sn"
              @tap="jumpToReverse(sn)"
            >
              {{ sn }}
            </text>
          </view>
        </view>
      </template>
    </view>

    <view class="bottom-space"></view>
    </view>
  </view>
</template>

<script>
import DynamicBackground from '@/components/DynamicBackground'
import { getSnReverseTrace, getSnForwardTrace } from '../../api/production'

export default {
  components: { DynamicBackground },
  data() {
    return {
      traceMode: 'reverse',
      searchCode: '',
      loading: false,
      hasSearched: false,
      reverseData: {},
      forwardData: {}
    }
  },
  computed: {
    quickList() {
      if (this.traceMode === 'reverse') {
        return [
          { code: 'SN202607010001', label: '标准手环', mode: 'reverse' },
          { code: 'SN202607010003', label: '含异常', mode: 'reverse' }
        ]
      } else {
        return [
          { code: 'BATCH2026062001', label: '芯片批次1', mode: 'forward' },
          { code: 'BATCH2026062501', label: '电池批次1', mode: 'forward' }
        ]
      }
    }
  },
  onLoad(options) {
    if (options.snCode) {
      this.searchCode = options.snCode
      this.traceMode = 'reverse'
      this.doSearch()
    }
  },
  methods: {
    handleScan() {
      uni.scanCode({
        success: (res) => {
          this.searchCode = res.result
          this.doSearch()
        }
      })
    },
    quickSearch(item) {
      this.traceMode = item.mode
      this.searchCode = item.code
      this.doSearch()
    },
    async doSearch() {
      if (!this.searchCode.trim()) {
        uni.showToast({ title: '请输入查询内容', icon: 'none' })
        return
      }
      this.loading = true
      this.hasSearched = true
      try {
        if (this.traceMode === 'reverse') {
          const res = await getSnReverseTrace(this.searchCode.trim())
          this.reverseData = res || {}
        } else {
          const res = await getSnForwardTrace(this.searchCode.trim())
          this.forwardData = res || {}
        }
      } catch (e) {
        uni.showToast({ title: '查询失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    jumpToReverse(sn) {
      this.traceMode = 'reverse'
      this.searchCode = sn
      this.doSearch()
    },
    formatTime(date) {
      if (!date) return '-'
      const d = new Date(date)
      if (isNaN(d.getTime())) return date
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const h = String(d.getHours()).padStart(2, '0')
      const min = String(d.getMinutes()).padStart(2, '0')
      return `${m}-${day} ${h}:${min}`
    }
  }
}
</script>

<style lang="scss" scoped>
.sn-trace-page {
  min-height: 100vh;
  background: #F5F7FA;
  padding-bottom: 40rpx;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
}

.search-section {
  padding: 30rpx;
}

.search-card {
  background: rgba(255, 255, 255, 0.95);
  border: 1rpx solid rgba(59, 124, 255, 0.2);
  border-radius: 28rpx;
  padding: 30rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.search-title {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}
.title-icon { font-size: 36rpx; margin-right: 12rpx; }
.title-text {
  font-size: 32rpx;
  font-weight: bold;
  color: #3B7CFF;
}

.mode-tabs {
  display: flex;
  background: #f5f7fa;
  border-radius: 12rpx;
  padding: 6rpx;
  margin-bottom: 24rpx;
}
.mode-tab {
  flex: 1;
  text-align: center;
  padding: 14rpx 0;
  font-size: 24rpx;
  color: #8C8C8C;
  border-radius: 10rpx;
  transition: all 0.2s;
}
.mode-tab.active {
  background: linear-gradient(90deg, #3B7CFF, #6BA1FF);
  color: #fff;
  font-weight: bold;
}

.input-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 20rpx;
}
.search-input {
  flex: 1;
  height: 80rpx;
  background: #f5f7fa;
  border: 1rpx solid rgba(59, 124, 255, 0.2);
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #4A4A4A;
}
.scan-btn {
  width: 80rpx;
  height: 80rpx;
  background: rgba(59, 124, 255, 0.1);
  border: 1rpx solid rgba(59, 124, 255, 0.3);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
}

.quick-list {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 24rpx;
}
.quick-label {
  font-size: 24rpx;
  color: #8C8C8C;
  margin-right: 12rpx;
}
.quick-item {
  font-size: 22rpx;
  color: #3B7CFF;
  background: rgba(59, 124, 255, 0.08);
  border: 1rpx solid rgba(59, 124, 255, 0.2);
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  margin-right: 12rpx;
  margin-bottom: 8rpx;
}

.search-btn {
  height: 80rpx;
  background: linear-gradient(90deg, #3B7CFF, #6BA1FF);
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  color: #fff;
  font-weight: bold;
  box-shadow: 0 8rpx 20rpx rgba(59, 124, 255, 0.3);
}

.empty-section {
  text-align: center;
  padding: 120rpx 40rpx;
}
.empty-icon { font-size: 120rpx; opacity: 0.5; }
.empty-text { font-size: 28rpx; color: #8C8C8C; margin-top: 24rpx; }
.empty-sub { font-size: 24rpx; color: #BFBFBF; margin-top: 12rpx; }

.not-found-card {
  margin: 40rpx 30rpx;
  background: rgba(255, 255, 255, 0.95);
  border: 1rpx solid rgba(245, 108, 108, 0.3);
  border-radius: 28rpx;
  padding: 60rpx 30rpx;
  text-align: center;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}
.nf-icon { font-size: 80rpx; }
.nf-title { font-size: 30rpx; color: #FF4D4F; margin-top: 16rpx; }
.nf-desc { font-size: 24rpx; color: #8C8C8C; margin-top: 8rpx; }

.result-section {
  padding: 0 30rpx;
}

.info-card {
  background: rgba(255, 255, 255, 0.95);
  border: 1rpx solid rgba(59, 124, 255, 0.15);
  border-radius: 28rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  background: linear-gradient(90deg, rgba(59, 124, 255, 0.08), transparent);
  border-bottom: 1rpx solid rgba(59, 124, 255, 0.1);
}
.card-icon { font-size: 28rpx; margin-right: 10rpx; }
.card-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #3B7CFF;
}
.status-badge {
  margin-left: auto;
  font-size: 22rpx;
  padding: 4rpx 14rpx;
  border-radius: 20rpx;
}
.status-badge.success { background: rgba(0, 255, 150, 0.15); color: #00ffaa; }
.status-badge.danger { background: rgba(255, 80, 80, 0.15); color: #ff7777; }

.sn-main {
  text-align: center;
  padding: 24rpx 24rpx 16rpx;
}
.sn-code {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  font-family: monospace;
  background: linear-gradient(90deg, #3B7CFF, #6BA1FF);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 2rpx;
}
.sn-name {
  display: block;
  font-size: 24rpx;
  color: #8C8C8C;
  margin-top: 8rpx;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12rpx;
  padding: 16rpx 24rpx 24rpx;
}
.info-item {
  background: #f5f7fa;
  padding: 14rpx 16rpx;
  border-radius: 10rpx;
  border-left: 4rpx solid #3B7CFF;
}
.info-label { display: block; font-size: 22rpx; color: #8C8C8C; margin-bottom: 4rpx; }
.info-value { font-size: 24rpx; color: #4A4A4A; }
.info-value.highlight { color: #FF8C00; font-weight: bold; }

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
  margin-bottom: 24rpx;
}
.stat-item {
  background: rgba(255, 255, 255, 0.95);
  border: 1rpx solid rgba(59, 124, 255, 0.2);
  border-radius: 20rpx;
  padding: 18rpx 10rpx;
  text-align: center;
  box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.06);
}
.stat-num {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #3B7CFF;
}
.stat-num.success { color: #52C41A; }
.stat-num.danger { color: #FF4D4F; }
.stat-label {
  display: block;
  font-size: 20rpx;
  color: #8C8C8C;
  margin-top: 4rpx;
}

/* 物料列表 */
.mat-list { padding: 12rpx 24rpx 20rpx; }
.mat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}
.mat-item:last-child { border-bottom: none; }
.mat-name {
  display: block;
  font-size: 26rpx;
  color: #4A4A4A;
  margin-bottom: 4rpx;
}
.mat-code {
  font-size: 22rpx;
  color: #8C8C8C;
  font-family: monospace;
}
.mat-right { text-align: right; }
.batch-tag {
  display: inline-block;
  font-size: 22rpx;
  color: #FF8C00;
  background: rgba(255, 140, 0, 0.08);
  border: 1rpx solid rgba(255, 140, 0, 0.2);
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  margin-bottom: 6rpx;
}
.mat-op { display: block; font-size: 20rpx; color: #BFBFBF; }

/* 生产过程时间轴 */
.process-timeline {
  padding: 16rpx 24rpx 20rpx 40rpx;
  position: relative;
}
.proc-item {
  position: relative;
  padding-left: 28rpx;
  padding-bottom: 20rpx;
  border-left: 2rpx solid rgba(59, 124, 255, 0.3);
}
.proc-item:last-child { border-left-color: transparent; padding-bottom: 0; }
.proc-dot {
  position: absolute;
  left: -14rpx;
  top: 0;
  width: 26rpx;
  height: 26rpx;
  border-radius: 50%;
  background: #fff;
  border: 3rpx solid #3B7CFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18rpx;
  color: #3B7CFF;
  font-weight: bold;
}
.proc-item.is-exception .proc-dot {
  border-color: #FF4D4F;
  color: #FF4D4F;
}
.proc-content {
  background: #fafafa;
  border: 1rpx solid rgba(59, 124, 255, 0.1);
  border-radius: 12rpx;
  padding: 14rpx 16rpx;
}
.proc-top {
  display: flex;
  align-items: center;
  margin-bottom: 6rpx;
}
.proc-name { font-size: 26rpx; color: #4A4A4A; font-weight: bold; margin-right: 12rpx; }
.proc-status { font-size: 20rpx; padding: 2rpx 10rpx; border-radius: 16rpx; }
.proc-status.status-success { background: rgba(82, 196, 26, 0.08); color: #52C41A; }
.proc-status.status-danger { background: rgba(245, 108, 108, 0.08); color: #FF4D4F; }
.proc-status.status-info { background: rgba(59, 124, 255, 0.08); color: #3B7CFF; }
.proc-info {
  display: block;
  font-size: 22rpx;
  color: #8C8C8C;
  line-height: 1.6;
}
.proc-remark {
  display: block;
  font-size: 22rpx;
  color: #FF8C00;
  margin-top: 6rpx;
}

/* 质检列表 */
.qc-list { padding: 12rpx 24rpx 20rpx; }
.qc-item {
  background: #fafafa;
  border: 1rpx solid rgba(59, 124, 255, 0.1);
  border-radius: 12rpx;
  padding: 14rpx 16rpx;
  margin-bottom: 12rpx;
}
.qc-item:last-child { margin-bottom: 0; }
.qc-item.qc-fail {
  background: rgba(245, 108, 108, 0.05);
  border-color: rgba(245, 108, 108, 0.2);
}
.qc-top {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}
.qc-type {
  font-size: 20rpx;
  padding: 2rpx 10rpx;
  border-radius: 16rpx;
  margin-right: 12rpx;
}
.qc-type-IQC { background: rgba(59, 124, 255, 0.08); color: #3B7CFF; }
.qc-type-PQC { background: rgba(255, 140, 0, 0.08); color: #FF8C00; }
.qc-type-OQC { background: rgba(82, 196, 26, 0.08); color: #52C41A; }
.qc-item-name { flex: 1; font-size: 26rpx; color: #4A4A4A; }
.qc-result { font-size: 22rpx; font-weight: bold; }
.qc-result.pass { color: #52C41A; }
.qc-result.fail { color: #FF4D4F; }
.qc-detail {
  display: flex;
  justify-content: space-between;
  font-size: 22rpx;
  color: #8C8C8C;
  margin-bottom: 6rpx;
}
.qc-footer {
  display: flex;
  justify-content: space-between;
  font-size: 20rpx;
  color: #BFBFBF;
}

/* 归属 */
.owner-list { padding: 12rpx 24rpx 20rpx; }
.owner-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}
.owner-item:last-child { border-bottom: none; }
.owner-label { font-size: 24rpx; color: #8C8C8C; }
.owner-value { font-size: 26rpx; color: #4A4A4A; }

.group-sub {
  padding: 0 24rpx;
  font-size: 22rpx;
  color: #8C8C8C;
  margin-bottom: 12rpx;
}

.sn-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  padding: 0 24rpx 24rpx;
}
.sn-tag {
  font-size: 22rpx;
  color: #3B7CFF;
  background: rgba(59, 124, 255, 0.08);
  border: 1rpx solid rgba(59, 124, 255, 0.2);
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  font-family: monospace;
}

.bottom-space { height: 40rpx; }
</style>
