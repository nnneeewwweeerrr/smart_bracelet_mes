<template>
  <view class="report-page">
    <DynamicBackground theme="theme-warning" />
    <view class="page-content">
      <view class="page-header">
        <view class="page-title-wrap">
          <text class="page-title">生产报工</text>
          <text class="page-desc">记录生产数据，追踪产品质量</text>
        </view>
      </view>

      <view class="scan-area" @click="handleScan">
        <view class="scan-circle">
          <text class="scan-icon-text">扫</text>
        </view>
        <text class="scan-text">点击扫描工单条码</text>
        <text class="scan-hint">支持扫描工单号（WO开头）</text>
      </view>

      <view class="form-section">
        <view class="section-header">
          <text class="section-title">📝 报工信息</text>
        </view>

        <u-form :model="form" labelPosition="left" labelWidth="180rpx">
          <u-form-item label="工单号">
            <u-input v-model="form.workNo" placeholder="扫码或手动输入" @click="!form.workNo && handleScan()"></u-input>
          </u-form-item>
          <u-form-item label="工序" prop="processId">
            <view class="picker-input" @click="showProcessPicker = true">
              <text :class="{ 'placeholder': !processName }">{{ processName || '请选择工序' }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </u-form-item>
          <u-form-item label="合格数量" prop="goodQuantity">
            <u-input v-model="form.goodQuantity" type="number" placeholder="请输入合格数量"></u-input>
          </u-form-item>
          <u-form-item label="不合格数量" prop="badQuantity">
            <u-input v-model="form.badQuantity" type="number" placeholder="请输入不合格数量"></u-input>
          </u-form-item>
        </u-form>

        <view class="summary" v-if="form.goodQuantity || form.badQuantity">
          <view class="summary-item">
            <text class="summary-label">合计</text>
            <text class="summary-value">{{ (Number(form.goodQuantity) || 0) + (Number(form.badQuantity) || 0) }} 件</text>
          </view>
          <view class="summary-divider"></view>
          <view class="summary-item">
            <text class="summary-label">合格率</text>
            <text class="summary-rate">{{ passRate }}%</text>
          </view>
        </view>
      </view>

      <view class="sop-panel" v-if="sopData && sopData.sop">
        <view class="sop-panel-header">
          <text class="section-title">📋 作业指导书</text>
          <text class="sop-version">{{ sopData.sop.sopVersion || 'V1.0' }}</text>
        </view>
        
        <scroll-view scroll-y class="sop-steps-scroll">
          <view class="sop-steps">
            <view class="sop-step" v-for="(step, idx) in sopSteps" :key="idx">
              <view class="sop-step-no">{{ idx + 1 }}</view>
              <view class="sop-step-content">
                <text class="sop-step-text">{{ step.step }}</text>
                <view class="sop-step-media" v-if="step.image">
                  <image class="sop-step-image" :src="step.image" mode="widthFix" @click="previewSopImage(step.image)"></image>
                </view>
                <view class="sop-video-container" v-if="step.video">
                  <video class="sop-step-video" :src="step.video" controls></video>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>

        <view class="sop-warn" v-if="sopData.sop.precautions">
          <text class="sop-warn-label">⚠️ 注意事项</text>
          <text class="sop-warn-text">{{ sopData.sop.precautions }}</text>
        </view>

        <!-- 物料校验 -->
        <view class="verify-section">
          <view class="verify-header">
            <text class="section-title">📦 物料校验</text>
            <view class="scan-material-btn" @click="handleScanMaterial">
              <text class="scan-btn-text">扫物料</text>
            </view>
          </view>

          <view class="verify-result" v-if="verifiedMaterial">
            <view class="verify-row">
              <text class="verify-label">物料编码</text>
              <text class="verify-value">{{ verifiedMaterial.materialCode }}</text>
            </view>
            <view class="verify-row">
              <text class="verify-label">物料名称</text>
              <text class="verify-value">{{ verifiedMaterial.materialName }}</text>
            </view>
            <view class="verify-row">
              <text class="verify-label">校验结果</text>
              <text :class="['verify-value', verifyStatus === 'pass' ? 'text-success' : 'text-error']">{{ verifyStatus === 'pass' ? '匹配' : '不匹配' }}</text>
            </view>
          </view>

          <view class="bom-tip" v-if="sopData && sopData.materialBom && sopData.materialBom.length > 0 && !verifiedMaterial">
            <text class="bom-tip-title">本工序需要以下物料：</text>
            <view class="bom-tip-item" v-for="bom in sopData.materialBom" :key="bom.materialId">
              <text class="bom-tip-code">{{ bom.materialCode }}</text>
              <text class="bom-tip-name">{{ bom.materialName }}</text>
              <text class="bom-tip-qty">{{ bom.quantity }}{{ bom.unit }}</text>
            </view>
          </view>
        </view>

        <!-- 工艺参数录入与校验 -->
        <view class="param-section">
          <view class="param-header">
            <text class="section-title">⚙️ 工艺参数录入</text>
            <u-tag v-if="paramVerifyStatus === 'pass'" text="参数合格" type="success" size="mini"></u-tag>
            <u-tag v-else-if="paramVerifyStatus === 'fail'" text="参数超差" type="error" size="mini"></u-tag>
          </view>

          <u-form :model="paramForm" labelPosition="left" labelWidth="200rpx">
            <u-form-item v-for="param in sopParamStandards" :key="param.id" :label="param.paramName + '(' + (param.unit || '') + ')'">
              <u-input v-model="paramForm[param.paramCode]" type="digit" :placeholder="'标准' + param.minValue + '~' + param.maxValue">
                <template #suffix>
                  <text v-if="param.isRequired === 1" class="required-mark">*</text>
                </template>
              </u-input>
            </u-form-item>
          </u-form>

          <u-button v-if="sopParamStandards.length > 0" type="primary" plain size="small" @click="handleVerifyParams" customStyle="margin-top: 10rpx; border-radius: 40rpx;">
            校验工艺参数
          </u-button>

          <view class="param-check-result" v-if="paramCheckDetails.length > 0">
            <view class="param-check-item" v-for="check in paramCheckDetails" :key="check.paramCode">
              <text :class="['param-check-status', check.pass ? 'pass' : 'fail']">{{ check.pass ? '✓' : '✗' }}</text>
              <text class="param-check-name">{{ check.paramName }}</text>
              <text class="param-check-reason">{{ check.reason }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="network-status" :class="{ offline: !isOnline }">
        <text class="network-icon">{{ isOnline ? '在线' : '离线' }}</text>
        <text class="network-text">{{ isOnline ? '网络正常' : '离线模式 - 报工数据将暂存本地' }}</text>
      </view>

      <view class="submit-area">
        <u-button type="primary" :loading="loading" @click="handleSubmit" customStyle="height: 88rpx; border-radius: 44rpx;" :disabled="!isOnline">
          {{ isOnline ? '提交报工' : '离线中，无法提交' }}
        </u-button>
        <u-button type="warning" plain @click="handleOfflineSubmit" customStyle="height: 88rpx; border-radius: 44rpx; margin-top: 16rpx;">
          离线暂存报工
        </u-button>
      </view>

      <view class="offline-section" v-if="offlineRecords.length > 0">
        <view class="offline-header">
          <text class="section-title">💾 离线暂存记录</text>
          <view class="sync-btn" @click="syncOfflineRecords">
            <text class="sync-icon">↻</text>
            <text class="sync-text">同步上传</text>
          </view>
        </view>
        <view class="offline-list">
          <view class="offline-item" v-for="(item, idx) in offlineRecords" :key="idx">
            <view class="offline-info">
              <text class="offline-workno">{{ item.form.workNo }}</text>
              <text class="offline-time">{{ item.createTime }}</text>
            </view>
            <view class="offline-actions">
              <view class="action-btn delete" @click="deleteOfflineRecord(idx)">删除</view>
            </view>
          </view>
        </view>
      </view>

      <u-picker :show="showProcessPicker" :columns="processColumns" keyName="processName" @confirm="onProcessConfirm" @cancel="showProcessPicker = false"></u-picker>
    </view>
  </view>
</template>

<script>
import { scanCode } from '@/utils/scan'
import { scanProductionReport, productionReport, getProcessList, verifyMaterial, verifyParams, getSopDetail } from '@/api/production'
import { getUser } from '@/utils/auth'
import DynamicBackground from '@/components/DynamicBackground'

export default {
  components: {
    DynamicBackground
  },
  data() {
    return {
      form: {
        workOrderId: '',
        workNo: '',
        processId: '',
        goodQuantity: '',
        badQuantity: ''
      },
      paramForm: {},
      processName: '',
      processList: [],
      processOptions: [],
      processColumns: [],
      showProcessPicker: false,
      loading: false,
      verifyStatus: '',
      verifiedMaterial: null,
      paramVerifyStatus: '',
      paramCheckDetails: [],
      sopData: null,
      sopSteps: [],
      offlineRecords: [],
      isOnline: true
    }
  },
  computed: {
    passRate() {
      const good = Number(this.form.goodQuantity) || 0
      const bad = Number(this.form.badQuantity) || 0
      const total = good + bad
      if (total === 0) return '0.0'
      return ((good / total) * 100).toFixed(1)
    },
    sopParamStandards() {
      return (this.sopData && this.sopData.paramStandards) || []
    }
  },
  watch: {
    'form.processId'(newVal) {
      if (newVal) {
        this.loadSop(newVal)
      } else {
        this.sopData = null
        this.sopSteps = []
      }
    }
  },
  onLoad(options) {
    if (options.workOrderId) this.form.workOrderId = options.workOrderId
    if (options.workNo) this.form.workNo = options.workNo
    if (options.processId) this.form.processId = options.processId
    if (options.processName) this.processName = decodeURIComponent(options.processName || '')
    this.loadProcesses()
    this.checkNetwork()
    this.watchNetwork()
  },
  onShow() {
    this.loadOfflineRecords()
    this.checkNetwork()
    if (this.isOnline && this.offlineRecords.length > 0) {
      uni.showModal({
        title: '检测到离线记录',
        content: `发现${this.offlineRecords.length}条未同步的离线报工记录，是否立即同步？`,
        success: (res) => {
          if (res.confirm) {
            this.syncOfflineRecords()
          }
        }
      })
    }
  },
  onUnload() {
    if (this.networkListener) {
      uni.offNetworkStatusChange(this.networkListener)
    }
  },
  methods: {
    checkNetwork() {
      const network = uni.getNetworkType({
        success: (res) => {
          this.isOnline = res.networkType !== 'none'
        },
        fail: () => {
          this.isOnline = false
        }
      })
    },
    watchNetwork() {
      this.networkListener = uni.onNetworkStatusChange((res) => {
        const wasOffline = !this.isOnline
        this.isOnline = res.isConnected
        
        if (this.isOnline && wasOffline) {
          uni.showToast({
            title: '网络已恢复',
            icon: 'success'
          })
          
          if (this.offlineRecords.length > 0) {
            setTimeout(() => {
              uni.showModal({
                title: '检测到离线记录',
                content: `发现${this.offlineRecords.length}条未同步的离线报工记录，是否立即同步？`,
                success: (res) => {
                  if (res.confirm) {
                    this.syncOfflineRecords()
                  }
                }
              })
            }, 1000)
          }
        } else if (!this.isOnline) {
          uni.showToast({
            title: '已切换至离线模式',
            icon: 'none'
          })
        }
      })
    },
    async loadProcesses() {
      try {
        this.processList = await getProcessList() || []
        this.processOptions = this.processList
        this.processColumns = [this.processList]
        if (this.form.processId) {
          const idx = this.processList.findIndex(p => p.id === Number(this.form.processId))
          if (idx >= 0) {
            this.processName = this.processList[idx].processName
          }
        }
      } catch {
        this.processList = []
        this.processOptions = []
        this.processColumns = []
      }
    },
    async loadSop(processId) {
      try {
        const res = await getSopDetail(processId)
        if (res) {
          this.sopData = res
          if (res.sop && res.sop.operationSteps) {
            try {
              const parsedSteps = JSON.parse(res.sop.operationSteps)
              if (Array.isArray(parsedSteps)) {
                this.sopSteps = parsedSteps.map(s => {
                  if (typeof s === 'string') {
                    return { step: s, image: '', video: '' }
                  }
                  return {
                    step: s.step || '',
                    image: s.image || '',
                    video: s.video || ''
                  }
                })
              } else {
                this.sopSteps = []
              }
            } catch {
              this.sopSteps = []
            }
          }
          if (res.paramStandards) {
            this.paramForm = {}
            res.paramStandards.forEach(p => {
              this.paramForm[p.paramCode] = ''
            })
          }
        }
      } catch {
        this.sopData = null
        this.sopSteps = []
      }
    },
    previewSopImage(url) {
      uni.previewImage({
        urls: [url]
      })
    },
    async handleScan() {
      try {
        const code = await scanCode()
        let res = null
        try {
          res = await scanProductionReport({ code })
        } catch {
          res = this.getMockWorkOrder(code)
        }
        this.form.workOrderId = res.workOrderId || ''
        this.form.workNo = res.workNo || code
        if (res.processId) this.form.processId = res.processId
        if (res.processName) this.processName = res.processName
      } catch {
        // cancelled
      }
    },
    getMockWorkOrder(code) {
      const mockOrders = {
        'WO202607150001': { workOrderId: '1', workNo: 'WO202607150001', processId: '1', processName: 'SMT贴片' },
        'WO202607150002': { workOrderId: '2', workNo: 'WO202607150002', processId: '2', processName: 'AOI检测' },
        'WO202607150003': { workOrderId: '3', workNo: 'WO202607150003', processId: '3', processName: '焊接组装' },
        'WO202607150004': { workOrderId: '4', workNo: 'WO202607150004', processId: '4', processName: '功能测试' },
        'WO202607150005': { workOrderId: '5', workNo: 'WO202607150005', processId: '5', processName: '外观检查' },
        'WO202607150006': { workOrderId: '6', workNo: 'WO202607150006', processId: '6', processName: '包装入库' }
      }
      return mockOrders[code] || { workOrderId: '', workNo: code, processId: '', processName: '' }
    },
    async handleScanMaterial() {
      if (!this.form.processId) {
        uni.showToast({ title: '请先选择工序', icon: 'none' })
        return
      }
      try {
        const code = await scanCode()
        const res = await verifyMaterial({
          materialCode: code,
          processId: this.form.processId
        })
        this.verifiedMaterial = {
          materialCode: res.materialCode || code,
          materialName: res.materialName || ''
        }
        this.verifyStatus = res.pass ? 'pass' : 'fail'

        if (!res.pass) {
          uni.showModal({
            title: '物料校验失败',
            content: res.reason || '扫码物料与当前工序BOM不匹配，请确认物料是否正确',
            showCancel: false,
            confirmColor: '#FF4D4F'
          })
        } else {
          uni.showToast({ title: '物料校验通过', icon: 'success' })
        }
      } catch {
        this.verifiedMaterial = null
        this.verifyStatus = ''
      }
    },
    async handleVerifyParams() {
      if (!this.form.processId) {
        uni.showToast({ title: '请先选择工序', icon: 'none' })
        return
      }
      const params = {}
      let hasAny = false
      for (const key in this.paramForm) {
        if (this.paramForm[key] !== '' && this.paramForm[key] != null) {
          params[key] = Number(this.paramForm[key])
          hasAny = true
        }
      }
      if (!hasAny) {
        uni.showToast({ title: '请至少填写一个参数', icon: 'none' })
        return
      }
      try {
        const res = await verifyParams({
          processId: this.form.processId,
          params
        })
        this.paramCheckDetails = res.details || []
        this.paramVerifyStatus = res.pass ? 'pass' : 'fail'

        if (!res.pass) {
          uni.showModal({
            title: '参数校验失败',
            content: res.reason || '部分工艺参数超差，请检查后重新录入',
            showCancel: false,
            confirmColor: '#FF4D4F'
          })
        } else {
          uni.showToast({ title: '参数校验通过', icon: 'success' })
        }
      } catch {
        this.paramVerifyStatus = ''
        this.paramCheckDetails = []
      }
    },
    onProcessConfirm({ value }) {
      const item = value[0]
      if (item) {
        this.form.processId = item.id
        this.processName = item.processName
        this.verifyStatus = ''
        this.verifiedMaterial = null
        this.paramVerifyStatus = ''
        this.paramCheckDetails = []
      }
      this.showProcessPicker = false
    },
    async handleSubmit() {
      if (!this.form.workNo) {
        uni.showToast({ title: '请输入工单号', icon: 'none' })
        return
      }
      if (!this.form.processId) {
        uni.showToast({ title: '请选择工序', icon: 'none' })
        return
      }
      const good = Number(this.form.goodQuantity) || 0
      const bad = Number(this.form.badQuantity) || 0
      if (good + bad <= 0) {
        uni.showToast({ title: '请输入报工数量', icon: 'none' })
        return
      }

      if (this.verifyStatus === 'fail') {
        uni.showModal({
          title: '物料校验未通过',
          content: '当前物料校验失败，无法提交报工。请重新扫码校验正确物料。',
          showCancel: false,
          confirmColor: '#FF4D4F'
        })
        return
      }

      if (this.paramVerifyStatus === 'fail') {
        uni.showModal({
          title: '参数校验未通过',
          content: '工艺参数超差，无法提交报工。请调整参数后重新校验。',
          showCancel: false,
          confirmColor: '#FF4D4F'
        })
        return
      }

      if (this.sopParamStandards.length > 0 && this.paramVerifyStatus !== 'pass') {
        uni.showModal({
          title: '请先校验工艺参数',
          content: '当前工序有必检工艺参数，请填写并校验通过后再提交报工。',
          showCancel: false,
          confirmColor: '#FAAD14'
        })
        return
      }

      const user = getUser() || {}
      this.loading = true
      try {
        const processParams = {}
        for (const key in this.paramForm) {
          if (this.paramForm[key] !== '' && this.paramForm[key] != null) {
            processParams[key] = Number(this.paramForm[key])
          }
        }

        await productionReport({
          workOrderId: this.form.workOrderId || undefined,
          workerId: user.id,
          processId: this.form.processId,
          goodQuantity: good,
          badQuantity: bad,
          materialCode: this.verifiedMaterial ? this.verifiedMaterial.materialCode : undefined,
          processParams
        })
        uni.showToast({ title: '报工成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 1500)
      } catch {
        // handled by request interceptor
      } finally {
        this.loading = false
      }
    },
    handleOfflineSubmit() {
      if (!this.form.workNo) {
        uni.showToast({ title: '请输入工单号', icon: 'none' })
        return
      }
      if (!this.form.processId) {
        uni.showToast({ title: '请选择工序', icon: 'none' })
        return
      }
      const good = Number(this.form.goodQuantity) || 0
      const bad = Number(this.form.badQuantity) || 0
      if (good + bad <= 0) {
        uni.showToast({ title: '请输入报工数量', icon: 'none' })
        return
      }

      const record = {
        form: { ...this.form },
        paramForm: { ...this.paramForm },
        processName: this.processName,
        verifyStatus: this.verifyStatus,
        verifiedMaterial: this.verifiedMaterial,
        createTime: new Date().toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        }),
        syncStatus: 'pending'
      }

      this.offlineRecords.unshift(record)
      this.saveOfflineRecords()

      uni.showModal({
        title: '离线暂存成功',
        content: `报工记录已保存到本地，当前共有${this.offlineRecords.length}条待同步记录。联网后可点击"同步上传"按钮提交。`,
        showCancel: false
      })

      this.form = {
        workOrderId: '',
        workNo: '',
        processId: '',
        goodQuantity: '',
        badQuantity: ''
      }
      this.processName = ''
      this.paramForm = {}
      this.verifyStatus = ''
      this.verifiedMaterial = null
      this.paramVerifyStatus = ''
      this.paramCheckDetails = []
    },
    loadOfflineRecords() {
      try {
        const records = uni.getStorageSync('offlineProductionReports')
        this.offlineRecords = records || []
      } catch {
        this.offlineRecords = []
      }
    },
    saveOfflineRecords() {
      try {
        uni.setStorageSync('offlineProductionReports', this.offlineRecords)
      } catch {
        // storage error
      }
    },
    deleteOfflineRecord(idx) {
      uni.showModal({
        title: '确认删除',
        content: '删除后将无法恢复，确定要删除这条离线记录吗？',
        success: (res) => {
          if (res.confirm) {
            this.offlineRecords.splice(idx, 1)
            this.saveOfflineRecords()
            uni.showToast({ title: '删除成功', icon: 'success' })
          }
        }
      })
    },
    async syncOfflineRecords() {
      if (!this.isOnline) {
        uni.showToast({ title: '当前处于离线状态', icon: 'none' })
        return
      }

      const total = this.offlineRecords.length
      if (total === 0) {
        uni.showToast({ title: '暂无待同步记录', icon: 'none' })
        return
      }

      let successCount = 0
      let failCount = 0
      const failedRecords = []

      uni.showLoading({ title: `同步中 0/${total}` })

      for (let i = 0; i < this.offlineRecords.length; i++) {
        const record = this.offlineRecords[i]
        
        await new Promise(resolve => setTimeout(resolve, 500))

        uni.showLoading({ title: `同步中 ${i + 1}/${total}` })

        try {
          const user = getUser() || {}
          await productionReport({
            workOrderId: record.form.workOrderId || undefined,
            workerId: user.id,
            processId: record.form.processId,
            goodQuantity: Number(record.form.goodQuantity) || 0,
            badQuantity: Number(record.form.badQuantity) || 0,
            materialCode: record.verifiedMaterial ? record.verifiedMaterial.materialCode : undefined,
            processParams: {
              temperature: record.paramForm.temperature ? Number(record.paramForm.temperature) : undefined,
              humidity: record.paramForm.humidity ? Number(record.paramForm.humidity) : undefined,
              pressure: record.paramForm.pressure ? Number(record.paramForm.pressure) : undefined,
              speed: record.paramForm.speed ? Number(record.paramForm.speed) : undefined
            }
          })
          successCount++
        } catch {
          failCount++
          failedRecords.push(record)
        }
      }

      uni.hideLoading()

      if (failedRecords.length > 0) {
        this.offlineRecords = failedRecords
        this.saveOfflineRecords()
      } else {
        this.offlineRecords = []
        this.saveOfflineRecords()
      }

      uni.showModal({
        title: successCount === total ? '同步成功' : '同步完成',
        content: `成功上传${successCount}条，${failCount > 0 ? `失败${failCount}条（已保留在本地）` : ''}`,
        showCancel: false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.report-page {
  min-height: 100vh;
  background: #F5F7FA;
  position: relative;
  z-index: 1;
}

.page-content {
  position: relative;
  z-index: 10;
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
  background: linear-gradient(145deg, #FFFFFF 0%, #FFFBE6 100%);
  border-radius: 28rpx;
  padding: 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(250, 173, 20, 0.15);
  border: 2rpx dashed rgba(250, 173, 20, 0.4);
}

.scan-circle {
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #FAAD14 0%, #FFC53D 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(250, 173, 20, 0.4);
}

.scan-icon-text {
  font-size: 56rpx;
  font-weight: 600;
  color: #ffffff;
}

.scan-text {
  font-size: 28rpx;
  color: #FAAD14;
  margin-top: 20rpx;
  font-weight: 500;
}

.scan-hint {
  font-size: 22rpx;
  color: #D48806;
  margin-top: 10rpx;
}

.picker-input {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 24rpx;
  background: #F8FAFF;
  border: 2rpx solid #E8E8E8;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #333333;
  transition: all 0.3s ease;

  &:active {
    background: #F0F0F0;
    border-color: #FAAD14;
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

.form-section,
.verify-section,
.param-section,
.sop-panel {
  margin: 0 24rpx 24rpx;
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

.sop-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.sop-version {
  font-size: 24rpx;
  color: #8C8C8C;
  background: #F5F5F5;
  padding: 8rpx 16rpx;
  border-radius: 12rpx;
}

.sop-steps-scroll {
  height: 500rpx;
  width: 100%;
  margin-bottom: 16rpx;
}

.sop-steps {
  background: #F0F7FF;
  border-radius: 16rpx;
  padding: 20rpx;
  min-height: 100%;
}

.sop-step {
  display: flex;
  align-items: flex-start;
  margin-bottom: 14rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.sop-step-no {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FAAD14 0%, #FFC53D 100%);
  color: #fff;
  font-size: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14rpx;
  flex-shrink: 0;
}

.sop-step-content {
  flex: 1;
}

.sop-step-text {
  font-size: 26rpx;
  color: #4A4A4A;
  line-height: 40rpx;
  display: block;
  margin-bottom: 12rpx;
}

.sop-step-media {
  margin-top: 12rpx;
  border-radius: 12rpx;
  overflow: hidden;
}

.sop-step-image {
  width: 100%;
  max-height: 360rpx;
  display: block;
}

.sop-video-container {
  width: 100%;
  background: #000;
  border-radius: 12rpx;
  overflow: hidden;
}

.sop-step-video {
  width: 100%;
  height: 320rpx;
}

.sop-warn {
  background: #FFF7E6;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-top: 16rpx;
}

.sop-warn-label {
  font-size: 24rpx;
  color: #FAAD14;
  font-weight: 600;
  display: block;
  margin-bottom: 8rpx;
}

.sop-warn-text {
  font-size: 24rpx;
  color: #D48806;
  line-height: 1.6;
}

.verify-header,
.param-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.scan-material-btn {
  background: linear-gradient(135deg, #FAAD14 0%, #FFC53D 100%);
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(250, 173, 20, 0.3);
}

.scan-btn-text {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 600;
}

.verify-result {
  background: #F5F7FA;
  border-radius: 16rpx;
  padding: 24rpx;
}

.verify-row {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.verify-label {
  font-size: 26rpx;
  color: #8C8C8C;
  width: 160rpx;
  flex-shrink: 0;
}

.verify-value {
  font-size: 26rpx;
  color: #4A4A4A;
  flex: 1;
  font-weight: 500;

  &.text-success { color: #52C41A; }
  &.text-error { color: #FF4D4F; }
}

.bom-tip {
  background: #F0F7FF;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-top: 16rpx;
}

.bom-tip-title {
  font-size: 24rpx;
  color: #3B7CFF;
  font-weight: 600;
  display: block;
  margin-bottom: 12rpx;
}

.bom-tip-item {
  display: flex;
  gap: 16rpx;
  margin-bottom: 8rpx;
}

.bom-tip-code {
  font-size: 22rpx;
  color: #3B7CFF;
  width: 160rpx;
}

.bom-tip-name {
  font-size: 22rpx;
  color: #4A4A4A;
  flex: 1;
}

.bom-tip-qty {
  font-size: 22rpx;
  color: #8C8C8C;
}

.required-mark {
  color: #FF4D4F;
  font-size: 28rpx;
  font-weight: 600;
}

.param-check-result {
  background: #F5F7FA;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-top: 16rpx;
}

.param-check-item {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.param-check-status {
  width: 36rpx;
  font-size: 28rpx;
  font-weight: 600;

  &.pass { color: #52C41A; }
  &.fail { color: #FF4D4F; }
}

.param-check-name {
  font-size: 24rpx;
  color: #4A4A4A;
  width: 140rpx;
  flex-shrink: 0;
}

.param-check-reason {
  font-size: 22rpx;
  color: #8C8C8C;
  flex: 1;
}

.summary {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: linear-gradient(145deg, #FFFBE6 0%, #FFF7E6 100%);
  border-radius: 16rpx;
  margin-top: 20rpx;
  margin-bottom: 16rpx;
}

.summary-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.summary-label {
  font-size: 24rpx;
  color: #D48806;
  margin-bottom: 6rpx;
}

.summary-value {
  font-size: 32rpx;
  color: #1F1F1F;
  font-weight: 600;
}

.summary-rate {
  font-size: 32rpx;
  color: #52C41A;
  font-weight: 600;
}

.summary-divider {
  width: 2rpx;
  height: 60rpx;
  background: rgba(250, 173, 20, 0.3);
  margin: 0 32rpx;
}

.network-status {
  margin: 0 24rpx 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 16rpx;
  background: rgba(82, 196, 26, 0.1);
  border-radius: 16rpx;

  &.offline {
    background: rgba(255, 77, 79, 0.1);

    .network-icon,
    .network-text {
      color: #FF4D4F;
    }
  }
}

.network-icon {
  font-size: 28rpx;
  color: #52C41A;
  font-weight: 600;
}

.network-text {
  font-size: 24rpx;
  color: #52C41A;
}

.submit-area {
  padding: 24rpx;
}

.offline-section {
  margin: 0 24rpx 40rpx;
  background: linear-gradient(145deg, #FFFFFF 0%, #F8FAFF 100%);
  border-radius: 28rpx;
  padding: 34rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.offline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.sync-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background: rgba(82, 196, 26, 0.1);
  border-radius: 20rpx;
  transition: all 0.3s ease;

  &:active {
    background: rgba(82, 196, 26, 0.2);
  }
}

.sync-icon {
  font-size: 24rpx;
  color: #52C41A;
}

.sync-text {
  font-size: 24rpx;
  color: #52C41A;
  font-weight: 600;
}

.offline-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.offline-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  background: #F8FAFF;
  border-radius: 16rpx;
  transition: all 0.3s ease;
}

.offline-info {
  flex: 1;
}

.offline-workno {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
  margin-bottom: 6rpx;
}

.offline-time {
  font-size: 22rpx;
  color: #8C8C8C;
}

.offline-actions {
  margin-left: 16rpx;
}

.action-btn {
  padding: 10rpx 24rpx;
  border-radius: 16rpx;
  font-size: 24rpx;
  font-weight: 500;
  transition: all 0.3s ease;

  &:active {
    opacity: 0.8;
  }

  &.delete {
    background: rgba(255, 77, 79, 0.1);
    color: #FF4D4F;
  }
}

:deep(.u-button) {
  border-radius: 48rpx !important;
}

:deep(.u-button--primary) {
  box-shadow: 0 4rpx 12rpx rgba(250, 173, 20, 0.3);
}
</style>