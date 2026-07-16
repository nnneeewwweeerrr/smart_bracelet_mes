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
          <text class="log-time">{{ item.createTime }}</text>
        </view>
        <text class="log-action">{{ item.operationDesc }}</text>
        <view class="log-detail">
          <text class="log-no">{{ item.ipAddress }}</text>
          <text class="log-status">{{ item.operationType }}</text>
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
        { name: '入库管理', value: '入库管理' },
        { name: '出库管理', value: '出库管理' },
        { name: '报工管理', value: '报工管理' },
        { name: '生产管理', value: '生产管理' },
        { name: 'SOP管理', value: 'SOP管理' },
        { name: '工单管理', value: '工单管理' },
        { name: '设备档案', value: '设备档案' },
        { name: '系统管理', value: '系统管理' }
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
      const map = {
        '入库管理': '入库管理',
        '出库管理': '出库管理',
        '报工管理': '报工管理',
        '生产管理': '生产管理',
        'SOP管理': 'SOP管理',
        '工单管理': '工单管理',
        '设备档案': '设备档案',
        '设备保养': '设备保养',
        '客户档案': '客户档案',
        '物料档案': '物料档案',
        '工序管理': '工序管理',
        '订单管理': '订单管理',
        '生产计划': '生产计划',
        '产品档案': '产品档案',
        '采购管理': '采购管理',
        '供应商管理': '供应商管理',
        '仓库管理': '仓库管理',
        'SN追溯': 'SN追溯',
        '系统管理': '系统管理'
      }
      return map[m] || m || '未知'
    },
    getModuleTag(m) {
      const map = {
        '入库管理': 'success',
        '出库管理': 'warning',
        '报工管理': 'primary',
        '生产管理': 'primary',
        'SOP管理': 'info',
        '工单管理': 'warning',
        '设备档案': 'info',
        '设备保养': 'info',
        '客户档案': 'info',
        '物料档案': 'info',
        '工序管理': 'primary',
        '订单管理': 'warning',
        '生产计划': 'warning',
        '产品档案': 'info',
        '采购管理': 'warning',
        '供应商管理': 'info',
        '仓库管理': 'success',
        'SN追溯': 'error',
        '系统管理': 'info'
      }
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
  color: #3B7CFF;
}
</style>