<template>
  <div class="flow-screen">
    <!-- 顶部标题 -->
    <div class="screen-header">
      <div class="header-left">
        <span class="title-main">生产全流程联动</span>
        <span class="title-sub">Work Order Full Process Linkage</span>
      </div>
      <div class="header-right">
        <span class="update-time">
          <el-icon><Clock /></el-icon>
          数据更新：{{ lastUpdateTime }}
        </span>
        <el-button class="refresh-btn" @click="loadOverview" :icon="Refresh" circle plain />
      </div>
    </div>

    <div class="flow-body">
      <!-- 左侧面板 -->
      <div class="left-panel">
        <!-- 状态概览卡片 -->
        <div class="stat-grid">
          <div
            v-for="item in statCards"
            :key="item.key"
            class="stat-card"
            :class="item.cls"
          >
            <div class="stat-num">{{ overview[item.key] || 0 }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>

        <!-- 工单列表 -->
        <div class="list-card">
          <div class="list-header">
            <span class="list-title">
              <el-icon><Tickets /></el-icon>
              工单列表
            </span>
            <span class="list-count">共 {{ workOrders.length }} 条</span>
          </div>
          <div class="list-body">
            <div
              v-for="wo in workOrders"
              :key="wo.id"
              class="wo-item"
              :class="{ active: selectedId === wo.id }"
              @click="selectWorkOrder(wo)"
            >
              <div class="wo-top">
                <span class="wo-no">{{ wo.workNo }}</span>
                <el-tag :type="getStatusType(wo.status)" effect="dark" size="small">
                  {{ getStatusText(wo.status) }}
                </el-tag>
              </div>
              <div class="wo-bottom">
                <span>计划：{{ wo.quantity }}</span>
                <span>{{ formatDateTime(wo.createTime) }}</span>
              </div>
            </div>
            <div v-if="workOrders.length === 0" class="empty-list">暂无工单数据</div>
          </div>
        </div>
      </div>

      <!-- 右侧面板 -->
      <div class="right-panel" v-loading="detailLoading">
        <div v-if="!selectedWorkOrder" class="empty-detail">
          <div class="empty-icon">📋</div>
          <div class="empty-text">请从左侧选择工单查看全流程详情</div>
        </div>

        <template v-else>
          <!-- 工单基本信息 -->
          <div class="info-card">
            <div class="card-header">
              <span class="card-icon">📦</span>
              <span class="card-title">工单基本信息</span>
              <el-tag :type="getStatusType(selectedWorkOrder.status)" effect="dark" class="header-tag">
                {{ getStatusText(selectedWorkOrder.status) }}
              </el-tag>
            </div>
            <div class="card-body">
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">工单号</span>
                  <span class="info-value wo-code">{{ selectedWorkOrder.workNo }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">计划数量</span>
                  <span class="info-value">{{ selectedWorkOrder.quantity }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">产品ID</span>
                  <span class="info-value">{{ selectedWorkOrder.productId || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">工序ID</span>
                  <span class="info-value">{{ selectedWorkOrder.processId || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">创建时间</span>
                  <span class="info-value">{{ formatDateTime(selectedWorkOrder.createTime) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">SN数量</span>
                  <span class="info-value">{{ detailData.productSns?.length || 0 }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 流程进度条 -->
          <div class="info-card">
            <div class="card-header">
              <span class="card-icon">🔗</span>
              <span class="card-title">流程进度</span>
            </div>
            <div class="card-body">
              <div class="progress-steps">
                <div
                  v-for="(step, idx) in progressSteps"
                  :key="idx"
                  class="step-item"
                  :class="step.state"
                >
                  <div class="step-circle">
                    <el-icon v-if="step.state === 'done'"><Check /></el-icon>
                    <span v-else>{{ idx + 1 }}</span>
                  </div>
                  <div class="step-label">{{ step.label }}</div>
                  <div v-if="idx < progressSteps.length - 1" class="step-line"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- 时间线 -->
          <div class="info-card">
            <div class="card-header">
              <span class="card-icon">🕐</span>
              <span class="card-title">关键事件时间线</span>
              <span class="card-count">共 {{ timeline.length }} 条</span>
            </div>
            <div class="card-body timeline-body">
              <el-timeline v-if="timeline.length > 0">
                <el-timeline-item
                  v-for="(evt, i) in timeline"
                  :key="i"
                  :type="getTimelineType(evt.type)"
                  :timestamp="formatDateTime(evt.time)"
                  placement="top"
                  :hollow="evt.type === 'create'"
                >
                  <div class="timeline-event">
                    <div class="event-title">
                      <el-icon class="event-icon"><component :is="evt.icon" /></el-icon>
                      <span>{{ evt.title }}</span>
                    </div>
                    <div class="event-desc">{{ evt.description }}</div>
                  </div>
                </el-timeline-item>
              </el-timeline>
              <div v-else class="empty-timeline">暂无时间线数据</div>
            </div>
          </div>

          <!-- 关联数据Tab -->
          <div class="info-card">
            <div class="card-header">
              <span class="card-icon">📊</span>
              <span class="card-title">关联数据</span>
            </div>
            <div class="card-body">
              <el-tabs v-model="activeTab" class="flow-tabs">
                <!-- 生产报工 -->
                <el-tab-pane label="生产报工" name="report">
                  <el-table
                    :data="detailData.productionReports || []"
                    border
                    stripe
                    size="small"
                    class="dark-table"
                    empty-text="暂无报工记录"
                  >
                    <el-table-column type="index" label="序号" width="60" align="center" />
                    <el-table-column prop="reportDate" label="报工日期" width="170">
                      <template #default="{ row }">{{ formatDateTime(row.reportDate) }}</template>
                    </el-table-column>
                    <el-table-column prop="goodQuantity" label="合格数量" width="100" align="center" />
                    <el-table-column prop="badQuantity" label="不合格数量" width="110" align="center" />
                    <el-table-column prop="workerId" label="工人ID" width="100" align="center" />
                    <el-table-column prop="processId" label="工序ID" align="center" />
                  </el-table>
                </el-tab-pane>

                <!-- 质检记录 -->
                <el-tab-pane :label="`质检记录`" name="quality">
                  <div v-if="qualityFlatList.length === 0" class="empty-tab">暂无质检记录</div>
                  <el-table
                    v-else
                    :data="qualityFlatList"
                    border
                    stripe
                    size="small"
                    class="dark-table"
                  >
                    <el-table-column type="index" label="序号" width="60" align="center" />
                    <el-table-column prop="taskNo" label="任务编号" width="150" />
                    <el-table-column prop="taskType" label="任务类型" width="110">
                      <template #default="{ row }">{{ getTaskTypeText(row.taskType) }}</template>
                    </el-table-column>
                    <el-table-column prop="checkItem" label="检验项目" min-width="140" />
                    <el-table-column prop="standardValue" label="标准值" width="120" />
                    <el-table-column prop="actualValue" label="实际值" width="120" />
                    <el-table-column prop="result" label="结果" width="90" align="center">
                      <template #default="{ row }">
                        <el-tag :type="row.result === 1 ? 'success' : 'danger'" effect="dark" size="small">
                          {{ row.result === 1 ? '合格' : '不合格' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>

                <!-- 入库记录 -->
                <el-tab-pane label="入库记录" name="stockIn">
                  <el-table
                    :data="detailData.stockIns || []"
                    border
                    stripe
                    size="small"
                    class="dark-table"
                    empty-text="暂无入库记录"
                  >
                    <el-table-column type="index" label="序号" width="60" align="center" />
                    <el-table-column prop="inNo" label="入库单号" width="160" />
                    <el-table-column prop="quantity" label="入库数量" width="110" align="center" />
                    <el-table-column prop="sourceType" label="来源类型" width="110" align="center" />
                    <el-table-column prop="sourceNo" label="来源单号" width="150" />
                    <el-table-column prop="warehouseId" label="仓库ID" width="100" align="center" />
                    <el-table-column prop="createTime" label="入库时间">
                      <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
              </el-tabs>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Refresh, Tickets, Check } from '@element-plus/icons-vue'
import { getFlowOverview, getFlowDetail, getFlowTimeline } from '../../api/processFlow'

const overview = reactive({})
const workOrders = ref([])
const selectedId = ref(null)
const selectedWorkOrder = ref(null)
const detailData = reactive({})
const timeline = ref([])
const detailLoading = ref(false)
const activeTab = ref('report')
const lastUpdateTime = ref('')

const statCards = [
  { key: 'pending', label: '待生产', cls: 'stat-pending' },
  { key: 'producing', label: '生产中', cls: 'stat-producing' },
  { key: 'inspecting', label: '质检中', cls: 'stat-inspecting' },
  { key: 'completed', label: '已完成', cls: 'stat-completed' }
]

// 质检记录扁平化（task + results 展开为行）
const qualityFlatList = computed(() => {
  const records = detailData.qualityRecords || []
  const list = []
  records.forEach(item => {
    const task = item.task || {}
    const results = item.results || []
    if (results.length === 0) {
      list.push({ taskNo: task.taskNo, taskType: task.taskType, checkItem: '-', standardValue: '-', actualValue: '-', result: null })
    } else {
      results.forEach(r => {
        list.push({ taskNo: task.taskNo, taskType: task.taskType, ...r })
      })
    }
  })
  return list
})

// 流程进度步骤
const progressSteps = computed(() => {
  if (!selectedWorkOrder.value) return []
  const status = selectedWorkOrder.value.status
  const hasReports = (detailData.productionReports?.length || 0) > 0
  const hasQuality = (detailData.qualityRecords?.length || 0) > 0
  const hasStockIn = (detailData.stockIns?.length || 0) > 0

  return [
    { label: '工单创建', state: 'done' },
    { label: '生产中', state: status >= 1 || hasReports ? (status >= 2 ? 'done' : 'active') : 'pending' },
    { label: '质检中', state: hasQuality ? (hasStockIn ? 'done' : 'active') : (status >= 2 ? 'done' : 'pending') },
    { label: '已入库', state: hasStockIn ? 'done' : 'pending' }
  ]
})

function getStatusType(status) {
  const map = { 0: 'info', 1: 'warning', 2: 'success' }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = { 0: '待生产', 1: '生产中', 2: '已完成' }
  return map[status] ?? '未知'
}

function getTaskTypeText(type) {
  const map = { IQC: '来料检验', IPQC: '过程检验', OQC: '成品检验' }
  return map[type] || type || '-'
}

function getTimelineType(type) {
  const map = {
    create: 'primary',
    report: 'warning',
    quality: 'info',
    qualityPass: 'success',
    qualityFail: 'danger',
    sn: 'primary',
    stockIn: 'success'
  }
  return map[type] || 'info'
}

function formatDateTime(date) {
  if (!date) return '-'
  const d = new Date(date)
  if (isNaN(d.getTime())) return date
  return d.toLocaleString('zh-CN', { hour12: false, month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

async function loadOverview() {
  try {
    const res = await getFlowOverview()
    const data = res.data || {}
    Object.assign(overview, {
      pending: data.pending,
      producing: data.producing,
      inspecting: data.inspecting,
      completed: data.completed,
      total: data.total
    })
    workOrders.value = data.workOrders || []
    lastUpdateTime.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
    // 默认选中第一个工单
    if (workOrders.value.length > 0 && !selectedId.value) {
      selectWorkOrder(workOrders.value[0])
    }
  } catch (e) {
    ElMessage.error('加载概览数据失败')
  }
}

async function selectWorkOrder(wo) {
  selectedId.value = wo.id
  selectedWorkOrder.value = wo
  detailLoading.value = true
  activeTab.value = 'report'
  try {
    const [detailRes, timelineRes] = await Promise.all([
      getFlowDetail(wo.id),
      getFlowTimeline(wo.id)
    ])
    Object.assign(detailData, detailRes.data || {})
    timeline.value = timelineRes.data || []
  } catch (e) {
    ElMessage.error('加载详情失败')
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  loadOverview()
})
</script>

<style scoped>
.flow-screen {
  min-height: 100vh;
  background: radial-gradient(ellipse at top, #0f2942 0%, #050d1a 50%, #02050a 100%);
  color: #e6f4ff;
  padding: 20px 24px;
  box-sizing: border-box;
}

/* 顶部 */
.screen-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(0, 170, 255, 0.2);
}
.header-left { display: flex; flex-direction: column; }
.title-main {
  font-size: 26px;
  font-weight: bold;
  background: linear-gradient(90deg, #00d4ff, #00aaff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 3px;
}
.title-sub {
  font-size: 12px;
  color: #6688aa;
  margin-top: 4px;
  letter-spacing: 2px;
}
.header-right { display: flex; align-items: center; gap: 16px; }
.update-time { font-size: 13px; color: #6688aa; display: flex; align-items: center; gap: 6px; }
.refresh-btn {
  --el-button-bg-color: rgba(0, 170, 255, 0.1);
  --el-button-border-color: rgba(0, 170, 255, 0.4);
  --el-button-text-color: #00d4ff;
  --el-button-hover-bg-color: rgba(0, 170, 255, 0.2);
  --el-button-hover-border-color: rgba(0, 170, 255, 0.6);
  --el-button-hover-text-color: #00e5ff;
}

/* 主体布局 */
.flow-body {
  display: flex;
  gap: 18px;
  align-items: flex-start;
}

/* 左侧面板 */
.left-panel {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 状态卡片网格 */
.stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.stat-card {
  background: linear-gradient(135deg, rgba(0, 100, 200, 0.2), rgba(0, 50, 100, 0.4));
  border: 1px solid rgba(0, 170, 255, 0.3);
  border-radius: 8px;
  padding: 16px 12px;
  text-align: center;
}
.stat-card.stat-pending { border-color: rgba(160, 180, 200, 0.3); }
.stat-card.stat-pending .stat-num { color: #aabbcc; }
.stat-card.stat-producing { border-color: rgba(255, 180, 0, 0.3); }
.stat-card.stat-producing .stat-num { color: #ffcc44; }
.stat-card.stat-inspecting { border-color: rgba(0, 200, 255, 0.3); }
.stat-card.stat-inspecting .stat-num { color: #00d4ff; }
.stat-card.stat-completed { border-color: rgba(0, 255, 150, 0.3); }
.stat-card.stat-completed .stat-num { color: #00ffaa; }
.stat-num { font-size: 28px; font-weight: bold; color: #00d4ff; }
.stat-label { font-size: 13px; color: #88aacc; margin-top: 4px; }

/* 工单列表 */
.list-card {
  background: linear-gradient(135deg, rgba(0, 60, 120, 0.25) 0%, rgba(0, 30, 60, 0.4) 100%);
  border: 1px solid rgba(0, 170, 255, 0.25);
  border-radius: 10px;
  overflow: hidden;
}
.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: linear-gradient(90deg, rgba(0, 170, 255, 0.15) 0%, transparent 100%);
  border-bottom: 1px solid rgba(0, 170, 255, 0.2);
}
.list-title {
  font-size: 15px;
  font-weight: bold;
  color: #00d4ff;
  display: flex;
  align-items: center;
  gap: 6px;
}
.list-count { font-size: 12px; color: #88aacc; }
.list-body {
  max-height: calc(100vh - 380px);
  overflow-y: auto;
  padding: 8px;
}
.wo-item {
  background: rgba(0, 30, 60, 0.4);
  border: 1px solid rgba(0, 170, 255, 0.12);
  border-radius: 6px;
  padding: 10px 14px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.wo-item:hover {
  background: rgba(0, 60, 120, 0.5);
  border-color: rgba(0, 170, 255, 0.4);
  transform: translateX(3px);
}
.wo-item.active {
  background: rgba(0, 100, 200, 0.35);
  border-color: #00d4ff;
  box-shadow: 0 0 12px rgba(0, 170, 255, 0.3);
}
.wo-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.wo-no {
  font-family: 'Courier New', monospace;
  font-size: 14px;
  color: #88ccff;
  font-weight: bold;
}
.wo-bottom {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #6688aa;
}
.empty-list {
  text-align: center;
  padding: 40px 0;
  color: #6688aa;
  font-size: 13px;
}

/* 右侧面板 */
.right-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.empty-detail {
  text-align: center;
  padding: 120px 0;
  background: rgba(0, 30, 60, 0.3);
  border: 1px dashed rgba(0, 170, 255, 0.2);
  border-radius: 10px;
}
.empty-icon { font-size: 60px; opacity: 0.5; }
.empty-text { margin-top: 16px; color: #6688aa; font-size: 15px; }

/* 信息卡片 */
.info-card {
  background: linear-gradient(135deg, rgba(0, 60, 120, 0.25) 0%, rgba(0, 30, 60, 0.4) 100%);
  border: 1px solid rgba(0, 170, 255, 0.25);
  border-radius: 10px;
  overflow: hidden;
}
.card-header {
  display: flex;
  align-items: center;
  padding: 12px 18px;
  background: linear-gradient(90deg, rgba(0, 170, 255, 0.15) 0%, transparent 100%);
  border-bottom: 1px solid rgba(0, 170, 255, 0.2);
}
.card-icon { font-size: 18px; margin-right: 10px; }
.card-title { font-size: 15px; font-weight: bold; color: #00d4ff; }
.card-count { margin-left: auto; font-size: 12px; color: #88aacc; }
.header-tag { margin-left: auto; }
.card-body { padding: 16px 18px; }

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.info-item {
  background: rgba(0, 30, 60, 0.5);
  padding: 10px 14px;
  border-radius: 6px;
  border-left: 3px solid #00aaff;
}
.info-label { display: block; font-size: 12px; color: #6688aa; margin-bottom: 4px; }
.info-value { font-size: 14px; color: #e6f4ff; }
.wo-code { font-family: 'Courier New', monospace; color: #88ccff; font-weight: bold; }

/* 进度条 */
.progress-steps {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 8px 12px;
}
.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  flex: 1;
}
.step-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 8px;
  z-index: 2;
  background: #0a1a2a;
  border: 2px solid #445566;
  color: #6688aa;
  transition: all 0.3s;
}
.step-item.done .step-circle {
  background: linear-gradient(135deg, #00aaff, #0088cc);
  border-color: #00d4ff;
  color: #fff;
  box-shadow: 0 0 12px rgba(0, 170, 255, 0.5);
}
.step-item.active .step-circle {
  background: linear-gradient(135deg, #ffcc44, #ff9900);
  border-color: #ffcc44;
  color: #fff;
  box-shadow: 0 0 12px rgba(255, 204, 68, 0.5);
  animation: stepPulse 1.5s infinite;
}
@keyframes stepPulse {
  0%, 100% { box-shadow: 0 0 12px rgba(255, 204, 68, 0.5); }
  50% { box-shadow: 0 0 20px rgba(255, 204, 68, 0.8); }
}
.step-label { font-size: 13px; color: #88aacc; }
.step-item.done .step-label { color: #00d4ff; font-weight: bold; }
.step-item.active .step-label { color: #ffcc44; font-weight: bold; }
.step-line {
  position: absolute;
  top: 18px;
  left: 50%;
  width: 100%;
  height: 2px;
  background: #445566;
  z-index: 1;
}
.step-item.done .step-line { background: linear-gradient(90deg, #00aaff, #445566); }

/* 时间线 */
.timeline-body { max-height: 360px; overflow-y: auto; padding-left: 8px; }
.timeline-event { display: flex; flex-direction: column; gap: 4px; }
.event-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: bold;
  color: #e6f4ff;
}
.event-icon { color: #00d4ff; }
.event-desc { font-size: 12px; color: #88aacc; }
.empty-timeline { text-align: center; padding: 30px 0; color: #6688aa; }

/* 深色表格样式 */
.dark-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-border-color: rgba(0, 170, 255, 0.15);
  --el-table-header-bg-color: rgba(0, 60, 120, 0.5);
  --el-table-row-hover-bg-color: rgba(0, 170, 255, 0.08);
  --el-table-text-color: #cce0f5;
  --el-table-header-text-color: #00d4ff;
  background: transparent !important;
}
.dark-table :deep(.el-table__inner-wrapper) {
  background: transparent;
}
.dark-table :deep(th.el-table__cell) {
  background: rgba(0, 60, 120, 0.5) !important;
  color: #00d4ff !important;
  font-weight: 600;
}
.dark-table :deep(td.el-table__cell) {
  background: rgba(0, 30, 60, 0.3) !important;
  color: #cce0f5;
}
.dark-table :deep(.el-table__body tr:hover td.el-table__cell) {
  background: rgba(0, 170, 255, 0.08) !important;
}
.dark-table :deep(.el-table__empty-text) { color: #6688aa; }

/* Tabs */
.flow-tabs :deep(.el-tabs__item) {
  color: #88aacc;
}
.flow-tabs :deep(.el-tabs__item.is-active) {
  color: #00d4ff;
}
.flow-tabs :deep(.el-tabs__active-bar) {
  background-color: #00d4ff;
}
.flow-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: rgba(0, 170, 255, 0.15);
}
.empty-tab { text-align: center; padding: 30px 0; color: #6688aa; font-size: 13px; }

/* 时间线深色适配 */
.timeline-body :deep(.el-timeline-item__timestamp) {
  color: #6688aa;
}
.timeline-body :deep(.el-timeline-item__node--primary) {
  background-color: #00aaff;
}
.timeline-body :deep(.el-timeline-item__tail) {
  border-left-color: rgba(0, 170, 255, 0.2);
}
</style>
