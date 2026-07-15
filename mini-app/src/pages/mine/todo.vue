<template>
  <view class="todo-page">
    <DynamicBackground theme="theme-primary"></DynamicBackground>
    <view class="page-content">
    <view class="filter-bar">
      <u-tabs :list="typeList" :current="currentType" @click="onTypeChange" :activeStyle="{ color: '#3B7CFF', fontWeight: 'bold' }" :inactiveStyle="{ color: '#8C8C8C' }" lineColor="#3B7CFF" :lineWidth="30"></u-tabs>
    </view>

    <view class="todo-list">
      <view v-if="list.length === 0" style="padding-top: 80rpx;">
        <u-empty mode="data" text="暂无待办任务"></u-empty>
      </view>

      <view class="todo-card" v-for="item in list" :key="item.id" @click="goAction(item)">
        <view class="card-header">
          <text class="todo-title">{{ item.title }}</text>
          <u-tag :text="item.typeName" :type="item.typeTag" size="mini" plain></u-tag>
        </view>
        <text class="todo-desc">{{ item.description }}</text>
        <view class="card-footer">
          <text class="todo-time">{{ item.createTime }}</text>
          <u-button type="primary" size="mini" :text="item.actionText" plain></u-button>
        </view>
      </view>
    </view>
    </view>
  </view>
</template>

<script>
import DynamicBackground from '@/components/DynamicBackground'
import { getWorkerTasks } from '@/api/user'
import { getWorkOrderList } from '@/api/production'
import { getQualityTaskList } from '@/api/quality'
import { getExceptionList } from '@/api/exception'

export default {
  components: { DynamicBackground },
  data() {
    return {
      currentType: 0,
      typeList: [
        { name: '全部' },
        { name: '生产' },
        { name: '质检' },
        { name: '异常' }
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
    onTypeChange({ index }) {
      this.currentType = index
      this.loadData()
    },
    async loadData() {
      const allTasks = []
      const type = this.currentType

      if (type === 0 || type === 1) {
        try {
          const res = await getWorkOrderList({ status: 1, page: 1, size: 50 })
          const orders = (res && res.records) || res || []
          orders.forEach(o => {
            allTasks.push({
              id: 'wo_' + o.id,
              title: o.workNo,
              description: (o.productName || '') + ' - ' + (o.processName || ''),
              typeName: '生产',
              typeTag: 'warning',
              actionText: '去报工',
              actionUrl: '/pages/production/report?workOrderId=' + o.id + '&workNo=' + o.workNo + '&processId=' + (o.processId || '') + '&processName=' + encodeURIComponent(o.processName || ''),
              createTime: o.createTime
            })
          })
        } catch { /* ignore */ }
      }

      if (type === 0 || type === 2) {
        try {
          const res = await getQualityTaskList({ status: 0, page: 1, size: 50 })
          const tasks = (res && res.records) || res || []
          tasks.forEach(t => {
            const typeMap = { incoming: '来料检验', process: '过程检验', finished: '成品检验' }
            allTasks.push({
              id: 'qt_' + t.id,
              title: t.taskNo,
              description: typeMap[t.taskType] || t.taskType || '质检任务',
              typeName: '质检',
              typeTag: 'error',
              actionText: '去检验',
              actionUrl: '/pages/quality/iqc?taskId=' + t.id + '&taskNo=' + t.taskNo,
              createTime: t.createTime
            })
          })
        } catch { /* ignore */ }
      }

      if (type === 0 || type === 3) {
        try {
          const res = await getExceptionList({ status: '0', page: 1, size: 50 })
          const exceptions = (res && res.records) || res || []
          exceptions.forEach(e => {
            const typeMap = { equipment: '设备故障', material: '物料短缺', quality: '质量问题', other: '其他异常' }
            allTasks.push({
              id: 'ex_' + e.id,
              title: e.exceptionNo,
              description: typeMap[e.exceptionType] || e.exceptionType || '异常',
              typeName: '异常',
              typeTag: 'primary',
              actionText: '去处理',
              actionUrl: '/pages/exception/pending',
              createTime: e.reportTime
            })
          })
        } catch { /* ignore */ }
      }

      this.list = allTasks
    },
    goAction(item) {
      uni.navigateTo({ url: item.actionUrl })
    }
  }
}
</script>

<style lang="scss" scoped>
.todo-page {
  min-height: 100vh;
  background: #F5F7FA;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
}

.filter-bar {
  background: #ffffff;
  padding: 0 24rpx;
  border-radius: 28rpx;
  margin: 24rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.todo-list {
  padding: 16rpx 24rpx;
}

.todo-card {
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
  margin-bottom: 12rpx;
}

.todo-title {
  font-size: 30rpx;
  color: #3B7CFF;
  font-weight: 500;
}

.todo-desc {
  font-size: 26rpx;
  color: #4A4A4A;
  display: block;
  margin-bottom: 16rpx;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.todo-time {
  font-size: 22rpx;
  color: #BFBFBF;
}
</style>