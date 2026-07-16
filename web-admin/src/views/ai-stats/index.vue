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
              <h3 class="card-title">提问用户分布</h3>
              <span class="card-subtitle">User Distribution</span>
            </div>
          </div>
          <div ref="userChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 对话记录表格 -->
    <div class="chart-card">
      <div class="card-header">
        <div class="card-title-group">
          <h3 class="card-title">对话记录</h3>
          <span class="card-subtitle">Chat History</span>
        </div>
        <div class="header-actions">
          <el-input v-model="searchUserName" placeholder="搜索用户名" clearable style="width: 160px" @keyup.enter="handleSearch" />
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>&nbsp;搜索
          </el-button>
        </div>
      </div>
      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        class="dark-table"
        :header-cell-style="{ background: 'rgba(14,165,233,0.08)', color: '#94a3b8', borderBottom: '1px solid rgba(14,165,233,0.15)' }"
        :row-style="{ background: 'transparent' }"
        :cell-style="{ borderBottom: '1px solid rgba(14,165,233,0.08)' }"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="userName" label="提问人" width="110" />
        <el-table-column prop="question" label="问题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="answer" label="回答摘要" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="answer-text">{{ row.answer }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="model" label="模型" width="110" />
        <el-table-column prop="responseTime" label="响应耗时" width="100" align="center">
          <template #default="{ row }">
            <span class="response-time">{{ row.responseTime || 0 }}ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="feedback" label="反馈" width="100" align="center">
          <template #default="{ row }">
            <span class="status-tag" :class="getFeedbackClass(row.feedback)">
              {{ getFeedbackLabel(row.feedback) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="chatTime" label="提问时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="success" link @click="handleFeedback(row, 1)">好评</el-button>
            <el-button size="small" type="warning" link @click="handleFeedback(row, -1)">差评</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="对话详情" width="680px" class="dark-dialog">
      <el-descriptions :column="2" border class="dark-desc">
        <el-descriptions-item label="提问人">{{ currentRow.userName }}</el-descriptions-item>
        <el-descriptions-item label="会话ID">{{ currentRow.sessionId }}</el-descriptions-item>
        <el-descriptions-item label="模型">{{ currentRow.model }}</el-descriptions-item>
        <el-descriptions-item label="场景">{{ currentRow.scene }}</el-descriptions-item>
        <el-descriptions-item label="响应耗时">{{ currentRow.responseTime }}ms</el-descriptions-item>
        <el-descriptions-item label="反馈">{{ getFeedbackLabel(currentRow.feedback) }}</el-descriptions-item>
        <el-descriptions-item label="问题" :span="2">
          <pre class="content-pre">{{ currentRow.question }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="回答" :span="2">
          <pre class="content-pre">{{ currentRow.answer }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="提问时间">{{ currentRow.chatTime }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, Document, CircleCheck, Timer, Search } from '@element-plus/icons-vue'
import { getAiChatStats, getAiChatHistoryList, feedbackAiChat, deleteAiChatHistory } from '@/api/aiChatHistory'

const loading = ref(false)
const detailVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const searchUserName = ref('')
const currentRow = reactive({})
const trendChartRef = ref(null)
const userChartRef = ref(null)
let trendChart = null
let userChart = null

const statCards = ref([
  { title: '总提问数', subtitle: 'Total Questions', value: '0', unit: '次', icon: 'ChatDotRound', type: 'primary' },
  { title: '今日提问', subtitle: 'Today Questions', value: '0', unit: '次', icon: 'Document', type: 'success' },
  { title: '好评率', subtitle: 'Positive Rate', value: '0', unit: '%', icon: 'CircleCheck', type: 'warning' },
  { title: '会话数', subtitle: 'Sessions', value: '0', unit: '个', icon: 'Timer', type: 'danger' }
])

const trendData = ref({ days: [], values: [] })
const userData = ref([])

function getFeedbackLabel(feedback) {
  if (feedback === 1) return '好评'
  if (feedback === -1) return '差评'
  return '未评价'
}

function getFeedbackClass(feedback) {
  if (feedback === 1) return 'enabled'
  if (feedback === -1) return 'disabled'
  return 'pending'
}

function fetchStats() {
  getAiChatStats().then(res => {
    const d = res.data || {}
    statCards.value[0].value = String(d.total || 0)
    statCards.value[1].value = String(d.today || 0)
    statCards.value[2].value = String(d.positiveRate || '0')
  }).catch(() => {})
}

function fetchData() {
  loading.value = true
  getAiChatHistoryList({
    pageNum: currentPage.value,
    pageSize: pageSize.value,
    userName: searchUserName.value
  }).then(res => {
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
    buildChartData(res.data.records)
  }).catch(() => {
    ElMessage.error('加载失败')
  }).finally(() => {
    loading.value = false
    nextTick(() => {
      initTrendChart()
      initUserChart()
    })
  })
}

function buildChartData(records) {
  // 近7天趋势
  const dayMap = {}
  const now = new Date()
  for (let i = 6; i >= 0; i--) {
    const d = new Date(now)
    d.setDate(d.getDate() - i)
    const key = `${d.getMonth() + 1}/${d.getDate()}`
    dayMap[key] = 0
  }
  records.forEach(r => {
    if (r.chatTime) {
      const d = new Date(r.chatTime)
      const key = `${d.getMonth() + 1}/${d.getDate()}`
      if (key in dayMap) dayMap[key]++
    }
  })
  trendData.value.days = Object.keys(dayMap)
  trendData.value.values = Object.values(dayMap)

  // 用户分布
  const userMap = {}
  records.forEach(r => {
    const name = r.userName || '未知'
    userMap[name] = (userMap[name] || 0) + 1
  })
  userData.value = Object.entries(userMap).map(([name, count]) => ({ name, count })).sort((a, b) => b.count - a.count).slice(0, 8)
}

function initTrendChart() {
  if (!trendChartRef.value) return
  if (trendChart) trendChart.dispose()
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(3, 10, 26, 0.9)',
      borderColor: 'rgba(14, 165, 233, 0.3)',
      borderWidth: 1,
      textStyle: { color: '#e2e8f0', fontSize: 12 },
      borderRadius: 8
    },
    grid: { left: '50px', right: '20px', bottom: '3%', top: '30px', containLabel: false },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendData.value.days,
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
    series: [{
      name: '提问数',
      type: 'line',
      data: trendData.value.values,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      itemStyle: { color: '#38bdf8', borderColor: '#fff', borderWidth: 2, shadowColor: 'rgba(56, 189, 248, 0.5)', shadowBlur: 10 },
      lineStyle: { width: 3, color: '#38bdf8' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(56, 189, 248, 0.35)' },
          { offset: 1, color: 'rgba(56, 189, 248, 0.02)' }
        ])
      }
    }]
  })
}

