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
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouse" placeholder="全部仓库" clearable style="width: 150px">
            <el-option label="原料仓" value="1" />
            <el-option label="成品仓" value="2" />
            <el-option label="半成品仓" value="3" />
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
            <span>库存出入库趋势</span>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>库存分类占比</span>
          </template>
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <span>库存周转统计</span>
      </template>
      <el-table :data="turnoverStats" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="category" label="物料分类" min-width="150" />
        <el-table-column prop="beginStock" label="期初库存" width="120" align="right" />
        <el-table-column prop="inStock" label="本期入库" width="120" align="right" />
        <el-table-column prop="outStock" label="本期出库" width="120" align="right" />
        <el-table-column prop="endStock" label="期末库存" width="120" align="right" />
        <el-table-column prop="turnoverDays" label="周转天数" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.turnoverDays > 60 ? 'danger' : row.turnoverDays > 30 ? 'warning' : 'success'" size="small">
              {{ row.turnoverDays }}天
            </el-tag>
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
const categoryChartRef = ref(null)
let trendChart = null
let categoryChart = null

const searchForm = reactive({
  range: 1,
  warehouse: ''
})

const statCards = ref([
  { title: '物料种类数', value: 256, icon: 'Box', color: '#409EFF' },
  { title: '库存总数量', value: '156,800', icon: 'Files', color: '#67C23A' },
  { title: '库存总金额', value: '¥5,680,000', icon: 'Money', color: '#E6A23C' },
  { title: '预警物料数', value: 18, icon: 'Warning', color: '#F56C6C' }
])

const turnoverStats = ref([
  { category: '电子元件', beginStock: 50000, inStock: 32000, outStock: 28000, endStock: 54000, turnoverDays: 25 },
  { category: '塑胶件', beginStock: 30000, inStock: 15000, outStock: 12000, endStock: 33000, turnoverDays: 42 },
  { category: '电池', beginStock: 8000, inStock: 5000, outStock: 4500, endStock: 8500, turnoverDays: 18 },
  { category: '五金件', beginStock: 12000, inStock: 6000, outStock: 4800, endStock: 13200, turnoverDays: 55 },
  { category: '屏幕模组', beginStock: 3000, inStock: 2000, outStock: 1800, endStock: 3200, turnoverDays: 30 },
  { category: '成品手环', beginStock: 5000, inStock: 8000, outStock: 7500, endStock: 5500, turnoverDays: 12 }
])

function initTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['入库数量', '出库数量'],
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
      name: '数量'
    },
    series: [
      {
        name: '入库数量',
        type: 'line',
        smooth: true,
        data: [12000, 15000, 10000, 14000, 18000, 22000, 20000, 16000, 19000, 23000, 25000, 28000],
        itemStyle: { color: '#67C23A' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        }
      },
      {
        name: '出库数量',
        type: 'line',
        smooth: true,
        data: [10000, 12000, 9000, 13000, 15000, 20000, 18000, 14000, 17000, 21000, 23000, 26000],
        itemStyle: { color: '#F56C6C' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 108, 108, 0.3)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0.05)' }
          ])
        }
      }
    ]
  }
  trendChart.setOption(option)
}

function initCategoryChart() {
  if (!categoryChartRef.value) return
  categoryChart = echarts.init(categoryChartRef.value)
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
        name: '库存分类',
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
          { value: 54000, name: '电子元件', itemStyle: { color: '#409EFF' } },
          { value: 33000, name: '塑胶件', itemStyle: { color: '#67C23A' } },
          { value: 8500, name: '电池', itemStyle: { color: '#E6A23C' } },
          { value: 13200, name: '五金件', itemStyle: { color: '#F56C6C' } },
          { value: 3200, name: '屏幕模组', itemStyle: { color: '#909399' } },
          { value: 5500, name: '成品', itemStyle: { color: '#9b59b6' } }
        ]
      }
    ]
  }
  categoryChart.setOption(option)
}

function handleResize() {
  trendChart?.resize()
  categoryChart?.resize()
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
    initCategoryChart()
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
