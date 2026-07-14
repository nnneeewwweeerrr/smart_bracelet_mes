<template>
  <div class="trace-screen">
    <!-- 顶部标题 -->
    <div class="screen-header">
      <div class="header-left">
        <el-button class="back-btn" @click="goBack" :icon="ArrowLeft" circle plain />
        <div class="title-text">
          <span class="title-main">SN全生命周期追溯</span>
          <span class="title-sub">Smart Bracelet Full Lifecycle Traceability</span>
        </div>
      </div>
      <div class="header-right">
        <div class="mode-switcher">
          <el-radio-group v-model="traceMode" size="default" @change="handleModeChange">
            <el-radio-button label="reverse">
              <span>🔍 逆向追溯</span>
            </el-radio-button>
            <el-radio-button label="forward">
              <span>📡 正向追溯</span>
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </div>

    <!-- 查询区域 -->
    <div class="search-section">
      <div class="search-box">
        <el-input
          v-model="searchCode"
          :placeholder="traceMode === 'reverse' ? '请输入成品SN码，如 SN202607010001' : '请输入物料批次号，如 BATCH2026062001'"
          class="search-input"
          size="large"
          @keyup.enter="doSearch"
          clearable
        >
          <template #prefix>
            <span class="search-icon">{{ traceMode === 'reverse' ? '🔎' : '📦' }}</span>
          </template>
          <template #append>
            <el-button type="primary" size="large" @click="doSearch" :loading="loading">
              {{ loading ? '查询中' : '立即追溯' }}
            </el-button>
          </template>
        </el-input>
        <div class="quick-tags">
          <span class="tags-label">快速查询：</span>
          <el-tag
            v-for="tag in quickTags"
            :key="tag.code"
            :type="traceMode === tag.mode ? 'primary' : 'info'"
            effect="dark"
            class="quick-tag"
            @click="quickSearch(tag)"
          >
            {{ tag.label }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!hasSearched" class="empty-state">
      <div class="empty-icon">🌐</div>
      <div class="empty-text">
        <p>输入SN码或物料批次号开始追溯</p>
        <p class="empty-sub">支持双向追溯，快速定位产品全链路信息</p>
      </div>
    </div>

    <!-- 逆向追溯结果 -->
    <div v-else-if="traceMode === 'reverse'" class="trace-content reverse-mode">
      <div v-if="!reverseData?.exists" class="not-found">
        <div class="nf-icon">❓</div>
        <div class="nf-text">未找到SN码信息</div>
        <div class="nf-sub">{{ reverseData?.message }}</div>
      </div>

      <template v-else>
        <!-- SN基本信息卡片 -->
        <div class="info-card sn-card">
          <div class="card-header">
            <span class="card-icon">⌚</span>
            <span class="card-title">产品SN档案</span>
            <el-tag v-if="reverseData.stats?.hasException" type="danger" effect="dark" class="exception-tag">
              ⚠ 存在异常
            </el-tag>
            <el-tag v-else type="success" effect="dark" class="exception-tag">
              ✓ 全链路正常
            </el-tag>
          </div>
          <div class="card-body sn-body">
            <div class="sn-main">
              <div class="sn-code">{{ reverseData.snInfo.snCode }}</div>
              <div class="sn-name">{{ reverseData.snInfo.productName }}</div>
            </div>
            <div class="sn-grid">
              <div class="sn-item">
                <span class="item-label">产品编码</span>
                <span class="item-value">{{ reverseData.snInfo.productCode }}</span>
              </div>
              <div class="sn-item">
                <span class="item-label">所属工单</span>
                <span class="item-value">{{ reverseData.snInfo.workOrderNo }}</span>
              </div>
              <div class="sn-item">
                <span class="item-label">关联订单</span>
                <span class="item-value">{{ reverseData.snInfo.orderNo || '-' }}</span>
              </div>
              <div class="sn-item">
                <span class="item-label">客户名称</span>
                <span class="item-value">{{ reverseData.snInfo.customerName || '-' }}</span>
              </div>
              <div class="sn-item">
                <span class="item-label">生产日期</span>
                <span class="item-value">{{ formatDate(reverseData.snInfo.productionDate) }}</span>
              </div>
              <div class="sn-item">
                <span class="item-label">当前状态</span>
                <el-tag :type="getStatusType(reverseData.snInfo.status)" effect="dark" size="small">
                  {{ getStatusText(reverseData.snInfo.status) }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 统计概览 -->
        <div class="stats-row">
          <div class="stat-card">
            <div class="stat-num">{{ reverseData.stats?.materialCount || 0 }}</div>
            <div class="stat-label">物料批次</div>
          </div>
          <div class="stat-card">
            <div class="stat-num">{{ reverseData.stats?.processCount || 0 }}</div>
            <div class="stat-label">加工工序</div>
          </div>
          <div class="stat-card">
            <div class="stat-num">{{ reverseData.stats?.qualityCount || 0 }}</div>
            <div class="stat-label">质检项目</div>
          </div>
          <div class="stat-card pass">
            <div class="stat-num">{{ reverseData.stats?.qualityPassCount || 0 }}</div>
            <div class="stat-label">合格项</div>
          </div>
          <div class="stat-card fail">
            <div class="stat-num">{{ reverseData.stats?.qualityFailCount || 0 }}</div>
            <div class="stat-label">不合格项</div>
          </div>
        </div>

        <!-- 质检统计图表 -->
        <div class="info-card chart-card">
          <div class="card-header">
            <span class="card-icon">📊</span>
            <span class="card-title">质检结果统计</span>
          </div>
          <div class="card-body">
            <div id="quality-chart" class="chart-container"></div>
          </div>
        </div>

        <!-- 物料明细 -->
        <div class="info-card">
          <div class="card-header">
            <span class="card-icon">🧩</span>
            <span class="card-title">物料明细</span>
            <span class="card-count">共 {{ reverseData.materialList?.length || 0 }} 个物料批次</span>
          </div>
          <div class="card-body">
            <table class="data-table">
              <thead>
                <tr>
                  <th>物料编码</th>
                  <th>物料名称</th>
                  <th>批次号</th>
                  <th>用量</th>
                  <th>上料工序</th>
                  <th>操作工</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(mat, i) in reverseData.materialList" :key="i">
                  <td><span class="code-text">{{ mat.materialCode }}</span></td>
                  <td>{{ mat.materialName }}</td>
                  <td><el-tag type="warning" effect="dark" size="small">{{ mat.batchNo }}</el-tag></td>
                  <td>{{ mat.quantity }}</td>
                  <td>{{ mat.operatorName }}</td>
                  <td>{{ mat.operatorName }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 生产过程时间轴 -->
        <div class="info-card">
          <div class="card-header">
            <span class="card-icon">⚙️</span>
            <span class="card-title">生产过程</span>
            <span class="card-count">共 {{ reverseData.processList?.length || 0 }} 道工序</span>
          </div>
          <div class="card-body process-body">
            <div class="timeline">
              <div
                v-for="(proc, i) in reverseData.processList"
                :key="i"
                class="timeline-item"
                :class="{ 'is-exception': proc.status === 2 }"
              >
                <div class="timeline-dot">
                  <span v-if="proc.status === 1">✓</span>
                  <span v-else-if="proc.status === 2">!</span>
                  <span v-else>○</span>
                </div>
                <div class="timeline-content">
                  <div class="proc-header">
                    <span class="proc-name">{{ proc.processName }}</span>
                    <el-tag
                      :type="proc.status === 1 ? 'success' : proc.status === 2 ? 'danger' : 'info'"
                      effect="dark"
                      size="small"
                    >
                      {{ proc.status === 1 ? '已完成' : proc.status === 2 ? '异常' : '进行中' }}
                    </el-tag>
                  </div>
                  <div class="proc-detail">
                    <span>👤 {{ proc.workerName }}</span>
                    <span>🔧 {{ proc.equipmentName }}</span>
                    <span>🕐 {{ formatDate(proc.startTime) }} ~ {{ formatDate(proc.endTime) }}</span>
                  </div>
                  <div v-if="proc.remark" class="proc-remark">
                    <span class="remark-label">备注：</span>{{ proc.remark }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 质检记录 -->
        <div class="info-card">
          <div class="card-header">
            <span class="card-icon">📋</span>
            <span class="card-title">质检记录</span>
            <span class="card-count">共 {{ reverseData.qualityList?.length || 0 }} 条</span>
          </div>
          <div class="card-body">
            <table class="data-table">
              <thead>
                <tr>
                  <th>检验类型</th>
                  <th>工序</th>
                  <th>检验项</th>
                  <th>标准值</th>
                  <th>实际值</th>
                  <th>结果</th>
                  <th>检验员</th>
                  <th>检验时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(q, i) in reverseData.qualityList" :key="i" :class="{ 'row-fail': q.result === 0 }">
                  <td>
                    <el-tag :type="getQcType(q.taskType)" effect="dark" size="small">
                      {{ q.taskType }}
                    </el-tag>
                  </td>
                  <td>{{ q.processName }}</td>
                  <td>{{ q.checkItem }}</td>
                  <td class="std-val">{{ q.standardValue }}</td>
                  <td class="act-val">{{ q.actualValue }}</td>
                  <td>
                    <el-tag :type="q.result === 1 ? 'success' : 'danger'" effect="dark" size="small">
                      {{ q.result === 1 ? '合格' : '不合格' }}
                    </el-tag>
                  </td>
                  <td>{{ q.inspectorName }}</td>
                  <td>{{ formatDate(q.checkTime) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 归属信息 -->
        <div class="info-card">
          <div class="card-header">
            <span class="card-icon">🏢</span>
            <span class="card-title">归属信息</span>
          </div>
          <div class="card-body owner-body">
            <div class="owner-item">
              <span class="owner-label">客户订单</span>
              <span class="owner-value">{{ reverseData.snInfo.orderNo || '-' }}</span>
            </div>
            <div class="owner-item">
              <span class="owner-label">客户名称</span>
              <span class="owner-value">{{ reverseData.snInfo.customerName || '-' }}</span>
            </div>
            <div class="owner-item">
              <span class="owner-label">生产工单</span>
              <span class="owner-value">{{ reverseData.snInfo.workOrderNo }}</span>
            </div>
            <div class="owner-item">
              <span class="owner-label">产品型号</span>
              <span class="owner-value">{{ reverseData.snInfo.productName }}</span>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 正向追溯结果 -->
    <div v-else class="trace-content forward-mode">
      <div v-if="!forwardData?.exists" class="not-found">
        <div class="nf-icon">❓</div>
        <div class="nf-text">未找到该批次物料</div>
        <div class="nf-sub">{{ forwardData?.message }}</div>
      </div>

      <template v-else>
        <!-- 物料批次信息 -->
        <div class="info-card sn-card">
          <div class="card-header">
            <span class="card-icon">📦</span>
            <span class="card-title">物料批次信息</span>
          </div>
          <div class="card-body sn-body">
            <div class="sn-main">
              <div class="sn-code">{{ forwardData.batchInfo.batchNo }}</div>
              <div class="sn-name">{{ forwardData.batchInfo.materialName }}</div>
            </div>
            <div class="sn-grid">
              <div class="sn-item">
                <span class="item-label">物料编码</span>
                <span class="item-value">{{ forwardData.batchInfo.materialCode }}</span>
              </div>
              <div class="sn-item">
                <span class="item-label">涉及成品数</span>
                <el-tag type="warning" effect="dark">{{ forwardData.batchInfo.totalSnCount }} 个SN</el-tag>
              </div>
              <div class="sn-item">
                <span class="item-label">涉及工单数</span>
                <el-tag type="info" effect="dark">{{ forwardData.workOrderGroups?.length || 0 }} 个工单</el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 按工单分组的SN列表 -->
        <div v-for="(group, gi) in forwardData.workOrderGroups" :key="gi" class="info-card">
          <div class="card-header">
            <span class="card-icon">📋</span>
            <span class="card-title">{{ group.workOrderNo }}</span>
            <span class="card-count">{{ group.productName }} · {{ group.snCount }} 个成品</span>
          </div>
          <div class="card-body">
            <div class="sn-grid-list">
              <el-tag
                v-for="sn in group.snCodes"
                :key="sn"
                type="primary"
                effect="dark"
                class="sn-tag-item"
                @click="jumpToReverse(sn)"
              >
                {{ sn }}
              </el-tag>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 底部装饰 -->
    <div class="screen-footer">
      <div class="footer-text">智能手环MES系统 · SN全生命周期追溯平台</div>
      <div class="footer-time">{{ currentTime }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getReverseTrace, getForwardTrace } from '../../api/snTrace'
import * as echarts from 'echarts'

const router = useRouter()

function goBack() {
  router.back()
}

const traceMode = ref('reverse')
const searchCode = ref('')
const loading = ref(false)
const hasSearched = ref(false)
const reverseData = reactive({})
const forwardData = reactive({})
const currentTime = ref('')

let timer = null
let qualityChart = null

const quickTags = computed(() => {
  if (traceMode.value === 'reverse') {
    return [
      { code: 'SN202607080001', label: '标准版SN1', mode: 'reverse' },
      { code: 'SN202607080002', label: '标准版SN2', mode: 'reverse' },
      { code: 'SN202607080003', label: '标准版SN3', mode: 'reverse' },
      { code: 'SN202607080004', label: 'Pro版SN1', mode: 'reverse' },
      { code: 'SN202607080005', label: 'Pro版SN2', mode: 'reverse' },
      { code: 'SN202607080007', label: '含异常SN', mode: 'reverse' }
    ]
  } else {
    return [
      { code: 'BATCH2026061501', label: '硅胶膜批次', mode: 'forward' },
      { code: 'BATCH2026061601', label: '触控屏批次', mode: 'forward' },
      { code: 'BATCH2026061701', label: '锂离子电芯批次', mode: 'forward' },
      { code: 'BATCH2026061801', label: 'PCB主板批次', mode: 'forward' },
      { code: 'BATCH2026061901', label: '传感器模块批次', mode: 'forward' },
      { code: 'BATCH2026062001', label: '蓝牙模块批次', mode: 'forward' }
    ]
  }
})

function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', { hour12: false })
}

function handleModeChange() {
  searchCode.value = ''
  hasSearched.value = false
}

function quickSearch(tag) {
  traceMode.value = tag.mode
  searchCode.value = tag.code
  doSearch()
}

async function doSearch() {
  if (!searchCode.value.trim()) {
    ElMessage.warning(traceMode.value === 'reverse' ? '请输入SN码' : '请输入物料批次号')
    return
  }
  loading.value = true
  hasSearched.value = true
  try {
    if (traceMode.value === 'reverse') {
      const res = await getReverseTrace(searchCode.value.trim())
      Object.assign(reverseData, res.data || {})
      nextTick(() => {
        renderQualityChart()
      })
    } else {
      const res = await getForwardTrace(searchCode.value.trim())
      Object.assign(forwardData, res.data || {})
    }
  } catch (e) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

function renderQualityChart() {
  if (!reverseData.qualityList || reverseData.qualityList.length === 0) return
  
  const chartDom = document.getElementById('quality-chart')
  if (!chartDom) return
  
  if (qualityChart) {
    qualityChart.dispose()
  }
  
  qualityChart = echarts.init(chartDom)
  
  const passCount = reverseData.qualityList.filter(q => q.result === 1).length
  const failCount = reverseData.qualityList.filter(q => q.result === 0).length
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      textStyle: { color: '#88aacc' }
    },
    series: [{
      name: '质检结果',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: 'rgba(0, 170, 255, 0.2)',
        borderWidth: 2
      },
      label: {
        show: true,
        color: '#e6f4ff',
        fontSize: 14,
        formatter: '{b}\n{c}项'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 16,
          fontWeight: 'bold'
        },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 170, 255, 0.5)'
        }
      },
      data: [
        { value: passCount, name: '合格', itemStyle: { color: '#00ffaa' } },
        { value: failCount, name: '不合格', itemStyle: { color: '#ff7777' } }
      ]
    }]
  }
  
  qualityChart.setOption(option)
  
  window.addEventListener('resize', () => {
    qualityChart?.resize()
  })
}

