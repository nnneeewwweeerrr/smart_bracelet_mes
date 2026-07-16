<template>
  <view class="test-input-page">
    <DynamicBackground theme="theme-success" />
    <view class="page-content">
      <view class="page-header">
        <view class="page-title-wrap">
          <text class="page-title">功能测试录入</text>
          <text class="page-desc">质检员现场录入测试结果</text>
        </view>
      </view>

      <view class="search-section">
        <view class="search-bar">
          <u-search v-model="searchKey" placeholder="扫描SN码或输入" @search="doSearch" @custom="doSearch" @clear="resetForm" shape="round"></u-search>
        </view>
        <view class="scan-btn" @click="handleScan">
          <view class="scan-icon-wrap">
            <text class="scan-icon">扫</text>
          </view>
          <text class="scan-text">扫SN码</text>
        </view>
      </view>

      <view class="test-form" v-if="currentSn">
        <view class="sn-card">
          <view class="sn-header">
            <text class="sn-label">产品SN码</text>
            <view class="sn-status" :class="testResultClass">
              <text class="sn-status-text">{{ testResultText }}</text>
            </view>
          </view>
          <text class="sn-value">{{ currentSn.snCode }}</text>
          <view class="sn-info">
            <text class="sn-product">{{ currentSn.productModel }}</text>
            <text class="sn-workorder">{{ currentSn.workOrderNo }}</text>
          </view>
        </view>

        <view class="test-section">
          <view class="section-header">
            <text class="section-title">功能测试项</text>
            <text class="section-count">共 {{ testItems.length }} 项</text>
          </view>

          <view class="test-items">
            <view 
              class="test-item" 
              v-for="(item, idx) in testItems" 
              :key="idx"
              @click="toggleTestItem(item)"
            >
              <view class="test-item-header">
                <view class="test-icon-wrap" :class="item.iconClass">
                  <text class="test-icon">{{ item.icon }}</text>
                </view>
                <view class="test-info">
                  <text class="test-name">{{ item.name }}</text>
                  <text class="test-desc">{{ item.description }}</text>
                </view>
                <view class="test-status-btn" :class="getStatusClass(item)">
                  <text class="test-status-text">{{ getStatusText(item) }}</text>
                </view>
              </view>
              <view class="test-result-options" v-if="showOptions[item.key] || item.pass === false">
                <view class="option-row" v-if="showOptions[item.key]">
                  <view class="option-item pass" @click.stop="setResult(item, true)">
                    <view class="option-icon-wrap pass">
                      <text class="option-icon">✓</text>
                    </view>
                    <text class="option-text">合格</text>
                  </view>
                  <view class="option-item fail" @click.stop="setResult(item, false)">
                    <view class="option-icon-wrap fail">
                      <text class="option-icon">✕</text>
                    </view>
                    <text class="option-text">不合格</text>
                  </view>
                </view>
                <view class="remark-input" v-if="item.pass === false">
                  <u-textarea v-model="item.remark" placeholder="请输入不合格原因"></u-textarea>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="summary-section">
          <view class="summary-row">
            <text class="summary-label">测试结果</text>
            <view class="summary-badge" :class="testResultClass">
              <text class="summary-value">{{ testResultText }}</text>
            </view>
          </view>
          <view class="summary-row">
            <text class="summary-label">合格数</text>
            <text class="summary-value pass">{{ passCount }}</text>
          </view>
          <view class="summary-row">
            <text class="summary-label">不合格数</text>
            <text class="summary-value fail">{{ failCount }}</text>
          </view>
          <view class="summary-row">
            <text class="summary-label">测试人</text>
            <text class="summary-value">{{ testPerson }}</text>
          </view>
          <view class="summary-row">
            <text class="summary-label">测试时间</text>
            <text class="summary-value">{{ testTime }}</text>
          </view>
        </view>

        <view class="action-bar">
          <u-button type="warning" @click="resetForm" customStyle="flex:1; margin-right: 20rpx;">重置</u-button>
          <u-button type="primary" :loading="submitting" :disabled="!canSubmit" @click="submitTest">
            提交测试结果
          </u-button>
        </view>
      </view>

      <view v-if="!currentSn" style="padding-top: 120rpx;">
        <view class="empty-state">
          <view class="empty-icon">🔍</view>
          <text class="empty-title">功能测试录入</text>
          <text class="empty-desc">扫描SN码，开始测试</text>
          <u-button type="primary" size="large" @click="handleScan" customStyle="margin-top: 40rpx;">
            <text class="btn-icon">📷</text>
            扫描SN码
          </u-button>
          <text class="empty-hint">心率 · 蓝牙 · 防水 · 电池等8项测试</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { scanCode } from '@/utils/scan'
