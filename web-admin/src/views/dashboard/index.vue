<template>
  <div class="dashboard">
    <div class="welcome-section">
      <div class="welcome-left">
        <h2 class="welcome-title">Hello, {{ userStore.userInfo.realName || '管理员' }} 👋</h2>
        <p class="welcome-desc">Here's your production summary for today</p>
      </div>
    </div>

    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="item in statCards" :key="item.title">
        <div class="stat-card" :class="'card-' + item.type">
          <div class="stat-top">
            <div class="stat-label-group">
              <span class="stat-label">{{ item.title }}</span>
              <span class="stat-update">Updated 5 min ago</span>
            </div>
            <div class="stat-icon">
              <el-icon :size="18"><component :is="item.icon" /></el-icon>
            </div>
          </div>
          <div class="stat-main">
            <span class="stat-value">{{ item.value }}</span>
            <span class="stat-unit">{{ item.unit }}</span>
          </div>
          <div class="stat-bottom">
            <div class="stat-trend" :class="item.trendClass">
              <el-icon :size="12"><component :is="item.trendIcon" /></el-icon>
              <span>{{ item.trend }}</span>
            </div>
            <span class="stat-compare">vs last month</span>
          </div>
          <div class="stat-chart">
            <div class="mini-bar" v-for="(h, i) in item.chartData" :key="i" :style="{ height: h + '%' }"></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="16">
        <div class="chart-card">
          <div class="card-header">
            <div class="card-title-group">
              <h3 class="card-title">生产产量趋势</h3>
              <span class="card-subtitle">Production Trend</span>
            </div>
            <div class="card-actions">
              <div class="legend-group">
                <span class="legend-item">
                  <span class="legend-dot dot-income"></span>
                  实际产量
                </span>
                <span class="legend-item">
                  <span class="legend-dot dot-expense"></span>
                  目标产量
                </span>
              </div>
              <el-radio-group v-model="chartType" size="small" class="chart-tabs">
                <el-radio-button value="line">折线</el-radio-button>
                <el-radio-button value="bar">柱状</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div ref="productionChartRef" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="card-header">
            <div class="card-title-group">
              <h3 class="card-title">产品产量占比</h3>
              <span class="card-subtitle">Income Source</span>
            </div>
          </div>
          <div ref="pieChartRef" class="chart-container pie-chart"></div>
          <div class="pie-legend">
            <div class="legend-row" v-for="item in pieData" :key="item.name">
              <div class="legend-left">
                <span class="legend-dot" :style="{ background: item.color }"></span>
                <span class="legend-name">{{ item.name }}</span>
              </div>
              <div class="legend-right">
                <span class="legend-value">{{ item.value }}件</span>
                <span class="legend-percent">{{ item.percent }}%</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="8">
        <div class="chart-card">
          <div class="card-header">
            <div class="card-title-group">
              <h3 class="card-title">工单状态分布</h3>
              <span class="card-subtitle">当前工单统计</span>
            </div>
          </div>
          <div ref="statusChartRef" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card quality-card">
          <div class="card-header">
            <div class="card-title-group">
              <h3 class="card-title">质量合格率</h3>
              <span class="card-subtitle">本周质检数据</span>
            </div>
          </div>
          <div ref="qualityChartRef" class="chart-container gauge-chart"></div>
          <div class="quality-info">
            <div class="quality-item">
              <span class="quality-label">送检总数</span>
              <span class="quality-value">1,280</span>
            </div>
            <div class="quality-item">
              <span class="quality-label">合格数</span>
              <span class="quality-value success">1,235</span>
            </div>
            <div class="quality-item">
              <span class="quality-label">不合格数</span>
              <span class="quality-value danger">45</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card todo-card">
          <div class="card-header">
            <div class="card-title-group">
              <h3 class="card-title">待办事项</h3>
              <span class="card-subtitle">{{ todoList.filter(t => t.status === '待处理').length }} 项待处理</span>
            </div>
          </div>
          <div class="todo-list">
            <div
              v-for="(item, index) in todoList.slice(0, 5)"
              :key="index"
              class="todo-item"
              :class="{ 'todo-done': item.status === '已完成' }"
            >
              <div class="todo-dot" :class="item.status === '待处理' ? 'dot-warning' : 'dot-success'"></div>
              <div class="todo-content">
                <div class="todo-title">{{ item.title }}</div>
                <div class="todo-meta">
                  <span class="todo-time">{{ item.time }}</span>
                </div>
              </div>
              <el-tag :type="item.status === '待处理' ? 'warning' : 'success'" size="small" effect="light">
                {{ item.status }}
              </el-tag>
            </div>
            <div v-if="todoList.length === 0" class="empty-todo">
              <el-empty description="暂无待办事项" :image-size="60" />
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { useUserStore } from '../../stores/user'
import {
  Document, Setting, CircleCheck, Warning,
  Top, Bottom, Minus
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const productionChartRef = ref(null)
const pieChartRef = ref(null)
const statusChartRef = ref(null)
const qualityChartRef = ref(null)
const chartType = ref('line')

let productionChart = null
let pieChart = null
let statusChart = null
let qualityChart = null

const statCards = ref([
  {
    title: '今日产量',
    value: '820',
    unit: '件',
    icon: 'Document',
    type: 'primary',
    trend: '+12.5%',
    trendIcon: 'Top',
    trendClass: 'up',
    chartData: [40, 55, 45, 70, 60, 80, 65, 90, 75, 85, 70, 82]
  },
  {
    title: '生产中工单',
    value: '8',
    unit: '个',
    icon: 'Setting',
    type: 'success',
    trend: '+2',
    trendIcon: 'Top',
    trendClass: 'up',
    chartData: [30, 40, 35, 50, 45, 60, 55, 65, 60, 70, 65, 68]
  },
  {
    title: '待质检',
    value: '23',
    unit: '批次',
    icon: 'CircleCheck',
    type: 'warning',
    trend: '-15%',
    trendIcon: 'Bottom',
    trendClass: 'down',
    chartData: [70, 65, 60, 55, 50, 45, 50, 40, 45, 35, 40, 38]
  },
  {
    title: '库存预警',
    value: '8',
    unit: '种',
    icon: 'Warning',
    type: 'danger',
    trend: '0%',
    trendIcon: 'Minus',
    trendClass: 'normal',
    chartData: [40, 42, 38, 45, 40, 38, 42, 40, 38, 40, 42, 40]
  }
])

const todoList = ref([
  { title: '订单ORD-2024001待审批', status: '待处理', time: '10分钟前' },
  { title: '采购单PUR-2024002已到货', status: '待处理', time: '30分钟前' },
  { title: '质检任务Q-2024015完成', status: '已完成', time: '1小时前' },
  { title: '设备EQ-003维护提醒', status: '待处理', time: '2小时前' },
  { title: 'WO-2024001工单已完成', status: '已完成', time: '3小时前' }
])

const pieData = [
  { name: '智能健康手环 Pro', value: 3500, percent: 54, color: '#60A5FA' },
  { name: '智能运动手环 Lite', value: 2200, percent: 34, color: '#4ADE80' },
  { name: '智能手表 Max', value: 800, percent: 12, color: '#FB923C' }
]

const days = ['1月10日', '1月11日', '1月12日', '1月13日', '1月14日', '1月15日', '1月16日']
const productionData = [820, 932, 901, 934, 1290, 1330, 1280]
const targetData = [1000, 1000, 1000, 1100, 1200, 1300, 1300]

function initProductionChart() {
  if (!productionChartRef.value) return
  productionChart = echarts.init(productionChartRef.value)
  updateProductionChart()
}

function updateProductionChart() {
  if (!productionChart) return
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30, 41, 59, 0.9)',
      borderColor: 'transparent',
      borderWidth: 0,
      textStyle: {
        color: '#fff',
        fontSize: 12
      },
      padding: [10, 14],
      borderRadius: 8
    },
    grid: {
      left: '50px',
      right: '20px',
      bottom: '3%',
      top: '30px',
      containLabel: false
    },
    xAxis: {
      type: 'category',
      boundaryGap: chartType.value === 'bar',
      data: days,
      axisLine: {
        lineStyle: {
          color: '#f1f5f9'
        }
      },
      axisLabel: {
        color: '#94a3b8',
        fontSize: 12
      },
      axisTick: {
        show: false
      }
    },
    yAxis: {
      type: 'value',
      name: '产量(件)',
      nameTextStyle: {
        color: '#94a3b8',
        fontSize: 12,
        padding: [0, 0, 10, 0]
      },
      nameLocation: 'end',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#94a3b8',
        fontSize: 12
      },
      splitLine: {
        lineStyle: {
          color: '#f1f5f9',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '实际产量',
        type: chartType.value,
        data: productionData,
        smooth: true,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#93C5FD' },
            { offset: 1, color: '#60A5FA' }
          ]),
          borderRadius: chartType.value === 'bar' ? [6, 6, 0, 0] : 0
        },
        lineStyle: {
          width: 3,
          color: '#60A5FA'
        },
        areaStyle: chartType.value === 'line' ? {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(147, 197, 253, 0.25)' },
            { offset: 1, color: 'rgba(147, 197, 253, 0.02)' }
          ])
        } : undefined,
        barWidth: '22%'
      },
      {
        name: '目标产量',
        type: chartType.value,
        data: targetData,
        smooth: true,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#86EFAC' },
            { offset: 1, color: '#4ADE80' }
          ]),
          borderRadius: chartType.value === 'bar' ? [6, 6, 0, 0] : 0
        },
        lineStyle: {
          width: 2,
          type: 'dashed',
          color: '#4ADE80'
        },
        areaStyle: chartType.value === 'line' ? {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(134, 239, 172, 0.2)' },
            { offset: 1, color: 'rgba(134, 239, 172, 0.02)' }
          ])
        } : undefined,
        barWidth: '22%'
      }
    ]
  }
  productionChart.setOption(option)
}