function initUserChart() {
  if (!userChartRef.value) return
  if (userChart) userChart.dispose()
  userChart = echarts.init(userChartRef.value)
  const data = userData.value.length > 0 ? [...userData.value].reverse() : [{ name: '无数据', count: 0 }]
  userChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(3, 10, 26, 0.9)',
      borderColor: 'rgba(14, 165, 233, 0.3)',
      borderWidth: 1,
      textStyle: { color: '#e2e8f0', fontSize: 12 },
      borderRadius: 8
    },
    grid: { left: '3%', right: '12%', bottom: '3%', top: '10px', containLabel: true },
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
    series: [{
      name: '提问次数',
      type: 'bar',
      data: data.map((d, i) => ({
        value: d.count,
        itemStyle: {
          borderRadius: [0, 6, 6, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: i >= 5 ? 'rgba(56, 189, 248, 0.5)' : 'rgba(56, 189, 248, 0.8)' },
            { offset: 1, color: i >= 5 ? '#0369a1' : '#0ea5e9' }
          ])
        }
      })),
      barWidth: '55%',
      label: { show: true, position: 'right', color: '#38bdf8', fontSize: 11, fontWeight: 600 }
    }]
  })
}

function handleResize() {
  trendChart && trendChart.resize()
  userChart && userChart.resize()
}

function handleSearch() {
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

function handleFeedback(row, feedback) {
  feedbackAiChat(row.id, feedback).then(() => {
    ElMessage.success(feedback === 1 ? '已标记好评' : '已标记差评')
    fetchData()
  }).catch(() => {
    ElMessage.error('操作失败')
  })
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除该对话记录吗？', '提示', { type: 'warning' }).then(() => {
    deleteAiChatHistory(row.id).then(() => {
      ElMessage.success('删除成功')
      fetchData()
      fetchStats()
    }).catch(() => {
      ElMessage.error('删除失败')
    })
  }).catch(() => {})
}

