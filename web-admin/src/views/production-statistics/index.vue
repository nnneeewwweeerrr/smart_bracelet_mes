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
        <el-form-item label="产品">
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
            <span>产量趋势</span>
          </template>
          <div ref="outputChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>产品产量占比</span>
          </template>
          <div ref="productChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <span>工序生产统计</span>
      </template>
      <el-table :data="processStats" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="processName" label="工序名称" min-width="150" />
        <el-table-column prop="planQty" label="计划产量" width="120" align="right" />
        <el-table-column prop="actualQty" label="实际产量" width="120" align="right" />
        <el-table-column prop="completionRate" label="完成率" width="150" align="center">
          <template #default="{ row }">
            <el-progress :percentage="row.completionRate" :stroke-width="10" />
          </template>
        </el-table-column>
        <el-table-column prop="passRate" label="合格率" width="150" align="center">
          <template #default="{ row }">
            <el-progress :percentage="row.passRate" :stroke-width="10" :color="row.passRate >= 95 ? '#67C23A' : '#E6A23C'" />
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

const outputChartRef = ref(null)
const productChartRef = ref(null)
let outputChart = null
let productChart = null

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
  { title: '生产工单', value: 45, icon: 'SetUp', color: '#A7CBFF' },
  { title: '总产量', value: '68,500', icon: 'Box', color: '#95D5B2' },
  { title: '平均合格率', value: '96.8%', icon: 'CircleCheck', color: '#FFD6A5' },
  { title: '设备稼动率', value: '89.2%', icon: 'Cpu', color: '#C8B6FF' }
])

const processStats = ref([
  { processName: 'SMT贴片', planQty: 80000, actualQty: 78500, completionRate: 98, passRate: 98.5 },
  { processName: 'PCB测试', planQty: 78000, actualQty: 76800, completionRate: 98, passRate: 97.8 },
  { processName: '组装', planQty: 76000, actualQty: 74200, completionRate: 97, passRate: 96.2 },
  { processName: '功能测试', planQty: 74000, actualQty: 72500, completionRate: 98, passRate: 97.5 },
  { processName: '老化测试', planQty: 72000, actualQty: 70800, completionRate: 98, passRate: 98.0 },
  { processName: '包装', planQty: 70000, actualQty: 69500, completionRate: 99, passRate: 99.8 }
])

function initOutputChart() {
  if (!outputChartRef.value) return
  outputChart = echarts.init(outputChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#e8e8e8',
      borderWidth: 1
    },
    legend: {
      data: ['计划产量', '实际产量'],
      top: 0,
      textStyle: {
        color: '#606266'
      }
    },
    grid: {
      top: '15%',
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      axisLine: {
        lineStyle: {
          color: '#e8e8e8'
        }
      },
      axisLabel: {
        color: '#909399'
      }
    },
    yAxis: {
      type: 'value',
      name: '产量',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      splitLine: {
        lineStyle: {
          type: 'dashed',
          color: '#f0f2f5'
        }
      }
    },
    series: [
      {
        name: '计划产量',
        type: 'line',
        smooth: true,
        data: [5000, 5500, 6000, 5800, 6500, 7000, 6800, 7200, 7500, 7800, 8000, 8500],
        itemStyle: { color: '#B5C7D9' },
        lineStyle: { type: 'dashed', color: '#B5C7D9' }
      },
      {
        name: '实际产量',
        type: 'line',
        smooth: true,
        data: [4800, 5300, 5900, 5600, 6300, 6800, 6600, 7000, 7300, 7600, 7800, 8200],
        itemStyle: { color: '#95D5B2' },
        lineStyle: { color: '#95D5B2', width: 3 },
        symbolSize: 6
      }
    ]
  }
  outputChart.setOption(option)
}

function initProductChart() {
  if (!productChartRef.value) return
  productChart = echarts.init(productChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '5%',
      left: 'center',
      itemWidth: 12,
      itemHeight: 12,
      textStyle: {
        color: '#606266'
      }
    },
    series: [
      {
        name: '产品产量',
        type: 'pie',
        radius: ['45%', '68%'],
        center: ['50%', '42%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 3
        },
        label: {
          show: false
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 32000, name: '智能手环Pro', itemStyle: { color: '#A7CBFF' } },
          { value: 25000, name: '智能手环Lite', itemStyle: { color: '#95D5B2' } },
          { value: 11500, name: '智能手环Ultra', itemStyle: { color: '#FFD6A5' } }
        ]
      }
    ]
  }
  productChart.setOption(option)
}

function handleResize() {
  outputChart?.resize()
  productChart?.resize()
}

function loadData() {
  ElMessage.success('数据已刷新')
}

function handleExport() {
  ElMessage.success('导出成功')
}

onMounted(() => {
  nextTick(() => {
    initOutputChart()
    initProductChart()
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
  border-radius: 16px;
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
