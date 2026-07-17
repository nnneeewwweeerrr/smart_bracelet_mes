<template>
  <div class="page-container">
    <!-- 搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="入库单号">
          <el-input v-model="searchForm.stockInNo" placeholder="请输入" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="物料编号">
          <el-input v-model="searchForm.materialNo" placeholder="请输入" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="物料名称">
          <el-input v-model="searchForm.materialName" placeholder="请输入" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 140px">
            <el-option label="采购入库" value="采购入库" />
            <el-option label="生产入库" value="生产入库" />
            <el-option label="退货入库" value="退货入库" />
            <el-option label="调拨入库" value="调拨入库" />
            <el-option label="其他" value="其他" />
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
        <span class="table-title">入库记录</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增入库
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="stockInNo" label="入库单号" width="150" />
        <el-table-column prop="materialNo" label="物料编号" width="110" />
        <el-table-column prop="materialName" label="物料名称" min-width="130" />
        <el-table-column prop="quantity" label="数量" width="90" align="right" />
        <el-table-column prop="unitPrice" label="单价" width="100" align="right">
          <template #default="{ row }">¥{{ row.unitPrice ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="120" align="right">
          <template #default="{ row }">¥{{ row.totalAmount ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100" align="center" />
        <el-table-column prop="warehouseName" label="仓库" width="100" />
        <el-table-column prop="operator" label="操作人" width="90" />
        <el-table-column prop="inDate" label="入库时间" width="160" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已入库' : '待审核' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑入库' : '新增入库'" width="700px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入库单号" prop="stockInNo">
              <el-input v-model="form.stockInNo" placeholder="如：IN202607001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
                <el-option label="采购入库" value="采购入库" />
                <el-option label="生产入库" value="生产入库" />
                <el-option label="退货入库" value="退货入库" />
                <el-option label="调拨入库" value="调拨入库" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料编号">
              <el-input v-model="form.materialNo" placeholder="请输入物料编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料名称">
              <el-input v-model="form.materialName" placeholder="请输入物料名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单价" prop="unitPrice">
              <el-input-number v-model="form.unitPrice" :min="0" :precision="2" :step="0.1" style="width: 100%" />
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
            <el-form-item label="供应商">
              <el-input v-model="form.supplierName" placeholder="请输入供应商" />
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
                <el-option label="待审核" :value="0" />
                <el-option label="已入库" :value="1" />
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
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { demoStockIn } from '@/api/demo'
import wsClient from '@/utils/websocket'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({ stockInNo: '', materialNo: '', materialName: '', type: '' })

const form = reactive({
  id: null,
  stockInNo: '',
  type: '采购入库',
  materialNo: '',
  materialName: '',
  warehouseName: '原材料仓',
  quantity: 0,
  unitPrice: 0,
  supplierName: '',
  operator: '',
  status: 0,
  remark: ''
})

const rules = {
  stockInNo: [{ required: true, message: '请输入入库单号', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  warehouseName: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }]
}

const tableData = ref([])

function fetchData() {
  loading.value = true
  demoStockIn.getList({
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
  searchForm.stockInNo = ''
  searchForm.materialNo = ''
  searchForm.materialName = ''
  searchForm.type = ''
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
    id: null, stockInNo: '', type: '采购入库', materialNo: '', materialName: '',
    warehouseName: '原材料仓', quantity: 0, unitPrice: 0, supplierName: '',
    operator: '', status: 0, remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除入库单「${row.stockInNo}」吗？`, '提示', { type: 'warning' })
    .then(() => {
      demoStockIn.remove(row.id).then(() => {
        ElMessage.success('删除成功')
        fetchData()
      }).catch(err => ElMessage.error('删除失败: ' + (err.message || '')))
    }).catch(() => {})
}

function handleSubmit() {
  formRef.value.validate(valid => {
    if (!valid) return
    submitLoading.value = true
    const action = isEdit.value ? demoStockIn.update(form) : demoStockIn.save(form)
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

function handleStockInPush(payload) {
  ElMessage.success(`新入库：${payload.materialName} ${payload.quantity}件，${payload.warehouseName}`)
  fetchData()
}

onMounted(() => {
  fetchData()
  wsClient.on('STOCK_IN', handleStockInPush)
})

onUnmounted(() => {
  wsClient.off('STOCK_IN', handleStockInPush)
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
