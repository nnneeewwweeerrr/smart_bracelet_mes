<template>
  <div class="ai-stats-page">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="item in statCards" :key="item.title">
        <div class="stat-card" :class="'card-' + item.type">
          <div class="stat-top">
            <div class="stat-label-group">
              <span class="stat-label">{{ item.title }}</span>
              <span class="stat-sub">{{ item.subtitle }}</span>
            </div>
            <div class="stat-icon">
              <el-icon :size="20"><component :is="item.icon" /></el-icon>
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
            <span class="stat-compare">较上周</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="14">
        <div class="chart-card">
          <div class="card-header">
            <div class="card-title-group">
              <h3 class="card-title">近7天提问趋势</h3>
              <span class="card-subtitle">Question Trend</span>
            </div>
          </div>
          <div ref="trendChartRef" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="chart-card">
          <div class="card-header">
            <div class="card-title-group">
              <h3 class="card-title">高频问题TOP10</h3>
              <span class="card-subtitle">Top Questions</span>
            </div>
          </div>
          <div ref="topChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 最近对话记录 -->
    <div class="chart-card">
      <div class="card-header">
        <div class="card-title-group">
          <h3 class="card-title">最近对话记录</h3>
          <span class="card-subtitle">Recent Conversations</span>
        </div>
      </div>
      <el-table
        :data="recentList"
        v-loading="loading"
        style="width: 100%"
        class="dark-table"
        :header-cell-style="{ background: 'rgba(14,165,233,0.08)', color: '#94a3b8', borderBottom: '1px solid rgba(14,165,233,0.15)' }"
        :row-style="{ background: 'transparent' }"
        :cell-style="{ borderBottom: '1px solid rgba(14,165,233,0.08)' }"
      >
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="userName" label="提问人" width="120" />
        <el-table-column prop="question" label="问题" min-width="240" show-overflow-tooltip />
        <el-table-column prop="answer" label="回答摘要" min-width="280" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="answer-text">{{ row.answer }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="resolved" label="状态" width="100" align="center">
          <template #default="{ row }">
            <span class="status-tag" :class="row.resolved ? 'resolved' : 'unresolved'">
              {{ row.resolved ? '已解决' : '未解决' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="responseTime" label="响应耗时" width="110" align="center">
          <template #default="{ row }">
            <span class="response-time">{{ row.responseTime }}s</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提问时间" width="180" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getAiStats } from '../../api/ai'
import {
  ChatDotRound, Document, CircleCheck, Timer,
  Top, Bottom
} from '@element-plus/icons-vue'

const loading = ref(false)
const trendChartRef = ref(null)
const topChartRef = ref(null)
let trendChart = null
let topChart = null

const statCards = ref([
  {
    title: '总提问数',
    subtitle: 'Total Questions',
    value: '1,286',
    unit: '次',
    icon: 'ChatDotRound',
    type: 'primary',
    trend: '+8.5%',
    trendIcon: 'Top',
    trendClass: 'up'
  },
  {
    title: '今日提问',
    subtitle: 'Today Questions',
    value: '47',
    unit: '次',
    icon: 'Document',
    type: 'success',
    trend: '+12.3%',
    trendIcon: 'Top',
    trendClass: 'up'
  },
  {
    title: '解决率',
    subtitle: 'Resolution Rate',
    value: '92.6',
    unit: '%',
    icon: 'CircleCheck',
    type: 'warning',
    trend: '+3.2%',
    trendIcon: 'Top',
    trendClass: 'up'
  },
  {
    title: '平均响应',
    subtitle: 'Avg Response',
    value: '1.8',
    unit: 's',
    icon: 'Timer',
    type: 'danger',
    trend: '-0.3s',
    trendIcon: 'Bottom',
    trendClass: 'down'
  }
])

const recentList = ref([])

const mockTrendData = {
  days: ['7月6日', '7月7日', '7月8日', '7月9日', '7月10日', '7月11日', '7月12日'],
  values: [38, 52, 45, 61, 73, 68, 47]
}

const mockTopData = [
  { name: '焊接温度标准', count: 86 },
  { name: '屏幕贴合气泡处理', count: 72 },
  { name: '报工操作流程', count: 65 },
  { name: '防水测试方法', count: 58 },
  { name: 'SMT贴片参数', count: 51 },
  { name: '工单进度查询', count: 47 },
  { name: '物料库存预警', count: 42 },
  { name: '设备维护周期', count: 38 },
  { name: '质检标准规范', count: 35 },
  { name: '异常处理流程', count: 31 }
]

const mockRecentList = [
  {
    userName: '李主管',
    question: 'WO-2024001工单当前生产进度如何？',
    answer: '工单WO-2024001当前状态为生产中，计划数量1000件，已完成650件，完成进度65%。当前处于组装工序...',
    resolved: true,
    responseTime: 1.5,
    createTime: '2026-07-12 10:30:00'
  },
  {
    userName: '王质检',
    question: '屏幕贴合有气泡怎么处理？',
    answer: '屏幕贴合产生气泡的主要原因包括：无尘环境不达标、贴合压力不均、胶水固化不充分等。建议处理方案...',
    resolved: true,
    responseTime: 2.1,
    createTime: '2026-07-12 09:45:00'
  },
  {
    userName: '张工程师',
    question: '回流焊接温度标准是多少？',
    answer: '回流焊接温度标准根据焊锡膏型号不同有所差异。一般铅锡合金峰值温度为210-230°C，无铅焊锡...',
    resolved: true,
    responseTime: 1.3,
    createTime: '2026-07-12 09:20:00'
  },
  {
    userName: '赵仓管',
    question: '电池物料库存不足怎么预警？',
    answer: '当电池物料库存低于安全库存阈值时，系统会自动触发预警。建议设置安全库存为3天用量...',
    resolved: false,
    responseTime: 2.8,
    createTime: '2026-07-11 16:30:00'
  },
  {
    userName: '孙质检',
    question: '防水测试IP67标准测试流程是什么？',
    answer: 'IP67防水测试包括两个部分：IP6X防尘测试和IPX7防水测试。防水测试需将产品浸入1米深水中30分钟...',
    resolved: true,
    responseTime: 1.9,
    createTime: '2026-07-11 15:10:00'
  },
  {
    userName: '李主管',
    question: '智能健康手环Pro的SOP操作规范有哪些？',
    answer: '智能健康手环Pro标准工艺包含SMT贴片、回流焊接、组装、测试、包装5个工序。各工序SOP规范如下...',
    resolved: true,
    responseTime: 2.5,
    createTime: '2026-07-11 14:00:00'
  },
  {
    userName: '张工程师',
    question: 'SMT贴片机01出现告警怎么处理？',
    answer: 'SMT贴片机告警常见原因包括：贴片精度下降、供料器异常、吸嘴堵塞等。建议按以下步骤排查...',
    resolved: true,
    responseTime: 1.7,
    createTime: '2026-07-11 11:25:00'
  },
  {
    userName: '王质检',
    question: '组装工序外观不良率超标如何排查？',
    answer: '外观不良率超标需从人、机、料、法、环五个方面排查。建议首先确认作业员操作是否规范...',
    resolved: false,
    responseTime: 3.2,
    createTime: '2026-07-10 16:45:00'
  }
]

function initTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(3, 10, 26, 0.9)',
      borderColor: 'rgba(14, 165, 233, 0.3)',
      borderWidth: 1,
      textStyle: { color: '#e2e8f0', fontSize: 12 },
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
      boundaryGap: false,
      data: mockTrendData.days,
      axisLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.2)' } },
      axisLabel: { color: '#94a3b8', fontSize: 12 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      name: '提问数(次)',
      nameTextStyle: { color: '#64748b', fontSize: 12, padding: [0, 0, 10, 0] },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 12 },
      splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.1)', type: 'dashed' } }
    },
    series: [
      {
        name: '提问数',
        type: 'line',
        data: mockTrendData.values,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: {
          color: '#38bdf8',
          borderColor: '#fff',
          borderWidth: 2,
          shadowColor: 'rgba(56, 189, 248, 0.5)',
          shadowBlur: 10
        },
        lineStyle: { width: 3, color: '#38bdf8' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(56, 189, 248, 0.35)' },
            { offset: 1, color: 'rgba(56, 189, 248, 0.02)' }
          ])
        }
      }
    ]
  }
  trendChart.setOption(option)
}

