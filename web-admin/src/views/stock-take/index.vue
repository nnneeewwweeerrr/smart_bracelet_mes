<template>
  <div class="page-container">
    <!-- 搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="盘点单号">
          <el-input v-model="searchForm.checkNo" placeholder="请输入" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="仓库">
          <el-input v-model="searchForm.warehouseName" placeholder="请输入" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="进行中" :value="0" />
            <el-option label="已审核" :value="1" />
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
        <span class="table-title">库存盘点</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增盘点
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="checkNo" label="盘点单号" width="160" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="checkDate" label="盘点时间" width="170" />
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" type="warning" link @click="handleAudit(row)">审核</el-button>
            <el-button v-if="row.status === 1" type="success" link @click="handleComplete(row)">完成</el-button>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑盘点' : '新增盘点'" width="600px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="盘点单号" prop="checkNo">
              <el-input v-model="form.checkNo" placeholder="如：PC202607001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库" prop="warehouseName">
              <el-select v-model="form.warehouseName" placeholder="请选择" style="width: 100%">
                <el-option label="原材料仓" value="原材料仓" />
                <el-option label="成品仓" value="成品仓" />
                <el-option label="半成品仓" value="半成品仓" />
                <el-option label="辅料仓" value="辅料仓" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作人" prop="operator">
              <el-input v-model="form.operator" placeholder="请输入操作人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="进行中" :value="0" />
                <el-option label="已审核" :value="1" />
                <el-option label="已完成" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
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
import { demoStockCheck } from '@/api/demo'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({ checkNo: '', warehouseName: '', status: '' })

const form = reactive({
  id: null,
  checkNo: '',
  warehouseName: '原材料仓',
  operator: '',
  status: 0,
  remark: ''
})

const rules = {
  checkNo: [{ required: true, message: '请输入盘点单号', trigger: 'blur' }],
  warehouseName: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }]
}

const tableData = ref([])

function statusText(status) {
  const map = { 0: '进行中', 1: '已审核', 2: '已完成' }
  return map[status] || '未知'
}

function statusTagType(status) {
  const map = { 0: 'info', 1: 'warning', 2: 'success' }
  return map[status] || 'info'
}

function fetchData() {
  loading.value = true
  demoStockCheck.getList({
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
  searchForm.checkNo = ''
  searchForm.warehouseName = ''
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
  Object.assign(form, {
    id: null, checkNo: '', warehouseName: '原材料仓',
    operator: '', status: 0, remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

function handleAudit(row) {
  ElMessageBox.confirm(`确定要审核盘点单「${row.checkNo}」吗？`, '提示', { type: 'warning' })
    .then(() => {
      demoStockCheck.audit(row.id).then(() => {
        ElMessage.success('审核成功')
        fetchData()
      }).catch(err => ElMessage.error('审核失败: ' + (err.message || '')))
    }).catch(() => {})
}

function handleComplete(row) {
  ElMessageBox.confirm(`确定要完成盘点单「${row.checkNo}」吗？完成后将更新库存。`, '提示', { type: 'warning' })
    .then(() => {
      demoStockCheck.complete(row.id).then(() => {
        ElMessage.success('完成成功')
        fetchData()
      }).catch(err => ElMessage.error('操作失败: ' + (err.message || '')))
    }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除盘点单「${row.checkNo}」吗？`, '提示', { type: 'warning' })
    .then(() => {
      demoStockCheck.remove(row.id).then(() => {
        ElMessage.success('删除成功')
        fetchData()
      }).catch(err => ElMessage.error('删除失败: ' + (err.message || '')))
    }).catch(() => {})
}

function handleSubmit() {
  formRef.value.validate(valid => {
    if (!valid) return
    submitLoading.value = true
    const action = isEdit.value ? demoStockCheck.update(form) : demoStockCheck.save(form)
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
