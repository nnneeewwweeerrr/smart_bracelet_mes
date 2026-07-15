<template>
  <div class="archive-overview">
    <!-- 顶部统计卡片 -->
    <div class="stats-row">
      <div class="stat-card" v-for="item in statsCards" :key="item.label">
        <div class="stat-icon" :style="{ background: item.iconBg }">
          <el-icon :size="24"><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </div>
    </div>

    <!-- 中间主区域 -->
    <div class="main-area">
      <!-- 左侧客户面板 -->
      <div class="side-panel left-panel">
        <div class="panel-header">
          <span class="panel-title">客户列表</span>
          <el-input
            v-model="customerSearch"
            placeholder="搜索客户"
            size="small"
            clearable
            :prefix-icon="Search"
            class="panel-search"
          />
        </div>
        <div class="panel-list">
          <div
            v-for="c in filteredCustomers"
            :key="c.name"
            class="list-item customer-item"
            @mouseenter="highlightNode(c.name)"
            @mouseleave="unhighlightNode"
          >
            <div class="item-dot" :style="{ background: c.color }"></div>
            <div class="item-info">
              <div class="item-name">{{ c.name }}</div>
              <div class="item-meta">
                <span>合作金额: <b>¥{{ c.amount }}万</b></span>
                <span>订单: <b>{{ c.orders }}</b></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中心网络图 -->
      <div class="graph-area">
        <canvas ref="graphCanvas" class="graph-canvas"></canvas>
        <div v-if="hoveredNode" class="node-tooltip" :style="tooltipStyle">
          <div class="tooltip-name">{{ hoveredNode.name }}</div>
          <div class="tooltip-detail">{{ hoveredNode.detail }}</div>
        </div>
        <!-- 图例 -->
        <div class="graph-legend">
          <div class="legend-item">
            <span class="legend-dot" style="background:#7c3aed"></span>
            <span>SmartBracelet MES</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot" style="background:#3b82f6"></span>
            <span>客户</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot" style="background:#10b981"></span>
            <span>供应商</span>
          </div>
        </div>
      </div>

      <!-- 右侧供应商面板 -->
      <div class="side-panel right-panel">
        <div class="panel-header">
          <span class="panel-title">供应商列表</span>
          <el-input
            v-model="supplierSearch"
            placeholder="搜索供应商"
            size="small"
            clearable
            :prefix-icon="Search"
            class="panel-search"
          />
        </div>
        <div class="panel-list">
          <div
            v-for="s in filteredSuppliers"
            :key="s.name"
            class="list-item supplier-item"
            @mouseenter="highlightNode(s.name)"
            @mouseleave="unhighlightNode"
          >
            <div class="item-dot" :style="{ background: s.color }"></div>
            <div class="item-info">
              <div class="item-name">{{ s.name }}</div>
              <div class="item-meta">
                <span>{{ s.category }}</span>
                <span>¥{{ s.amount }}万</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部图表 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-title">客户地区分布</div>
        <canvas ref="customerChartRef" class="chart-canvas"></canvas>
      </div>
      <div class="chart-card">
        <div class="chart-title">供应商类别分布</div>
        <canvas ref="supplierChartRef" class="chart-canvas"></canvas>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, shallowRef } from 'vue'
import { Search, User, OfficeBuilding, Money, TrendCharts } from '@element-plus/icons-vue'
import Chart from 'chart.js/auto'

