<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-icon" style="background: #409eff"><el-icon><Document /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.total || 0 }}</div>
              <div class="stat-label">不合格品总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-icon" style="background: #e6a23c"><el-icon><Warning /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pending || 0 }}</div>
              <div class="stat-label">待处置</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-icon" style="background: #67c23a"><el-icon><CircleCheck /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.completed || 0 }}</div>
              <div class="stat-label">已处置</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="不合格品编号">
          <el-input v-model="searchForm.unqualifiedNo" placeholder="请输入" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="来源类型">
          <el-select v-model="searchForm.sourceType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="来料检验" value="来料检验" />
            <el-option label="过程检验" value="过程检验" />
            <el-option label="出货检验" value="出货检验" />
            <el-option label="客诉退货" value="客诉退货" />
          </el-select>
        </el-form-item>
        <el-form-item label="缺陷类型">
          <el-select v-model="searchForm.defectType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="外观缺陷" value="外观缺陷" />
            <el-option label="功能缺陷" value="功能缺陷" />
            <el-option label="尺寸偏差" value="尺寸偏差" />
            <el-option label="性能不达标" value="性能不达标" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待处置" :value="0" />
            <el-option label="处置中" :value="1" />
            <el-option label="已处置" :value="2" />
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
        <span class="table-title">不合格品管理</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增记录
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="unqualifiedNo" label="不合格品编号" width="150" />
        <el-table-column prop="sourceType" label="来源类型" width="110" align="center" />
        <el-table-column prop="sourceNo" label="来源单号" width="130" />
        <el-table-column prop="materialNo" label="物料编号" width="110" />
        <el-table-column prop="materialName" label="物料名称" min-width="120" />
        <el-table-column prop="quantity" label="数量" width="80" align="right" />
        <el-table-column prop="defectType" label="缺陷类型" width="100" align="center" />
        <el-table-column prop="discoverer" label="发现人" width="90" />
        <el-table-column prop="discoveryTime" label="发现时间" width="160" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status !== 2" type="warning" link @click="handleDispose(row)">处置</el-button>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑记录' : '新增记录'" width="750px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="不合格品编号" prop="unqualifiedNo">
              <el-input v-model="form.unqualifiedNo" placeholder="如：UQ202607001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源类型" prop="sourceType">
              <el-select v-model="form.sourceType" placeholder="请选择" style="width: 100%">
                <el-option label="来料检验" value="来料检验" />
                <el-option label="过程检验" value="过程检验" />
                <el-option label="出货检验" value="出货检验" />
                <el-option label="客诉退货" value="客诉退货" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源单号">
              <el-input v-model="form.sourceNo" placeholder="请输入来源单号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工单号">
              <el-input v-model="form.workOrderNo" placeholder="请输入工单号" />
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
            <el-form-item label="批次号">
              <el-input v-model="form.batchNo" placeholder="请输入批次号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="缺陷类型" prop="defectType">
              <el-select v-model="form.defectType" placeholder="请选择" style="width: 100%">
                <el-option label="外观缺陷" value="外观缺陷" />
                <el-option label="功能缺陷" value="功能缺陷" />
                <el-option label="尺寸偏差" value="尺寸偏差" />
                <el-option label="性能不达标" value="性能不达标" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发现人">
              <el-input v-model="form.discoverer" placeholder="请输入发现人" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="缺陷描述">
              <el-input v-model="form.defectDesc" type="textarea" :rows="2" placeholder="请输入缺陷描述" />
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

    <!-- 处置弹窗 -->
    <el-dialog v-model="disposeVisible" title="处置不合格品" width="550px" :close-on-click-modal="false">
      <el-form ref="disposeFormRef" :model="disposeForm" :rules="disposeRules" label-width="100px">
        <el-form-item label="不合格品编号">
          <el-input :model-value="disposeForm.unqualifiedNo" disabled />
        </el-form-item>
        <el-form-item label="处置方式" prop="disposition">
          <el-select v-model="disposeForm.disposition" placeholder="请选择" style="width: 100%">
            <el-option label="返修" value="返修" />
            <el-option label="返工" value="返工" />
            <el-option label="报废" value="报废" />
            <el-option label="让步接收" value="让步接收" />
            <el-option label="退货" value="退货" />
          </el-select>
        </el-form-item>
        <el-form-item label="处置结果" prop="dispositionResult">
          <el-input v-model="disposeForm.dispositionResult" type="textarea" :rows="2" placeholder="请输入处置结果" />
        </el-form-item>
        <el-form-item label="责任人">
          <el-input v-model="disposeForm.responsible" placeholder="请输入责任人" />
        </el-form-item>
        <el-form-item label="纠正措施">
          <el-input v-model="disposeForm.rectifyAction" type="textarea" :rows="2" placeholder="请输入纠正措施" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="disposeVisible = false">取消</el-button>
        <el-button type="primary" :loading="disposeLoading" @click="handleDisposeSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUnqualifiedList, addUnqualified, updateUnqualified, handleUnqualified, deleteUnqualified, getUnqualifiedStats } from '@/api/unqualifiedProduct'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const disposeVisible = ref(false)
