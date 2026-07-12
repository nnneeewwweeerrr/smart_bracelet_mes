<template>
  <div class="page-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="统计范围">
          <el-radio-group v-model="searchForm.range">
            <el-radio :value="1">本月</el-radio>
            <el-radio :value="2">本季度</el-radio>
            <el-radio :value="3">本年度</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="质检类型">
          <el-select v-model="searchForm.type" placeholder="全部类型" clearable style="width: 150px">
            <el-option label="来料检验" value="1" />
            <el-option label="过程检验" value="2" />
            <el-option label="成品检验" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

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
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>合格率趋势</span>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>不合格类型分布</span>
          </template>
          <div ref="defectChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <span>各工序质量统计</span>
      </template>
      <el-table :data="processQualityStats" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="processName" label="工序名称" min-width="150" />
        <el-table-column prop="inspectQty" label="检验数量" width="120" align="right" />
        <el-table-column prop="passQty" label="合格数量" width="120" align="right" />
        <el-table-column prop="failQty" label="不合格数量" width="130" align="right" />
        <el-table-column prop="passRate" label="合格率" width="150" align="center">
          <template #default="{ row }">
            <el-progress :percentage="row.passRate" :stroke-width="10" :color="row.passRate >= 95 ? '#67C23A' : row.passRate >= 90 ? '#E6A23C' : '#F56C6C'" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

const trendChartRef = ref(null)
const defectChartRef = ref(null)
let trendChart = null
let defectChart = null

const searchForm = reactive({
  range: 1,
  type: ''
})

const statCards = ref([
  { title: '质检批次', value: 325, icon: 'Document', color: '#409EFF' },
  { title: '总检验数', value: '85,600', icon: 'Files', color: '#67C23A' },
  { title: '平均合格率', value: '96.8%', icon: 'CircleCheck', color: '#E6A23C' },
  { title: '不合格数', value: '2,739', icon: 'CircleClose', color: '#F56C6C' }
])

const processQualityStats = ref([
  { processName: '来料检验', inspectQty: 50000, passQty: 48500, failQty: 1500, passRate: 97 },
  { processName: 'SMT贴片', inspectQty: 45000, passQty: 43875, failQty: 1125, passRate: 97.5 },
  { processName: 'PCB测试', inspectQty: 43000, passQty: 41710, failQty: 1290, passRate: 97 },
  { processName: '组装', inspectQty: 40000, passQty: 38400, failQty: 1600, passRate: 96 },
  { processName: '功能测试', inspectQty: 38000, passQty: 37050, failQty: 950, passRate: 97.5 },
  { processName: '老化测试', inspectQty: 36000, passQty: 35280, failQty: 720, passRate: 98 },
  { processName: '成品检验', inspectQty: 35000, passQty: 34300, failQty: 700, passRate: 98 }
])

function initTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['合格率', '目标值'],
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
    },
    yAxis: {
      type: 'value',
      name: '合格率(%)',
      min: 90,
      max: 100
    },
    series: [
      {
        name: '合格率',
        type: 'line',
        smooth: true,
        data: [95.2, 94.8, 95.6, 96.1, 95.9, 96.5, 96.8, 97.0, 96.5, 97.2, 97.5, 97.8],
        itemStyle: { color: '#67C23A' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        },
        markPoint: {
          data: [
            { type: 'max', name: '最高' },
            { type: 'min', name: '最低' }
          ]
        }
      },
      {
        name: '目标值',
        type: 'line',
        lineStyle: { type: 'dashed' },
        data: [95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95, 95],
        itemStyle: { color: '#F56C6C' },
        symbol: 'none'
      }
    ]
  }
  trendChart.setOption(option)
}

function initDefectChart() {
  if (!defectChartRef.value) return
  defectChart = echarts.init(defectChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '不合格类型',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 850, name: '外观不良', itemStyle: { color: '#F56C6C' } },
          { value: 620, name: '功能异常', itemStyle: { color: '#E6A23C' } },
          { value: 480, name: '尺寸偏差', itemStyle: { color: '#409EFF' } },
          { value: 350, name: '焊接不良', itemStyle: { color: '#67C23A' } },
          { value: 280, name: '包装问题', itemStyle: { color: '#909399' } },
          { value: 159, name: '其他', itemStyle: { color: '#9b59b6' } }
        ]
      }
    ]
  }
  defectChart.setOption(option)
}

function handleResize() {
  trendChart?.resize()
  defectChart?.resize()
}

function loadData() {
  ElMessage.success('数据已刷新')
}

function handleExport() {
  ElMessage.success('导出成功')
}

onMounted(() => {
  nextTick(() => {
    initTrendChart()
    initDefectChart()
    window.addEventListener('resize', handleResize)
  })
})
</script>

<style scoped>
.page-container {
  width: 100%;
}

.search-card {
  margin-bottom: 20px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
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
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.chart-container {
  width: 100%;
  height: 350px;
}
</style>
