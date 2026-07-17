<template>
  <div class="big-screen">
    <!-- 顶部标题栏 -->
    <div class="screen-header">
      <div class="header-left">
        <span class="header-date">{{ currentDate }}</span>
      </div>
      <div class="header-center">
        <div class="title-decoration left"></div>
        <div class="screen-title-wrap">
          <h1 class="screen-title">智能手环MES</h1>
          <h2 class="screen-title-sub">数字化车间大屏</h2>
        </div>
        <div class="title-decoration right"></div>
      </div>
      <div class="header-right">
        <span class="header-time">{{ currentTime }}</span>
        <button class="switch-btn" @click="goToDigitalTwin">
          <span class="btn-icon">🎥</span>
          3D产线视角
        </button>
        <button class="back-btn" @click="goBack">返回系统</button>
      </div>
    </div>

    <!-- 主体三栏布局 -->
    <div class="screen-body" v-loading="loading" element-loading-background="rgba(3,10,26,0.8)">
      <!-- 左栏 -->
      <div class="column column-left">
        <!-- 实时产量 -->
        <div class="panel panel-large">
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
        <div class="panel panel-small">
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
        <div class="panel panel-large">
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
        <div class="panel panel-small">
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
        <div class="panel panel-small">
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
        <div class="panel panel-large">
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

function goToDigitalTwin() {
  router.push('/digital-twin')
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

function fetchData() {
  // 大屏使用演示数据，不请求后端接口
  useDefaultData()
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
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(15,23,42,0.9)', borderColor: 'rgba(58,103,171,0.3)', borderWidth: 1, textStyle: { color: '#e2e8f0', fontSize: 13 } },
    legend: { data: ['实际产量', '计划产量'], textStyle: { color: '#94a3b8', fontSize: 12 }, right: '5%', top: 0 },
    grid: { left: '12%', right: '6%', bottom: '14%', top: '24px' },
    xAxis: {
      type: 'category', data: d.dates || [],
      axisLine: { lineStyle: { color: 'rgba(148,163,184,0.2)' } },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 12 },
      splitLine: { lineStyle: { color: 'rgba(148,163,184,0.1)', type: 'dashed' } }
    },
    series: [
      {
        name: '实际产量', type: 'bar', data: d.actualTrend || [],
        barWidth: '40%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#a3e7f2' }, { offset: 1, color: '#3a67ab' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      },
      {
        name: '计划产量', type: 'line', data: d.plannedTrend || [],
        smooth: true,
        lineStyle: { color: '#cafcc3', width: 2, type: 'dashed' },
        itemStyle: { color: '#cafcc3' },
        symbol: 'circle', symbolSize: 7
      }
    ]
  })
}

function updateYieldChart() {
  if (!yieldChart) return
  const d = screenData.value.yield
  const threshold = d.threshold || 95
  yieldChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(15,23,42,0.9)', borderColor: 'rgba(58,103,171,0.3)', borderWidth: 1, textStyle: { color: '#e2e8f0', fontSize: 13 } },
    legend: { data: ['良率', '不良率'], textStyle: { color: '#94a3b8', fontSize: 12 }, right: '5%', top: 0 },
    grid: { left: '14%', right: '6%', bottom: '14%', top: '24px' },
    xAxis: {
      type: 'category', data: d.dates || [],
      axisLine: { lineStyle: { color: 'rgba(148,163,184,0.2)' } },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 12 }
    },
    yAxis: {
      type: 'value', max: 100,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 12, formatter: '{value}%' },
      splitLine: { lineStyle: { color: 'rgba(148,163,184,0.1)', type: 'dashed' } }
    },
    series: [
      {
        name: '良率', type: 'line', data: d.yieldRates || [], smooth: true,
        lineStyle: { color: '#74A8FF', width: 3 },
        itemStyle: { color: '#74A8FF' },
        symbol: 'circle', symbolSize: 7,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(116,168,255,0.2)' }, { offset: 1, color: 'rgba(116,168,255,0.02)' }
          ])
        },
        markLine: {
          data: [{ yAxis: threshold }],
          lineStyle: { color: '#d4a5a5', type: 'dashed', width: 2 },
          label: { formatter: '预警线', color: '#d4a5a5', fontSize: 11, position: 'insideStartTop' }
        }
      },
      {
        name: '不良率', type: 'line', data: d.defectRates || [], smooth: true,
        lineStyle: { color: '#d4a5a5', width: 2 },
        itemStyle: { color: '#d4a5a5' },
        symbol: 'circle', symbolSize: 6
      }
    ]
  })
}

