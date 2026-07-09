<template>
  <div>
    <div class="page-header">
      <h2>生产进度监控</h2>
    </div>

    <div class="plan-section">
      <div class="section-title">生产计划列表</div>
      <div class="plan-cards">
        <div
          v-for="plan in planList"
          :key="plan.id"
          class="plan-card"
          :class="{ active: selectedPlanId === plan.id }"
          @click="handleSelectPlan(plan)"
        >
          <div class="plan-header">
            <span class="plan-no">{{ plan.planNo }}</span>
            <el-tag :type="planStatusType(plan.status)" size="small">{{ planStatusText(plan.status) }}</el-tag>
          </div>
          <div class="plan-info">
            <div class="plan-item">
              <span class="label">产品：</span>
              <span class="value">{{ productMap[plan.productId] || plan.productName }}</span>
            </div>
            <div class="plan-item">
              <span class="label">计划数量：</span>
              <span class="value">{{ plan.quantity }}</span>
            </div>
          </div>
          <div class="plan-progress">
            <div class="progress-header">
              <span>完成进度</span>
              <span>{{ planProgress(plan) }}%</span>
            </div>
            <el-progress :percentage="planProgress(plan)" :status="plan.status === 2 ? 'success' : ''" />
          </div>
        </div>
      </div>
      <div v-if="planList.length === 0" class="empty-tip">
        <el-empty description="暂无生产计划" />
      </div>
    </div>

    <div v-if="selectedPlanId" class="work-order-section">
      <div class="section-title">工单进度明细</div>
      <div class="process-stats">
        <div v-for="stat in processStats" :key="stat.processId" class="stat-item">
          <div class="stat-name">{{ stat.processName }}</div>
          <div class="stat-bar">
            <div class="stat-fill" :style="{ width: stat.percent + '%' }"></div>
          </div>
          <div class="stat-num">{{ stat.completed }}/{{ stat.total }}</div>
        </div>
      </div>
      <el-table :data="workOrderList" v-loading="workOrderLoading" border>
        <el-table-column prop="workNo" label="工单号" width="170" />
        <el-table-column label="工序" min-width="140">
          <template #default="{ row }">{{ processMap[row.processId] || row.processName }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="工单数量" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="workOrderStatusType(row.status)">{{ workOrderStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="完成进度" min-width="200">
          <template #default="{ row }">
            <el-progress :percentage="workOrderProgress(row)" :status="row.status === 2 ? 'success' : ''" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <template #empty>
          <el-empty description="暂无工单" />
        </template>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getList as getPlanList } from '../../api/productionPlan'
import { getList as getWorkOrderList } from '../../api/workOrder'
import { getList as getProductList } from '../../api/product'
import { getByProductId } from '../../api/process'

const planStatusOptions = [
  { value: 0, label: '待执行' },
  { value: 1, label: '执行中' },
  { value: 2, label: '已完成' }
]

const workOrderStatusOptions = [
  { value: 0, label: '待派发' },
  { value: 1, label: '执行中' },
  { value: 2, label: '已完成' }
]

const planStatusText = (status) => {
  const item = planStatusOptions.find(s => s.value === status)
  return item ? item.label : '未知'
}
const planStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success' }
  return map[status] || 'info'
}

const workOrderStatusText = (status) => {
  const item = workOrderStatusOptions.find(s => s.value === status)
  return item ? item.label : '未知'
}
const workOrderStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success' }
  return map[status] || 'info'
}

const planList = ref([])
const productList = ref([])
const processList = ref([])
const productMap = computed(() => Object.fromEntries(productList.value.map(i => [i.id, i.name])))
const processMap = computed(() => Object.fromEntries(processList.value.map(i => [i.id, i.name])))

const selectedPlanId = ref(null)
const workOrderList = ref([])
const workOrderLoading = ref(false)

const planProgress = (plan) => {
  if (!plan.quantity || plan.quantity === 0) return 0
  const completed = plan.completedQuantity || 0
  return Math.min(100, Math.round((completed / plan.quantity) * 100))
}

const workOrderProgress = (row) => {
  if (!row.quantity || row.quantity === 0) return 0
  const completed = row.completedQuantity || (row.status === 2 ? row.quantity : 0)
  return Math.min(100, Math.round((completed / row.quantity) * 100))
}

const processStats = computed(() => {
  if (!workOrderList.value.length) return []
  const stats = {}
  workOrderList.value.forEach(wo => {
    const pid = wo.processId
    if (!stats[pid]) {
      stats[pid] = {
        processId: pid,
        processName: processMap.value[pid] || wo.processName || '未知工序',
        total: 0,
        completed: 0
      }
    }
    stats[pid].total += 1
    if (wo.status === 2) {
      stats[pid].completed += 1
    }
  })
  return Object.values(stats).map(s => ({
    ...s,
    percent: s.total > 0 ? Math.round((s.completed / s.total) * 100) : 0
  }))
})

const fetchPlans = async () => {
  try {
    const res = await getPlanList({ page: 1, size: 20 })
    planList.value = res.data.records || res.data || []
  } catch (e) {
    console.error(e)
  }
}

const handleSelectPlan = async (plan) => {
  selectedPlanId.value = plan.id
  workOrderLoading.value = true
  try {
    const res = await getWorkOrderList({ page: 1, size: 100, planId: plan.id })
    workOrderList.value = res.data.records || res.data || []
    if (plan.productId) {
      const pres = await getByProductId(plan.productId)
      processList.value = pres.data || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    workOrderLoading.value = false
  }
}

const loadOptions = async () => {
  try {
    const res = await getProductList({})
    productList.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadOptions()
  fetchPlans()
})
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}
.plan-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}
.plan-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.plan-card {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
}
.plan-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
.plan-card.active {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}
.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.plan-no {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}
.plan-info {
  margin-bottom: 12px;
}
.plan-item {
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}
.plan-item .label {
  color: #909399;
}
.plan-item .value {
  color: #303133;
}
.plan-progress .progress-header {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}
.work-order-section {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}
.process-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}
.stat-item {
  flex: 1;
  min-width: 150px;
}
.stat-name {
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}
.stat-bar {
  height: 8px;
  background: #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 4px;
}
.stat-fill {
  height: 100%;
  background: #67c23a;
  border-radius: 4px;
  transition: width 0.3s;
}
.stat-num {
  font-size: 12px;
  color: #909399;
  text-align: right;
}
.empty-tip {
  padding: 40px 0;
}
</style>
