<template>
  <view class="report-page">
    <view class="scan-area" @click="handleScan">
      <text class="scan-icon-text">扫</text>
      <text class="scan-text">点击扫描工单条码</text>
    </view>

    <view class="form-section">
      <text class="section-title">报工信息</text>

      <u-form :model="form" labelPosition="left" labelWidth="180rpx">
        <u-form-item label="工单号">
          <u-input v-model="form.workNo" placeholder="扫码或手动输入" @click="!form.workNo && handleScan()"></u-input>
        </u-form-item>
        <u-form-item label="工序" prop="processId">
          <u-input v-model="processName" placeholder="请选择工序" readonly @click="showProcessPicker = true">
            <template #suffix>
              <text class="suffix-arrow">›</text>
            </template>
          </u-input>
        </u-form-item>
        <u-form-item label="合格数量" prop="goodQuantity">
          <u-input v-model="form.goodQuantity" type="number" placeholder="请输入合格数量"></u-input>
        </u-form-item>
        <u-form-item label="不合格数量" prop="badQuantity">
          <u-input v-model="form.badQuantity" type="number" placeholder="请输入不合格数量"></u-input>
        </u-form-item>
      </u-form>

      <view class="summary" v-if="form.goodQuantity || form.badQuantity">
        <text class="summary-text">合计: {{ (Number(form.goodQuantity) || 0) + (Number(form.badQuantity) || 0) }} 件</text>
        <text class="summary-rate">合格率: {{ passRate }}%</text>
      </view>
    </view>

    <!-- SOP展示面板（选择工序后自动加载） -->
    <view class="sop-panel" v-if="sopData && sopData.sop">
      <view class="sop-panel-header">
        <text class="section-title">电子SOP指导</text>
        <u-tag :text="sopData.sop.sopVersion || 'V1.0'" type="primary" size="mini"></u-tag>
      </view>
      <view class="sop-steps" v-if="sopSteps.length > 0">
        <view class="sop-step" v-for="(step, idx) in sopSteps" :key="idx">
          <view class="sop-step-no">{{ idx + 1 }}</view>
          <text class="sop-step-text">{{ step }}</text>
        </view>
      </view>
      <view class="sop-warn" v-if="sopData.sop.precautions">
        <text class="sop-warn-label">注意事项</text>
        <text class="sop-warn-text">{{ sopData.sop.precautions }}</text>
      </view>
    </view>

    <!-- 物料校验区域 -->
    <view class="verify-section">
      <view class="verify-header">
        <text class="section-title">物料防错校验</text>
        <u-tag v-if="verifyStatus === 'pass'" text="校验通过" type="success" size="mini"></u-tag>
        <u-tag v-else-if="verifyStatus === 'fail'" text="校验失败" type="error" size="mini"></u-tag>
      </view>

      <u-button type="warning" plain @click="handleScanMaterial" customStyle="height: 80rpx; border-radius: 40rpx; margin-bottom: 20rpx;">
        <text style="margin-right: 8rpx;">扫</text>
        扫码校验物料
      </u-button>

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

      <!-- BOM清单提示 -->
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
        <text class="section-title">工艺参数录入</text>
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

    <view class="submit-area">
      <u-button type="primary" :loading="loading" @click="handleSubmit" customStyle="height: 88rpx; border-radius: 44rpx;">
        提交报工
      </u-button>
    </view>

    <u-picker :show="showProcessPicker" :columns="processColumns" keyName="processName" @confirm="onProcessConfirm" @cancel="showProcessPicker = false"></u-picker>
  </view>
</template>

<script>
import { scanCode } from '@/utils/scan'
import { scanProductionReport, productionReport, getProcessList, verifyMaterial, verifyParams, getSopDetail } from '@/api/production'
import { getUser } from '@/utils/auth'

