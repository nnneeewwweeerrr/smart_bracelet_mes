<template>
  <div class="page">
    <div class="stats-row">
      <el-card class="stat-card">
        <div class="stat-icon total">
          <el-icon><Monitor /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">设备总数</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon running">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.running }}</div>
          <div class="stat-label">运行中</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon repairing">
          <el-icon><Tools /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.repairing }}</div>
          <div class="stat-label">维修中</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon stopped">
          <el-icon><SwitchButton /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.stopped }}</div>
          <div class="stat-label">已停用</div>
        </div>
      </el-card>
      <el-card class="stat-card warning">
        <div class="stat-icon need-maintain">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.needMaintain }}</div>
          <div class="stat-label">需保养</div>
        </div>
      </el-card>
    </div>

    <div class="chart-row">
      <el-card class="chart-card">
        <template #header>
          <span>设备状态分布</span>
        </template>
        <div class="pie-chart">
          <div class="pie">
            <svg viewBox="0 0 100 100">
              <circle cx="50" cy="50" r="40" :stroke-dasharray="pieData.running" fill="none" stroke="#67C23A" stroke-width="20" />
              <circle cx="50" cy="50" r="40" :stroke-dasharray="pieData.stopped" fill="none" stroke="#909399" stroke-width="20" transform="rotate(180 50 50)" />
              <circle cx="50" cy="50" r="40" :stroke-dasharray="pieData.repairing" fill="none" stroke="#E6A23C" stroke-width="20" transform="rotate(225 50 50)" />
            </svg>
          </div>
          <div class="pie-legend">
            <div><span class="legend-dot running"></span>运行中 {{ stats.running }}</div>
            <div><span class="legend-dot stopped"></span>已停用 {{ stats.stopped }}</div>
            <div><span class="legend-dot repairing"></span>维修中 {{ stats.repairing }}</div>
          </div>
        </div>
      </el-card>

      <el-card class="chart-card">
        <template #header>
          <span>设备状态实时监控</span>
        </template>
        <div class="status-grid">
          <div v-for="item in statusList" :key="item.id" 
               class="status-item" :class="getStatusClass(item.status)">
            <div class="status-indicator"></div>
            <div class="status-info">
              <div class="status-code">{{ item.equipmentCode }}</div>
              <div class="status-name">{{ item.equipmentName }}</div>
              <div class="status-meta">{{ item.location }} | {{ item.runningHours }}h</div>
            </div>
            <div class="status-badge">{{ statusText(item.status) }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <el-card>
      <template #header>
        <span>近期需保养设备</span>
      </template>
      <el-table :data="needMaintainList" v-loading="loading" border stripe>
        <el-table-column prop="equipmentCode" label="设备编码" width="140" />
        <el-table-column prop="equipmentName" label="设备名称" min-width="160" />
        <el-table-column prop="equipmentType" label="设备类型" width="120" />
        <el-table-column prop="location" label="位置" width="160" />
        <el-table-column prop="nextMaintainDate" label="下次保养日期" width="160">
          <template #default="{ row }">
            <span class="text-warning">{{ row.nextMaintainDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="runningHours" label="运行时长" width="100" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="goToEquipment(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="needMaintainList.length === 0" class="empty-tip">
        暂无需要保养的设备
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Monitor, CircleCheck, Tools, SwitchButton, Clock } from '@element-plus/icons-vue'
import { getStatistics, getStatusList } from '../../api/equipment'

const router = useRouter()
const loading = ref(false)
const stats = ref({ total: 0, running: 0, stopped: 0, repairing: 0, needMaintain: 0 })
const statusList = ref([])

let timer = null

const pieData = computed(() => {
  const total = stats.value.running + stats.value.stopped + stats.value.repairing
  if (total === 0) return { running: '0 251.2', stopped: '0 251.2', repairing: '0 251.2' }
  const running = (stats.value.running / total * 251.2) + ' 251.2'
  const stopped = (stats.value.stopped / total * 251.2) + ' 251.2'
  const repairing = (stats.value.repairing / total * 251.2) + ' 251.2'
  return { running, stopped, repairing }
})

const needMaintainList = computed(() => {
  return statusList.value.filter(item => {
    if (!item.nextMaintainDate) return false
    const next = new Date(item.nextMaintainDate)
    const now = new Date()
    return next <= now
  })
})

const statusText = (s) => ({ 0: '停用', 1: '运行', 2: '维修' }[s] || '未知')

const getStatusClass = (s) => ({ 0: 'stopped', 1: 'running', 2: 'repairing' }[s] || 'unknown')

const fetchData = async () => {
  loading.value = true
  try {
    const [statsRes, statusRes] = await Promise.all([getStatistics(), getStatusList()])
    stats.value = statsRes.data || {}
    statusList.value = statusRes.data || []
  } finally {
    loading.value = false
  }
}

const goToEquipment = (row) => {
  router.push('/equipment')
}

onMounted(() => {
  fetchData()
  timer = setInterval(fetchData, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.page { padding: 0; }

.stats-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
  min-height: 100px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
}

.stat-icon.total { background: linear-gradient(135deg, #409EFF, #66B1FF); color: #fff; }
.stat-icon.running { background: linear-gradient(135deg, #67C23A, #85CE61); color: #fff; }
.stat-icon.repairing { background: linear-gradient(135deg, #E6A23C, #F5C870); color: #fff; }
.stat-icon.stopped { background: linear-gradient(135deg, #909399, #B4BCCC); color: #fff; }
.stat-icon.need-maintain { background: linear-gradient(135deg, #F56C6C, #F89898); color: #fff; }

.stat-content { flex: 1; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { font-size: 14px; color: #606266; margin-top: 4px; }

.chart-row {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 16px;
  margin-bottom: 16px;
}

.pie-chart {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
  padding: 20px 0;
}

.pie { width: 120px; height: 120px; }
.pie svg { width: 100%; height: 100%; transform: rotate(-90deg); }

.pie-legend { font-size: 14px; color: #606266; }
.pie-legend div { margin-bottom: 8px; }
.legend-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
}
.legend-dot.running { background: #67C23A; }
.legend-dot.stopped { background: #909399; }
.legend-dot.repairing { background: #E6A23C; }

.status-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.status-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.status-item.running { border-left: 4px solid #67C23A; }
.status-item.stopped { border-left: 4px solid #909399; }
.status-item.repairing { border-left: 4px solid #E6A23C; }

.status-indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 12px;
}

.status-item.running .status-indicator { background: #67C23A; box-shadow: 0 0 8px #67C23A; }
.status-item.stopped .status-indicator { background: #909399; }
.status-item.repairing .status-indicator { background: #E6A23C; box-shadow: 0 0 8px #E6A23C; }

.status-info { flex: 1; overflow: hidden; }
.status-code { font-size: 12px; color: #909399; }
.status-name { font-size: 14px; font-weight: 500; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.status-meta { font-size: 12px; color: #909399; margin-top: 4px; }

.status-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-item.running .status-badge { background: #E8F5E9; color: #67C23A; }
.status-item.stopped .status-badge { background: #F5F5F5; color: #909399; }
.status-item.repairing .status-badge { background: #FFF8E1; color: #E6A23C; }

.empty-tip { text-align: center; padding: 32px; color: #909399; }
.text-warning { color: #E6A23C; }
</style>