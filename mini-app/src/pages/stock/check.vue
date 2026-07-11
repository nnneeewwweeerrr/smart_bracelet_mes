﻿<template>
  <view class="check-page">
    <view class="scan-area" @click="handleScan">
      <text class="scan-icon-text">扫</text>
      <text class="scan-text">点击扫描物料条码盘点</text>
    </view>

    <view class="form-section" v-if="form.materialCode">
      <text class="section-title">盘点信息</text>

      <u-form :model="form" labelPosition="left" labelWidth="160rpx">
        <u-form-item label="物料编码">
          <u-input v-model="form.materialCode" disabled></u-input>
        </u-form-item>
        <u-form-item label="物料名称">
          <u-input v-model="form.materialName" disabled></u-input>
        </u-form-item>
        <u-form-item label="仓库">
          <u-input v-model="form.warehouseName" disabled></u-input>
        </u-form-item>
        <u-form-item label="系统库存">
          <u-input v-model="form.systemQuantity" disabled></u-input>
        </u-form-item>
        <u-form-item label="实盘数量" prop="actualQuantity">
          <u-input v-model="form.actualQuantity" type="number" placeholder="请输入实际盘点数量"></u-input>
        </u-form-item>
      </u-form>

      <view class="diff-info" v-if="form.actualQuantity !== ''">
        <text class="diff-label">差异：</text>
        <text :class="diffClass">{{ diffText }}</text>
      </view>

      <u-button type="primary" :loading="loading" @click="handleSubmit" customStyle="margin-top: 40rpx; height: 88rpx; border-radius: 44rpx;">
        提交盘点
      </u-button>
    </view>

    <view class="history-section">
      <text class="section-title">盘点记录</text>
      <view v-if="checkList.length === 0">
        <u-empty mode="data" text="暂无盘点记录" iconSize="80"></u-empty>
      </view>
      <view class="check-card" v-for="item in checkList" :key="item.id">
        <view class="check-header">
          <text class="check-material">{{ item.materialCode }}</text>
          <u-tag :text="item.diff >= 0 ? '盘盈' : '盘亏'" :type="item.diff >= 0 ? 'success' : 'error'" size="mini"></u-tag>
        </view>
        <view class="check-info">
          <text class="check-item">系统: {{ item.systemQuantity }}</text>
          <text class="check-item">实盘: {{ item.actualQuantity }}</text>
          <text class="check-item">差异: {{ item.diff >= 0 ? '+' : '' }}{{ item.diff }}</text>
        </view>
        <text class="check-time">{{ item.checkTime }}</text>
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
        this.form.materialCode = code
        this.form.materialName = '物料' + code
        this.form.systemQuantity = '100'
        this.form.actualQuantity = ''
      } catch {
        // cancelled
      }
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
  background: transparent;
  padding: 24rpx;
}

.scan-area {
  background: #ffffff;
  border-radius: 28rpx;
  border: 2rpx dashed rgba(59, 124, 255, 0.35);
  padding: 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.scan-icon-text {
  font-size: 56rpx;
  font-weight: 600;
  color: #FF4D4F;
}

.scan-text {
  font-size: 28rpx;
  color: #3B7CFF;
  margin-top: 20rpx;
}

.form-section {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 34rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
  margin-bottom: 20rpx;
  display: block;
}

.diff-info {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
  margin-top: 20rpx;
}

.diff-label {
  font-size: 28rpx;
  color: #4A4A4A;
  margin-right: 12rpx;
}

.diff-positive { color: #52C41A; font-size: 32rpx; font-weight: 600; }
.diff-negative { color: #FF4D4F; font-size: 32rpx; font-weight: 600; }
.diff-zero { color: #8C8C8C; font-size: 32rpx; font-weight: 600; }

.history-section {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 30rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.check-card {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F2F4F8;

  &:last-child {
    border-bottom: none;
  }
}

.check-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.check-material {
  font-size: 28rpx;
  color: #4A4A4A;
  font-weight: 500;
}

.check-info {
  display: flex;
  gap: 24rpx;
  margin-bottom: 8rpx;
}

.check-item {
  font-size: 24rpx;
  color: #4A4A4A;
}

.check-time {
  font-size: 22rpx;
  color: #BFBFBF;
}

:deep(.u-button) {
  border-radius: 48rpx !important;
  box-shadow: 0 4rpx 12rpx rgba(59, 124, 255, 0.3);
}
</style>