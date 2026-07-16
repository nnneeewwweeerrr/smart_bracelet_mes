﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <view class="trace-page">
    <DynamicBackground theme="theme-primary" />
    <view class="page-content">
    <view class="search-section">
      <view class="search-bar">
        <u-search v-model="searchKey" :placeholder="mode === 'reverse' ? '输入SN码/工单号' : '输入批次号'" @search="doSearch" @custom="doSearch" @clear="doSearch" shape="round"></u-search>
        <view class="scan-btn" @click="handleScan">
          <text class="scan-icon">📷</text>
          <text class="scan-text">扫码</text>
        </view>
      </view>

      <view class="mode-switch">
        <u-tabs :list="modeList" :current="modeIndex" @click="onModeChange" :activeStyle="{ color: '#3B7CFF', fontWeight: 'bold' }" :inactiveStyle="{ color: '#8C8C8C' }" lineColor="#3B7CFF" :lineWidth="30"></u-tabs>
      </view>
    </view>

    <view class="trace-result" v-if="snData" v-show="showResult">
      <view v-if="mode === 'reverse'" :class="{ 'slide-in': showResult }">
        <view class="sn-header">
          <view class="sn-card">
            <view class="sn-icon">
              <text class="icon-text">SN</text>
            </view>
            <view class="sn-info">
              <text class="sn-label">产品序列号</text>
              <text class="sn-value">{{ snData.snCode }}</text>
            </view>
          </view>
          <view class="sn-status" :class="snData.statusClass">
            <text class="status-text">{{ snData.statusText }}</text>
          </view>
        </view>

        <view class="product-info" :class="{ 'slide-in-delay-1': showResult }">
          <view class="info-grid">
            <view class="info-item">
              <text class="info-icon">📱</text>
              <text class="info-label">产品型号</text>
              <text class="info-value">{{ snData.productModel }}</text>
            </view>
            <view class="info-item">
              <text class="info-icon">📋</text>
              <text class="info-label">工单编号</text>
              <text class="info-value">{{ snData.workOrderNo }}</text>
            </view>
            <view class="info-item">
              <text class="info-icon">📦</text>
              <text class="info-label">生产批次</text>
              <text class="info-value">{{ snData.batchNo }}</text>
            </view>
            <view class="info-item">
              <text class="info-icon">📅</text>
              <text class="info-label">生产日期</text>
              <text class="info-value">{{ snData.productionDate }}</text>
            </view>
          </view>
        </view>
      </view>

      <view v-else :class="{ 'slide-in': showResult }">
        <view class="batch-header">
          <view class="batch-card">
            <view class="batch-icon">
              <text class="icon-text">批次</text>
            </view>
            <view class="batch-info">
              <text class="batch-label">批次编号</text>
              <text class="batch-value">{{ batchData.batchInfo?.batchNo || snData.batchNo }}</text>
            </view>
          </view>
          <view class="batch-count">
            <text class="count-text">{{ batchData.batchInfo?.totalSnCount || 0 }}</text>
            <text class="count-label">件产品</text>
          </view>
        </view>

        <view class="batch-summary" :class="{ 'slide-in-delay-1': showResult }">
          <view class="summary-grid">
            <view class="summary-item">
              <text class="summary-icon">📦</text>
              <text class="summary-label">关联物料</text>
              <text class="summary-value">{{ batchData.batchInfo?.materialName || '-' }}</text>
            </view>
            <view class="summary-item">
              <text class="summary-icon">📋</text>
              <text class="summary-label">关联工单</text>
              <text class="summary-value">{{ batchData.workOrderGroups?.length || 0 }}个</text>
            </view>
            <view class="summary-item">
              <text class="summary-icon">🏭</text>
              <text class="summary-label">生产工序</text>
              <text class="summary-value">{{ batchData.processList?.length || 0 }}道</text>
            </view>
            <view class="summary-item">
              <text class="summary-icon">✅</text>
              <text class="summary-label">质检记录</text>
              <text class="summary-value">{{ batchData.qualityList?.length || 0 }}条</text>
            </view>
          </view>
        </view>

        <view class="section-card" :class="{ 'slide-in-delay-2': showResult }">
          <view class="section-header">
            <text class="section-title">📋 工单分组统计</text>
          </view>
          <view class="wo-group-list">
            <view class="wo-group-item" v-for="group in batchData.workOrderGroups" :key="group.workOrderNo">
              <view class="wo-group-header">
                <text class="wo-group-no">{{ group.workOrderNo }}</text>
                <text class="wo-group-count">{{ group.snCount }}件</text>
              </view>
              <view class="wo-group-sns">
                <text class="wo-sn-tag" v-for="sn in group.snCodes" :key="sn" @click="loadSnDetail(sn)">{{ sn }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="section-card" :class="{ 'slide-in-delay-3': showResult }">
          <view class="section-header">
            <text class="section-title">📱 产品SN列表</text>
            <text class="section-count">{{ batchData.snList?.length || 0 }}件</text>
          </view>
          <view class="sn-list-grid">
            <view class="sn-list-item" v-for="sn in batchData.snList" :key="sn.snCode" @click="loadSnDetail(sn.snCode)">
              <view class="sn-list-icon" :class="getStatusClass(sn.status)">
                <text class="sn-list-icon-text">{{ getStatusIcon(sn.status) }}</text>
              </view>
              <view class="sn-list-info">
                <text class="sn-list-code">{{ sn.snCode }}</text>
                <text class="sn-list-meta">{{ sn.productName || '智能手环' }}</text>
              </view>
              <text class="sn-list-arrow">›</text>
            </view>
          </view>
        </view>
      </view>

      <view class="section-card" :class="{ 'slide-in-delay-2': showResult }">
        <view class="section-header">
          <text class="section-title">📦 物料溯源</text>
          <text class="section-count">{{ snData.materialList.length }}种物料</text>
        </view>
        <view class="material-list">
          <view class="material-item" v-for="(item, idx) in snData.materialList" :key="item.id" :class="{ 'fade-in': showResult }" :style="{ animationDelay: `${idx * 0.1}s` }">
            <view class="material-icon" :style="{ background: item.color }">
              <text class="material-icon-text">{{ item.name.charAt(0) }}</text>
            </view>
            <view class="material-info">
              <text class="material-name">{{ item.name }}</text>
              <text class="material-detail">{{ item.code }} | 批次: {{ item.batch }}</text>
            </view>
            <text class="material-supplier">{{ item.supplier }}</text>
          </view>
        </view>
      </view>

      <view class="section-card timeline-card" :class="{ 'slide-in-delay-3': showResult }">
        <view class="section-header">
          <text class="section-title">🏭 生产履历</text>
          <text class="section-sub">时间轴展示</text>
        </view>
        <view class="timeline">
          <view class="timeline-item" v-for="(item, idx) in snData.productionHistory" :key="idx" :class="{ 'timeline-item-active': idx === currentTimelineIndex }">
            <view class="timeline-left">
              <view class="timeline-dot-wrapper">
                <view class="timeline-dot" :class="getDotClass(item.status)"></view>
                <view class="timeline-dot-glow" :class="getDotClass(item.status)" v-if="idx === 0"></view>
              </view>
              <view class="timeline-line" v-if="idx < snData.productionHistory.length - 1" :class="{ 'line-active': idx < currentTimelineIndex }"></view>
            </view>
            <view class="timeline-content" @click="currentTimelineIndex = idx">
              <view class="content-header">
                <text class="content-process">{{ item.processName }}</text>
                <u-tag :text="item.statusText" :type="item.statusTag" size="mini"></u-tag>
              </view>
              <view class="content-meta">
                <text class="meta-item">🛠️ {{ item.lineName }}</text>
                <text class="meta-item">👤 {{ item.workerName }}</text>
              </view>
              <text class="content-time">{{ item.executeTime }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="section-card" :class="{ 'slide-in-delay-4': showResult }">
        <view class="section-header">
          <text class="section-title">✅ 质检记录</text>
        </view>
        <view class="quality-list">
          <view class="quality-item" v-for="item in snData.qualityRecords" :key="item.id">
            <view class="quality-header">
              <text class="quality-type">{{ item.inspectionType }}</text>
              <u-tag :text="item.resultText" :type="item.resultTag" size="mini"></u-tag>
            </view>
            <view class="quality-content">
              <text class="quality-detail">{{ item.detail }}</text>
            </view>
            <view class="quality-footer">
              <text class="quality-inspector">{{ item.inspector }}</text>
              <text class="quality-time">{{ item.inspectionTime }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="section-card" v-if="snData.testData" :class="{ 'slide-in-delay-5': showResult }">
        <view class="section-header">
          <text class="section-title">🔬 功能测试数据</text>
        </view>
        <view class="test-grid">
          <view class="test-item" :class="snData.testData.heartRate.pass ? 'test-pass' : 'test-fail'" @click="showTestDetail('心率传感器', snData.testData.heartRate)">
            <view class="test-icon-wrap">
              <text class="test-icon">❤️</text>
            </view>
            <text class="test-name">心率传感器</text>
            <text class="test-value">{{ snData.testData.heartRate.value }}</text>
            <view class="test-badge" :class="snData.testData.heartRate.pass ? 'badge-pass' : 'badge-fail'">
              <text class="test-result">{{ snData.testData.heartRate.pass ? '通过' : '失败' }}</text>
            </view>
          </view>
          <view class="test-item" :class="snData.testData.bluetooth.pass ? 'test-pass' : 'test-fail'" @click="showTestDetail('蓝牙连接', snData.testData.bluetooth)">
            <view class="test-icon-wrap">
              <text class="test-icon">🔵</text>
            </view>
            <text class="test-name">蓝牙连接</text>
            <text class="test-value">{{ snData.testData.bluetooth.value }}</text>
            <view class="test-badge" :class="snData.testData.bluetooth.pass ? 'badge-pass' : 'badge-fail'">
              <text class="test-result">{{ snData.testData.bluetooth.pass ? '通过' : '失败' }}</text>
            </view>
          </view>
          <view class="test-item" :class="snData.testData.battery.pass ? 'test-pass' : 'test-fail'" @click="showTestDetail('电池容量', snData.testData.battery)">
            <view class="test-icon-wrap">
              <text class="test-icon">🔋</text>
            </view>
            <text class="test-name">电池容量</text>
            <text class="test-value">{{ snData.testData.battery.value }}</text>
            <view class="test-badge" :class="snData.testData.battery.pass ? 'badge-pass' : 'badge-fail'">
              <text class="test-result">{{ snData.testData.battery.pass ? '通过' : '失败' }}</text>
            </view>
          </view>
          <view class="test-item" :class="snData.testData.waterproof.pass ? 'test-pass' : 'test-fail'" @click="showTestDetail('防水测试', snData.testData.waterproof)">
            <view class="test-icon-wrap">
              <text class="test-icon">💧</text>
            </view>
            <text class="test-name">防水测试</text>
            <text class="test-value">{{ snData.testData.waterproof.value }}</text>
            <view class="test-badge" :class="snData.testData.waterproof.pass ? 'badge-pass' : 'badge-fail'">
              <text class="test-result">{{ snData.testData.waterproof.pass ? '通过' : '失败' }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="section-card" v-if="snData.deliveryInfo" :class="{ 'slide-in-delay-6': showResult }">
        <view class="section-header">
          <text class="section-title">📤 交付信息</text>
        </view>
        <view class="delivery-info">
          <view class="delivery-grid">
            <view class="delivery-item">
              <text class="delivery-icon">👤</text>
              <text class="delivery-label">客户名称</text>
              <text class="delivery-value">{{ snData.deliveryInfo.customerName }}</text>
            </view>
            <view class="delivery-item">
              <text class="delivery-icon">📋</text>
              <text class="delivery-label">订单编号</text>
              <text class="delivery-value">{{ snData.deliveryInfo.orderNo }}</text>
            </view>
            <view class="delivery-item">
              <text class="delivery-icon">📅</text>
              <text class="delivery-label">发货日期</text>
              <text class="delivery-value">{{ snData.deliveryInfo.deliveryDate }}</text>
            </view>
            <view class="delivery-item">
              <text class="delivery-icon">📦</text>
              <text class="delivery-label">物流单号</text>
              <text class="delivery-value">{{ snData.deliveryInfo.trackingNo }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="action-bar">
        <u-button type="primary" size="large" @click="handleScan">
          <text class="btn-icon">🔄</text>
          扫描下一个SN码
        </u-button>
      </view>
    </view>

    <view v-if="searched && !snData" style="padding-top: 80rpx;">
      <u-empty mode="search" text="未找到追溯数据"></u-empty>
    </view>

    <view v-if="!searched" style="padding-top: 120rpx;">
      <view class="empty-state">
        <view class="empty-icon">🔍</view>
        <text class="empty-title">SN码全生命周期追溯</text>
        <text class="empty-desc">扫描智能手环成品SN码，一键调出完整产品档案</text>
        <u-button type="primary" size="large" @click="handleScan" customStyle="margin-top: 40rpx;">
          <text class="btn-icon">📷</text>
          立即扫码追溯
        </u-button>
        <view class="demo-section">
          <text class="demo-label">📋 查看示例数据</text>
          <view class="demo-list">
            <view class="demo-item" @click="loadDemo('SN20240115001')">SN20240115001</view>
            <view class="demo-item" @click="loadDemo('SN20240115002')">SN20240115002</view>
            <view class="demo-item" @click="loadDemo('BATCH20240101')">批次正向追溯</view>
          </view>
        </view>
        <text class="empty-hint">支持SN码格式：SN+数字（如 SN20240115001）</text>
      </view>
    </view>

    <u-popup v-if="showTestDetailModal" :show="showTestDetailModal" mode="center" round="24" @close="showTestDetailModal = false">
      <view class="test-detail-modal">
        <view class="modal-header">
          <text class="modal-title">{{ testDetailName }}</text>
          <view class="modal-close" @click="showTestDetailModal = false">✕</view>
        </view>
        <view class="modal-body">
          <view class="test-detail-status" :class="testDetailData.pass ? 'status-pass' : 'status-fail'">
            <text class="status-icon">{{ testDetailData.pass ? '✅' : '❌' }}</text>
            <text class="status-text">{{ testDetailData.pass ? '测试通过' : '测试失败' }}</text>
          </view>
          <view class="test-detail-info">
            <text class="detail-label">测试结果</text>
            <text class="detail-value">{{ testDetailData.value }}</text>
          </view>
          <view class="test-detail-desc">
            <text class="detail-label">测试说明</text>
            <text class="detail-desc-text">{{ getTestDescription(testDetailName) }}</text>
          </view>
        </view>
      </view>
    </u-popup>
    </view>
  </view>
</template>

<script>
import { getSnReverseTrace, getSnForwardTrace } from '@/api/production'
import { scanCode } from '@/utils/scan'
import DynamicBackground from '@/components/DynamicBackground'

export default {
  components: {
    DynamicBackground
  },
  data() {
    return {
      mode: 'reverse',
      modeIndex: 0,
      modeList: [
        { name: 'SN码追溯', value: 'reverse' },
        { name: '批次正向追溯', value: 'forward' }
      ],
      searchKey: '',
      searched: false,
      snData: null,
      batchData: null,
      showResult: false,
      currentTimelineIndex: 0,
      showTestDetailModal: false,
      testDetailName: '',
      testDetailData: {}
    }
  },
  onLoad(options) {
    if (options.snCode) {
      this.searchKey = options.snCode
      this.doSearch()
    } else if (options.workNo) {
      this.searchKey = options.workNo
      this.doSearch()
    } else if (options.batchNo) {
      this.searchKey = options.batchNo
      this.mode = 'forward'
      this.modeIndex = 1
      this.doSearch()
    }
  },
  methods: {
    async handleScan() {
      try {
        const code = await scanCode()
        this.searchKey = code
        if (code.startsWith('BATCH')) {
          this.mode = 'forward'
          this.modeIndex = 1
        } else {
          this.mode = 'reverse'
          this.modeIndex = 0
        }
        this.doSearch()
      } catch {
        // cancelled
      }
    },
    onModeChange({ index }) {
      this.modeIndex = index
      this.mode = this.modeList[index].value
      this.searchKey = ''
      this.snData = null
      this.batchData = null
      this.searched = false
      this.showResult = false
    },
    async doSearch() {
      if (!this.searchKey.trim()) {
        this.snData = null
        this.searched = false
        this.showResult = false
        return
      }

      this.showResult = false
      uni.showLoading({ title: '正在追溯...' })

      try {
        if (this.mode === 'reverse') {
          const res = await getSnReverseTrace(this.searchKey.trim())
          if (res) {
            const snInfo = res.snInfo || {}
            this.snData = {
              snCode: snInfo.snCode || '',
              status: snInfo.status || 0,
              statusClass: this.getStatusClass(snInfo.status),
              statusText: this.getStatusText(snInfo.status),
              productModel: snInfo.productName || '',
              workOrderNo: snInfo.workOrderNo || '',
              batchNo: '',
              productionDate: snInfo.productionDate || '',
              materialList: (res.materialList || []).map(item => ({
                id: item.id || item.snId,
                name: item.materialName || item.name || '',
                code: item.materialCode || item.code || '',
                batch: item.batchNo || item.batch || '',
                supplier: item.supplierName || item.supplier || '',
                color: '#3B7CFF'
              })),
              productionHistory: (res.processList || []).map(item => ({
                processName: item.processName || '',
                status: item.status === 1 ? 'pass' : (item.status === 0 ? 'pending' : 'running'),
                statusText: item.status === 1 ? '完成' : (item.status === 0 ? '待执行' : '进行中'),
                statusTag: item.status === 1 ? 'success' : (item.status === 0 ? 'warning' : 'primary'),
                lineName: item.equipmentName || '',
                workerName: item.workerName || '',
                executeTime: item.endTime || item.startTime || ''
              })),
              qualityRecords: (res.qualityList || []).map(item => ({
                id: item.id || '',
                inspectionType: item.taskType || item.checkItem || '',
                result: item.result === 1 ? 'pass' : 'fail',
                resultText: item.result === 1 ? '合格' : '不合格',
                resultTag: item.result === 1 ? 'success' : 'error',
                detail: item.defectDesc || item.checkItem || '',
                inspector: item.inspectorName || '',
                inspectionTime: item.checkTime || ''
              }))
            }
          }
        } else {
          const res = await getSnForwardTrace(this.searchKey.trim())
          if (res && res.exists) {
            this.batchData = res
            const snList = res.snList || []
            const firstSn = snList.length > 0 ? snList[0] : {}
            this.snData = {
              snCode: this.searchKey.trim(),
              batchNo: this.searchKey.trim(),
              status: firstSn.status || 1,
              statusClass: this.getStatusClass(firstSn.status),
              statusText: this.getStatusText(firstSn.status),
              productModel: firstSn.productName || '智能运动手环',
              workOrderNo: firstSn.workOrderNo || '-',
              productionDate: firstSn.productionDate || '',
              materialList: (res.materialList || []).map(item => ({
                id: item.id || item.snId,
                name: item.materialName || item.name || '',
                code: item.materialCode || item.code || '',
                batch: item.batchNo || item.batch || '',
                supplier: item.supplierName || item.supplier || '',
                color: '#3B7CFF'
              })),
              productionHistory: (res.processList || []).map(item => ({
                processName: item.processName || '',
                status: item.status === 1 ? 'pass' : (item.status === 0 ? 'pending' : 'running'),
                statusText: item.status === 1 ? '完成' : (item.status === 0 ? '待执行' : '进行中'),
                statusTag: item.status === 1 ? 'success' : (item.status === 0 ? 'warning' : 'primary'),
                lineName: item.equipmentName || '',
                workerName: item.workerName || '',
                executeTime: item.endTime || item.startTime || ''
              })),
              qualityRecords: (res.qualityList || []).map(item => ({
                id: item.id || '',
                inspectionType: item.taskType || item.checkItem || '',
                result: item.result === 1 ? 'pass' : 'fail',
                resultText: item.result === 1 ? '合格' : '不合格',
                resultTag: item.result === 1 ? 'success' : 'error',
                detail: item.defectDesc || item.checkItem || '',
                inspector: item.inspectorName || '',
                inspectionTime: item.checkTime || ''
              }))
            }
          }
        }
        this.searched = true
        this.showResult = true
      } catch {
        uni.showToast({ title: '追溯失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
    buildSnData(res) {
      return {
        ...res,
        statusText: this.getStatusText(res.status),
        statusClass: this.getStatusClass(res.status),
        productionHistory: (res.productionHistory || []).map(item => ({
          ...item,
          statusText: this.getResultText(item.status),
          statusTag: this.getResultTag(item.status)
        })),
        qualityRecords: (res.qualityRecords || []).map(item => ({
          ...item,
          resultText: this.getResultText(item.result),
          resultTag: this.getResultTag(item.result)
        }))
      }
    },
    getStatusText(status) {
      if (typeof status === 'number') {
        const m = { 0: '在产', 1: '成品', 2: '已出库', 3: '已售后' }
        return m[status] || '未知'
      }
      const m = { produced: '已生产', tested: '已测试', delivered: '已交付', returned: '已退货' }
      return m[status] || status || '未知'
    },
    getStatusClass(status) {
      if (typeof status === 'number') {
        const m = { 0: 'status-default', 1: 'status-produced', 2: 'status-tested', 3: 'status-delivered' }
        return m[status] || 'status-default'
      }
      const m = { produced: 'status-produced', tested: 'status-tested', delivered: 'status-delivered', returned: 'status-returned' }
      return m[status] || 'status-default'
    },
    getResultText(r) {
      const m = { pass: '合格', fail: '不合格', pending: '待检', running: '进行中' }
      return m[r] || r || '未知'
    },
    getResultTag(r) {
      const m = { pass: 'success', fail: 'error', pending: 'warning', running: 'primary' }
      return m[r] || 'info'
    },
    getDotClass(r) {
      const m = { pass: 'dot-success', fail: 'dot-error', pending: 'dot-warn', running: 'dot-primary' }
      return m[r] || 'dot-default'
    },
    showTestDetail(name, data) {
      this.testDetailName = name
      this.testDetailData = data
      this.showTestDetailModal = true
    },
    getTestDescription(name) {
      const desc = {
        '心率传感器': '测试心率传感器的检测范围（60-180bpm），验证传感器灵敏度和准确性，确保能够实时准确监测用户心率数据。',
        '蓝牙连接': '测试手环蓝牙连接功能，验证BLE 5.0协议通信稳定性，确保能快速稳定连接手机APP，支持数据同步。',
        '电池容量': '测试锂电池实际容量，验证是否达到标称380mAh，检测充电效率和续航能力。',
        '防水测试': '进行IP68防水等级验证，测试手环在2米水深下的防水性能，确保日常防水和游泳使用安全。'
      }
      return desc[name] || '该功能测试已完成验证。'
    },
    getStatusIcon(status) {
      if (typeof status === 'number') {
        const m = { 0: '🏭', 1: '✅', 2: '📤', 3: '🔄' }
        return m[status] || '❓'
      }
      const m = { produced: '✅', tested: '✅', delivered: '📤', returned: '🔄' }
      return m[status] || '❓'
    },
    loadSnDetail(snCode) {
      this.mode = 'reverse'
      this.modeIndex = 0
      this.searchKey = snCode
      this.doSearch()
    },
    loadDemo(key) {
      this.searchKey = key
      if (key.startsWith('BATCH')) {
        this.mode = 'forward'
        this.modeIndex = 1
      } else {
        this.mode = 'reverse'
        this.modeIndex = 0
      }
      this.doSearch()
    }
  }
}
</script>

<style lang="scss" scoped>
@keyframes slideIn {
  from { opacity: 0; transform: translateY(30rpx); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes pulseGlow {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.5); }
}

.trace-page {
  min-height: 100vh;
  background: #F5F7FA;
  padding-bottom: $tabbar-height;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
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
  display: flex;
  align-items: center;
}

.scan-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8rpx 16rpx;
  margin-left: 16rpx;
  background: rgba(59, 124, 255, 0.1);
  border-radius: 16rpx;
}

.scan-icon {
  font-size: 28rpx;
}

.scan-text {
  font-size: 18rpx;
  color: #3B7CFF;
  margin-top: 2rpx;
}

.mode-switch {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 8rpx 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.trace-result {
  padding: 0 24rpx;
}

.sn-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.slide-in {
  animation: slideIn 0.5s ease-out;
}

.slide-in-delay-1 {
  animation: slideIn 0.5s ease-out 0.1s both;
}

.slide-in-delay-2 {
  animation: slideIn 0.5s ease-out 0.2s both;
}

.slide-in-delay-3 {
  animation: slideIn 0.5s ease-out 0.3s both;
}

.slide-in-delay-4 {
  animation: slideIn 0.5s ease-out 0.4s both;
}

.slide-in-delay-5 {
  animation: slideIn 0.5s ease-out 0.5s both;
}

.slide-in-delay-6 {
  animation: slideIn 0.5s ease-out 0.6s both;
}

.sn-card {
  flex: 1;
  background: linear-gradient(135deg, #3B7CFF 0%, #5B9AFF 100%);
  border-radius: 24rpx;
  padding: 24rpx 32rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 8rpx 32rpx rgba(59, 124, 255, 0.3);
}

.sn-icon {
  width: 72rpx;
  height: 72rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.icon-text {
  font-size: 32rpx;
  font-weight: 700;
  color: #ffffff;
}

.sn-info {
  flex: 1;
}

.sn-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  display: block;
  margin-bottom: 4rpx;
}

.sn-value {
  font-size: 28rpx;
  font-weight: 600;
  color: #ffffff;
  display: block;
}

.sn-status {
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
  margin-left: 16rpx;

  &.status-produced { background: rgba(250, 173, 20, 0.15); }
  &.status-tested { background: rgba(59, 124, 255, 0.15); }
  &.status-delivered { background: rgba(103, 194, 58, 0.15); }
  &.status-returned { background: rgba(245, 108, 108, 0.15); }
  &.status-default { background: rgba(191, 191, 191, 0.15); }
}

.status-text {
  font-size: 24rpx;
  font-weight: 600;

  .status-produced & { color: #FAAD14; }
  .status-tested & { color: #3B7CFF; }
  .status-delivered & { color: #52C41A; }
  .status-returned & { color: #FF4D4F; }
  .status-default & { color: #8C8C8C; }
}

.product-info {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  padding: 28rpx 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 0 0 1rpx rgba(255, 255, 255, 0.9);
  margin-bottom: 24rpx;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
}

.info-item {
  text-align: center;
  padding: 16rpx;
  background: rgba(59, 124, 255, 0.05);
  border-radius: 16rpx;
}

.info-icon {
  font-size: 32rpx;
  display: block;
  margin-bottom: 8rpx;
}

.info-label {
  font-size: 22rpx;
  color: #8C8C8C;
  display: block;
  margin-bottom: 6rpx;
}

.info-value {
  font-size: 26rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
}

.section-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  padding: 28rpx 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 0 0 1rpx rgba(255, 255, 255, 0.9);
  margin-bottom: 24rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.section-count {
  font-size: 24rpx;
  color: #3B7CFF;
  font-weight: 500;
}

.section-sub {
  font-size: 24rpx;
  color: #8C8C8C;
}

.material-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.material-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #F8FAFF;
  border-radius: 20rpx;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  }
}

.fade-in {
  animation: fadeIn 0.4s ease-out both;
}

.material-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.material-icon-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #3B7CFF;
}

.material-info {
  flex: 1;
}

.material-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
  margin-bottom: 4rpx;
}

.material-detail {
  font-size: 24rpx;
  color: #8C8C8C;
  display: block;
}

.material-supplier {
  font-size: 24rpx;
  color: #3B7CFF;
  flex-shrink: 0;
}

.timeline-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #F0F7FF 100%);
}

.timeline {
  padding-left: 10rpx;
}

.timeline-item {
  display: flex;
  min-height: 150rpx;
  transition: all 0.3s ease;
}

.timeline-left {
  width: 50rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.timeline-dot-wrapper {
  position: relative;
  width: 32rpx;
  height: 32rpx;
}

.timeline-dot {
  width: 28rpx;
  height: 28rpx;
  border-radius: 50%;
  margin-top: 6rpx;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
  border: 4rpx solid #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);

  &.dot-success { background: #52C41A; }
  &.dot-error { background: #FF4D4F; }
  &.dot-warn { background: #FAAD14; }
  &.dot-primary { background: #3B7CFF; }
  &.dot-default { background: #BFBFBF; }
}

.timeline-dot-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  animation: pulseGlow 2s ease-in-out infinite;

  &.dot-success { background: rgba(103, 194, 58, 0.4); }
  &.dot-error { background: rgba(245, 108, 108, 0.4); }
  &.dot-warn { background: rgba(250, 173, 20, 0.4); }
  &.dot-primary { background: rgba(59, 124, 255, 0.4); }
}

.timeline-line {
  width: 4rpx;
  flex: 1;
  background: #E0E0E0;
  margin: 6rpx 0;
  transition: background 0.3s ease;

  &.line-active {
    background: linear-gradient(180deg, #52C41A 0%, #73D13D 100%);
  }
}

.timeline-content {
  flex: 1;
  background: #ffffff;
  border-radius: 20rpx;
  padding: 20rpx 24rpx;
  margin-left: 20rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:active {
    transform: scale(0.99);
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
  }
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.content-process {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.content-meta {
  display: flex;
  gap: 20rpx;
  margin-bottom: 10rpx;
}

.meta-item {
  font-size: 24rpx;
  color: #8C8C8C;
}

.content-time {
  font-size: 22rpx;
  color: #BFBFBF;
  display: block;
}

.quality-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.quality-item {
  background: #F8FAFF;
  border-radius: 16rpx;
  padding: 24rpx;
}

.quality-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.quality-type {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.quality-content {
  margin-bottom: 12rpx;
}

.quality-detail {
  font-size: 24rpx;
  color: #4A4A4A;
  line-height: 1.6;
}

.quality-footer {
  display: flex;
  justify-content: space-between;
}

.quality-inspector {
  font-size: 22rpx;
  color: #8C8C8C;
}

.quality-time {
  font-size: 22rpx;
  color: #BFBFBF;
}

.test-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.test-item {
  width: calc(50% - 8rpx);
  background: #F8FAFF;
  border-radius: 20rpx;
  padding: 24rpx 20rpx;
  text-align: center;
  border: 2rpx solid transparent;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:active {
    transform: scale(0.97);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  }

  &.test-pass {
    border-color: rgba(103, 194, 58, 0.3);
    background: rgba(103, 194, 58, 0.05);
  }

  &.test-fail {
    border-color: rgba(245, 108, 108, 0.3);
    background: rgba(245, 108, 108, 0.05);
  }
}

.test-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  margin: 0 auto 12rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.test-icon {
  font-size: 36rpx;
}

.test-name {
  font-size: 26rpx;
  color: #1F1F1F;
  font-weight: 500;
  display: block;
  margin-bottom: 8rpx;
}

.test-value {
  font-size: 32rpx;
  font-weight: 700;
  color: #1F1F1F;
  display: block;
  margin-bottom: 12rpx;
}

.test-badge {
  display: inline-block;
  padding: 6rpx 20rpx;
  border-radius: 20rpx;

  &.badge-pass {
    background: rgba(103, 194, 58, 0.15);
    .test-result { color: #52C41A; }
  }

  &.badge-fail {
    background: rgba(245, 108, 108, 0.15);
    .test-result { color: #FF4D4F; }
  }
}

.test-result {
  font-size: 22rpx;
  font-weight: 600;
}

.delivery-info {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.delivery-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
}

.delivery-item {
  text-align: center;
  padding: 20rpx;
  background: #F8FAFF;
  border-radius: 16rpx;
}

.delivery-icon {
  font-size: 32rpx;
  display: block;
  margin-bottom: 8rpx;
}

.delivery-label {
  font-size: 22rpx;
  color: #8C8C8C;
  display: block;
  margin-bottom: 6rpx;
}

.delivery-value {
  font-size: 26rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
}

.action-bar {
  padding: 24rpx 0 40rpx;
}

.btn-icon {
  margin-right: 8rpx;
}

.empty-state {
  text-align: center;
  padding: 40rpx;
}

.empty-icon {
  font-size: 96rpx;
  display: block;
  margin-bottom: 32rpx;
}

.empty-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1F1F1F;
  display: block;
  margin-bottom: 16rpx;
}

.empty-desc {
  font-size: 28rpx;
  color: #8C8C8C;
  display: block;
  margin-bottom: 40rpx;
  line-height: 1.6;
}

.empty-hint {
  font-size: 24rpx;
  color: #BFBFBF;
  display: block;
  margin-top: 32rpx;
}

.demo-section {
  margin-top: 32rpx;
  padding: 0 40rpx;
}

.demo-label {
  font-size: 26rpx;
  color: #8C8C8C;
  display: block;
  margin-bottom: 16rpx;
  text-align: center;
}

.demo-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 16rpx;
}

.demo-item {
  padding: 16rpx 28rpx;
  background: rgba(59, 124, 255, 0.1);
  border: 2rpx solid rgba(59, 124, 255, 0.2);
  border-radius: 32rpx;
  font-size: 26rpx;
  color: #3B7CFF;
  font-weight: 500;
  transition: all 0.3s ease;

  &:active {
    background: rgba(59, 124, 255, 0.2);
    transform: scale(0.96);
  }
}

.test-detail-modal {
  width: 600rpx;
  padding: 0;
  border-radius: 24rpx;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  background: linear-gradient(135deg, #3B7CFF 0%, #5B9AFF 100%);
}

.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #ffffff;
}

.modal-close {
  width: 48rpx;
  height: 48rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 24rpx;
}

.modal-body {
  padding: 32rpx;
}

.test-detail-status {
  text-align: center;
  padding: 24rpx;
  border-radius: 16rpx;
  margin-bottom: 24rpx;

  &.status-pass {
    background: rgba(103, 194, 58, 0.1);
    .status-icon { color: #52C41A; }
    .status-text { color: #52C41A; }
  }

  &.status-fail {
    background: rgba(245, 108, 108, 0.1);
    .status-icon { color: #FF4D4F; }
    .status-text { color: #FF4D4F; }
  }
}

.status-icon {
  font-size: 48rpx;
  display: block;
  margin-bottom: 12rpx;
}

.status-text {
  font-size: 28rpx;
  font-weight: 600;
}

.test-detail-info,
.test-detail-desc {
  margin-bottom: 20rpx;
}

.detail-label {
  font-size: 24rpx;
  color: #8C8C8C;
  display: block;
  margin-bottom: 8rpx;
}

.detail-value {
  font-size: 32rpx;
  font-weight: 700;
  color: #1F1F1F;
  display: block;
}

.detail-desc-text {
  font-size: 26rpx;
  color: #4A4A4A;
  line-height: 1.6;
  display: block;
}

.batch-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.batch-card {
  flex: 1;
  background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
  border-radius: 24rpx;
  padding: 24rpx 32rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 8rpx 32rpx rgba(82, 196, 26, 0.3);
}

.batch-icon {
  width: 72rpx;
  height: 72rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.batch-info {
  flex: 1;
}

.batch-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  display: block;
  margin-bottom: 4rpx;
}

.batch-value {
  font-size: 28rpx;
  font-weight: 600;
  color: #ffffff;
  display: block;
}

.batch-count {
  padding: 16rpx 24rpx;
  background: rgba(82, 196, 26, 0.1);
  border-radius: 32rpx;
  text-align: center;
  margin-left: 16rpx;
}

.count-text {
  font-size: 36rpx;
  font-weight: 700;
  color: #52C41A;
  display: block;
}

.count-label {
  font-size: 20rpx;
  color: #52C41A;
  display: block;
}

.batch-summary {
  background: linear-gradient(145deg, #FFFFFF 0%, #F0FFF0 100%);
  border-radius: 28rpx;
  padding: 28rpx 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 0 0 1rpx rgba(255, 255, 255, 0.9);
  margin-bottom: 24rpx;
}

.summary-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
}

.summary-item {
  text-align: center;
  padding: 16rpx;
  background: rgba(82, 196, 26, 0.05);
  border-radius: 16rpx;
}

.summary-icon {
  font-size: 32rpx;
  display: block;
  margin-bottom: 8rpx;
}

.summary-label {
  font-size: 22rpx;
  color: #8C8C8C;
  display: block;
  margin-bottom: 6rpx;
}

.summary-value {
  font-size: 26rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
}

.wo-group-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.wo-group-item {
  background: #F8FAFF;
  border-radius: 16rpx;
  padding: 20rpx;
}

.wo-group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.wo-group-no {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.wo-group-count {
  font-size: 24rpx;
  color: #52C41A;
  font-weight: 500;
  padding: 4rpx 16rpx;
  background: rgba(82, 196, 26, 0.1);
  border-radius: 16rpx;
}

.wo-group-sns {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.wo-sn-tag {
  padding: 8rpx 16rpx;
  background: rgba(59, 124, 255, 0.1);
  border-radius: 12rpx;
  font-size: 22rpx;
  color: #3B7CFF;
  transition: all 0.3s ease;

  &:active {
    background: rgba(59, 124, 255, 0.2);
    transform: scale(0.95);
  }
}

.sn-list-grid {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.sn-list-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #F8FAFF;
  border-radius: 16rpx;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  }
}

.sn-list-icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;

  &.status-default { background: rgba(191, 191, 191, 0.2); }
  &.status-produced { background: rgba(250, 173, 20, 0.2); }
  &.status-tested { background: rgba(59, 124, 255, 0.2); }
  &.status-delivered { background: rgba(82, 196, 26, 0.2); }
}

.sn-list-icon-text {
  font-size: 24rpx;
}

.sn-list-info {
  flex: 1;
}

.sn-list-code {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
  margin-bottom: 4rpx;
}

.sn-list-meta {
  font-size: 22rpx;
  color: #8C8C8C;
  display: block;
}

.sn-list-arrow {
  font-size: 32rpx;
  color: #BFBFBF;
}
</style>