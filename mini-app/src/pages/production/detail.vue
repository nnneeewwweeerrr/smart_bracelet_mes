<template>
  <view class="detail-page">
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
      <u-button type="success" plain @click="openSop" customStyle="flex:1; height: 88rpx; border-radius: 44rpx;">
        查看作业指导
      </u-button>
      <u-button type="warning" @click="goTrace" customStyle="flex:1; height: 88rpx; border-radius: 44rpx;">
        查看完整履历
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
</template>

<script>
import { getWorkOrderDetail, getSopDetail } from '@/api/production'

export default {
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
      }
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
    },
    async openSop() {
      const processId = this.detail.processId
      if (!processId) {
        uni.showToast({ title: '未获取到工序信息', icon: 'none' })
        return
      }
      try {
        uni.showLoading({ title: '加载中...' })
        const res = await getSopDetail(processId)
        this.sopDetail = res || { processName: '', productName: '', steps: [] }
        this.sopVisible = true
      } catch {
        uni.showToast({ title: '获取作业指导失败', icon: 'none' })
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
  padding: 24rpx 24rpx 160rpx;
}

.status-bar {
  margin-bottom: 24rpx;
}

.info-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 34rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
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
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
  margin-bottom: 20rpx;
  display: block;
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

.report-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
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
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
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
