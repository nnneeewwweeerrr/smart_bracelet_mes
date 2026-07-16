<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-icon" style="background: #909399"><el-icon><Document /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total || 0 }}</div>
              <div class="stat-label">异常总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-icon" style="background: #e6a23c"><el-icon><Warning /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pending || 0 }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-icon" style="background: #409eff"><el-icon><Loading /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.processing || 0 }}</div>
              <div class="stat-label">处理中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-icon" style="background: #67c23a"><el-icon><CircleCheck /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ (stats.resolved || 0) + (stats.closed || 0) }}</div>
              <div class="stat-label">已解决/关闭</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="异常编号">
          <el-input v-model="searchForm.exceptionNo" placeholder="请输入" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="异常类型">
          <el-input v-model="searchForm.exceptionType" placeholder="请输入" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="级别">
          <el-select v-model="searchForm.exceptionLevel" placeholder="请选择" clearable style="width: 120px">
            <el-option label="紧急" value="紧急" />
            <el-option label="严重" value="严重" />
            <el-option label="一般" value="一般" />
            <el-option label="轻微" value="轻微" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已解决" :value="2" />
            <el-option label="已关闭" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区 -->
    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <span class="table-title">异常处理工作台</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 上报异常
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="exceptionNo" label="异常编号" width="150" />
        <el-table-column prop="title" label="异常标题" min-width="150" />
        <el-table-column prop="exceptionType" label="类型" width="110" align="center" />
        <el-table-column prop="exceptionLevel" label="级别" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.exceptionLevel)">{{ row.exceptionLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reporter" label="上报人" width="90" />
        <el-table-column prop="reportTime" label="上报时间" width="160" />
        <el-table-column prop="handler" label="处理人" width="90" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="primary" link @click="handleStart(row)">开始处理</el-button>
            <el-button v-if="row.status === 1" type="success" link @click="handleSubmit(row)">提交报告</el-button>
            <el-button v-if="row.status === 2" type="warning" link @click="handleClose(row)">关闭</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 上报异常弹窗 -->
    <el-dialog v-model="dialogVisible" title="上报异常" width="650px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="异常编号" prop="exceptionNo">
              <el-input v-model="form.exceptionNo" placeholder="如：EX202607001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="异常类型" prop="exceptionType">
              <el-select v-model="form.exceptionType" placeholder="请选择" style="width: 100%">
                <el-option label="设备异常" value="设备异常" />
                <el-option label="质量异常" value="质量异常" />
                <el-option label="工艺异常" value="工艺异常" />
                <el-option label="物料异常" value="物料异常" />
                <el-option label="安全异常" value="安全异常" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="异常级别" prop="exceptionLevel">
              <el-select v-model="form.exceptionLevel" placeholder="请选择" style="width: 100%">
                <el-option label="紧急" value="紧急" />
                <el-option label="严重" value="严重" />
                <el-option label="一般" value="一般" />
                <el-option label="轻微" value="轻微" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-input-number v-model="form.priority" :min="1" :max="5" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="异常标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入异常标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上报人">
              <el-input v-model="form.reporter" placeholder="请输入上报人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源单号">
              <el-input v-model="form.sourceNo" placeholder="请输入来源单号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="异常描述">
              <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入异常描述" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitAdd">确定</el-button>
      </template>
    </el-dialog>

    <!-- 提交处理报告弹窗 -->
    <el-dialog v-model="reportVisible" title="提交处理报告" width="550px" :close-on-click-modal="false">
      <el-form ref="reportFormRef" :model="reportForm" :rules="reportRules" label-width="100px">
        <el-form-item label="异常编号">
          <el-input :model-value="reportForm.exceptionNo" disabled />
        </el-form-item>
        <el-form-item label="处理内容" prop="handleContent">
          <el-input v-model="reportForm.handleContent" type="textarea" :rows="3" placeholder="请输入处理内容" />
        </el-form-item>
        <el-form-item label="处理结果" prop="handleResult">
          <el-input v-model="reportForm.handleResult" type="textarea" :rows="2" placeholder="请输入处理结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" :loading="reportLoading" @click="handleSubmitReport">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getList as getExceptionList, save as saveException, remove as deleteException, getStatistics } from '@/api/exception'
import request from '@/api/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const reportVisible = ref(false)
const reportLoading = ref(false)
const reportFormRef = ref(null)

const stats = ref({})
const searchForm = reactive({ exceptionNo: '', exceptionType: '', exceptionLevel: '', status: '' })

const form = reactive({
  exceptionNo: '',
  exceptionType: '设备异常',
  exceptionLevel: '一般',
  title: '',
  sourceNo: '',
  reporter: '',
  description: '',
  priority: 3,
  status: 0
})

