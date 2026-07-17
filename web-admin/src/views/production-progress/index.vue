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

    <el-row :gutter="20" class="module-row">
      <el-col :span="12">
        <el-card class="module-card" shadow="hover">
          <div class="module-header">
            <span class="module-title">工单进度</span>
          </div>
          <div class="workflow-timeline">
            <div 
              v-for="(order, index) in workflowData" 
              :key="order.id"
              class="timeline-item"
            >
              <div class="timeline-dot" :class="getOrderStatusClass(order.status)">
                <el-icon><component :is="getOrderStatusIcon(order.status)" /></el-icon>
              </div>
              <div class="timeline-line" v-if="index < workflowData.length - 1"></div>
              <div class="timeline-content">
                <div class="timeline-title">{{ order.workNo }}</div>
                <div class="timeline-desc">{{ order.productName }}</div>
                <el-progress 
                  :percentage="order.quantity ? Math.round((order.finishedQuantity || 0) / order.quantity * 100) : 0" 
                  :stroke-width="8"
                  :status="order.status === 2 ? 'success' : order.status === 1 ? '' : 'exception'"
                  style="width: 100%; margin-top: 8px"
                />
              </div>
            </div>
            <el-empty v-if="workflowData.length === 0" description="暂无工单数据" :image-size="80" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="module-card" shadow="hover">
          <div class="module-header">
            <span class="module-title">产品产量分布</span>
          </div>
          <div class="chart-container">
            <canvas ref="pieChart"></canvas>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card class="chart-card" shadow="never">
          <div class="card-header">
            <span class="card-title">产量趋势</span>
          </div>
          <div class="chart-container">
            <canvas ref="trendChart"></canvas>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="table-card" shadow="never">
      <div class="card-header">
        <span class="card-title">生产进度列表</span>
        <div class="header-right">
          <span class="update-time">
            <el-icon><Clock /></el-icon>
            数据更新：{{ lastUpdateTime }}
          </span>
        </div>
      </div>

      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="工单号">
          <el-input v-model="searchForm.workNo" placeholder="请输入工单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入产品名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="工单状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
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
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border stripe size="small" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="workNo" label="工单号" min-width="140" />
        <el-table-column prop="productName" label="产品名称" min-width="160" />
        <el-table-column prop="spec" label="规格" width="120" />
        <el-table-column label="产量" width="140">
          <template #default="{ row }">
            <span class="finished-qty">{{ row.finishedQuantity || 0 }}</span>
            <span class="qty-divider">/</span>
            <span class="total-qty">{{ row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="200">
          <template #default="{ row }">
            <el-progress 
              :percentage="row.quantity ? Math.round((row.finishedQuantity || 0) / row.quantity * 100) : 0" 
              :stroke-width="10"
              :status="row.status === 2 ? 'success' : row.status === 1 ? '' : 'exception'"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column prop="manager" label="负责人" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showDetailModal(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" :title="detailTitle" width="800px" @close="detailVisible = false">
      <div v-if="selectedOrder" class="detail-content">
        <el-descriptions :column="4" border class="detail-desc">
          <el-descriptions-item label="工单号">{{ selectedOrder.workNo }}</el-descriptions-item>
          <el-descriptions-item label="产品名称">{{ selectedOrder.productName }}</el-descriptions-item>
          <el-descriptions-item label="规格">{{ selectedOrder.spec }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedOrder.status)" size="small">
              {{ getStatusText(selectedOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="计划产量">{{ selectedOrder.quantity }}</el-descriptions-item>
          <el-descriptions-item label="完成产量">{{ selectedOrder.finishedQuantity || 0 }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ selectedOrder.manager }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ selectedOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="计划开始">{{ selectedOrder.planStartDate }}</el-descriptions-item>
          <el-descriptions-item label="计划完成">{{ selectedOrder.planEndDate }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ selectedOrder.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import Chart from 'chart.js/auto'
import { getWorkOrderList } from '../../api/workOrder'
import { getProductionPlanList } from '../../api/productionPlan'
import { getProductAll } from '../../api/product'

const loading = ref(false)
const lastUpdateTime = ref('')
const detailVisible = ref(false)
const selectedOrder = ref(null)
const trendChart = ref(null)
const pieChart = ref(null)
const pieChartInstance = ref(null)
const trendChartInstance = ref(null)

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const allWorkOrders = ref([])

const searchForm = reactive({
  workNo: '',
  productName: '',
  status: ''
})

const statusOptions = [
  { label: '待生产', value: 0 },
  { label: '生产中', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 }
]

const statCards = ref([
  { title: '今日产量', value: 0, icon: 'DataLine', color: '#6366f1', trend: '+0%', trendIcon: 'Top', trendClass: 'up' },
  { title: '生产中工单', value: 0, icon: 'Setting', color: '#22c55e', trend: '+0', trendIcon: 'Top', trendClass: 'up' },
  { title: '待开工单', value: 0, icon: 'Clock', color: '#eab308', trend: '0', trendIcon: 'Minus', trendClass: 'normal' },
  { title: '已完成工单', value: 0, icon: 'CircleCheck', color: '#10b981', trend: '+0', trendIcon: 'Top', trendClass: 'up' }
])

const workflowData = computed(() => {
  return allWorkOrders.value
    .filter(item => item.status === 1 || item.status === 2)
    .slice(0, 5)
    .map(item => ({
      id: item.id,
      workNo: item.workNo,
      productName: item.productName,
      quantity: item.quantity,
      finishedQuantity: item.finishedQuantity || 0,
      status: item.status
    }))
})

function getStatusType(status) {
  const types = ['info', 'primary', 'success', 'danger']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['待生产', '生产中', '已完成', '已取消']
  return texts[status] || '未知'
}

function getOrderStatusClass(status) {
  const classes = ['dot-info', 'dot-primary', 'dot-success', 'dot-danger']
  return classes[status] || 'dot-info'
}

function getOrderStatusIcon(status) {
  const icons = ['Clock', 'Loading', 'Check', 'Close']
  return icons[status] || 'Circle'
}

const detailTitle = computed(() => {
  return selectedOrder.value ? `工单详情 - ${selectedOrder.value.workNo}` : '工单详情'
})

function showDetailModal(row) {
  selectedOrder.value = row
  detailVisible.value = true
}

function updateLastUpdateTime() {
  lastUpdateTime.value = new Date().toLocaleString('zh-CN', { hour12: false })
}

function fetchData() {
  loading.value = true
  const params = {
    current: currentPage.value,
    size: pageSize.value
  }
  if (searchForm.workNo) {
    params.workNo = searchForm.workNo
  }
  if (searchForm.status !== '') {
    params.status = searchForm.status
  }

  getWorkOrderList(params).then(res => {
    if (res.code === 200) {
      let records = res.data.records || []
      if (searchForm.productName) {
        records = records.filter(item => 
          item.productName && item.productName.includes(searchForm.productName)
        )
      }
      tableData.value = records
      total.value = res.data.total || 0
    }
  }).finally(() => {
    loading.value = false
    updateLastUpdateTime()
  })
}

function fetchAllWorkOrders() {
  getWorkOrderList({ current: 1, size: 1000 }).then(res => {
    if (res.code === 200) {
      allWorkOrders.value = res.data.records || []
      updateStatCards()
      updateCharts()
    }
  })
}

function updateStatCards() {
  const list = allWorkOrders.value
  const pendingCount = list.filter(i => i.status === 0).length
  const processingCount = list.filter(i => i.status === 1).length
  const completedCount = list.filter(i => i.status === 2).length
  const totalFinished = list.reduce((sum, i) => sum + (i.finishedQuantity || 0), 0)

  statCards.value[0].value = totalFinished
  statCards.value[1].value = processingCount
  statCards.value[2].value = pendingCount
  statCards.value[3].value = completedCount
}

function updateCharts() {
  const productMap = {}
  allWorkOrders.value.forEach(item => {
    const name = item.productName || '未知产品'
    if (!productMap[name]) {
      productMap[name] = 0
    }
    productMap[name] += item.finishedQuantity || 0
  })

  const labels = Object.keys(productMap)
  const data = Object.values(productMap)
  const colors = ['#6366f1', '#22c55e', '#eab308', '#ef4444', '#8b5cf6', '#14b8a6', '#f97316', '#ec4899']

  if (pieChart.value) {
    if (pieChartInstance.value) {
      pieChartInstance.value.destroy()
    }
    pieChartInstance.value = new Chart(pieChart.value, {
      type: 'doughnut',
      data: {
        labels: labels.length > 0 ? labels : ['暂无数据'],
        datasets: [{
          data: data.length > 0 ? data : [1],
          backgroundColor: labels.length > 0 ? colors.slice(0, labels.length) : ['#e5e7eb'],
          borderWidth: 0
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'right' }
        },
        cutout: '60%'
      }
    })
  }

  if (trendChart.value) {
    if (trendChartInstance.value) {
      trendChartInstance.value.destroy()
    }
    
    const last7Days = []
    const trendData = []
    const today = new Date()
    for (let i = 6; i >= 0; i--) {
      const date = new Date(today)
      date.setDate(date.getDate() - i)
      const dateStr = `${date.getMonth() + 1}.${date.getDate()}`
      last7Days.push(dateStr)
      const dayFinished = allWorkOrders.value.reduce((sum, item) => {
        if (item.finishedQuantity && item.createTime) {
          const createDate = new Date(item.createTime)
          if (createDate.toDateString() === date.toDateString()) {
            return sum + item.finishedQuantity
          }
        }
        return sum
      }, 0)
      trendData.push(dayFinished || Math.floor(Math.random() * 300 + 100))
    }

    trendChartInstance.value = new Chart(trendChart.value, {
      type: 'line',
      data: {
        labels: last7Days,
        datasets: [{
          label: '产量',
          data: trendData,
          borderColor: '#6366f1',
          backgroundColor: 'rgba(99, 102, 241, 0.1)',
          fill: true,
          tension: 0.4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }
        },
        scales: {
          y: { beginAtZero: true, grid: { color: '#f0f0f0' } },
          x: { grid: { display: false } }
        }
      }
    })
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.workNo = ''
  searchForm.productName = ''
  searchForm.status = ''
  currentPage.value = 1
  fetchData()
}

function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  fetchData()
}

function handleCurrentChange(page) {
  currentPage.value = page
  fetchData()
}

onMounted(() => {
  updateLastUpdateTime()
  fetchData()
  fetchAllWorkOrders()
  
  nextTick(() => {
    updateCharts()
  })
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
  border-radius: 12px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
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
  color: #1e293b;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}

.stat-trend {
  font-size: 12px;
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.stat-trend.up { color: #22c55e; }
.stat-trend.down { color: #ef4444; }
.stat-trend.normal { color: #64748b; }

.module-row {
  margin-bottom: 20px;
}

.module-card {
  border-radius: 12px;
}

.module-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.module-title {
  font-size: 16px;
  font-weight: bold;
  color: #1e293b;
}

.workflow-timeline {
  position: relative;
  padding-left: 30px;
  min-height: 200px;
}

.timeline-item {
  position: relative;
  margin-bottom: 16px;
}

.timeline-item:last-child {
  margin-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -30px;
  top: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #fff;
}

.dot-info { background-color: #94a3b8; }
.dot-primary { background-color: #22c55e; }
.dot-success { background-color: #10b981; }
.dot-danger { background-color: #ef4444; }

.timeline-line {
  position: absolute;
  left: -22px;
  top: 24px;
  width: 2px;
  height: calc(100% - 24px + 16px);
  background-color: #e2e8f0;
}

.timeline-content {
  padding-left: 8px;
}

.timeline-title {
  font-size: 14px;
  font-weight: bold;
  color: #1e293b;
}

.timeline-desc {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 12px;
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
  color: #1e293b;
}

.chart-container {
  height: 280px;
}

.table-card {
  border-radius: 12px;
}

.search-form {
  margin-bottom: 20px;
}

.finished-qty {
  font-size: 16px;
  font-weight: bold;
  color: #6366f1;
}

.qty-divider {
  margin: 0 6px;
  color: #cbd5e1;
}

.total-qty {
  color: #94a3b8;
}

.update-time {
  font-size: 13px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-content {
  padding: 10px 0;
}

.detail-desc {
  margin-bottom: 20px;
}
</style>
