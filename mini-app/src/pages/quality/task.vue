<template>
  <view class="task-page">
    <view class="filter-bar">
      <u-tabs :list="statusList" :current="currentStatus" @click="onStatusChange" :activeStyle="{ color: '#3B7CFF', fontWeight: 'bold' }" :inactiveStyle="{ color: '#8C8C8C' }" lineColor="#3B7CFF" :lineWidth="30"></u-tabs>
    </view>

    <view class="task-list">
      <view v-if="list.length === 0" style="padding-top: 80rpx;">
        <u-empty mode="data" text="暂无质检任务"></u-empty>
      </view>

      <view class="task-card" v-for="item in list" :key="item.id" @click="goIqc(item)">
        <view class="card-header">
          <text class="task-no">{{ item.taskNo }}</text>
          <u-tag :text="item.statusText" :type="item.statusType" size="mini"></u-tag>
        </view>
        <view class="card-body">
          <view class="info-row">
            <text class="info-label">类型</text>
            <text class="info-value">{{ item.taskTypeName }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">检验员</text>
            <text class="info-value">{{ item.inspectorName || '-' }}</text>
          </view>
        </view>
        <text class="task-time">{{ item.createTime }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getQualityTaskList } from '@/api/quality'

export default {
  data() {
    return {
      currentStatus: 0,
      statusList: [
        { name: '全部', value: '' },
        { name: '待处理', value: '0' },
        { name: '处理中', value: '1' },
        { name: '已完成', value: '2' }
      ],
      list: []
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
        const res = await getQualityTaskList({ status: statusVal || undefined, page: 1, size: 50 })
        const records = (res && res.records) || res || []
        this.list = records.map(item => ({
          ...item,
          statusText: this.getStatusText(item.status),
          statusType: this.getStatusType(item.status),
          taskTypeName: this.getTypeName(item.taskType)
        }))
      } catch {
        this.list = []
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
    goIqc(item) {
      if (item.status === 0 || item.status === 1) {
        uni.navigateTo({ url: '/pages/quality/iqc?taskId=' + item.id + '&taskNo=' + item.taskNo })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.task-page {
  min-height: 100vh;
  background: transparent;
}

.filter-bar {
  background: #ffffff;
  padding: 0 24rpx;
  border-radius: 28rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.task-list {
  padding: 16rpx 24rpx;
}

.task-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 28rpx 30rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.task-no {
  font-size: 30rpx;
  color: #3B7CFF;
  font-weight: 500;
}

.card-body {
  margin-bottom: 12rpx;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.info-label {
  font-size: 26rpx;
  color: #8C8C8C;
  width: 120rpx;
  flex-shrink: 0;
}

.info-value {
  font-size: 26rpx;
  color: #4A4A4A;
  flex: 1;
}

.task-time {
  font-size: 22rpx;
  color: #BFBFBF;
  display: block;
}
</style>