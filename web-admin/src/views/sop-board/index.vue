<template>
  <div class="sop-board">
    <!-- 顶部标题栏 -->
    <div class="screen-header">
      <div class="header-left">
        <span class="header-date">{{ currentDate }}</span>
      </div>
      <div class="header-center">
        <div class="title-decoration left"></div>
        <h1 class="screen-title">SOP电子看板</h1>
        <div class="title-decoration right"></div>
      </div>
      <div class="header-right">
        <span class="header-time">{{ currentTime }}</span>
        <button class="back-btn" @click="goBack">返回系统</button>
      </div>
    </div>

    <!-- 主体布局 -->
    <div class="screen-body" v-loading="loading" element-loading-background="rgba(3,10,26,0.8)">
      <!-- 左侧选择栏 -->
      <div class="sidebar">
        <div class="panel sidebar-panel">
          <div class="panel-title">
            <span class="title-icon"></span>
            工单/工序选择
            <span class="title-en">SELECT</span>
          </div>
          <div class="sidebar-content">
            <el-select
              v-model="workOrderId"
              placeholder="请选择工单"
              filterable
              class="dark-select"
              @change="handleWorkOrderChange"
            >
              <el-option
                v-for="item in workOrderList"
                :key="item.id"
                :label="item.workNo || item.workOrderNo"
                :value="item.id"
              />
            </el-select>

            <div class="process-list-title">工序列表</div>
            <div class="process-list">
              <div
                v-for="item in processList"
                :key="item.id"
                class="process-item"
                :class="{ active: currentProcessId === item.id }"
                @click="handleSelectProcess(item)"
              >
                <div class="process-item-index">{{ item.sortOrder || item.index }}</div>
                <div class="process-item-info">
                  <div class="process-item-name">{{ item.processName }}</div>
                  <div class="process-item-equip">{{ item.equipmentName || item.equipment || '-' }}</div>
                </div>
                <span v-if="currentProcessId === item.id" class="process-item-arrow">›</span>
              </div>
              <div v-if="processList.length === 0" class="empty-tip">暂无工序数据</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧SOP详情 -->
      <div class="main-content">
        <div v-if="!currentProcessId" class="empty-board">
          <el-icon class="empty-icon"><Document /></el-icon>
          <div class="empty-text">请从左侧选择工序查看SOP详情</div>
        </div>

        <template v-else>
          <!-- 工序基本信息卡片 -->
          <div class="panel info-panel">
            <div class="panel-title">
              <span class="title-icon"></span>
              工序基本信息
              <span class="title-en">PROCESS INFO</span>
            </div>
            <div class="panel-content info-content">
              <div class="info-grid">
                <div class="info-item">
                  <div class="info-label">工艺名称</div>
                  <div class="info-value">{{ sopInfo.processName || '-' }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">工序名称</div>
                  <div class="info-value highlight">{{ currentProcess.processName || '-' }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">SOP版本</div>
                  <div class="info-value">{{ sopInfo.version || '-' }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">设备要求</div>
                  <div class="info-value">{{ sopInfo.equipmentRequire || sopInfo.equipmentName || '-' }}</div>
                </div>
              </div>
              <div v-if="sopInfo.precautions" class="precautions-box">
                <div class="precautions-label">
                  <el-icon><Warning /></el-icon>
                  注意事项
                </div>
                <div class="precautions-text">{{ sopInfo.precautions }}</div>
              </div>
            </div>
          </div>

          <div class="detail-row">
            <!-- 操作步骤 -->
            <div class="panel step-panel">
              <div class="panel-title">
                <span class="title-icon"></span>
                操作步骤
                <span class="title-en">OPERATION STEPS</span>
              </div>
              <div class="panel-content">
                <div class="step-timeline">
                  <div
                    v-for="(step, index) in stepList"
                    :key="index"
                    class="step-node"
                  >
                    <div class="step-line" v-if="index < stepList.length - 1"></div>
                    <div class="step-dot">{{ index + 1 }}</div>
                    <div class="step-body">
                      <div class="step-content">{{ step.content || step }}</div>
                    </div>
                  </div>
                  <div v-if="stepList.length === 0" class="empty-tip">暂无操作步骤</div>
                </div>
              </div>
            </div>

            <!-- 工艺参数标准 -->
            <div class="panel param-panel">
              <div class="panel-title">
                <span class="title-icon"></span>
                工艺参数标准
                <span class="title-en">PARAM STANDARD</span>
              </div>
              <div class="panel-content">
                <div class="param-table-wrapper">
                  <table class="dark-table">
                    <thead>
                      <tr>
                        <th style="width: 50px">序号</th>
                        <th>参数名称</th>
                        <th>编码</th>
                        <th>标准范围</th>
                        <th>目标值</th>
                        <th style="width: 70px">必检</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(item, index) in paramList" :key="index">
                        <td class="center">{{ index + 1 }}</td>
                        <td>{{ item.paramName }}</td>
                        <td>{{ item.paramCode }}</td>
                        <td class="center">
                          <span class="range-text">
                            {{ item.minValue }} ~ {{ item.maxValue }}
                            <span class="unit-text" v-if="item.unit">{{ item.unit }}</span>
                          </span>
                        </td>
                        <td class="center target">{{ item.targetValue }}</td>
                        <td class="center">
                          <span class="tag" :class="item.required === 1 ? 'tag-danger' : 'tag-info'">
                            {{ item.required === 1 ? '必检' : '免检' }}
                          </span>
                        </td>
                      </tr>
                      <tr v-if="paramList.length === 0">
                        <td colspan="6" class="empty-tip">暂无参数标准</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>

          <!-- 物料BOM -->
          <div class="panel bom-panel">
            <div class="panel-title">
              <span class="title-icon"></span>
              物料BOM
              <span class="title-en">MATERIAL BOM</span>
            </div>
            <div class="panel-content">
              <div class="param-table-wrapper">
                <table class="dark-table">
                  <thead>
                    <tr>
                      <th style="width: 50px">序号</th>
                      <th>物料编码</th>
                      <th>物料名称</th>
                      <th>规格</th>
                      <th>单位</th>
                      <th>用量</th>
                      <th style="width: 70px">必检</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(item, index) in bomList" :key="index">
                      <td class="center">{{ index + 1 }}</td>
                      <td>{{ item.materialNo }}</td>
                      <td>{{ item.materialName }}</td>
                      <td>{{ item.spec || '-' }}</td>
                      <td class="center">{{ item.unit }}</td>
                      <td class="center target">{{ item.quantity }}</td>
                      <td class="center">
                        <span class="tag" :class="item.required === 1 ? 'tag-danger' : 'tag-info'">
                          {{ item.required === 1 ? '必检' : '免检' }}
                        </span>
                      </td>
                    </tr>
                    <tr v-if="bomList.length === 0">
                      <td colspan="7" class="empty-tip">暂无物料BOM</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
          <!-- 防错校验模块 -->
          <div class="panel verify-panel">
            <div class="panel-title">
              <span class="title-icon"></span>
              关键工序防错校验
              <span class="title-en">ERROR PREVENTION</span>
            </div>
            <div class="panel-content">
              <div class="verify-section">
                <div class="verify-title">
                  <el-icon><Box /></el-icon>
                  物料防错校验
                </div>
                <div class="verify-input-group">
                  <el-input
                    v-model="verifyMaterialCode"
                    placeholder="请扫描或输入物料编码"
                    class="verify-input"
                    @keyup.enter="doVerifyMaterial"
                    clearable
                  />
                  <el-button type="primary" @click="doVerifyMaterial" :disabled="!verifyMaterialCode.trim()">
                    开始校验
                  </el-button>
                </div>
                <div v-if="verifyMaterialResult.pass !== null" class="verify-result" :class="verifyMaterialResult.pass ? 'pass' : 'fail'">
                  <div class="result-icon">{{ verifyMaterialResult.pass ? '✓' : '✗' }}</div>
                  <div class="result-content">
                    <div class="result-title">{{ verifyMaterialResult.pass ? '校验通过' : '校验失败' }}</div>
                    <div class="result-detail">{{ verifyMaterialResult.reason }}</div>
                    <div v-if="verifyMaterialResult.materialName" class="result-detail">物料名称：{{ verifyMaterialResult.materialName }}</div>
                  </div>
                </div>
              </div>

              <div class="verify-divider"></div>

              <div class="verify-section">
                <div class="verify-title">
                  <el-icon><Setting /></el-icon>
                  工艺参数校验
                </div>
                <div class="param-input-grid">
                  <div v-for="param in paramList" :key="param.paramCode" class="param-input-item">
                    <div class="param-input-label">
                      {{ param.paramName }}
                      <span v-if="param.required === 1" class="required-mark">*</span>
                      <span class="param-unit">({{ param.unit || '' }})</span>
                    </div>
                    <div class="param-input-row">
                      <el-input
                        v-model="verifyParamForm[param.paramCode]"
                        :placeholder="`标准: ${param.minValue}~${param.maxValue}`"
                        class="param-input"
                      />
                      <span class="param-standard">{{ param.targetValue }} (目标)</span>
                    </div>
                  </div>
                </div>
                <div class="param-verify-actions">
                  <el-button type="primary" @click="doVerifyParams" :disabled="paramList.length === 0">
                    参数校验
                  </el-button>
                  <el-button @click="resetVerifyResults">重置</el-button>
                </div>
                <div v-if="verifyParamResult.pass !== null" class="verify-result" :class="verifyParamResult.pass ? 'pass' : 'fail'">
                  <div class="result-icon">{{ verifyParamResult.pass ? '✓' : '✗' }}</div>
                  <div class="result-content">
                    <div class="result-title">{{ verifyParamResult.pass ? '全部参数校验通过' : '参数校验未通过' }}</div>
                    <div class="result-detail">{{ verifyParamResult.reason }}</div>
                  </div>
                </div>
                <div v-if="verifyParamResult.details && verifyParamResult.details.length > 0" class="param-verify-details">
                  <div class="detail-title">校验详情</div>
                  <table class="dark-table">
                    <thead>
                      <tr>
                        <th>参数名称</th>
                        <th>标准范围</th>
                        <th>实际值</th>
                        <th>结果</th>
                        <th>说明</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(detail, i) in verifyParamResult.details" :key="i">
                        <td>{{ detail.paramName }}</td>
                        <td>{{ detail.minValue }} ~ {{ detail.maxValue }}{{ detail.unit || '' }}</td>
                        <td>{{ detail.actualValue || '-' }}</td>
                        <td>
                          <span class="tag" :class="detail.pass ? 'tag-success' : 'tag-danger'">
                            {{ detail.pass ? '合格' : '不合格' }}
                          </span>
                        </td>
                        <td class="detail-reason">{{ detail.reason }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Warning, Box, Setting } from '@element-plus/icons-vue'
import { getSopByProcessId, verifyMaterial, verifyParams } from '../../api/sop'
import { getWorkOrderList } from '../../api/workOrder'
import { getProcessList } from '../../api/process'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const workOrderId = ref(null)
const currentProcessId = ref(null)
const currentProcess = reactive({})
const workOrderList = ref([])
const processList = ref([])
const sopInfo = reactive({
  processName: '',
  version: '',
  equipmentRequire: '',
  equipmentName: '',
  precautions: ''
})
const stepList = ref([])
const paramList = ref([])
const bomList = ref([])

// 防错校验相关
const verifyMaterialCode = ref('')
const verifyMaterialResult = reactive({ pass: null, reason: '', materialName: '' })
const verifyParamForm = reactive({})
const verifyParamResult = reactive({ pass: null, reason: '', details: [] })

const currentDate = ref('')
const currentTime = ref('')
let timeTimer = null

function updateTime() {
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  const week = ['日', '一', '二', '三', '四', '五', '六'][now.getDay()]
  currentDate.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} 星期${week}`
  currentTime.value = `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

function goBack() {
  router.push('/')
}

async function loadWorkOrders() {
  try {
    const res = await getWorkOrderList({ pageNum: 1, pageSize: 1000 })
    workOrderList.value = res.data?.records || res.data || []
  } catch (e) {
    workOrderList.value = []
  }
}

async function handleWorkOrderChange(val) {
  const order = workOrderList.value.find(o => o.id === val)
  if (order && order.productId) {
    try {
      const res = await getProcessList({ productId: order.productId })
      processList.value = (res.data || []).map((p, i) => ({
        ...p,
        sortOrder: p.sortOrder || i + 1,
        index: i + 1
      }))
    } catch (e) {
      processList.value = []
    }
  } else {
    processList.value = []
  }
  currentProcessId.value = null
  resetSopDisplay()
}

function handleSelectProcess(item) {
  currentProcessId.value = item.id
  Object.assign(currentProcess, item)
  sopInfo.processName = item.processName
  sopInfo.equipmentName = item.equipmentName || item.equipment
  loadSopDetail(item.id)
}

function resetSopDisplay() {
  sopInfo.version = ''
  sopInfo.equipmentRequire = ''
  sopInfo.precautions = ''
  stepList.value = []
  paramList.value = []
  bomList.value = []
}

async function loadSopDetail(processId) {
  loading.value = true
  resetSopDisplay()
  try {
    // 后端 /sop/process/{processId} 一次返回完整数据：{process, sop, paramStandards, materialBom}
    const sopRes = await getSopByProcessId(processId)
    const data = sopRes?.data || {}
    const sopData = data.sop
    if (sopData) {
      sopInfo.version = sopData.sopVersion || ''
      sopInfo.equipmentRequire = sopData.equipmentRequirement || ''
      sopInfo.precautions = sopData.precautions || ''
      stepList.value = parseSteps(sopData.operationSteps)
    }
    // 参数标准（字段：paramName/paramCode/unit/minValue/maxValue/targetValue/isRequired）
    paramList.value = (data.paramStandards || []).map(p => ({
      ...p,
      required: p.isRequired
    }))
    // 物料BOM（字段：materialCode/materialName/unit/quantity/isRequired）
    bomList.value = (data.materialBom || []).map(b => ({
      ...b,
      materialNo: b.materialCode,
      spec: b.spec || '-',
      required: b.isRequired
    }))
  } catch (e) {
    // 接口未就绪时静默处理
  } finally {
    loading.value = false
  }
}

function parseSteps(steps) {
  if (!steps) return []
  if (Array.isArray(steps)) {
    return steps.map(s => typeof s === 'string' ? s : { content: s.content || '' })
  }
  if (typeof steps === 'string') {
    const trimmed = steps.trim()
    if (trimmed.startsWith('[')) {
      try {
        const arr = JSON.parse(trimmed)
        return arr.map(s => typeof s === 'string' ? s : { content: s.content || '' })
      } catch (e) {
      }
    }
    return steps.split('\n').filter(s => s.trim()).map(s => ({ content: s }))
  }
  return []
}

async function doVerifyMaterial() {
  if (!verifyMaterialCode.value.trim()) {
    ElMessage.warning('请输入物料编码')
    return
  }
  if (!currentProcessId.value) {
    ElMessage.warning('请先选择工序')
    return
  }
  try {
    const res = await verifyMaterial({
      materialCode: verifyMaterialCode.value.trim(),
      processId: currentProcessId.value
    })
    const data = res?.data || {}
    verifyMaterialResult.pass = data.pass
    verifyMaterialResult.reason = data.reason
    verifyMaterialResult.materialName = data.materialName || ''
  } catch (e) {
    ElMessage.error('校验失败')
  }
}

async function doVerifyParams() {
  if (!currentProcessId.value) {
    ElMessage.warning('请先选择工序')
    return
  }
  try {
    const res = await verifyParams({
      processId: currentProcessId.value,
      params: verifyParamForm
    })
    const data = res?.data || {}
    verifyParamResult.pass = data.pass
    verifyParamResult.reason = data.reason
    verifyParamResult.details = data.details || []
  } catch (e) {
    ElMessage.error('校验失败')
  }
}

function resetVerifyResults() {
  verifyMaterialResult.pass = null
  verifyMaterialResult.reason = ''
  verifyMaterialResult.materialName = ''
  verifyParamResult.pass = null
  verifyParamResult.reason = ''
  verifyParamResult.details = []
}

onMounted(async () => {
  updateTime()
  timeTimer = setInterval(updateTime, 1000)

  await loadWorkOrders()

  if (workOrderList.value.length > 0) {
    workOrderId.value = workOrderList.value[0].id
    await handleWorkOrderChange(workOrderList.value[0].id)
  }

  const processId = route.query.processId
  if (processId) {
    currentProcessId.value = Number(processId)
    currentProcess.processName = route.query.processName || '当前工序'
    loadSopDetail(Number(processId))
  }
})

onUnmounted(() => {
  if (timeTimer) clearInterval(timeTimer)
})
</script>

<style scoped>
.sop-board {
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

.header-left,
.header-right {
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

/* 主体 */
.screen-body {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 16px;
  overflow: hidden;
}

.sidebar {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
}

/* 面板通用样式 */
.panel {
  background: linear-gradient(135deg, rgba(14,165,233,0.06) 0%, rgba(3,10,26,0.5) 100%);
  border: 1px solid rgba(14,165,233,0.15);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(14,165,233,0.5), transparent);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  font-size: 15px;
  font-weight: 600;
  color: #e2e8f0;
  border-bottom: 1px solid rgba(14,165,233,0.1);
  flex-shrink: 0;
}

.title-icon {
  width: 4px;
  height: 16px;
  background: linear-gradient(180deg, #0ea5e9, #0369a1);
  border-radius: 2px;
}

.title-en {
  font-size: 10px;
  color: #475569;
  margin-left: auto;
  letter-spacing: 1px;
}

.panel-content {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 侧栏 */
.sidebar-panel {
  flex: 1;
}

.sidebar-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  overflow: hidden;
}

.process-list-title {
  font-size: 13px;
  color: #64748b;
  padding: 0 2px;
}

.process-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.process-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: rgba(14,165,233,0.05);
  border: 1px solid rgba(14,165,233,0.1);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.process-item:hover {
  background: rgba(14,165,233,0.15);
  border-color: rgba(14,165,233,0.4);
}

.process-item.active {
  background: rgba(14,165,233,0.2);
  border-color: #0ea5e9;
  box-shadow: 0 0 12px rgba(14,165,233,0.3);
}

.process-item-index {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0ea5e9, #0369a1);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

.process-item-info {
  flex: 1;
  min-width: 0;
}

.process-item-name {
  font-size: 14px;
  color: #e2e8f0;
  font-weight: 500;
}

.process-item-equip {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

.process-item-arrow {
  color: #38bdf8;
  font-size: 20px;
  font-weight: bold;
}

/* 空状态 */
.empty-board {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.empty-icon {
  font-size: 60px;
  color: rgba(14,165,233,0.3);
}

.empty-text {
  font-size: 15px;
  color: #64748b;
}

.empty-tip {
  text-align: center;
  color: #475569;
  font-size: 13px;
  padding: 24px 0;
}

/* 工序基本信息 */
.info-panel {
  flex-shrink: 0;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.info-item {
  background: rgba(14,165,233,0.05);
  border: 1px solid rgba(14,165,233,0.1);
  border-radius: 6px;
  padding: 12px 14px;
}

.info-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
}

.info-value {
  font-size: 15px;
  color: #e2e8f0;
  font-weight: 500;
  word-break: break-all;
}

.info-value.highlight {
  color: #38bdf8;
  font-size: 17px;
  text-shadow: 0 0 8px rgba(56,189,248,0.4);
}

.precautions-box {
  background: rgba(245,158,11,0.08);
  border: 1px solid rgba(245,158,11,0.25);
  border-radius: 6px;
  padding: 12px 14px;
}

.precautions-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #f59e0b;
  font-weight: 600;
  margin-bottom: 6px;
}

.precautions-text {
  font-size: 13px;
  color: #cbd5e1;
  line-height: 1.7;
  white-space: pre-wrap;
}

/* 详情行 */
.detail-row {
  display: flex;
  gap: 16px;
  flex: 1;
  min-height: 0;
}

.step-panel {
  flex: 1;
  min-width: 0;
}

.param-panel {
  flex: 1.2;
  min-width: 0;
}

/* 操作步骤时间轴 */
.step-timeline {
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  padding-right: 4px;
}

.step-node {
  position: relative;
  padding-left: 36px;
  padding-bottom: 20px;
}

.step-line {
  position: absolute;
  left: 13px;
  top: 28px;
  bottom: 0;
  width: 2px;
  background: linear-gradient(180deg, rgba(14,165,233,0.5), rgba(14,165,233,0.05));
}

.step-dot {
  position: absolute;
  left: 0;
  top: 4px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0ea5e9, #0369a1);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: bold;
  box-shadow: 0 0 10px rgba(14,165,233,0.5);
  z-index: 1;
}

.step-body {
  background: rgba(14,165,233,0.06);
  border: 1px solid rgba(14,165,233,0.12);
  border-radius: 6px;
  padding: 10px 14px;
}

.step-content {
  font-size: 14px;
  color: #e2e8f0;
  line-height: 1.6;
}

/* 深色表格 */
.param-table-wrapper {
  overflow: auto;
}

.dark-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.dark-table th {
  background: rgba(14,165,233,0.12);
  color: #38bdf8;
  font-weight: 600;
  padding: 10px 12px;
  text-align: left;
  border-bottom: 1px solid rgba(14,165,233,0.2);
  white-space: nowrap;
}

.dark-table td {
  padding: 10px 12px;
  color: #cbd5e1;
  border-bottom: 1px solid rgba(14,165,233,0.08);
}

.dark-table tbody tr:hover {
  background: rgba(14,165,233,0.06);
}

.dark-table .center {
  text-align: center;
}

.dark-table .target {
  color: #22d3ee;
  font-weight: 600;
  font-family: 'Courier New', monospace;
}

.range-text {
  color: #94a3b8;
}

.unit-text {
  color: #64748b;
  margin-left: 4px;
}

.tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
}

.tag-danger {
  background: rgba(239,68,68,0.15);
  color: #f87171;
  border: 1px solid rgba(239,68,68,0.3);
}

.tag-info {
  background: rgba(100,116,139,0.15);
  color: #94a3b8;
  border: 1px solid rgba(100,116,139,0.3);
}

.bom-panel {
  flex-shrink: 0;
}

/* 下拉框深色适配 */
:deep(.dark-select .el-input__wrapper) {
  background: rgba(14,165,233,0.08);
  box-shadow: 0 0 0 1px rgba(14,165,233,0.3) inset;
}

:deep(.dark-select .el-input__inner) {
  color: #e2e8f0;
}

:deep(.dark-select .el-input__inner::placeholder) {
  color: #64748b;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-thumb {
  background: rgba(14,165,233,0.3);
  border-radius: 3px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

/* 防错校验模块 */
.verify-panel {
  flex-shrink: 0;
}

.verify-section {
  margin-bottom: 16px;
}

.verify-section:last-child {
  margin-bottom: 0;
}

.verify-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #38bdf8;
  margin-bottom: 12px;
}

.verify-input-group {
  display: flex;
  gap: 10px;
}

.verify-input {
  flex: 1;
}

:deep(.verify-input .el-input__wrapper) {
  background: rgba(14,165,233,0.08);
  box-shadow: 0 0 0 1px rgba(14,165,233,0.3) inset;
}

:deep(.verify-input .el-input__inner) {
  color: #e2e8f0;
}

.verify-result {
  margin-top: 12px;
  padding: 12px 14px;
  border-radius: 6px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.verify-result.pass {
  background: rgba(34,197,94,0.12);
  border: 1px solid rgba(34,197,94,0.3);
}

.verify-result.fail {
  background: rgba(239,68,68,0.12);
  border: 1px solid rgba(239,68,68,0.3);
}

.result-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  flex-shrink: 0;
}

.verify-result.pass .result-icon {
  background: rgba(34,197,94,0.2);
  color: #22c55e;
}

.verify-result.fail .result-icon {
  background: rgba(239,68,68,0.2);
  color: #ef4444;
}

.result-content {
  flex: 1;
}

.result-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.verify-result.pass .result-title {
  color: #22c55e;
}

.verify-result.fail .result-title {
  color: #ef4444;
}

.result-detail {
  font-size: 13px;
  color: #94a3b8;
  margin-top: 2px;
}

.verify-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(14,165,233,0.3), transparent);
  margin: 16px 0;
}

/* 参数输入网格 */
.param-input-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}

.param-input-item {
  background: rgba(14,165,233,0.05);
  border: 1px solid rgba(14,165,233,0.1);
  border-radius: 6px;
  padding: 10px 12px;
}

.param-input-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.required-mark {
  color: #ef4444;
}

.param-unit {
  color: #475569;
}

.param-input-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.param-input {
  flex: 1;
}

:deep(.param-input .el-input__wrapper) {
  background: rgba(14,165,233,0.08);
  box-shadow: 0 0 0 1px rgba(14,165,233,0.2) inset;
}

:deep(.param-input .el-input__inner) {
  color: #e2e8f0;
  font-family: 'Courier New', monospace;
}

.param-standard {
  font-size: 12px;
  color: #22d3ee;
  font-family: 'Courier New', monospace;
  white-space: nowrap;
}

.param-verify-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.param-verify-details {
  margin-top: 12px;
}

.detail-title {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 10px;
}

.detail-reason {
  font-size: 12px;
  color: #94a3b8;
}

.tag-success {
  background: rgba(34,197,94,0.15);
  color: #22c55e;
  border: 1px solid rgba(34,197,94,0.3);
}
</style>