import { get, post } from '@/utils/request'
import { getUser } from '@/utils/auth'
import DynamicBackground from '@/components/DynamicBackground'

export default {
  components: {
    DynamicBackground
  },
  data() {
    return {
      searchKey: '',
      currentSn: null,
      testPerson: (getUser() && getUser().realName) || '',
      testTime: '',
      submitting: false,
      showOptions: {},
      testItems: [
        {
          key: 'heartRate',
          name: '心率传感器',
          description: '灵敏度与准确性',
          icon: '❤️',
          iconClass: 'heart-icon',
          pass: null,
          value: '',
          remark: ''
        },
        {
          key: 'bluetooth',
          name: '蓝牙连接',
          description: '连接稳定性',
          icon: '🔵',
          iconClass: 'blue-icon',
          pass: null,
          value: '',
          remark: ''
        },
        {
          key: 'battery',
          name: '电池容量',
          description: '实际容量达标',
          icon: '🔋',
          iconClass: 'battery-icon',
          pass: null,
          value: '',
          remark: ''
        },
        {
          key: 'waterproof',
          name: '防水测试',
          description: 'IP68等级验证',
          icon: '💧',
          iconClass: 'water-icon',
          pass: null,
          value: '',
          remark: ''
        },
        {
          key: 'screen',
          name: '屏幕显示',
          description: '亮度与触控',
          icon: '🖥️',
          iconClass: 'screen-icon',
          pass: null,
          value: '',
          remark: ''
        },
        {
          key: 'charging',
          name: '充电功能',
          description: '电流与速度',
          icon: '🔌',
          iconClass: 'charge-icon',
          pass: null,
          value: '',
          remark: ''
        },
        {
          key: 'accelerometer',
          name: '加速度计',
          description: '运动传感器',
          icon: '🏃',
          iconClass: 'move-icon',
          pass: null,
          value: '',
          remark: ''
        },
        {
          key: 'gps',
          name: 'GPS定位',
          description: '定位精度',
          icon: '📍',
          iconClass: 'gps-icon',
          pass: null,
          value: '',
          remark: ''
        }
      ]
    }
  },
  computed: {
    passCount() {
      return this.testItems.filter(item => item.pass === true).length
    },
    failCount() {
      return this.testItems.filter(item => item.pass === false).length
    },
    completedCount() {
      return this.testItems.filter(item => item.pass !== null).length
    },
    testResultClass() {
      if (this.completedCount === 0) return 'status-default'
      if (this.failCount === 0) return 'status-pass'
      return 'status-fail'
    },
    testResultText() {
      if (this.completedCount === 0) return '未开始'
      if (this.completedCount < this.testItems.length) return `${this.completedCount}/${this.testItems.length}`
      if (this.failCount === 0) return '全部合格'
      return `${this.failCount}项不合格`
    },
    canSubmit() {
      return this.completedCount === this.testItems.length && this.currentSn
    }
  },
  methods: {
    async handleScan() {
      try {
        const code = await scanCode()
        this.searchKey = code
        this.doSearch()
      } catch {
      }
    },
    async doSearch() {
      if (!this.searchKey.trim()) {
        this.currentSn = null
        return
      }

      uni.showLoading({ title: '加载产品信息...' })
      try {
        const res = await get('/mini/scan/decode', { code: this.searchKey.trim() })
        const data = (res && res.data) || res
        this.currentSn = {
          snCode: (data && (data.snCode || data.code)) || this.searchKey.trim(),
          productModel: (data && (data.productModel || data.materialName)) || '',
          workOrderNo: (data && data.workOrderNo) || '',
          batchNo: (data && data.batchNo) || ''
        }
        this.testTime = new Date().toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
        this.resetTestItems()
      } catch (e) {
        console.error('查询产品信息失败:', e)
        const mockData = this.getMockSnData(this.searchKey.trim())
        this.currentSn = {
          snCode: mockData.snCode,
          productModel: mockData.productModel,
          workOrderNo: mockData.workOrderNo,
          batchNo: mockData.batchNo
        }
        this.testTime = new Date().toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
        this.resetTestItems()
      } finally {
        uni.hideLoading()
      }
    },
    resetTestItems() {
      this.testItems.forEach(item => {
        item.pass = null
        item.value = ''
        item.remark = ''
      })
      this.showOptions = {}
    },
    resetForm() {
      this.searchKey = ''
      this.currentSn = null
      this.resetTestItems()
    },
    toggleTestItem(item) {
      if (this.showOptions[item.key]) {
        delete this.showOptions[item.key]
      } else {
        Object.keys(this.showOptions).forEach(key => {
          delete this.showOptions[key]
        })
        this.showOptions[item.key] = true
      }
    },
    setResult(item, pass) {
      item.pass = pass
      item.value = pass ? '测试通过' : '测试失败'
      if (!pass) {
        this.showOptions[item.key] = true
      } else {
        delete this.showOptions[item.key]
        item.remark = ''
      }
    },
    getStatusClass(item) {
      if (item.pass === true) return 'status-pass'
      if (item.pass === false) return 'status-fail'
      return 'status-default'
    },
    getStatusText(item) {
      if (item.pass === true) return '✓ 合格'
      if (item.pass === false) return '✕ 不合格'
      return '点击选择'
    },
    getMockSnData(code) {
      const mockItems = {
        'SN202607150001': { snCode: 'SN202607150001', productModel: '智能手环 X1 Pro', workOrderNo: 'WO202607150001', batchNo: 'BATCH2026071501' },
        'SN202607150002': { snCode: 'SN202607150002', productModel: '智能手环 X1 Pro', workOrderNo: 'WO202607150001', batchNo: 'BATCH2026071501' },
        'SN202607150003': { snCode: 'SN202607150003', productModel: '智能手环 X1 Pro', workOrderNo: 'WO202607150002', batchNo: 'BATCH2026071502' },
        'SN202607150004': { snCode: 'SN202607150004', productModel: '智能手环 X1', workOrderNo: 'WO202607150003', batchNo: 'BATCH2026071503' },
        'SN202607150005': { snCode: 'SN202607150005', productModel: '智能手环 X1', workOrderNo: 'WO202607150003', batchNo: 'BATCH2026071503' }
      }
      return mockItems[code] || { snCode: code, productModel: '智能手环', workOrderNo: '', batchNo: '' }
    },
    async submitTest() {
      this.submitting = true
      try {
        const requests = this.testItems.map(item =>
          post('/quality-result/save', {
            checkItem: item.name,
            standardValue: item.description,
            actualValue: item.value,
            result: item.pass ? 1 : 0,
            remark: item.remark,
            snCode: this.currentSn.snCode,
            workOrderNo: this.currentSn.workOrderNo,
            testPerson: this.testPerson,
            testTime: this.testTime
          })
        )
        await Promise.all(requests)

        uni.showModal({
          title: '提交成功',
          content: `测试结果：${this.testResultText}\n已同步到网页端质检看板`,
          showCancel: false,
          success: () => {
            uni.navigateTo({ url: '/pages/quality/test-dashboard' })
          }
        })
      } catch (e) {
        console.error('提交测试结果失败:', e)
        uni.showToast({ title: '提交失败，请重试', icon: 'none' })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.test-input-page {
  min-height: 100vh;
  background: #F5F7FA;
  padding-bottom: 180rpx;
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

.search-section {
  display: flex;
  gap: 16rpx;
  padding: 24rpx;
  margin-top: -20rpx;
  position: relative;
  z-index: 10;
}

.search-bar {
  flex: 1;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
}

.scan-btn {
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(82, 196, 26, 0.4);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.95);
    box-shadow: 0 4rpx 12rpx rgba(82, 196, 26, 0.3);
  }
}

.scan-icon-wrap {
  width: 48rpx;
  height: 48rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scan-icon {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 600;
}

.scan-text {
  font-size: 20rpx;
  color: #ffffff;
  margin-top: 6rpx;
}

.test-form {
  padding: 0 24rpx;
}

.sn-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
}

.sn-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.sn-label {
  font-size: 24rpx;
  color: #8C8C8C;
}

.sn-status {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;

  &.status-default { background: rgba(191, 191, 191, 0.15); }
  &.status-pass { background: rgba(82, 196, 26, 0.15); }
  &.status-fail { background: rgba(255, 77, 79, 0.15); }
}

.sn-status-text {
  font-size: 22rpx;
  font-weight: 600;

  .status-default & { color: #8C8C8C; }
  .status-pass & { color: #52C41A; }
  .status-fail & { color: #FF4D4F; }
}

.sn-value {
  font-size: 36rpx;
  font-weight: 700;
  color: #1F1F1F;
  display: block;
  font-family: monospace;
}

.sn-info {
  display: flex;
  gap: 24rpx;
  margin-top: 12rpx;
}

.sn-product,
.sn-workorder {
  font-size: 24rpx;
  color: #8C8C8C;
}

.test-section {
  margin-bottom: 24rpx;
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

.test-items {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.test-item {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid #F0F0F0;
  transition: all 0.3s ease;

  &:active {
    background: #F8FAFF;
  }
}

.test-item-header {
  display: flex;
  align-items: center;
}

.test-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  flex-shrink: 0;

  &.heart-icon { background: rgba(255, 107, 107, 0.1); }
  &.blue-icon { background: rgba(59, 124, 255, 0.1); }
  &.battery-icon { background: rgba(250, 173, 20, 0.1); }
  &.water-icon { background: rgba(59, 170, 248, 0.1); }
  &.screen-icon { background: rgba(147, 112, 219, 0.1); }
  &.charge-icon { background: rgba(82, 196, 26, 0.1); }
  &.move-icon { background: rgba(82, 196, 26, 0.1); }
  &.gps-icon { background: rgba(255, 107, 107, 0.1); }
}

.test-icon {
  font-size: 32rpx;
}

.test-info {
  flex: 1;
}

.test-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  display: block;
}

.test-desc {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
}

.test-status-btn {
  padding: 12rpx 24rpx;
  border-radius: 24rpx;
  transition: all 0.3s ease;

  &.status-default { background: #F0F0F0; }
  &.status-pass { background: rgba(82, 196, 26, 0.15); }
  &.status-fail { background: rgba(255, 77, 79, 0.15); }
}

.test-status-text {
  font-size: 24rpx;
  font-weight: 600;

  .status-default & { color: #8C8C8C; }
  .status-pass & { color: #52C41A; }
  .status-fail & { color: #FF4D4F; }
}

.test-result-options {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx dashed #E8E8E8;
}

.option-row {
  display: flex;
  gap: 24rpx;
}

.option-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  transition: all 0.3s ease;

  &.pass { background: rgba(82, 196, 26, 0.08); border: 2rpx solid rgba(82, 196, 26, 0.3); }
  &.fail { background: rgba(255, 77, 79, 0.08); border: 2rpx solid rgba(255, 77, 79, 0.3); }

  &:active {
    transform: scale(0.98);
  }
}

.option-icon-wrap {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;

  &.pass { background: #52C41A; }
  &.fail { background: #FF4D4F; }
}

.option-icon {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 600;
}

.option-text {
  font-size: 28rpx;
  font-weight: 600;

  .pass & { color: #52C41A; }
  .fail & { color: #FF4D4F; }
}

.remark-input {
  margin-top: 16rpx;
}

.summary-section {
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #F5F5F5;

  &:last-child { border-bottom: none; }
}

.summary-label {
  font-size: 26rpx;
  color: #8C8C8C;
}

.summary-value {
  font-size: 26rpx;
  font-weight: 600;
  color: #1F1F1F;

  &.pass { color: #52C41A; }
  &.fail { color: #FF4D4F; }
}

.summary-badge {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;

  &.status-default { background: rgba(191, 191, 191, 0.15); }
  &.status-pass { background: rgba(82, 196, 26, 0.15); }
  &.status-fail { background: rgba(255, 77, 79, 0.15); }
}

.action-bar {
  display: flex;
  padding: 24rpx 0;
}

.empty-state {
  text-align: center;
  padding: 40rpx;
}

.empty-icon {
  font-size: 96rpx;
  display: block;
  margin-bottom: 32rpx;
}

.empty-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1F1F1F;
  display: block;
  margin-bottom: 16rpx;
}

.empty-desc {
  font-size: 28rpx;
  color: #8C8C8C;
  display: block;
  margin-bottom: 40rpx;
  line-height: 1.6;
}

.btn-icon {
  margin-right: 8rpx;
}

.empty-hint {
  font-size: 24rpx;
  color: #BFBFBF;
  display: block;
  margin-top: 32rpx;
}
</style>