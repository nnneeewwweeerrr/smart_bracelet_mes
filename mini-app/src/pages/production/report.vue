﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <view class="report-page">
    <view class="scan-area" @click="handleScan">
      <text class="scan-icon-text">扫</text>
      <text class="scan-text">点击扫描工单条码</text>
    </view>

    <view class="form-section">
      <text class="section-title">报工信息</text>

      <u-form :model="form" labelPosition="left" labelWidth="180rpx">
        <u-form-item label="工单号">
          <u-input v-model="form.workNo" placeholder="扫码或手动输入" @click="!form.workNo && handleScan()"></u-input>
        </u-form-item>
        <u-form-item label="工序" prop="processId">
          <u-input v-model="processName" placeholder="请选择工序" readonly @click="showProcessPicker = true">
            <template #suffix>
              <text class="suffix-arrow">›</text>
            </template>
          </u-input>
        </u-form-item>
        <u-form-item label="合格数量" prop="goodQuantity">
          <u-input v-model="form.goodQuantity" type="number" placeholder="请输入合格数量"></u-input>
        </u-form-item>
        <u-form-item label="不合格数量" prop="badQuantity">
          <u-input v-model="form.badQuantity" type="number" placeholder="请输入不合格数量"></u-input>
        </u-form-item>
      </u-form>

      <view class="summary" v-if="form.goodQuantity || form.badQuantity">
        <text class="summary-text">合计: {{ (Number(form.goodQuantity) || 0) + (Number(form.badQuantity) || 0) }} 件</text>
        <text class="summary-rate">合格率: {{ passRate }}%</text>
      </view>
    </view>

    <view class="verify-section">
      <view class="verify-header">
        <text class="section-title">物料校验</text>
        <u-tag v-if="verifyStatus === 'pass'" text="校验通过" type="success" size="mini"></u-tag>
        <u-tag v-else-if="verifyStatus === 'fail'" text="校验失败" type="error" size="mini"></u-tag>
      </view>

      <u-button type="warning" plain @click="handleScanMaterial" customStyle="height: 80rpx; border-radius: 40rpx; margin-bottom: 20rpx;">
        <text style="margin-right: 8rpx;">扫</text>
        扫码校验物料
      </u-button>

      <view class="verify-result" v-if="verifiedMaterial">
        <view class="verify-row">
          <text class="verify-label">物料编码</text>
          <text class="verify-value">{{ verifiedMaterial.materialCode }}</text>
        </view>
        <view class="verify-row">
          <text class="verify-label">物料名称</text>
          <text class="verify-value">{{ verifiedMaterial.materialName }}</text>
        </view>
        <view class="verify-row">
          <text class="verify-label">校验结果</text>
          <text :class="['verify-value', verifyStatus === 'pass' ? 'text-success' : 'text-error']">{{ verifyStatus === 'pass' ? '匹配' : '不匹配' }}</text>
        </view>
      </view>
    </view>

    <view class="param-section">
      <text class="section-title">实际工艺参数</text>

      <u-form :model="paramForm" labelPosition="left" labelWidth="180rpx">
        <u-form-item label="温度(℃)">
          <u-input v-model="paramForm.temperature" type="digit" placeholder="如 260"></u-input>
        </u-form-item>
        <u-form-item label="湿度(%RH)">
          <u-input v-model="paramForm.humidity" type="digit" placeholder="如 45"></u-input>
        </u-form-item>
        <u-form-item label="压力(MPa)">
          <u-input v-model="paramForm.pressure" type="digit" placeholder="如 0.5"></u-input>
        </u-form-item>
        <u-form-item label="速度(mm/s)">
          <u-input v-model="paramForm.speed" type="digit" placeholder="如 120"></u-input>
        </u-form-item>
      </u-form>
    </view>

    <view class="network-status" :class="{ offline: !isOnline }">
      <text class="network-icon">{{ isOnline ? '📶' : '📴' }}</text>
      <text class="network-text">{{ isOnline ? '网络正常' : '离线模式 - 报工数据将暂存本地' }}</text>
    </view>

    <view class="submit-area">
      <u-button type="primary" :loading="loading" @click="handleSubmit" customStyle="height: 88rpx; border-radius: 44rpx;" :disabled="!isOnline">
        {{ isOnline ? '提交报工' : '离线中，无法提交' }}
      </u-button>
      <u-button type="warning" plain @click="handleOfflineSubmit" customStyle="height: 88rpx; border-radius: 44rpx; margin-top: 16rpx;">
        <text style="margin-right: 8rpx;">📱</text>
        离线暂存报工
      </u-button>
    </view>

    <view class="offline-section" v-if="offlineRecords.length > 0">
      <view class="offline-header">
        <text class="section-title">离线暂存记录</text>
        <view class="sync-btn" @click="syncOfflineRecords">
          <text class="sync-icon">↻</text>
          <text class="sync-text">同步上传</text>
        </view>
      </view>
      <view class="offline-list">
        <view class="offline-item" v-for="(item, idx) in offlineRecords" :key="idx">
          <view class="offline-info">
            <text class="offline-workno">{{ item.form.workNo }}</text>
            <text class="offline-time">{{ item.createTime }}</text>
          </view>
          <view class="offline-actions">
            <view class="action-btn delete" @click="deleteOfflineRecord(idx)">删除</view>
          </view>
        </view>
      </view>
    </view>

    <u-picker :show="showProcessPicker" :columns="processColumns" keyName="processName" @confirm="onProcessConfirm" @cancel="showProcessPicker = false"></u-picker>
  </view>
