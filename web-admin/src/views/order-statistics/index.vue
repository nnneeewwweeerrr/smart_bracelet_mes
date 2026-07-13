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
  { title: '订单总数', value: 156, icon: 'Document', color: '#409EFF' },
  { title: '总金额(元)', value: '3,256,800', icon: 'Money', color: '#67C23A' },
  { title: '已完成订单', value: 98, icon: 'CircleCheck', color: '#E6A23C' },
  { title: '待处理订单', value: 23, icon: 'Clock', color: '#F56C6C' }
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
      trigger: 'axis'
    },
    legend: {
      data: ['订单金额', '订单数量'],
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
        name: '订单数',
        position: 'right'
      }
    ],
    series: [
      {
        name: '订单金额',
        type: 'line',
        smooth: true,
        data: [120000, 132000, 101000, 134000, 90000, 230000, 210000, 182000, 191000, 234000, 290000, 330000],
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        }
      },
      {
        name: '订单数量',
        type: 'bar',
        yAxisIndex: 1,
        data: [12, 15, 10, 14, 8, 22, 20, 18, 19, 23, 28, 32],
        itemStyle: { color: '#67C23A' }
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
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '订单状态',
        type: 'pie',
        radius: ['40%', '70%'],
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
          { value: 23, name: '待审批', itemStyle: { color: '#F56C6C' } },
          { value: 18, name: '已审批', itemStyle: { color: '#409EFF' } },
          { value: 35, name: '生产中', itemStyle: { color: '#E6A23C' } },
          { value: 98, name: '已完成', itemStyle: { color: '#67C23A' } },
          { value: 8, name: '已取消', itemStyle: { color: '#909399' } }
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
