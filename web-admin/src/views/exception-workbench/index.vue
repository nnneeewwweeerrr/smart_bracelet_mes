<template>
  <div class="page-container">
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

    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="异常单号">
          <el-input v-model="searchForm.exceptionNo" placeholder="请输入异常单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="工单号">
          <el-input v-model="searchForm.workOrderNo" placeholder="请输入工单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="异常类型">
          <el-select v-model="searchForm.exceptionType" placeholder="全部类型" clearable style="width: 140px">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <span class="table-title">异常记录列表</span>
        <div class="header-btns">
          <el-button type="primary" plain @click="handleAskAi">
            <el-icon><ChatDotRound /></el-icon>
            问AI
          </el-button>
          <el-button type="danger" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            上报异常
          </el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="exceptionNo" label="异常单号" width="160" />
        <el-table-column prop="workOrderNo" label="工单号" width="150" />
        <el-table-column prop="exceptionType" label="异常类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.exceptionType)" size="small">
              {{ getTypeText(row.exceptionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="exceptionDesc" label="异常描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="处理状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reporter" label="上报人" width="100" align="center" />
        <el-table-column prop="handler" label="处理人" width="100" align="center">
          <template #default="{ row }">
            {{ row.handler || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上报时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button
              type="warning"
              link
              @click="handleProcess(row)"
              :disabled="row.status === 2 || row.status === 3"
            >
              处理
            </el-button>
            <el-button
              type="success"
              link
              @click="handleClose(row)"
              :disabled="row.status !== 1"
            >
              关闭
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="detailVisible"
      title="异常详情"
      width="700px"
    >
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="异常单号">{{ currentException.exceptionNo }}</el-descriptions-item>
        <el-descriptions-item label="异常类型">
          <el-tag :type="getTypeTagType(currentException.exceptionType)" size="small">
            {{ getTypeText(currentException.exceptionType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="工单号">{{ currentException.workOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ currentException.productName }}</el-descriptions-item>
        <el-descriptions-item label="上报人">{{ currentException.reporter }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ currentException.handler || '-' }}</el-descriptions-item>
        <el-descriptions-item label="上报时间" :span="2">{{ currentException.createTime }}</el-descriptions-item>
        <el-descriptions-item label="异常描述" :span="2">{{ currentException.exceptionDesc }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="currentException.processRecords && currentException.processRecords.length > 0" class="process-section">
        <div class="section-title">
          <el-icon><List /></el-icon>
          处理记录
        </div>
        <el-timeline>
          <el-timeline-item
            v-for="(record, index) in currentException.processRecords"
            :key="index"
            :timestamp="record.time"
            :type="index === 0 ? 'primary' : ''"
          >
            <div class="timeline-content">
              <div class="timeline-title">{{ record.action }}</div>
              <div class="timeline-person">操作人：{{ record.person }}</div>
              <div v-if="record.remark" class="timeline-remark">{{ record.remark }}</div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="processVisible"
      title="处理异常"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="processFormRef" :model="processForm" :rules="processRules" label-width="100px">
        <el-form-item label="处理方式" prop="action">
          <el-radio-group v-model="processForm.action">
            <el-radio value="处理中">处理中</el-radio>
            <el-radio value="已解决">已解决</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理人" prop="handler">
          <el-input v-model="processForm.handler" placeholder="请输入处理人" />
        </el-form-item>
        <el-form-item label="处理备注" prop="remark">
          <el-input v-model="processForm.remark" type="textarea" :rows="4" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitProcess">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="addVisible"
      title="上报异常"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="120px">
        <el-form-item label="工单号" prop="workOrderId">
          <el-select v-model="addForm.workOrderId" placeholder="请选择工单" style="width: 100%" @change="handleWorkOrderChange">
            <el-option
              v-for="item in workOrderList"
              :key="item.id"
              :label="item.workOrderNo"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="addForm.productName" disabled />
        </el-form-item>
        <el-form-item label="异常类型" prop="exceptionType">
          <el-select v-model="addForm.exceptionType" placeholder="请选择异常类型" style="width: 100%">
            <el-option label="设备故障" :value="1" />
            <el-option label="质量异常" :value="2" />
            <el-option label="物料短缺" :value="3" />
            <el-option label="工艺问题" :value="4" />
            <el-option label="人员异常" :value="5" />
            <el-option label="其他" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="上报人" prop="reporter">
          <el-input v-model="addForm.reporter" placeholder="请输入上报人" />
        </el-form-item>
        <el-form-item label="异常描述" prop="exceptionDesc">
          <el-input v-model="addForm.exceptionDesc" type="textarea" :rows="4" placeholder="请详细描述异常情况" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitAdd">提交上报</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const detailVisible = ref(false)
const processVisible = ref(false)
const addVisible = ref(false)
const processFormRef = ref(null)
const addFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  exceptionNo: '',
  workOrderNo: '',
  exceptionType: '',
  status: ''
})

const currentException = reactive({})

const processForm = reactive({
  id: null,
  action: '处理中',
  handler: '',
  remark: ''
})

const addForm = reactive({
  workOrderId: null,
  workOrderNo: '',
  productName: '',
  exceptionType: null,
  reporter: '',
  exceptionDesc: ''
})

const typeOptions = [
  { label: '设备故障', value: 1 },
  { label: '质量异常', value: 2 },
  { label: '物料短缺', value: 3 },
  { label: '工艺问题', value: 4 },
  { label: '人员异常', value: 5 },
  { label: '其他', value: 6 }
]

const statusOptions = [
  { label: '待处理', value: 0 },
  { label: '处理中', value: 1 },
  { label: '已解决', value: 2 },
  { label: '已关闭', value: 3 }
]

const processRules = {
  action: [
    { required: true, message: '请选择处理方式', trigger: 'change' }
  ],
  handler: [
    { required: true, message: '请输入处理人', trigger: 'blur' }
  ]
}

const addRules = {
  workOrderId: [
    { required: true, message: '请选择工单', trigger: 'change' }
  ],
  exceptionType: [
    { required: true, message: '请选择异常类型', trigger: 'change' }
  ],
  reporter: [
    { required: true, message: '请输入上报人', trigger: 'blur' }
  ],
  exceptionDesc: [
    { required: true, message: '请输入异常描述', trigger: 'blur' }
  ]
}

const workOrderList = ref([
  { id: 1, workOrderNo: 'WO-2024001', productName: '智能健康手环 Pro' },
  { id: 2, workOrderNo: 'WO-2024002', productName: '智能运动手环 Lite' },
  { id: 3, workOrderNo: 'WO-2024003', productName: '智能健康手环 Pro' },
  { id: 4, workOrderNo: 'WO-2024004', productName: '智能运动手环 Lite' },
  { id: 6, workOrderNo: 'WO-2024006', productName: '智能健康手环 Pro' }
])

const statCards = ref([
  { title: '待处理', value: 3, icon: 'Warning', color: '#F56C6C' },
  { title: '处理中', value: 5, icon: 'Loading', color: '#E6A23C' },
  { title: '已解决', value: 12, icon: 'CircleCheck', color: '#67C23A' },
  { title: '本月异常', value: 20, icon: 'Document', color: '#409EFF' }
])

const mockData = ref([
  {
    id: 1,
    exceptionNo: 'EX-20240115001',
    workOrderId: 1,
    workOrderNo: 'WO-2024001',
    productName: '智能健康手环 Pro',
    exceptionType: 1,
    exceptionDesc: 'SMT贴片机01出现告警，贴片精度下降，需要维修',
    status: 1,
    reporter: '张操作员',
    handler: '李工程师',
    createTime: '2024-01-15 09:30:00',
    processRecords: [
      { action: '异常上报', person: '张操作员', time: '2024-01-15 09:30:00', remark: 'SMT贴片机01出现告警' },
      { action: '开始处理', person: '李工程师', time: '2024-01-15 10:00:00', remark: '已到达现场，正在检查设备' }
    ]
  },
  {
    id: 2,
    exceptionNo: 'EX-20240115002',
    workOrderId: 1,
    workOrderNo: 'WO-2024001',
    productName: '智能健康手环 Pro',
    exceptionType: 2,
    exceptionDesc: '组装工序发现外观不良率上升，达到5%，需要排查原因',
    status: 0,
    reporter: '王质检',
    handler: '',
    createTime: '2024-01-15 14:20:00',
    processRecords: [
      { action: '异常上报', person: '王质检', time: '2024-01-15 14:20:00', remark: '外观不良率5%，超标' }
    ]
  },
  {
    id: 3,
    exceptionNo: 'EX-20240114001',
    workOrderId: 4,
    workOrderNo: 'WO-2024004',
    productName: '智能运动手环 Lite',
    exceptionType: 3,
    exceptionDesc: '电池物料库存不足，预计下午会缺料停产',
    status: 1,
    reporter: '赵仓管',
    handler: '刘采购',
    createTime: '2024-01-14 10:00:00',
    processRecords: [
      { action: '异常上报', person: '赵仓管', time: '2024-01-14 10:00:00', remark: '电池库存仅剩500pcs' },
      { action: '开始处理', person: '刘采购', time: '2024-01-14 10:30:00', remark: '已联系供应商紧急补货' }
    ]
  },
  {
    id: 4,
    exceptionNo: 'EX-20240114002',
    workOrderId: 6,
    workOrderNo: 'WO-2024006',
    productName: '智能健康手环 Pro',
    exceptionType: 4,
    exceptionDesc: '老化测试工序参数需要调整，当前工艺参数不达标',
    status: 2,
    reporter: '张工程师',
    handler: '李工艺师',
    createTime: '2024-01-14 11:30:00',
    processRecords: [
      { action: '异常上报', person: '张工程师', time: '2024-01-14 11:30:00', remark: '老化测试参数需优化' },
      { action: '开始处理', person: '李工艺师', time: '2024-01-14 13:00:00', remark: '正在调整工艺参数' },
      { action: '问题解决', person: '李工艺师', time: '2024-01-14 16:00:00', remark: '参数已优化，测试通过' }
    ]
  },
  {
    id: 5,
    exceptionNo: 'EX-20240113001',
    workOrderId: 2,
    workOrderNo: 'WO-2024002',
    productName: '智能运动手环 Lite',
    exceptionType: 5,
    exceptionDesc: '组装线工人请假，人手不足，可能影响产能',
    status: 3,
    reporter: '王主管',
    handler: '李主管',
    createTime: '2024-01-13 08:00:00',
    processRecords: [
      { action: '异常上报', person: '王主管', time: '2024-01-13 08:00:00', remark: '2人请假' },
      { action: '开始处理', person: '李主管', time: '2024-01-13 08:30:00', remark: '已安排其他线支援' },
      { action: '问题解决', person: '李主管', time: '2024-01-13 10:00:00', remark: '人员已到位，产能恢复' },
      { action: '关闭异常', person: '王主管', time: '2024-01-13 18:00:00', remark: '当日产能达标' }
    ]
  },
  {
    id: 6,
    exceptionNo: 'EX-20240113002',
    workOrderId: 1,
    workOrderNo: 'WO-2024001',
    productName: '智能健康手环 Pro',
    exceptionType: 2,
    exceptionDesc: '测试工序发现功能异常，屏幕不亮',
    status: 2,
    reporter: '李质检',
    handler: '张工程师',
    createTime: '2024-01-13 15:00:00',
    processRecords: [
      { action: '异常上报', person: '李质检', time: '2024-01-13 15:00:00', remark: '3台屏幕不亮' },
      { action: '开始处理', person: '张工程师', time: '2024-01-13 15:30:00', remark: '正在排查原因' },
      { action: '问题解决', person: '张工程师', time: '2024-01-13 17:00:00', remark: 'FPC接触不良，已优化组装工艺' }
    ]
  }
])

const tableData = ref([])

function getTypeText(type) {
  const texts = ['', '设备故障', '质量异常', '物料短缺', '工艺问题', '人员异常', '其他']
  return texts[type] || '未知'
}

function getTypeTagType(type) {
  const types = ['', 'danger', 'warning', 'info', 'primary', 'warning', 'info']
  return types[type] || 'info'
}

function getStatusText(status) {
  const texts = ['待处理', '处理中', '已解决', '已关闭']
  return texts[status] || '未知'
}

function getStatusTagType(status) {
  const types = ['danger', 'warning', 'success', 'info']
  return types[status] || 'info'
}

onMounted(() => {
  fetchData()
})

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filtered = [...mockData.value]
    if (searchForm.exceptionNo) {
      filtered = filtered.filter(item => item.exceptionNo.includes(searchForm.exceptionNo))
    }
    if (searchForm.workOrderNo) {
      filtered = filtered.filter(item => item.workOrderNo.includes(searchForm.workOrderNo))
    }
    if (searchForm.exceptionType !== '') {
      filtered = filtered.filter(item => item.exceptionType === searchForm.exceptionType)
    }
    if (searchForm.status !== '') {
      filtered = filtered.filter(item => item.status === searchForm.status)
    }
    total.value = filtered.length
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    tableData.value = filtered.slice(start, end)
    loading.value = false
  }, 300)
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.exceptionNo = ''
  searchForm.workOrderNo = ''
  searchForm.exceptionType = ''
  searchForm.status = ''
  currentPage.value = 1
  fetchData()
}

function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  fetchData()
}

function handleCurrentChange(page) {
  currentPage.value = page
  fetchData()
}

function handleView(row) {
  Object.assign(currentException, row)
  detailVisible.value = true
}

function handleAskAi() {
  let question
  if (currentException.exceptionNo) {
    const typeText = getTypeText(currentException.exceptionType)
    const statusText = getStatusText(currentException.status)
    question = `生产异常「${currentException.exceptionNo}」需要处理方案。异常类型为「${typeText}」，当前状态「${statusText}」，关联工单「${currentException.workOrderNo}」，产品「${currentException.productName}」。异常描述：${currentException.exceptionDesc}。请给出针对该异常的处理方案和建议。`
  } else {
    question = '请介绍生产过程中常见异常（设备故障、质量异常、物料短缺、工艺问题等）的标准处理方案和预防措施。'
  }
  window.dispatchEvent(new CustomEvent('ask-ai', { detail: { question } }))
}

function handleProcess(row) {
  processForm.id = row.id
  processForm.action = row.status === 0 ? '处理中' : '已解决'
  processForm.handler = row.handler || ''
  processForm.remark = ''
  processVisible.value = true
}

function handleClose(row) {
  ElMessageBox.confirm(`确定要关闭异常「${row.exceptionNo}」吗？`, '提示', {
    confirmButtonText: '确定关闭',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const item = mockData.value.find(i => i.id === row.id)
    if (item) {
      item.status = 3
      if (!item.processRecords) item.processRecords = []
      item.processRecords.unshift({
        action: '关闭异常',
        person: '当前用户',
        time: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
        remark: '异常已关闭'
      })
    }
    ElMessage.success('关闭成功')
    fetchData()
  }).catch(() => {})
}

function handleAdd() {
  addForm.workOrderId = null
  addForm.workOrderNo = ''
  addForm.productName = ''
  addForm.exceptionType = null
  addForm.reporter = ''
  addForm.exceptionDesc = ''
  if (addFormRef.value) {
    addFormRef.value.resetFields()
  }
  addVisible.value = true
}

function handleWorkOrderChange(val) {
  const order = workOrderList.value.find(o => o.id === val)
  if (order) {
    addForm.workOrderNo = order.workOrderNo
    addForm.productName = order.productName
  }
}

async function handleSubmitProcess() {
  if (!processFormRef.value) return
  
  try {
    await processFormRef.value.validate()
    submitLoading.value = true
    
    setTimeout(() => {
      const item = mockData.value.find(i => i.id === processForm.id)
      if (item) {
        item.handler = processForm.handler
        item.status = processForm.action === '已解决' ? 2 : 1
        if (!item.processRecords) item.processRecords = []
        item.processRecords.unshift({
          action: processForm.action === '已解决' ? '问题解决' : '开始处理',
          person: processForm.handler,
          time: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
          remark: processForm.remark
        })
      }
      ElMessage.success('处理成功')
      processVisible.value = false
      submitLoading.value = false
      fetchData()
    }, 300)
  } catch (error) {
    submitLoading.value = false
  }
}

async function handleSubmitAdd() {
  if (!addFormRef.value) return
  
  try {
    await addFormRef.value.validate()
    submitLoading.value = true
    
    setTimeout(() => {
      const maxId = Math.max(...mockData.value.map(item => item.id), 0)
      const now = new Date()
      const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
      const newException = {
        id: maxId + 1,
        exceptionNo: 'EX-' + dateStr + String(maxId + 1).padStart(3, '0'),
        workOrderId: addForm.workOrderId,
        workOrderNo: addForm.workOrderNo,
        productName: addForm.productName,
        exceptionType: addForm.exceptionType,
        exceptionDesc: addForm.exceptionDesc,
        status: 0,
        reporter: addForm.reporter,
        handler: '',
        createTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
        processRecords: [
          {
            action: '异常上报',
            person: addForm.reporter,
            time: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
            remark: addForm.exceptionDesc
          }
        ]
      }
      mockData.value.unshift(newException)
      ElMessage.success('上报成功')
      addVisible.value = false
      submitLoading.value = false
      fetchData()
    }, 300)
  } catch (error) {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.page-container {
  width: 100%;
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

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.header-btns {
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.process-section {
  margin-top: 10px;
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.timeline-content {
  padding: 4px 0;
}

.timeline-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.timeline-person {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.timeline-remark {
  font-size: 13px;
  color: #606266;
}
</style>
