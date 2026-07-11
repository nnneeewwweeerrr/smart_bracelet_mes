<template>
  <view class="material-page">
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
</template>

<script>
import { getMaterialList } from '@/api/stock'

export default {
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
  background: transparent;
}

.search-bar,
.filter-bar {
  padding: 20rpx 24rpx;
  background: #ffffff;
  border-radius: 28rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
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