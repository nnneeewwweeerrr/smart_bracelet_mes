<template>
  <view class="log-page">
    <DynamicBackground theme="theme-primary" />
    <view class="page-content">
    <view class="filter-bar">
      <u-tabs :list="moduleList" :current="currentModule" @click="onModuleChange" :activeStyle="{ color: '#3B7CFF', fontWeight: 'bold' }" :inactiveStyle="{ color: '#8C8C8C' }" lineColor="#3B7CFF" :lineWidth="30"></u-tabs>
    </view>

    <view class="log-list">
      <view v-if="list.length === 0" style="padding-top: 80rpx;">
        <u-empty mode="data" text="暂无操作记录"></u-empty>
      </view>

      <view class="log-card" v-for="item in list" :key="item.id">
        <view class="log-header">
          <u-tag :text="item.moduleName" :type="item.moduleTag" size="mini" plain></u-tag>
          <text class="log-time">{{ item.operationTime }}</text>
        </view>
        <text class="log-action">{{ item.operationContent }}</text>
        <view class="log-detail">
          <text class="log-no">{{ item.operationNo }}</text>
          <text :class="['log-status', item.success ? 'success' : 'fail']">{{ item.success ? '成功' : '失败' }}</text>
        </view>
      </view>
    </view>
    </view>
  </view>
</template>

<script>
import { getOperationLogList } from '@/api/user'
import DynamicBackground from '@/components/DynamicBackground'

export default {
  components: { DynamicBackground },
  data() {
    return {
      currentModule: 0,
      moduleList: [
        { name: '全部', value: '' },
        { name: '仓储', value: 'stock' },
        { name: '生产', value: 'production' },
        { name: '质检', value: 'quality' },
        { name: '异常', value: 'exception' }
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
    onModuleChange({ index }) {
      this.currentModule = index
      this.page = 1
      this.loadData()
    },
    async loadData() {
      const moduleVal = this.moduleList[this.currentModule].value
      try {
        const res = await getOperationLogList({
          page: this.page,
          size: this.size,
          module: moduleVal || undefined
        })
        const records = (res && res.records) || res || []
        this.list = records.map(item => ({
          ...item,
          moduleName: this.getModuleName(item.operationModule),
          moduleTag: this.getModuleTag(item.operationModule)
        }))
      } catch {
        this.list = []
      }
    },
    async loadMore() {
      this.page++
      const moduleVal = this.moduleList[this.currentModule].value
      try {
        const res = await getOperationLogList({
          page: this.page,
          size: this.size,
          module: moduleVal || undefined
        })
        const records = (res && res.records) || res || []
        if (records.length === 0) { this.page--; return }
        this.list = this.list.concat(records.map(item => ({
          ...item,
          moduleName: this.getModuleName(item.operationModule),
          moduleTag: this.getModuleTag(item.operationModule)
        })))
      } catch {
        this.page--
      }
    },
    getModuleName(m) {
      const map = { stock: '仓储', production: '生产', quality: '质检', exception: '异常', system: '系统' }
      return map[m] || m || '未知'
    },
    getModuleTag(m) {
      const map = { stock: 'success', production: 'warning', quality: 'error', exception: 'primary', system: 'info' }
      return map[m] || 'info'
    }
  }
}
</script>

<style lang="scss" scoped>
.log-page {
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

.log-list {
  padding: 16rpx 24rpx;
}

.log-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 24rpx 30rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.log-time {
  font-size: 22rpx;
  color: #BFBFBF;
}

.log-action {
  font-size: 28rpx;
  color: #4A4A4A;
  display: block;
  margin-bottom: 12rpx;
}

.log-detail {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.log-no {
  font-size: 24rpx;
  color: #8C8C8C;
}

.log-status {
  font-size: 24rpx;
  font-weight: 500;

  &.success { color: #52C41A; }
  &.fail { color: #FF4D4F; }
}
</style>