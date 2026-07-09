<template>
  <div>
    <div class="stat-cards">
      <div class="stat-card pending" @click="filterByStatus(0)">
        <div class="stat-icon">
          <el-icon :size="32"><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pending }}</div>
          <div class="stat-label">待处理</div>
        </div>
      </div>
      <div class="stat-card processing" @click="filterByStatus(1)">
        <div class="stat-icon">
          <el-icon :size="32"><Loading /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.processing }}</div>
          <div class="stat-label">处理中</div>
        </div>
      </div>
      <div class="stat-card resolved" @click="filterByStatus(2)">
        <div class="stat-icon">
          <el-icon :size="32"><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.resolved }}</div>
          <div class="stat-label">已解决</div>
        </div>
      </div>
      <div class="stat-card total">
        <div class="stat-icon">
          <el-icon :size="32"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">异常总数</div>
        </div>
      </div>
    </div>

    <div class="search-section">
      <el-form :model="query" inline>
        <el-form-item label="异常编号">
          <el-input v-model="query.exceptionNo" placeholder="请输入异常编号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="异常类型">
          <el-select v-model="query.exceptionType" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="工单号">
          <el-input v-model="traceWorkNo" placeholder="输入工单号追溯" clearable style="width: 180px" />
          <el-button type="primary" style="margin-left: 8px" @click="handleBatchTrace">批次追溯</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="danger" @click="handleReport">上报异常</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="exceptionNo" label="异常编号" width="170" />
      <el-table-column label="异常类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.exceptionType)">{{ typeText(row.exceptionType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="workOrderNo" label="关联工单" width="150" />
      <el-table-column prop="description" label="异常描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reporter" label="上报人" width="100" align="center" />
      <el-table-column prop="reportTime" label="上报时间" width="170" align="center" />
      <el-table-column label="操作" width="240" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" link type="primary" @click="handleHandle(row)">处理</el-button>
          <el-button v-if="row.status === 1" link type="success" @click="handleResolve(row)">解决</el-button>
          <el-button link type="info" @click="handleDetail(row)">详情</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无数据" />
      </template>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </div>

    <el-dialog v-model="reportDialogVisible" title="上报异常" width="560px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="异常类型" prop="exceptionType">
          <el-select v-model="form.exceptionType" placeholder="请选择异常类型" style="width: 100%">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联工单" prop="workOrderId">
          <el-select v-model="form.workOrderId" placeholder="请选择工单" filterable style="width: 100%">
            <el-option v-for="item in workOrderList" :key="item.id" :label="item.workNo" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="异常描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请详细描述异常情况" />
        </el-form-item>
        <el-form-item label="上报人" prop="reporter">
          <el-input v-model="form.reporter" placeholder="请输入上报人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="异常详情" width="600px">
      <el-descriptions v-if="currentDetail" :column="2" border>
        <el-descriptions-item label="异常编号">{{ currentDetail.exceptionNo }}</el-descriptions-item>
        <el-descriptions-item label="异常类型">
          <el-tag :type="typeTag(currentDetail.exceptionType)">{{ typeText(currentDetail.exceptionType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="关联工单">{{ currentDetail.workOrderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(currentDetail.status)">{{ statusText(currentDetail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="上报人">{{ currentDetail.reporter || '-' }}</el-descriptions-item>
        <el-descriptions-item label="上报时间">{{ currentDetail.reportTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="异常描述" :span="2">{{ currentDetail.description }}</el-descriptions-item>
        <el-descriptions-item v-if="currentDetail.handler" label="处理人">{{ currentDetail.handler }}</el-descriptions-item>
        <el-descriptions-item v-if="currentDetail.handleTime" label="处理时间">{{ currentDetail.handleTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentDetail.handleResult" label="处理结果" :span="2">
          {{ currentDetail.handleResult }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resolveDialogVisible" title="解决异常" width="500px">
      <el-form :model="resolveForm" :rules="resolveRules" ref="resolveFormRef" label-width="100px">
        <el-form-item label="处理结果" prop="handleResult">
          <el-input v-model="resolveForm.handleResult" type="textarea" :rows="4" placeholder="请填写处理结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="resolveSubmitting" @click="handleResolveSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Loading, CircleCheck, Document } from '@element-plus/icons-vue'
import {
  getList, getById, save, remove, handle, resolve, batchTrace
} from '../../api/exception'
import { getList as getWorkOrderList } from '../../api/workOrder'

const statusOptions = [
  { value: 0, label: '待处理' },
  { value: 1, label: '处理中' },
  { value: 2, label: '已解决' }
]

const typeOptions = [
  { value: 'quality', label: '质量异常' },
  { value: 'equipment', label: '设备异常' },
  { value: 'material', label: '物料异常' },
  { value: 'process', label: '工艺异常' },
  { value: 'other', label: '其他异常' }
]

const statusText = (status) => {
  const item = statusOptions.find(s => s.value === status)
  return item ? item.label : '未知'
}
const statusType = (status) => {
  const map = { 0: 'warning', 1: 'primary', 2: 'success' }
  return map[status] || 'info'
}

const typeText = (type) => {
  const item = typeOptions.find(t => t.value === type)
  return item ? item.label : '未知'
}
const typeTag = (type) => {
  const map = {
    quality: 'danger',
    equipment: 'warning',
    material: 'primary',
    process: 'success',
    other: 'info'
  }
  return map[type] || 'info'
}

const query = reactive({ page: 1, size: 10, exceptionNo: '', exceptionType: '', status: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const traceWorkNo = ref('')

const stats = reactive({
  pending: 0,
  processing: 0,
  resolved: 0,
  total: 0
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getList({ ...query })
    list.value = res.data.records || []
    total.value = res.data.total || 0
    await fetchStats()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  try {
    const [p0, p1, p2] = await Promise.all([
      getList({ page: 1, size: 1, status: 0 }),
      getList({ page: 1, size: 1, status: 1 }),
      getList({ page: 1, size: 1, status: 2 })
    ])
    stats.pending = p0.data.total || 0
    stats.processing = p1.data.total || 0
    stats.resolved = p2.data.total || 0
    stats.total = stats.pending + stats.processing + stats.resolved
  } catch (e) {
    console.error(e)
  }
}

const handleSearch = () => {
  query.page = 1
  fetchList()
}

const handleReset = () => {
  query.exceptionNo = ''
  query.exceptionType = ''
  query.status = ''
  query.page = 1
  traceWorkNo.value = ''
  fetchList()
}

const filterByStatus = (status) => {
  query.status = status
  query.page = 1
  fetchList()
}

const handleBatchTrace = async () => {
  if (!traceWorkNo.value) {
    ElMessage.warning('请输入工单号')
    return
  }
  loading.value = true
  try {
    const res = await batchTrace(traceWorkNo.value)
    list.value = res.data || []
    total.value = list.value.length
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const workOrderList = ref([])

const loadWorkOrders = async () => {
  try {
    const res = await getWorkOrderList({ page: 1, size: 100 })
    workOrderList.value = res.data.records || res.data || []
  } catch (e) {
    console.error(e)
  }
}

const reportDialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  exceptionType: '',
  workOrderId: null,
  description: '',
  reporter: ''
})

const rules = {
  exceptionType: [{ required: true, message: '请选择异常类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入异常描述', trigger: 'blur' }]
}

const resetForm = () => {
  form.id = null
  form.exceptionType = ''
  form.workOrderId = null
  form.description = ''
  form.reporter = ''
}

const handleReport = () => {
  resetForm()
  reportDialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await save({ ...form })
    ElMessage.success('上报成功')
    reportDialogVisible.value = false
    fetchList()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const handleHandle = (row) => {
  ElMessageBox.confirm(`确定开始处理异常「${row.exceptionNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await handle(row.id)
      ElMessage.success('已开始处理')
      fetchList()
    }).catch(() => {})
}

const resolveDialogVisible = ref(false)
const resolveSubmitting = ref(false)
const resolveFormRef = ref()
const resolveForm = reactive({ id: null, handleResult: '' })
const resolveRules = {
  handleResult: [{ required: true, message: '请填写处理结果', trigger: 'blur' }]
}

const handleResolve = (row) => {
  resolveForm.id = row.id
  resolveForm.handleResult = ''
  resolveDialogVisible.value = true
}

const handleResolveSubmit = async () => {
  await resolveFormRef.value.validate()
  resolveSubmitting.value = true
  try {
    await resolve(resolveForm.id)
    ElMessage.success('已解决')
    resolveDialogVisible.value = false
    fetchList()
  } catch (e) {
    console.error(e)
  } finally {
    resolveSubmitting.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除异常「${row.exceptionNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await remove(row.id)
      ElMessage.success('删除成功')
      fetchList()
    }).catch(() => {})
}

const detailDialogVisible = ref(false)
const currentDetail = ref(null)

const handleDetail = async (row) => {
  try {
    const res = await getById(row.id)
    currentDetail.value = res.data || row
    detailDialogVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadWorkOrders()
  fetchList()
})
</script>

<style scoped>
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.stat-card.pending .stat-icon {
  background: linear-gradient(135deg, #e6a23c, #f56c6c);
}
.stat-card.processing .stat-icon {
  background: linear-gradient(135deg, #409eff, #67c23a);
}
.stat-card.resolved .stat-icon {
  background: linear-gradient(135deg, #67c23a, #909399);
}
.stat-card.total .stat-icon {
  background: linear-gradient(135deg, #909399, #606266);
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
  margin-bottom: 4px;
}
.stat-label {
  font-size: 14px;
  color: #909399;
}
.search-section {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