function initTopChart() {
  if (!topChartRef.value) return
  topChart = echarts.init(topChartRef.value)
  const data = [...mockTopData].reverse()
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(3, 10, 26, 0.9)',
      borderColor: 'rgba(14, 165, 233, 0.3)',
      borderWidth: 1,
      textStyle: { color: '#e2e8f0', fontSize: 12 },
      padding: [10, 14],
      borderRadius: 8
    },
    grid: {
      left: '3%',
      right: '12%',
      bottom: '3%',
      top: '10px',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#64748b', fontSize: 11 },
      splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.1)', type: 'dashed' } }
    },
    yAxis: {
      type: 'category',
      data: data.map(d => d.name),
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 11 }
    },
    series: [
      {
        name: '提问次数',
        type: 'bar',
        data: data.map((d, i) => ({
          value: d.count,
          itemStyle: {
            borderRadius: [0, 6, 6, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: i >= 7 ? 'rgba(56, 189, 248, 0.5)' : 'rgba(56, 189, 248, 0.8)' },
              { offset: 1, color: i >= 7 ? '#0369a1' : '#0ea5e9' }
            ])
          }
        })),
        barWidth: '55%',
        label: {
          show: true,
          position: 'right',
          color: '#38bdf8',
          fontSize: 11,
          fontWeight: 600
        }
      }
    ]
  }
  topChart.setOption(option)
}

function handleResize() {
  trendChart && trendChart.resize()
  topChart && topChart.resize()
}

