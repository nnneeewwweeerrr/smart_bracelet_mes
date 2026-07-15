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
      <el-col :span="8">
        <el-card class="module-card" shadow="hover" @click="showDetail('equipment')">
          <div class="module-header">
            <span class="module-title">设备监控</span>
            <el-button type="text" size="small" class="view-detail">查看详情</el-button>
          </div>
          <div class="equipment-grid">
            <div 
              v-for="eq in equipmentStatus" 
              :key="eq.name" 
              class="eq-item"
              :class="{ 'eq-running': eq.status === 'running', 'eq-warning': eq.status === 'warning', 'eq-stopped': eq.status === 'stopped' }"
            >
              <div class="eq-icon">
                <el-icon :size="24"><component :is="eq.icon" /></el-icon>
              </div>
              <div class="eq-info">
                <div class="eq-name">{{ eq.name }}</div>
                <div class="eq-status">{{ eq.statusText }}</div>
              </div>
              <div class="eq-progress">
                <el-progress 
                  :percentage="eq.load" 
                  :stroke-width="6" 
                  :status="eq.load > 80 ? 'exception' : eq.load > 50 ? '' : 'success'"
                  style="width: 60px"
                />
              </div>
            </div>
          </div>
          <div class="module-footer">
            <span class="footer-info">运行中: {{ runningCount }} / {{ equipmentStatus.length }}</span>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="module-card" shadow="hover" @click="showDetail('workflow')">
          <div class="module-header">
            <span class="module-title">工单进度</span>
            <el-button type="text" size="small" class="view-detail">查看详情</el-button>
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
                <div class="timeline-title">{{ order.workOrderNo }}</div>
                <div class="timeline-desc">{{ order.productName }}</div>
                <el-progress 
                  :percentage="Math.round(order.finishedQuantity / order.quantity * 100)" 
                  :stroke-width="8"
                  :status="order.status === 2 ? 'success' : order.status === 1 ? '' : 'exception'"
                  style="width: 100%; margin-top: 8px"
                />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="module-card" shadow="hover" @click="showDetail('process')">
          <div class="module-header">
            <span class="module-title">工序监控</span>
            <el-button type="text" size="small" class="view-detail">查看详情</el-button>
          </div>
          <div class="process-flow">
            <div 
              v-for="(process, index) in processSteps" 
              :key="process.name"
              class="process-step"
              :class="process.status"
            >
              <div class="step-icon">
                <el-icon :size="20"><component :is="process.icon" /></el-icon>
              </div>
              <div class="step-name">{{ process.name }}</div>
              <div class="step-count">{{ process.completed }}/{{ process.total }}</div>
              <div class="step-line" v-if="index < processSteps.length - 1"></div>
            </div>
          </div>
          <div class="process-summary">
            <span>完成率: {{ processCompletionRate }}%</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card" shadow="never">
          <div class="card-header">
            <span class="card-title">产量趋势</span>
          </div>
          <div class="chart-container">
            <canvas ref="trendChart"></canvas>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="chart-card" shadow="never">
          <div class="card-header">
            <span class="card-title">产品产量分布</span>
          </div>
          <div class="chart-container">
            <canvas ref="pieChart"></canvas>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="table-card" shadow="never">
      <div class="card-header">
        <span class="card-title">生产进度列表</span>
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

      <el-form :model="searchForm" :inline="true" class="search-form">
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

      <el-table :data="filteredData" border stripe size="small" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="workOrderNo" label="工单号" min-width="120" />
        <el-table-column prop="productName" label="产品名称" min-width="140" />
        <el-table-column prop="spec" label="规格" width="100" />
        <el-table-column label="产量" width="140">
          <template #default="{ row }">
            <span class="finished-qty">{{ row.finishedQuantity }}</span>
            <span class="qty-divider">/</span>
            <span class="total-qty">{{ row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="200">
          <template #default="{ row }">
            <el-progress 
              :percentage="Math.round((row.finishedQuantity / row.quantity) * 100)" 
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
    </el-card>

    <el-dialog v-model="detailVisible" :title="detailTitle" width="800px" @close="detailVisible = false">
      <div v-if="selectedOrder" class="detail-content">
        <el-descriptions :column="4" border class="detail-desc">
          <el-descriptions-item label="工单号">{{ selectedOrder.workOrderNo }}</el-descriptions-item>
          <el-descriptions-item label="关联订单">{{ selectedOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="产品名称">{{ selectedOrder.productName }}</el-descriptions-item>
          <el-descriptions-item label="规格">{{ selectedOrder.spec }}</el-descriptions-item>
          <el-descriptions-item label="计划产量">{{ selectedOrder.quantity }}</el-descriptions-item>
          <el-descriptions-item label="完成产量">{{ selectedOrder.finishedQuantity }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ selectedOrder.manager }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedOrder.status)" size="small">
              {{ getStatusText(selectedOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="计划开始">{{ selectedOrder.planStartDate }}</el-descriptions-item>
          <el-descriptions-item label="计划完成">{{ selectedOrder.planEndDate }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ selectedOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ selectedOrder.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="process-detail-section">
          <h4 class="section-title">工序详情</h4>
          <el-table :data="selectedOrder.processList || []" border stripe size="small" style="width: 100%">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="processName" label="工序名称" width="140" />
            <el-table-column prop="equipment" label="设备" width="140" />
            <el-table-column label="进度" width="200">
              <template #default="{ row }">
                <el-progress 
                  :percentage="Math.round((row.finishedQuantity / row.quantity) * 100)" 
                  :stroke-width="8"
                  :status="row.status === 'finished' ? 'success' : row.status === 'processing' ? '' : 'exception'"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="完成/计划" width="120">
              <template #default="{ row }">{{ row.finishedQuantity }}/{{ row.quantity }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 'pending'" type="info" size="small">待开始</el-tag>
                <el-tag v-else-if="row.status === 'processing'" type="primary" size="small">进行中</el-tag>
                <el-tag v-else type="success" size="small">已完成</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import Chart from 'chart.js/auto'

const loading = ref(false)
const lastUpdateTime = ref('')
const wsConnected = ref(false)
const detailVisible = ref(false)
const selectedOrder = ref(null)
const trendChart = ref(null)
const pieChart = ref(null)

const searchForm = reactive({
  workOrderNo: '',
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
  { title: '今日产量', value: 1280, icon: 'DataLine', color: '#6366f1', trend: '+12%', trendIcon: 'Top', trendClass: 'up' },
  { title: '生产中工单', value: 5, icon: 'Setting', color: '#22c55e', trend: '+2', trendIcon: 'Top', trendClass: 'up' },
  { title: '待开工单', value: 2, icon: 'Clock', color: '#eab308', trend: '-1', trendIcon: 'Bottom', trendClass: 'down' },
  { title: '异常工单', value: 1, icon: 'Warning', color: '#ef4444', trend: '0', trendIcon: 'Minus', trendClass: 'normal' }
])

const equipmentStatus = ref([
  { name: 'SMT贴片机01', status: 'running', statusText: '运行中', load: 85, icon: 'Cpu' },
  { name: '回流焊机01', status: 'running', statusText: '运行中', load: 72, icon: 'Cpu' },
  { name: '自动组装线01', status: 'running', statusText: '运行中', load: 90, icon: 'Cpu' },
  { name: '测试台01', status: 'warning', statusText: '预警', load: 45, icon: 'Cpu' },
  { name: '自动包装机01', status: 'running', statusText: '运行中', load: 68, icon: 'Cpu' },
  { name: 'SMT贴片机02', status: 'stopped', statusText: '停机', load: 0, icon: 'Cpu' }
])

const workflowData = ref([
  { id: 1, workOrderNo: 'WO-2026001', productName: '智能健康手环 Pro', quantity: 1000, finishedQuantity: 650, status: 1 },
  { id: 2, workOrderNo: 'WO-2026002', productName: '智能运动手环 Lite', quantity: 500, finishedQuantity: 500, status: 2 },
  { id: 3, workOrderNo: 'WO-2026003', productName: '智能健康手环 Pro', quantity: 800, finishedQuantity: 0, status: 0 },
  { id: 4, workOrderNo: 'WO-2026004', productName: '智能运动手环 Lite', quantity: 600, finishedQuantity: 200, status: 1 }
])

const processSteps = ref([
  { name: 'SMT贴片', completed: 3900, total: 3900, status: 'finished', icon: 'Cpu' },
  { name: '回流焊接', completed: 3900, total: 3900, status: 'finished', icon: 'Cpu' },
  { name: '组装', completed: 2050, total: 2900, status: 'processing', icon: 'Cpu' },
  { name: '测试', completed: 300, total: 2900, status: 'processing', icon: 'Cpu' },
  { name: '包装', completed: 0, total: 2900, status: 'pending', icon: 'Cpu' }
])

const mockData = ref([
  { id: 1, workOrderNo: 'WO-2026001', orderNo: 'ORD-2026001', productName: '智能健康手环 Pro', spec: 'SH-P001', quantity: 1000, finishedQuantity: 650, status: 1, planStartDate: '2026-07-10', planEndDate: '2026-07-18', manager: '张工', remark: '加急订单', createTime: '2026-07-08 10:30:00', processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机01', quantity: 1000, finishedQuantity: 1000, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机01', quantity: 1000, finishedQuantity: 1000, status: 'finished' }, { processName: '组装', equipment: '自动组装线01', quantity: 1000, finishedQuantity: 650, status: 'processing' }, { processName: '测试', equipment: '测试台01', quantity: 1000, finishedQuantity: 0, status: 'pending' }, { processName: '包装', equipment: '自动包装机01', quantity: 1000, finishedQuantity: 0, status: 'pending' }] },
  { id: 2, workOrderNo: 'WO-2026002', orderNo: 'ORD-2026002', productName: '智能运动手环 Lite', spec: 'SH-L002', quantity: 500, finishedQuantity: 500, status: 2, planStartDate: '2026-07-05', planEndDate: '2026-07-12', manager: '李工', remark: '', createTime: '2026-07-03 14:20:00', processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机02', quantity: 500, finishedQuantity: 500, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机01', quantity: 500, finishedQuantity: 500, status: 'finished' }, { processName: '组装', equipment: '自动组装线01', quantity: 500, finishedQuantity: 500, status: 'finished' }, { processName: '测试', equipment: '测试台02', quantity: 500, finishedQuantity: 500, status: 'finished' }, { processName: '包装', equipment: '自动包装机01', quantity: 500, finishedQuantity: 500, status: 'finished' }] },
  { id: 3, workOrderNo: 'WO-2026003', orderNo: 'ORD-2026003', productName: '智能健康手环 Pro', spec: 'SH-P001', quantity: 800, finishedQuantity: 0, status: 0, planStartDate: '2026-07-15', planEndDate: '2026-07-22', manager: '王工', remark: '新批次订单', createTime: '2026-07-12 09:00:00', processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机01', quantity: 800, finishedQuantity: 0, status: 'pending' }, { processName: '回流焊接', equipment: '回流焊机01', quantity: 800, finishedQuantity: 0, status: 'pending' }, { processName: '组装', equipment: '自动组装线01', quantity: 800, finishedQuantity: 0, status: 'pending' }, { processName: '测试', equipment: '测试台01', quantity: 800, finishedQuantity: 0, status: 'pending' }, { processName: '包装', equipment: '自动包装机01', quantity: 800, finishedQuantity: 0, status: 'pending' }] },
  { id: 4, workOrderNo: 'WO-2026004', orderNo: 'ORD-2026004', productName: '智能运动手环 Lite', spec: 'SH-L002', quantity: 600, finishedQuantity: 200, status: 1, planStartDate: '2026-07-12', planEndDate: '2026-07-20', manager: '赵工', remark: '', createTime: '2026-07-10 11:45:00', processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机02', quantity: 600, finishedQuantity: 600, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机02', quantity: 600, finishedQuantity: 600, status: 'finished' }, { processName: '组装', equipment: '自动组装线02', quantity: 600, finishedQuantity: 200, status: 'processing' }, { processName: '测试', equipment: '测试台01', quantity: 600, finishedQuantity: 0, status: 'pending' }, { processName: '包装', equipment: '自动包装机02', quantity: 600, finishedQuantity: 0, status: 'pending' }] },
  { id: 5, workOrderNo: 'WO-2026005', orderNo: 'ORD-2026005', productName: '智能手表 Max', spec: 'SW-M003', quantity: 600, finishedQuantity: 600, status: 2, planStartDate: '2026-07-01', planEndDate: '2026-07-10', manager: '刘工', remark: '首批量产订单', createTime: '2026-06-28 16:30:00', processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机01', quantity: 600, finishedQuantity: 600, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机01', quantity: 600, finishedQuantity: 600, status: 'finished' }, { processName: '组装', equipment: '自动组装线01', quantity: 600, finishedQuantity: 600, status: 'finished' }, { processName: '测试', equipment: '测试台02', quantity: 600, finishedQuantity: 600, status: 'finished' }, { processName: '包装', equipment: '自动包装机01', quantity: 600, finishedQuantity: 600, status: 'finished' }] },
  { id: 6, workOrderNo: 'WO-2026006', orderNo: 'ORD-2026006', productName: '智能健康手环 Pro', spec: 'SH-P001', quantity: 1500, finishedQuantity: 450, status: 1, planStartDate: '2026-07-14', planEndDate: '2026-07-25', manager: '张工', remark: '大客户订单', createTime: '2026-07-11 08:15:00', processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机01', quantity: 1500, finishedQuantity: 1500, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机01', quantity: 1500, finishedQuantity: 1500, status: 'finished' }, { processName: '组装', equipment: '自动组装线01', quantity: 1500, finishedQuantity: 450, status: 'processing' }, { processName: '测试', equipment: '测试台01', quantity: 1500, finishedQuantity: 0, status: 'pending' }, { processName: '包装', equipment: '自动包装机01', quantity: 1500, finishedQuantity: 0, status: 'pending' }] },
  { id: 7, workOrderNo: 'WO-2026007', orderNo: 'ORD-2026001', productName: '智能心率手环', spec: 'SH-H004', quantity: 300, finishedQuantity: 0, status: 0, planStartDate: '2026-07-16', planEndDate: '2026-07-23', manager: '孙工', remark: '样品订单', createTime: '2026-07-13 13:00:00', processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机02', quantity: 300, finishedQuantity: 0, status: 'pending' }, { processName: '回流焊接', equipment: '回流焊机02', quantity: 300, finishedQuantity: 0, status: 'pending' }, { processName: '组装', equipment: '自动组装线02', quantity: 300, finishedQuantity: 0, status: 'pending' }, { processName: '测试', equipment: '测试台02', quantity: 300, finishedQuantity: 0, status: 'pending' }, { processName: '包装', equipment: '自动包装机02', quantity: 300, finishedQuantity: 0, status: 'pending' }] },
  { id: 8, workOrderNo: 'WO-2026008', orderNo: 'ORD-2026002', productName: '智能睡眠手环', spec: 'SH-S005', quantity: 400, finishedQuantity: 400, status: 2, planStartDate: '2026-07-03', planEndDate: '2026-07-10', manager: '周工', remark: '', createTime: '2026-07-01 10:00:00', processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机02', quantity: 400, finishedQuantity: 400, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机02', quantity: 400, finishedQuantity: 400, status: 'finished' }, { processName: '组装', equipment: '自动组装线02', quantity: 400, finishedQuantity: 400, status: 'finished' }, { processName: '测试', equipment: '测试台02', quantity: 400, finishedQuantity: 400, status: 'finished' }, { processName: '包装', equipment: '自动包装机02', quantity: 400, finishedQuantity: 400, status: 'finished' }] },
  { id: 9, workOrderNo: 'WO-2026009', orderNo: 'ORD-2026003', productName: '智能运动手环 Lite', spec: 'SH-L002', quantity: 700, finishedQuantity: 350, status: 1, planStartDate: '2026-07-11', planEndDate: '2026-07-19', manager: '吴工', remark: '', createTime: '2026-07-09 15:20:00', processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机01', quantity: 700, finishedQuantity: 700, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机01', quantity: 700, finishedQuantity: 700, status: 'finished' }, { processName: '组装', equipment: '自动组装线01', quantity: 700, finishedQuantity: 350, status: 'processing' }, { processName: '测试', equipment: '测试台01', quantity: 700, finishedQuantity: 0, status: 'pending' }, { processName: '包装', equipment: '自动包装机01', quantity: 700, finishedQuantity: 0, status: 'pending' }] },
  { id: 10, workOrderNo: 'WO-2026010', orderNo: 'ORD-2026004', productName: '智能健康手环 Pro', spec: 'SH-P001', quantity: 900, finishedQuantity: 0, status: 3, planStartDate: '2026-07-08', planEndDate: '2026-07-16', manager: '郑工', remark: '订单已取消', createTime: '2026-07-06 09:30:00', processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机01', quantity: 900, finishedQuantity: 0, status: 'pending' }, { processName: '回流焊接', equipment: '回流焊机01', quantity: 900, finishedQuantity: 0, status: 'pending' }, { processName: '组装', equipment: '自动组装线01', quantity: 900, finishedQuantity: 0, status: 'pending' }, { processName: '测试', equipment: '测试台01', quantity: 900, finishedQuantity: 0, status: 'pending' }, { processName: '包装', equipment: '自动包装机01', quantity: 900, finishedQuantity: 0, status: 'pending' }] },
  { id: 11, workOrderNo: 'WO-2026011', orderNo: 'ORD-2026005', productName: '智能手表 Max', spec: 'SW-M003', quantity: 800, finishedQuantity: 200, status: 1, planStartDate: '2026-07-13', planEndDate: '2026-07-24', manager: '陈工', remark: '', createTime: '2026-07-10 14:00:00', processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机01', quantity: 800, finishedQuantity: 800, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机01', quantity: 800, finishedQuantity: 800, status: 'finished' }, { processName: '组装', equipment: '自动组装线01', quantity: 800, finishedQuantity: 200, status: 'processing' }, { processName: '测试', equipment: '测试台02', quantity: 800, finishedQuantity: 0, status: 'pending' }, { processName: '包装', equipment: '自动包装机01', quantity: 800, finishedQuantity: 0, status: 'pending' }] },
  { id: 12, workOrderNo: 'WO-2026012', orderNo: 'ORD-2026006', productName: '智能心率手环', spec: 'SH-H004', quantity: 500, finishedQuantity: 500, status: 2, planStartDate: '2026-07-06', planEndDate: '2026-07-13', manager: '黄工', remark: '', createTime: '2026-07-04 11:15:00', processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机02', quantity: 500, finishedQuantity: 500, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机02', quantity: 500, finishedQuantity: 500, status: 'finished' }, { processName: '组装', equipment: '自动组装线02', quantity: 500, finishedQuantity: 500, status: 'finished' }, { processName: '测试', equipment: '测试台02', quantity: 500, finishedQuantity: 500, status: 'finished' }, { processName: '包装', equipment: '自动包装机02', quantity: 500, finishedQuantity: 500, status: 'finished' }] }
])

