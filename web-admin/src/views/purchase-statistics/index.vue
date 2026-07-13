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
        <el-form-item label="供应商">
          <el-select v-model="searchForm.supplierId" placeholder="全部供应商" clearable style="width: 180px">
            <el-option v-for="item in supplierList" :key="item.id" :label="item.supplierName" :value="item.id" />
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
            <span>采购金额趋势</span>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>物料采购占比</span>
          </template>
          <div ref="materialChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <span>供应商采购统计</span>
      </template>
      <el-table :data="supplierStats" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="supplierName" label="供应商名称" min-width="180" />
        <el-table-column prop="orderCount" label="采购单数量" width="120" align="right" />
        <el-table-column prop="amount" label="采购总金额(元)" width="150" align="right">
          <template #default="{ row }">¥{{ row.amount.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="onTimeRate" label="准时交付率" width="120" align="center">
          <template #default="{ row }">
            <el-progress :percentage="row.onTimeRate" :stroke-width="10" />
          </template>
        </el-table-column>
        <el-table-column prop="qualityRate" label="合格率" width="120" align="center">
          <template #default="{ row }">
            <el-progress :percentage="row.qualityRate" :stroke-width="10" :color="row.qualityRate >= 95 ? '#67C23A' : '#E6A23C'" />
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
const materialChartRef = ref(null)
let trendChart = null
let materialChart = null

const searchForm = reactive({
  range: 1,
  supplierId: ''
})

const supplierList = ref([
  { id: 1, supplierName: '深圳电子元件有限公司' },
  { id: 2, supplierName: '东莞塑胶制品厂' },
  { id: 3, supplierName: '广州电池科技' },
  { id: 4, supplierName: '佛山五金配件' },
  { id: 5, supplierName: '中山屏幕模组厂' }
])

const statCards = ref([
  { title: '采购单总数', value: 89, icon: 'ShoppingBag', color: '#409EFF' },
  { title: '采购总金额', value: '¥1,856,200', icon: 'Money', color: '#67C23A' },
  { title: '待到货', value: 12, icon: 'Van', color: '#E6A23C' },
  { title: '本月新增', value: 25, icon: 'Plus', color: '#F56C6C' }
])

const supplierStats = ref([
  { supplierName: '深圳电子元件有限公司', orderCount: 28, amount: 685000, onTimeRate: 96, qualityRate: 98 },
  { supplierName: '东莞塑胶制品厂', orderCount: 22, amount: 356000, onTimeRate: 92, qualityRate: 94 },
  { supplierName: '广州电池科技', orderCount: 18, amount: 420000, onTimeRate: 98, qualityRate: 97 },
  { supplierName: '佛山五金配件', orderCount: 12, amount: 185200, onTimeRate: 88, qualityRate: 92 },
  { supplierName: '中山屏幕模组厂', orderCount: 9, amount: 210000, onTimeRate: 95, qualityRate: 96 }
])

function initTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['采购金额', '采购单数'],
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
    yAxis: [
      {
        type: 'value',
        name: '金额(元)',
        position: 'left'
      },
      {
        type: 'value',
        name: '单数',
        position: 'right'
      }
    ],
    series: [
      {
        name: '采购金额',
        type: 'line',
        smooth: true,
        data: [95000, 108000, 89000, 125000, 110000, 156000, 142000, 168000, 155000, 178000, 195000, 210000],
        itemStyle: { color: '#E6A23C' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(230, 162, 60, 0.3)' },
            { offset: 1, color: 'rgba(230, 162, 60, 0.05)' }
          ])
        }
      },
      {
        name: '采购单数',
        type: 'bar',
        yAxisIndex: 1,
        data: [5, 8, 6, 9, 7, 10, 8, 12, 10, 11, 13, 15],
        itemStyle: { color: '#67C23A' }
      }
    ]
  }
  trendChart.setOption(option)
}

function initMaterialChart() {
  if (!materialChartRef.value) return
  materialChart = echarts.init(materialChartRef.value)
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
        name: '物料采购',
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
          { value: 685, name: '电子元件', itemStyle: { color: '#409EFF' } },
          { value: 356, name: '塑胶件', itemStyle: { color: '#67C23A' } },
          { value: 420, name: '电池', itemStyle: { color: '#E6A23C' } },
          { value: 185, name: '五金件', itemStyle: { color: '#F56C6C' } },
          { value: 210, name: '屏幕模组', itemStyle: { color: '#909399' } }
        ]
      }
    ]
  }
  materialChart.setOption(option)
}

function handleResize() {
  trendChart?.resize()
  materialChart?.resize()
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
    initMaterialChart()
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
