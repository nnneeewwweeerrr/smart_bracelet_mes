﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <view class="check-page">
    <DynamicBackground theme="theme-primary"></DynamicBackground>
    <view class="page-content">
      <view class="page-header">
        <view class="page-title-wrap">
          <text class="page-title">库存盘点</text>
          <text class="page-desc">扫描物料条码，快速完成库存盘点</text>
        </view>
      </view>

      <view class="scan-area" @click="handleScan">
        <view class="scan-circle">
          <text class="scan-icon-text">扫</text>
        </view>
        <text class="scan-text">点击扫描物料条码盘点</text>
        <text class="scan-hint">支持物料编码和SN码扫描</text>
      </view>

      <view class="form-section" v-if="form.materialCode">
        <view class="section-header">
          <text class="section-title">📊 盘点信息</text>
        </view>

        <view class="info-card">
          <view class="info-row" v-if="form.materialCode">
            <text class="info-label">物料编码</text>
            <text class="info-value font-mono">{{ form.materialCode }}</text>
          </view>
          <view class="info-row" v-if="form.materialName">
            <text class="info-label">物料名称</text>
            <text class="info-value">{{ form.materialName }}</text>
          </view>
          <view class="info-row" v-if="form.warehouseName">
            <text class="info-label">仓库</text>
            <text class="info-value">{{ form.warehouseName }}</text>
          </view>
          <view class="info-row" v-if="form.systemQuantity">
            <text class="info-label">系统库存</text>
            <text class="info-value">{{ form.systemQuantity }}</text>
          </view>
        </view>

        <u-form :model="form" labelPosition="left" labelWidth="160rpx">
          <u-form-item label="实盘数量" prop="actualQuantity">
            <u-input v-model="form.actualQuantity" type="number" placeholder="请输入实际盘点数量"></u-input>
          </u-form-item>
        </u-form>

        <view class="diff-info" v-if="form.actualQuantity !== ''">
          <view class="diff-label-wrap">
            <text class="diff-label">差异：</text>
          </view>
          <view class="diff-value-wrap" :class="diffClass">
            <text class="diff-value">{{ diffText }}</text>
          </view>
        </view>

        <view class="submit-area">
          <u-button type="primary" :loading="loading" @click="handleSubmit" customStyle="height: 88rpx; border-radius: 44rpx; font-size: 32rpx;">
            <text class="btn-icon">📤</text>
            提交盘点
          </u-button>
        </view>
      </view>

      <view class="history-section">
        <view class="section-header">
          <text class="section-title">📝 盘点记录</text>
        </view>
        <view v-if="checkList.length === 0">
          <u-empty mode="data" text="暂无盘点记录" iconSize="80"></u-empty>
        </view>
        <view class="check-list">
          <view class="check-card" v-for="item in checkList" :key="item.id">
            <view class="check-header">
              <text class="check-material">{{ item.materialCode }}</text>
              <u-tag :text="item.diff >= 0 ? '盘盈' : '盘亏'" :type="item.diff >= 0 ? 'success' : 'error'" size="mini"></u-tag>
            </view>
            <view class="check-info">
              <view class="check-info-row">
                <text class="check-item-label">系统</text>
                <text class="check-item-value">{{ item.systemQuantity }}</text>
              </view>
              <view class="check-info-row">
                <text class="check-item-label">实盘</text>
                <text class="check-item-value">{{ item.actualQuantity }}</text>
              </view>
              <view class="check-info-row">
                <text class="check-item-label">差异</text>
                <text :class="['check-item-value', item.diff >= 0 ? 'diff-positive' : 'diff-negative']">{{ item.diff >= 0 ? '+' : '' }}{{ item.diff }}</text>
              </view>
            </view>
            <text class="check-time">{{ item.checkTime }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { scanCode } from '@/utils/scan'
import { stockCheck } from '@/api/stock'

export default {
  data() {
    return {
      form: {
        materialCode: '',
        materialName: '',
        materialId: null,
        warehouseId: null,
        warehouseName: '',
        systemQuantity: '',
        actualQuantity: ''
      },
      loading: false,
      checkList: []
    }
  },
  computed: {
    diff() {
      if (this.form.actualQuantity === '') return 0
      return Number(this.form.actualQuantity) - Number(this.form.systemQuantity || 0)
    },
    diffText() {
      const d = this.diff
      return d >= 0 ? '+' + d : String(d)
    },
    diffClass() {
      return this.diff > 0 ? 'diff-positive' : this.diff < 0 ? 'diff-negative' : 'diff-zero'
    }
  },
  methods: {
    async handleScan() {
      try {
        const code = await scanCode()
        let res = null
        try {
          const { get } = await import('@/utils/request')
          res = await get('/scan/decode', { code })
        } catch {
          res = this.getMockData(code)
        }
        
        if (res.type === 'material') {
          this.form.materialCode = res.materialCode || code
          this.form.materialName = res.materialName || '物料' + code
          this.form.materialId = null
          
          if (res.stocks && res.stocks.length > 0) {
            const firstStock = res.stocks[0]
            this.form.warehouseId = firstStock.warehouseId
            this.form.warehouseName = firstStock.warehouseName || '仓库' + firstStock.warehouseId
            this.form.systemQuantity = String(firstStock.quantity || 0)
          } else {
            this.form.warehouseId = null
            this.form.warehouseName = ''
            this.form.systemQuantity = '0'
          }
        } else if (res.type === 'sn_code') {
          this.form.materialCode = res.snCode || code
          this.form.materialName = res.productName || '产品' + code
          this.form.materialId = res.productId || null
          this.form.warehouseName = ''
          this.form.systemQuantity = '1'
        } else {
          this.form.materialCode = code
          this.form.materialName = '物料' + code
          this.form.systemQuantity = '0'
          this.form.warehouseName = ''
        }
        this.form.actualQuantity = ''
      } catch {
        this.form.materialCode = ''
      }
    },
    getMockData(code) {
      const mockItems = {
        'IC-0001': { type: 'material', materialCode: 'IC-0001', materialName: '心率传感器芯片', stocks: [{ warehouseId: 1, warehouseName: '电子物料仓', quantity: 1500 }] },
        'IC-0002': { type: 'material', materialCode: 'IC-0002', materialName: '蓝牙模块', stocks: [{ warehouseId: 1, warehouseName: '电子物料仓', quantity: 800 }] },
        'BAT-0001': { type: 'material', materialCode: 'BAT-0001', materialName: '锂电池 420mAh', stocks: [{ warehouseId: 2, warehouseName: '电池仓', quantity: 2000 }] },
        'LCD-0001': { type: 'material', materialCode: 'LCD-0001', materialName: 'OLED显示屏', stocks: [{ warehouseId: 1, warehouseName: '电子物料仓', quantity: 1200 }] },
        'SN202607150001': { type: 'sn_code', snCode: 'SN202607150001', productName: '智能手环成品' },
        'SN202607150002': { type: 'sn_code', snCode: 'SN202607150002', productName: '智能手环成品' },
        'MTRL-IC-0001': { type: 'material', materialCode: 'MTRL-IC-0001', materialName: '主控芯片', stocks: [{ warehouseId: 1, warehouseName: '电子物料仓', quantity: 500 }] },
        'MTRL-BAT-0001': { type: 'material', materialCode: 'MTRL-BAT-0001', materialName: '电池组', stocks: [{ warehouseId: 2, warehouseName: '电池仓', quantity: 600 }] }
      }
      return mockItems[code] || { type: 'material', materialCode: code, materialName: '未知物料', stocks: [] }
    },
    async handleSubmit() {
      if (!this.form.materialCode) {
        uni.showToast({ title: '请先扫码', icon: 'none' })
        return
      }
      if (this.form.actualQuantity === '' || Number(this.form.actualQuantity) < 0) {
        uni.showToast({ title: '请输入有效数量', icon: 'none' })
        return
      }

      this.loading = true
      try {
        await stockCheck({
          materialId: this.form.materialId,
          materialCode: this.form.materialCode,
          warehouseId: this.form.warehouseId,
          systemQuantity: Number(this.form.systemQuantity || 0),
          actualQuantity: Number(this.form.actualQuantity)
        })
        uni.showToast({ title: '盘点提交成功', icon: 'success' })
        this.checkList.unshift({
          id: Date.now(),
          materialCode: this.form.materialCode,
          systemQuantity: this.form.systemQuantity,
          actualQuantity: this.form.actualQuantity,
          diff: this.diff,
          checkTime: this.formatTime(new Date())
        })
        this.form.materialCode = ''
        this.form.actualQuantity = ''
      } catch {
        // handled
      } finally {
        this.loading = false
      }
    },
    formatTime(date) {
      const pad = n => String(n).padStart(2, '0')
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
    }
  }
}
</script>

<style lang="scss" scoped>
.check-page {
  min-height: 100vh;
  background: #F5F7FA;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
}

.page-header {
  padding: 40rpx 32rpx 32rpx;
  background: linear-gradient(135deg, #FAAD14 0%, #FFC53D 40%, #FFD666 80%, #FFE58F 100%);
  border-radius: 0 0 48rpx 48rpx;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -120rpx;
    right: -80rpx;
    width: 300rpx;
    height: 300rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
  }
}

.page-title-wrap {
  position: relative;
  z-index: 1;
}

.page-title {
  font-size: 44rpx;
  font-weight: 700;
  color: #ffffff;
  display: block;
  margin-bottom: 8rpx;
}

.page-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  display: block;
}