const workOrderData = ref([...mockData.value])

const runningCount = computed(() => equipmentStatus.value.filter(eq => eq.status === 'running').length)

const processCompletionRate = computed(() => {
  const total = processSteps.value.reduce((sum, step) => sum + step.total, 0)
  const completed = processSteps.value.reduce((sum, step) => sum + step.completed, 0)
  return Math.round((completed / total) * 100)
})

const filteredData = computed(() => {
  let result = [...workOrderData.value]
  if (searchForm.workOrderNo) {
    result = result.filter(item => (item.workOrderNo || item.workNo) && (item.workOrderNo || item.workNo).includes(searchForm.workOrderNo))
  }
  if (searchForm.productName) {
    result = result.filter(item => item.productName && item.productName.includes(searchForm.productName))
  }
  if (searchForm.status !== '') {
    result = result.filter(item => item.status === searchForm.status)
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

function getOrderStatusClass(status) {
  const classes = ['dot-info', 'dot-primary', 'dot-success', 'dot-danger']
  return classes[status] || 'dot-info'
}

function getOrderStatusIcon(status) {
  const icons = ['Clock', 'Loading', 'Check', 'Close']
  return icons[status] || 'Circle'
}

function showDetail(type) {
  ElMessage.info(`正在查看${getDetailTitle(type)}详情`)
}

function getDetailTitle(type) {
  const titles = { equipment: '设备监控', workflow: '工单进度', process: '工序监控' }
  return titles[type] || '详情'
}

function showDetailModal(row) {
  selectedOrder.value = row
  detailVisible.value = true
}

const detailTitle = computed(() => {
  return selectedOrder.value ? `工单详情 - ${selectedOrder.value.workOrderNo || selectedOrder.value.workNo}` : '工单详情'
})

function fetchWorkOrders() {
  loading.value = true
  try {
    workOrderData.value = [...mockData.value]
    workflowData.value = workOrderData.value.slice(0, 4).map(item => ({
      id: item.id,
      workOrderNo: item.workOrderNo || item.workNo,
      productName: item.productName,
      quantity: item.quantity,
      finishedQuantity: item.finishedQuantity || 0,
      status: item.status
    }))
    updateStatCards()
  } catch (error) {
    console.error('Fetch work orders failed:', error)
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
  updateLastUpdateTime()
}

function updateLastUpdateTime() {
  lastUpdateTime.value = new Date().toLocaleString('zh-CN', { hour12: false })
}

function initCharts() {
  if (trendChart.value) {
    new Chart(trendChart.value, {
      type: 'line',
      data: {
        labels: ['7.12', '7.13', '7.14', '7.15', '7.16', '7.17', '7.18', '7.19'],
        datasets: [{
          label: '产量',
          data: [800, 950, 1100, 1050, 1200, 1150, 1300, 1280],
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

  if (pieChart.value) {
    new Chart(pieChart.value, {
      type: 'doughnut',
      data: {
        labels: ['智能健康手环 Pro', '智能运动手环 Lite', '智能手表 Max'],
        datasets: [{
          data: [65, 25, 10],
          backgroundColor: ['#6366f1', '#22c55e', '#eab308'],
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
}

onMounted(() => {
  updateLastUpdateTime()
  fetchWorkOrders()
  wsConnected.value = true
  
  nextTick(() => {
    initCharts()
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
  cursor: pointer;
  transition: all 0.3s ease;
}

.module-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
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

.view-detail {
  color: #6366f1;
  font-size: 12px;
}

.equipment-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.eq-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  background-color: #f8fafc;
  transition: all 0.2s;
}

.eq-item:hover {
  background-color: #f1f5f9;
}

.eq-item.eq-running .eq-icon { color: #22c55e; }
.eq-item.eq-warning .eq-icon { color: #eab308; }
.eq-item.eq-stopped .eq-icon { color: #94a3b8; }

.eq-info {
  flex: 1;
}

.eq-name {
  font-size: 13px;
  font-weight: 500;
  color: #1e293b;
}

.eq-status {
  font-size: 11px;
  color: #64748b;
}

.eq-progress {
  flex-shrink: 0;
}

.module-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
}

.footer-info {
  font-size: 12px;
  color: #64748b;
}

.workflow-timeline {
  position: relative;
  padding-left: 30px;
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

.dot-info { background-color: #6366f1; }
.dot-primary { background-color: #22c55e; }
.dot-success { background-color: #10b981; }
.dot-danger { background-color: #ef4444; }

.timeline-line {
  position: absolute;
  left: -22px;
  top: 24px;
  width: 2px;
  height: calc(100% - 24px);
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

.process-flow {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
}

.process-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  flex: 1;
  position: relative;
}

.step-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #fff;
  transition: all 0.3s;
}

.process-step.finished .step-icon {
  background: linear-gradient(135deg, #10b981, #34d399);
}

.process-step.processing .step-icon {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  animation: pulse-step 2s infinite;
}

.process-step.pending .step-icon {
  background-color: #94a3b8;
}

@keyframes pulse-step {
  0%, 100% { box-shadow: 0 0 0 0 rgba(99, 102, 241, 0.4); }
  50% { box-shadow: 0 0 0 8px rgba(99, 102, 241, 0); }
}

.step-name {
  font-size: 11px;
  color: #64748b;
  text-align: center;
}

.step-count {
  font-size: 10px;
  color: #94a3b8;
}

.step-line {
  position: absolute;
  top: 18px;
  left: calc(50% + 50%);
  width: calc(100% - 36px);
  height: 3px;
  background-color: #e2e8f0;
}

.process-step.finished + .process-step .step-line {
  background: linear-gradient(90deg, #10b981, #e2e8f0);
}

.process-summary {
  text-align: center;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
  font-size: 14px;
  font-weight: bold;
  color: #6366f1;
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

.conn-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #94a3b8;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #ef4444;
}

.conn-status.connected {
  color: #22c55e;
}

.conn-status.connected .status-dot {
  background-color: #22c55e;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
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

.detail-content {
  padding: 10px 0;
}

.detail-desc {
  margin-bottom: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  color: #1e293b;
  margin-bottom: 12px;
}

.process-detail-section {
  margin-top: 20px;
}
</style>