function initPieChart() {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}件 ({d}%)',
      backgroundColor: 'rgba(30, 41, 59, 0.9)',
      borderColor: 'transparent',
      borderWidth: 0,
      textStyle: {
        color: '#fff',
        fontSize: 12
      },
      padding: [10, 14],
      borderRadius: 8
    },
    series: [
      {
        name: '产品产量',
        type: 'pie',
        radius: ['55%', '75%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: false
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.08)'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          {
            value: 3500,
            name: '智能健康手环 Pro',
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                { offset: 0, color: '#93C5FD' },
                { offset: 1, color: '#60A5FA' }
              ])
            }
          },
          {
            value: 2200,
            name: '智能运动手环 Lite',
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                { offset: 0, color: '#86EFAC' },
                { offset: 1, color: '#4ADE80' }
              ])
            }
          },
          {
            value: 800,
            name: '智能手表 Max',
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                { offset: 0, color: '#FDBA74' },
                { offset: 1, color: '#FB923C' }
              ])
            }
          }
        ]
      }
    ]
  }
  pieChart.setOption(option)
}

function initStatusChart() {
  if (!statusChartRef.value) return
  statusChart = echarts.init(statusChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      backgroundColor: 'rgba(30, 41, 59, 0.9)',
      borderColor: 'transparent',
      borderWidth: 0,
      textStyle: {
        color: '#fff',
        fontSize: 12
      },
      padding: [10, 14],
      borderRadius: 8
    },
    grid: {
      left: '3%',
      right: '10%',
      bottom: '3%',
      top: '10px',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#94a3b8',
        fontSize: 11
      },
      splitLine: {
        lineStyle: {
          color: '#f1f5f9',
          type: 'dashed'
        }
      }
    },
    yAxis: {
      type: 'category',
      data: ['已取消', '已完成', '生产中', '待生产'],
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#64748b',
        fontSize: 12
      }
    },
    series: [
      {
        name: '工单数',
        type: 'bar',
        data: [
          {
            value: 2,
            itemStyle: {
              borderRadius: [0, 6, 6, 0],
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#FCA5A5' },
                { offset: 1, color: '#F87171' }
              ])
            }
          },
          {
            value: 8,
            itemStyle: {
              borderRadius: [0, 6, 6, 0],
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#86EFAC' },
                { offset: 1, color: '#4ADE80' }
              ])
            }
          },
          {
            value: 5,
            itemStyle: {
              borderRadius: [0, 6, 6, 0],
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#93C5FD' },
                { offset: 1, color: '#60A5FA' }
              ])
            }
          },
          {
            value: 3,
            itemStyle: {
              borderRadius: [0, 6, 6, 0],
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#E2E8F0' },
                { offset: 1, color: '#CBD5E1' }
              ])
            }
          }
        ],
        barWidth: '50%',
        label: {
          show: true,
          position: 'right',
          color: '#64748b',
          fontSize: 12
        }
      }
    ]
  }
  statusChart.setOption(option)
}

