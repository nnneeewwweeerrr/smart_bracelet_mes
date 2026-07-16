<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-icon" style="background: #f56c6c"><el-icon><Warning /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.lowStockCount || 0 }}</div>
              <div class="stat-label">低于安全库存</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-icon" style="background: #e6a23c"><el-icon><Warning /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.highStockCount || 0 }}</div>
              <div class="stat-label">高于最大库存</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-icon" style="background: #909399"><el-icon><Bell /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingCount || 0 }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-icon" style="background: #67c23a"><el-icon><CircleCheck /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.processedCount || 0 }}</div>
              <div class="stat-label">已处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="预警单号">
          <el-input v-model="searchForm.alertNo" placeholder="请输入" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="物料编号">
          <el-input v-model="searchForm.materialNo" placeholder="请输入" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="物料名称">
          <el-input v-model="searchForm.materialName" placeholder="请输入" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="待处理" :value="0" />
            <el-option label="已处理" :value="1" />
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
        <span class="table-title">库存预警</span>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="alertNo" label="预警单号" width="150" />
        <el-table-column prop="materialNo" label="物料编号" width="110" />
        <el-table-column prop="materialName" label="物料名称" min-width="130" />
        <el-table-column prop="warehouseName" label="仓库" width="100" />
        <el-table-column prop="alertType" label="预警类型" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="row.alertType === '低于安全库存' ? 'danger' : 'warning'">{{ row.alertType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentQuantity" label="当前数量" width="100" align="right" />
        <el-table-column prop="threshold" label="阈值" width="90" align="right" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已处理' : '待处理' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handleRemark" label="处理备注" min-width="130" />
        <el-table-column prop="handleTime" label="处理时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="primary" link @click="handleProcess(row)">处理</el-button>
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

    <!-- 处理弹窗 -->
    <el-dialog v-model="dialogVisible" title="处理预警" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="预警单号">
          <el-input :model-value="form.alertNo" disabled />
        </el-form-item>
        <el-form-item label="物料">
          <el-input :model-value="form.materialName" disabled />
        </el-form-item>
        <el-form-item label="处理备注" prop="processRemark">
          <el-input v-model="form.processRemark" type="textarea" :rows="3" placeholder="请输入处理备注" />
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
import { demoStockAlert } from '@/api/demo'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const stats = ref({})
const searchForm = reactive({ alertNo: '', materialNo: '', materialName: '', status: '' })

const form = reactive({
  id: null,
  alertNo: '',
  materialName: '',
  processRemark: ''
})

const rules = {
  processRemark: [{ required: true, message: '请输入处理备注', trigger: 'blur' }]
}

const tableData = ref([])

function fetchData() {
  loading.value = true
  demoStockAlert.getList({
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
  demoStockAlert.getStatistics().then(res => {
    stats.value = res.data || {}
  }).catch(() => {})
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.alertNo = ''
  searchForm.materialNo = ''
  searchForm.materialName = ''
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

function handleProcess(row) {
  form.id = row.id
  form.alertNo = row.alertNo
  form.materialName = row.materialName
  form.processRemark = ''
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除预警单「${row.alertNo}」吗？`, '提示', { type: 'warning' })
    .then(() => {
      demoStockAlert.remove(row.id).then(() => {
        ElMessage.success('删除成功')
        fetchData()
        fetchStats()
      }).catch(err => ElMessage.error('删除失败: ' + (err.message || '')))
    }).catch(() => {})
}

function handleSubmit() {
  formRef.value.validate(valid => {
    if (!valid) return
    submitLoading.value = true
    demoStockAlert.process(form.id, { processRemark: form.processRemark }).then(() => {
      ElMessage.success('处理成功')
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