</template>

<script>
import { scanCode } from '@/utils/scan'
import { scanProductionReport, productionReport, getProcessList, verifyMaterial } from '@/api/production'
import { getUser } from '@/utils/auth'

export default {
  data() {
    return {
      form: {
        workOrderId: '',
        workNo: '',
        processId: '',
        goodQuantity: '',
        badQuantity: ''
      },
      paramForm: {
        temperature: '',
        humidity: '',
        pressure: '',
        speed: ''
      },
      processName: '',
      processList: [],
      processColumns: [],
      showProcessPicker: false,
      loading: false,
      verifyStatus: '',
      verifiedMaterial: null,
      offlineRecords: [],
      isOnline: true
    }
  },
  computed: {
    passRate() {
      const good = Number(this.form.goodQuantity) || 0
      const bad = Number(this.form.badQuantity) || 0
      const total = good + bad
      if (total === 0) return '0.0'
      return ((good / total) * 100).toFixed(1)
    }
  },
  onLoad(options) {
    if (options.workOrderId) this.form.workOrderId = options.workOrderId
    if (options.workNo) this.form.workNo = options.workNo
    if (options.processId) this.form.processId = options.processId
    if (options.processName) this.processName = decodeURIComponent(options.processName || '')
    this.loadProcesses()
    this.checkNetwork()
    this.watchNetwork()
  },
  onShow() {
    this.loadOfflineRecords()
    this.checkNetwork()
    if (this.isOnline && this.offlineRecords.length > 0) {
      uni.showModal({
        title: '检测到离线记录',
        content: `发现${this.offlineRecords.length}条未同步的离线报工记录，是否立即同步？`,
        success: (res) => {
          if (res.confirm) {
            this.syncOfflineRecords()
          }
        }
      })
    }
  },
  onUnload() {
    if (this.networkListener) {
      uni.offNetworkStatusChange(this.networkListener)
    }
  },
  methods: {
    checkNetwork() {
      const network = uni.getNetworkType({
        success: (res) => {
          this.isOnline = res.networkType !== 'none'
        },
        fail: () => {
          this.isOnline = false
        }
      })
    },
    watchNetwork() {
      this.networkListener = uni.onNetworkStatusChange((res) => {
        const wasOffline = !this.isOnline
        this.isOnline = res.isConnected
        
        if (this.isOnline && wasOffline) {
          uni.showToast({
            title: '网络已恢复',
            icon: 'success'
          })
          
          if (this.offlineRecords.length > 0) {
            setTimeout(() => {
              uni.showModal({
                title: '检测到离线记录',
                content: `发现${this.offlineRecords.length}条未同步的离线报工记录，是否立即同步？`,
                success: (res) => {
                  if (res.confirm) {
                    this.syncOfflineRecords()
                  }
                }
              })
            }, 1000)
          }
        } else if (!this.isOnline) {
          uni.showToast({
            title: '已切换至离线模式',
            icon: 'none'
          })
        }
      })
    },
    async loadProcesses() {
      try {
        this.processList = await getProcessList() || []
        this.processColumns = [this.processList]
      } catch {
        this.processList = []
        this.processColumns = [[]]
      }
    },
    async handleScan() {
      try {
        const code = await scanCode()
        const res = await scanProductionReport({ code })
        this.form.workOrderId = res.workOrderId || ''
        this.form.workNo = res.workNo || code
        if (res.processId) this.form.processId = res.processId
        if (res.processName) this.processName = res.processName
      } catch {
        // cancelled
      }
    },
    async handleScanMaterial() {
      try {
        const code = await scanCode()
        const res = await verifyMaterial({
          materialCode: code,
          workOrderId: this.form.workOrderId || undefined,
          processId: this.form.processId || undefined
        })
        this.verifiedMaterial = {
          materialCode: res.materialCode || code,
          materialName: res.materialName || ''
        }
        this.verifyStatus = res.pass ? 'pass' : 'fail'

        if (!res.pass) {
          uni.showModal({
            title: '物料校验失败',
            content: res.reason || '扫码物料与当前工单/工序不匹配，请确认物料是否正确',
            showCancel: false,
            confirmColor: '#FF4D4F'
          })
        }
      } catch {
        this.verifiedMaterial = null
        this.verifyStatus = ''
      }
    },
    onProcessConfirm({ value }) {
      const item = value[0]
      if (item) {
        this.form.processId = item.id
        this.processName = item.processName
      }
      this.showProcessPicker = false
    },
    async handleSubmit() {
      if (!this.form.workNo) {
        uni.showToast({ title: '请输入工单号', icon: 'none' })
        return
      }
      if (!this.form.processId) {
        uni.showToast({ title: '请选择工序', icon: 'none' })
        return
      }
      const good = Number(this.form.goodQuantity) || 0
      const bad = Number(this.form.badQuantity) || 0
      if (good + bad <= 0) {
        uni.showToast({ title: '请输入报工数量', icon: 'none' })
        return
      }

      if (this.verifyStatus === 'fail') {
        uni.showModal({
          title: '物料校验未通过',
          content: '当前物料校验失败，无法提交报工。请重新扫码校验正确物料。',
          showCancel: false,
          confirmColor: '#FF4D4F'
        })
        return
      }

      const user = getUser() || {}
      this.loading = true
      try {
        await productionReport({
          workOrderId: this.form.workOrderId || undefined,
          workerId: user.id,
          processId: this.form.processId,
          goodQuantity: good,
          badQuantity: bad,
          materialCode: this.verifiedMaterial ? this.verifiedMaterial.materialCode : undefined,
          processParams: {
            temperature: this.paramForm.temperature ? Number(this.paramForm.temperature) : undefined,
            humidity: this.paramForm.humidity ? Number(this.paramForm.humidity) : undefined,
            pressure: this.paramForm.pressure ? Number(this.paramForm.pressure) : undefined,
            speed: this.paramForm.speed ? Number(this.paramForm.speed) : undefined
          }
        })
        uni.showToast({ title: '报工成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 1500)
      } catch {
        // handled
      } finally {
        this.loading = false
      }
    },
    handleOfflineSubmit() {
      if (!this.form.workNo) {
        uni.showToast({ title: '请输入工单号', icon: 'none' })
        return
      }
      if (!this.form.processId) {
        uni.showToast({ title: '请选择工序', icon: 'none' })
        return
      }
      const good = Number(this.form.goodQuantity) || 0
      const bad = Number(this.form.badQuantity) || 0
      if (good + bad <= 0) {
        uni.showToast({ title: '请输入报工数量', icon: 'none' })
        return
      }

      const record = {
        form: { ...this.form },
        paramForm: { ...this.paramForm },
        processName: this.processName,
        verifyStatus: this.verifyStatus,
        verifiedMaterial: this.verifiedMaterial,
        createTime: new Date().toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        }),
        syncStatus: 'pending'
      }

      this.offlineRecords.unshift(record)
      this.saveOfflineRecords()

      uni.showModal({
        title: '离线暂存成功',
        content: `报工记录已保存到本地，当前共有${this.offlineRecords.length}条待同步记录。联网后可点击"同步上传"按钮提交。`,
        showCancel: false
      })

      this.form = { workOrderId: '', workNo: '', processId: '', goodQuantity: '', badQuantity: '' }
      this.paramForm = { temperature: '', humidity: '', pressure: '', speed: '' }
      this.processName = ''
      this.verifyStatus = ''
      this.verifiedMaterial = null
    },
    loadOfflineRecords() {
      try {
        const records = uni.getStorageSync('offlineReportRecords')
        this.offlineRecords = records ? JSON.parse(records) : []
      } catch {
        this.offlineRecords = []
      }
    },
    saveOfflineRecords() {
      try {
        uni.setStorageSync('offlineReportRecords', JSON.stringify(this.offlineRecords))
      } catch {
        uni.showToast({ title: '存储失败', icon: 'none' })
      }
    },
    deleteOfflineRecord(index) {
      uni.showModal({
        title: '确认删除',
        content: '确定要删除这条离线记录吗？',
        success: (res) => {
          if (res.confirm) {
            this.offlineRecords.splice(index, 1)
            this.saveOfflineRecords()
            uni.showToast({ title: '删除成功', icon: 'success' })
          }
        }
      })
    },
    async syncOfflineRecords() {
      if (this.offlineRecords.length === 0) {
        uni.showToast({ title: '暂无离线记录', icon: 'none' })
        return
      }

      if (!this.isOnline) {
        uni.showToast({ title: '当前处于离线状态', icon: 'none' })
        return
      }

      const total = this.offlineRecords.length
      let successCount = 0
      let failCount = 0
      const failedRecords = []

      uni.showLoading({ title: `同步中 0/${total}` })

      for (let i = 0; i < this.offlineRecords.length; i++) {
        const record = this.offlineRecords[i]
        
        await new Promise(resolve => setTimeout(resolve, 500))

        uni.showLoading({ title: `同步中 ${i + 1}/${total}` })

        try {
          const user = getUser() || {}
          await productionReport({
            workOrderId: record.form.workOrderId || undefined,
            workerId: user.id,
            processId: record.form.processId,
            goodQuantity: Number(record.form.goodQuantity) || 0,
            badQuantity: Number(record.form.badQuantity) || 0,
            materialCode: record.verifiedMaterial ? record.verifiedMaterial.materialCode : undefined,
            processParams: {
              temperature: record.paramForm.temperature ? Number(record.paramForm.temperature) : undefined,
              humidity: record.paramForm.humidity ? Number(record.paramForm.humidity) : undefined,
              pressure: record.paramForm.pressure ? Number(record.paramForm.pressure) : undefined,
              speed: record.paramForm.speed ? Number(record.paramForm.speed) : undefined
            }
          })
          successCount++
        } catch {
          failCount++
          failedRecords.push(record)
        }
      }

      uni.hideLoading()

      if (failedRecords.length > 0) {
        this.offlineRecords = failedRecords
        this.saveOfflineRecords()
      } else {
        this.offlineRecords = []
        this.saveOfflineRecords()
      }

      uni.showModal({
        title: successCount === total ? '同步成功' : '同步完成',
        content: `成功上传${successCount}条，${failCount > 0 ? `失败${failCount}条（已保留在本地）` : ''}`,
        showCancel: false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.report-page {
  min-height: 100vh;
  background: transparent;
  padding: 24rpx 24rpx 40rpx;
}

.scan-area {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
  border: 2rpx dashed rgba(250, 173, 20, 0.35);
}

.scan-icon-text {
  font-size: 56rpx;
  font-weight: 600;
  color: #FAAD14;
}

.scan-text {
  font-size: 28rpx;
  color: #FAAD14;
  margin-top: 20rpx;
}

.suffix-arrow {
  font-size: 24rpx;
  color: #BFBFBF;
}

.form-section,
.verify-section,
.param-section {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 34rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
  margin-bottom: 20rpx;
  display: block;
}

.verify-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;

  .section-title {
    margin-bottom: 0;
  }
}

.verify-result {
  background: #F5F7FA;
  border-radius: 12rpx;
  padding: 20rpx;
}

.verify-row {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.verify-label {
  font-size: 26rpx;
  color: #8C8C8C;
  width: 160rpx;
  flex-shrink: 0;
}

.verify-value {
  font-size: 26rpx;
  color: #4A4A4A;
  flex: 1;

  &.text-success { color: #52C41A; font-weight: 500; }
  &.text-error { color: #FF4D4F; font-weight: 500; }
}

.summary {
  display: flex;
  justify-content: space-between;
  padding: 20rpx;
  background: #F5F7FA;
  border-radius: 12rpx;
  margin-top: 20rpx;
}

.summary-text {
  font-size: 28rpx;
  color: #4A4A4A;
  font-weight: 500;
}

.summary-rate {
  font-size: 28rpx;
  color: #52C41A;
  font-weight: 500;
}

.network-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 16rpx;
  background: rgba(82, 196, 26, 0.1);
  border-radius: 16rpx;
  margin-bottom: 20rpx;

  &.offline {
    background: rgba(255, 77, 79, 0.1);

    .network-icon,
    .network-text {
      color: #FF4D4F;
    }
  }
}

.network-icon {
  font-size: 28rpx;
}

.network-text {
  font-size: 24rpx;
  color: #52C41A;
}

.submit-area {
  padding-top: 10rpx;
}

.offline-section {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 34rpx;
  margin-top: 24rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.offline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.sync-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 20rpx;
  background: rgba(82, 196, 26, 0.1);
  border-radius: 20rpx;
}

.sync-icon {
  font-size: 24rpx;
  color: #52C41A;
}

.sync-text {
  font-size: 24rpx;
  color: #52C41A;
  font-weight: 500;
}

.offline-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.offline-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background: #F8FAFF;
  border-radius: 16rpx;
}

.offline-info {
  flex: 1;
}

.offline-workno {
  font-size: 26rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
  margin-bottom: 6rpx;
}

.offline-time {
  font-size: 22rpx;
  color: #8C8C8C;
}

.offline-actions {
  margin-left: 16rpx;
}

.action-btn {
  padding: 8rpx 20rpx;
  border-radius: 16rpx;
  font-size: 24rpx;
  font-weight: 500;

  &.delete {
    background: rgba(255, 77, 79, 0.1);
    color: #FF4D4F;
  }
}
</style>