// ===== Mock 数据 =====
const customers = [
  { name: '华为', amount: 5280, orders: 342, region: '深圳', color: '#3b82f6', detail: '智能手环Pro系列主要客户，年度合作金额¥5280万' },
  { name: '小米', amount: 4150, orders: 276, region: '北京', color: '#60a5fa', detail: '手环标准版核心客户，年度合作金额¥4150万' },
  { name: 'OPPO', amount: 3620, orders: 198, region: '东莞', color: '#93c5fd', detail: '健康监测手环ODM客户，年度合作金额¥3620万' },
  { name: '京东', amount: 2980, orders: 520, region: '北京', color: '#2563eb', detail: '电商渠道核心合作伙伴，年度合作金额¥2980万' },
  { name: '大疆', amount: 2450, orders: 156, region: '深圳', color: '#1d4ed8', detail: '运动健康手环定制客户，年度合作金额¥2450万' },
  { name: '阿里', amount: 2100, orders: 189, region: '杭州', color: '#3b82f6', detail: 'IoT生态合作客户，年度合作金额¥2100万' },
  { name: '苏宁', amount: 1860, orders: 134, region: '南京', color: '#60a5fa', detail: '线下零售渠道客户，年度合作金额¥1860万' },
  { name: '字节跳动', amount: 1520, orders: 98, region: '北京', color: '#93c5fd', detail: '员工健康手环定制客户，年度合作金额¥1520万' },
  { name: '万科', amount: 980, orders: 45, region: '深圳', color: '#2563eb', detail: '社区健康管理项目客户，年度合作金额¥980万' },
  { name: '腾讯', amount: 1350, orders: 112, region: '深圳', color: '#1d4ed8', detail: '微信生态合作客户，年度合作金额¥1350万' }
]

const suppliers = [
  { name: '联发科技', category: '芯片供应商', amount: 3800, color: '#10b981', detail: 'MTK芯片核心供应商，年度采购¥3800万' },
  { name: '汇顶科技', category: '传感器供应商', amount: 2650, color: '#34d399', detail: '心率/血氧传感器供应商，年度采购¥2650万' },
  { name: '京东方', category: '屏幕供应商', amount: 2200, color: '#059669', detail: 'AMOLED屏幕供应商，年度采购¥2200万' },
  { name: '宁德时代', category: '电池供应商', amount: 1980, color: '#6ee7b7', detail: '锂电池核心供应商，年度采购¥1980万' },
  { name: '蓝思科技', category: '外壳供应商', amount: 1560, color: '#10b981', detail: '玻璃/陶瓷外壳供应商，年度采购¥1560万' },
  { name: '瑞声科技', category: '马达供应商', amount: 1200, color: '#34d399', detail: '线性马达供应商，年度采购¥1200万' },
  { name: '歌尔股份', category: '声学供应商', amount: 980, color: '#059669', detail: '扬声器/麦克风供应商，年度采购¥980万' },
  { name: '长盈精密', category: '结构件供应商', amount: 860, color: '#6ee7b7', detail: '金属结构件供应商，年度采购¥860万' },
  { name: '信维通信', category: '天线供应商', amount: 720, color: '#10b981', detail: '蓝牙/WiFi天线供应商，年度采购¥720万' },
  { name: '立讯精密', category: '连接器供应商', amount: 650, color: '#34d399', detail: 'FPC连接器供应商，年度采购¥650万' }
]

// ===== 统计卡片 =====
const totalCustomerAmount = customers.reduce((s, c) => s + c.amount, 0)
const activeSuppliers = suppliers.filter(s => s.amount > 800).length
const statsCards = [
  { label: '客户总数', value: customers.length, icon: User, iconBg: 'linear-gradient(135deg,#3b82f6,#1d4ed8)' },
  { label: '供应商总数', value: suppliers.length, icon: OfficeBuilding, iconBg: 'linear-gradient(135deg,#10b981,#059669)' },
  { label: '合作总金额', value: `¥${(totalCustomerAmount / 10000).toFixed(1)}亿`, icon: Money, iconBg: 'linear-gradient(135deg,#f59e0b,#d97706)' },
  { label: '活跃供应商', value: activeSuppliers, icon: TrendCharts, iconBg: 'linear-gradient(135deg,#8b5cf6,#6d28d9)' }
]