const rules = {
  exceptionNo: [{ required: true, message: '请输入异常编号', trigger: 'blur' }],
  exceptionType: [{ required: true, message: '请选择异常类型', trigger: 'change' }],
  exceptionLevel: [{ required: true, message: '请选择异常级别', trigger: 'change' }],
  title: [{ required: true, message: '请输入异常标题', trigger: 'blur' }]
}

const reportForm = reactive({
  id: null,
  exceptionNo: '',
  handleContent: '',
  handleResult: ''
})

const reportRules = {
  handleContent: [{ required: true, message: '请输入处理内容', trigger: 'blur' }],
  handleResult: [{ required: true, message: '请输入处理结果', trigger: 'blur' }]
}

const tableData = ref([])

function statusText(status) {
  const map = { 0: '待处理', 1: '处理中', 2: '已解决', 3: '已关闭' }
  return map[status] || '未知'
}

function statusTagType(status) {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'success' }
  return map[status] || 'info'
}

function levelTagType(level) {
  const map = { '紧急': 'danger', '严重': 'danger', '一般': 'warning', '轻微': 'info' }
  return map[level] || 'info'
}

function fetchData() {
  loading.value = true
  getExceptionList({
    pageNum: currentPage.value,
    pageSize: pageSize.value,
    ...searchForm
  }).then(res => {
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }).catch(err => {
    ElMessage.error('加载失败: ' + (err.message || ''))
  }).finally(() => {
    loading.value = false
  })
}

function fetchStats() {
  getStatistics().then(res => {
    stats.value = res.data || {}
  }).catch(() => {})
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.exceptionNo = ''
  searchForm.exceptionType = ''
  searchForm.exceptionLevel = ''
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

function handleAdd() {
  Object.assign(form, {
    exceptionNo: '', exceptionType: '设备异常', exceptionLevel: '一般',
    title: '', sourceNo: '', reporter: '', description: '', priority: 3, status: 0
  })
  dialogVisible.value = true
}

function handleStart(row) {
  ElMessageBox.confirm(`确定要开始处理异常「${row.exceptionNo}」吗？`, '提示', { type: 'warning' })
    .then(() => {
      request.put(`/exception-handle/start/${row.id}`, { handlerName: row.reporter || '当前用户' }).then(() => {
        ElMessage.success('已开始处理')
        fetchData()
        fetchStats()
      }).catch(err => ElMessage.error('操作失败: ' + (err.message || '')))
    }).catch(() => {})
}

function handleSubmit(row) {
  Object.assign(reportForm, {
    id: row.id, exceptionNo: row.exceptionNo,
    handleContent: '', handleResult: ''
  })
  reportVisible.value = true
}

function handleClose(row) {
  ElMessageBox.confirm(`确定要关闭异常「${row.exceptionNo}」吗？`, '提示', { type: 'warning' })
    .then(() => {
      request.put(`/exception-handle/close/${row.id}`).then(() => {
        ElMessage.success('已关闭')
        fetchData()
        fetchStats()
      }).catch(err => ElMessage.error('操作失败: ' + (err.message || '')))
    }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除异常「${row.exceptionNo}」吗？`, '提示', { type: 'warning' })
    .then(() => {
      deleteException(row.id).then(() => {
        ElMessage.success('删除成功')
        fetchData()
        fetchStats()
      }).catch(err => ElMessage.error('删除失败: ' + (err.message || '')))
    }).catch(() => {})
}

function handleSubmitAdd() {
  formRef.value.validate(valid => {
    if (!valid) return
    submitLoading.value = true
    saveException(form).then(() => {
      ElMessage.success('上报成功')
      dialogVisible.value = false
      fetchData()
      fetchStats()
    }).catch(err => {
      ElMessage.error('操作失败: ' + (err.message || ''))
    }).finally(() => {
      submitLoading.value = false
    })
  })
}

function handleSubmitReport() {
  reportFormRef.value.validate(valid => {
    if (!valid) return
    reportLoading.value = true
    request.put(`/exception-handle/submit/${reportForm.id}`, {
      handleContent: reportForm.handleContent,
      handleResult: reportForm.handleResult
    }).then(() => {
      ElMessage.success('报告已提交')
      reportVisible.value = false
      fetchData()
      fetchStats()
    }).catch(err => {
      ElMessage.error('提交失败: ' + (err.message || ''))
    }).finally(() => {
      reportLoading.value = false
    })
  })
}

onMounted(() => {
  fetchData()
  fetchStats()
})
</script>

<style scoped>
.page-container { width: 100%; }
.stats-row { margin-bottom: 20px; }
.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  flex-shrink: 0;
}
.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}
.search-card { margin-bottom: 20px; }
.table-card { margin-bottom: 20px; }
.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
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
</style>
