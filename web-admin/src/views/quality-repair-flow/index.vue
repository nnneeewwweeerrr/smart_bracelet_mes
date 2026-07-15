<template>
  <div class="quality-repair-flow">
    <!-- 第一行：统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="item in statCards" :key="item.title">
        <div class="stat-card" :style="{ background: item.gradient }">
          <div class="stat-card-bg"></div>
          <div class="stat-icon">
            <el-icon :size="30"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-label">{{ item.title }}</div>
            <div class="stat-trend">
              <el-icon><component :is="item.trendIcon" /></el-icon>
              <span>{{ item.trend }}</span>
              <span class="trend-text">较昨日</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 第二行：流程图区域 -->
    <el-card shadow="never" class="flow-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Connection /></el-icon>
            质检维修动态流程
          </span>
          <div class="flow-legend">
            <span class="legend-item"><i class="dot done"></i>已完成</span>
            <span class="legend-item"><i class="dot active"></i>进行中</span>
            <span class="legend-item"><i class="dot pending"></i>待处理</span>
          </div>
        </div>
      </template>

      <div class="flow-chart">
        <div
          v-for="(node, index) in flowNodes"
          :key="node.id"
          class="flow-node-wrapper"
        >
          <div
            class="flow-node"
            :class="[node.status, { expanded: activeNodeId === node.id }]"
            @click="toggleNode(node.id)"
          >
            <div class="node-icon" :style="{ background: node.gradient }">
              <el-icon :size="26"><component :is="node.icon" /></el-icon>
              <span v-if="node.status === 'active'" class="node-pulse"></span>
            </div>
            <div class="node-content">
              <div class="node-title">
                {{ node.title }}
                <span v-if="node.subtitle" class="node-subtitle">{{ node.subtitle }}</span>
              </div>
              <div class="node-desc">{{ node.desc }}</div>
              <div class="node-status">
                <el-tag
                  :type="getStatusTagType(node.status)"
                  effect="light"
                  size="small"
                  round
                >
                  {{ getStatusText(node.status) }}
                </el-tag>
              </div>
            </div>
            <div class="node-expand">
              <el-icon><ArrowDown v-if="activeNodeId !== node.id" /><ArrowUp v-else /></el-icon>
            </div>
          </div>

          <!-- 连接线 -->
          <div v-if="index < flowNodes.length - 1" class="flow-connector" :class="node.status">
            <div class="connector-line"></div>
            <div class="connector-flow"></div>
            <div class="connector-arrow">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 节点详情面板 -->
      <transition name="detail-slide">
        <div v-if="activeNode" class="node-detail-panel">
          <div class="detail-header" :style="{ background: activeNode.gradient }">
            <div class="detail-title">
              <el-icon :size="22"><component :is="activeNode.icon" /></el-icon>
              <span>{{ activeNode.title }} - 详情</span>
            </div>
            <el-button class="detail-close" circle size="small" @click="activeNodeId = null">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          <div class="detail-body">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="detail-stat">
                  <div class="detail-stat-label">今日处理量</div>
                  <div class="detail-stat-value">{{ activeNode.detail.todayCount }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="detail-stat">
                  <div class="detail-stat-label">累计处理量</div>
                  <div class="detail-stat-value">{{ activeNode.detail.totalCount }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="detail-stat">
                  <div class="detail-stat-label">完成率</div>
                  <div class="detail-stat-value" :style="{ color: activeNode.detail.completionRate >= 90 ? '#22c55e' : '#eab308' }">
                    {{ activeNode.detail.completionRate }}%
                  </div>
                </div>
              </el-col>
            </el-row>

            <div class="sub-nodes-title">子流程节点</div>
            <div class="sub-nodes">
              <div
                v-for="(sub, sIdx) in activeNode.subNodes"
                :key="sIdx"
                class="sub-node"
                :class="sub.status"
              >
                <div class="sub-node-dot">
                  <el-icon v-if="sub.status === 'done'"><Check /></el-icon>
                  <span v-else>{{ sIdx + 1 }}</span>
                </div>
                <div class="sub-node-info">
                  <div class="sub-node-name">{{ sub.name }}</div>
                  <div class="sub-node-meta">{{ sub.meta }}</div>
                </div>
                <div class="sub-node-line" v-if="sIdx < activeNode.subNodes.length - 1"></div>
              </div>
            </div>

            <div class="detail-remark">
              <el-icon><InfoFilled /></el-icon>
              <span>{{ activeNode.detail.remark }}</span>
            </div>
          </div>
        </div>
      </transition>
    </el-card>

    <!-- 第三行：合格率图表 + 维修时间线 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><DataAnalysis /></el-icon>
                各检验类型合格率
              </span>
            </div>
          </template>
          <div class="chart-content">
            <div class="doughnut-wrap">
              <canvas ref="doughnutChartRef"></canvas>
              <div class="chart-center-text">
                <div class="center-value">{{ overallPassRate }}%</div>
                <div class="center-label">综合合格率</div>
              </div>
            </div>
            <div class="rate-list">
              <div v-for="item in passRateData" :key="item.type" class="rate-item">
                <div class="rate-header">
                  <span class="rate-type">
                    <i class="rate-color" :style="{ background: item.color }"></i>
                    {{ item.type }}
                  </span>
                  <span class="rate-value">{{ item.passRate }}%</span>
                </div>
                <el-progress
                  :percentage="item.passRate"
                  :stroke-width="8"
                  :color="item.color"
                  :show-text="false"
                />
                <div class="rate-detail">合格 {{ item.pass }} / 共 {{ item.total }} 批</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Timer /></el-icon>
                维修任务时间线
              </span>
              <el-tag type="warning" effect="light" size="small" round>进行中</el-tag>
            </div>
          </template>
          <div class="timeline-wrap">
            <el-timeline>
              <el-timeline-item
                v-for="(task, idx) in repairTimeline"
                :key="idx"
                :type="getTimelineType(task.status)"
                :timestamp="task.time"
                placement="top"
                :hollow="task.status === 'pending'"
              >
                <div class="timeline-card" :class="task.status">
                  <div class="timeline-title">
                    <el-icon class="timeline-icon"><component :is="task.icon" /></el-icon>
                    <span>{{ task.title }}</span>
                    <el-tag
                      :type="getTimelineTagType(task.status)"
                      effect="plain"
                      size="small"
                      class="timeline-tag"
                    >
                      {{ getTimelineStatusText(task.status) }}
                    </el-tag>
                  </div>
                  <div class="timeline-desc">{{ task.desc }}</div>
                  <div class="timeline-operator" v-if="task.operator">
                    <el-icon><User /></el-icon>
                    <span>{{ task.operator }}</span>
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第四行：不合格品处理列表 + 维修工单列表 -->
    <el-row :gutter="20" class="table-row">
      <el-col :span="12">
        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Warning /></el-icon>
                不合格品处理列表
              </span>
              <el-tag type="danger" effect="light" size="small">共 {{ unqualifiedList.length }} 条</el-tag>
            </div>
          </template>
          <el-table :data="unqualifiedList" border stripe size="small" style="width: 100%">
            <el-table-column type="index" label="序号" width="55" align="center" />
            <el-table-column prop="batchNo" label="不合格单号" min-width="130" />
            <el-table-column prop="productName" label="产品名称" min-width="120" show-overflow-tooltip />
            <el-table-column prop="inspectType" label="检验类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getInspectTagType(row.inspectType)" effect="plain" size="small">
                  {{ row.inspectType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="defectQty" label="不合格数" width="80" align="right" />
            <el-table-column prop="handleType" label="处理方式" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="getHandleTagType(row.handleType)" effect="dark" size="small">
                  {{ row.handleType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="getHandleStatusType(row.status)" effect="light" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="handleTime" label="处理时间" width="150" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Tools /></el-icon>
                维修工单列表
              </span>
              <el-tag type="warning" effect="light" size="small">共 {{ repairOrderList.length }} 条</el-tag>
            </div>
          </template>
          <el-table :data="repairOrderList" border stripe size="small" style="width: 100%">
            <el-table-column type="index" label="序号" width="55" align="center" />
            <el-table-column prop="repairNo" label="维修单号" min-width="130" />
            <el-table-column prop="equipmentName" label="设备名称" min-width="130" show-overflow-tooltip />
            <el-table-column prop="faultType" label="故障类型" width="110" show-overflow-tooltip />
            <el-table-column prop="reportTime" label="报修时间" width="150" />
            <el-table-column prop="repairman" label="维修人员" width="90" align="center" />
            <el-table-column prop="status" label="维修状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getRepairStatusType(row.status)" effect="light" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="priority" label="优先级" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="getPriorityType(row.priority)" effect="dark" size="small">
                  {{ row.priority }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import Chart from 'chart.js/auto'

/* ============ 统计卡片 ============ */
const statCards = ref([
  {
    title: '今日检验批次',
    value: 128,
    icon: 'Document',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    trend: '+8%',
    trendIcon: 'Top'
  },
  {
    title: '合格率',
    value: '96.8%',
    icon: 'CircleCheck',
    gradient: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
    trend: '+1.2%',
    trendIcon: 'Top'
  },
  {
    title: '不合格品数',
    value: 41,
    icon: 'CircleClose',
    gradient: 'linear-gradient(135deg, #eb3349 0%, #f45c43 100%)',
    trend: '-5',
    trendIcon: 'Bottom'
  },
  {
    title: '待维修工单数',
    value: 7,
    icon: 'Tools',
    gradient: 'linear-gradient(135deg, #2193b0 0%, #6dd5ed 100%)',
    trend: '+2',
    trendIcon: 'Top'
  }
])

/* ============ 流程节点 ============ */
const activeNodeId = ref('ipqc')

const flowNodes = ref([
  {
    id: 'iqc',
    title: 'IQC来料检验',
    desc: '来料质量把控',
    icon: 'Goods',
    status: 'done',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    subNodes: [
      { name: 'IQC检验记录', status: 'done', meta: '已完成 32 批次' }
    ],
    detail: {
      todayCount: 32,
      totalCount: 1280,
      completionRate: 100,
      remark: '今日来料检验全部完成,合格率 97.5%,2 批次需复检。'
    }
  },
  {
    id: 'ipqc',
    title: '生产过程检验',
    subtitle: 'QC',
    desc: '首件/巡检/末件',
    icon: 'View',
    status: 'active',
    gradient: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
    subNodes: [
      { name: '首件检验', status: 'done', meta: '已通过 8/8' },
      { name: '巡检', status: 'active', meta: '进行中 · 第 3 次' },
      { name: '末件检验', status: 'pending', meta: '待生产末件' }
    ],
    detail: {
      todayCount: 24,
      totalCount: 860,
      completionRate: 75,
      remark: '首件检验全部通过,巡检正在进行中,末件检验待生产完成后启动。'
    }
  },
  {
    id: 'fqc',
    title: '成品检验',
    subtitle: 'FQC',
    desc: '成品抽检放行',
    icon: 'CircleCheck',
    status: 'pending',
    gradient: 'linear-gradient(135deg, #f7971e 0%, #ffd200 100%)',
    subNodes: [
      { name: '成品抽检', status: 'pending', meta: '待批次入库' }
    ],
    detail: {
      todayCount: 0,
      totalCount: 520,
      completionRate: 0,
      remark: '等待生产批次完成后进行成品抽检,预计 16:00 开始。'
    }
  },
  {
    id: 'unqualified',
    title: '不合格品处理',
    desc: '返工/返修/报废',
    icon: 'Warning',
    status: 'pending',
    gradient: 'linear-gradient(135deg, #eb3349 0%, #f45c43 100%)',
    subNodes: [
      { name: '返工', status: 'done', meta: '处理 18 件' },
      { name: '返修', status: 'active', meta: '处理中 12 件' },
      { name: '报废', status: 'pending', meta: '待评审 11 件' }
    ],
    detail: {
      todayCount: 41,
      totalCount: 312,
      completionRate: 60,
      remark: '返工已完成,返修进行中,报废申请待质量主管评审。'
    }
  },
  {
    id: 'repair',
    title: '设备维修',
    desc: '报修-派工-维修-验收',
    icon: 'Tools',
    status: 'active',
    gradient: 'linear-gradient(135deg, #2193b0 0%, #6dd5ed 100%)',
    subNodes: [
      { name: '故障报修', status: 'done', meta: 'SMT贴片机01' },
      { name: '派工', status: 'done', meta: '张师傅' },
      { name: '维修', status: 'active', meta: '更换贴片头组件' },
      { name: '验收', status: 'pending', meta: '待维修完成' }
    ],
    detail: {
      todayCount: 7,
      totalCount: 96,
      completionRate: 50,
      remark: '当前 2 台设备维修中,1 台待验收,4 台待派工。'
    }
  }
])

const activeNode = computed(() => flowNodes.value.find(n => n.id === activeNodeId.value) || null)

function toggleNode(id) {
  activeNodeId.value = activeNodeId.value === id ? null : id
}

function getStatusText(status) {
  const map = { done: '已完成', active: '进行中', pending: '待处理' }
  return map[status] || '未知'
}

function getStatusTagType(status) {
  const map = { done: 'success', active: 'warning', pending: 'info' }
  return map[status] || 'info'
}

/* ============ 合格率图表 ============ */
const doughnutChartRef = ref(null)
let doughnutChart = null

const passRateData = ref([
  { type: 'IQC来料检验', passRate: 97.5, pass: 975, total: 1000, color: '#667eea' },
  { type: 'IPQC过程检验', passRate: 96.2, pass: 1924, total: 2000, color: '#38ef7d' },
  { type: 'FQC成品检验', passRate: 98.1, pass: 981, total: 1000, color: '#ffd200' }
])

const overallPassRate = computed(() => {
  const totalPass = passRateData.value.reduce((s, i) => s + i.pass, 0)
  const totalAll = passRateData.value.reduce((s, i) => s + i.total, 0)
  return ((totalPass / totalAll) * 100).toFixed(1)
})

function initDoughnutChart() {
  if (!doughnutChartRef.value) return
  const ctx = doughnutChartRef.value.getContext('2d')
  doughnutChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: passRateData.value.map(i => i.type),
      datasets: [
        {
          label: '合格数',
          data: passRateData.value.map(i => i.pass),
          backgroundColor: passRateData.value.map(i => i.color),
          borderColor: '#fff',
          borderWidth: 4,
          hoverOffset: 12,
          borderRadius: 6
        },
        {
          label: '不合格数',
          data: passRateData.value.map(i => i.total - i.pass),
          backgroundColor: passRateData.value.map(() => '#eef0f5'),
          borderColor: '#fff',
          borderWidth: 4,
          hoverOffset: 6
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '68%',
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: 'rgba(30, 41, 59, 0.95)',
          titleColor: '#fff',
          bodyColor: '#cbd5e1',
          padding: 12,
          cornerRadius: 8,
          callbacks: {
            label: function (context) {
              const idx = context.dataIndex
              const item = passRateData.value[idx]
              return ` ${context.dataset.label}: ${context.parsed} (合格率 ${item.passRate}%)`
            }
          }
        }
      },
      animation: {
        animateRotate: true,
        animateScale: true,
        duration: 1200,
        easing: 'easeOutQuart'
      }
    }
  })
}

function handleResize() {
  doughnutChart?.resize()
}

/* ============ 维修时间线 ============ */
const repairTimeline = ref([
  {
    time: '2026-07-15 09:30',
    title: '故障报修',
    desc: 'SMT贴片机01 - 贴片头拾取异常,影响贴装精度',
    operator: '李操作',
    status: 'done',
    icon: 'Warning'
  },
  {
    time: '2026-07-15 10:15',
    title: '已派工',
    desc: '已指派维修工程师,优先级:紧急',
    operator: '张师傅',
    status: 'done',
    icon: 'User'
  },
  {
    time: '2026-07-15 11:00',
    title: '维修中',
    desc: '更换贴片头组件,校准视觉定位系统',
    operator: '张师傅',
    status: 'active',
    icon: 'Tools'
  },
  {
    time: '2026-07-15 14:00',
    title: '待验收',
    desc: '预计维修完成后进行试产验收',
    operator: '陈质检',
    status: 'pending',
    icon: 'CircleCheck'
  }
])

function getTimelineType(status) {
  const map = { done: 'success', active: 'warning', pending: 'info' }
  return map[status] || 'info'
}

function getTimelineTagType(status) {
  const map = { done: 'success', active: 'warning', pending: 'info' }
  return map[status] || 'info'
}

function getTimelineStatusText(status) {
  const map = { done: '已完成', active: '进行中', pending: '待处理' }
  return map[status] || '未知'
}

/* ============ 不合格品列表 ============ */
const unqualifiedList = ref([
  {
    batchNo: 'NG-20260715-001',
    productName: '智能健康手环 Pro',
    inspectType: 'IQC',
    defectQty: 12,
    handleType: '返工',
    status: '已完成',
    handleTime: '2026-07-15 09:45'
  },
  {
    batchNo: 'NG-20260715-002',
    productName: '智能运动手环 Lite',
    inspectType: 'IPQC',
    defectQty: 8,
    handleType: '返修',
    status: '处理中',
    handleTime: '2026-07-15 10:20'
  },
  {
    batchNo: 'NG-20260715-003',
    productName: '智能健康手环 Pro',
    inspectType: 'IPQC',
    defectQty: 5,
    handleType: '报废',
    status: '待评审',
    handleTime: '2026-07-15 11:05'
  },
  {
    batchNo: 'NG-20260715-004',
    productName: '智能手表 Max',
    inspectType: 'FQC',
    defectQty: 6,
    handleType: '返工',
    status: '已完成',
    handleTime: '2026-07-15 11:30'
  },
  {
    batchNo: 'NG-20260715-005',
    productName: '智能运动手环 Lite',
    inspectType: 'IQC',
    defectQty: 10,
    handleType: '返修',
    status: '处理中',
    handleTime: '2026-07-15 13:15'
  }
])

function getInspectTagType(type) {
  const map = { IQC: 'primary', IPQC: 'success', FQC: 'warning' }
  return map[type] || 'info'
}

function getHandleTagType(type) {
  const map = { 返工: 'primary', 返修: 'warning', 报废: 'danger' }
  return map[type] || 'info'
}

function getHandleStatusType(status) {
  const map = { 已完成: 'success', 处理中: 'warning', 待评审: 'info' }
  return map[status] || 'info'
}

/* ============ 维修工单列表 ============ */
const repairOrderList = ref([
  {
    repairNo: 'RP-20260715-001',
    equipmentName: 'SMT贴片机01',
    faultType: '贴片头拾取异常',
    reportTime: '2026-07-15 09:30',
    repairman: '张师傅',
    status: '维修中',
    priority: '紧急'
  },
  {
    repairNo: 'RP-20260715-002',
    equipmentName: '回流焊机01',
    faultType: '温区温度异常',
    reportTime: '2026-07-15 10:05',
    repairman: '王师傅',
    status: '维修中',
    priority: '高'
  },
  {
    repairNo: 'RP-20260715-003',
    equipmentName: '自动组装线01',
    faultType: '气缸动作卡顿',
    reportTime: '2026-07-15 11:20',
    repairman: '张师傅',
    status: '待验收',
    priority: '中'
  },
  {
    repairNo: 'RP-20260715-004',
    equipmentName: '测试台02',
    faultType: '通讯连接失败',
    reportTime: '2026-07-15 12:10',
    repairman: '待派工',
    status: '待派工',
    priority: '中'
  },
  {
    repairNo: 'RP-20260715-005',
    equipmentName: '自动包装机01',
    faultType: '封口不严',
    reportTime: '2026-07-15 13:00',
    repairman: '李师傅',
    status: '已完成',
    priority: '低'
  },
  {
    repairNo: 'RP-20260715-006',
    equipmentName: 'SMT贴片机02',
    faultType: '导轨润滑不足',
    reportTime: '2026-07-15 13:45',
    repairman: '待派工',
    status: '待派工',
    priority: '低'
  }
])

function getRepairStatusType(status) {
  const map = { 维修中: 'warning', 待验收: 'primary', 待派工: 'info', 已完成: 'success' }
  return map[status] || 'info'
}

function getPriorityType(priority) {
  const map = { 紧急: 'danger', 高: 'warning', 中: 'primary', 低: 'info' }
  return map[priority] || 'info'
}

/* ============ 生命周期 ============ */
onMounted(() => {
  nextTick(() => {
    initDoughnutChart()
  })
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  doughnutChart?.destroy()
})
</script>

<style scoped>
.quality-repair-flow {
  width: 100%;
}

/* ============ 统计卡片 ============ */
.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  border-radius: 16px;
  padding: 22px 24px;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 18px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.18);
}

.stat-card-bg {
  position: absolute;
  top: -30px;
  right: -30px;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  backdrop-filter: blur(4px);
}

.stat-info {
  flex: 1;
  position: relative;
  z-index: 1;
}

.stat-value {
  font-size: 30px;
  font-weight: bold;
  line-height: 1.2;
  letter-spacing: 1px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 4px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  margin-top: 8px;
  opacity: 0.85;
}

.trend-text {
  opacity: 0.7;
  margin-left: 2px;
}

/* ============ 通用卡片头部 ============ */
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title .el-icon {
  color: #6366f1;
}

/* ============ 流程图 ============ */
.flow-card {
  border-radius: 16px;
  margin-bottom: 20px;
}

.flow-legend {
  display: flex;
  gap: 18px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
}

.legend-item .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.legend-item .dot.done { background: #22c55e; }
.legend-item .dot.active { background: #f59e0b; animation: dotPulse 1.5s infinite; }
.legend-item .dot.pending { background: #cbd5e1; }

@keyframes dotPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(245, 158, 11, 0.5); }
  50% { box-shadow: 0 0 0 6px rgba(245, 158, 11, 0); }
}

.flow-chart {
  display: flex;
  align-items: stretch;
  gap: 0;
  padding: 12px 4px 8px;
  overflow-x: auto;
}

.flow-node-wrapper {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 200px;
}

.flow-node {
  flex: 1;
  background: #fff;
  border: 2px solid #e2e8f0;
  border-radius: 14px;
  padding: 16px 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-height: 110px;
}

.flow-node:hover {
  border-color: #6366f1;
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.15);
  transform: translateY(-2px);
}

.flow-node.done {
  border-color: #86efac;
  background: linear-gradient(135deg, #f0fdf4 0%, #fff 100%);
}

.flow-node.active {
  border-color: #fbbf24;
  background: linear-gradient(135deg, #fffbeb 0%, #fff 100%);
  box-shadow: 0 0 0 4px rgba(251, 191, 36, 0.15);
}

.flow-node.pending {
  border-color: #e2e8f0;
  background: #f8fafc;
  opacity: 0.85;
}

.flow-node.expanded {
  border-color: #6366f1;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.2);
}

.node-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  position: relative;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.node-icon .node-pulse {
  position: absolute;
  inset: 0;
  border-radius: 14px;
  animation: nodePulse 2s infinite;
}

@keyframes nodePulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(245, 158, 11, 0.5); }
  50% { box-shadow: 0 0 0 10px rgba(245, 158, 11, 0); }
}

.flow-node.active .node-icon {
  animation: iconBreath 2.5s infinite;
}

@keyframes iconBreath {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.06); }
}