// ===== 搜索 =====
const customerSearch = ref('')
const supplierSearch = ref('')
const filteredCustomers = computed(() => {
  if (!customerSearch.value) return customers
  return customers.filter(c => c.name.includes(customerSearch.value))
})
const filteredSuppliers = computed(() => {
  if (!supplierSearch.value) return suppliers
  return suppliers.filter(s => s.name.includes(supplierSearch.value) || s.category.includes(supplierSearch.value))
})

// ===== Canvas 力导向网络图 =====
const graphCanvas = ref(null)
const hoveredNode = shallowRef(null)
const tooltipStyle = reactive({ left: '0px', top: '0px' })

// 构建图节点和边
let nodes = []
let edges = []
let animationId = null
let dragNode = null
let highlightName = ''
let canvasCtx = null
let canvasWidth = 0
let canvasHeight = 0

function buildGraph() {
  const cx = canvasWidth / 2
  const cy = canvasHeight / 2
  nodes = []

  // 中心节点
  nodes.push({
    id: 'center',
    name: 'SmartBracelet MES',
    x: cx,
    y: cy,
    vx: 0,
    vy: 0,
    radius: 40,
    color: '#7c3aed',
    glowColor: 'rgba(124,58,237,0.6)',
    type: 'center',
    detail: '智能手环MES系统，连接8大客户与10大供应商'
  })

  // 客户节点 - 左半部分散布
  const customerCount = customers.length
  customers.forEach((c, i) => {
    const angle = (Math.PI * 0.6) + (Math.PI * 0.8) * (i / (customerCount - 1))
    const dist = 180 + Math.random() * 60
    nodes.push({
      id: `customer_${i}`,
      name: c.name,
      x: cx + Math.cos(angle) * dist,
      y: cy + Math.sin(angle) * dist,
      vx: 0,
      vy: 0,
      radius: 14 + (c.amount / 5280) * 16,
      color: c.color,
      glowColor: c.color.replace(')', ',0.5)').replace('rgb', 'rgba'),
      type: 'customer',
      detail: c.detail,
      amount: c.amount
    })
  })

  // 供应商节点 - 右半部分散布
  const supplierCount = suppliers.length
  suppliers.forEach((s, i) => {
    const angle = (-Math.PI * 0.4) + (Math.PI * 0.8) * (i / (supplierCount - 1))
    const dist = 180 + Math.random() * 60
    nodes.push({
      id: `supplier_${i}`,
      name: s.name,
      x: cx + Math.cos(angle) * dist,
      y: cy + Math.sin(angle) * dist,
      vx: 0,
      vy: 0,
      radius: 14 + (s.amount / 3800) * 16,
      color: s.color,
      glowColor: s.color.replace(')', ',0.5)').replace('rgb', 'rgba'),
      type: 'supplier',
      detail: s.detail,
      amount: s.amount
    })
  })

  // 构建边：所有节点连接到中心
  edges = []
  for (let i = 1; i < nodes.length; i++) {
    const maxAmount = nodes[i].type === 'customer' ? 5280 : 3800
    const weight = nodes[i].amount / maxAmount
    edges.push({
      source: 0,
      target: i,
      weight: weight
    })
  }
}