function initQualityChart() {
  if (!qualityChartRef.value) return
  qualityChart = echarts.init(qualityChartRef.value)
  const option = {
    series: [
      {
        type: 'gauge',
        startAngle: 210,
        endAngle: -30,
        min: 0,
        max: 100,
        splitNumber: 10,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#93C5FD' },
            { offset: 0.5, color: '#86EFAC' },
            { offset: 1, color: '#4ADE80' }
          ])
        },
        progress: {
          show: true,
          width: 14,
          roundCap: true
        },
        pointer: {
          show: false
        },
        axisLine: {
          lineStyle: {
            width: 14,
            color: [[1, '#f1f5f9']]
          },
          roundCap: true
        },
        axisTick: {
          show: false
        },
        splitLine: {
          show: false
        },
        axisLabel: {
          show: false
        },
        anchor: {
          show: false
        },
        title: {
          show: false
        },
        detail: {
          valueAnimation: true,
          width: '60%',
          lineHeight: 36,
          borderRadius: 8,
          offsetCenter: [0, '-5%'],
          fontSize: 28,
          fontWeight: 'bold',
          formatter: '{value}%',
          color: '#1e293b'
        },
        data: [
          {
            value: 96.5
          }
        ]
      }
    ]
  }
  qualityChart.setOption(option)
}

