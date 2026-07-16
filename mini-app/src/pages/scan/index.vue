<template>
  <view class="scan-page">
    <scroll-view class="page-body" scroll-y>
      <view class="page-header" :style="{ paddingTop: statusBarPadding }">
        <view class="header-bg"></view>
        <view class="header-wave"></view>
        <view class="header-content">
          <view class="title-area">
            <text class="page-title">扫码操作</text>
            <text class="page-desc">扫码查信息 → 扫码看规范 → 扫码报异常</text>
          </view>
          <view class="scan-btn" @click="handleQuickScan">
            <view class="scan-ring"></view>
            <view class="scan-circle">
              <text class="scan-icon">扫</text>
            </view>
            <text class="scan-text">快捷扫码</text>
          </view>
        </view>
      </view>
      <view class="core-section">
        <view class="section-header">
          <text class="section-title">核心功能</text>
        </view>
        <view class="core-grid">
          <view class="core-card" @click="goTrace">
            <view class="core-icon blue">
              <text class="icon-text">溯</text>
            </view>
            <text class="core-title">SN码追溯</text>
            <text class="core-desc">扫描成品SN码，查看完整产品履历</text>
            <view class="core-badge">一物一码</view>
          </view>
          <view class="core-card" @click="goSop">
            <view class="core-icon green">
              <text class="icon-text">规</text>
            </view>
            <text class="core-title">工位SOP</text>
            <text class="core-desc">扫工位码，查看电子作业指导书</text>
            <view class="core-badge">标准作业</view>
          </view>
          <view class="core-card" @click="goException">
            <view class="core-icon orange">
              <text class="icon-text">异</text>
            </view>
            <text class="core-title">异常快报</text>
            <text class="core-desc">拍照+语音，快速上报现场异常</text>
            <view class="core-badge">实时响应</view>
          </view>
        </view>
      </view>

      <view class="action-section">
        <view class="section-header">
          <text class="section-title">仓储操作</text>
        </view>
        <view class="action-list">
          <view class="action-item" @click="handleScan('stock/in')">
            <view class="action-icon in">
              <text class="icon-text">入</text>
            </view>
            <view class="action-info">
              <text class="action-title">扫码入库</text>
              <text class="action-desc">扫描物料条码进行入库操作</text>
            </view>
            <text class="action-arrow">›</text>
          </view>
          <view class="action-item" @click="handleScan('stock/out')">
            <view class="action-icon out">
              <text class="icon-text">出</text>
            </view>
            <view class="action-info">
              <text class="action-title">扫码出库</text>
              <text class="action-desc">扫描物料条码进行出库操作</text>
            </view>
            <text class="action-arrow">›</text>
          </view>
        </view>
      </view>

      <view class="action-section">
        <view class="section-header">
          <text class="section-title">生产操作</text>
        </view>
        <view class="action-list">
          <view class="action-item" @click="handleScan('production/report')">
            <view class="action-icon report">
              <text class="icon-text">工</text>
            </view>
            <view class="action-info">
              <text class="action-title">扫码报工</text>
              <text class="action-desc">扫描工单条码进行生产报工</text>
            </view>
            <text class="action-arrow">›</text>
          </view>
          <view class="action-item" @click="handleScan('quality/check')">
            <view class="action-icon check">
              <text class="icon-text">检</text>
            </view>
            <view class="action-info">
              <text class="action-title">质检扫码</text>
              <text class="action-desc">扫描产品条码进行质量检验</text>
            </view>
            <text class="action-arrow">›</text>
          </view>
        </view>
      </view>

      <view v-if="scanHistory.length > 0" class="history-section">
        <view class="section-header">
          <text class="section-title">扫码记录</text>
          <text class="section-more" @click="clearHistory">清空</text>
        </view>
        <view class="history-list">
          <view class="history-item" v-for="(item, index) in scanHistory" :key="index">
            <view class="history-tag" :class="'tag-' + (index % 4)">{{ item.typeName }}</view>
            <text class="history-code">{{ item.code }}</text>
            <text class="history-time">{{ item.time }}</text>
          </view>
        </view>
      </view>

      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script>
import { scanCode, navigateByScanResult, parseScanResult, getCodeTypeName } from '@/utils/scan'
import { isLoggedIn } from '@/utils/auth'