function updateWipChart() {
  if (!wipChart) return
  const d = screenData.value.wip
  const counts = d.counts || []
  wipChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(15,23,42,0.9)', borderColor: 'rgba(58,103,171,0.3)', borderWidth: 1, textStyle: { color: '#e2e8f0', fontSize: 13 } },
    grid: { left: '12%', right: '8%', bottom: '14%', top: '24px' },
    xAxis: {
      type: 'category', data: d.processes || [],
      axisLine: { lineStyle: { color: 'rgba(148,163,184,0.2)' } },
      axisTick: { show: false },
      axisLabel: { color: '#cbd5e1', fontSize: 13 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 12 },
      splitLine: { lineStyle: { color: 'rgba(148,163,184,0.1)', type: 'dashed' } }
    },
    series: [{
      type: 'bar', data: counts,
      barWidth: '45%',
      itemStyle: {
        color: function(params) {
          const colorList = [
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#a3e7f2' }, { offset: 1, color: '#3a67ab' }
            ]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#e8f5e9' }, { offset: 1, color: '#cafcc3' }
            ]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#bacbe3' }, { offset: 1, color: '#74A8FF' }
            ]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#cafcc3' }, { offset: 1, color: '#81c784' }
            ]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#def1f5' }, { offset: 1, color: '#9dc9d4' }
            ])
          ]
          return colorList[params.dataIndex % colorList.length]
        },
        borderRadius: [6, 6, 0, 0]
      },
      label: { show: true, position: 'top', color: '#cbd5e1', fontSize: 14, fontWeight: '600' }
    }]
  })
}

function updateDefectChart() {
  if (!defectChart) return
  const d = screenData.value.defects
  const names = d.names || []
  const counts = d.counts || []
  defectChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(15,23,42,0.9)', borderColor: 'rgba(58,103,171,0.3)', borderWidth: 1, textStyle: { color: '#e2e8f0', fontSize: 13 } },
    grid: { left: '28%', right: '12%', bottom: '12%', top: '18px' },
    xAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 12 },
      splitLine: { lineStyle: { color: 'rgba(148,163,184,0.1)', type: 'dashed' } }
    },
    yAxis: {
      type: 'category', data: names,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#cbd5e1', fontSize: 13 }
    },
    series: [{
      type: 'bar', data: counts,
      barWidth: '50%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#a07a7a' }, { offset: 1, color: '#d4a5a5' }
        ]),
        borderRadius: [0, 6, 6, 0]
      },
      label: { show: true, position: 'right', color: '#d4a5a5', fontSize: 13, fontWeight: '600' }
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
  background: radial-gradient(ellipse at top, #1e3a5f 0%, #0f172a 60%, #020617 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: 'Microsoft YaHei', sans-serif;
  color: #e2e8f0;
}

/* 顶部标题栏 */
.screen-header {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: linear-gradient(180deg, rgba(58,103,171,0.15) 0%, transparent 100%);
  border-bottom: 1px solid rgba(58,103,171,0.2);
  flex-shrink: 0;
}

