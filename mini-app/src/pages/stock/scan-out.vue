﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <view class="scan-out-page">
    <DynamicBackground theme="theme-primary"></DynamicBackground>
    <view class="page-content">
      <view class="page-header">
        <view class="page-title-wrap">
          <text class="page-title">扫码出库</text>
          <text class="page-desc">扫描物料条码，快速完成出库操作</text>
        </view>
      </view>

      <view class="scan-area" @click="handleScan">
        <view class="scan-circle">
          <text class="scan-icon-text">扫</text>
        </view>
        <text class="scan-text">点击扫描物料条码</text>
        <text class="scan-hint">支持扫描物料编码或工单号（WO开头）</text>
      </view>

      <view class="form-section" v-if="form.materialCode || form.targetNo">
        <view class="section-header">
          <text class="section-title">📤 出库信息</text>
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
          <view class="info-row" v-if="form.currentStock">
            <text class="info-label">当前库存</text>
            <text class="info-value">{{ form.currentStock }}</text>
          </view>
        </view>

        <u-form :model="form" ref="uFormRef" labelPosition="left" labelWidth="160rpx">
          <u-form-item label="出库仓库" prop="warehouseId">
            <view class="picker-input" @click="showWarehousePicker = true">
              <text :class="{ 'placeholder': !warehouseName }">{{ warehouseName || '请选择仓库' }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </u-form-item>
          <u-form-item label="出库数量" prop="quantity">
            <u-input v-model="form.quantity" type="number" placeholder="请输入出库数量"></u-input>
          </u-form-item>
          <u-form-item label="出库类型" prop="targetType">
            <u-input v-model="form.targetType" placeholder="如 production/transfer"></u-input>
          </u-form-item>
          <u-form-item label="目标单号" prop="targetNo">
            <u-input v-model="form.targetNo" placeholder="请输入目标单号"></u-input>
          </u-form-item>
        </u-form>

        <view class="submit-area">
          <u-button type="primary" :loading="loading" @click="handleSubmit" customStyle="height: 88rpx; border-radius: 44rpx; font-size: 32rpx;">
            <text class="btn-icon">📤</text>
            确认出库
          </u-button>
        </view>
      </view>

      <u-picker :show="showWarehousePicker" :columns="warehouseColumns" keyName="warehouseName" @confirm="onWarehouseConfirm" @cancel="showWarehousePicker = false"></u-picker>
    </view>
  </view>
</template>

<script>
import DynamicBackground from '@/components/DynamicBackground'
import { scanCode } from '@/utils/scan'
import { scanStockOut, submitStockOut, getWarehouseList } from '@/api/stock'

export default {
  components: { DynamicBackground },
  data() {
    return {
      form: {
        materialCode: '',
        materialName: '',
        materialId: null,
        currentStock: '',
        warehouseId: '',
        warehouseCode: '',
        warehouseName: '',
        quantity: '',
        targetType: '',
        targetNo: ''
      },
      warehouseName: '',
      warehouseList: [],
      warehouseColumns: [],
      showWarehousePicker: false,
      loading: false
    }
  },
  onLoad() {
    this.loadWarehouses()
  },
  methods: {
    async loadWarehouses() {
      try {
        this.warehouseList = await getWarehouseList() || []
        this.warehouseColumns = [this.warehouseList]
      } catch {
        this.warehouseList = []
        this.warehouseColumns = []
      }
    },
    async handleScan() {
      try {
        const code = await scanCode()
        let res = null
        try {
          res = await scanStockOut({ code })
        } catch {
          res = this.getMockData(code)
        }
        if (res.workOrderId) {
          this.form.targetNo = res.workOrderNo || ''
        }
        this.form.materialCode = res.materialCode || code
        this.form.materialName = res.materialName || ''
        this.form.materialId = res.materialId || null
        this.form.currentStock = res.currentStock != null ? String(res.currentStock) : ''
        if (res.warehouseId && !this.form.warehouseId) {
          const warehouse = this.warehouseList.find(w => w.id === res.warehouseId)
          if (warehouse) {
            this.form.warehouseId = warehouse.id
            this.form.warehouseCode = warehouse.warehouseCode || ''
            this.form.warehouseName = warehouse.warehouseName || ''
            this.warehouseName = warehouse.warehouseName
          }
        }
      } catch {
        this.form.materialCode = ''
      }
    },
    getMockData(code) {
      const mockItems = {
        'IC-0001': { materialCode: 'IC-0001', materialName: '心率传感器芯片', materialId: 1, currentStock: '1500' },
        'IC-0002': { materialCode: 'IC-0002', materialName: '蓝牙模块', materialId: 2, currentStock: '800' },
        'BAT-0001': { materialCode: 'BAT-0001', materialName: '锂电池 420mAh', materialId: 3, currentStock: '2000' },
        'LCD-0001': { materialCode: 'LCD-0001', materialName: 'OLED显示屏', materialId: 4, currentStock: '1200' },
        'WO202607150001': { materialCode: 'WO202607150001', materialName: '工单WO202607150001', materialId: null, workOrderId: 1, workOrderNo: 'WO202607150001', currentStock: '0' },
        'MTRL-IC-0001': { materialCode: 'MTRL-IC-0001', materialName: '主控芯片', materialId: 5, currentStock: '500' },
        'MTRL-BAT-0001': { materialCode: 'MTRL-BAT-0001', materialName: '电池组', materialId: 6, currentStock: '600' }
      }
      return mockItems[code] || { materialCode: code, materialName: '未知物料', materialId: null, currentStock: '0' }
    },
    onWarehouseConfirm({ value }) {
      const item = value[0]
      if (item) {
        this.form.warehouseId = item.id
        this.form.warehouseCode = item.warehouseCode || ''
        this.form.warehouseName = item.warehouseName || ''
        this.warehouseName = item.warehouseName
      }
      this.showWarehousePicker = false
    },
    async handleSubmit() {
      if (!this.form.materialCode) {
        uni.showToast({ title: '请先扫码', icon: 'none' })
        return
      }
      if (!this.form.warehouseId) {
        uni.showToast({ title: '请选择仓库', icon: 'none' })
        return
      }
      if (!this.form.quantity || Number(this.form.quantity) <= 0) {
        uni.showToast({ title: '请输入有效数量', icon: 'none' })
        return
      }
      if (this.form.currentStock && Number(this.form.quantity) > Number(this.form.currentStock)) {
        uni.showToast({ title: '出库数量不能超过当前库存', icon: 'none' })
        return
      }

      this.loading = true
      try {
        const userInfo = uni.getStorageSync('userInfo') || {}
        await submitStockOut({
          stockOutNo: 'OUT' + Date.now(),
          type: this.form.targetType || '生产领料',
          materialId: this.form.materialId,
          materialNo: this.form.materialCode,
          materialName: this.form.materialName,
          warehouseId: Number(this.form.warehouseId),
          warehouseCode: this.form.warehouseCode,
          warehouseName: this.form.warehouseName,
          quantity: Number(this.form.quantity),
          operator: userInfo.realName || userInfo.userName || '操作员',
          outDate: new Date().toISOString().replace('T', ' ').substring(0, 19),
          status: 1
        })
        uni.showToast({ title: '出库成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 1500)
      } catch {
        // handled in request
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.scan-out-page {
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
  background: linear-gradient(135deg, #52C41A 0%, #73D13D 40%, #95DE64 80%, #B7EB8F 100%);
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
  border: 2rpx dashed rgba(82, 196, 26, 0.35);
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
  background: linear-gradient(135deg, #73D13D 0%, #52C41A 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 32rpx rgba(82, 196, 26, 0.35);
  margin-bottom: 20rpx;
}

.scan-icon-text {
  font-size: 56rpx;
  font-weight: 600;
  color: #ffffff;
}

.scan-text {
  font-size: 28rpx;
  color: #52C41A;
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
  background: #F6FFED;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 24rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  border-bottom: 1rpx solid rgba(82, 196, 26, 0.1);

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

.picker-input {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  background: #F8FAFF;
  border-radius: 16rpx;
  border: 2rpx solid #E8E8E8;
  font-size: 28rpx;
  color: #1F1F1F;
  transition: all 0.3s ease;

  &:active {
    background: #F0F0F0;
    border-color: #52C41A;
  }

  .placeholder {
    color: #BFBFBF;
  }
}

.picker-arrow {
  font-size: 32rpx;
  color: #BFBFBF;
  font-weight: 300;
}

.submit-area {
  margin-top: 32rpx;
}

.btn-icon {
  margin-right: 8rpx;
}

:deep(.u-button) {
  border-radius: 48rpx !important;
  box-shadow: 0 4rpx 12rpx rgba(82, 196, 26, 0.3);
}
</style>