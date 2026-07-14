<template>
  <view class="record-page">
    <DynamicBackground theme="theme-primary"></DynamicBackground>
    <view class="page-content">
    <view class="tab-bar">
      <u-tabs :list="tabList" :current="currentTab" @click="onTabChange" :activeStyle="{ color: '#3B7CFF', fontWeight: 'bold' }" :inactiveStyle="{ color: '#8C8C8C' }" lineColor="#3B7CFF" :lineWidth="30"></u-tabs>
    </view>

    <view class="filter-row">
      <u-search v-model="keyword" placeholder="搜索单号" @search="doSearch" @custom="doSearch" @clear="doSearch" shape="round"></u-search>
    </view>

    <view class="record-list">
      <view v-if="list.length === 0" style="padding-top: 80rpx;">
        <u-empty mode="data" :text="currentTab === 0 ? '暂无入库记录' : '暂无出库记录'"></u-empty>
      </view>

      <view class="record-card" v-for="item in list" :key="item.id">
        <view class="card-header">
          <text class="record-no">{{ currentTab === 0 ? item.inNo : item.outNo }}</text>
          <u-tag :text="item.sourceType || item.targetType || '-'" :type="currentTab === 0 ? 'success' : 'primary'" size="mini" plain></u-tag>
        </view>
        <view class="card-body">
          <view class="info-row">
            <text class="info-label">物料</text>
            <text class="info-value">{{ item.materialName || '-' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">仓库</text>
            <text class="info-value">{{ item.warehouseName || '-' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">数量</text>
            <text class="info-value quantity">{{ currentTab === 0 ? '+' : '-' }}{{ item.quantity }}</text>
          </view>
          <view class="info-row" v-if="item.sourceNo || item.targetNo">
            <text class="info-label">关联单号</text>
            <text class="info-value">{{ item.sourceNo || item.targetNo }}</text>
          </view>
        </view>
        <text class="record-time">{{ item.createTime }}</text>
      </view>
    </view>
    </view>
  </view>
</template>

<script>
import DynamicBackground from '@/components/DynamicBackground'
import { getStockInList, getStockOutList } from '@/api/stock'

export default {
  components: { DynamicBackground },
  data() {
    return {
      currentTab: 0,
      tabList: [
        { name: '入库记录' },
        { name: '出库记录' }
      ],
      keyword: '',
      list: [],
      page: 1,
      size: 20
    }
  },
  onLoad() {
    this.doSearch()
  },
  onPullDownRefresh() {
    this.page = 1
    this.doSearch()
    uni.stopPullDownRefresh()
  },
  onReachBottom() {
    this.loadMore()
  },
  methods: {
    onTabChange({ index }) {
      this.currentTab = index
      this.page = 1
      this.doSearch()
    },
    async doSearch() {
      this.page = 1
      await this.loadData()
    },
    async loadData() {
      const api = this.currentTab === 0 ? getStockInList : getStockOutList
      const noField = this.currentTab === 0 ? 'inNo' : 'outNo'
      try {
        const res = await api({
          page: this.page,
          size: this.size,
          keyword: this.keyword
        })
        const records = (res && res.records) || res || []
        this.list = records
      } catch {
        this.list = []
      }
    },
    generateMockData() {
      if (this.currentTab === 0) {
        const mockInData = [
          { id: 1, inNo: 'IN20240115001', materialName: '主控芯片STM32L4', warehouseName: 'A仓-电子件区', quantity: 1000, sourceType: '采购入库', sourceNo: 'PO20240114001', createTime: '2024-01-15 09:30' },
          { id: 2, inNo: 'IN20240115002', materialName: '心率传感器MAX30102', warehouseName: 'A仓-电子件区', quantity: 500, sourceType: '采购入库', sourceNo: 'PO20240114002', createTime: '2024-01-15 10:15' },
          { id: 3, inNo: 'IN20240115003', materialName: '锂电池380mAh', warehouseName: 'B仓-电池区', quantity: 2000, sourceType: '采购入库', sourceNo: 'PO20240114003', createTime: '2024-01-15 11:00' },
          { id: 4, inNo: 'IN20240114001', materialName: '智能运动手环A1', warehouseName: 'C仓-成品区', quantity: 150, sourceType: '生产入库', sourceNo: 'WO20240113001', createTime: '2024-01-14 16:30' },
          { id: 5, inNo: 'IN20240114002', materialName: 'PCB电路板A1', warehouseName: 'A仓-电子件区', quantity: 1500, sourceType: '采购入库', sourceNo: 'PO20240113001', createTime: '2024-01-14 14:00' },
          { id: 6, inNo: 'IN20240113001', materialName: '蓝牙模块BLE5.0', warehouseName: 'A仓-电子件区', quantity: 800, sourceType: '采购入库', sourceNo: 'PO20240112001', createTime: '2024-01-13 10:30' }
        ]
        
        if (this.keyword.trim()) {
          const key = this.keyword.toLowerCase()
          return mockInData.filter(item => item.inNo.toLowerCase().includes(key))
        }
        return mockInData
      } else {
        const mockOutData = [
          { id: 1, outNo: 'OUT20240115001', materialName: '主控芯片STM32L4', warehouseName: 'A仓-电子件区', quantity: 200, targetType: '生产领料', targetNo: 'WO20240115001', createTime: '2024-01-15 08:30' },
          { id: 2, outNo: 'OUT20240115002', materialName: '心率传感器MAX30102', warehouseName: 'A仓-电子件区', quantity: 200, targetType: '生产领料', targetNo: 'WO20240115001', createTime: '2024-01-15 08:35' },
          { id: 3, outNo: 'OUT20240115003', materialName: '锂电池380mAh', warehouseName: 'B仓-电池区', quantity: 200, targetType: '生产领料', targetNo: 'WO20240115001', createTime: '2024-01-15 08:40' },
          { id: 4, outNo: 'OUT20240114001', materialName: '智能运动手环A1', warehouseName: 'C仓-成品区', quantity: 50, targetType: '销售出库', targetNo: 'SO20240114001', createTime: '2024-01-14 15:00' },
          { id: 5, outNo: 'OUT20240114002', materialName: 'PCB电路板A1', warehouseName: 'A仓-电子件区', quantity: 300, targetType: '生产领料', targetNo: 'WO20240114001', createTime: '2024-01-14 09:00' },
          { id: 6, outNo: 'OUT20240113001', materialName: '蓝牙模块BLE5.0', warehouseName: 'A仓-电子件区', quantity: 150, targetType: '生产领料', targetNo: 'WO20240113001', createTime: '2024-01-13 08:30' }
        ]
        
        if (this.keyword.trim()) {
          const key = this.keyword.toLowerCase()
          return mockOutData.filter(item => item.outNo.toLowerCase().includes(key))
        }
        return mockOutData
      }
    },
    async loadMore() {
      this.page++
      const api = this.currentTab === 0 ? getStockInList : getStockOutList
      try {
        const res = await api({
          page: this.page,
          size: this.size,
          keyword: this.keyword
        })
        const records = (res && res.records) || res || []
        if (records.length === 0) {
          this.page--
          return
        }
        this.list = this.list.concat(records)
      } catch {
        this.page--
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.record-page {
  min-height: 100vh;
  background: #F5F7FA;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
}

.tab-bar,
.filter-row {
  background: #ffffff;
  border-radius: 28rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 10;
}

.tab-bar {
  padding: 0 24rpx;
}

.filter-row {
  padding: 16rpx 24rpx;
  margin-bottom: 16rpx;
}

.record-list {
  padding: 0 24rpx 24rpx;
}

.record-card {
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

.record-no {
  font-size: 28rpx;
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
  width: 140rpx;
  flex-shrink: 0;
}

.info-value {
  font-size: 26rpx;
  color: #4A4A4A;
  flex: 1;
}

.info-value.quantity {
  font-weight: 600;
  color: #4A4A4A;
}

.record-time {
  font-size: 22rpx;
  color: #BFBFBF;
  display: block;
}
</style>