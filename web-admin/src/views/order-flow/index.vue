<template>
  <div class="order-flow-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <span class="title-main">订单采购动态流程</span>
        <span class="title-sub">Order Procurement Dynamic Flow</span>
      </div>
      <div class="header-right">
        <span class="update-time">
          <el-icon><Clock /></el-icon>
          数据更新：{{ lastUpdateTime }}
        </span>
        <el-button class="refresh-btn" :icon="Refresh" circle plain @click="handleRefresh" />
      </div>
    </div>

    <!-- 第一行：统计卡片 -->
    <div class="stat-grid">
      <div
        v-for="(item, idx) in statCards"
        :key="idx"
        class="stat-card"
        :class="item.cls"
      >
        <div class="stat-icon-wrap">
          <el-icon :size="26"><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-trend">
            <el-icon :size="12"><Top /></el-icon>
            <span>{{ item.trend }}</span>
          </div>
        </div>
        <div class="stat-bg-icon">
          <el-icon :size="90"><component :is="item.icon" /></el-icon>
        </div>
      </div>
    </div>

    <!-- 第二行：完整流程图 -->
    <div class="flow-section">
      <div class="section-header">
        <span class="section-title">
          <el-icon><Connection /></el-icon>
          完整采购生产流程
        </span>
        <span class="section-tip">点击节点查看详情</span>
      </div>
      <div class="flow-container">
        <div
          v-for="(phase, pi) in flowPhases"
          :key="phase.key"
          class="flow-phase"
          :class="phase.cls"
        >
          <div class="phase-label">
            <span class="phase-index">0{{ pi + 1 }}</span>
            <span class="phase-name">{{ phase.label }}</span>
          </div>
          <div class="phase-nodes">
            <template v-for="(node, ni) in phase.nodes" :key="node.key">
              <div
                class="flow-node"
                :class="[phase.cls, { active: activeNode === node.key }]"
                @click="handleNodeClick(node)"
              >
                <div class="node-icon">
                  <el-icon :size="20"><component :is="node.icon" /></el-icon>
                </div>
                <div class="node-text">
                  <div class="node-title">{{ node.title }}</div>
                  <div class="node-desc">{{ node.desc }}</div>
                </div>
                <span class="node-pulse"></span>
              </div>
              <div v-if="ni < phase.nodes.length - 1" class="node-connector">
                <span class="connector-dot"></span>
                <span class="connector-dot"></span>
                <span class="connector-dot"></span>
              </div>
            </template>
          </div>
          <div v-if="pi < flowPhases.length - 1" class="phase-connector">
            <span class="phase-line"></span>
            <span class="phase-arrow">
              <el-icon><ArrowRight /></el-icon>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 第三行：图表 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="section-header">
          <span class="section-title">
            <el-icon><TrendCharts /></el-icon>
            采购趋势分析
          </span>
          <span class="section-tip">2024.07.12 - 07.19</span>
        </div>
        <div class="chart-wrap">
          <canvas ref="trendChartRef"></canvas>
        </div>
      </div>
      <div class="chart-card">
        <div class="section-header">
          <span class="section-title">
            <el-icon><PieChart /></el-icon>
            订单状态分布
          </span>
          <span class="section-tip">本月数据</span>
        </div>
        <div class="chart-wrap">
          <canvas ref="statusChartRef"></canvas>
        </div>
      </div>
    </div>

    <!-- 第四行：订单流转列表 -->
    <div class="list-section">
      <div class="section-header">
        <span class="section-title">
          <el-icon><Tickets /></el-icon>
          订单流转列表
        </span>
        <span class="section-tip">共 {{ orderList.length }} 条</span>
      </div>
      <el-table :data="orderList" border stripe style="width: 100%" size="default">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="productName" label="产品名称" min-width="140" />
        <el-table-column prop="customer" label="客户名称" width="130" />
        <el-table-column prop="currentStage" label="当前阶段" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStageTagType(row.currentStage)" effect="light" size="small" round>
              {{ row.currentStage }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="流程进度" min-width="240">
          <template #default="{ row }">
            <el-progress
              :percentage="row.progress"
              :color="getProgressColor(row.progress)"
              :stroke-width="14"
              :text-inside="true"
              striped
              striped-flow
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="dark" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="170" />
      </el-table>
    </div>

    <!-- 节点详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="`节点详情 · ${currentNode?.title || ''}`"
      size="42%"
      direction="rtl"
    >
      <template v-if="currentNode">
        <div class="drawer-hero" :class="currentNode.phaseCls">
          <div class="hero-icon">
            <el-icon :size="34"><component :is="currentNode.icon" /></el-icon>
          </div>
          <div class="hero-meta">
            <div class="hero-title">{{ currentNode.title }}</div>
            <div class="hero-desc">{{ currentNode.desc }}</div>
          </div>
        </div>
        <div class="drawer-section">
          <div class="drawer-section-title">节点说明</div>
          <div class="drawer-section-body">{{ currentNode.detail }}</div>
        </div>
        <div class="drawer-section">
          <div class="drawer-section-title">关键指标</div>
          <div class="drawer-metrics">
            <div class="metric-item" v-for="m in currentNode.metrics" :key="m.label">
              <div class="metric-value">{{ m.value }}</div>
              <div class="metric-label">{{ m.label }}</div>
            </div>
          </div>
        </div>
        <div class="drawer-section">
          <div class="drawer-section-title">关联订单</div>
          <el-table :data="currentNode.relatedOrders" border size="small" style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="140" />
            <el-table-column prop="productName" label="产品" min-width="120" />
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small" effect="dark">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import Chart from 'chart.js/auto'
import { ElMessage } from 'element-plus'
import {
  Clock, Refresh, Top, Connection, TrendCharts, PieChart, Tickets,
  ArrowRight
} from '@element-plus/icons-vue'

const lastUpdateTime = ref('2024-07-19 14:32:08')
const trendChartRef = ref(null)
const statusChartRef = ref(null)
let trendChart = null
let statusChart = null

// 统计卡片
const statCards = ref([
  {
    label: '本月订单数',
    value: '186',
    trend: '12.5%',
    icon: 'Document',
    cls: 'stat-blue'
  },
  {
    label: '采购金额',
    value: '¥2.86M',
    trend: '8.3%',
    icon: 'Money',
    cls: 'stat-purple'
  },
  {
    label: '入库批次',
    value: '142',
    trend: '5.7%',
    icon: 'Box',
    cls: 'stat-teal'
  },
  {
    label: '生产工单数',
    value: '98',
    trend: '15.2%',
    icon: 'Operation',
    cls: 'stat-orange'
  }
])

// 流程阶段与节点
const flowPhases = ref([
  {
    key: 'order',
    label: '订单阶段',
    cls: 'phase-blue',
    nodes: [
      { key: 'customer-order', title: '客户下单', desc: '客户提交需求', icon: 'ShoppingCart', phaseCls: 'phase-blue', detail: '客户通过线上商城或线下渠道提交智能手环采购需求，系统自动生成订单草稿并校验库存与产能。', metrics: [{ label: '今日新增', value: '24' }, { label: '待确认', value: '12' }, { label: '转化率', value: '68%' }], relatedOrders: [{ orderNo: 'SO20240719001', productName: '智能手环Pro', status: '进行中' }, { orderNo: 'SO20240719002', productName: '智能手环Lite', status: '进行中' }] },
      { key: 'order-confirm', title: '订单确认', desc: '审核与确认', icon: 'DocumentChecked', phaseCls: 'phase-blue', detail: '销售主管审核订单信息、价格、交期，确认后流转至采购环节，同步推送客户确认函。', metrics: [{ label: '待审核', value: '6' }, { label: '已确认', value: '18' }, { label: '平均时长', value: '2.1h' }], relatedOrders: [{ orderNo: 'SO20240718008', productName: '智能手环Ultra', status: '进行中' }] }
    ]
  },
  {
    key: 'purchase',
    label: '采购阶段',
    cls: 'phase-purple',
    nodes: [
      { key: 'purchase-apply', title: '采购申请', desc: '生成采购单', icon: 'Document', phaseCls: 'phase-purple', detail: '根据订单BOM与库存缺口，自动生成采购申请单，注明物料型号、数量与期望到货时间。', metrics: [{ label: '申请数', value: '34' }, { label: '已审批', value: '28' }, { label: '待审批', value: '6' }], relatedOrders: [{ orderNo: 'PO20240719003', productName: '蓝牙模组', status: '进行中' }] },
      { key: 'supplier-select', title: '供应商选择', desc: '比价与选定', icon: 'OfficeBuilding', phaseCls: 'phase-purple', detail: '系统从合格供应商库中按价格、交期、质量评分智能推荐，采购员确认后下达采购订单。', metrics: [{ label: '候选供应商', value: '5' }, { label: '平均交期', value: '6.2天' }, { label: '合格率', value: '98.5%' }], relatedOrders: [{ orderNo: 'PO20240719004', productName: '显示屏模组', status: '进行中' }] },
      { key: 'purchase-order', title: '采购下单', desc: '订单下发', icon: 'Files', phaseCls: 'phase-purple', detail: '向供应商正式下发采购订单，同步至供应商协同平台，跟踪发货与物流状态。', metrics: [{ label: '在途订单', value: '19' }, { label: '今日下单', value: '8' }, { label: '金额', value: '¥1.2M' }], relatedOrders: [{ orderNo: 'PO20240719005', productName: '电池模组', status: '进行中' }] }
    ]
  },
  {
    key: 'warehouse',
    label: '入库阶段',
    cls: 'phase-teal',
    nodes: [
      { key: 'stock-in', title: '来料入库', desc: '收货登记', icon: 'Box', phaseCls: 'phase-teal', detail: '仓库收货人员核对送货单与采购订单，扫码登记物料批次并暂存待检区。', metrics: [{ label: '今日收货', value: '12' }, { label: '待检批次', value: '7' }, { label: '总数量', value: '8640' }], relatedOrders: [{ orderNo: 'IN20240719006', productName: 'PCBA板', status: '进行中' }] },
      { key: 'iqc-check', title: 'IQC检验', desc: '来料质检', icon: 'Select', phaseCls: 'phase-teal', detail: 'IQC按抽样方案对来料进行外观、功能、尺寸检验，合格批次转入正式库位，不合格批次隔离处理。', metrics: [{ label: '检验批次', value: '11' }, { label: '合格率', value: '96.4%' }, { label: '不合格', value: '1' }], relatedOrders: [{ orderNo: 'QC20240719007', productName: '表带硅胶', status: '已完成' }] },
      { key: 'stock-update', title: '库存更新', desc: '账务同步', icon: 'RefreshRight', phaseCls: 'phase-teal', detail: '合格物料上架入库，系统实时更新库存账务，触发生产可用料预警。', metrics: [{ label: '上架SKU', value: '46' }, { label: '库存增量', value: '+8240' }, { label: '可用率', value: '94%' }], relatedOrders: [{ orderNo: 'IN20240719008', productName: '充电芯片', status: '已完成' }] }
    ]
  },
  {
    key: 'production',
    label: '生产阶段',
    cls: 'phase-orange',
    nodes: [
      { key: 'production-plan', title: '生产排产', desc: '计划编排', icon: 'Calendar', phaseCls: 'phase-orange', detail: '根据订单交期与产能负荷，APS系统自动排产，生成各车间日生产计划。', metrics: [{ label: '排产工单', value: '23' }, { label: '排产达成率', value: '92%' }, { label: '产能利用率', value: '86%' }], relatedOrders: [{ orderNo: 'WO20240719009', productName: '智能手环Pro', status: '进行中' }] },
      { key: 'work-order', title: '工单下发', desc: '派工执行', icon: 'Tickets', phaseCls: 'phase-orange', detail: '将生产工单下发至车间MES终端，明确工序、物料、工时与质检要求。', metrics: [{ label: '今日派工', value: '18' }, { label: '在制工单', value: '32' }, { label: '准点率', value: '97%' }], relatedOrders: [{ orderNo: 'WO20240719010', productName: '智能手环Lite', status: '进行中' }] },
      { key: 'production-run', title: '生产执行', desc: '工序作业', icon: 'Operation', phaseCls: 'phase-orange', detail: '车间按SOP作业，实时报工与首检、巡检，异常自动上报处理。', metrics: [{ label: '在制数量', value: '4280' }, { label: '良率', value: '98.2%' }, { label: 'OEE', value: '82%' }], relatedOrders: [{ orderNo: 'WO20240719011', productName: '智能手环Ultra', status: '进行中' }] }
    ]
  },
  {
    key: 'delivery',
    label: '出货阶段',
    cls: 'phase-pink',
    nodes: [
      { key: 'finished-in', title: '成品入库', desc: '成品上架', icon: 'Goods', phaseCls: 'phase-pink', detail: '生产完工的成品经OQC检验合格后扫码入库，更新成品库存。', metrics: [{ label: '入库数量', value: '3860' }, { label: 'OQC合格率', value: '99.1%' }, { label: 'SKU', value: '12' }], relatedOrders: [{ orderNo: 'FIN20240719012', productName: '智能手环Pro', status: '已完成' }] },
      { key: 'ship-out', title: '发货出库', desc: '物流发货', icon: 'Van', phaseCls: 'phase-pink', detail: '按订单拣货、复核、打包，关联物流单号发货，客户可在系统实时查看物流。', metrics: [{ label: '今日发货', value: '16' }, { label: '准时率', value: '95.6%' }, { label: '在途订单', value: '9' }], relatedOrders: [{ orderNo: 'SO20240719013', productName: '智能手环Lite', status: '已完成' }] }
    ]
  }
])

// 订单流转列表
const orderList = ref([
  { orderNo: 'SO20240719001', productName: '智能手环Pro', customer: '深圳智联科技', currentStage: '生产执行', progress: 65, status: '进行中', updateTime: '2024-07-19 14:20' },
  { orderNo: 'SO20240719002', productName: '智能手环Lite', customer: '广州穿戴电子', currentStage: '采购下单', progress: 28, status: '进行中', updateTime: '2024-07-19 13:45' },
  { orderNo: 'SO20240718008', productName: '智能手环Ultra', customer: '北京运动健康', currentStage: 'IQC检验', progress: 42, status: '进行中', updateTime: '2024-07-19 11:30' },
  { orderNo: 'SO20240718007', productName: '智能手环Pro', customer: '上海FitBand', currentStage: '发货出库', progress: 96, status: '已完成', updateTime: '2024-07-19 10:15' },
  { orderNo: 'SO20240718006', productName: '智能手环Lite', customer: '杭州乐动', currentStage: '成品入库', progress: 88, status: '进行中', updateTime: '2024-07-19 09:50' },
  { orderNo: 'SO20240718005', productName: '智能手环Ultra', customer: '成都极客数码', currentStage: '订单确认', progress: 12, status: '待处理', updateTime: '2024-07-18 17:20' },
  { orderNo: 'SO20240718004', productName: '智能手环Pro', customer: '深圳智联科技', currentStage: '工单下发', progress: 55, status: '进行中', updateTime: '2024-07-18 16:40' },
  { orderNo: 'SO20240718003', productName: '智能手环Lite', customer: '南京跃动', currentStage: '库存更新', progress: 50, status: '进行中', updateTime: '2024-07-18 15:10' }
])

// 抽屉
const drawerVisible = ref(false)
const currentNode = ref(null)

function handleNodeClick(node) {
  currentNode.value = node
  drawerVisible.value = true
}

function getStageTagType(stage) {
  const map = {
    '客户下单': 'info',
    '订单确认': 'info',
    '采购申请': 'warning',
    '供应商选择': 'warning',
    '采购下单': 'warning',
    '来料入库': '',
    'IQC检验': '',
    '库存更新': '',
    '生产排产': 'success',
    '工单下发': 'success',
    '生产执行': 'success',
    '成品入库': 'primary',
    '发货出库': 'primary'
  }
  return map[stage] || ''
}

function getStatusType(status) {
  const map = { '进行中': 'primary', '已完成': 'success', '待处理': 'warning', '已取消': 'danger' }
  return map[status] || 'info'
}

function getProgressColor(percentage) {
  if (percentage >= 90) return '#10b981'
  if (percentage >= 60) return '#3b82f6'
  if (percentage >= 30) return '#f59e0b'
  return '#ef4444'
}

function handleRefresh() {
  lastUpdateTime.value = formatNow()
  ElMessage.success('数据已刷新')
}

function formatNow() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

// 采购趋势图（line + bar 组合）
function initTrendChart() {
  if (!trendChartRef.value) return
  const ctx = trendChartRef.value.getContext('2d')
  const lineGradient = ctx.createLinearGradient(0, 0, 0, 280)
  lineGradient.addColorStop(0, 'rgba(59, 130, 246, 0.35)')
  lineGradient.addColorStop(1, 'rgba(59, 130, 246, 0.02)')

  trendChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: ['7.12', '7.13', '7.14', '7.15', '7.16', '7.17', '7.18', '7.19'],
      datasets: [
        {
          type: 'line',
          label: '采购金额(万元)',
          data: [38.5, 42.1, 35.8, 48.6, 52.3, 46.7, 58.2, 63.5],
          borderColor: '#3b82f6',
          backgroundColor: lineGradient,
          borderWidth: 3,
          tension: 0.4,
          fill: true,
          pointRadius: 4,
          pointHoverRadius: 6,
          pointBackgroundColor: '#fff',
          pointBorderColor: '#3b82f6',
          pointBorderWidth: 2,
          yAxisID: 'y'
        },
        {
          type: 'bar',
          label: '采购批次',
          data: [12, 15, 11, 18, 21, 17, 23, 25],
          backgroundColor: (context) => {
            const chart = context.chart
            const { ctx: c, chartArea } = chart
            if (!chartArea) return 'rgba(139, 92, 246, 0.6)'
            const g = c.createLinearGradient(0, chartArea.bottom, 0, chartArea.top)
            g.addColorStop(0, 'rgba(139, 92, 246, 0.2)')
            g.addColorStop(1, 'rgba(139, 92, 246, 0.85)')
            return g
          },
          borderRadius: 6,
          barPercentage: 0.5,
          yAxisID: 'y1'
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: 'index', intersect: false },
      plugins: {
        legend: {
          position: 'top',
          labels: { color: '#64748b', font: { size: 12 }, usePointStyle: true, boxWidth: 8 }
        },
        tooltip: {
          backgroundColor: 'rgba(30, 41, 59, 0.92)',
          titleColor: '#fff',
          bodyColor: '#e2e8f0',
          borderColor: 'rgba(59,130,246,0.4)',
          borderWidth: 1,
          padding: 12,
          cornerRadius: 8
        }
      },
      scales: {
        x: {
          grid: { display: false },
          ticks: { color: '#94a3b8' }
        },
        y: {
          position: 'left',
          title: { display: true, text: '金额(万元)', color: '#94a3b8' },
          grid: { color: 'rgba(226, 232, 240, 0.6)', drawBorder: false },
          ticks: { color: '#94a3b8' }
        },
        y1: {
          position: 'right',
          title: { display: true, text: '批次', color: '#94a3b8' },
          grid: { drawOnChartArea: false },
          ticks: { color: '#94a3b8' }
        }
      },
      animation: { duration: 1200, easing: 'easeOutQuart' }
    }
  })
}