const disposeLoading = ref(false)
const disposeFormRef = ref(null)

const stats = ref({})
const searchForm = reactive({ unqualifiedNo: '', sourceType: '', defectType: '', status: '' })

const form = reactive({
  id: null,
  unqualifiedNo: '',
  sourceType: '来料检验',
  sourceNo: '',
  workOrderNo: '',
  materialNo: '',
  materialName: '',
  batchNo: '',
  quantity: 0,
  defectType: '外观缺陷',
  defectDesc: '',
  discoverer: '',
  disposition: '',
  dispositionResult: '',
  responsible: '',
  rectifyAction: '',
  status: 0,
  remark: ''
})

const rules = {
  unqualifiedNo: [{ required: true, message: '请输入不合格品编号', trigger: 'blur' }],
  sourceType: [{ required: true, message: '请选择来源类型', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  defectType: [{ required: true, message: '请选择缺陷类型', trigger: 'change' }]
}

const disposeForm = reactive({
  id: null,
  unqualifiedNo: '',
  disposition: '',
  dispositionResult: '',
  responsible: '',
  rectifyAction: ''
})

const disposeRules = {
  disposition: [{ required: true, message: '请选择处置方式', trigger: 'change' }],
  dispositionResult: [{ required: true, message: '请输入处置结果', trigger: 'blur' }]
}

const tableData = ref([])

function statusText(status) {
  const map = { 0: '待处置', 1: '处置中', 2: '已处置' }
  return map[status] || '未知'
}

function statusTagType(status) {
  const map = { 0: 'info', 1: 'warning', 2: 'success' }
  return map[status] || 'info'
}

function fetchData() {
  loading.value = true
  getUnqualifiedList({
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
  getUnqualifiedStats().then(res => {
    stats.value = res.data || {}
  }).catch(() => {})
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.unqualifiedNo = ''
  searchForm.sourceType = ''
  searchForm.defectType = ''
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
    id: null, unqualifiedNo: '', sourceType: '来料检验', sourceNo: '', workOrderNo: '',
    materialNo: '', materialName: '', batchNo: '', quantity: 0, defectType: '外观缺陷',
    defectDesc: '', discoverer: '', disposition: '', dispositionResult: '',
    responsible: '', rectifyAction: '', status: 0, remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

function handleDispose(row) {
  Object.assign(disposeForm, {
    id: row.id, unqualifiedNo: row.unqualifiedNo,
    disposition: row.disposition || '', dispositionResult: row.dispositionResult || '',
    responsible: row.responsible || '', rectifyAction: row.rectifyAction || ''
  })
  disposeVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除「${row.unqualifiedNo}」吗？`, '提示', { type: 'warning' })
    .then(() => {
      deleteUnqualified(row.id).then(() => {
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
    const action = isEdit.value ? updateUnqualified(form) : addUnqualified(form)
    action.then(() => {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
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

function handleDisposeSubmit() {
  disposeFormRef.value.validate(valid => {
    if (!valid) return
    disposeLoading.value = true
    handleUnqualified(disposeForm.id, {
      disposition: disposeForm.disposition,
      dispositionResult: disposeForm.dispositionResult,
      responsible: disposeForm.responsible,
      rectifyAction: disposeForm.rectifyAction
    }).then(() => {
      ElMessage.success('处置成功')
      disposeVisible.value = false
      fetchData()
      fetchStats()
    }).catch(err => {
      ElMessage.error('处置失败: ' + (err.message || ''))
    }).finally(() => {
      disposeLoading.value = false
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