function jumpToReverse(sn) {
  traceMode.value = 'reverse'
  searchCode.value = sn
  doSearch()
}

function formatDate(date) {
  if (!date) return '-'
  const d = new Date(date)
  if (isNaN(d.getTime())) return date
  return d.toLocaleString('zh-CN', { hour12: false, month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function getStatusType(status) {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'info' }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = { 0: '在产', 1: '成品', 2: '已出库', 3: '已售后' }
  return map[status] || '未知'
}

function getQcType(type) {
  const map = { IQC: 'info', PQC: 'warning', OQC: 'success' }
  return map[type] || 'info'
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.trace-screen {
  min-height: 100vh;
  background: radial-gradient(ellipse at top, #0f2942 0%, #050d1a 50%, #02050a 100%);
  color: #e6f4ff;
  padding: 20px 30px 80px;
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
.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}
.back-btn {
  --el-button-bg-color: rgba(0, 170, 255, 0.1);
  --el-button-border-color: rgba(0, 170, 255, 0.4);
  --el-button-text-color: #00d4ff;
  --el-button-hover-bg-color: rgba(0, 170, 255, 0.2);
  --el-button-hover-border-color: rgba(0, 170, 255, 0.6);
  --el-button-hover-text-color: #00e5ff;
}
.title-text { display: flex; flex-direction: column; }
.title-main {
  font-size: 28px;
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

/* 查询区 */
.search-section { margin-bottom: 25px; }
.search-box {
  max-width: 700px;
  margin: 0 auto;
}
.search-input :deep(.el-input__wrapper) {
  background: rgba(0, 40, 80, 0.5);
  border: 1px solid rgba(0, 170, 255, 0.4);
  box-shadow: none;
}
.search-input :deep(.el-input__inner) { color: #e6f4ff; }
.search-icon { font-size: 18px; }
.quick-tags {
  margin-top: 12px;
  text-align: center;
}
.tags-label { color: #6688aa; font-size: 13px; margin-right: 8px; }
.quick-tag {
  margin-right: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.quick-tag:hover { transform: scale(1.05); }

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 100px 0;
}
.empty-icon { font-size: 80px; opacity: 0.5; }
.empty-text { margin-top: 20px; color: #6688aa; }
.empty-text p { margin: 6px 0; }
.empty-sub { font-size: 13px; opacity: 0.7; }

.not-found {
  text-align: center;
  padding: 60px 0;
  background: rgba(255, 60, 60, 0.05);
  border: 1px solid rgba(255, 100, 100, 0.2);
  border-radius: 8px;
}
.nf-icon { font-size: 50px; }
.nf-text { font-size: 18px; color: #ff7777; margin-top: 12px; }
.nf-sub { font-size: 13px; color: #999; margin-top: 6px; }

/* 卡片 */
.info-card {
  background: linear-gradient(135deg, rgba(0, 60, 120, 0.25) 0%, rgba(0, 30, 60, 0.4) 100%);
  border: 1px solid rgba(0, 170, 255, 0.25);
  border-radius: 10px;
  margin-bottom: 18px;
  backdrop-filter: blur(5px);
  overflow: hidden;
}
.card-header {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  background: linear-gradient(90deg, rgba(0, 170, 255, 0.15) 0%, transparent 100%);
  border-bottom: 1px solid rgba(0, 170, 255, 0.2);
}
.card-icon { font-size: 18px; margin-right: 10px; }
.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #00d4ff;
}
.card-count {
  margin-left: auto;
  font-size: 13px;
  color: #88aacc;
}
.exception-tag { margin-left: auto; }
.card-body { padding: 18px 20px; }

/* SN卡片 */
.sn-body { display: flex; gap: 30px; align-items: center; }
.sn-main { text-align: center; min-width: 220px; }
.sn-code {
  font-size: 32px;
  font-weight: bold;
  font-family: 'Courier New', monospace;
  background: linear-gradient(90deg, #00ffcc, #00aaff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 2px;
}
.sn-name {
  font-size: 14px;
  color: #88ccff;
  margin-top: 6px;
}
.sn-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.sn-item {
  background: rgba(0, 30, 60, 0.5);
  padding: 10px 14px;
  border-radius: 6px;
  border-left: 3px solid #00aaff;
}
.item-label { display: block; font-size: 12px; color: #6688aa; margin-bottom: 4px; }
.item-value { font-size: 14px; color: #e6f4ff; }

/* 统计行 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-bottom: 18px;
}
.stat-card {
  background: linear-gradient(135deg, rgba(0, 100, 200, 0.2), rgba(0, 50, 100, 0.4));
  border: 1px solid rgba(0, 170, 255, 0.3);
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}
.stat-card.pass { border-color: rgba(0, 255, 150, 0.3); }
.stat-card.pass .stat-num { color: #00ffaa; }
.stat-card.fail { border-color: rgba(255, 100, 100, 0.3); }
.stat-card.fail .stat-num { color: #ff7777; }
.stat-num { font-size: 28px; font-weight: bold; color: #00d4ff; }
.stat-label { font-size: 13px; color: #88aacc; margin-top: 4px; }

/* 数据表格 */
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.data-table th {
  background: rgba(0, 60, 120, 0.5);
  color: #00d4ff;
  padding: 10px 12px;
  text-align: left;
  font-weight: normal;
  border-bottom: 1px solid rgba(0, 170, 255, 0.3);
}
.data-table td {
  padding: 10px 12px;
  border-bottom: 1px solid rgba(0, 170, 255, 0.08);
  color: #cce0f5;
}
.data-table tr:hover td { background: rgba(0, 170, 255, 0.05); }
.data-table tr.row-fail td { background: rgba(255, 60, 60, 0.08); }
.code-text {
  font-family: 'Courier New', monospace;
  color: #88ccff;
}
.std-val { color: #ffcc66; }
.act-val { color: #aaff88; }

/* 时间轴 */
.timeline { padding-left: 20px; position: relative; }
.timeline::before {
  content: '';
  position: absolute;
  left: 10px;
  top: 8px;
  bottom: 8px;
  width: 2px;
  background: linear-gradient(180deg, #00aaff, #0066aa);
}
.timeline-item {
  position: relative;
  padding-bottom: 18px;
  padding-left: 24px;
}
.timeline-dot {
  position: absolute;
  left: -14px;
  top: 2px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #0a1a2a;
  border: 2px solid #00aaff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: bold;
  color: #00d4ff;
}
.timeline-item.is-exception .timeline-dot {
  border-color: #ff5555;
  color: #ff5555;
  animation: pulse 1.5s infinite;
}
@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255, 85, 85, 0.6); }
  50% { box-shadow: 0 0 0 8px rgba(255, 85, 85, 0); }
}
.timeline-content {
  background: rgba(0, 30, 60, 0.4);
  border: 1px solid rgba(0, 170, 255, 0.15);
  border-radius: 6px;
  padding: 10px 14px;
}
.proc-header {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}
.proc-name {
  font-size: 15px;
  font-weight: bold;
  color: #cce0f5;
  margin-right: 10px;
}
.proc-detail {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #6688aa;
  flex-wrap: wrap;
}
.proc-remark {
  margin-top: 6px;
  font-size: 12px;
  color: #ffcc66;
}
.remark-label { color: #886633; }

/* 归属 */
.owner-body {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.owner-item {
  background: rgba(0, 30, 60, 0.5);
  padding: 12px 16px;
  border-radius: 6px;
  text-align: center;
  border-left: 3px solid #00ffaa;
}
.owner-label { display: block; font-size: 12px; color: #6688aa; margin-bottom: 6px; }
.owner-value { font-size: 14px; color: #aaffdd; font-weight: bold; }

/* SN网格列表 */
.sn-grid-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.sn-tag-item {
  padding: 6px 14px !important;
  font-size: 13px !important;
  cursor: pointer;
  transition: all 0.2s;
}
.sn-tag-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 170, 255, 0.3);
}

/* 图表卡片 */
.chart-card {
  background: linear-gradient(135deg, rgba(0, 80, 150, 0.25) 0%, rgba(0, 40, 80, 0.4) 100%);
}
.chart-container {
  height: 280px;
  width: 100%;
}

/* 底部 */
.screen-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 30px;
  background: rgba(0, 20, 40, 0.9);
  border-top: 1px solid rgba(0, 170, 255, 0.2);
  font-size: 12px;
  color: #6688aa;
}
</style>