// 订单状态分布图（doughnut）
function initStatusChart() {
  if (!statusChartRef.value) return
  const ctx = statusChartRef.value.getContext('2d')
  statusChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ['待确认', '采购中', '生产中', '待发货', '已完成'],
      datasets: [
        {
          data: [18, 26, 42, 15, 85],
          backgroundColor: [
            'rgba(245, 158, 11, 0.85)',
            'rgba(139, 92, 246, 0.85)',
            'rgba(59, 130, 246, 0.85)',
            'rgba(236, 72, 153, 0.85)',
            'rgba(16, 185, 129, 0.85)'
          ],
          borderColor: '#fff',
          borderWidth: 3,
          hoverOffset: 12,
          spacing: 2
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '62%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            color: '#64748b',
            font: { size: 12 },
            usePointStyle: true,
            pointStyle: 'circle',
            boxWidth: 8,
            padding: 14
          }
        },
        tooltip: {
          backgroundColor: 'rgba(30, 41, 59, 0.92)',
          titleColor: '#fff',
          bodyColor: '#e2e8f0',
          padding: 12,
          cornerRadius: 8,
          callbacks: {
            label: (ctx) => {
              const total = ctx.dataset.data.reduce((a, b) => a + b, 0)
              const pct = ((ctx.raw / total) * 100).toFixed(1)
              return ` ${ctx.label}: ${ctx.raw} 单 (${pct}%)`
            }
          }
        }
      },
      animation: { animateRotate: true, animateScale: true, duration: 1200 }
    }
  })
}