async function fetchStats() {
  loading.value = true
  try {
    const res = await getAiStats()
    const data = res?.data
    if (data) {
      if (data.statCards) statCards.value = data.statCards
      if (data.trend) {
        mockTrendData.days = data.trend.days || mockTrendData.days
        mockTrendData.values = data.trend.values || mockTrendData.values
      }
      if (data.topQuestions) {
        mockTopData.length = 0
        mockTopData.push(...data.topQuestions)
      }
      if (data.recentList) recentList.value = data.recentList
      else recentList.value = mockRecentList
    } else {
      recentList.value = mockRecentList
    }
  } catch (e) {
    recentList.value = mockRecentList
  } finally {
    loading.value = false
    nextTick(() => {
      initTrendChart()
      initTopChart()
    })
  }
}

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart && trendChart.dispose()
  topChart && topChart.dispose()
})
</script>

<style scoped>
.ai-stats-page {
  width: 100%;
  background: radial-gradient(ellipse at top, #0a1929 0%, #030a1a 100%);
  border-radius: 12px;
  padding: 20px;
  min-height: calc(100vh - 120px);
  color: #e2e8f0;
}

.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.08) 0%, rgba(3, 10, 26, 0.6) 100%);
  border: 1px solid rgba(14, 165, 233, 0.15);
  border-radius: 12px;
  padding: 18px 20px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(14, 165, 233, 0.5), transparent);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.15);
  border-color: rgba(14, 165, 233, 0.3);
}

.card-primary .stat-icon {
  background: rgba(56, 189, 248, 0.15);
  color: #38bdf8;
}

.card-success .stat-icon {
  background: rgba(74, 222, 128, 0.15);
  color: #4ade80;
}

.card-warning .stat-icon {
  background: rgba(251, 191, 36, 0.15);
  color: #fbbf24;
}

.card-danger .stat-icon {
  background: rgba(248, 113, 113, 0.15);
  color: #f87171;
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
  font-size: 14px;
  font-weight: 600;
  color: #e2e8f0;
}

.stat-sub {
  font-size: 10px;
  color: #475569;
  letter-spacing: 0.5px;
}

.stat-icon {
  width: 40px;
  height: 40px;
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
  font-size: 30px;
  font-weight: 700;
  color: #f1f5f9;
  line-height: 1;
  font-family: 'Courier New', monospace;
}

.card-primary .stat-value {
  color: #38bdf8;
}

.card-success .stat-value {
  color: #4ade80;
}

.card-warning .stat-value {
  color: #fbbf24;
}

.card-danger .stat-value {
  color: #f87171;
}

.stat-unit {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.stat-bottom {
  display: flex;
  align-items: center;
  gap: 8px;
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
  color: #4ade80;
  background: rgba(74, 222, 128, 0.1);
}

.stat-trend.down {
  color: #38bdf8;
  background: rgba(56, 189, 248, 0.1);
}

.stat-compare {
  font-size: 11px;
  color: #475569;
}

.chart-row {
  margin-bottom: 16px;
}

.chart-card {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.06) 0%, rgba(3, 10, 26, 0.5) 100%);
  border: 1px solid rgba(14, 165, 233, 0.15);
  border-radius: 12px;
  padding: 18px 20px;
  position: relative;
  overflow: hidden;
  margin-bottom: 16px;
}

.chart-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(14, 165, 233, 0.4), transparent);
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
  color: #e2e8f0;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title::before {
  content: '';
  width: 4px;
  height: 16px;
  background: linear-gradient(180deg, #0ea5e9, #0369a1);
  border-radius: 2px;
}

.card-subtitle {
  font-size: 11px;
  color: #475569;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.chart-container {
  width: 100%;
  height: 320px;
}

.answer-text {
  color: #94a3b8;
  font-size: 13px;
}

.status-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.resolved {
  color: #4ade80;
  background: rgba(74, 222, 128, 0.12);
  border: 1px solid rgba(74, 222, 128, 0.25);
}

.status-tag.unresolved {
  color: #fbbf24;
  background: rgba(251, 191, 36, 0.12);
  border: 1px solid rgba(251, 191, 36, 0.25);
}

.response-time {
  color: #38bdf8;
  font-family: 'Courier New', monospace;
  font-weight: 600;
}

:deep(.dark-table) {
  background: transparent !important;
  color: #e2e8f0;
}

:deep(.dark-table .el-table__inner-wrapper) {
  background: transparent;
}

:deep(.dark-table tr) {
  background: transparent !important;
}

:deep(.dark-table td) {
  color: #cbd5e1;
  background: transparent !important;
}

:deep(.dark-table td .cell) {
  color: #cbd5e1;
}

:deep(.dark-table--enable-row-hover:hover > td) {
  background: rgba(14, 165, 233, 0.05) !important;
}

:deep(.dark-table::before) {
  display: none;
}

:deep(.dark-table .el-table__body-wrapper) {
  background: transparent;
}

:deep(.dark-table .el-scrollbar) {
  background: transparent;
}
</style>
