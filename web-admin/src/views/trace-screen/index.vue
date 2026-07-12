<template>
  <div class="trace-screen">
    <!-- 顶部标题栏 -->
    <div class="screen-header">
      <div class="header-left">
        <span class="header-date">{{ currentDate }}</span>
      </div>
      <div class="header-center">
        <div class="title-decoration left"></div>
        <h1 class="screen-title">智能手环MES · 订单全链路追溯</h1>
        <div class="title-decoration right"></div>
      </div>
      <div class="header-right">
        <span class="header-time">{{ currentTime }}</span>
        <button class="back-btn" @click="goBack">返回系统</button>
      </div>
    </div>

    <!-- 查询区域 -->
    <div class="search-section">
      <div class="search-box">
        <div class="search-icon">🔍</div>
        <input
          v-model="searchOrderNo"
          class="search-input"
          placeholder="请输入订单号，如 SO202501001"
          @keyup.enter="handleSearch"
          @input="handleSearchInput"
          @focus="showSuggestions = true"
          @blur="handleBlur"
        />
        <button class="search-btn" @click="handleSearch">追溯查询</button>
        <div v-if="showSuggestions && suggestionList.length > 0" class="suggestion-dropdown">
          <div
            v-for="item in suggestionList"
            :key="item.orderNo"
            class="suggestion-item"
            @mousedown="selectSuggestion(item.orderNo)"
          >
            <span class="sug-order">{{ item.orderNo }}</span>
            <span class="sug-product">{{ item.productName }}</span>
            <span class="sug-qty">{{ item.quantity }}件</span>
            <span class="sug-status" :class="'status-' + item.status">{{ item.statusText }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="screen-body" v-loading="loading" element-loading-background="rgba(3,10,26,0.8)">
      <!-- 订单基本信息卡片 -->
      <div v-if="traceData.orderInfo" class="order-info-card">
        <div class="order-info-left">
          <div class="order-no-label">订单编号</div>
          <div class="order-no-value">{{ traceData.orderInfo.orderNo }}</div>
        </div>
        <div class="order-info-divider"></div>
        <div class="order-info-item">
          <div class="info-label">客户名称</div>
          <div class="info-value">{{ traceData.orderInfo.customerName }}</div>
        </div>
        <div class="order-info-item">
          <div class="info-label">产品名称</div>
          <div class="info-value">{{ traceData.orderInfo.productName }}</div>
        </div>
        <div class="order-info-item">
          <div class="info-label">订单数量</div>
          <div class="info-value highlight">{{ traceData.orderInfo.quantity }} 件</div>
        </div>
        <div class="order-info-item">
          <div class="info-label">订单金额</div>
          <div class="info-value highlight">¥ {{ formatAmount(traceData.orderInfo.totalAmount) }}</div>
        </div>
        <div class="order-info-item">
          <div class="info-label">订单状态</div>
          <div class="info-value">
            <span class="order-status" :class="'order-status-' + traceData.orderInfo.status">
              {{ traceData.orderInfo.statusText }}
            </span>
          </div>
        </div>
        <div class="order-info-item">
          <div class="info-label">交货日期</div>
          <div class="info-value">{{ formatDate(traceData.orderInfo.deliveryDate) }}</div>
        </div>
        <div class="order-progress">
          <div class="progress-label">整体进度</div>
          <div class="progress-bar-wrap">
            <div class="progress-bar-bg">
              <div class="progress-bar-fill" :style="{ width: traceData.overallProgress + '%' }"></div>
            </div>
            <span class="progress-text">{{ traceData.overallProgress }}%</span>
          </div>
        </div>
      </div>

      <!-- 链路流程图 -->
      <div class="trace-flow">
        <div class="flow-container">
          <div
            v-for="(node, index) in traceData.nodes"
            :key="index"
            class="flow-node"
            :class="[
              'node-' + node.status,
              { 'active': activeNodeIndex === index }
            ]"
            @click="toggleNodeDetail(index)"
          >
            <!-- 节点圆圈 -->
            <div class="node-circle">
              <div class="node-icon">{{ getNodeIcon(node.icon) }}</div>
              <div class="node-index">{{ node.index }}</div>
            </div>
            <!-- 节点名称 -->
            <div class="node-info">
              <div class="node-name">{{ node.name }}</div>
              <div class="node-name-en">{{ node.nameEn }}</div>
              <div class="node-time">{{ formatDateTime(node.time) }}</div>
            </div>
            <!-- 状态标签 -->
            <div class="node-status-tag" :class="'status-' + node.status">
              {{ getStatusText(node.status) }}
            </div>
            <!-- 连接线 -->
            <div v-if="index < traceData.nodes.length - 1" class="node-connector">
              <div class="connector-line"></div>
              <div class="connector-arrow">▶</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 详情面板 -->
      <div v-if="activeNodeIndex !== null && traceData.nodes[activeNodeIndex]" class="detail-panel">
        <div class="detail-header">
          <span class="detail-title">
            <span class="detail-icon">{{ getNodeIcon(traceData.nodes[activeNodeIndex].icon) }}</span>
            {{ traceData.nodes[activeNodeIndex].name }} - 详情
          </span>
          <button class="detail-close" @click="activeNodeIndex = null">✕</button>
        </div>
        <div class="detail-body">
          <template v-if="activeNodeIndex === 0">
            <div class="detail-grid">
              <div class="detail-item">
                <span class="detail-label">订单编号</span>
                <span class="detail-value">{{ traceData.orderInfo?.orderNo }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">订单数量</span>
                <span class="detail-value">{{ traceData.orderInfo?.quantity }} 件</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">订单金额</span>
                <span class="detail-value">¥ {{ formatAmount(traceData.orderInfo?.totalAmount) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">创建时间</span>
                <span class="detail-value">{{ formatDateTime(traceData.orderInfo?.createTime) }}</span>
              </div>
            </div>
          </template>

          <template v-else-if="activeNodeIndex === 1">
            <div class="detail-summary">
              <div class="summary-stat">
                <div class="stat-value">{{ traceData.nodes[1]?.detail?.purchaseCount || 0 }}</div>
                <div class="stat-label">采购单数</div>
              </div>
              <div class="summary-stat">
                <div class="stat-value">{{ traceData.nodes[1]?.detail?.totalQuantity || 0 }}</div>
                <div class="stat-label">采购总量</div>
              </div>
            </div>
            <div class="detail-table">
              <table>
                <thead>
                  <tr>
                    <th>采购单号</th>
                    <th>物料名称</th>
                    <th>供应商</th>
                    <th>数量</th>
                    <th>状态</th>
                    <th>创建时间</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in traceData.nodes[1]?.detail?.purchaseList || []" :key="item.purchaseNo">
                    <td>{{ item.purchaseNo }}</td>
                    <td>{{ item.materialName }}</td>
                    <td>{{ item.supplierName }}</td>
                    <td>{{ item.quantity }}</td>
                    <td><span class="table-status">{{ item.status }}</span></td>
                    <td>{{ formatDateTime(item.createTime) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

          <template v-else-if="activeNodeIndex === 2">
            <div class="detail-summary">
              <div class="summary-stat">
                <div class="stat-value">{{ traceData.nodes[2]?.detail?.taskCount || 0 }}</div>
                <div class="stat-label">检验任务</div>
              </div>
              <div class="summary-stat good">
                <div class="stat-value">{{ traceData.nodes[2]?.detail?.passCount || 0 }}</div>
                <div class="stat-label">合格项</div>
              </div>
              <div class="summary-stat bad">
                <div class="stat-value">{{ traceData.nodes[2]?.detail?.failCount || 0 }}</div>
                <div class="stat-label">不合格项</div>
              </div>
              <div class="summary-stat rate">
                <div class="stat-value">{{ traceData.nodes[2]?.detail?.passRate || 100 }}%</div>
                <div class="stat-label">合格率</div>
              </div>
            </div>
            <div class="detail-table">
              <table>
                <thead>
                  <tr>
                    <th>任务编号</th>
                    <th>状态</th>
                    <th>创建时间</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in traceData.nodes[2]?.detail?.taskList || []" :key="item.taskNo">
                    <td>{{ item.taskNo }}</td>
                    <td><span class="table-status">{{ item.status }}</span></td>
                    <td>{{ formatDateTime(item.createTime) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

          <template v-else-if="activeNodeIndex === 3">
            <div class="detail-summary">
              <div class="summary-stat">
                <div class="stat-value">{{ traceData.nodes[3]?.detail?.stockInCount || 0 }}</div>
                <div class="stat-label">入库单数</div>
              </div>
              <div class="summary-stat">
                <div class="stat-value">{{ traceData.nodes[3]?.detail?.totalQuantity || 0 }}</div>
                <div class="stat-label">入库总量</div>
              </div>
            </div>
            <div class="detail-table">
              <table>
                <thead>
                  <tr>
                    <th>入库单号</th>
                    <th>物料名称</th>
                    <th>仓库</th>
                    <th>数量</th>
                    <th>来源类型</th>
                    <th>创建时间</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in traceData.nodes[3]?.detail?.stockInList || []" :key="item.inNo">
                    <td>{{ item.inNo }}</td>
                    <td>{{ item.materialName }}</td>
                    <td>{{ item.warehouseName }}</td>
                    <td>{{ item.quantity }}</td>
                    <td>{{ item.sourceType }}</td>
                    <td>{{ formatDateTime(item.createTime) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

          <template v-else-if="activeNodeIndex === 4">
            <div class="detail-summary">
              <div class="summary-stat">
                <div class="stat-value">{{ traceData.nodes[4]?.detail?.planCount || 0 }}</div>
                <div class="stat-label">生产计划</div>
              </div>
              <div class="summary-stat">
                <div class="stat-value">{{ traceData.nodes[4]?.detail?.workOrderCount || 0 }}</div>
                <div class="stat-label">工单数量</div>
              </div>
              <div class="summary-stat good">
                <div class="stat-value">{{ traceData.nodes[4]?.detail?.completedQty || 0 }}</div>
                <div class="stat-label">已完成数量</div>
              </div>
              <div class="summary-stat rate">
                <div class="stat-value">{{ traceData.nodes[4]?.detail?.progress || 0 }}%</div>
                <div class="stat-label">生产进度</div>
              </div>
            </div>

            <div class="detail-section-title">各工序进度</div>
            <div class="process-progress-list">
              <div
                v-for="item in traceData.nodes[4]?.detail?.processProgress || []"
                :key="item.processName"
                class="process-item"
              >
                <div class="process-name">{{ item.processName }}</div>
                <div class="process-bar-wrap">
                  <div class="process-bar-bg">
                    <div class="process-bar-fill" :style="{ width: item.progress + '%' }"></div>
                  </div>
                  <span class="process-percent">{{ item.progress }}%</span>
                </div>
                <div class="process-qty">
                  <span class="good">合格 {{ item.goodQty }}</span>
                  <span class="bad" v-if="item.badQty > 0">不良 {{ item.badQty }}</span>
                </div>
              </div>
            </div>

            <div class="detail-section-title">工单列表</div>
            <div class="detail-table">
              <table>
                <thead>
                  <tr>
                    <th>工单号</th>
                    <th>工序名称</th>
                    <th>数量</th>
                    <th>状态</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in traceData.nodes[4]?.detail?.workOrderList || []" :key="item.workNo">
                    <td>{{ item.workNo }}</td>
                    <td>{{ item.processName }}</td>
                    <td>{{ item.quantity }}</td>
                    <td><span class="table-status">{{ item.status }}</span></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

          <template v-else-if="activeNodeIndex === 5">
            <div class="detail-summary">
              <div class="summary-stat">
                <div class="stat-value">{{ traceData.nodes[5]?.detail?.taskCount || 0 }}</div>
                <div class="stat-label">检验任务</div>
              </div>
              <div class="summary-stat good">
                <div class="stat-value">{{ traceData.nodes[5]?.detail?.passCount || 0 }}</div>
                <div class="stat-label">合格项</div>
              </div>
              <div class="summary-stat bad">
                <div class="stat-value">{{ traceData.nodes[5]?.detail?.failCount || 0 }}</div>
                <div class="stat-label">不合格项</div>
              </div>
              <div class="summary-stat rate">
                <div class="stat-value">{{ traceData.nodes[5]?.detail?.passRate || 100 }}%</div>
                <div class="stat-label">合格率</div>
              </div>
            </div>
            <div class="detail-table">
              <table>
                <thead>
                  <tr>
                    <th>任务编号</th>
                    <th>状态</th>
                    <th>创建时间</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in traceData.nodes[5]?.detail?.taskList || []" :key="item.taskNo">
                    <td>{{ item.taskNo }}</td>
                    <td><span class="table-status">{{ item.status }}</span></td>
                    <td>{{ formatDateTime(item.createTime) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>

          <template v-else-if="activeNodeIndex === 6">
            <div class="detail-summary">
              <div class="summary-stat">
                <div class="stat-value">{{ traceData.nodes[6]?.detail?.stockInCount || 0 }}</div>
                <div class="stat-label">入库单数</div>
              </div>
              <div class="summary-stat">
                <div class="stat-value">{{ traceData.nodes[6]?.detail?.totalInQty || 0 }}</div>
                <div class="stat-label">已入库数量</div>
              </div>
              <div class="summary-stat rate">
                <div class="stat-value">{{ traceData.nodes[6]?.detail?.progress || 0 }}%</div>
                <div class="stat-label">入库完成率</div>
              </div>
            </div>
            <div class="detail-table">
              <table>
                <thead>
                  <tr>
                    <th>入库单号</th>
                    <th>物料/成品</th>
                    <th>仓库</th>
                    <th>数量</th>
                    <th>创建时间</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in traceData.nodes[6]?.detail?.stockInList || []" :key="item.inNo">
                    <td>{{ item.inNo }}</td>
                    <td>{{ item.materialName }}</td>
                    <td>{{ item.warehouseName }}</td>
                    <td>{{ item.quantity }}</td>
                    <td>{{ formatDateTime(item.createTime) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { traceOrder, getOrderList } from '../../api/trace'

const router = useRouter()
const loading = ref(false)

const searchOrderNo = ref('')
const showSuggestions = ref(false)
const suggestionList = ref([])
let suggestionTimer = null

const activeNodeIndex = ref(null)

const currentDate = ref('')
const currentTime = ref('')
let timeTimer = null

const traceData = ref({
  orderInfo: null,
  nodes: [],
  overallProgress: 0
})

function updateTime() {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const week = ['日', '一', '二', '三', '四', '五', '六'][now.getDay()]
  currentDate.value = `${y}年${m}月${d}日 星期${week}`
  const h = String(now.getHours()).padStart(2, '0')
  const min = String(now.getMinutes()).padStart(2, '0')
  const s = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${h}:${min}:${s}`
}

function goBack() {
  router.push('/dashboard')
}

function getNodeIcon(icon) {
  const icons = {
    'document': '📋',
    'shopping-cart': '🛒',
    'check-circle': '✅',
    'bottom': '📥',
    'tickets': '🎫',
    'quality': '🔍',
    'warehouse': '🏭'
  }
  return icons[icon] || '📌'
}

function getStatusText(status) {
  const texts = {
    'completed': '已完成',
    'inProgress': '进行中',
    'pending': '待开始',
    'abnormal': '异常'
  }
  return texts[status] || '未知'
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function formatAmount(amount) {
  if (!amount) return '0.00'
  return Number(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function handleSearchInput() {
  if (suggestionTimer) clearTimeout(suggestionTimer)
  suggestionTimer = setTimeout(async () => {
    if (searchOrderNo.value.length > 0) {
      try {
        const res = await getOrderList(searchOrderNo.value)
        if (res && res.data) {
          suggestionList.value = res.data
        }
      } catch (e) {
        console.log(e)
      }
    } else {
      suggestionList.value = []
    }
  }, 300)
}

function selectSuggestion(orderNo) {
  searchOrderNo.value = orderNo
  showSuggestions.value = false
  handleSearch()
}

function handleBlur() {
  setTimeout(() => {
    showSuggestions.value = false
  }, 200)
}

async function handleSearch() {
  if (!searchOrderNo.value.trim()) {
    return
  }
  showSuggestions.value = false
  loading.value = true
  activeNodeIndex.value = null
  try {
    const res = await traceOrder(searchOrderNo.value.trim())
    if (res && res.data) {
      traceData.value = res.data
    }
  } catch (e) {
    console.log('追溯查询异常', e)
    useDefaultData()
  } finally {
    loading.value = false
  }
}

function toggleNodeDetail(index) {
  if (activeNodeIndex.value === index) {
    activeNodeIndex.value = null
  } else {
    activeNodeIndex.value = index
  }
}

function useDefaultData() {
  traceData.value = {
    orderInfo: {
      orderNo: searchOrderNo.value || 'SO202501001',
      customerName: '华为技术有限公司',
      productName: '智能手环X1',
      productSpec: '黑色/标准版',
      quantity: 5000,
      totalAmount: 1495000.00,
      status: 2,
      statusText: '生产中',
      deliveryDate: '2025-01-20T00:00:00',
      createTime: '2025-01-05T09:00:00'
    },
    overallProgress: 65,
    nodes: [
      { index: 1, name: '订单录入', nameEn: 'ORDER ENTRY', status: 'completed', time: '2025-01-05T09:00:00', icon: 'document', detail: {} },
      { index: 2, name: '采购下单', nameEn: 'PURCHASE ORDER', status: 'completed', time: '2025-01-06T10:00:00', icon: 'shopping-cart', detail: {
        purchaseCount: 5, totalQuantity: 25000,
        purchaseList: [
          { purchaseNo: 'PO202501001', materialName: '手环主板', supplierName: '深圳鑫达电子', quantity: 10000, status: '已完成', createTime: '2025-01-06T09:00:00' },
          { purchaseNo: 'PO202501002', materialName: 'OLED屏幕', supplierName: '上海芯片技术', quantity: 8000, status: '已到货', createTime: '2025-01-05T14:00:00' }
        ]
      }},
      { index: 3, name: '来料检验', nameEn: 'IQC INSPECTION', status: 'completed', time: '2025-01-08T09:00:00', icon: 'check-circle', detail: {
        taskCount: 5, passCount: 28, failCount: 2, passRate: 93,
        taskList: [
          { taskNo: 'QT20250108001', status: '已完成', createTime: '2025-01-08T09:00:00' },
          { taskNo: 'QT20250108002', status: '已完成', createTime: '2025-01-08T10:30:00' }
        ]
      }},
      { index: 4, name: '物料入库', nameEn: 'MATERIAL STOCK-IN', status: 'completed', time: '2025-01-09T10:00:00', icon: 'bottom', detail: {
        stockInCount: 8, totalQuantity: 18500,
        stockInList: [
          { inNo: 'IN20250102001', materialName: '手环主板', warehouseName: '主原料仓', quantity: 10000, sourceType: '采购', createTime: '2025-01-08T10:00:00' },
          { inNo: 'IN20250103001', materialName: '硅胶表带', warehouseName: '主原料仓', quantity: 50000, sourceType: '采购', createTime: '2025-01-06T14:00:00' }
        ]
      }},
      { index: 5, name: '生产执行', nameEn: 'PRODUCTION EXECUTION', status: 'inProgress', time: '2025-01-10T08:00:00', icon: 'tickets', detail: {
        planCount: 1, workOrderCount: 5, completedQty: 3250, totalQty: 5000, progress: 65,
        processProgress: [
          { processName: '主板贴片', goodQty: 4980, badQty: 20, progress: 100 },
          { processName: '屏幕组装', goodQty: 4950, badQty: 30, progress: 99 },
          { processName: '外壳组装', goodQty: 4200, badQty: 50, progress: 84 },
          { processName: '功能测试', goodQty: 3500, badQty: 20, progress: 70 },
          { processName: '包装入库', goodQty: 2800, badQty: 10, progress: 56 }
        ],
        workOrderList: [
          { workNo: 'WO202501080001', processName: '主板贴片', quantity: 5000, status: '已完成' },
          { workNo: 'WO202501080002', processName: '屏幕组装', quantity: 5000, status: '已完成' },
          { workNo: 'WO202501080003', processName: '外壳组装', quantity: 5000, status: '执行中' }
        ]
      }},
      { index: 6, name: '成品质检', nameEn: 'OQC INSPECTION', status: 'inProgress', time: '2025-01-14T10:00:00', icon: 'quality', detail: {
        taskCount: 2, passCount: 15, failCount: 1, passRate: 94,
        taskList: [
          { taskNo: 'QT20250115001', status: '已完成', createTime: '2025-01-15T10:00:00' },
          { taskNo: 'QT20250118001', status: '处理中', createTime: '2025-01-18T14:00:00' }
        ]
      }},
      { index: 7, name: '成品入库', nameEn: 'FINISHED STOCK-IN', status: 'pending', time: null, icon: 'warehouse', detail: {
        stockInCount: 0, totalInQty: 0, orderQty: 5000, progress: 0,
        stockInList: []
      }}
    ]
  }
}

onMounted(() => {
  updateTime()
  timeTimer = setInterval(updateTime, 1000)

  // 默认加载演示数据
  searchOrderNo.value = 'SO202501001'
  useDefaultData()
})

onUnmounted(() => {
  if (timeTimer) clearInterval(timeTimer)
  if (suggestionTimer) clearTimeout(suggestionTimer)
})
</script>

<style scoped>
.trace-screen {
  width: 100vw;
  height: 100vh;
  background: radial-gradient(ellipse at center, #061a36 0%, #030a1a 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: 'Microsoft YaHei', sans-serif;
  color: #e2e8f0;
}

/* 顶部标题栏 */
.screen-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  background: linear-gradient(180deg, rgba(14,165,233,0.15) 0%, transparent 100%);
  border-bottom: 1px solid rgba(14,165,233,0.2);
  flex-shrink: 0;
}

.header-left, .header-right {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-right {
  justify-content: flex-end;
}

.header-date {
  font-size: 14px;
  color: #64748b;
}

.header-time {
  font-size: 22px;
  font-weight: 700;
  color: #0ea5e9;
  font-family: 'Courier New', monospace;
  letter-spacing: 2px;
}

.header-center {
  flex: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.screen-title {
  font-size: 26px;
  font-weight: 700;
  color: #f1f5f9;
  margin: 0;
  letter-spacing: 4px;
  text-shadow: 0 0 20px rgba(14,165,233,0.5);
  background: linear-gradient(90deg, #38bdf8, #818cf8, #38bdf8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-decoration {
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #0ea5e9);
}

.title-decoration.right {
  background: linear-gradient(90deg, #0ea5e9, transparent);
}

.back-btn {
  background: rgba(14,165,233,0.15);
  border: 1px solid rgba(14,165,233,0.3);
  color: #38bdf8;
  padding: 6px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.back-btn:hover {
  background: rgba(14,165,233,0.3);
  color: #fff;
}

/* 查询区域 */
.search-section {
  padding: 20px 40px;
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}

.search-box {
  display: flex;
  align-items: center;
  background: rgba(14,165,233,0.08);
  border: 1px solid rgba(14,165,233,0.2);
  border-radius: 8px;
  padding: 4px 4px 4px 16px;
  width: 600px;
  position: relative;
  transition: all 0.3s;
}

.search-box:focus-within {
  border-color: rgba(14,165,233,0.5);
  box-shadow: 0 0 20px rgba(14,165,233,0.15);
}

.search-icon {
  font-size: 18px;
  margin-right: 10px;
}

.search-input {
  flex: 1;
  height: 40px;
  background: transparent;
  border: none;
  outline: none;
  color: #e2e8f0;
  font-size: 15px;
}

.search-input::placeholder {
  color: #475569;
}

.search-btn {
  background: linear-gradient(135deg, #0ea5e9, #0369a1);
  color: #fff;
  border: none;
  padding: 0 24px;
  height: 40px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s;
}

.search-btn:hover {
  box-shadow: 0 0 15px rgba(14,165,233,0.4);
  transform: translateY(-1px);
}

.suggestion-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  background: rgba(3,10,26,0.98);
  border: 1px solid rgba(14,165,233,0.2);
  border-radius: 8px;
  max-height: 300px;
  overflow-y: auto;
  z-index: 100;
}

.suggestion-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  cursor: pointer;
  gap: 12px;
  border-bottom: 1px solid rgba(14,165,233,0.08);
  transition: all 0.2s;
}

.suggestion-item:hover {
  background: rgba(14,165,233,0.1);
}

.sug-order {
  color: #38bdf8;
  font-weight: 600;
  font-family: 'Courier New', monospace;
  width: 140px;
}

.sug-product {
  flex: 1;
  color: #cbd5e1;
  font-size: 13px;
}

.sug-qty {
  color: #94a3b8;
  font-size: 13px;
  width: 70px;
  text-align: right;
}

.sug-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.sug-status.status-0 { background: rgba(245,158,11,0.15); color: #f59e0b; }
.sug-status.status-1 { background: rgba(59,130,246,0.15); color: #3b82f6; }
.sug-status.status-2 { background: rgba(34,197,94,0.15); color: #22c55e; }
.sug-status.status-3 { background: rgba(14,165,233,0.15); color: #0ea5e9; }

/* 主体 */
.screen-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 30px 30px;
  overflow: hidden;
}

/* 订单信息卡片 */
.order-info-card {
  background: linear-gradient(135deg, rgba(14,165,233,0.08) 0%, rgba(3,10,26,0.6) 100%);
  border: 1px solid rgba(14,165,233,0.15);
  border-radius: 10px;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
  position: relative;
}

.order-info-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(14,165,233,0.5), transparent);
}

.order-info-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-no-label {
  font-size: 12px;
  color: #64748b;
}

.order-no-value {
  font-size: 24px;
  font-weight: 700;
  color: #38bdf8;
  font-family: 'Courier New', monospace;
  letter-spacing: 1px;
}

.order-info-divider {
  width: 1px;
  height: 50px;
  background: rgba(14,165,233,0.2);
}

.order-info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 100px;
}

.info-label {
  font-size: 12px;
  color: #64748b;
}

.info-value {
  font-size: 15px;
  color: #e2e8f0;
  font-weight: 500;
}

.info-value.highlight {
  color: #f59e0b;
  font-weight: 600;
}

.order-status {
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
}

.order-status-0 { background: rgba(245,158,11,0.15); color: #f59e0b; }
.order-status-1 { background: rgba(59,130,246,0.15); color: #3b82f6; }
.order-status-2 { background: rgba(34,197,94,0.15); color: #22c55e; }
.order-status-3 { background: rgba(14,165,233,0.15); color: #0ea5e9; }
.order-status-4 { background: rgba(100,116,139,0.15); color: #64748b; }

.order-progress {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-label {
  font-size: 13px;
  color: #64748b;
}

.progress-bar-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-bar-bg {
  width: 150px;
  height: 10px;
  background: rgba(14,165,233,0.1);
  border-radius: 5px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #0369a1, #0ea5e9, #38bdf8);
  border-radius: 5px;
  transition: width 1s ease;
  position: relative;
}

.progress-bar-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.progress-text {
  font-size: 18px;
  font-weight: 700;
  color: #38bdf8;
  font-family: 'Courier New', monospace;
  min-width: 50px;
}

/* 链路流程图 */
.trace-flow {
  flex: 1;
  background: linear-gradient(135deg, rgba(14,165,233,0.06) 0%, rgba(3,10,26,0.5) 100%);
  border: 1px solid rgba(14,165,233,0.15);
  border-radius: 10px;
  padding: 30px 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  min-height: 200px;
}

.trace-flow::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(14,165,233,0.5), transparent);
}

.flow-container {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  width: 100%;
  max-width: 1400px;
  position: relative;
}

.flow-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  cursor: pointer;
  transition: all 0.3s;
  flex: 1;
  z-index: 2;
}

.flow-node:hover {
  transform: translateY(-4px);
}

.flow-node.active .node-circle {
  box-shadow: 0 0 25px rgba(14,165,233,0.6);
  transform: scale(1.1);
}

.node-circle {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1e3a5f, #0f172a);
  border: 3px solid #334155;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.3s;
}

.node-icon {
  font-size: 28px;
}

.node-index {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #334155;
  color: #94a3b8;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 已完成状态 */
.node-completed .node-circle {
  border-color: #22c55e;
  background: linear-gradient(135deg, #064e3b, #0f172a);
  box-shadow: 0 0 15px rgba(34,197,94,0.3);
}

.node-completed .node-index {
  background: #22c55e;
  color: #fff;
}

.node-completed .node-name {
  color: #22c55e;
}

/* 进行中状态 */
.node-inProgress .node-circle {
  border-color: #f59e0b;
  background: linear-gradient(135deg, #78350f, #0f172a);
  box-shadow: 0 0 15px rgba(245,158,11,0.3);
  animation: pulse-yellow 2s infinite;
}

.node-inProgress .node-index {
  background: #f59e0b;
  color: #fff;
}

.node-inProgress .node-name {
  color: #f59e0b;
}

@keyframes pulse-yellow {
  0%, 100% { box-shadow: 0 0 15px rgba(245,158,11,0.3); }
  50% { box-shadow: 0 0 25px rgba(245,158,11,0.5); }
}

/* 异常状态 */
.node-abnormal .node-circle {
  border-color: #ef4444;
  background: linear-gradient(135deg, #7f1d1d, #0f172a);
  box-shadow: 0 0 15px rgba(239,68,68,0.3);
  animation: pulse-red 1.5s infinite;
}

.node-abnormal .node-index {
  background: #ef4444;
  color: #fff;
}

.node-abnormal .node-name {
  color: #ef4444;
}

@keyframes pulse-red {
  0%, 100% { box-shadow: 0 0 15px rgba(239,68,68,0.3); }
  50% { box-shadow: 0 0 25px rgba(239,68,68,0.6); }
}

/* 待开始状态 */
.node-pending .node-circle {
  border-color: #475569;
  opacity: 0.6;
}

.node-pending .node-name {
  color: #64748b;
}

.node-info {
  margin-top: 12px;
  text-align: center;
}

.node-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 2px;
  transition: color 0.3s;
}

.node-name-en {
  font-size: 10px;
  color: #475569;
  letter-spacing: 1px;
  margin-bottom: 4px;
}

.node-time {
  font-size: 11px;
  color: #64748b;
  font-family: 'Courier New', monospace;
}

.node-status-tag {
  margin-top: 8px;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.node-status-tag.status-completed {
  background: rgba(34,197,94,0.15);
  color: #22c55e;
}

.node-status-tag.status-inProgress {
  background: rgba(245,158,11,0.15);
  color: #f59e0b;
}

.node-status-tag.status-pending {
  background: rgba(100,116,139,0.15);
  color: #64748b;
}

.node-status-tag.status-abnormal {
  background: rgba(239,68,68,0.15);
  color: #ef4444;
}

/* 连接线 */
.node-connector {
  position: absolute;
  top: 35px;
  left: 50%;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.connector-line {
  flex: 1;
  height: 3px;
  background: linear-gradient(90deg, #334155, #475569);
  margin: 0 10px;
  border-radius: 2px;
}

.node-completed + .flow-node .connector-line,
.node-completed .connector-line {
  background: linear-gradient(90deg, #22c55e, #16a34a);
}

.connector-arrow {
  font-size: 10px;
  color: #475569;
}

.node-completed .connector-arrow {
  color: #22c55e;
}

/* 详情面板 */
.detail-panel {
  background: linear-gradient(135deg, rgba(14,165,233,0.06) 0%, rgba(3,10,26,0.6) 100%);
  border: 1px solid rgba(14,165,233,0.2);
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  max-height: 320px;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: rgba(14,165,233,0.1);
  border-bottom: 1px solid rgba(14,165,233,0.15);
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
  color: #e2e8f0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-icon {
  font-size: 20px;
}

.detail-close {
  background: transparent;
  border: none;
  color: #64748b;
  font-size: 18px;
  cursor: pointer;
  width: 28px;
  height: 28px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.detail-close:hover {
  background: rgba(239,68,68,0.15);
  color: #f87171;
}

.detail-body {
  flex: 1;
  padding: 16px 20px;
  overflow-y: auto;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 16px;
  background: rgba(14,165,233,0.05);
  border-radius: 8px;
  border: 1px solid rgba(14,165,233,0.1);
}

.detail-label {
  font-size: 12px;
  color: #64748b;
}

.detail-value {
  font-size: 15px;
  color: #e2e8f0;
  font-weight: 500;
}

.detail-summary {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
}

.summary-stat {
  flex: 1;
  text-align: center;
  padding: 12px;
  background: rgba(14,165,233,0.05);
  border-radius: 8px;
  border: 1px solid rgba(14,165,233,0.1);
}

.summary-stat.good .stat-value { color: #22c55e; }
.summary-stat.bad .stat-value { color: #ef4444; }
.summary-stat.rate .stat-value { color: #38bdf8; }

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #f1f5f9;
  font-family: 'Courier New', monospace;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.detail-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #cbd5e1;
  margin: 16px 0 10px;
  padding-left: 10px;
  border-left: 3px solid #0ea5e9;
}

.process-progress-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.process-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.process-name {
  width: 100px;
  font-size: 13px;
  color: #cbd5e1;
  flex-shrink: 0;
}

.process-bar-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
}

.process-bar-bg {
  flex: 1;
  height: 8px;
  background: rgba(14,165,233,0.1);
  border-radius: 4px;
  overflow: hidden;
}

.process-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #0369a1, #0ea5e9, #38bdf8);
  border-radius: 4px;
  transition: width 0.8s ease;
}

.process-percent {
  width: 45px;
  text-align: right;
  font-size: 13px;
  font-weight: 600;
  color: #38bdf8;
  font-family: 'Courier New', monospace;
}

.process-qty {
  width: 150px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  font-size: 12px;
}

.process-qty .good { color: #22c55e; }
.process-qty .bad { color: #ef4444; }

.detail-table {
  overflow-x: auto;
}

.detail-table table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.detail-table th {
  background: rgba(14,165,233,0.1);
  color: #94a3b8;
  font-weight: 600;
  padding: 10px 12px;
  text-align: left;
  border-bottom: 1px solid rgba(14,165,233,0.15);
}

.detail-table td {
  padding: 10px 12px;
  color: #cbd5e1;
  border-bottom: 1px solid rgba(14,165,233,0.06);
}

.detail-table tr:hover td {
  background: rgba(14,165,233,0.05);
}

.table-status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  background: rgba(14,165,233,0.15);
  color: #38bdf8;
}
</style>