function handleResize() {
  trendChart?.resize()
  statusChart?.resize()
}

onMounted(() => {
  nextTick(() => {
    initTrendChart()
    initStatusChart()
    window.addEventListener('resize', handleResize)
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.destroy()
  statusChart?.destroy()
})
</script>

<style scoped>
.order-flow-page {
  width: 100%;
  min-height: 100%;
  padding: 18px 20px 28px;
  background:
    radial-gradient(1200px 500px at 100% -10%, rgba(59, 130, 246, 0.08), transparent 60%),
    radial-gradient(1000px 500px at -10% 110%, rgba(139, 92, 246, 0.08), transparent 60%),
    linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
}

/* 头部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}
.header-left {
  display: flex;
  flex-direction: column;
}
.title-main {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.title-sub {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
  letter-spacing: 0.5px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.update-time {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
  background: #fff;
  padding: 8px 14px;
  border-radius: 20px;
  box-shadow: var(--shadow-sm);
}
.refresh-btn {
  background: #fff;
}

/* 通用 section header */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.section-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.section-title .el-icon {
  color: #3b82f6;
}
.section-tip {
  font-size: 12px;
  color: var(--text-placeholder);
}

/* 统计卡片 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 18px;
}
.stat-card {
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-lg);
  padding: 20px 22px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: #fff;
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.18);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: default;
}
.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 14px 32px rgba(59, 130, 246, 0.28);
}
.stat-blue {
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.22);
}
.stat-purple {
  background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%);
  box-shadow: 0 8px 24px rgba(139, 92, 246, 0.22);
}
.stat-teal {
  background: linear-gradient(135deg, #14b8a6 0%, #2dd4bf 100%);
  box-shadow: 0 8px 24px rgba(20, 184, 166, 0.22);
}
.stat-orange {
  background: linear-gradient(135deg, #f97316 0%, #fb923c 100%);
  box-shadow: 0 8px 24px rgba(249, 115, 22, 0.22);
}
.stat-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  z-index: 1;
}
.stat-info {
  z-index: 1;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.1;
}
.stat-label {
  font-size: 13px;
  opacity: 0.9;
  margin-top: 2px;
}
.stat-trend {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  margin-top: 6px;
  padding: 2px 8px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 10px;
}
.stat-bg-icon {
  position: absolute;
  right: -10px;
  bottom: -20px;
  opacity: 0.16;
  color: #fff;
  transform: rotate(-15deg);
  pointer-events: none;
}

/* 流程图区域 */
.flow-section {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 20px 22px 26px;
  margin-bottom: 18px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
}
.flow-container {
  display: flex;
  align-items: stretch;
  gap: 0;
  overflow-x: auto;
  padding: 8px 4px 12px;
}
.flow-phase {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
.phase-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 56px;
  margin-right: 12px;
  padding: 10px 4px;
  border-radius: 12px;
  color: #fff;
  writing-mode: vertical-rl;
  text-orientation: upright;
  letter-spacing: 2px;
  font-size: 12px;
  font-weight: 600;
  position: relative;
}
.phase-index {
  writing-mode: horizontal-tb;
  text-orientation: mixed;
  font-size: 11px;
  opacity: 0.85;
  margin-bottom: 6px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 6px;
  padding: 2px 4px;
}
.phase-blue .phase-label { background: linear-gradient(180deg, #3b82f6, #60a5fa); }
.phase-purple .phase-label { background: linear-gradient(180deg, #8b5cf6, #a78bfa); }
.phase-teal .phase-label { background: linear-gradient(180deg, #14b8a6, #2dd4bf); }
.phase-orange .phase-label { background: linear-gradient(180deg, #f97316, #fb923c); }
.phase-pink .phase-label { background: linear-gradient(180deg, #ec4899, #f472b6); }

.phase-nodes {
  display: flex;
  flex-direction: column;
  gap: 0;
}
.flow-node {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 190px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 12px;
  border: 1.5px solid var(--border-color);
  cursor: pointer;
  transition: all 0.28s ease;
  overflow: hidden;
}
.flow-node::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  transition: width 0.28s ease;
}
.phase-blue .flow-node::before { background: linear-gradient(180deg, #3b82f6, #60a5fa); }
.phase-purple .flow-node::before { background: linear-gradient(180deg, #8b5cf6, #a78bfa); }
.phase-teal .flow-node::before { background: linear-gradient(180deg, #14b8a6, #2dd4bf); }
.phase-orange .flow-node::before { background: linear-gradient(180deg, #f97316, #fb923c); }
.phase-pink .flow-node::before { background: linear-gradient(180deg, #ec4899, #f472b6); }

.flow-node:hover {
  transform: translateX(4px);
  border-color: transparent;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.1);
}
.flow-node:hover::before {
  width: 100%;
  opacity: 0.08;
}
.flow-node.active {
  border-color: transparent;
  box-shadow: 0 10px 24px rgba(59, 130, 246, 0.25);
}
.flow-node.active::before {
  width: 100%;
  opacity: 0.12;
}
.node-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
}
.phase-blue .node-icon { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
.phase-purple .node-icon { background: linear-gradient(135deg, #8b5cf6, #a78bfa); }
.phase-teal .node-icon { background: linear-gradient(135deg, #14b8a6, #2dd4bf); }
.phase-orange .node-icon { background: linear-gradient(135deg, #f97316, #fb923c); }
.phase-pink .node-icon { background: linear-gradient(135deg, #ec4899, #f472b6); }

.node-text {
  flex: 1;
  min-width: 0;
}
.node-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}
.node-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}
.node-pulse {
  position: absolute;
  right: 12px;
  top: 12px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.6);
  animation: pulse 2s infinite;
}
@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.6); }
  70% { box-shadow: 0 0 0 8px rgba(16, 185, 129, 0); }
  100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
}

/* 节点之间的纵向连接（流动点） */
.node-connector {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4px 0;
  margin-left: 36px;
}
.node-connector .connector-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #3b82f6;
  margin: 2px 0;
  opacity: 0.4;
  animation: dotFlow 1.4s infinite;
}
.node-connector .connector-dot:nth-child(2) { animation-delay: 0.2s; }
.node-connector .connector-dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes dotFlow {
  0% { opacity: 0.2; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
  100% { opacity: 0.2; transform: scale(0.8); }
}

/* 阶段之间的横向连接（流动线） */
.phase-connector {
  display: flex;
  align-items: center;
  align-self: center;
  margin: 0 8px;
  height: 40px;
  position: relative;
}
.phase-line {
  width: 36px;
  height: 3px;
  border-radius: 2px;
  background: linear-gradient(90deg,
    rgba(59, 130, 246, 0.15) 0%,
    rgba(139, 92, 246, 0.9) 50%,
    rgba(236, 72, 153, 0.15) 100%);
  background-size: 200% 100%;
  animation: phaseFlow 1.8s linear infinite;
  position: relative;
}
@keyframes phaseFlow {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
.phase-arrow {
  color: #8b5cf6;
  font-size: 16px;
  margin-left: 2px;
  animation: arrowSlide 1.8s ease-in-out infinite;
}
@keyframes arrowSlide {
  0%, 100% { transform: translateX(0); opacity: 0.7; }
  50% { transform: translateX(3px); opacity: 1; }
}

/* 图表行 */
.charts-row {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 18px;
  margin-bottom: 18px;
}
.chart-card {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 20px 22px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
}
.chart-wrap {
  position: relative;
  height: 320px;
  width: 100%;
}

/* 列表区域 */
.list-section {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 20px 22px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
}

/* 抽屉内容 */
.drawer-hero {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 14px;
  color: #fff;
  margin-bottom: 18px;
}
.drawer-hero.phase-blue { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
.drawer-hero.phase-purple { background: linear-gradient(135deg, #8b5cf6, #a78bfa); }
.drawer-hero.phase-teal { background: linear-gradient(135deg, #14b8a6, #2dd4bf); }
.drawer-hero.phase-orange { background: linear-gradient(135deg, #f97316, #fb923c); }
.drawer-hero.phase-pink { background: linear-gradient(135deg, #ec4899, #f472b6); }
.hero-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.hero-title {
  font-size: 18px;
  font-weight: 700;
}
.hero-desc {
  font-size: 13px;
  opacity: 0.9;
  margin-top: 4px;
}
.drawer-section {
  margin-bottom: 20px;
}
.drawer-section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
  padding-left: 10px;
  border-left: 3px solid #3b82f6;
}
.drawer-section-body {
  font-size: 13px;
  color: var(--text-regular);
  line-height: 1.8;
  background: #f8fafc;
  padding: 14px 16px;
  border-radius: 10px;
}
.drawer-metrics {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.metric-item {
  text-align: center;
  padding: 16px 8px;
  background: linear-gradient(180deg, #f8fafc, #f1f5f9);
  border-radius: 10px;
  border: 1px solid var(--border-light);
}
.metric-value {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.metric-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .charts-row {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 900px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
