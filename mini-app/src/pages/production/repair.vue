<template>
  <view class="repair-page">
    <DynamicBackground theme="theme-error" />
    <view class="page-content">
    <view class="page-header">
      <view class="page-title-wrap">
        <text class="page-title">不良品维修</text>
        <text class="page-desc">扫码返修、维修闭环、全程留痕</text>
      </view>
    </view>

    <view class="scan-section">
      <view class="search-bar">
        <u-search v-model="searchKey" placeholder="扫描维修单号/SN码" @search="doSearch" @custom="doSearch" @clear="doSearch" shape="round"></u-search>
      </view>
      <view class="scan-btn" @click="handleScan">
        <view class="scan-icon-wrap">
          <text class="scan-icon">扫</text>
        </view>
        <text class="scan-text">扫码</text>
      </view>
    </view>

    <view class="filter-bar">
      <u-tabs :list="statusList" :current="currentStatus" @click="onStatusChange" :activeStyle="{ color: '#FF4D4F', fontWeight: 'bold' }" :inactiveStyle="{ color: '#8C8C8C' }" lineColor="#FF4D4F" :lineWidth="30"></u-tabs>
    </view>

    <view class="repair-list" v-if="repairList.length > 0">
      <view class="repair-card" v-for="item in repairList" :key="item.id" @click="viewDetail(item)">
        <view class="card-status-bar" :class="`status-${item.status}`"></view>
        <view class="card-content">
          <view class="card-header">
              <view class="header-left">
                <text class="repair-no">{{ item.taskNo }}</text>
                <text class="sn-code">{{ item.equipmentNo }}</text>
              </view>
              <view class="status-badge" :class="`badge-${getStatusTag(item.status)}`">
                <view class="status-dot"></view>
                <text class="status-text">{{ getStatusText(item.status) }}</text>
              </view>
            </view>
            
            <view class="card-body">
              <view class="defect-row">
                <view class="defect-tag">
                  <text class="tag-icon">⚠️</text>
                  <text class="tag-text">{{ item.faultType || item.priority || '设备故障' }}</text>
                </view>
              </view>
              <view class="desc-row">
                <text class="desc-text">{{ item.faultDesc || item.faultDescription }}</text>
              </view>
            </view>
            
            <view class="card-footer">
              <view class="footer-item">
                <text class="footer-icon">👤</text>
                <text class="footer-text">{{ item.reporter }}</text>
              </view>
              <view class="footer-item">
                <text class="footer-icon">📍</text>
                <text class="footer-text">{{ item.equipmentName }}</text>
              </view>
              <view class="footer-item">
                <text class="footer-icon">🕐</text>
                <text class="footer-text">{{ item.createTime || item.reportTime }}</text>
              </view>
            </view>
          
          <view class="card-action">
            <text class="action-text">查看详情</text>
            <text class="action-arrow">›</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="searched && repairList.length === 0" style="padding-top: 80rpx;">
      <u-empty mode="data" text="暂无维修工单"></u-empty>
    </view>

    <view v-if="!searched" style="padding-top: 120rpx;">
      <u-empty mode="search" text="选择状态查看维修工单"></u-empty>
    </view>

    <view class="bottom-action">
      <u-button type="error" @click="createRepair" customStyle="flex:1; height: 88rpx; border-radius: 44rpx;">
        新增维修工单
      </u-button>
    </view>

    <u-popup v-if="detailVisible" :show="detailVisible" mode="bottom" round="32" closeable @close="detailVisible = false" customStyle="height: 95vh;">
      <view class="detail-popup">
        <view class="detail-header">
          <view class="header-left">
            <text class="detail-title">维修详情</text>
            <text class="detail-no">{{ currentRepair.taskNo }}</text>
          </view>
          <view class="status-badge-large" :class="`badge-${getStatusTag(currentRepair.status)}`">
            <view class="status-dot"></view>
            <text class="status-text">{{ getStatusText(currentRepair.status) }}</text>
          </view>
        </view>

        <scroll-view scroll-y class="detail-scroll">
          <view class="info-card">
            <view class="card-label">基本信息</view>
            <view class="info-row-sn">
              <text class="info-label">任务编号</text>
              <text class="info-value-sn">{{ currentRepair.taskNo }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">设备编号</text>
              <text class="info-value">{{ currentRepair.equipmentNo }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">设备名称</text>
              <text class="info-value">{{ currentRepair.equipmentName }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">优先级</text>
              <text class="info-value">{{ getPriorityText(currentRepair.priority) }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">创建时间</text>
              <text class="info-value">{{ currentRepair.createTime || currentRepair.reportTime }}</text>
            </view>
          </view>

          <view class="defect-card">
            <view class="card-header-row">
              <view class="card-label">故障信息</view>
              <view class="defect-type-tag">
                <text class="tag-icon">⚠️</text>
                <text class="tag-text">{{ currentRepair.faultType || currentRepair.priority || '设备故障' }}</text>
              </view>
            </view>
            <view class="defect-desc-box">
              <text class="defect-desc">{{ currentRepair.faultDesc || currentRepair.faultDescription }}</text>
            </view>
            <view class="defect-meta">
              <view class="meta-item">
                <text class="meta-icon">📍</text>
                <text class="meta-label">设备位置</text>
                <text class="meta-value">{{ currentRepair.equipmentName }}</text>
              </view>
              <view class="meta-item">
                <text class="meta-icon">👤</text>
                <text class="meta-label">报修人</text>
                <text class="meta-value">{{ currentRepair.reporter }}</text>
              </view>
            </view>
          </view>

          <view class="record-card">
            <view class="card-label">维修记录</view>
            <view class="record-list" v-if="currentRepair.repairRecords && currentRepair.repairRecords.length > 0">
              <view class="record-item" v-for="(record, idx) in currentRepair.repairRecords" :key="idx">
                <view class="record-header-row">
                  <text class="record-title">第{{ idx + 1 }}次维修</text>
                  <u-tag :text="record.repairResult === 'repaired' ? '已修复' : '待复测'" :type="record.repairResult === 'repaired' ? 'success' : 'warning'" size="mini"></u-tag>
                </view>
                <view class="record-info">
                  <text class="record-time">{{ record.repairTime }}</text>
                  <text class="record-person">{{ record.repairPerson }}</text>
                </view>
                <view class="record-detail">
                  <view class="detail-row">
                    <text class="detail-label">维修措施</text>
                    <text class="detail-text">{{ record.repairAction }}</text>
                  </view>
                  <view class="detail-row" v-if="record.replacedParts">
                    <text class="detail-label">更换部件</text>
                    <text class="detail-text">{{ record.replacedParts }}</text>
                  </view>
                  <view class="detail-row" v-if="record.repairNote">
                    <text class="detail-label">备注</text>
                    <text class="detail-text">{{ record.repairNote }}</text>
                  </view>
                </view>
              </view>
            </view>
            <view v-else class="empty-record">
              <text class="empty-icon">🔧</text>
              <text class="empty-text">暂无维修记录</text>
            </view>
          </view>

          <view class="recheck-card" v-if="currentRepair.recheckResult">
            <view class="card-header-row">
              <view class="card-label">复测结果</view>
              <view class="recheck-result-badge" :class="currentRepair.recheckResult === 'pass' ? 'result-pass' : 'result-fail'">
                <text class="result-icon">{{ currentRepair.recheckResult === 'pass' ? '✓' : '✕' }}</text>
                <text class="result-text">{{ currentRepair.recheckResult === 'pass' ? '复测合格' : '复测不合格' }}</text>
              </view>
            </view>
            <view class="recheck-detail">
              <view class="recheck-row">
                <text class="recheck-label">复测人</text>
                <text class="recheck-value">{{ currentRepair.recheckPerson }}</text>
              </view>
              <view class="recheck-row">
                <text class="recheck-label">复测时间</text>
                <text class="recheck-value">{{ currentRepair.recheckTime }}</text>
              </view>
              <view class="recheck-row-full">
                <text class="recheck-label">复测结论</text>
                <text class="recheck-desc">{{ currentRepair.recheckConclusion }}</text>
              </view>
            </view>
          </view>
          <view class="recheck-card empty" v-else>
            <view class="card-label">复测结果</view>
            <view class="empty-recheck">
              <text class="empty-icon">✅</text>
              <text class="empty-text">等待复测</text>
            </view>
          </view>
        </scroll-view>

        <view class="detail-footer" v-if="currentRepair.status === 0">
          <u-button type="primary" @click="startRepair" customStyle="flex:1; height: 80rpx; border-radius: 40rpx;">
            领取维修
          </u-button>
        </view>
        <view class="detail-footer" v-if="currentRepair.status === 1">
          <u-button type="warning" @click="handleStartRepair" customStyle="flex:1; height: 80rpx; border-radius: 40rpx;">
            开始维修
          </u-button>
        </view>
        <view class="detail-footer" v-if="currentRepair.status === 2">
          <u-button type="success" @click="finishRepair" customStyle="flex:1; height: 80rpx; border-radius: 40rpx;">
            完成维修
          </u-button>
        </view>
        <view class="detail-footer" v-if="currentRepair.status === 3">
          <u-button type="warning" @click="startRecheck" customStyle="flex:1; height: 80rpx; border-radius: 40rpx;">
            开始验收
          </u-button>
        </view>
      </view>
    </u-popup>

    <u-popup v-if="repairFormVisible" :show="repairFormVisible" mode="bottom" round="24" closeable @close="repairFormVisible = false" customStyle="height: 85vh;">
      <view class="form-popup">
        <view class="form-header">
          <text class="form-title">新增维修工单</text>
        </view>

        <scroll-view scroll-y class="form-scroll">
          <u-form :model="repairForm" labelPosition="left" labelWidth="180rpx">
            <u-form-item label="设备编号">
              <u-input v-model="repairForm.equipmentNo" placeholder="输入设备编号" @click="scanSnCode"></u-input>
            </u-form-item>
            <u-form-item label="设备名称">
              <u-input v-model="repairForm.equipmentName" placeholder="输入设备名称"></u-input>
            </u-form-item>
            <u-form-item label="故障类型">
              <u-input v-model="repairForm.faultType" placeholder="如：设备过热、运行异常"></u-input>
            </u-form-item>
            <u-form-item label="故障描述">
              <u-textarea v-model="repairForm.faultDesc" placeholder="详细描述故障现象"></u-textarea>
            </u-form-item>
            <u-form-item label="优先级">
              <u-radio-group :value="repairForm.priority" @change="(val) => repairForm.priority = val">
                <u-radio label="紧急" value="major"></u-radio>
                <u-radio label="一般" value="minor"></u-radio>
                <u-radio label="轻微" value="warning"></u-radio>
              </u-radio-group>
            </u-form-item>
            <u-form-item label="报修人">
              <u-input v-model="repairForm.reporter" placeholder="输入报修人"></u-input>
            </u-form-item>
          </u-form>
        </scroll-view>

        <view class="form-footer">
          <u-button type="error" :loading="submitting" @click="submitRepair" customStyle="flex:1; height: 88rpx; border-radius: 44rpx;">
            提交维修工单
          </u-button>
        </view>
      </view>
    </u-popup>

    <u-popup v-if="finishFormVisible" :show="finishFormVisible" mode="bottom" round="24" closeable @close="finishFormVisible = false" customStyle="height: 70vh;">
      <view class="form-popup">
        <view class="form-header">
          <text class="form-title">完成维修</text>
        </view>

        <scroll-view scroll-y class="form-scroll">
          <u-form :model="finishForm" labelPosition="left" labelWidth="180rpx">
            <u-form-item label="维修内容">
              <u-textarea v-model="finishForm.repairContent" placeholder="描述采取的维修措施"></u-textarea>
            </u-form-item>
            <u-form-item label="使用配件">
              <u-input v-model="finishForm.partsUsed" placeholder="如：更换传感器"></u-input>
            </u-form-item>
            <u-form-item label="维修费用">
              <u-input type="number" v-model="finishForm.repairCost" placeholder="输入维修费用"></u-input>
            </u-form-item>
            <u-form-item label="备注">
              <u-textarea v-model="finishForm.remark" placeholder="其他备注"></u-textarea>
            </u-form-item>
          </u-form>
        </scroll-view>

        <view class="form-footer">
          <u-button type="success" :loading="finishing" @click="submitFinish" customStyle="flex:1; height: 88rpx; border-radius: 44rpx;">
            确认完成维修
          </u-button>
        </view>
      </view>
    </u-popup>

    <u-popup v-if="recheckFormVisible" :show="recheckFormVisible" mode="bottom" round="24" closeable @close="recheckFormVisible = false" customStyle="height: 70vh;">
      <view class="form-popup">
        <view class="form-header">
          <text class="form-title">复测记录</text>
        </view>

        <scroll-view scroll-y class="form-scroll">
          <u-form :model="recheckForm" labelPosition="left" labelWidth="180rpx">
            <u-form-item label="验收结果">
              <u-radio-group :value="recheckForm.acceptanceResult" @change="onRecheckResultChange">
                <u-radio label="通过" value="pass"></u-radio>
                <u-radio label="不通过" value="fail"></u-radio>
              </u-radio-group>
            </u-form-item>
            <u-form-item label="验收人">
              <u-input v-model="recheckForm.acceptanceBy" placeholder="输入验收人"></u-input>
            </u-form-item>
            <u-form-item label="验收意见">
              <u-textarea v-model="recheckForm.remark" placeholder="描述验收结果"></u-textarea>
            </u-form-item>
          </u-form>
        </scroll-view>

        <view class="form-footer">
          <u-button type="warning" :loading="rechecking" @click="submitRecheck" customStyle="flex:1; height: 88rpx; border-radius: 44rpx;">
            提交复测结果
          </u-button>
        </view>
      </view>
    </u-popup>
    </view>
  </view>
</template>

<script>
import { scanCode } from '@/utils/scan'
import DynamicBackground from '@/components/DynamicBackground'
import { getRepairList, getRepairDetail, createRepair, acceptRepair, startRepair, finishRepair, recheckRepair } from '@/api/repair'

export default {
  components: {
    DynamicBackground
  },
  data() {
    return {
      searchKey: '',
      currentStatus: 0,
      statusList: [
        { name: '全部', value: '' },
        { name: '待处理', value: '0' },
        { name: '已接单', value: '1' },
        { name: '维修中', value: '2' },
        { name: '待验收', value: '3' },
        { name: '已验收', value: '4' }
      ],
      searched: false,
      repairList: [],
      detailVisible: false,
      currentRepair: {},
      repairFormVisible: false,
      repairForm: {
        taskNo: '',
        equipmentNo: '',
        equipmentName: '',
        faultDesc: '',
        faultType: '',
        priority: 'minor',
        reporter: '',
        assignee: ''
      },
      finishFormVisible: false,
      finishForm: {
        repairContent: '',
        partsUsed: '',
        repairCost: 0,
        remark: ''
      },
      recheckFormVisible: false,
      recheckForm: {
        recheckResult: '',
        recheckConclusion: ''
      },
      submitting: false,
      finishing: false,
      rechecking: false
    }
  },
  onLoad(options) {
    this.loadData()
    if (options && options.repairNo) {
      this.searchKey = options.repairNo
      this.doSearch()
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
        this.loadData()
        return
      }

      uni.showLoading({ title: '搜索中...' })
      try {
        const res = await getRepairList({ taskNo: this.searchKey })
        uni.hideLoading()
        const list = res.data?.records || res.data || []
        if (list.length > 0) {
          this.viewDetail(list[0])
        } else {
          uni.showToast({ title: '未找到对应工单', icon: 'none' })
        }
      } catch {
        uni.hideLoading()
        uni.showToast({ title: '搜索失败', icon: 'none' })
      }
    },
    getStatusTag(status) {
      const map = { 0: 'error', 1: 'warning', 2: 'primary', 3: 'success', 4: 'success' }
      return map[status] || 'info'
    },
    getStatusText(status) {
      const map = { 0: '待处理', 1: '已接单', 2: '维修中', 3: '待验收', 4: '已验收' }
      return map[status] || '未知'
    },
    getPriorityText(priority) {
      const map = { major: '紧急', minor: '一般', warning: '轻微' }
      return map[priority] || priority || '一般'
    },
    onStatusChange({ index }) {
      this.currentStatus = index
      this.loadData()
    },
    async loadData() {
      this.searched = true
      try {
        const status = this.statusList[this.currentStatus].value
        const params = status ? { status: Number(status) } : {}
        const res = await getRepairList(params)
        this.repairList = res.data?.records || res.data || []
      } catch {
        this.repairList = []
      }
    },
    async viewDetail(item) {
      try {
        const res = await getRepairDetail(item.id)
        this.currentRepair = res.data || item
      } catch {
        this.currentRepair = item
      }
      this.detailVisible = true
    },
    createRepair() {
      this.repairForm = {
        taskNo: '',
        equipmentNo: '',
        equipmentName: '',
        faultDesc: '',
        faultType: '',
        priority: 'minor',
        reporter: '',
        assignee: ''
      }
      this.repairFormVisible = true
    },
    async scanSnCode() {
      try {
        const code = await scanCode()
        this.repairForm.equipmentNo = code
      } catch {
      }
    },
    async submitRepair() {
      if (!this.repairForm.equipmentNo) {
        uni.showToast({ title: '请输入设备编号', icon: 'none' })
        return
      }
      if (!this.repairForm.faultDesc) {
        uni.showToast({ title: '请输入故障描述', icon: 'none' })
        return
      }

      this.submitting = true
      try {
        await createRepair(this.repairForm)
        this.submitting = false
        this.repairFormVisible = false
        uni.showToast({ title: '提交成功', icon: 'success' })
        this.loadData()
      } catch {
        this.submitting = false
      }
    },
    async startRepair() {
      uni.showModal({
        title: '确认领取',
        content: '确认领取此维修工单？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await acceptRepair(this.currentRepair.id)
              uni.showToast({ title: '已领取维修', icon: 'success' })
              this.loadData()
              this.detailVisible = false
            } catch {
              uni.showToast({ title: '操作失败', icon: 'none' })
            }
          }
        }
      })
    },
    async handleStartRepair() {
      uni.showModal({
        title: '确认开始维修',
        content: '确认开始维修此设备？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await startRepair(this.currentRepair.id)
              uni.showToast({ title: '已开始维修', icon: 'success' })
              this.loadData()
              this.detailVisible = false
            } catch {
              uni.showToast({ title: '操作失败', icon: 'none' })
            }
          }
        }
      })
    },
    finishRepair() {
      this.finishForm = {
        repairContent: '',
        partsUsed: '',
        repairCost: 0,
        remark: ''
      }
      this.finishFormVisible = true
    },
    async submitFinish() {
      if (!this.finishForm.repairContent) {
        uni.showToast({ title: '请输入维修内容', icon: 'none' })
        return
      }

      this.finishing = true
      try {
        await finishRepair(this.currentRepair.id, this.finishForm)
        this.finishing = false
        this.finishFormVisible = false
        uni.showToast({ title: '维修完成', icon: 'success' })
        this.loadData()
        this.detailVisible = false
      } catch {
        this.finishing = false
      }
    },
    startRecheck() {
      this.recheckForm = {
        acceptanceResult: 'pass',
        acceptanceBy: '',
        remark: ''
      }
      this.recheckFormVisible = true
    },
    onRecheckResultChange(val) {
      this.recheckForm.acceptanceResult = val
    },
    async submitRecheck() {
      if (!this.recheckForm.acceptanceResult) {
        uni.showToast({ title: '请选择验收结果', icon: 'none' })
        return
      }
      if (!this.recheckForm.acceptanceBy) {
        uni.showToast({ title: '请输入验收人', icon: 'none' })
        return
      }

      this.rechecking = true
      try {
        await recheckRepair(this.currentRepair.id, this.recheckForm)
        this.rechecking = false
        this.recheckFormVisible = false
        uni.showToast({ title: '验收完成', icon: 'success' })
        this.loadData()
        this.detailVisible = false
      } catch {
        this.rechecking = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.repair-page {
  min-height: 100vh;
  background: #F5F7FA;
  padding-bottom: 160rpx;
  position: relative;
}

.page-content {
  position: relative;
  z-index: 1;
}

.page-header {
  padding: 40rpx 32rpx 32rpx;
  background: linear-gradient(135deg, #FF4D4F 0%, #FF7875 40%, #FF9E9E 80%, #FFCCC7 100%);
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

.scan-section {
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
  background: linear-gradient(135deg, #FF4D4F 0%, #FF7875 100%);
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(255, 77, 79, 0.4);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.95);
    box-shadow: 0 4rpx 12rpx rgba(255, 77, 79, 0.3);
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

.filter-bar {
  margin: 24rpx;
  background: #ffffff;
  border-radius: 28rpx;
  padding: 8rpx 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.repair-list {
  padding: 0 24rpx;
}

.repair-card {
  background: #ffffff;
  border-radius: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
  overflow: hidden;
  border: 1rpx solid rgba(0, 0, 0, 0.04);
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:active {
    transform: scale(0.99);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  }
}

.card-status-bar {
  height: 8rpx;
  width: 100%;

  &.status-1 { background: linear-gradient(90deg, #FAAD14, #FFC53D); }
  &.status-2 { background: linear-gradient(90deg, #3B7CFF, #60A5FA); }
  &.status-3 { background: linear-gradient(90deg, #722ED1, #9254DE); }
  &.status-4 { background: linear-gradient(90deg, #52C41A, #73D13D); }
}

.card-content {
  padding: 28rpx;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.repair-no {
  font-size: 32rpx;
  font-weight: 700;
  color: #FF4D4F;
  letter-spacing: 0.5rpx;
}

.sn-code {
  font-size: 24rpx;
  color: #8C8C8C;
  font-family: monospace;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 20rpx;
  border-radius: 20rpx;

  &.badge-warning {
    background: rgba(250, 173, 20, 0.1);
    .status-dot { background: #FAAD14; }
    .status-text { color: #FAAD14; }
  }

  &.badge-primary {
    background: rgba(59, 124, 255, 0.1);
    .status-dot { background: #3B7CFF; }
    .status-text { color: #3B7CFF; }
  }

  &.badge-success {
    background: rgba(82, 196, 26, 0.1);
    .status-dot { background: #52C41A; }
    .status-text { color: #52C41A; }
  }

  &.badge-error {
    background: rgba(255, 77, 79, 0.1);
    .status-dot { background: #FF4D4F; }
    .status-text { color: #FF4D4F; }
  }
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
}

.status-text {
  font-size: 24rpx;
  font-weight: 500;
}

.card-body {
  margin-bottom: 20rpx;
}

.defect-row {
  margin-bottom: 12rpx;
}

.defect-tag {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 20rpx;
  background: linear-gradient(135deg, rgba(255, 77, 79, 0.1) 0%, rgba(255, 120, 117, 0.1) 100%);
  border-radius: 16rpx;
  border: 1rpx solid rgba(255, 77, 79, 0.2);
}

.tag-icon {
  font-size: 24rpx;
}

.tag-text {
  font-size: 26rpx;
  color: #FF4D4F;
  font-weight: 500;
}

.desc-row {
  background: #F8FAFF;
  border-radius: 12rpx;
  padding: 16rpx;
}

.desc-text {
  font-size: 26rpx;
  color: #4A4A4A;
  line-height: 1.6;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  padding-top: 16rpx;
  border-top: 1rpx dashed #E8E8E8;
}

.footer-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.footer-icon {
  font-size: 24rpx;
}

.footer-text {
  font-size: 24rpx;
  color: #8C8C8C;
}

.card-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  margin-top: 20rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #F5F5F5;
}

.action-text {
  font-size: 26rpx;
  color: #3B7CFF;
  font-weight: 500;
}

.action-arrow {
  font-size: 32rpx;
  color: #3B7CFF;
}

.bottom-action {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 24rpx;
  background: #ffffff;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.detail-popup,
.form-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.detail-header {
  padding: 30rpx;
  border-bottom: 1rpx solid #F5F5F5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  flex: 1;
}

.detail-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #1F1F1F;
  display: block;
}

.detail-no {
  font-size: 26rpx;
  color: #8C8C8C;
  margin-top: 8rpx;
  display: block;
  font-family: monospace;
}

.status-badge-large {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  border-radius: 24rpx;

  &.badge-warning {
    background: rgba(250, 173, 20, 0.1);
    .status-dot { background: #FAAD14; }
    .status-text { color: #FAAD14; }
  }

  &.badge-primary {
    background: rgba(59, 124, 255, 0.1);
    .status-dot { background: #3B7CFF; }
    .status-text { color: #3B7CFF; }
  }

  &.badge-success {
    background: rgba(82, 196, 26, 0.1);
    .status-dot { background: #52C41A; }
    .status-text { color: #52C41A; }
  }

  &.badge-error {
    background: rgba(255, 77, 79, 0.1);
    .status-dot { background: #FF4D4F; }
    .status-text { color: #FF4D4F; }
  }
}

.form-header {
  padding: 30rpx;
  border-bottom: 1rpx solid #F5F5F5;
  text-align: center;
}

.form-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1F1F1F;
  display: block;
}

.detail-scroll,
.form-scroll {
  flex: 1;
  padding: 60rpx 20rpx 20rpx;
}

.info-card,
.defect-card,
.record-card,
.recheck-card {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.card-label {
  font-size: 28rpx;
  font-weight: 700;
  color: #1F1F1F;
  margin-bottom: 16rpx;
  padding-left: 12rpx;
  border-left: 6rpx solid #FF4D4F;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.info-row-sn {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx;
  background: linear-gradient(135deg, rgba(59, 124, 255, 0.08) 0%, rgba(59, 124, 255, 0.12) 100%);
  border-radius: 12rpx;
  border: 1rpx solid rgba(59, 124, 255, 0.2);
  margin-bottom: 16rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F0F0F0;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 26rpx;
  color: #8C8C8C;
}

.info-value {
  font-size: 28rpx;
  color: #1F1F1F;
  font-weight: 500;
}

.info-value-sn {
  font-size: 28rpx;
  font-weight: 700;
  color: #3B7CFF;
  font-family: monospace;
}

.defect-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #FFF7F7 100%);
}

.defect-type-tag {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 20rpx;
  background: linear-gradient(135deg, rgba(255, 77, 79, 0.1) 0%, rgba(255, 120, 117, 0.1) 100%);
  border-radius: 16rpx;
  border: 1rpx solid rgba(255, 77, 79, 0.2);
}

.tag-icon {
  font-size: 24rpx;
}

.tag-text {
  font-size: 26rpx;
  color: #FF4D4F;
  font-weight: 600;
}

.defect-desc-box {
  background: #FFFFFF;
  border-radius: 12rpx;
  padding: 16rpx;
  margin-bottom: 20rpx;
}

.defect-desc {
  font-size: 26rpx;
  color: #4A4A4A;
  line-height: 1.6;
}

.defect-meta {
  display: flex;
  justify-content: space-between;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-icon {
  font-size: 24rpx;
}

.meta-label {
  font-size: 24rpx;
  color: #8C8C8C;
}

.meta-value {
  font-size: 26rpx;
  color: #1F1F1F;
  font-weight: 500;
}

.record-timeline {
  position: relative;
  padding-left: 40rpx;
}

.timeline-line {
  position: absolute;
  left: 15rpx;
  top: 0;
  bottom: 0;
  width: 4rpx;
  background: #E8E8E8;
}

.record-item {
  position: relative;
  margin-bottom: 28rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.timeline-dot {
  position: absolute;
  left: -35rpx;
  top: 4rpx;
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: #E8E8E8;
  border: 4rpx solid #FFFFFF;
  box-shadow: 0 0 0 4rpx #E8E8E8;

  &.dot-success {
    background: #52C41A;
    box-shadow: 0 0 0 4rpx rgba(82, 196, 26, 0.2);
  }

  &.dot-warning {
    background: #FAAD14;
    box-shadow: 0 0 0 4rpx rgba(250, 173, 20, 0.2);
  }
}

.record-content {
  background: #F8FAFF;
  border-radius: 16rpx;
  padding: 20rpx;
}

.record-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.record-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F1F1F;
}

.record-info {
  display: flex;
  gap: 20rpx;
  margin-bottom: 16rpx;
}

.record-time {
  font-size: 24rpx;
  color: #8C8C8C;
}

.record-person {
  font-size: 24rpx;
  color: #3B7CFF;
  font-weight: 500;
}

.record-detail {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.detail-row {
  display: flex;
  gap: 16rpx;
}

.detail-label {
  font-size: 24rpx;
  color: #8C8C8C;
  flex-shrink: 0;
}

.detail-text {
  font-size: 26rpx;
  color: #4A4A4A;
  flex: 1;
  line-height: 1.5;
}

.empty-record,
.empty-recheck {
  text-align: center;
  padding: 48rpx;
}

.empty-icon {
  font-size: 64rpx;
  display: block;
  margin-bottom: 16rpx;
}

.empty-text {
  font-size: 26rpx;
  color: #8C8C8C;
}

.recheck-card {
  background: linear-gradient(145deg, #FFFFFF 0%, #F6FFED 100%);

  &.empty {
    background: #FFFFFF;
  }
}

.recheck-result-badge {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  border-radius: 24rpx;

  &.result-pass {
    background: rgba(82, 196, 26, 0.15);
    .result-icon, .result-text { color: #52C41A; }
  }

  &.result-fail {
    background: rgba(255, 77, 79, 0.15);
    .result-icon, .result-text { color: #FF4D4F; }
  }
}

.result-icon {
  font-size: 24rpx;
  font-weight: 700;
}

.result-text {
  font-size: 26rpx;
  font-weight: 600;
}

.recheck-detail {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.recheck-row {
  display: flex;
  justify-content: space-between;
}

.recheck-label {
  font-size: 26rpx;
  color: #8C8C8C;
}

.recheck-value {
  font-size: 26rpx;
  color: #1F1F1F;
  font-weight: 500;
}

.recheck-row-full {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.recheck-desc {
  font-size: 26rpx;
  color: #4A4A4A;
  line-height: 1.6;
}

.detail-footer,
.form-footer {
  padding: 20rpx 30rpx;
  border-top: 1rpx solid #F5F5F5;
}

.photo-section {
  margin-top: 24rpx;
}

.photo-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 12rpx;
}

.upload-item {
  width: 180rpx;
  height: 180rpx;
  position: relative;
  border-radius: 16rpx;
  overflow: hidden;
}

.upload-image {
  width: 100%;
  height: 100%;
}

.upload-delete {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 40rpx;
  height: 40rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  color: #ffffff;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-btn {
  width: 180rpx;
  height: 180rpx;
  border: 2rpx dashed #D9D9D9;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-icon {
  font-size: 48rpx;
  color: #D9D9D9;
}

.upload-text {
  font-size: 24rpx;
  color: #D9D9D9;
  margin-top: 8rpx;
}
</style>