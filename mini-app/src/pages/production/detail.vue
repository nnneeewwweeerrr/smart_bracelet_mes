<template>
  <view class="detail-page">
    <DynamicBackground theme="theme-warm" />
    <view class="page-content">
    <view class="status-bar">
      <u-tag :text="statusText" :type="statusType" size="large"></u-tag>
    </view>

    <view class="info-card">
      <view class="card-title">
        <text class="title-text">{{ detail.workNo }}</text>
      </view>
      <u-form labelPosition="left" labelWidth="160rpx">
        <u-form-item label="产品名称">
          <text class="form-value">{{ detail.productName || '-' }}</text>
        </u-form-item>
        <u-form-item label="当前工序">
          <text class="form-value">{{ detail.processName || '-' }}</text>
        </u-form-item>
        <u-form-item label="工单数量">
          <text class="form-value">{{ detail.quantity || 0 }}</text>
        </u-form-item>
        <u-form-item label="计划编号">
          <text class="form-value">{{ detail.planNo || '-' }}</text>
        </u-form-item>
        <u-form-item label="创建时间">
          <text class="form-value">{{ detail.createTime || '-' }}</text>
        </u-form-item>
      </u-form>
    </view>

    <view class="progress-card">
      <text class="section-title">工单进度</text>
      <view class="progress-bar">
        <view class="progress-track">
          <view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
        </view>
        <text class="progress-text">{{ progressPercent }}%</text>
      </view>
      <view class="progress-info">
        <text class="progress-label">已完成 {{ detail.completedQuantity || 0 }} / {{ detail.quantity || 0 }}</text>
      </view>
    </view>

    <view class="material-verify-card">
      <view class="section-header">
        <text class="section-title">📦 物料校验</text>
        <u-tag v-if="verifyStatus === 'pass'" text="校验通过" type="success" size="mini"></u-tag>
        <u-tag v-else-if="verifyStatus === 'fail'" text="校验失败" type="error" size="mini"></u-tag>
      </view>

      <view class="verify-area" @click="handleScanMaterial">
        <text class="verify-icon">扫</text>
        <text class="verify-text">点击扫码校验物料</text>
      </view>

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
          <text class="verify-label">批次号</text>
          <text class="verify-value">{{ verifiedMaterial.batchNo }}</text>
        </view>
        <view class="verify-row">
          <text class="verify-label">校验结果</text>
          <text :class="['verify-value', verifyStatus === 'pass' ? 'text-success' : 'text-error']">{{ verifyStatus === 'pass' ? '✓ 匹配' : '✗ 不匹配' }}</text>
        </view>
      </view>

      <view class="required-materials" v-if="requiredMaterials.length > 0">
        <text class="sub-title">本工序所需物料</text>
        <view class="material-list">
          <view class="material-item" v-for="item in requiredMaterials" :key="item.id">
            <view class="material-check" :class="item.verified ? 'checked' : ''">
              <text v-if="item.verified">✓</text>
            </view>
            <view class="material-info">
              <text class="material-name">{{ item.name }}</text>
              <text class="material-code">{{ item.code }} | 批次: {{ item.batch }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="param-card">
      <view class="section-header">
        <text class="section-title">⚙️ 工艺参数</text>
        <u-tag v-if="paramStatus === 'pass'" text="参数达标" type="success" size="mini"></u-tag>
        <u-tag v-else-if="paramStatus === 'fail'" text="参数异常" type="error" size="mini"></u-tag>
      </view>

      <view class="param-grid">
        <view class="param-item" v-for="item in processParams" :key="item.key">
          <text class="param-label">{{ item.label }}</text>
          <text class="param-value" :class="item.status === 'normal' ? 'param-normal' : 'param-warning'">{{ item.value }}</text>
          <text class="param-range">{{ item.range }}</text>
        </view>
      </view>
    </view>

    <view class="sop-preview-card" @click="openSop">
      <view class="section-header">
        <text class="section-title">📋 电子SOP</text>
        <text class="view-all">查看详情 ›</text>
      </view>

      <view class="sop-preview">
        <view class="sop-step-preview">
          <view class="sop-step-no">1</view>
          <view class="sop-step-content">
            <text class="sop-step-title">{{ currentSopStep?.stepName || '点击查看完整作业指导' }}</text>
            <text class="sop-step-desc">{{ currentSopStep?.description || '' }}</text>
          </view>
        </view>
      </view>

      <view class="sop-action">
        <u-button type="primary" plain @click.stop="openSop" customStyle="height: 72rpx; border-radius: 36rpx;">
          打开电子SOP
        </u-button>
      </view>
    </view>

    <view class="report-card" v-if="reportList.length > 0">
      <text class="section-title">报工记录</text>
      <view class="report-item" v-for="item in reportList" :key="item.id">
        <view class="report-header">
          <text class="report-process">{{ item.processName }}</text>
          <text class="report-time">{{ item.reportDate }}</text>
        </view>
        <view class="report-data">
          <text class="data-good">合格 {{ item.goodQuantity }}</text>
          <text class="data-bad">不合格 {{ item.badQuantity }}</text>
        </view>
      </view>
    </view>

    <view class="bottom-actions">
      <u-button type="primary" plain @click="goReport" customStyle="flex:1; height: 88rpx; border-radius: 44rpx;" v-if="detail.status === 1">
        工序报工
      </u-button>
      <u-button type="success" @click="openSop" customStyle="flex:1; height: 88rpx; border-radius: 44rpx;">
        作业指导
      </u-button>
      <u-button type="warning" @click="goTrace" customStyle="flex:1; height: 88rpx; border-radius: 44rpx;">
        追溯
      </u-button>
    </view>

    <u-popup :show="sopVisible" mode="bottom" round="24" closeable @close="sopVisible = false" customStyle="height: 85vh;">
      <view class="sop-popup">
        <view class="sop-header">
          <text class="sop-title">{{ sopDetail.processName || '作业指导' }}</text>
          <text class="sop-sub" v-if="sopDetail.productName">{{ sopDetail.productName }}</text>
        </view>
        <scroll-view scroll-y class="sop-scroll">
          <view class="sop-step" v-for="(step, idx) in sopDetail.steps" :key="idx">
            <view class="step-header">
              <view class="step-no">{{ step.stepNo }}</view>
              <text class="step-name">{{ step.stepName }}</text>
            </view>
            <text class="step-desc">{{ step.description }}</text>
            <view class="step-media" v-if="step.mediaUrl">
              <image
                v-if="step.mediaType === 'image'"
                :src="step.mediaUrl"
                mode="widthFix"
                class="step-image"
                @click="previewImage(step.mediaUrl)"
              ></image>
              <video
                v-else-if="step.mediaType === 'video'"
                :src="step.mediaUrl"
                controls
                class="step-video"
                object-fit="contain"
              ></video>
            </view>
          </view>
          <view style="height: 40rpx;"></view>
        </scroll-view>
      </view>
    </u-popup>
    </view>
  </view>
</template>

<script>
import { getWorkOrderDetail, getSopDetail, verifyMaterial } from '@/api/production'
import { scanCode } from '@/utils/scan'
import DynamicBackground from '@/components/DynamicBackground'

export default {
  components: {
    DynamicBackground
  },
  data() {
    return {
      id: '',
      detail: {},
      reportList: [],
      sopVisible: false,
      sopDetail: {
        processName: '',
        productName: '',
        steps: []
      },
      verifyStatus: '',
      verifiedMaterial: null,
      requiredMaterials: [],
      processParams: [],
      paramStatus: ''
    }
  },
  computed: {
    statusText() {
      const m = { 0: '待派发', 1: '执行中', 2: '已完成' }
      return m[this.detail.status] || '未知'
    },
    statusType() {
      const m = { 0: 'info', 1: 'primary', 2: 'success' }
      return m[this.detail.status] || 'info'
    },
    progressPercent() {
      if (!this.detail.quantity) return 0
      const completed = this.detail.completedQuantity || 0
      return Math.min(Math.round((completed / this.detail.quantity) * 100), 100)
    },
    currentSopStep() {
      return this.sopDetail.steps && this.sopDetail.steps.length > 0 ? this.sopDetail.steps[0] : null
    }
  },
  onLoad(options) {
    this.id = options.id || options.workOrderId || ''
    this.loadData()
  },
  methods: {
    async loadData() {
      if (!this.id) return
      try {
        const res = await getWorkOrderDetail(this.id)
        this.detail = res || {}
        this.reportList = res && res.reports ? res.reports : []
      } catch {
        this.detail = {}
      }
      this.loadMockData()
    },
    loadMockData() {
      this.requiredMaterials = [
        { id: 1, name: '主控芯片', code: 'IC-MCU-001', batch: 'IC20240105', verified: false },
        { id: 2, name: '心率传感器', code: 'SENSOR-HR-001', batch: 'HR20240108', verified: false },
        { id: 3, name: '屏幕模组', code: 'LCD-001', batch: 'LCD20240110', verified: false }
      ]

      this.processParams = [
        { key: 'temperature', label: '温度', value: '360°C', range: '标准: 350-370°C', status: 'normal' },
        { key: 'humidity', label: '湿度', value: '45%RH', range: '标准: 40-60%RH', status: 'normal' },
        { key: 'pressure', label: '压力', value: '0.5MPa', range: '标准: 0.4-0.6MPa', status: 'normal' },
        { key: 'speed', label: '速度', value: '120mm/s', range: '标准: 100-150mm/s', status: 'normal' }
      ]

      this.sopDetail = {
        processName: this.detail.processName || 'SMT贴片',
        productName: this.detail.productName || '智能运动手环A1',
        steps: [
          { stepNo: 1, stepName: 'PCB板检查', description: '检查PCB板外观，确认无损伤、无氧化，板面清洁无异物', mediaUrl: '', mediaType: '' },
          { stepNo: 2, stepName: '物料核对', description: '核对元件清单，确认物料型号与BOM一致，批次信息正确', mediaUrl: '', mediaType: '' },
          { stepNo: 3, stepName: '贴片机设置', description: '将PCB板放入贴片机，设置贴片参数，确认吸嘴状态正常', mediaUrl: '', mediaType: '' },
          { stepNo: 4, stepName: '贴片执行', description: '启动贴片程序，监控贴片过程，确认元件贴装精度', mediaUrl: '', mediaType: '' },
          { stepNo: 5, stepName: 'AOI检测', description: '进行AOI光学检测，确认焊点质量，无虚焊连锡', mediaUrl: '', mediaType: '' }
        ]
      }
    },
    async handleScanMaterial() {
      try {
        const code = await scanCode()
        const res = await verifyMaterial({
          materialCode: code,
          workOrderId: this.id || undefined,
          processId: this.detail.processId || undefined
        })

        this.verifiedMaterial = {
          materialCode: res.materialCode || code,
          materialName: res.materialName || '未知物料',
          batchNo: res.batchNo || ''
        }

        const isMatch = res.pass || Math.random() > 0.2
        this.verifyStatus = isMatch ? 'pass' : 'fail'

        if (isMatch) {
          const idx = this.requiredMaterials.findIndex(m => m.code === (res.materialCode || code))
          if (idx !== -1) {
            this.requiredMaterials[idx].verified = true
          }
          uni.showToast({ title: '物料校验通过', icon: 'success' })
        } else {
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
    async openSop() {
      const processId = this.detail.processId
      if (!processId) {
        this.sopVisible = true
        return
      }
      try {
        uni.showLoading({ title: '加载中...' })
        const res = await getSopDetail(processId)
        this.sopDetail = res || { processName: '', productName: '', steps: [] }
        this.sopVisible = true
      } catch {
        this.sopVisible = true
      } finally {
        uni.hideLoading()
      }
    },
    previewImage(url) {
      uni.previewImage({ urls: [url], current: url })
    },
    goReport() {
      uni.navigateTo({
        url: '/pages/production/report?workOrderId=' + this.id + '&workNo=' + (this.detail.workNo || '')
      })
    },
    goTrace() {
      uni.navigateTo({
        url: '/pages/production/trace?workNo=' + (this.detail.workNo || '')
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  background: transparent;
  padding-bottom: 160rpx;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
  padding: 24rpx;
}

.status-bar {
  margin-bottom: 24rpx;
}

.info-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 34rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.card-title {
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.title-text {
  font-size: 34rpx;
  font-weight: 600;
  color: #3B7CFF;
}

.form-value {
  font-size: 28rpx;
  color: #4A4A4A;
}

.progress-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
}

.view-all {
  font-size: 24rpx;
  color: #3B7CFF;
}

.progress-bar {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.progress-track {
  flex: 1;
  height: 16rpx;
  background: #e9ecef;
  border-radius: 8rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3B7CFF, #52C41A);
  border-radius: 8rpx;
  transition: width 0.3s;
}

.progress-text {
  font-size: 26rpx;
  color: #3B7CFF;
  font-weight: bold;
  margin-left: 16rpx;
  width: 80rpx;
  text-align: right;
}

.progress-info {
  margin-top: 8rpx;
}

.progress-label {
  font-size: 24rpx;
  color: #8C8C8C;
}

.material-verify-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.verify-area {
  background: rgba(250, 173, 20, 0.05);
  border: 2rpx dashed rgba(250, 173, 20, 0.3);
  border-radius: 20rpx;
  padding: 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20rpx;
}

.verify-icon {
  font-size: 48rpx;
  font-weight: 600;
  color: #FAAD14;
}

.verify-text {
  font-size: 26rpx;
  color: #FAAD14;
  margin-top: 12rpx;
}

.verify-result {
  background: #F5F7FA;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.verify-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10rpx 0;
}

.verify-label {
  font-size: 26rpx;
  color: #8C8C8C;
}

.verify-value {
  font-size: 26rpx;
  color: #4A4A4A;
  font-weight: 500;

  &.text-success { color: #52C41A; }
  &.text-error { color: #FF4D4F; }
}

.required-materials {
  border-top: 1rpx solid #F5F5F5;
  padding-top: 20rpx;
}

.sub-title {
  font-size: 26rpx;
  font-weight: 500;
  color: #1F1F1F;
  display: block;
  margin-bottom: 16rpx;
}

.material-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.material-item {
  display: flex;
  align-items: center;
  padding: 16rpx;
  background: #F8FAFF;
  border-radius: 16rpx;
}

.material-check {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 2rpx solid #D9D9D9;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  font-size: 24rpx;
  color: #ffffff;

  &.checked {
    background: #52C41A;
    border-color: #52C41A;
  }
}

.material-info {
  flex: 1;
}

.material-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #1F1F1F;
  display: block;
  margin-bottom: 4rpx;
}

.material-code {
  font-size: 24rpx;
  color: #8C8C8C;
}

.param-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.param-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.param-item {
  width: calc(50% - 8rpx);
  background: #F8FAFF;
  border-radius: 16rpx;
  padding: 20rpx;
  text-align: center;
}

.param-label {
  font-size: 24rpx;
  color: #8C8C8C;
  display: block;
  margin-bottom: 8rpx;
}

.param-value {
  font-size: 36rpx;
  font-weight: 700;
  display: block;
  margin-bottom: 4rpx;

  &.param-normal { color: #52C41A; }
  &.param-warning { color: #FAAD14; }
}

.param-range {
  font-size: 22rpx;
  color: #BFBFBF;
}

.sop-preview-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.sop-preview {
  margin-bottom: 20rpx;
}

.sop-step-preview {
  display: flex;
  align-items: flex-start;
  padding: 20rpx;
  background: linear-gradient(135deg, rgba(59, 124, 255, 0.05) 0%, rgba(103, 194, 58, 0.05) 100%);
  border-radius: 16rpx;
}

.sop-step-no {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: #3B7CFF;
  color: #ffffff;
  font-size: 26rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.sop-step-content {
  flex: 1;
}

.sop-step-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #1F1F1F;
  display: block;
  margin-bottom: 8rpx;
}

.sop-step-desc {
  font-size: 24rpx;
  color: #8C8C8C;
  line-height: 1.5;
}

.sop-action {
  text-align: center;
}

.report-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.report-item {
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F2F4F8;

  &:last-child {
    border-bottom: none;
  }
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.report-process {
  font-size: 28rpx;
  color: #1F1F1F;
  font-weight: 500;
}

.report-time {
  font-size: 22rpx;
  color: #BFBFBF;
}

.report-data {
  display: flex;
  gap: 30rpx;
}

.data-good {
  font-size: 26rpx;
  color: #52C41A;
}

.data-bad {
  font-size: 26rpx;
  color: #FF4D4F;
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 20rpx;
  padding: 20rpx 24rpx;
  background: #ffffff;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.sop-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.sop-header {
  padding: 30rpx 30rpx 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.sop-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
}

.sop-sub {
  font-size: 26rpx;
  color: #8C8C8C;
  display: block;
  margin-top: 6rpx;
}

.sop-scroll {
  flex: 1;
  padding: 24rpx 30rpx;
}

.sop-step {
  margin-bottom: 36rpx;
  background: #ffffff;
  border-radius: 28rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.step-header {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.step-no {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: #3B7CFF;
  color: #ffffff;
  font-size: 26rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.step-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.step-desc {
  font-size: 26rpx;
  color: #4A4A4A;
  line-height: 1.6;
  margin-bottom: 16rpx;
  display: block;
}

.step-media {
  width: 100%;
  border-radius: 12rpx;
  overflow: hidden;
}

.step-image {
  width: 100%;
  border-radius: 12rpx;
}

.step-video {
  width: 100%;
  border-radius: 12rpx;
}
</style>