.header-left, .header-right {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-right {
  justify-content: flex-end;
}

.header-date {
  font-size: 13px;
  color: #94a3b8;
}

.header-time {
  font-size: 18px;
  font-weight: 600;
  color: #a3e7f2;
  font-family: 'Courier New', monospace;
  letter-spacing: 2px;
}

.header-center {
  flex: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.screen-title-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  line-height: 1.2;
}

.screen-title {
  font-size: 24px;
  font-weight: 700;
  color: #f1f5f9;
  margin: 0;
  letter-spacing: 6px;
  background: linear-gradient(90deg, #bacbe3, #3a67ab, #bacbe3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.screen-title-sub {
  font-size: 14px;
  font-weight: 500;
  color: #cbd5e1;
  margin: 0;
  letter-spacing: 4px;
}

.title-decoration {
  width: 100px;
  height: 1px;
  background: linear-gradient(90deg, transparent, #3a67ab);
}

.title-decoration.right {
  background: linear-gradient(90deg, #3a67ab, transparent);
}

.back-btn {
  background: rgba(58,103,171,0.2);
  border: 1px solid rgba(58,103,171,0.4);
  color: #bacbe3;
  padding: 6px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.back-btn:hover {
  background: rgba(58,103,171,0.35);
  color: #fff;
}

.switch-btn {
  background: rgba(59,130,246,0.25);
  border: 1px solid rgba(96,165,250,0.5);
  color: #93c5fd;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
  margin-right: 10px;
}

.switch-btn:hover {
  background: rgba(59,130,246,0.4);
  color: #fff;
  box-shadow: 0 0 15px rgba(96,165,250,0.3);
}

.btn-icon {
  margin-right: 4px;
}

/* 主体 */
.screen-body {
  flex: 1;
  display: flex;
  gap: 10px;
  padding: 10px;
  overflow: hidden;
}

.column {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.column-left, .column-right {
  flex: 1;
}

.column-center {
  flex: 1.2;
}

/* 面板 */
.panel {
  flex: 1;
  background: linear-gradient(135deg, rgba(58,103,171,0.06) 0%, rgba(15,23,42,0.6) 100%);
  border: 1px solid rgba(58,103,171,0.15);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  min-height: 0;
}

.panel-small {
  flex: 0.75;
}

.panel-large {
  flex: 1.25;
}

.panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(186,203,227,0.3), transparent);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  font-size: 14px;
  font-weight: 600;
  color: #e2e8f0;
  border-bottom: 1px solid rgba(58,103,171,0.1);
  flex-shrink: 0;
}

.title-icon {
  width: 3px;
  height: 12px;
  background: linear-gradient(180deg, #a3e7f2, #3a67ab);
  border-radius: 2px;
}

.title-en {
  font-size: 9px;
  color: #64748b;
  margin-left: auto;
  letter-spacing: 1px;
}

.panel-content {
  flex: 1;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
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
  gap: 20px;
  margin-bottom: 4px;
}

.prod-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
}

.prod-label {
  font-size: 12px;
  color: #94a3b8;
}

.prod-value {
  font-size: 26px;
  font-weight: 700;
  font-family: 'Courier New', monospace;
}

.prod-value.planned {
  color: #cafcc3;
}

.prod-value.actual {
  color: #a3e7f2;
}

.prod-unit {
  font-size: 11px;
  color: #94a3b8;
}

.prod-divider {
  width: 1px;
  height: 30px;
  background: rgba(148,163,184,0.15);
}

.progress-bar-wrapper {
  margin-bottom: 4px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 4px;
}

.progress-percent {
  color: #a3e7f2;
  font-weight: 600;
  font-size: 13px;
}

.progress-bar-bg {
  height: 6px;
  background: rgba(148,163,184,0.1);
  border-radius: 3px;
  overflow: hidden;
  position: relative;
}

.progress-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #3a67ab, #74A8FF, #a3e7f2);
  border-radius: 3px;
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
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.25), transparent);
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
  gap: 24px;
  margin-bottom: 4px;
}

.yield-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
}

.yield-label {
  font-size: 12px;
  color: #94a3b8;
}