function handleResize() {
  productionChart?.resize()
  pieChart?.resize()
  statusChart?.resize()
  qualityChart?.resize()
}

watch(chartType, () => {
  updateProductionChart()
})

onMounted(() => {
  nextTick(() => {
    initProductionChart()
    initPieChart()
    initStatusChart()
    initQualityChart()
    window.addEventListener('resize', handleResize)
  })
})

onUnmounted(() => {
  productionChart?.dispose()
  pieChart?.dispose()
  statusChart?.dispose()
  qualityChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard {
  width: 100%;
}

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.welcome-title {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  line-height: 1.3;
}

.welcome-desc {
  font-size: 13px;
  color: #94a3b8;
  margin-top: 6px;
}

.action-btn {
  border-radius: 8px;
  padding: 0 18px;
  height: 38px;
  font-weight: 500;
}

.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 18px 20px;
  border: 1px solid #f1f5f9;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  transform: translateY(-2px);
}

.stat-card.card-primary .stat-icon {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.stat-card.card-success .stat-icon {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.stat-card.card-warning .stat-icon {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.stat-card.card-danger .stat-icon {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.stat-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 14px;
}

.stat-label-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}

.stat-update {
  font-size: 10px;
  color: #cbd5e1;
}

.stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-main {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 12px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

.stat-unit {
  font-size: 13px;
  color: #94a3b8;
  font-weight: 500;
}

.stat-bottom {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 20px;
}

.stat-trend.up {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
}

.stat-trend.down {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.stat-trend.normal {
  color: #94a3b8;
  background: rgba(148, 163, 184, 0.1);
}

.stat-compare {
  font-size: 11px;
  color: #cbd5e1;
}

.stat-chart {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 36px;
}

.mini-bar {
  flex: 1;
  background: linear-gradient(180deg, rgba(59, 130, 246, 0.3) 0%, rgba(59, 130, 246, 0.1) 100%);
  border-radius: 2px 2px 0 0;
  min-height: 4px;
  transition: height 0.3s ease;
}

.card-success .mini-bar {
  background: linear-gradient(180deg, rgba(16, 185, 129, 0.3) 0%, rgba(16, 185, 129, 0.1) 100%);
}

.card-warning .mini-bar {
  background: linear-gradient(180deg, rgba(245, 158, 11, 0.3) 0%, rgba(245, 158, 11, 0.1) 100%);
}

.card-danger .mini-bar {
  background: linear-gradient(180deg, rgba(239, 68, 68, 0.3) 0%, rgba(239, 68, 68, 0.1) 100%);
}

.chart-row {
  margin-bottom: 16px;
}

.chart-card {
  background: #fff;
  border-radius: 14px;
  padding: 18px 20px;
  border: 1px solid #f1f5f9;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.card-title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.card-subtitle {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 500;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.legend-group {
  display: flex;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #64748b;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot-income {
  background: #60A5FA;
}

.dot-expense {
  background: #4ADE80;
}

.chart-tabs {
  --el-radio-button-checked-bg-color: #60A5FA;
  --el-radio-button-checked-border-color: #60A5FA;
}

.chart-container {
  height: 240px;
  width: 100%;
}

.pie-chart {
  height: 200px;
}

.pie-legend {
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.legend-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
}

.legend-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-name {
  font-size: 12px;
  color: #64748b;
}

.legend-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.legend-value {
  font-size: 12px;
  font-weight: 600;
  color: #1e293b;
}

.legend-percent {
  font-size: 11px;
  color: #94a3b8;
  min-width: 36px;
  text-align: right;
}

.gauge-chart {
  height: 180px;
}

.quality-info {
  display: flex;
  justify-content: space-around;
  padding-top: 8px;
  border-top: 1px solid #f1f5f9;
}

.quality-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.quality-label {
  font-size: 11px;
  color: #94a3b8;
}

.quality-value {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}

.quality-value.success {
  color: #10b981;
}

.quality-value.danger {
  color: #ef4444;
}

.todo-card {
  display: flex;
  flex-direction: column;
}

.todo-list {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
  transition: background 0.2s ease;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-item:hover {
  background: #f8fafc;
  margin: 0 -8px;
  padding: 10px 8px;
  border-radius: 8px;
}

.todo-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dot-warning {
  background: #f59e0b;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.15);
}

.dot-success {
  background: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.15);
}

.todo-content {
  flex: 1;
  min-width: 0;
}

.todo-title {
  font-size: 12px;
  color: #334155;
  margin-bottom: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.todo-done .todo-title {
  text-decoration: line-through;
  color: #94a3b8;
}

.todo-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.todo-time {
  font-size: 10px;
  color: #94a3b8;
}

.empty-todo {
  padding: 20px 0;
}
</style>
