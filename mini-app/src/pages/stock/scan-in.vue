<template>
  <view class="scan-in-page">
    <view class="scan-area" @click="handleScan">
      <text class="scan-icon-text">扫</text>
      <text class="scan-text">点击扫描物料条码</text>
    </view>

    <view class="form-section" v-if="form.materialCode">
      <text class="section-title">入库信息</text>

      <u-form :model="form" ref="uFormRef" labelPosition="left" labelWidth="160rpx">
        <u-form-item label="物料编码">
          <u-input v-model="form.materialCode" disabled placeholder="扫码获取"></u-input>
        </u-form-item>
        <u-form-item label="物料名称">
          <u-input v-model="form.materialName" disabled placeholder="扫码获取"></u-input>
        </u-form-item>
        <u-form-item label="入库仓库" prop="warehouseId">
          <u-input v-model="warehouseName" placeholder="请选择仓库" readonly @click="showWarehousePicker = true">
            <template #suffix>
              <text class="suffix-arrow">›</text>
            </template>
          </u-input>
        </u-form-item>
        <u-form-item label="入库数量" prop="quantity">
          <u-input v-model="form.quantity" type="number" placeholder="请输入入库数量"></u-input>
        </u-form-item>
        <u-form-item label="来源类型" prop="sourceType">
          <u-input v-model="form.sourceType" placeholder="如 purchase/production"></u-input>
        </u-form-item>
        <u-form-item label="来源单号" prop="sourceNo">
          <u-input v-model="form.sourceNo" placeholder="请输入来源单号"></u-input>
        </u-form-item>
      </u-form>

      <u-button type="primary" :loading="loading" @click="handleSubmit" customStyle="margin-top: 40rpx; height: 88rpx; border-radius: 44rpx;">
        确认入库
      </u-button>
    </view>

    <u-picker :show="showWarehousePicker" :columns="warehouseColumns" keyName="warehouseName" @confirm="onWarehouseConfirm" @cancel="showWarehousePicker = false"></u-picker>
  </view>
</template>

<script>
import { scanCode } from '@/utils/scan'
import { scanStockIn, getWarehouseList } from '@/api/stock'

export default {
  data() {
    return {
      form: {
        materialCode: '',
        materialName: '',
        materialId: null,
        warehouseId: '',
        quantity: '',
        sourceType: '',
        sourceNo: ''
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
        this.warehouseColumns = [[]]
      }
    },
    async handleScan() {
      try {
        const code = await scanCode()
        const res = await scanStockIn({ code })
        this.form.materialCode = res.materialCode || code
        this.form.materialName = res.materialName || ''
        this.form.materialId = res.materialId || null
      } catch {
        this.form.materialCode = ''
      }
    },
    onWarehouseConfirm({ value }) {
      const item = value[0]
      if (item) {
        this.form.warehouseId = item.id
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

      this.loading = true
      try {
        await scanStockIn({
          materialId: this.form.materialId,
          materialCode: this.form.materialCode,
          warehouseId: this.form.warehouseId,
          quantity: Number(this.form.quantity),
          sourceType: this.form.sourceType,
          sourceNo: this.form.sourceNo
        })
        uni.showToast({ title: '入库成功', icon: 'success' })
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
.scan-in-page {
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
  color: #3B7CFF;
}

.scan-text {
  font-size: 28rpx;
  color: #3B7CFF;
  margin-top: 20rpx;
}

.suffix-arrow {
  font-size: 24rpx;
  color: #BFBFBF;
}

.form-section {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 34rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
  margin-bottom: 20rpx;
  display: block;
}

:deep(.u-button) {
  border-radius: 48rpx !important;
  box-shadow: 0 4rpx 12rpx rgba(59, 124, 255, 0.3);
}
</style>