// 力导向模拟
function simulate() {
  const alpha = 0.3
  const repulsion = 8000
  const attraction = 0.005
  const damping = 0.85
  const cx = canvasWidth / 2
  const cy = canvasHeight / 2

  // 重置力
  for (const n of nodes) {
    n.fx = 0
    n.fy = 0
  }

  // 斥力
  for (let i = 0; i < nodes.length; i++) {
    for (let j = i + 1; j < nodes.length; j++) {
      const dx = nodes[j].x - nodes[i].x
      const dy = nodes[j].y - nodes[i].y
      const dist = Math.max(Math.sqrt(dx * dx + dy * dy), 1)
      const force = repulsion / (dist * dist)
      const fx = (dx / dist) * force
      const fy = (dy / dist) * force
      nodes[i].fx -= fx
      nodes[i].fy -= fy
      nodes[j].fx += fx
      nodes[j].fy += fy
    }
  }

  // 引力（弹簧）
  for (const edge of edges) {
    const source = nodes[edge.source]
    const target = nodes[edge.target]
    const dx = target.x - source.x
    const dy = target.y - source.y
    const dist = Math.sqrt(dx * dx + dy * dy)
    const idealDist = 220
    const force = (dist - idealDist) * attraction
    const fx = (dx / Math.max(dist, 1)) * force
    const fy = (dy / Math.max(dist, 1)) * force
    source.fx += fx
    source.fy += fy
    target.fx -= fx
    target.fy -= fy
  }

  // 中心引力
  for (const n of nodes) {
    if (n.type === 'center') continue
    n.fx += (cx - n.x) * 0.001
    n.fy += (cy - n.y) * 0.001
  }

  // 更新位置
  for (const n of nodes) {
    if (n === dragNode) continue
    n.vx = (n.vx + n.fx * alpha) * damping
    n.vy = (n.vy + n.fy * alpha) * damping
    n.x += n.vx
    n.y += n.vy
    // 边界约束
    const pad = n.radius + 5
    n.x = Math.max(pad, Math.min(canvasWidth - pad, n.x))
    n.y = Math.max(pad, Math.min(canvasHeight - pad, n.y))
  }
}

// 绘制
function drawGraph() {
  if (!canvasCtx) return
  const ctx = canvasCtx
  ctx.clearRect(0, 0, canvasWidth, canvasHeight)

  // 绘制边
  for (const edge of edges) {
    const source = nodes[edge.source]
    const target = nodes[edge.target]
    const isHighlight = highlightName && (target.name === highlightName || source.name === highlightName)
    ctx.beginPath()
    ctx.moveTo(source.x, source.y)
    ctx.lineTo(target.x, target.y)

    if (isHighlight) {
      ctx.strokeStyle = target.name === highlightName ? target.color : source.color
      ctx.globalAlpha = 0.8
      ctx.lineWidth = 2 + edge.weight * 4
    } else if (highlightName) {
      ctx.strokeStyle = '#334155'
      ctx.globalAlpha = 0.15
      ctx.lineWidth = 1 + edge.weight * 2
    } else {
      ctx.strokeStyle = '#475569'
      ctx.globalAlpha = 0.35
      ctx.lineWidth = 1 + edge.weight * 3
    }
    ctx.stroke()
    ctx.globalAlpha = 1
  }

  // 绘制节点
  const time = Date.now() / 1000
  for (const n of nodes) {
    const isHighlight = highlightName === n.name
    const isCenter = n.type === 'center'
    const pulseRadius = isCenter ? n.radius + Math.sin(time * 2) * 3 : n.radius
    const r = isHighlight ? n.radius * 1.25 : pulseRadius

    // 发光效果
    const glowSize = isHighlight ? 40 : (isCenter ? 30 : 20)
    const gradient = ctx.createRadialGradient(n.x, n.y, r * 0.3, n.x, n.y, r + glowSize)
    gradient.addColorStop(0, n.color)
    gradient.addColorStop(0.5, n.glowColor || n.color.replace(')', ',0.3)').replace('rgb', 'rgba'))
    gradient.addColorStop(1, 'rgba(0,0,0,0)')
    ctx.beginPath()
    ctx.arc(n.x, n.y, r + glowSize, 0, Math.PI * 2)
    ctx.fillStyle = gradient
    ctx.globalAlpha = isHighlight ? 0.9 : (highlightName ? 0.25 : 0.6)
    ctx.fill()
    ctx.globalAlpha = 1

    // 节点主体
    ctx.beginPath()
    ctx.arc(n.x, n.y, r, 0, Math.PI * 2)
    const bodyGrad = ctx.createRadialGradient(n.x - r * 0.3, n.y - r * 0.3, r * 0.1, n.x, n.y, r)
    bodyGrad.addColorStop(0, '#ffffff')
    bodyGrad.addColorStop(0.3, n.color)
    bodyGrad.addColorStop(1, n.color)
    ctx.fillStyle = bodyGrad
    ctx.globalAlpha = isHighlight ? 1 : (highlightName ? 0.4 : 0.95)
    ctx.fill()
    ctx.globalAlpha = 1

    // 节点边框
    ctx.strokeStyle = isHighlight ? '#ffffff' : n.color
    ctx.lineWidth = isHighlight ? 2.5 : 1
    ctx.globalAlpha = isHighlight ? 1 : (highlightName ? 0.4 : 0.7)
    ctx.stroke()
    ctx.globalAlpha = 1

    // 节点标签
    const showLabel = isHighlight || isCenter || !highlightName
    if (showLabel) {
      ctx.font = isCenter ? 'bold 13px sans-serif' : '11px sans-serif'
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      ctx.fillStyle = isHighlight ? '#ffffff' : (isCenter ? '#e2e8f0' : '#cbd5e1')
      ctx.shadowColor = 'rgba(0,0,0,0.8)'
      ctx.shadowBlur = 4
      ctx.fillText(n.name, n.x, n.y + r + 16)
      ctx.shadowBlur = 0
    }
  }
}