.yield-value {
  font-size: 26px;
  font-weight: 700;
  font-family: 'Courier New', monospace;
}

.yield-value.good {
  color: #74A8FF;
}

.yield-value.good.warning {
  color: #d4a5a5;
  animation: pulse 1s infinite;
}

.yield-value.bad {
  color: #d4a5a5;
}

.yield-warning {
  font-size: 12px;
  color: #d4a5a5;
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
  gap: 28px;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(58,103,171,0.12);
}

.eq-stat {
  display: flex;
  align-items: center;
  gap: 6px;
}

.eq-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.eq-dot.green { background: #a3e7f2; box-shadow: 0 0 10px rgba(163,231,242,0.6); }
.eq-dot.yellow { background: #cafcc3; box-shadow: 0 0 10px rgba(202,252,195,0.6); }
.eq-dot.red { background: #ff6b6b; box-shadow: 0 0 10px rgba(255,107,107,0.6); }
.eq-dot.blue { background: #74A8FF; box-shadow: 0 0 10px rgba(116,168,255,0.6); }

.eq-num {
  font-size: 24px;
  font-weight: 700;
  font-family: 'Courier New', monospace;
}

.eq-num.green { color: #a3e7f2; }
.eq-num.yellow { color: #cafcc3; }
.eq-num.red { color: #ff6b6b; }
.eq-num.blue { color: #74A8FF; }

.eq-text {
  font-size: 13px;
  color: #94a3b8;
}

.equipment-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  overflow-y: auto;
  align-content: start;
}

.eq-card {
  background: rgba(148,163,184,0.05);
  border: 1px solid rgba(148,163,184,0.12);
  border-radius: 8px;
  padding: 6px 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
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
  background: #a3e7f2;
  box-shadow: 0 0 10px rgba(163,231,242,0.7);
  animation: blink-green 2s infinite;
}

.eq-card.status-standby .eq-card-light {
  background: #cafcc3;
  box-shadow: 0 0 8px rgba(202,252,195,0.6);
}

.eq-card.status-fault .eq-card-light {
  background: #ff6b6b;
  box-shadow: 0 0 10px rgba(255,107,107,0.7);
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
  font-size: 13px;
  color: #e2e8f0;
  font-weight: 500;
  text-align: center;
}

.eq-card-code {
  font-size: 11px;
  color: #64748b;
}

.eq-card-status {
  font-size: 12px;
  font-weight: 600;
}

.status-running .eq-card-status { color: #a3e7f2; }
.status-standby .eq-card-status { color: #cafcc3; }
.status-fault .eq-card-status { color: #ff6b6b; }

/* 生产汇总 */
.summary-grid {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: repeat(4, 1fr);
  gap: 8px;
  min-height: 0;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 14px;
  background: rgba(148,163,184,0.05);
  border: 1px solid rgba(148,163,184,0.1);
  border-radius: 10px;
  transition: all 0.3s;
  min-height: 0;
}

.summary-card:hover {
  background: rgba(58,103,171,0.1);
  border-color: rgba(58,103,171,0.25);
}

.summary-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  flex-shrink: 0;
}

.icon-prod {
  background: linear-gradient(135deg, #a3e7f2, #3a67ab);
}

.icon-yield {
  background: linear-gradient(135deg, #a3d5ff, #74A8FF);
}

.icon-device {
  background: linear-gradient(135deg, #bacbe3, #74A8FF);
}

.icon-wip {
  background: linear-gradient(135deg, #3a67ab, #74A8FF);
}

.summary-info {
  flex: 1;
  min-width: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-label {
  font-size: 14px;
  color: #94a3b8;
}

.summary-value {
  font-size: 28px;
  font-weight: 700;
  color: #f1f5f9;
  font-family: 'Courier New', monospace;
  line-height: 1.2;
}

.text-warning {
  color: #cafcc3 !important;
}
</style>
