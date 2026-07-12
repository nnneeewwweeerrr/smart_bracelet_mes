<template>
  <div class="big-screen">
    <!-- 顶部标题栏 -->
    <div class="screen-header">
      <div class="header-left">
        <span class="header-date">{{ currentDate }}</span>
      </div>
      <div class="header-center">
        <div class="title-decoration left"></div>
        <h1 class="screen-title">智能手环MES · 数字化车间大屏</h1>
        <div class="title-decoration right"></div>
      </div>
      <div class="header-right">
        <span class="header-time">{{ currentTime }}</span>
        <button class="back-btn" @click="goBack">返回系统</button>
      </div>
    </div>

    <!-- 主体三栏布局 -->
    <div class="screen-body" v-loading="loading" element-loading-background="rgba(3,10,26,0.8)">
      <!-- 左栏 -->
      <div class="column column-left">
        <!-- 实时产量 -->
        <div class="panel">
          <div class="panel-title">
            <span class="title-icon"></span>
            实时产量
            <span class="title-en">REAL-TIME OUTPUT</span>
          </div>
          <div class="panel-content">
            <div class="production-overview">
              <div class="prod-item">
                <div class="prod-label">今日计划</div>
                <div class="prod-value planned">{{ screenData.production.planned || 0 }}</div>
                <div class="prod-unit">件</div>
              </div>
              <div class="prod-divider"></div>
              <div class="prod-item">
                <div class="prod-label">实际完成</div>
                <div class="prod-value actual">{{ screenData.production.actual || 0 }}</div>
                <div class="prod-unit">件</div>
              </div>
            </div>
            <div class="progress-bar-wrapper">
              <div class="progress-info">
                <span>完成进度</span>
                <span class="progress-percent">{{ screenData.production.progress || 0 }}%</span>
              </div>
              <div class="progress-bar-bg">
                <div class="progress-bar-fill" :style="{ width: (screenData.production.progress || 0) + '%' }"></div>
              </div>
            </div>
            <div ref="productionChartRef" class="chart-box"></div>
          </div>
        </div>

        <!-- 产线良率 -->
        <div class="panel">
          <div class="panel-title">
            <span class="title-icon"></span>
            产线良率
            <span class="title-en">YIELD RATE</span>
          </div>
          <div class="panel-content">
            <div class="yield-overview">
              <div class="yield-item">
                <span class="yield-label">当前良率</span>
                <span class="yield-value good" :class="{ 'warning': screenData.yield.currentYield < (screenData.yield.threshold || 95) }">
                  {{ screenData.yield.currentYield || 100 }}%
                </span>
              </div>
              <div class="yield-item">
                <span class="yield-label">不良率</span>
                <span class="yield-value bad">{{ screenData.yield.currentDefect || 0 }}%</span>
              </div>
              <div class="yield-item" v-if="screenData.yield.currentYield < (screenData.yield.threshold || 95)">
                <span class="yield-warning">⚠ 不良率超标预警</span>
              </div>
            </div>
            <div ref="yieldChartRef" class="chart-box"></div>
          </div>
        </div>
      </div>

      <!-- 中栏 -->
      <div class="column column-center">
        <!-- 设备状态 -->
        <div class="panel">
          <div class="panel-title">
            <span class="title-icon"></span>
            设备状态监控
            <span class="title-en">EQUIPMENT STATUS</span>
          </div>
          <div class="panel-content">
            <div class="equipment-summary">
              <div class="eq-stat">
                <span class="eq-dot green"></span>
                <span class="eq-num green">{{ screenData.equipment.running || 0 }}</span>
                <span class="eq-text">运行</span>
              </div>
              <div class="eq-stat">
                <span class="eq-dot yellow"></span>
                <span class="eq-num yellow">{{ screenData.equipment.standby || 0 }}</span>
                <span class="eq-text">待机</span>
              </div>
              <div class="eq-stat">
                <span class="eq-dot red"></span>
                <span class="eq-num red">{{ screenData.equipment.fault || 0 }}</span>
                <span class="eq-text">故障</span>
              </div>
              <div class="eq-stat">
                <span class="eq-dot blue"></span>
                <span class="eq-num blue">{{ screenData.equipment.total || 0 }}</span>
                <span class="eq-text">总数</span>
              </div>
            </div>
            <div class="equipment-grid">
              <div v-for="(device, index) in (screenData.equipment.devices || [])" :key="index" class="eq-card" :class="getEqStatusClass(device.status)">
                <div class="eq-card-light"></div>
                <div class="eq-card-name">{{ device.name }}</div>
                <div class="eq-card-code">{{ device.code }}</div>
                <div class="eq-card-status">{{ getEqStatusText(device.status) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 在制分布 -->
        <div class="panel">
          <div class="panel-title">
            <span class="title-icon"></span>
            在制WIP分布
            <span class="title-en">WIP DISTRIBUTION</span>
          </div>
          <div class="panel-content">
            <div ref="wipChartRef" class="chart-box"></div>
          </div>
        </div>
      </div>

      <!-- 右栏 -->
      <div class="column column-right">
        <!-- TOP5不良 -->
        <div class="panel">
          <div class="panel-title">
            <span class="title-icon"></span>
            TOP5不良类型
            <span class="title-en">TOP DEFECTS</span>
          </div>
          <div class="panel-content">
            <div ref="defectChartRef" class="chart-box"></div>
          </div>
        </div>

        <!-- 生产汇总 -->
        <div class="panel">
          <div class="panel-title">
            <span class="title-icon"></span>
            生产实时看板
            <span class="title-en">PRODUCTION DASHBOARD</span>
          </div>
          <div class="panel-content">
            <div class="summary-grid">
              <div class="summary-card">
                <div class="summary-icon icon-prod"></div>
                <div class="summary-info">
                  <div class="summary-label">今日产量</div>
                  <div class="summary-value">{{ screenData.production.actual || 0 }}</div>
                </div>
              </div>
              <div class="summary-card">
                <div class="summary-icon icon-yield"></div>
                <div class="summary-info">
                  <div class="summary-label">当前良率</div>
                  <div class="summary-value" :class="{ 'text-warning': screenData.yield.currentYield < (screenData.yield.threshold || 95) }">
                    {{ screenData.yield.currentYield || 100 }}%
                  </div>
                </div>
              </div>
              <div class="summary-card">
                <div class="summary-icon icon-device"></div>
                <div class="summary-info">
                  <div class="summary-label">设备运行率</div>
                  <div class="summary-value">{{ deviceRunRate }}%</div>
                </div>
              </div>
              <div class="summary-card">
                <div class="summary-icon icon-wip"></div>
                <div class="summary-info">
                  <div class="summary-label">在制总量</div>
                  <div class="summary-value">{{ totalWip }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getBigScreenData } from '../../api/bigScreen'

const router = useRouter()
const loading = ref(false)

const productionChartRef = ref(null)
const yieldChartRef = ref(null)
const wipChartRef = ref(null)
const defectChartRef = ref(null)

let productionChart = null
let yieldChart = null
let wipChart = null
let defectChart = null
let refreshTimer = null
let timeTimer = null

const currentDate = ref('')
const currentTime = ref('')

const screenData = ref({
  production: { planned: 0, actual: 0, progress: 0, dates: [], actualTrend: [], plannedTrend: [] },
  yield: { dates: [], yieldRates: [], defectRates: [], currentYield: 100, currentDefect: 0, threshold: 95 },
  equipment: { devices: [], total: 0, running: 0, standby: 0, fault: 0 },
  wip: { processes: [], counts: [] },
  defects: { names: [], counts: [], items: [] }
})

const deviceRunRate = computed(() => {
  const total = screenData.value.equipment.total || 1
  const running = screenData.value.equipment.running || 0
  return Math.round(running * 100 / total)
})

const totalWip = computed(() => {
  return (screenData.value.wip.counts || []).reduce((sum, c) => sum + c, 0)
})

function updateTime() {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const week = ['日', '一', '二', '三', '四', '五', '六'][now.getDay()]
  currentDate.value = `${y}年${m}月${d}日 星期${week}`
  const h = String(now.getHours()).padStart(2, '0')
  const min = String(now.getMinutes()).padStart(2, '0')
  const s = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${h}:${min}:${s}`
}

function goBack() {
  router.push('/dashboard')
}

function getEqStatusClass(status) {
  if (status === 1) return 'status-running'
  if (status === 2) return 'status-fault'
  return 'status-standby'
}

function getEqStatusText(status) {
  if (status === 1) return '运行中'
  if (status === 2) return '故障'
  return '待机'
}

async function fetchData() {
  try {
    const res = await getBigScreenData()
    if (res && res.data) {
      screenData.value = res.data
      nextTick(() => {
        updateAllCharts()
      })
    }
  } catch (e) {
    console.log('大屏接口异常，使用默认演示数据')
    useDefaultData()
  }
}

function useDefaultData() {
  screenData.value = {
    production: {
      planned: 5000, actual: 3850, progress: 77.0,
      dates: ['7/5', '7/6', '7/7', '7/8', '7/9', '7/10', '7/11'],
      actualTrend: [3200, 3500, 3100, 3800, 4200, 3900, 3850],
      plannedTrend: [5000, 5000, 5000, 5000, 5000, 5000, 5000]
    },
    yield: {
      dates: ['7/5', '7/6', '7/7', '7/8', '7/9', '7/10', '7/11'],
      yieldRates: [98.2, 97.5, 96.8, 97.9, 95.3, 94.1, 96.5],
      defectRates: [1.8, 2.5, 3.2, 2.1, 4.7, 5.9, 3.5],
      currentYield: 96.5, currentDefect: 3.5, threshold: 95.0
    },
    equipment: {
      total: 12, running: 9, standby: 2, fault: 1,
      devices: [
        { name: '贴片机A', code: 'SMT-001', status: 1 },
        { name: '贴片机B', code: 'SMT-002', status: 1 },
        { name: '回流焊', code: 'REF-001', status: 1 },
        { name: '焊接机A', code: 'WLD-001', status: 1 },
        { name: '焊接机B', code: 'WLD-002', status: 2 },
        { name: '组装台A', code: 'ASM-001', status: 1 },
        { name: '组装台B', code: 'ASM-002', status: 1 },
        { name: '测试仪A', code: 'TST-001', status: 1 },
        { name: '测试仪B', code: 'TST-002', status: 0 },
        { name: '老化柜', code: 'AGE-001', status: 1 },
        { name: '包装机', code: 'PKG-001', status: 1 },
        { name: '打码机', code: 'COD-001', status: 0 }
      ]
    },
    wip: {
      processes: ['贴片', '焊接', '组装', '测试', '包装'],
      counts: [120, 85, 200, 65, 30]
    },
    defects: {
      names: ['屏幕划痕', '心率模块焊接不良', '蓝牙连接异常', '外壳缝隙过大', '电池续航不足'],
      counts: [28, 19, 15, 12, 8],
      items: [
        { name: '屏幕划痕', count: 28 },
        { name: '心率模块焊接不良', count: 19 },
        { name: '蓝牙连接异常', count: 15 },
        { name: '外壳缝隙过大', count: 12 },
        { name: '电池续航不足', count: 8 }
      ]
    }
  }
  nextTick(() => updateAllCharts())
}

function initCharts() {
  if (productionChartRef.value) productionChart = echarts.init(productionChartRef.value)
  if (yieldChartRef.value) yieldChart = echarts.init(yieldChartRef.value)
  if (wipChartRef.value) wipChart = echarts.init(wipChartRef.value)
  if (defectChartRef.value) defectChart = echarts.init(defectChartRef.value)
}

function updateAllCharts() {
  updateProductionChart()
  updateYieldChart()
  updateWipChart()
  updateDefectChart()
}

function updateProductionChart() {
  if (!productionChart) return
  const d = screenData.value.production
  productionChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(0,21,41,0.8)', borderColor: '#0ea5e9', textStyle: { color: '#e2e8f0' } },
    legend: { data: ['实际产量', '计划产量'], textStyle: { color: '#64748b', fontSize: 11 }, right: 10, top: 0 },
    grid: { left: '8%', right: '5%', bottom: '8%', top: '25px' },
    xAxis: {
      type: 'category', data: d.dates || [],
      axisLine: { lineStyle: { color: '#1e3a5f' } },
      axisLabel: { color: '#64748b', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#64748b', fontSize: 11 },
      splitLine: { lineStyle: { color: 'rgba(30,58,95,0.4)' } }
    },
    series: [
      {
        name: '实际产量', type: 'bar', data: d.actualTrend || [],
        barWidth: '35%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#0ea5e9' }, { offset: 1, color: '#0369a1' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      },
      {
        name: '计划产量', type: 'line', data: d.plannedTrend || [],
        smooth: true,
        lineStyle: { color: '#f59e0b', width: 2, type: 'dashed' },
        itemStyle: { color: '#f59e0b' },
        symbol: 'circle', symbolSize: 6
      }
    ]
  })
}

function updateYieldChart() {
  if (!yieldChart) return
  const d = screenData.value.yield
  const threshold = d.threshold || 95
  yieldChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(0,21,41,0.8)', borderColor: '#0ea5e9', textStyle: { color: '#e2e8f0' } },
    legend: { data: ['良率', '不良率'], textStyle: { color: '#64748b', fontSize: 11 }, right: 10, top: 0 },
    grid: { left: '8%', right: '5%', bottom: '8%', top: '25px' },
    xAxis: {
      type: 'category', data: d.dates || [],
      axisLine: { lineStyle: { color: '#1e3a5f' } },
      axisLabel: { color: '#64748b', fontSize: 11 }
    },
    yAxis: {
      type: 'value', max: 100,
      axisLine: { show: false },
      axisLabel: { color: '#64748b', fontSize: 11, formatter: '{value}%' },
      splitLine: { lineStyle: { color: 'rgba(30,58,95,0.4)' } }
    },
    series: [
      {
        name: '良率', type: 'line', data: d.yieldRates || [], smooth: true,
        lineStyle: { color: '#22d3ee', width: 3 },
        itemStyle: { color: '#22d3ee' },
        symbol: 'circle', symbolSize: 6,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(34,211,238,0.25)' }, { offset: 1, color: 'rgba(34,211,238,0.02)' }
          ])
        },
        markLine: {
          data: [{ yAxis: threshold }],
          lineStyle: { color: '#ef4444', type: 'dashed', width: 2 },
          label: { formatter: '预警线', color: '#ef4444', fontSize: 10 }
        }
      },
      {
        name: '不良率', type: 'line', data: d.defectRates || [], smooth: true,
        lineStyle: { color: '#f87171', width: 2 },
        itemStyle: { color: '#f87171' },
        symbol: 'circle', symbolSize: 5
      }
    ]
  })
}

function updateWipChart() {
  if (!wipChart) return
  const d = screenData.value.wip
  const counts = d.counts || []
  const maxVal = Math.max(...counts, 1)
  wipChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(0,21,41,0.8)', borderColor: '#0ea5e9', textStyle: { color: '#e2e8f0' } },
    grid: { left: '10%', right: '8%', bottom: '8%', top: '15px' },
    xAxis: {
      type: 'category', data: d.processes || [],
      axisLine: { lineStyle: { color: '#1e3a5f' } },
      axisLabel: { color: '#94a3b8', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#64748b', fontSize: 11 },
      splitLine: { lineStyle: { color: 'rgba(30,58,95,0.4)' } }
    },
    series: [{
      type: 'bar', data: counts,
      barWidth: '40%',
      itemStyle: {
        color: function(params) {
          const val = params.value
          const ratio = val / maxVal
          if (ratio > 0.7) {
            return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#ef4444' }, { offset: 1, color: '#991b1b' }
            ])
          } else if (ratio > 0.4) {
            return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#f59e0b' }, { offset: 1, color: '#92400e' }
            ])
          }
          return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#22d3ee' }, { offset: 1, color: '#0e7490' }
          ])
        },
        borderRadius: [6, 6, 0, 0]
      },
      label: { show: true, position: 'top', color: '#94a3b8', fontSize: 13, fontWeight: 'bold' }
    }]
  })
}

function updateDefectChart() {
  if (!defectChart) return
  const d = screenData.value.defects
  const names = d.names || []
  const counts = d.counts || []
  defectChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(0,21,41,0.8)', borderColor: '#0ea5e9', textStyle: { color: '#e2e8f0' } },
    grid: { left: '22%', right: '8%', bottom: '5%', top: '10px' },
    xAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#64748b', fontSize: 11 },
      splitLine: { lineStyle: { color: 'rgba(30,58,95,0.4)' } }
    },
    yAxis: {
      type: 'category', data: names,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#cbd5e1', fontSize: 12 }
    },
    series: [{
      type: 'bar', data: counts,
      barWidth: '50%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#f97316' }, { offset: 1, color: '#ef4444' }
        ]),
        borderRadius: [0, 6, 6, 0]
      },
      label: { show: true, position: 'right', color: '#f87171', fontSize: 13, fontWeight: 'bold' }
    }]
  })
}

function handleResize() {
  productionChart?.resize()
  yieldChart?.resize()
  wipChart?.resize()
  defectChart?.resize()
}

onMounted(() => {
  updateTime()
  timeTimer = setInterval(updateTime, 1000)

  nextTick(() => {
    initCharts()
    fetchData()
  })

  refreshTimer = setInterval(fetchData, 5000)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  if (timeTimer) clearInterval(timeTimer)
  productionChart?.dispose()
  yieldChart?.dispose()
  wipChart?.dispose()
  defectChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.big-screen {
  width: 100vw;
  height: 100vh;
  background: radial-gradient(ellipse at center, #061a36 0%, #030a1a 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: 'Microsoft YaHei', sans-serif;
  color: #e2e8f0;
}

/* 顶部标题栏 */
.screen-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  background: linear-gradient(180deg, rgba(14,165,233,0.15) 0%, transparent 100%);
  border-bottom: 1px solid rgba(14,165,233,0.2);
  flex-shrink: 0;
}

.header-left, .header-right {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-right {
  justify-content: flex-end;
}

.header-date {
  font-size: 14px;
  color: #64748b;
}

.header-time {
  font-size: 22px;
  font-weight: 700;
  color: #0ea5e9;
  font-family: 'Courier New', monospace;
  letter-spacing: 2px;
}

.header-center {
  flex: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.screen-title {
  font-size: 26px;
  font-weight: 700;
  color: #f1f5f9;
  margin: 0;
  letter-spacing: 4px;
  text-shadow: 0 0 20px rgba(14,165,233,0.5);
  background: linear-gradient(90deg, #38bdf8, #818cf8, #38bdf8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-decoration {
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #0ea5e9);
}

.title-decoration.right {
  background: linear-gradient(90deg, #0ea5e9, transparent);
}

.back-btn {
  background: rgba(14,165,233,0.15);
  border: 1px solid rgba(14,165,233,0.3);
  color: #38bdf8;
  padding: 6px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.back-btn:hover {
  background: rgba(14,165,233,0.3);
  color: #fff;
}

/* 主体 */
.screen-body {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 16px;
  overflow: hidden;
}

.column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.column-left, .column-right {
  flex: 1;
}

.column-center {
  flex: 1.3;
}

/* 面板 */
.panel {
  flex: 1;
  background: linear-gradient(135deg, rgba(14,165,233,0.06) 0%, rgba(3,10,26,0.5) 100%);
  border: 1px solid rgba(14,165,233,0.15);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(14,165,233,0.5), transparent);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  font-size: 15px;
  font-weight: 600;
  color: #e2e8f0;
  border-bottom: 1px solid rgba(14,165,233,0.1);
  flex-shrink: 0;
}

.title-icon {
  width: 4px;
  height: 16px;
  background: linear-gradient(180deg, #0ea5e9, #0369a1);
  border-radius: 2px;
}

.title-en {
  font-size: 10px;
  color: #475569;
  margin-left: auto;
  letter-spacing: 1px;
}

.panel-content {
  flex: 1;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chart-box {
  flex: 1;
  width: 100%;
  min-height: 0;
}

/* 实时产量 */
.production-overview {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  margin-bottom: 12px;
}

.prod-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.prod-label {
  font-size: 12px;
  color: #64748b;
}

.prod-value {
  font-size: 32px;
  font-weight: 700;
  font-family: 'Courier New', monospace;
}

.prod-value.planned {
  color: #f59e0b;
}

.prod-value.actual {
  color: #0ea5e9;
}

.prod-unit {
  font-size: 12px;
  color: #64748b;
}

.prod-divider {
  width: 1px;
  height: 40px;
  background: rgba(14,165,233,0.2);
}

.progress-bar-wrapper {
  margin-bottom: 8px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
}

.progress-percent {
  color: #0ea5e9;
  font-weight: 700;
  font-size: 14px;
}

.progress-bar-bg {
  height: 10px;
  background: rgba(14,165,233,0.1);
  border-radius: 5px;
  overflow: hidden;
  position: relative;
}

.progress-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #0369a1, #0ea5e9, #38bdf8);
  border-radius: 5px;
  transition: width 1s ease;
  position: relative;
  overflow: hidden;
}

.progress-bar-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* 良率 */
.yield-overview {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 8px;
}

.yield-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.yield-label {
  font-size: 12px;
  color: #64748b;
}

.yield-value {
  font-size: 24px;
  font-weight: 700;
  font-family: 'Courier New', monospace;
}

.yield-value.good {
  color: #22d3ee;
}

.yield-value.good.warning {
  color: #ef4444;
  animation: pulse 1s infinite;
}

.yield-value.bad {
  color: #f87171;
}

.yield-warning {
  font-size: 12px;
  color: #ef4444;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 设备状态 */
.equipment-summary {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(14,165,233,0.1);
}

.eq-stat {
  display: flex;
  align-items: center;
  gap: 6px;
}

.eq-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.eq-dot.green { background: #22c55e; box-shadow: 0 0 8px #22c55e; }
.eq-dot.yellow { background: #eab308; box-shadow: 0 0 8px #eab308; }
.eq-dot.red { background: #ef4444; box-shadow: 0 0 8px #ef4444; }
.eq-dot.blue { background: #3b82f6; box-shadow: 0 0 8px #3b82f6; }

.eq-num {
  font-size: 20px;
  font-weight: 700;
  font-family: 'Courier New', monospace;
}

.eq-num.green { color: #22c55e; }
.eq-num.yellow { color: #eab308; }
.eq-num.red { color: #ef4444; }
.eq-num.blue { color: #3b82f6; }

.eq-text {
  font-size: 12px;
  color: #64748b;
}

.equipment-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  overflow-y: auto;
}

.eq-card {
  background: rgba(14,165,233,0.05);
  border: 1px solid rgba(14,165,233,0.15);
  border-radius: 6px;
  padding: 10px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  position: relative;
  transition: all 0.3s;
}

.eq-card-light {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-bottom: 2px;
}

.eq-card.status-running .eq-card-light {
  background: #22c55e;
  box-shadow: 0 0 12px #22c55e;
  animation: blink-green 2s infinite;
}

.eq-card.status-standby .eq-card-light {
  background: #eab308;
  box-shadow: 0 0 8px #eab308;
}

.eq-card.status-fault .eq-card-light {
  background: #ef4444;
  box-shadow: 0 0 12px #ef4444;
  animation: blink-red 1s infinite;
}

@keyframes blink-green {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

@keyframes blink-red {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.eq-card-name {
  font-size: 12px;
  color: #e2e8f0;
  font-weight: 500;
  text-align: center;
}

.eq-card-code {
  font-size: 10px;
  color: #475569;
}

.eq-card-status {
  font-size: 11px;
  font-weight: 600;
}

.status-running .eq-card-status { color: #22c55e; }
.status-standby .eq-card-status { color: #eab308; }
.status-fault .eq-card-status { color: #ef4444; }

/* 生产汇总 */
.summary-grid {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: repeat(4, 1fr);
  gap: 10px;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: rgba(14,165,233,0.05);
  border: 1px solid rgba(14,165,233,0.1);
  border-radius: 8px;
  transition: all 0.3s;
}

.summary-card:hover {
  background: rgba(14,165,233,0.1);
  border-color: rgba(14,165,233,0.3);
}

.summary-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  flex-shrink: 0;
}

.icon-prod {
  background: linear-gradient(135deg, #0ea5e9, #0369a1);
  box-shadow: 0 0 12px rgba(14,165,233,0.3);
}

.icon-yield {
  background: linear-gradient(135deg, #22d3ee, #0891b2);
  box-shadow: 0 0 12px rgba(34,211,238,0.3);
}

.icon-device {
  background: linear-gradient(135deg, #22c55e, #15803d);
  box-shadow: 0 0 12px rgba(34,197,94,0.3);
}

.icon-wip {
  background: linear-gradient(135deg, #f59e0b, #92400e);
  box-shadow: 0 0 12px rgba(245,158,11,0.3);
}

.summary-info {
  flex: 1;
}

.summary-label {
  font-size: 12px;
  color: #64748b;
}

.summary-value {
  font-size: 24px;
  font-weight: 700;
  color: #f1f5f9;
  font-family: 'Courier New', monospace;
}

.text-warning {
  color: #ef4444 !important;
}
</style>
