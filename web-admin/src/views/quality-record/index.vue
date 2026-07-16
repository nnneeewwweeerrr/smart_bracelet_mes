<template>
  <div class="page-container">
    <!-- 搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="任务编号">
          <el-input v-model="searchForm.taskNo" placeholder="请输入" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="任务类型">
          <el-select v-model="searchForm.taskType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="IQC来料检验" value="IQC" />
            <el-option label="IPQC过程检验" value="IPQC" />
            <el-option label="OQC出货检验" value="OQC" />
            <el-option label="FQC成品检验" value="FQC" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="待开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已完成" :value="2" />
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
        <span class="table-title">质检记录</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增任务
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="taskNo" label="任务编号" width="160" />
        <el-table-column prop="taskType" label="任务类型" width="140" align="center">
          <template #default="{ row }">{{ typeText(row.taskType) }}</template>
        </el-table-column>
        <el-table-column prop="sourceId" label="来源ID" width="100" align="center" />
        <el-table-column prop="inspectorId" label="检验员ID" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="primary" link @click="handleStart(row)">开始</el-button>
            <el-button v-if="row.status === 1" type="success" link @click="handleComplete(row)">完成</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑任务' : '新增任务'" width="550px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="任务编号" prop="taskNo">
          <el-input v-model="form.taskNo" placeholder="如：QT202607001" />
        </el-form-item>
        <el-form-item label="任务类型" prop="taskType">
          <el-select v-model="form.taskType" placeholder="请选择" style="width: 100%">
            <el-option label="IQC来料检验" value="IQC" />
            <el-option label="IPQC过程检验" value="IPQC" />
            <el-option label="OQC出货检验" value="OQC" />
            <el-option label="FQC成品检验" value="FQC" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源ID">
          <el-input-number v-model="form.sourceId" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="检验员ID">
          <el-input-number v-model="form.inspectorId" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="待开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getList, save, update, remove, start, complete } from '@/api/qualityTask'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({ taskNo: '', taskType: '', status: '' })

const form = reactive({
  id: null,
  taskNo: '',
  taskType: 'IQC',
  sourceId: null,
  inspectorId: null,
  status: 0
})

const rules = {
  taskNo: [{ required: true, message: '请输入任务编号', trigger: 'blur' }],
  taskType: [{ required: true, message: '请选择任务类型', trigger: 'change' }]
}

const tableData = ref([])

function typeText(type) {
  const map = { IQC: 'IQC来料检验', IPQC: 'IPQC过程检验', OQC: 'OQC出货检验', FQC: 'FQC成品检验' }
  return map[type] || type || '-'
}

function statusText(status) {
  const map = { 0: '待开始', 1: '进行中', 2: '已完成' }
  return map[status] || '未知'
}

function statusTagType(status) {
  const map = { 0: 'info', 1: 'warning', 2: 'success' }
  return map[status] || 'info'
}

function fetchData() {
  loading.value = true
  getList({
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

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.taskNo = ''
  searchForm.taskType = ''
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
  isEdit.value = false
  Object.assign(form, { id: null, taskNo: '', taskType: 'IQC', sourceId: null, inspectorId: null, status: 0 })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

function handleStart(row) {
  ElMessageBox.confirm(`确定要开始任务「${row.taskNo}」吗？`, '提示', { type: 'warning' })
    .then(() => {
      start(row.id).then(() => {
        ElMessage.success('任务已开始')
        fetchData()
      }).catch(err => ElMessage.error('操作失败: ' + (err.message || '')))
    }).catch(() => {})
}

function handleComplete(row) {
  ElMessageBox.confirm(`确定要完成任务「${row.taskNo}」吗？`, '提示', { type: 'warning' })
    .then(() => {
      complete(row.id).then(() => {
        ElMessage.success('任务已完成')
        fetchData()
      }).catch(err => ElMessage.error('操作失败: ' + (err.message || '')))
    }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除任务「${row.taskNo}」吗？`, '提示', { type: 'warning' })
    .then(() => {
      remove(row.id).then(() => {
        ElMessage.success('删除成功')
        fetchData()
      }).catch(err => ElMessage.error('删除失败: ' + (err.message || '')))
    }).catch(() => {})
}

function handleSubmit() {
  formRef.value.validate(valid => {
    if (!valid) return
    submitLoading.value = true
    const action = isEdit.value ? update(form) : save(form)
    action.then(() => {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      fetchData()
    }).catch(err => {
      ElMessage.error('操作失败: ' + (err.message || ''))
    }).finally(() => {
      submitLoading.value = false
    })
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container { width: 100%; }
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