function animationLoop() {
  simulate()
  drawGraph()
  animationId = requestAnimationFrame(animationLoop)
}

// 交互
function getNodeAt(x, y) {
  for (let i = nodes.length - 1; i >= 0; i--) {
    const n = nodes[i]
    const dx = x - n.x
    const dy = y - n.y
    if (dx * dx + dy * dy < (n.radius + 8) * (n.radius + 8)) return n
  }
  return null
}

function getCanvasPos(e) {
  const rect = graphCanvas.value.getBoundingClientRect()
  const scaleX = canvasWidth / rect.width
  const scaleY = canvasHeight / rect.height
  return {
    x: (e.clientX - rect.left) * scaleX,
    y: (e.clientY - rect.top) * scaleY
  }
}

let isDragging = false
let dragOffsetX = 0
let dragOffsetY = 0

function onCanvasMouseDown(e) {
  const pos = getCanvasPos(e)
  const node = getNodeAt(pos.x, pos.y)
  if (node) {
    dragNode = node
    isDragging = false
    dragOffsetX = pos.x - node.x
    dragOffsetY = pos.y - node.y
  }
}

function onCanvasMouseMove(e) {
  const pos = getCanvasPos(e)
  if (dragNode) {
    isDragging = true
    dragNode.x = pos.x - dragOffsetX
    dragNode.y = pos.y - dragOffsetY
    dragNode.vx = 0
    dragNode.vy = 0
  } else {
    // hover
    const node = getNodeAt(pos.x, pos.y)
    if (node) {
      hoveredNode.value = node
      const rect = graphCanvas.value.getBoundingClientRect()
      tooltipStyle.left = (e.clientX - rect.left + 15) + 'px'
      tooltipStyle.top = (e.clientY - rect.top - 10) + 'px'
      graphCanvas.value.style.cursor = 'pointer'
    } else {
      hoveredNode.value = null
      graphCanvas.value.style.cursor = 'default'
    }
  }
}

function onCanvasMouseUp() {
  dragNode = null
  isDragging = false
}

function onCanvasMouseLeave() {
  dragNode = null
  isDragging = false
  hoveredNode.value = null
}

function highlightNode(name) {
  highlightName = name
}

function unhighlightNode() {
  highlightName = ''
}

// ===== Chart.js 底部图表 =====
const customerChartRef = ref(null)
const supplierChartRef = ref(null)
let customerChart = null
let supplierChart = null

