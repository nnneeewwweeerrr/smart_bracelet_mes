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
        <el-form-item label="产品名称">
          <el-select v-model="searchForm.productId" placeholder="全部产品" clearable style="width: 160px">
            <el-option v-for="item in productList" :key="item.id" :label="item.productName" :value="item.id" />
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
            <span>订单金额趋势</span>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>订单状态分布</span>
          </template>
          <div ref="statusChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <span>产品订单统计</span>
      </template>
      <el-table :data="productStats" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="productName" label="产品名称" min-width="150" />
        <el-table-column prop="orderCount" label="订单数" width="100" align="right" />
        <el-table-column prop="quantity" label="总数量" width="100" align="right" />
        <el-table-column prop="amount" label="总金额(元)" width="140" align="right">
          <template #default="{ row }">¥{{ row.amount.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="avgPrice" label="平均单价(元)" width="130" align="right">
          <template #default="{ row }">¥{{ row.avgPrice.toFixed(2) }}</template>
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
const statusChartRef = ref(null)
let trendChart = null
let statusChart = null

const searchForm = reactive({
  range: 1,
  productId: ''
})

const productList = ref([
  { id: 1, productName: '智能手环Pro' },
  { id: 2, productName: '智能手环Lite' },
  { id: 3, productName: '智能手环Ultra' }
])

const statCards = ref([
  { title: '订单总数', value: 156, icon: 'Document', color: '#A7CBFF' },
  { title: '总金额(元)', value: '3,256,800', icon: 'Money', color: '#95D5B2' },
  { title: '已完成订单', value: 98, icon: 'CircleCheck', color: '#FFD6A5' },
  { title: '待处理订单', value: 23, icon: 'Clock', color: '#FFADAD' }
])

const productStats = ref([
  { productName: '智能手环Pro', orderCount: 68, quantity: 45600, amount: 8664000, avgPrice: 190 },
  { productName: '智能手环Lite', orderCount: 52, quantity: 38400, amount: 3648000, avgPrice: 95 },
  { productName: '智能手环Ultra', orderCount: 36, quantity: 12000, amount: 3480000, avgPrice: 290 }
])

function initTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e8e8e8',
      borderWidth: 1,
      textStyle: { color: '#606266' }
    },
    legend: {
      data: ['订单金额', '订单数量'],
      top: 0,
      textStyle: { color: '#909399' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      axisLine: { lineStyle: { color: '#f0f2f5' } },
      axisLabel: { color: '#a8abb2' }
    },
    yAxis: [
      {
        type: 'value',
        name: '金额(元)',
        position: 'left',
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { lineStyle: { color: '#f5f7fa', type: 'dashed' } },
        axisLabel: { color: '#a8abb2' },
        nameTextStyle: { color: '#a8abb2' }
      },
      {
        type: 'value',
        name: '订单数',
        position: 'right',
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { color: '#a8abb2' },
        nameTextStyle: { color: '#a8abb2' }
      }
    ],
    series: [
      {
        name: '订单金额',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: [120000, 132000, 101000, 134000, 90000, 230000, 210000, 182000, 191000, 234000, 290000, 330000],
        itemStyle: { color: '#A7CBFF' },
        lineStyle: { width: 3, color: '#A7CBFF' },
        areaStyle: {
          color: 'rgba(167, 203, 255, 0.15)'
        }
      },
      {
        name: '订单数量',
        type: 'bar',
        yAxisIndex: 1,
        barWidth: '40%',
        data: [12, 15, 10, 14, 8, 22, 20, 18, 19, 23, 28, 32],
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: '#95D5B2'
        }
      }
    ]
  }
  trendChart.setOption(option)
}

function initStatusChart() {
  if (!statusChartRef.value) return
  statusChart = echarts.init(statusChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e8e8e8',
      borderWidth: 1,
      textStyle: { color: '#606266' }
    },
    legend: {
      bottom: '5%',
      left: 'center',
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { color: '#909399', fontSize: 12 },
      padding: [10, 0, 0, 0]
    },
    series: [
      {
        name: '订单状态',
        type: 'pie',
        radius: ['45%', '68%'],
        center: ['50%', '42%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 3
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 500
          },
          itemStyle: {
            shadowBlur: 0,
            shadowOffsetX: 0,
            shadowColor: 'transparent'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 23, name: '待审批', itemStyle: { color: '#FFADAD' } },
          { value: 18, name: '已审批', itemStyle: { color: '#A7CBFF' } },
          { value: 35, name: '生产中', itemStyle: { color: '#95D5B2' } },
          { value: 98, name: '已完成', itemStyle: { color: '#C8B6FF' } },
          { value: 8, name: '已取消', itemStyle: { color: '#FFD6A5' } }
        ]
      }
    ]
  }
  statusChart.setOption(option)
}

function handleResize() {
  trendChart?.resize()
  statusChart?.resize()
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
    initStatusChart()
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
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-info {
  width: 100%;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
  white-space: nowrap;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.chart-container {
  width: 100%;
  height: 320px;
}
</style>