onMounted(() => {
  fetchStats()
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart && trendChart.dispose()
  userChart && userChart.dispose()
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

.stat-row { margin-bottom: 16px; }

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
  top: 0; left: 0;
  width: 100%; height: 1px;
  background: linear-gradient(90deg, transparent, rgba(14, 165, 233, 0.5), transparent);
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.15);
  border-color: rgba(14, 165, 233, 0.3);
}
.card-primary .stat-icon { background: rgba(56, 189, 248, 0.15); color: #38bdf8; }
.card-success .stat-icon { background: rgba(74, 222, 128, 0.15); color: #4ade80; }
.card-warning .stat-icon { background: rgba(251, 191, 36, 0.15); color: #fbbf24; }
.card-danger .stat-icon { background: rgba(248, 113, 113, 0.15); color: #f87171; }

.stat-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 14px; }
.stat-label-group { display: flex; flex-direction: column; gap: 4px; }
.stat-label { font-size: 14px; font-weight: 600; color: #e2e8f0; }
.stat-sub { font-size: 10px; color: #475569; letter-spacing: 0.5px; }
.stat-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.stat-main { display: flex; align-items: baseline; gap: 4px; }
.stat-value { font-size: 30px; font-weight: 700; line-height: 1; font-family: 'Courier New', monospace; }
.card-primary .stat-value { color: #38bdf8; }
.card-success .stat-value { color: #4ade80; }
.card-warning .stat-value { color: #fbbf24; }
.card-danger .stat-value { color: #f87171; }
.stat-unit { font-size: 13px; color: #64748b; font-weight: 500; }

.chart-row { margin-bottom: 16px; }

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
  top: 0; left: 0;
  width: 100%; height: 1px;
  background: linear-gradient(90deg, transparent, rgba(14, 165, 233, 0.4), transparent);
}

.card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.card-title-group { display: flex; flex-direction: column; gap: 4px; }
.card-title { font-size: 15px; font-weight: 600; color: #e2e8f0; margin: 0; display: flex; align-items: center; gap: 8px; }
.card-title::before { content: ''; width: 4px; height: 16px; background: linear-gradient(180deg, #0ea5e9, #0369a1); border-radius: 2px; }
.card-subtitle { font-size: 11px; color: #475569; font-weight: 500; letter-spacing: 0.5px; }
.header-actions { display: flex; gap: 10px; align-items: center; }

.chart-container { width: 100%; height: 300px; }

.answer-text { color: #94a3b8; font-size: 13px; }
.response-time { color: #38bdf8; font-family: 'Courier New', monospace; font-weight: 600; }

.status-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}
.status-tag.enabled { color: #4ade80; background: rgba(74, 222, 128, 0.12); border: 1px solid rgba(74, 222, 128, 0.25); }
.status-tag.disabled { color: #f87171; background: rgba(248, 113, 113, 0.12); border: 1px solid rgba(248, 113, 113, 0.25); }
.status-tag.pending { color: #94a3b8; background: rgba(148, 163, 184, 0.12); border: 1px solid rgba(148, 163, 184, 0.25); }

.pagination-wrapper { margin-top: 18px; display: flex; justify-content: flex-end; }

.content-pre {
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
  line-height: 1.6;
  color: #cbd5e1;
  margin: 0;
  max-height: 200px;
  overflow-y: auto;
}

:deep(.dark-table) { background: transparent !important; color: #e2e8f0; }
:deep(.dark-table .el-table__inner-wrapper) { background: transparent; }
:deep(.dark-table tr) { background: transparent !important; }
:deep(.dark-table td) { color: #cbd5e1; background: transparent !important; }
:deep(.dark-table td .cell) { color: #cbd5e1; }
:deep(.dark-table--enable-row-hover:hover > td) { background: rgba(14, 165, 233, 0.05) !important; }
:deep(.dark-table::before) { display: none; }
:deep(.dark-table .el-table__body-wrapper) { background: transparent; }
:deep(.dark-table .el-scrollbar) { background: transparent; }
</style>