function initCharts() {
  // 客户地区分布
  const regionMap = {}
  customers.forEach(c => {
    regionMap[c.region] = (regionMap[c.region] || 0) + c.amount
  })
  const regionLabels = Object.keys(regionMap)
  const regionData = Object.values(regionMap)
  const regionColors = ['#3b82f6', '#60a5fa', '#93c5fd', '#2563eb', '#1d4ed8', '#1e40af']

  customerChart = new Chart(customerChartRef.value, {
    type: 'doughnut',
    data: {
      labels: regionLabels,
      datasets: [{
        data: regionData,
        backgroundColor: regionColors.slice(0, regionLabels.length),
        borderColor: '#0f172a',
        borderWidth: 2,
        hoverBorderColor: '#ffffff',
        hoverBorderWidth: 2
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '55%',
      plugins: {
        legend: {
          position: 'right',
          labels: { color: '#94a3b8', font: { size: 12 }, padding: 12, usePointStyle: true }
        },
        tooltip: {
          backgroundColor: '#1e293b',
          titleColor: '#e2e8f0',
          bodyColor: '#94a3b8',
          borderColor: '#334155',
          borderWidth: 1,
          callbacks: {
            label: (ctx) => `${ctx.label}: ¥${ctx.raw}万`
          }
        }
      }
    }
  })

  // 供应商类别分布
  const categoryMap = {}
  suppliers.forEach(s => {
    const cat = s.category.replace('供应商', '')
    categoryMap[cat] = (categoryMap[cat] || 0) + s.amount
  })
  const catLabels = Object.keys(categoryMap)
  const catData = Object.values(categoryMap)
  const catColors = ['#10b981', '#34d399', '#6ee7b7', '#059669', '#047857', '#065f46', '#0d9488', '#14b8a6', '#2dd4bf', '#5eead4']

  supplierChart = new Chart(supplierChartRef.value, {
    type: 'doughnut',
    data: {
      labels: catLabels,
      datasets: [{
        data: catData,
        backgroundColor: catColors.slice(0, catLabels.length),
        borderColor: '#0f172a',
        borderWidth: 2,
        hoverBorderColor: '#ffffff',
        hoverBorderWidth: 2
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '55%',
      plugins: {
        legend: {
          position: 'right',
          labels: { color: '#94a3b8', font: { size: 12 }, padding: 12, usePointStyle: true }
        },
        tooltip: {
          backgroundColor: '#1e293b',
          titleColor: '#e2e8f0',
          bodyColor: '#94a3b8',
          borderColor: '#334155',
          borderWidth: 1,
          callbacks: {
            label: (ctx) => `${ctx.label}: ¥${ctx.raw}万`
          }
        }
      }
    }
  })
}

// ===== 初始化 =====
onMounted(async () => {
  await nextTick()
  const canvas = graphCanvas.value
  const container = canvas.parentElement
  const dpr = window.devicePixelRatio || 1

  canvasWidth = container.clientWidth
  canvasHeight = container.clientHeight
  canvas.width = canvasWidth * dpr
  canvas.height = canvasHeight * dpr
  canvas.style.width = canvasWidth + 'px'
  canvas.style.height = canvasHeight + 'px'

  canvasCtx = canvas.getContext('2d')
  canvasCtx.scale(dpr, dpr)

  buildGraph()
  animationLoop()

  canvas.addEventListener('mousedown', onCanvasMouseDown)
  canvas.addEventListener('mousemove', onCanvasMouseMove)
  canvas.addEventListener('mouseup', onCanvasMouseUp)
  canvas.addEventListener('mouseleave', onCanvasMouseLeave)

  initCharts()
})

onBeforeUnmount(() => {
  if (animationId) cancelAnimationFrame(animationId)
  if (customerChart) customerChart.destroy()
  if (supplierChart) supplierChart.destroy()
  const canvas = graphCanvas.value
  if (canvas) {
    canvas.removeEventListener('mousedown', onCanvasMouseDown)
    canvas.removeEventListener('mousemove', onCanvasMouseMove)
    canvas.removeEventListener('mouseup', onCanvasMouseUp)
    canvas.removeEventListener('mouseleave', onCanvasMouseLeave)
  }
})
</script>

<style scoped>
.archive-overview {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(160deg, #0a0e1a 0%, #111827 40%, #0f172a 100%);
  padding: 20px;
  box-sizing: border-box;
  color: #e2e8f0;
}

/* 统计卡片 */
.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(30, 41, 59, 0.7);
  border: 1px solid rgba(71, 85, 105, 0.4);
  border-radius: 12px;
  padding: 18px 22px;
  backdrop-filter: blur(8px);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #f1f5f9;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #94a3b8;
  margin-top: 2px;
}

/* 主区域 */
.main-area {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  height: 520px;
}

/* 侧面板 */
.side-panel {
  width: 260px;
  background: rgba(30, 41, 59, 0.65);
  border: 1px solid rgba(71, 85, 105, 0.4);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(8px);
  flex-shrink: 0;
}

.panel-header {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(71, 85, 105, 0.4);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #e2e8f0;
}

.panel-search :deep(.el-input__wrapper) {
  background: rgba(15, 23, 42, 0.6);
  border-color: rgba(71, 85, 105, 0.5);
  box-shadow: none;
}

.panel-search :deep(.el-input__inner) {
  color: #cbd5e1;
  font-size: 12px;
}

.panel-search :deep(.el-input__inner::placeholder) {
  color: #64748b;
}

.panel-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.panel-list::-webkit-scrollbar {
  width: 4px;
}

.panel-list::-webkit-scrollbar-thumb {
  background: rgba(100, 116, 139, 0.4);
  border-radius: 4px;
}

.list-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.list-item:hover {
  background: rgba(71, 85, 105, 0.35);
}

.item-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
  box-shadow: 0 0 6px currentColor;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 13px;
  font-weight: 600;
  color: #e2e8f0;
  margin-bottom: 2px;
}

.item-meta {
  display: flex;
  gap: 12px;
  font-size: 11px;
  color: #94a3b8;
}

.item-meta b {
  color: #cbd5e1;
}

/* 网络图 */
.graph-area {
  flex: 1;
  position: relative;
  background: rgba(15, 23, 42, 0.5);
  border: 1px solid rgba(71, 85, 105, 0.4);
  border-radius: 12px;
  overflow: hidden;
}

.graph-canvas {
  width: 100%;
  height: 100%;
  display: block;
}

.node-tooltip {
  position: absolute;
  background: rgba(15, 23, 42, 0.92);
  border: 1px solid rgba(124, 58, 237, 0.5);
  border-radius: 8px;
  padding: 10px 14px;
  pointer-events: none;
  z-index: 10;
  max-width: 280px;
  backdrop-filter: blur(8px);
}

.tooltip-name {
  font-size: 14px;
  font-weight: 600;
  color: #f1f5f9;
  margin-bottom: 4px;
}

.tooltip-detail {
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.5;
}

.graph-legend {
  position: absolute;
  top: 14px;
  right: 14px;
  display: flex;
  gap: 16px;
  background: rgba(15, 23, 42, 0.75);
  border: 1px solid rgba(71, 85, 105, 0.3);
  border-radius: 8px;
  padding: 8px 14px;
  backdrop-filter: blur(6px);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #94a3b8;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

/* 底部图表 */
.charts-row {
  display: flex;
  gap: 16px;
}

.chart-card {
  flex: 1;
  background: rgba(30, 41, 59, 0.65);
  border: 1px solid rgba(71, 85, 105, 0.4);
  border-radius: 12px;
  padding: 18px;
  backdrop-filter: blur(8px);
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #e2e8f0;
  margin-bottom: 14px;
}

.chart-canvas {
  width: 100% !important;
  height: 240px !important;
}
</style>