export default {
  components: {},
  data() {
    return {
      scanHistory: [],
      loading: false,
      statusBarPadding: ''
    }
  },
  onShow() {
    if (!isLoggedIn()) {
      uni.reLaunch({ url: '/pages/login/index' })
    }
  },
  onLoad() {
    const sysInfo = uni.getSystemInfoSync()
    const statusBarHeight = sysInfo.statusBarHeight || 44
    this.statusBarPadding = `${statusBarHeight}px`
    this.bodyPadding = `calc(${statusBarHeight}px + 30rpx)`
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
        
        await this.queryScanInfo(result)
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
        
        await this.queryScanInfo(result)
      } catch {
      }
    },
    async queryScanInfo(code) {
      this.loading = true
      try {
        const { get } = await import('@/utils/request')
        const res = await get('/mini/scan/decode', { code })
        const data = (res && res.data) || res

        if (data && data.type) {
          if (data.type === 'material') {
            this.showMaterialDetail(data)
          } else if (data.type === 'sn_code') {
            this.showSnDetail(data)
          } else if (data.type === 'work_order') {
            this.showWorkOrderDetail(data)
          } else if (data.type === 'station') {
            this.showStationDetail(data)
          } else {
            navigateByScanResult(code)
          }
        } else {
          navigateByScanResult(code)
        }
      } catch (e) {
        console.error('扫码查询失败:', e)
        navigateByScanResult(code)
      } finally {
        this.loading = false
      }
    },
    showStationDetail(data) {
      let content = `工位: ${data.stationCode || '未知'}\n`
      if (data.processName) {
        content += `工序: ${data.processName}\n`
        content += `工序编码: ${data.processCode || '未知'}\n`
      } else {
        content += '未关联工序信息\n'
      }
      
      uni.showModal({
        title: '工位详情',
        content: content,
        showCancel: true,
        cancelText: '关闭',
        confirmText: '查看SOP',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({ url: `/pages/production/sop?stationCode=${encodeURIComponent(data.stationCode)}` })
          }
        }
      })
    },
    showSnDetail(data) {
      const traces = data.processTraces || []
      const materialTraces = data.materialTraces || []
      const qualityTraces = data.qualityTraces || []
      
      let content = `产品: ${data.productName || '未知'}\n`
      content += `SN码: ${data.snCode}\n`
      content += `工单: ${data.workOrderNo || '无'}\n`
      content += `客户: ${data.customerName || '无'}\n`
      content += `状态: ${data.status === 1 ? '在产' : data.status === 2 ? '已完成' : '未知'}\n\n`
      
      content += '--- 工艺流程 ---\n'
      if (traces.length > 0) {
        traces.forEach((t, i) => {
          content += `${i + 1}. ${t.processName}(${t.processCode}) - `
          content += t.status === 2 ? '已完成' : '进行中'
          if (t.workerName) content += ` (${t.workerName})`
          content += '\n'
        })
      } else {
        content += '暂无工艺记录\n'
      }
      
      content += '\n--- 物料追溯 ---\n'
      if (materialTraces.length > 0) {
        materialTraces.forEach((m, i) => {
          content += `${i + 1}. ${m.materialName}(${m.materialCode})\n`
          if (m.batchNo) content += `   批次: ${m.batchNo}\n`
        })
      } else {
        content += '暂无物料记录\n'
      }
      
      content += '\n--- 质检记录 ---\n'
      if (qualityTraces.length > 0) {
        qualityTraces.slice(0, 3).forEach((q, i) => {
          content += `${i + 1}. ${q.checkItem}: ${q.actualValue}`
          content += q.result === 1 ? '(合格)' : '(不合格)'
          content += ` ${q.inspectorName || ''}\n`
        })
      } else {
        content += '暂无质检记录\n'
      }
      
      uni.showModal({
        title: 'SN码详情',
        content: content,
        showCancel: true,
        cancelText: '关闭',
        confirmText: '查看详情',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({ url: `/pages/production/trace?snCode=${encodeURIComponent(data.snCode)}` })
          }
        }
      })
    },
    showWorkOrderDetail(data) {
      let content = `工单号: ${data.workNo}\n`
      content += `产品: ${data.productName || '未知'}\n`
      content += `工序: ${data.processName || '未知'}\n`
      content += `数量: ${data.quantity}件\n`
      content += `状态: ${data.status === 0 ? '待生产' : data.status === 1 ? '生产中' : data.status === 2 ? '已完成' : '未知'}\n`
      content += `创建时间: ${data.createTime || '未知'}\n`
      
      uni.showModal({
        title: '工单详情',
        content: content,
        showCancel: true,
        cancelText: '关闭',
        confirmText: '查看详情',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({ url: `/pages/production/detail?workOrderId=${encodeURIComponent(data.workNo)}` })
          }
        }
      })
    },
    showMaterialDetail(data) {
      let content = `物料编码: ${data.materialCode}\n`
      content += `物料名称: ${data.materialName}\n`
      content += `规格型号: ${data.materialType || '未知'}\n`
      content += `单位: ${data.unit || '个'}\n`
      content += `单价: ${data.unitPrice || '0'}元\n`
      content += `安全库存: ${data.safetyStock || 0}\n`
      content += `总库存: ${data.totalStock || 0} ${data.unit || '个'}\n\n`
      
      content += '--- 库存分布 ---\n'
      const stocks = data.stocks || []
      if (stocks.length > 0) {
        stocks.forEach((s, i) => {
          content += `${i + 1}. ${s.warehouseName || '仓库' + s.warehouseId}: `
          content += `${s.quantity || 0} ${data.unit || '个'}`
          if (s.location) content += ` (${s.location})`
          content += '\n'
        })
      } else {
        content += '暂无库存\n'
      }
      
      uni.showModal({
        title: '物料详情',
        content: content,
        showCancel: true,
        cancelText: '关闭',
        confirmText: '查看详情',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({ url: `/pages/stock/material?code=${encodeURIComponent(data.materialCode)}` })
          }
        }
      })
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
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