.scan-area {
  margin: 24rpx;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  border: 2rpx dashed rgba(250, 173, 20, 0.35);
  padding: 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  }
}

.scan-circle {
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #FFC53D 0%, #FAAD14 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 32rpx rgba(250, 173, 20, 0.35);
  margin-bottom: 20rpx;
}

.scan-icon-text {
  font-size: 56rpx;
  font-weight: 600;
  color: #ffffff;
}

.scan-text {
  font-size: 28rpx;
  color: #FAAD14;
  margin-top: 8rpx;
  font-weight: 500;
}

.scan-hint {
  font-size: 22rpx;
  color: #8C8C8C;
  margin-top: 10rpx;
}

.form-section {
  margin: 0 24rpx;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  padding: 34rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
  margin-bottom: 24rpx;
}

.section-header {
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
}

.info-card {
  background: #FFFBE6;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 24rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  border-bottom: 1rpx solid rgba(250, 173, 20, 0.1);

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: 26rpx;
  color: #8C8C8C;
}

.info-value {
  font-size: 26rpx;
  color: #1F1F1F;
  font-weight: 500;

  &.font-mono {
    font-family: monospace;
  }
}

.diff-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  background: #F5F7FA;
  border-radius: 16rpx;
  margin-top: 20rpx;
}

