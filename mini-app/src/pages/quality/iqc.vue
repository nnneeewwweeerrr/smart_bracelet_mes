﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <view class="iqc-page">
    <DynamicBackground theme="theme-success"></DynamicBackground>
    <view class="page-content">
      <view class="page-header">
        <view class="page-title-wrap">
          <text class="page-title">质检录入</text>
          <text class="page-desc">来料检验，保障产品质量</text>
        </view>
      </view>

      <view class="task-select">
        <view class="section-header">
          <text class="section-title">📋 选择质检任务</text>
        </view>
        <view class="task-input-wrap" @click="showTaskPicker = true">
          <text :class="{ 'placeholder': !taskNo }">{{ taskNo || '请选择或扫码获取任务编号' }}</text>
          <view class="task-input-suffix">
            <text class="suffix-scan" @click.stop="handleScanTask">扫</text>
            <text class="suffix-arrow">›</text>
          </view>
        </view>
      </view>

      <view class="check-form" v-if="form.taskId">
        <view class="section-header">
          <text class="section-title">🔍 检验项目</text>
          <text class="section-count">共 {{ form.results.length }} 项</text>
        </view>

        <view class="check-items">
          <view class="check-item" v-for="(item, idx) in form.results" :key="idx">
            <view class="check-item-header">
              <view class="check-item-index">
                <text class="index-num">{{ idx + 1 }}</text>
              </view>
              <view class="check-item-info">
                <text class="check-name">{{ item.checkItem }}</text>
              </view>
              <view class="check-radio-wrap">
                <u-radio-group v-model="item.result" placement="row">
                  <u-radio name="1" label="合格" activeColor="#52C41A" size="18"></u-radio>
                  <u-radio name="0" label="不合格" activeColor="#FF4D4F" size="18"></u-radio>
                </u-radio-group>
              </view>
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
        </view>

        <view class="add-check" @click="addCheckItem">
          <view class="add-icon-wrap">
            <text class="add-icon">+</text>
          </view>
          <text class="add-text">添加检验项</text>
        </view>

        <view class="submit-area">
          <u-button type="primary" :loading="loading" @click="handleSubmit" customStyle="height: 88rpx; border-radius: 44rpx; font-size: 32rpx;">
            <text class="btn-icon">📤</text>
            提交检验结果
          </u-button>
        </view>
      </view>

      <u-picker :show="showTaskPicker" :columns="taskColumns" keyName="taskNo" @confirm="confirmTaskPicker" @cancel="showTaskPicker = false"></u-picker>
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
        this.taskColumns = []
      }
    },
    async handleScanTask() {
      try {
        const code = await scanCode()
        const mockData = this.getMockTask(code)
        this.taskNo = mockData.taskNo
        this.form.taskId = mockData.taskId
        if (mockData.results) {
          this.form.results = mockData.results
        }
      } catch {
        // cancelled
      }
    },
    getMockTask(code) {
      const mockTasks = {
        'QCTASK20260715001': { taskId: 'QCTASK20260715001', taskNo: 'QCTASK20260715001', results: [
          { checkItem: '外观检查', standardValue: '无划痕、无变形', actualValue: '', result: '' },
          { checkItem: '尺寸测量', standardValue: '符合图纸公差', actualValue: '', result: '' },
          { checkItem: '功能测试', standardValue: '功能正常', actualValue: '', result: '' }
        ]},
        'QCTASK20260715002': { taskId: 'QCTASK20260715002', taskNo: 'QCTASK20260715002', results: [
          { checkItem: '焊点检查', standardValue: '无虚焊、连锡', actualValue: '', result: '' },
          { checkItem: '元件极性', standardValue: '极性正确', actualValue: '', result: '' },
          { checkItem: 'AOI检测', standardValue: '零缺陷', actualValue: '', result: '' }
        ]},
        'QCTASK20260715003': { taskId: 'QCTASK20260715003', taskNo: 'QCTASK20260715003', results: [
          { checkItem: '防水测试', standardValue: 'IP68', actualValue: '', result: '' },
          { checkItem: '电池容量', standardValue: '≥420mAh', actualValue: '', result: '' },
          { checkItem: '蓝牙连接', standardValue: '连接正常', actualValue: '', result: '' }
        ]}
      }
      return mockTasks[code] || { taskId: code, taskNo: code, results: [
        { checkItem: '外观检查', standardValue: '无划痕、无变形', actualValue: '', result: '' },
        { checkItem: '尺寸测量', standardValue: '符合图纸公差', actualValue: '', result: '' },
        { checkItem: '功能测试', standardValue: '功能正常', actualValue: '', result: '' }
      ]}
    },
    confirmTaskPicker({ value }) {
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

.task-select {
  margin: 24rpx;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  padding: 34rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.section-count {
  font-size: 24rpx;
  color: #8C8C8C;
}

.task-input-wrap {
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

.task-input-suffix {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.suffix-scan {
  font-size: 26rpx;
  color: #52C41A;
  font-weight: 600;
  padding: 8rpx 16rpx;
  background: rgba(82, 196, 26, 0.1);
  border-radius: 12rpx;
}

.suffix-arrow {
  font-size: 32rpx;
  color: #BFBFBF;
  font-weight: 300;
}

.check-form {
  margin: 0 24rpx;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  padding: 34rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.check-items {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.check-item {
  background: #FAFBFC;
  border-radius: 16rpx;
  padding: 20rpx;
}

.check-item-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.check-item-index {
  width: 44rpx;
  height: 44rpx;
  background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.index-num {
  font-size: 24rpx;
  font-weight: 600;
  color: #ffffff;
}

.check-item-info {
  flex: 1;
}

.check-name {
  font-size: 28rpx;
  color: #4A4A4A;
  font-weight: 600;
}

.check-radio-wrap {
  flex-shrink: 0;
}

.check-fields {
  padding-left: 60rpx;
}

.add-check {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 24rpx;
  border: 2rpx dashed #52C41A;
  border-radius: 16rpx;
  margin-top: 24rpx;
  transition: all 0.3s ease;

  &:active {
    background: rgba(82, 196, 26, 0.05);
  }
}

.add-icon-wrap {
  width: 48rpx;
  height: 48rpx;
  background: rgba(82, 196, 26, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-icon {
  font-size: 36rpx;
  color: #52C41A;
  font-weight: 500;
}

.add-text {
  font-size: 28rpx;
  color: #52C41A;
  font-weight: 500;
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