.scan-page {
  min-height: 100vh;
  background: #F0F2F5;
}

.page-header {
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, #3B7CFF 0%, #5B9AFF 40%, #7CB7FF 100%);
}

.header-wave {
  position: absolute;
  bottom: -2rpx;
  left: 0;
  right: 0;
  height: 60rpx;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 120'%3E%3Cpath fill='%23F0F2F5' fill-opacity='1' d='M0,64L60,69.3C120,75,240,85,360,80C480,75,600,53,720,48C840,43,960,53,1080,58.7C1200,64,1320,64,1380,64L1440,64L1440,120L1380,120C1320,120,1200,120,1080,120C960,120,840,120,720,120C600,120,480,120,360,120C240,120,120,120,60,120L0,120Z'%3E%3C/path%3E%3C/svg%3E") no-repeat;
  background-size: 100% 100%;
}

.header-content {
  position: relative;
  z-index: 1;
  padding: 20rpx 32rpx 40rpx;
  text-align: center;
}

.title-area {
  margin-bottom: 32rpx;
}

.page-title {
  font-size: 44rpx;
  font-weight: 700;
  color: #ffffff;
  display: block;
  margin-bottom: 10rpx;
}

.page-desc {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  display: block;
}

.scan-btn {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.scan-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  animation: pulse-ring 2s ease-out infinite;
}

.scan-circle {
  width: 130rpx;
  height: 130rpx;
  background: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 40rpx rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.95);
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
  }
}

.scan-icon {
  font-size: 52rpx;
  font-weight: 600;
  color: #3B7CFF;
}

.scan-text {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 18rpx;
}

.page-body {
  height: 100vh;
  padding: 0;
}

.core-section {
  margin-bottom: 24rpx;
  padding: 0 32rpx;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.section-more {
  font-size: 24rpx;
  color: #3B7CFF;
  margin-left: auto;
}

.core-grid {
  display: flex;
  gap: 20rpx;
}

.core-card {
  flex: 1;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 28rpx 20rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
  text-align: center;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.95);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
  }
}

.core-icon {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16rpx;

  &.blue { background: linear-gradient(135deg, #6B8FFF 0%, #3B7CFF 100%); }
  &.green { background: linear-gradient(135deg, #52C41A 0%, #389E0D 100%); }
  &.orange { background: linear-gradient(135deg, #FAAD14 0%, #D48806 100%); }
}

.icon-text {
  font-size: 38rpx;
  font-weight: 600;
  color: #ffffff;
}

.core-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.core-desc {
  font-size: 22rpx;
  color: #999;
  display: block;
  line-height: 1.4;
}

.core-badge {
  font-size: 18rpx;
  color: #ffffff;
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
  display: inline-block;
  margin-top: 14rpx;

  .blue & { background: rgba(59, 124, 255, 0.6); }
  .green & { background: rgba(82, 196, 26, 0.6); }
  .orange & { background: rgba(250, 173, 20, 0.6); }
}

.action-section {
  margin-bottom: 24rpx;
  padding: 0 32rpx;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.action-item {
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
  }
}

.action-icon {
  width: 88rpx;
  height: 88rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;

  &.in { background: linear-gradient(135deg, rgba(82, 196, 26, 0.15) 0%, rgba(82, 196, 26, 0.05) 100%); }
  &.out { background: linear-gradient(135deg, rgba(59, 124, 255, 0.15) 0%, rgba(59, 124, 255, 0.05) 100%); }
  &.report { background: linear-gradient(135deg, rgba(250, 173, 20, 0.15) 0%, rgba(250, 173, 20, 0.05) 100%); }
  &.check { background: linear-gradient(135deg, rgba(255, 77, 79, 0.15) 0%, rgba(255, 77, 79, 0.05) 100%); }
}

.action-icon .icon-text {
  font-size: 40rpx;
  font-weight: 600;

  .in & { color: #52C41A; }
  .out & { color: #3B7CFF; }
  .report & { color: #FAAD14; }
  .check & { color: #FF4D4F; }
}

.action-info {
  flex: 1;
}

.action-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 6rpx;
}

.action-desc {
  font-size: 24rpx;
  color: #999;
  display: block;
}

.action-arrow {
  font-size: 36rpx;
  color: #DDD;
}

.history-section {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #FAFBFC;
  border-radius: 16rpx;
}

.history-tag {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 500;
  padding: 6rpx 18rpx;
  border-radius: 10rpx;
  margin-right: 16rpx;
  flex-shrink: 0;

  &.tag-0 { background: #3B7CFF; }
  &.tag-1 { background: #52C41A; }
  &.tag-2 { background: #FAAD14; }
  &.tag-3 { background: #FF4D4F; }
}

.history-code {
  font-size: 26rpx;
  color: #555;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-time {
  font-size: 22rpx;
  color: #BBB;
  margin-left: 16rpx;
  flex-shrink: 0;
}

.bottom-space {
  height: 160rpx;
}
</style>