.node-content {
  flex: 1;
  min-width: 0;
}

.node-title {
  font-size: 15px;
  font-weight: bold;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 6px;
}

.node-subtitle {
  font-size: 11px;
  color: #fff;
  background: #6366f1;
  padding: 1px 6px;
  border-radius: 8px;
  font-weight: normal;
}

.node-desc {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.node-status {
  margin-top: 8px;
}

.node-expand {
  position: absolute;
  top: 12px;
  right: 12px;
  color: #94a3b8;
  font-size: 12px;
  transition: all 0.3s;
}

.flow-node:hover .node-expand {
  color: #6366f1;
}

/* 连接线 */
.flow-connector {
  flex: 0 0 60px;
  height: 110px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.connector-line {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 3px;
  background: #e2e8f0;
  border-radius: 2px;
  transform: translateY(-50%);
}

.connector-flow {
  position: absolute;
  top: 50%;
  left: 0;
  width: 30%;
  height: 3px;
  border-radius: 2px;
  transform: translateY(-50%);
  background: linear-gradient(90deg, transparent, #6366f1, transparent);
}

.flow-connector.done .connector-flow {
  background: linear-gradient(90deg, transparent, #22c55e, transparent);
  animation: flowMove 2s infinite linear;
}

.flow-connector.active .connector-flow {
  animation: flowMove 1.5s infinite linear;
}

@keyframes flowMove {
  0% { left: -30%; }
  100% { left: 100%; }
}

.connector-arrow {
  position: relative;
  z-index: 1;
  color: #94a3b8;
  background: #fff;
  padding: 0 2px;
}

.flow-connector.done .connector-arrow {
  color: #22c55e;
}

.flow-connector.active .connector-arrow {
  color: #f59e0b;
}

/* 节点详情面板 */
.node-detail-panel {
  margin-top: 20px;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  color: #fff;
}

.detail-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: bold;
}

.detail-close {
  background: rgba(255, 255, 255, 0.25) !important;
  border: none !important;
  color: #fff !important;
}

.detail-close:hover {
  background: rgba(255, 255, 255, 0.4) !important;
}

.detail-body {
  padding: 20px;
  background: #fff;
}

.detail-stat {
  background: #f8fafc;
  border-radius: 10px;
  padding: 14px 16px;
  text-align: center;
  border: 1px solid #f1f5f9;
}

.detail-stat-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 6px;
}

.detail-stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1e293b;
}

.sub-nodes-title {
  font-size: 14px;
  font-weight: bold;
  color: #1e293b;
  margin: 20px 0 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.sub-nodes-title::before {
  content: '';
  width: 4px;
  height: 16px;
  background: #6366f1;
  border-radius: 2px;
}

.sub-nodes {
  display: flex;
  align-items: flex-start;
  gap: 0;
  flex-wrap: wrap;
}

.sub-node {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  flex: 1;
  min-width: 140px;
  padding-right: 20px;
}

.sub-node-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: bold;
  flex-shrink: 0;
  z-index: 1;
  background: #e2e8f0;
  color: #64748b;
}

.sub-node.done .sub-node-dot {
  background: linear-gradient(135deg, #22c55e, #4ade80);
  color: #fff;
}

.sub-node.active .sub-node-dot {
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
  color: #fff;
  animation: dotPulse 1.5s infinite;
}

.sub-node-info {
  flex: 1;
  min-width: 0;
}

.sub-node-name {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}

.sub-node-meta {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

.sub-node-line {
  position: absolute;
  top: 16px;
  left: calc(100% - 20px);
  width: 20px;
  height: 2px;
  background: #e2e8f0;
}

.detail-remark {
  margin-top: 18px;
  padding: 12px 16px;
  background: #eff6ff;
  border-radius: 10px;
  border-left: 4px solid #6366f1;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #475569;
}

.detail-remark .el-icon {
  color: #6366f1;
  flex-shrink: 0;
}

/* 详情展开过渡 */
.detail-slide-enter-active,
.detail-slide-leave-active {
  transition: all 0.35s ease;
}

.detail-slide-enter-from,
.detail-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* ============ 图表行 ============ */
.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 16px;
  height: 100%;
}

.chart-content {
  display: flex;
  align-items: center;
  gap: 24px;
}

.doughnut-wrap {
  position: relative;
  width: 240px;
  height: 240px;
  flex-shrink: 0;
}

.chart-center-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  pointer-events: none;
}

.center-value {
  font-size: 28px;
  font-weight: bold;
  color: #1e293b;
  line-height: 1;
}

.center-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.rate-list {
  flex: 1;
  min-width: 0;
}

.rate-item {
  margin-bottom: 16px;
}

.rate-item:last-child {
  margin-bottom: 0;
}

.rate-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.rate-type {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 6px;
}

.rate-color {
  width: 10px;
  height: 10px;
  border-radius: 3px;
  display: inline-block;
}

.rate-value {
  font-size: 14px;
  font-weight: bold;
  color: #6366f1;
}

.rate-detail {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

/* ============ 时间线 ============ */
.timeline-wrap {
  max-height: 360px;
  overflow-y: auto;
  padding-right: 8px;
}

.timeline-card {
  background: #f8fafc;
  border-radius: 10px;
  padding: 10px 14px;
  border-left: 3px solid #e2e8f0;
  transition: all 0.2s;
}

.timeline-card:hover {
  background: #f1f5f9;
  transform: translateX(2px);
}

.timeline-card.done {
  border-left-color: #22c55e;
}

.timeline-card.active {
  border-left-color: #f59e0b;
  background: #fffbeb;
  animation: cardGlow 2s infinite;
}

@keyframes cardGlow {
  0%, 100% { box-shadow: 0 0 0 0 rgba(245, 158, 11, 0); }
  50% { box-shadow: 0 0 0 4px rgba(245, 158, 11, 0.1); }
}

.timeline-card.pending {
  border-left-color: #cbd5e1;
  opacity: 0.85;
}

.timeline-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: bold;
  color: #1e293b;
}

.timeline-icon {
  color: #6366f1;
}

.timeline-tag {
  margin-left: auto;
}

.timeline-desc {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
  line-height: 1.5;
}

.timeline-operator {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

/* ============ 表格行 ============ */
.table-row {
  margin-bottom: 20px;
}

.table-card {
  border-radius: 16px;
}

/* 滚动条美化 */
.timeline-wrap::-webkit-scrollbar,
.flow-chart::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.timeline-wrap::-webkit-scrollbar-thumb,
.flow-chart::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.timeline-wrap::-webkit-scrollbar-track,
.flow-chart::-webkit-scrollbar-track {
  background: transparent;
}
</style>
