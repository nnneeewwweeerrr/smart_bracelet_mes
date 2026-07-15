﻿﻿<template>
  <view class="iqc-page">
    <DynamicBackground theme="theme-success"></DynamicBackground>
    <view class="page-content">
    <view class="task-select">
      <text class="section-title">选择质检任务</text>
      <u-input v-model="taskNo" placeholder="请输入或扫码获取任务编号" @click="showTaskPicker = true" readonly>
        <template #suffix>
          <text class="suffix-scan" @click="handleScanTask">扫</text>
        </template>
      </u-input>
    </view>

    <view class="check-form" v-if="form.taskId">
      <text class="section-title">检验项目</text>

      <view class="check-item" v-for="(item, idx) in form.results" :key="idx">
        <view class="check-header">
          <text class="check-name">{{ item.checkItem }}</text>
          <u-radio-group v-model="item.result" placement="row">
            <u-radio name="1" label="合格" activeColor="#52C41A" size="18"></u-radio>
            <u-radio name="0" label="不合格" activeColor="#FF4D4F" size="18"></u-radio>
          </u-radio-group>
        </view>
        <view class="check-fields">
          <u-form labelPosition="left" labelWidth="140rpx">
            <u-form-item label="标准值">
              <u-input v-model="item.standardValue" placeholder="标准值"></u-input>
            </u-form-item>
            <u-form-item label="实际值">
              <u-input v-model="item.actualValue" placeholder="实际值"></u-input>
            </u-form-item>
          </u-form>
        </view>
      </view>

      <view class="add-check" @click="addCheckItem">
        <text class="add-icon">+</text>
        <text class="add-text">添加检验项</text>
      </view>

      <u-button type="primary" :loading="loading" @click="handleSubmit" customStyle="margin-top: 40rpx; height: 88rpx; border-radius: 48rpx;">
        提交检验结果
      </u-button>
    </view>

    <u-picker :show="showTaskPicker" :columns="taskColumns" keyName="taskNo" @confirm="onTaskConfirm" @cancel="showTaskPicker = false"></u-picker>
    </view>
  </view>
</template>

<script>
import DynamicBackground from '@/components/DynamicBackground'
import { scanCode } from '@/utils/scan'
import { getQualityTaskList, submitQualityResult } from '@/api/quality'

export default {
  components: { DynamicBackground },
  data() {
    return {
      taskNo: '',
      taskList: [],
      taskColumns: [],
      showTaskPicker: false,
      form: {
        taskId: '',
        results: [
          { checkItem: '外观检查', standardValue: '无划痕、无变形', actualValue: '', result: '' },
          { checkItem: '尺寸测量', standardValue: '符合图纸公差', actualValue: '', result: '' },
          { checkItem: '功能测试', standardValue: '功能正常', actualValue: '', result: '' }
        ]
      },
      loading: false
    }
  },
  onLoad() {
    this.loadTasks()
  },
  methods: {
    async loadTasks() {
      try {
        const res = await getQualityTaskList({ taskType: 'incoming', status: 0, page: 1, size: 100 })
        this.taskList = (res && res.records) || res || []
        this.taskColumns = [this.taskList]
      } catch {
        this.taskList = []
        this.taskColumns = [[]]
      }
    },
    async handleScanTask() {
      try {
        const code = await scanCode()
        this.taskNo = code
        this.form.taskId = code
      } catch {
        // cancelled
      }
    },
    onTaskConfirm({ value }) {
      const item = value[0]
      if (item) {
        this.form.taskId = item.id
        this.taskNo = item.taskNo
      }
      this.showTaskPicker = false
    },
    addCheckItem() {
      this.form.results.push({
        checkItem: '',
        standardValue: '',
        actualValue: '',
        result: ''
      })
    },
    async handleSubmit() {
      if (!this.form.taskId) {
        uni.showToast({ title: '请选择质检任务', icon: 'none' })
        return
      }
      const incomplete = this.form.results.find(r => !r.result && r.result !== '0')
      if (incomplete) {
        uni.showToast({ title: '请完成所有检验项判定', icon: 'none' })
        return
      }

      this.loading = true
      try {
        await submitQualityResult({
          taskId: this.form.taskId,
          results: this.form.results.map(r => ({
            checkItem: r.checkItem,
            standardValue: r.standardValue,
            actualValue: r.actualValue,
            result: Number(r.result)
          }))
        })
        uni.showToast({ title: '提交成功', icon: 'success' })
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
.iqc-page {
  min-height: 100vh;
  background: #F5F7FA;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
  padding: 24rpx;
}

.task-select {
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

.check-form {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 34rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
}

.check-item {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F0F0F0;

  &:last-child {
    border-bottom: none;
  }
}

.check-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.check-name {
  font-size: 28rpx;
  color: #4A4A4A;
  font-weight: 500;
}

.check-fields {
  padding-left: 10rpx;
}

.add-check {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx;
  border: 1rpx dashed #dcdfe6;
  border-radius: 12rpx;
  margin-top: 20rpx;
}

.add-text {
  font-size: 28rpx;
  color: #3B7CFF;
  margin-left: 8rpx;
}
</style>