<template>
  <view class="pending-page">
    <view class="filter-bar">
      <u-tabs :list="statusList" :current="currentStatus" @click="onStatusChange" :activeStyle="{ color: '#3B7CFF', fontWeight: 'bold' }" :inactiveStyle="{ color: '#8C8C8C' }" lineColor="#3B7CFF" :lineWidth="30"></u-tabs>
    </view>

    <view class="pending-list">
      <view v-if="list.length === 0" style="padding-top: 80rpx;">
        <u-empty mode="data" text="暂无待处理异常"></u-empty>
      </view>

      <view class="pending-card" v-for="item in list" :key="item.id" @click="showDetail(item)">
        <view class="card-header">
          <text class="exception-no">{{ item.exceptionNo }}</text>
          <u-tag :text="item.statusText" :type="item.statusType" size="mini"></u-tag>
        </view>
        <view class="card-body">
          <view class="info-row">
            <text class="info-label">类型</text>
            <text class="info-value">{{ item.exceptionTypeName }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">描述</text>
            <text class="info-value desc">{{ item.description }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">上报人</text>
            <text class="info-value">{{ item.reporterName || '-' }}</text>
          </view>
        </view>
        <view class="card-footer">
          <text class="exception-time">{{ item.reportTime }}</text>
          <u-button v-if="item.status === 0" type="warning" size="mini" text="开始处理" @click.stop="handleStart(item)"></u-button>
          <u-button v-if="item.status === 1" type="success" size="mini" text="完成处理" @click.stop="handleFinish(item)"></u-button>
        </view>
      </view>
    </view>

    <u-popup :show="showHandlePopup" mode="bottom" round="24" @close="showHandlePopup = false" closeable>
      <view class="handle-popup">
        <text class="popup-title">处理异常</text>
        <view class="popup-info">
          <text class="popup-label">异常编号: {{ currentException.exceptionNo }}</text>
          <text class="popup-label">类型: {{ currentException.exceptionTypeName }}</text>
        </view>
        <u-form labelPosition="left" labelWidth="160rpx">
          <u-form-item label="处理说明">
            <u-textarea v-model="handleForm.remark" placeholder="请输入处理说明" count maxlength="500"></u-textarea>
          </u-form-item>
        </u-form>
        <u-button type="primary" :loading="handleLoading" @click="submitHandle" customStyle="margin-top: 30rpx; height: 88rpx; border-radius: 44rpx;">
          确认提交
        </u-button>
      </view>
    </u-popup>
  </view>
</template>

<script>
import { getExceptionList, handleException } from '@/api/exception'

export default {
  data() {
    return {
      currentStatus: 0,
      statusList: [
        { name: '待处理', value: '0' },
        { name: '处理中', value: '1' },
        { name: '已解决', value: '2' }
      ],
      list: [],
      showHandlePopup: false,
      currentException: {},
      handleForm: { remark: '' },
      handleLoading: false
    }
  },
  onLoad() {
    this.loadData()
  },
  onPullDownRefresh() {
    this.loadData()
    uni.stopPullDownRefresh()
  },
  methods: {
    onStatusChange({ index }) {
      this.currentStatus = index
      this.loadData()
    },
    async loadData() {
      const statusVal = this.statusList[this.currentStatus].value
      try {
        const res = await getExceptionList({ status: statusVal, page: 1, size: 50 })
        const records = (res && res.records) || res || []
        this.list = records.map(item => ({
          ...item,
          statusText: this.getStatusText(item.status),
          statusType: this.getStatusType(item.status),
          exceptionTypeName: this.getTypeName(item.exceptionType)
        }))
      } catch {
        this.list = []
      }
    },
    getStatusText(s) {
      const m = { 0: '待处理', 1: '处理中', 2: '已解决' }
      return m[s] || '未知'
    },
    getStatusType(s) {
      const m = { 0: 'error', 1: 'warning', 2: 'success' }
      return m[s] || 'info'
    },
    getTypeName(t) {
      const m = { equipment: '设备故障', material: '物料短缺', quality: '质量问题', other: '其他异常' }
      return m[t] || t || '未知'
    },
    showDetail(item) {
      uni.showModal({
        title: item.exceptionNo,
        content: `类型: ${item.exceptionTypeName}\n描述: ${item.description}\n状态: ${item.statusText}\n上报人: ${item.reporterName || '-'}\n时间: ${item.reportTime}`,
        showCancel: false
      })
    },
    handleStart(item) {
      this.currentException = item
      this.handleForm.remark = ''
      this.showHandlePopup = true
    },
    handleFinish(item) {
      this.currentException = item
      this.handleForm.remark = ''
      this.showHandlePopup = true
    },
    async submitHandle() {
      if (!this.handleForm.remark) {
        uni.showToast({ title: '请填写处理说明', icon: 'none' })
        return
      }
      this.handleLoading = true
      try {
        const newStatus = this.currentException.status === 0 ? 1 : 2
        await handleException(this.currentException.id, {
          approveStatus: newStatus,
          remark: this.handleForm.remark
        })
        uni.showToast({ title: '处理成功', icon: 'success' })
        this.showHandlePopup = false
        this.loadData()
      } catch {
        // handled
      } finally {
        this.handleLoading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.pending-page {
  min-height: 100vh;
  background: transparent;
}

.filter-bar {
  background: #ffffff;
  padding: 0 24rpx;
  border-radius: 28rpx;
  margin: 24rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.pending-list {
  padding: 16rpx 24rpx;
}

.pending-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 24rpx 30rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.exception-no {
  font-size: 30rpx;
  color: #3B7CFF;
  font-weight: 500;
}

.card-body {
  margin-bottom: 16rpx;
}

.info-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8rpx;
}

.info-label {
  font-size: 26rpx;
  color: #8C8C8C;
  width: 100rpx;
  flex-shrink: 0;
}

.info-value {
  font-size: 26rpx;
  color: #4A4A4A;
  flex: 1;

  &.desc {
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.exception-time {
  font-size: 22rpx;
  color: #BFBFBF;
}

.handle-popup {
  padding: 40rpx 30rpx;
}

.popup-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
  margin-bottom: 20rpx;
  text-align: center;
}

.popup-info {
  margin-bottom: 20rpx;
}

.popup-label {
  font-size: 26rpx;
  color: #8C8C8C;
  display: block;
  margin-bottom: 8rpx;
}
</style>