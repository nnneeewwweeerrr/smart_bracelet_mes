<template>
  <view class="workorder-page">
    <view class="filter-bar">
      <u-tabs :list="statusList" :current="currentStatus" @click="onStatusChange" :activeStyle="{ color: '#3B7CFF', fontWeight: 'bold' }" :inactiveStyle="{ color: '#8C8C8C' }" lineColor="#3B7CFF" :lineWidth="30"></u-tabs>
    </view>

    <view class="order-list">
      <view v-if="list.length === 0" style="padding-top: 80rpx;">
        <u-empty mode="data" text="暂无工单"></u-empty>
      </view>

      <view class="order-card" v-for="item in list" :key="item.id" @click="goDetail(item)">
        <view class="card-header">
          <text class="order-no">{{ item.workNo }}</text>
          <u-tag :text="item.statusText" :type="item.statusType" size="mini"></u-tag>
        </view>
        <view class="card-body">
          <view class="info-row">
            <text class="info-label">产品</text>
            <text class="info-value">{{ item.productName || '-' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">工序</text>
            <text class="info-value">{{ item.processName || '-' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">数量</text>
            <text class="info-value">{{ item.quantity }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">计划编号</text>
            <text class="info-value">{{ item.planNo || '-' }}</text>
          </view>
        </view>
        <view class="card-footer">
          <text class="order-time">{{ item.createTime }}</text>
          <view class="card-btns">
            <u-button type="primary" size="mini" text="报工" v-if="item.status === 1" @click.stop="goReport(item)"></u-button>
            <u-button type="warning" size="mini" text="详情" plain @click.stop="goDetail(item)"></u-button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getWorkOrderList } from '@/api/production'

export default {
  data() {
    return {
      currentStatus: 0,
      statusList: [
        { name: '全部', value: '' },
        { name: '待派发', value: '0' },
        { name: '执行中', value: '1' },
        { name: '已完成', value: '2' }
      ],
      list: [],
      page: 1,
      size: 20
    }
  },
  onLoad() {
    this.loadData()
  },
  onPullDownRefresh() {
    this.page = 1
    this.loadData()
    uni.stopPullDownRefresh()
  },
  onReachBottom() {
    this.loadMore()
  },
  methods: {
    onStatusChange({ index }) {
      this.currentStatus = index
      this.page = 1
      this.loadData()
    },
    async loadData() {
      const statusVal = this.statusList[this.currentStatus].value
      const params = {
        page: this.page,
        size: this.size
      }
      if (statusVal && statusVal !== '') {
        params.status = parseInt(statusVal)
      }
      try {
        const res = await getWorkOrderList(params)
        const records = (res && res.records) || res || []
        this.list = records.map(item => ({
          ...item,
          statusText: this.getStatusText(item.status),
          statusType: this.getStatusType(item.status)
        }))
      } catch {
        this.list = []
      }
    },
    async loadMore() {
      this.page++
      const statusVal = this.statusList[this.currentStatus].value
      const params = {
        page: this.page,
        size: this.size
      }
      if (statusVal && statusVal !== '') {
        params.status = parseInt(statusVal)
      }
      try {
        const res = await getWorkOrderList(params)
        const records = (res && res.records) || res || []
        if (records.length === 0) { this.page--; return }
        this.list = this.list.concat(records.map(item => ({
          ...item,
          statusText: this.getStatusText(item.status),
          statusType: this.getStatusType(item.status)
        })))
      } catch {
        this.page--
      }
    },
    getStatusText(s) {
      const m = { 0: '待派发', 1: '执行中', 2: '已完成' }
      return m[s] || '未知'
    },
    getStatusType(s) {
      const m = { 0: 'info', 1: 'primary', 2: 'success' }
      return m[s] || 'info'
    },
    goReport(item) {
      uni.navigateTo({
        url: '/pages/production/report?workOrderId=' + item.id + '&workNo=' + item.workNo + '&processId=' + (item.processId || '') + '&processName=' + (item.processName || '')
      })
    },
    goDetail(item) {
      uni.navigateTo({
        url: '/pages/production/detail?id=' + item.id
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.workorder-page {
  min-height: 100vh;
  background: #F5F7FA;
}

.filter-bar {
  background: #ffffff;
  padding: 0 24rpx;
  border-radius: 28rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.order-list {
  padding: 16rpx 24rpx;
}

.order-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 28rpx 32rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.order-no {
  font-size: 30rpx;
  color: #3B7CFF;
  font-weight: 500;
}

.card-body {
  margin-bottom: 16rpx;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.info-label {
  font-size: 26rpx;
  color: #8C8C8C;
  width: 140rpx;
  flex-shrink: 0;
}

.info-value {
  font-size: 26rpx;
  color: #4A4A4A;
  flex: 1;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-btns {
  display: flex;
  gap: 12rpx;
}

.order-time {
  font-size: 22rpx;
  color: #BFBFBF;
}
</style>