.diff-label-wrap {
  flex: 1;
}

.diff-label {
  font-size: 28rpx;
  color: #4A4A4A;
  font-weight: 500;
}

.diff-value-wrap {
  padding: 12rpx 24rpx;
  border-radius: 20rpx;

  &.diff-positive { background: rgba(82, 196, 26, 0.15); }
  &.diff-negative { background: rgba(255, 77, 79, 0.15); }
  &.diff-zero { background: rgba(191, 191, 191, 0.15); }
}

.diff-value {
  font-size: 32rpx;
  font-weight: 700;

  .diff-positive & { color: #52C41A; }
  .diff-negative & { color: #FF4D4F; }
  .diff-zero & { color: #8C8C8C; }
}

.submit-area {
  margin-top: 32rpx;
}

.btn-icon {
  margin-right: 8rpx;
}

.history-section {
  margin: 0 24rpx;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  padding: 34rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.check-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.check-card {
  background: #FAFBFC;
  border-radius: 16rpx;
  padding: 20rpx;
}

.check-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.check-material {
  font-size: 28rpx;
  color: #4A4A4A;
  font-weight: 600;
}

.check-info {
  display: flex;
  gap: 24rpx;
  margin-bottom: 10rpx;
}

.check-info-row {
  flex: 1;
  text-align: center;
}

.check-item-label {
  font-size: 22rpx;
  color: #8C8C8C;
  display: block;
  margin-bottom: 4rpx;
}

.check-item-value {
  font-size: 24rpx;
  color: #4A4A4A;
  font-weight: 500;

  &.diff-positive { color: #52C41A; }
  &.diff-negative { color: #FF4D4F; }
}

.check-time {
  font-size: 22rpx;
  color: #BFBFBF;
  display: block;
  text-align: center;
}

:deep(.u-button) {
  border-radius: 48rpx !important;
  box-shadow: 0 4rpx 12rpx rgba(250, 173, 20, 0.3);
}
</style>