<template>
  <view class="material-page">
    <DynamicBackground theme="theme-primary"></DynamicBackground>
    <view class="page-content">
    <view class="search-bar">
      <u-search v-model="keyword" placeholder="搜索物料编码/名称" @search="doSearch" @custom="doSearch" @clear="doSearch"></u-search>
    </view>

    <view class="filter-bar">
      <u-tabs :list="typeList" :current="currentType" @click="onTypeChange" :activeStyle="{ color: '#3B7CFF', fontWeight: 'bold' }" :inactiveStyle="{ color: '#8C8C8C' }" lineColor="#3B7CFF" :lineWidth="30"></u-tabs>
    </view>

    <view class="material-list">
      <view v-if="list.length === 0" style="padding-top: 80rpx;">
        <u-empty mode="data" text="暂无物料数据"></u-empty>
      </view>
      <view class="material-card" v-for="item in list" :key="item.id" @click="showDetail(item)">
        <view class="card-header">
          <text class="material-code">{{ item.materialCode }}</text>
          <u-tag :text="item.materialTypeName" type="primary" size="mini" plain></u-tag>
        </view>
        <text class="material-name">{{ item.materialName }}</text>
        <view class="card-info">
          <text class="info-item">单位: {{ item.unit || '-' }}</text>
          <text class="info-item">单价: ¥{{ item.unitPrice || '0.00' }}</text>
          <text class="info-item">安全库存: {{ item.safetyStock || 0 }}</text>
        </view>
      </view>
    </view>
    </view>
  </view>
</template>

<script>
import DynamicBackground from '@/components/DynamicBackground'
import { getMaterialList } from '@/api/stock'

export default {
  components: { DynamicBackground },
  data() {
    return {
      keyword: '',
      currentType: 0,
      typeList: [
        { name: '全部', value: '' },
        { name: '原材料', value: 'raw' },
        { name: '零部件', value: 'component' },
        { name: '成品', value: 'finished' }
      ],
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
    onTypeChange({ index }) {
      this.currentType = index
      this.page = 1
      this.doSearch()
    },
    async doSearch() {
      this.page = 1
      const typeVal = this.typeList[this.currentType].value
      try {
        const res = await getMaterialList({
          page: this.page,
          size: this.size,
          keyword: this.keyword,
          materialType: typeVal || undefined
        })
        const records = (res && res.records) || res || []
        this.list = records.map(item => ({
          ...item,
          materialTypeName: this.getTypeName(item.materialType)
        }))
      } catch {
        this.list = []
      }
    },
    generateMockData(type) {
      const mockData = [
        { id: 1, materialCode: 'MAT001', materialName: '主控芯片STM32L4', materialType: 'component', unit: '颗', unitPrice: 12.50, safetyStock: 500 },
        { id: 2, materialCode: 'MAT002', materialName: '心率传感器MAX30102', materialType: 'component', unit: '颗', unitPrice: 8.90, safetyStock: 500 },
        { id: 3, materialCode: 'MAT003', materialName: 'OLED显示屏0.96寸', materialType: 'component', unit: '片', unitPrice: 15.00, safetyStock: 300 },
        { id: 4, materialCode: 'MAT004', materialName: '锂电池380mAh', materialType: 'component', unit: '节', unitPrice: 6.80, safetyStock: 1000 },
        { id: 5, materialCode: 'MAT005', materialName: '蓝牙模块BLE5.0', materialType: 'component', unit: '个', unitPrice: 9.20, safetyStock: 500 },
        { id: 6, materialCode: 'MAT006', materialName: 'PCB电路板A1', materialType: 'raw', unit: '块', unitPrice: 3.50, safetyStock: 2000 },
        { id: 7, materialCode: 'MAT007', materialName: '硅胶表带-黑色', materialType: 'component', unit: '条', unitPrice: 2.80, safetyStock: 2000 },
        { id: 8, materialCode: 'MAT008', materialName: '外壳ABS-白色', materialType: 'component', unit: '个', unitPrice: 4.20, safetyStock: 1000 },
        { id: 9, materialCode: 'MAT009', materialName: '充电座Type-C', materialType: 'component', unit: '个', unitPrice: 5.50, safetyStock: 500 },
        { id: 10, materialCode: 'MAT010', materialName: '智能运动手环A1', materialType: 'finished', unit: '台', unitPrice: 89.00, safetyStock: 100 },
        { id: 11, materialCode: 'MAT011', materialName: '电阻电容包', materialType: 'raw', unit: '包', unitPrice: 18.00, safetyStock: 100 },
        { id: 12, materialCode: 'MAT012', materialName: '防水胶圈', materialType: 'component', unit: '个', unitPrice: 0.50, safetyStock: 5000 }
      ]
      
      let filtered = mockData
      if (type) {
        filtered = mockData.filter(item => item.materialType === type)
      }
      
      if (this.keyword.trim()) {
        const key = this.keyword.toLowerCase()
        filtered = filtered.filter(item => 
          item.materialCode.toLowerCase().includes(key) || 
          item.materialName.toLowerCase().includes(key)
        )
      }
      
      return filtered.map(item => ({
        ...item,
        materialTypeName: this.getTypeName(item.materialType)
      }))
    },
    async loadMore() {
      this.page++
      const typeVal = this.typeList[this.currentType].value
      try {
        const res = await getMaterialList({
          page: this.page,
          size: this.size,
          keyword: this.keyword,
          materialType: typeVal || undefined
        })
        const records = (res && res.records) || res || []
        if (records.length === 0) {
          this.page--
          return
        }
        this.list = this.list.concat(records.map(item => ({
          ...item,
          materialTypeName: this.getTypeName(item.materialType)
        })))
      } catch {
        this.page--
      }
    },
    getTypeName(type) {
      const map = { raw: '原材料', component: '零部件', finished: '成品' }
      return map[type] || type || '未知'
    },
    showDetail(item) {
      uni.showModal({
        title: item.materialName,
        content: `编码: ${item.materialCode}\n类型: ${this.getTypeName(item.materialType)}\n单位: ${item.unit || '-'}\n单价: ¥${item.unitPrice || '0.00'}\n安全库存: ${item.safetyStock || 0}`,
        showCancel: false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.material-page {
  min-height: 100vh;
  background: #F5F7FA;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
}

.search-bar,
.filter-bar {
  padding: 20rpx 24rpx;
  background: #ffffff;
  border-radius: 28rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 10;
}

.filter-bar {
  margin-bottom: 16rpx;
}

.material-list {
  padding: 0 24rpx 24rpx;
}

.material-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 34rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.material-code {
  font-size: 26rpx;
  color: #3B7CFF;
  font-weight: 500;
}

.material-name {
  font-size: 30rpx;
  color: #4A4A4A;
  font-weight: 500;
  display: block;
  margin-bottom: 16rpx;
}

.card-info {
  display: flex;
  gap: 24rpx;
}

.info-item {
  font-size: 24rpx;
  color: #8C8C8C;
}
</style>