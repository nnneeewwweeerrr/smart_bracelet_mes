<template>
  <div class="page-container">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="item in statCards" :key="item.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: item.color }">
              <el-icon :size="28"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.title }}</div>
              <div class="stat-trend" :class="item.trendClass">
                <el-icon><component :is="item.trendIcon" /></el-icon>
                <span>{{ item.trend }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="工单号">
          <el-input v-model="searchForm.workOrderNo" placeholder="请输入工单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入产品名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="工单状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="searchForm.manager" placeholder="请输入负责人" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleRefresh">
            <el-icon><Promotion /></el-icon>
            刷新数据
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="progress-card" shadow="never" v-loading="loading">
      <div class="card-header">
        <span class="card-title">生产进度监控</span>
        <div class="header-right">
          <span class="conn-status" :class="{ connected: wsConnected }">
            <span class="status-dot"></span>
            {{ wsConnected ? '实时连接中' : '连接断开' }}
          </span>
          <span class="update-time">
            <el-icon><Clock /></el-icon>
            数据更新：{{ lastUpdateTime }}
          </span>
        </div>
      </div>

      <el-empty v-if="filteredData.length === 0" description="暂无生产数据" style="padding: 60px 0" />

      <div v-else class="progress-list">
        <div
          v-for="item in filteredData"
          :key="item.id"
          class="progress-item"
          :class="{ 'item-expanded': expandedIds.includes(item.id), 'flash': flashId === item.id }"
        >
          <div class="item-header" @click="toggleExpand(item.id)">
            <div class="item-left">
              <el-icon class="expand-icon">
                <component :is="expandedIds.includes(item.id) ? 'ArrowDown' : 'ArrowRight'" />
              </el-icon>
              <div class="item-title">
                <span class="work-order-no">{{ item.workOrderNo }}</span>
                <el-tag :type="getStatusType(item.status)" size="small" class="status-tag">
                  {{ getStatusText(item.status) }}
                </el-tag>
              </div>
              <span class="product-name">{{ item.productName }}</span>
              <span class="spec">{{ item.spec }}</span>
            </div>
            <div class="item-right">
              <div class="quantity-info">
                <span class="finished">{{ item.finishedQuantity }}</span>
                <span class="divider">/</span>
                <span class="total">{{ item.quantity }}</span>
              </div>
              <el-progress
                :percentage="Math.round(item.finishedQuantity / item.quantity * 100)"
                :stroke-width="12"
                :text-inside="false"
                :status="getProgressStatus(item.status)"
                style="width: 180px"
              />
            </div>
          </div>

          <div v-if="expandedIds.includes(item.id)" class="item-detail">
            <el-descriptions :column="4" border size="small" class="detail-desc">
              <el-descriptions-item label="关联订单">{{ item.orderNo }}</el-descriptions-item>
              <el-descriptions-item label="负责人">{{ item.manager }}</el-descriptions-item>
              <el-descriptions-item label="计划开始">{{ item.planStartDate }}</el-descriptions-item>
              <el-descriptions-item label="计划完成">{{ item.planEndDate }}</el-descriptions-item>
            </el-descriptions>

            <div class="process-section">
              <div class="section-title">
                <el-icon><List /></el-icon>
                工序进度
              </div>
              <el-table :data="item.processList || []" border stripe size="small" style="width: 100%">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="processName" label="工序名称" min-width="140" />
                <el-table-column prop="equipment" label="生产设备" width="160" />
                <el-table-column label="进度" min-width="200">
                  <template #default="{ row }">
                    <div class="process-progress">
                      <el-progress
                        :percentage="Math.round(row.finishedQuantity / row.quantity * 100)"
                        :stroke-width="10"
                        :text-inside="true"
                        :status="row.status === 'finished' ? 'success' : row.status === 'processing' ? '' : 'exception'"
                        size="small"
                      />
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="finishedQuantity" label="完成/计划" width="120" align="center">
                  <template #default="{ row }">
                    {{ row.finishedQuantity }} / {{ row.quantity }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag v-if="row.status === 'pending'" type="info" size="small">待开始</el-tag>
                    <el-tag v-else-if="row.status === 'processing'" type="primary" size="small">
                      <el-icon class="loading-icon"><Loading /></el-icon>
                      进行中
                    </el-tag>
                    <el-tag v-else type="success" size="small">已完成</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100" align="center">
                  <template #default="{ row }">
                    <el-button
                      v-if="row.status === 'pending'"
                      type="primary"
                      link
                      size="small"
                      @click.stop="handleStartProcess(item, row)"
                    >
                      开始工序
                    </el-button>
                    <el-button
                      v-else-if="row.status === 'processing'"
                      type="success"
                      link
                      size="small"
                      @click.stop="handleFinishProcess(item, row)"
                    >
                      完成工序
                    </el-button>
                    <span v-else>-</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div v-if="item.remark" class="remark-section">
              <span class="remark-label">备注：</span>
              <span class="remark-content">{{ item.remark }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import wsClient from '../../utils/websocket'
import request from '../../utils/request'

const loading = ref(false)
const lastUpdateTime = ref('')
const expandedIds = ref([])
const wsConnected = ref(false)
const flashId = ref(null)

const searchForm = reactive({
  workOrderNo: '',
  productName: '',
  status: '',
  manager: ''
})

const statusOptions = [
  { label: '待生产', value: 0 },
  { label: '生产中', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 }
]

const statCards = ref([
  { title: '今日产量', value: 1280, icon: 'DataLine', color: '#93c5dd', trend: '+12%', trendIcon: 'Top', trendClass: 'up' },
  { title: '生产中工单', value: 8, icon: 'Setting', color: '#83d8cf', trend: '+2', trendIcon: 'Top', trendClass: 'up' },
  { title: '待开工单', value: 5, icon: 'Clock', color: '#fae1a4', trend: '-1', trendIcon: 'Bottom', trendClass: 'down' },
  { title: '异常工单', value: 1, icon: 'Warning', color: '#f1a7b8', trend: '0', trendIcon: 'Minus', trendClass: 'normal' }
])

const mockData = ref([
  {
    id: 1,
    workOrderNo: 'WO-2024001',
    orderNo: 'ORD-2024003',
    productName: '智能健康手环 Pro',
    spec: 'SH-P001',
    quantity: 1000,
    finishedQuantity: 650,
    status: 1,
    planStartDate: '2024-01-15',
    planEndDate: '2024-01-25',
    manager: '李主管',
    createTime: '2024-01-14 10:00:00',
    remark: '紧急订单，优先排产',
    processList: [
      { processName: 'SMT贴片', equipment: 'SMT贴片机01', quantity: 1000, finishedQuantity: 1000, status: 'finished' },
      { processName: '回流焊接', equipment: '回流焊机01', quantity: 1000, finishedQuantity: 1000, status: 'finished' },
      { processName: '组装', equipment: '自动组装线01', quantity: 1000, finishedQuantity: 650, status: 'processing' },
      { processName: '测试', equipment: '测试台01', quantity: 1000, finishedQuantity: 0, status: 'pending' },
      { processName: '包装', equipment: '自动包装机01', quantity: 1000, finishedQuantity: 0, status: 'pending' }
    ]
  },
  {
    id: 2,
    workOrderNo: 'WO-2024002',
    orderNo: 'ORD-2024002',
    productName: '智能运动手环 Lite',
    spec: 'SH-L002',
    quantity: 500,
    finishedQuantity: 500,
    status: 2,
    planStartDate: '2024-01-10',
    planEndDate: '2024-01-18',
    manager: '王主管',
    createTime: '2024-01-09 14:30:00',
    remark: '',
    processList: [
      { processName: 'SMT贴片', equipment: 'SMT贴片机01', quantity: 500, finishedQuantity: 500, status: 'finished' },
      { processName: '回流焊接', equipment: '回流焊机01', quantity: 500, finishedQuantity: 500, status: 'finished' },
      { processName: '组装', equipment: '自动组装线01', quantity: 500, finishedQuantity: 500, status: 'finished' },
      { processName: '测试', equipment: '测试台01', quantity: 500, finishedQuantity: 500, status: 'finished' },
      { processName: '包装', equipment: '自动包装机01', quantity: 500, finishedQuantity: 500, status: 'finished' }
    ]
  },
  {
    id: 3,
    workOrderNo: 'WO-2024003',
    orderNo: 'ORD-2024001',
    productName: '智能健康手环 Pro',
    spec: 'SH-P001',
    quantity: 800,
    finishedQuantity: 0,
    status: 0,
    planStartDate: '2024-01-20',
    planEndDate: '2024-01-30',
    manager: '张主管',
    createTime: '2024-01-17 09:00:00',
    remark: '新订单排产',
    processList: [
      { processName: 'SMT贴片', equipment: 'SMT贴片机02', quantity: 800, finishedQuantity: 0, status: 'pending' },
      { processName: '回流焊接', equipment: '回流焊机02', quantity: 800, finishedQuantity: 0, status: 'pending' },
      { processName: '组装', equipment: '自动组装线01', quantity: 800, finishedQuantity: 0, status: 'pending' },
      { processName: '测试', equipment: '测试台02', quantity: 800, finishedQuantity: 0, status: 'pending' },
      { processName: '包装', equipment: '自动包装机02', quantity: 800, finishedQuantity: 0, status: 'pending' }
    ]
  },
  {
    id: 4,
    workOrderNo: 'WO-2024004',
    orderNo: 'ORD-2024005',
    productName: '智能运动手环 Lite',
    spec: 'SH-L002',
    quantity: 600,
    finishedQuantity: 200,
    status: 1,
    planStartDate: '2024-01-16',
    planEndDate: '2024-01-26',
    manager: '赵主管',
    createTime: '2024-01-15 16:00:00',
    remark: '',
    processList: [
      { processName: 'SMT贴片', equipment: 'SMT贴片机02', quantity: 600, finishedQuantity: 600, status: 'finished' },
      { processName: '回流焊接', equipment: '回流焊机02', quantity: 600, finishedQuantity: 600, status: 'finished' },
      { processName: '组装', equipment: '自动组装线02', quantity: 600, finishedQuantity: 200, status: 'processing' },
      { processName: '测试', equipment: '测试台02', quantity: 600, finishedQuantity: 0, status: 'pending' },
      { processName: '包装', equipment: '自动包装机01', quantity: 600, finishedQuantity: 0, status: 'pending' }
    ]
  },
  {
    id: 5,
    workOrderNo: 'WO-2024005',
    orderNo: '-',
    productName: '智能手表 Max',
    spec: 'SW-M003',
    quantity: 200,
    finishedQuantity: 0,
    status: 3,
    planStartDate: '2024-01-12',
    planEndDate: '2024-01-22',
    manager: '李主管',
    createTime: '2024-01-11 11:00:00',
    remark: '设计变更，取消工单',
    processList: []
  },
  {
    id: 6,
    workOrderNo: 'WO-2024006',
    orderNo: 'ORD-2024006',
    productName: '智能健康手环 Pro',
    spec: 'SH-P001',
    quantity: 1500,
    finishedQuantity: 1200,
    status: 1,
    planStartDate: '2024-01-14',
    planEndDate: '2024-01-24',
    manager: '张主管',
    createTime: '2024-01-13 08:00:00',
    remark: '',
    processList: [
      { processName: 'SMT贴片', equipment: 'SMT贴片机01', quantity: 1500, finishedQuantity: 1500, status: 'finished' },
      { processName: '回流焊接', equipment: '回流焊机01', quantity: 1500, finishedQuantity: 1500, status: 'finished' },
      { processName: '组装', equipment: '自动组装线01', quantity: 1500, finishedQuantity: 1200, status: 'processing' },
      { processName: '测试', equipment: '测试台01', quantity: 1500, finishedQuantity: 300, status: 'processing' },
      { processName: '包装', equipment: '自动包装机01', quantity: 1500, finishedQuantity: 0, status: 'pending' }
    ]
  }
])

const workOrderData = ref([])

const filteredData = computed(() => {
  let result = [...workOrderData.value]
  if (searchForm.workOrderNo) {
    result = result.filter(item => item.workOrderNo && item.workOrderNo.includes(searchForm.workOrderNo))
  }
  if (searchForm.productName) {
    result = result.filter(item => item.productName && item.productName.includes(searchForm.productName))
  }
  if (searchForm.status !== '') {
    result = result.filter(item => item.status === searchForm.status)
  }
  if (searchForm.manager) {
    result = result.filter(item => item.manager && item.manager.includes(searchForm.manager))
  }
  return result
})

function getStatusType(status) {
  const types = ['info', 'primary', 'success', 'danger']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['待生产', '生产中', '已完成', '已取消']
  return texts[status] || '未知'
}

function getProgressStatus(status) {
  if (status === 2) return 'success'
  if (status === 3) return 'exception'
  return ''
}

function toggleExpand(id) {
  const index = expandedIds.value.indexOf(id)
  if (index === -1) {
    expandedIds.value.push(id)
  } else {
    expandedIds.value.splice(index, 1)
  }
}

async function fetchWorkOrders() {
  loading.value = true
  try {
    const res = await request.get('/production-progress/list')
    if (res.data) {
      workOrderData.value = res.data || []
      updateStatCards()
    }
  } catch (error) {
    console.error('Fetch work orders failed:', error)
    workOrderData.value = []
  } finally {
    loading.value = false
  }
}

function updateStatCards() {
  const list = workOrderData.value
  statCards.value[1].value = list.filter(i => i.status === 1).length
  statCards.value[2].value = list.filter(i => i.status === 0).length
  statCards.value[3].value = list.filter(i => i.status === 3).length || list.filter(i => i.status === 4).length
}

function handleSearch() {
  updateLastUpdateTime()
}

function handleReset() {
  searchForm.workOrderNo = ''
  searchForm.productName = ''
  searchForm.status = ''
  searchForm.manager = ''
  updateLastUpdateTime()
}

async function handleRefresh() {
  await fetchWorkOrders()
  updateLastUpdateTime()
  ElMessage.success('数据已刷新')
}

function handleStartProcess(workOrder, process) {
  ElMessage.success(`工序「${process.processName}」已开始`)
  process.status = 'processing'
}

function handleFinishProcess(workOrder, process) {
  ElMessage.success(`工序「${process.processName}」已完成`)
  process.status = 'finished'
  process.finishedQuantity = process.quantity
}

function updateLastUpdateTime() {
  lastUpdateTime.value = new Date().toLocaleString('zh-CN', { hour12: false })
}

function handleProgressUpdate(data) {
  fetchWorkOrders()
  flashId.value = data.workOrderId
  setTimeout(() => { flashId.value = null }, 2000)
  updateLastUpdateTime()
  ElNotification({
    title: '生产进度更新',
    message: `工单 ${data.workOrderNo} 在 ${data.processName} 工序新增报工 ${data.addQuantity} 件`,
    type: 'success',
    duration: 4000
  })
}

onMounted(() => {
  updateLastUpdateTime()
  fetchWorkOrders()
  wsClient.connect()
  wsConnected.value = true
  wsClient.on('PROGRESS_UPDATE', handleProgressUpdate)
})

onUnmounted(() => {
  wsClient.off('PROGRESS_UPDATE', handleProgressUpdate)
})
</script>

<style scoped>
.page-container {
  width: 100%;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.stat-trend {
  font-size: 12px;
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.stat-trend.up {
  color: #83d8cf;
}

.stat-trend.down {
  color: #f1a7b8;
}

.stat-trend.normal {
  color: #909399;
}

.search-card {
  margin-bottom: 20px;
}

.progress-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.update-time {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.progress-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.progress-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.progress-item.flash {
  animation: flashBorder 2s ease-out;
}

@keyframes flashBorder {
  0% {
    border-color: #83d8cf;
    box-shadow: 0 0 20px rgba(131, 216, 207, 0.3);
  }
  100% {
    border-color: #e4e7ed;
    box-shadow: none;
  }
}

.conn-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}

.conn-status .status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #f1a7b8;
}

.conn-status.connected {
  color: #83d8cf;
}

.conn-status.connected .status-dot {
  background-color: #83d8cf;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.progress-item:hover {
  border-color: #93c5dd;
  box-shadow: 0 2px 12px 0 rgba(147, 197, 221, 0.2);
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background-color: #fafafa;
  cursor: pointer;
  user-select: none;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.expand-icon {
  color: #909399;
  font-size: 16px;
  transition: transform 0.3s;
}

.item-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.work-order-no {
  font-size: 15px;
  font-weight: bold;
  color: #303133;
}

.status-tag {
  margin-left: 0;
}

.product-name {
  font-size: 14px;
  color: #606266;
  margin-left: 20px;
}

.spec {
  font-size: 13px;
  color: #909399;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.quantity-info {
  font-size: 14px;
  color: #606266;
}

.quantity-info .finished {
  font-size: 18px;
  font-weight: bold;
  color: #93c5dd;
}

.quantity-info .divider {
  margin: 0 4px;
  color: #c0c4cc;
}

.quantity-info .total {
  color: #909399;
}

.item-detail {
  padding: 20px;
  border-top: 1px solid #e4e7ed;
  background-color: #fff;
}

.detail-desc {
  margin-bottom: 20px;
}

.process-section {
  margin-bottom: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.item-right :deep(.el-progress__text) {
  color: #909399 !important;
}

.process-progress {
  padding: 0 10px;
}

.process-progress :deep(.el-progress-bar__innerText) {
  color: #606266 !important;
  font-size: 11px;
  font-weight: bold;
  line-height: 16px;
}

.process-progress :deep(.el-progress-bar__outer) {
  height: 16px !important;
}

.process-progress :deep(.el-progress-bar__inner) {
  height: 16px !important;
}

.loading-icon {
  animation: rotate 1.5s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.remark-section {
  padding-top: 12px;
  border-top: 1px dashed #e4e7ed;
  font-size: 13px;
  color: #606266;
}

.remark-label {
  color: #909399;
}
</style>
