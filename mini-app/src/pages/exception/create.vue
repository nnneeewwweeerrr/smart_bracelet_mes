<template>
  <view class="create-page">
    <view class="form-section">
      <text class="section-title">异常上报</text>

      <u-form :model="form" labelPosition="left" labelWidth="160rpx">
        <u-form-item label="异常类型" prop="exceptionType">
          <u-input v-model="exceptionTypeName" placeholder="请选择异常类型" readonly @click="showTypePicker = true">
            <template #suffix>
              <text class="suffix-arrow">›</text>
            </template>
          </u-input>
        </u-form-item>
        <u-form-item label="关联工单" prop="workOrderId">
          <u-input v-model="form.workOrderNo" placeholder="扫码或输入工单号">
            <template #suffix>
              <text class="suffix-scan" @click="handleScanOrder">扫</text>
            </template>
          </u-input>
        </u-form-item>
        <u-form-item label="异常描述" prop="description">
          <u-textarea v-model="form.description" placeholder="请详细描述异常情况" count maxlength="500"></u-textarea>
        </u-form-item>
      </u-form>

      <u-button type="error" :loading="loading" @click="handleSubmit" customStyle="margin-top: 40rpx; height: 88rpx; border-radius: 44rpx;">
        提交上报
      </u-button>
    </view>

    <u-picker :show="showTypePicker" :columns="typeColumns" keyName="label" @confirm="onTypeConfirm" @cancel="showTypePicker = false"></u-picker>
  </view>
</template>

<script>
import { scanCode } from '@/utils/scan'
import { createException } from '@/api/exception'
import { getUser } from '@/utils/auth'

export default {
  data() {
    return {
      form: {
        exceptionType: '',
        workOrderId: '',
        workOrderNo: '',
        description: ''
      },
      exceptionTypeName: '',
      typeList: [
        { label: '设备故障', value: 'equipment' },
        { label: '物料短缺', value: 'material' },
        { label: '质量问题', value: 'quality' },
        { label: '其他异常', value: 'other' }
      ],
      typeColumns: [],
      showTypePicker: false,
      loading: false
    }
  },
  onLoad() {
    this.typeColumns = [this.typeList]
  },
  methods: {
    async handleScanOrder() {
      try {
        const code = await scanCode()
        this.form.workOrderNo = code
      } catch {
        // cancelled
      }
    },
    onTypeConfirm({ value }) {
      const item = value[0]
      if (item) {
        this.form.exceptionType = item.value
        this.exceptionTypeName = item.label
      }
      this.showTypePicker = false
    },
    async handleSubmit() {
      if (!this.form.exceptionType) {
        uni.showToast({ title: '请选择异常类型', icon: 'none' })
        return
      }
      if (!this.form.description) {
        uni.showToast({ title: '请填写异常描述', icon: 'none' })
        return
      }

      const user = getUser() || {}
      this.loading = true
      try {
        await createException({
          exceptionType: this.form.exceptionType,
          workOrderId: this.form.workOrderId || undefined,
          description: this.form.description,
          reporterId: user.id
        })
        uni.showToast({ title: '上报成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 1500)
      } catch {
        // handled
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.create-page {
  min-height: 100vh;
  background: transparent;
  padding: 24rpx;
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

.suffix-arrow {
  font-size: 24rpx;
  color: #BFBFBF;
}

.suffix-scan {
  font-size: 24rpx;
  color: #3B7CFF;
  font-weight: 600;
}
</style>