export default {
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
      processColumns: [],
      showProcessPicker: false,
      loading: false,
      verifyStatus: '',
      verifiedMaterial: null,
      paramVerifyStatus: '',
      paramCheckDetails: [],
      sopData: null,
      sopSteps: []
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
  },
  methods: {
    async loadProcesses() {
      try {
        this.processList = await getProcessList() || []
        this.processColumns = [this.processList]
      } catch {
        this.processList = []
        this.processColumns = [[]]
      }
    },
    async loadSop(processId) {
      try {
        const res = await getSopDetail(processId)
        if (res) {
          this.sopData = res
          // 解析操作步骤
          if (res.sop && res.sop.operationSteps) {
            try {
              this.sopSteps = JSON.parse(res.sop.operationSteps)
            } catch {
              this.sopSteps = []
            }
          }
          // 初始化参数表单
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
    async handleScan() {
      try {
        const code = await scanCode()
        const res = await scanProductionReport({ code })
        this.form.workOrderId = res.workOrderId || ''
        this.form.workNo = res.workNo || code
        if (res.processId) this.form.processId = res.processId
        if (res.processName) this.processName = res.processName
      } catch {
        // cancelled
      }
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
      // 构建参数对象，过滤空值转为数字
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
        // 重置校验状态
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

      // 物料校验拦截
      if (this.verifyStatus === 'fail') {
        uni.showModal({
          title: '物料校验未通过',
          content: '当前物料校验失败，无法提交报工。请重新扫码校验正确物料。',
          showCancel: false,
          confirmColor: '#FF4D4F'
        })
        return
      }

      // 参数校验拦截
      if (this.paramVerifyStatus === 'fail') {
        uni.showModal({
          title: '参数校验未通过',
          content: '工艺参数超差，无法提交报工。请调整参数后重新校验。',
          showCancel: false,
          confirmColor: '#FF4D4F'
        })
        return
      }

      // 如果有参数标准但未校验，提示先校验
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
        // 构建参数对象
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
    }
  }
}
</script>

<style lang="scss" scoped>
.report-page {
  min-height: 100vh;
  background: transparent;
  padding: 24rpx 24rpx 40rpx;
}

.scan-area {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
  box-shadow: 0 14rpx 28rpx rgba(0, 0, 0, 0.08);
  border: 2rpx dashed rgba(250, 173, 20, 0.35);
}

.scan-icon-text {
  font-size: 56rpx;
  font-weight: 600;
  color: #FAAD14;
}

.scan-text {
  font-size: 28rpx;
  color: #FAAD14;
  margin-top: 20rpx;
}

.suffix-arrow {
  font-size: 24rpx;
  color: #BFBFBF;
}

.form-section,
.verify-section,
.param-section,
.sop-panel {
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

/* SOP面板 */
.sop-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;

  .section-title {
    margin-bottom: 0;
  }
}

.sop-steps {
  background: #F0F7FF;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
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
  background: #3B7CFF;
  color: #fff;
  font-size: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14rpx;
  flex-shrink: 0;
}

.sop-step-text {
  font-size: 26rpx;
  color: #4A4A4A;
  line-height: 40rpx;
  flex: 1;
}

.sop-warn {
  background: #FFF7E6;
  border-radius: 12rpx;
  padding: 16rpx;
}

.sop-warn-label {
  font-size: 24rpx;
  color: #FAAD14;
  font-weight: 600;
  display: block;
  margin-bottom: 6rpx;
}

.sop-warn-text {
  font-size: 24rpx;
  color: #D48806;
  line-height: 1.5;
}

/* 物料校验 */
.verify-header,
.param-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;

  .section-title {
    margin-bottom: 0;
  }
}

.verify-result {
  background: #F5F7FA;
  border-radius: 12rpx;
  padding: 20rpx;
}

.verify-row {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;

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

  &.text-success { color: #52C41A; font-weight: 500; }
  &.text-error { color: #FF4D4F; font-weight: 500; }
}

.bom-tip {
  background: #F0F7FF;
  border-radius: 12rpx;
  padding: 16rpx;
  margin-top: 16rpx;
}

.bom-tip-title {
  font-size: 24rpx;
  color: #3B7CFF;
  font-weight: 500;
  display: block;
  margin-bottom: 10rpx;
}

.bom-tip-item {
  display: flex;
  gap: 16rpx;
  margin-bottom: 6rpx;
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

/* 参数校验 */
.required-mark {
  color: #FF4D4F;
  font-size: 28rpx;
  font-weight: 600;
}

.param-check-result {
  background: #F5F7FA;
  border-radius: 12rpx;
  padding: 16rpx;
  margin-top: 16rpx;
}

.param-check-item {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;

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
  width: 120rpx;
  flex-shrink: 0;
}

.param-check-reason {
  font-size: 22rpx;
  color: #8C8C8C;
  flex: 1;
}

.summary {
  display: flex;
  justify-content: space-between;
  padding: 20rpx;
  background: #F5F7FA;
  border-radius: 12rpx;
  margin-top: 20rpx;
}

.summary-text {
  font-size: 28rpx;
  color: #4A4A4A;
  font-weight: 500;
}

.summary-rate {
  font-size: 28rpx;
  color: #52C41A;
  font-weight: 500;
}

.submit-area {
  padding-top: 10rpx;
}
</style>
