<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="计划编号">
          <el-input v-model="searchForm.planNo" placeholder="请输入计划编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="计划名称">
          <el-input v-model="searchForm.planName" placeholder="请输入计划名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="计划状态">
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
        <span class="table-title">生产计划列表</span>
        <div class="header-btns">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增计划
          </el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="planNo" label="计划编号" width="160" />
        <el-table-column prop="planName" label="计划名称" min-width="180" />
        <el-table-column prop="productName" label="产品名称" width="180" />
        <el-table-column prop="quantity" label="计划数量" width="100" align="center" />
        <el-table-column prop="completedQuantity" label="完成数量" width="100" align="center" />
        <el-table-column label="完成进度" width="180" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="row.quantity ? Math.round((row.completedQuantity || 0) / row.quantity * 100) : 0"
              :stroke-width="18"
              :text-inside="true"
              size="small"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="110" />
        <el-table-column prop="endDate" label="结束日期" width="110" />
        <el-table-column prop="responsible" label="负责人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="300" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)" :disabled="row.status !== 0">编辑</el-button>
            <el-button type="success" link @click="handleStart(row)" :disabled="row.status !== 0">启动</el-button>
            <el-button type="danger" link @click="handleDelete(row)" :disabled="row.status !== 0">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑计划' : '新增计划'" width="650px" :close-on-click-modal="false">
      <el-form ref="planFormRef" :model="planForm" :rules="planRules" label-width="100px">
        <el-form-item label="计划名称" prop="planName">
          <el-input v-model="planForm.planName" placeholder="请输入计划名称" />
        </el-form-item>
        <el-form-item label="产品名称" prop="productId">
          <el-select v-model="planForm.productId" placeholder="请选择产品" style="width: 100%" @change="handleProductChange" filterable>
            <el-option v-for="item in productList" :key="item.id" :label="item.productName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="planForm.productSpec" placeholder="自动带出" disabled />
        </el-form-item>
        <el-form-item label="计划数量" prop="quantity">
          <el-input-number v-model="planForm.quantity" :min="1" :max="99999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="planForm.startDate"
            type="date"
            placeholder="选择开始日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="planForm.endDate"
            type="date"
            placeholder="选择结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="负责人" prop="responsible">
          <el-input v-model="planForm.responsible" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="planForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="计划详情" width="700px">
      <el-descriptions :column="2" border style="margin-bottom: 20px" v-if="currentPlan">
        <el-descriptions-item label="计划编号">{{ currentPlan.planNo }}</el-descriptions-item>
        <el-descriptions-item label="计划状态">
          <el-tag :type="getStatusType(currentPlan.status)" size="small">
            {{ getStatusText(currentPlan.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="计划名称">{{ currentPlan.planName }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ currentPlan.productName }}</el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ currentPlan.productSpec }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ currentPlan.responsible }}</el-descriptions-item>
        <el-descriptions-item label="计划数量">{{ currentPlan.quantity }}</el-descriptions-item>
        <el-descriptions-item label="完成数量">{{ currentPlan.completedQuantity || 0 }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ currentPlan.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ currentPlan.endDate }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentPlan.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentPlan.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="currentPlan && workOrders.length > 0" style="margin-top: 20px;">
        <div style="font-weight: bold; margin-bottom: 10px;">关联工单</div>
        <el-table :data="workOrders" border size="small">
          <el-table-column prop="workNo" label="工单号" width="160" />
          <el-table-column prop="productName" label="产品名称" />
          <el-table-column prop="quantity" label="数量" width="100" align="center" />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getWorkOrderStatusType(row.status)" size="small">
              {{ getWorkOrderStatusText(row.status) }}
            </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getProductionPlanList,
  getProductionPlanDetail,
  createProductionPlan,
  updateProductionPlan,
  deleteProductionPlan,
  startProductionPlan
} from '../../api/productionPlan'
import { getProductAll } from '../../api/product'
import { getWorkOrderList } from '../../api/workOrder'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const planFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const searchForm = reactive({
  planNo: '',
  planName: '',
  status: ''
})

const planForm = reactive({
  id: null,
  planName: '',
  productId: null,
  productName: '',
  productSpec: '',
  quantity: 1,
  startDate: '',
  endDate: '',
  responsible: '',
  remark: ''
})

const currentPlan = ref(null)
const workOrders = ref([])
const productList = ref([])

const statusOptions = [
  { label: '未启动', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 }
]

const planRules = {
  planName: [
    { required: true, message: '请输入计划名称', trigger: 'blur' }
  ],
  productId: [
    { required: true, message: '请选择产品', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入计划数量', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ],
  responsible: [
    { required: true, message: '请输入负责人', trigger: 'blur' }
  ]
}

function getStatusType(status) {
  const types = ['info', 'primary', 'success', 'danger']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['未启动', '进行中', '已完成', '已取消']
  return texts[status] || '未知'
}

function getWorkOrderStatusType(status) {
  const types = ['info', 'primary', 'success', 'danger']
  return types[status] || 'info'
}

function getWorkOrderStatusText(status) {
  const texts = ['待生产', '生产中', '已完成', '已取消']
  return texts[status] || '未知'
}

function fetchData() {
  loading.value = true
  const params = {
    current: currentPage.value,
    size: pageSize.value
  }
  if (searchForm.planNo) {
    params.planNo = searchForm.planNo
  }
  if (searchForm.planName) {
    params.planName = searchForm.planName
  }
  if (searchForm.status !== '') {
    params.status = searchForm.status
  }

  getProductionPlanList(params).then(res => {
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  }).finally(() => {
    loading.value = false
  })
}

function loadProducts() {
  getProductAll().then(res => {
    if (res.code === 200) {
      productList.value = res.data || []
    }
  })
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.planNo = ''
  searchForm.planName = ''
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
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  planForm.id = row.id
  planForm.planName = row.planName
  planForm.productId = row.productId
  planForm.productName = row.productName
  planForm.productSpec = row.productSpec
  planForm.quantity = row.quantity
  planForm.startDate = row.startDate
  planForm.endDate = row.endDate
  planForm.responsible = row.responsible
  planForm.remark = row.remark
  dialogVisible.value = true
}

function handleView(row) {
  getProductionPlanDetail(row.id).then(res => {
    if (res.code === 200) {
      currentPlan.value = res.data
      loadWorkOrders(row.id)
      detailVisible.value = true
    }
  })
}

function loadWorkOrders(planId) {
  getWorkOrderList({ current: 1, size: 50, planId }).then(res => {
    if (res.code === 200) {
      workOrders.value = res.data.records || []
    }
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除计划「${row.planNo}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteProductionPlan(row.id).then(res => {
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchData()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
  }).catch(() => {})
}

function handleStart(row) {
  ElMessageBox.confirm(`确定要启动计划「${row.planNo}」吗？`, '确认', {
    confirmButtonText: '启动计划',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => {
    startProductionPlan(row.id).then(res => {
      if (res.code === 200) {
        ElMessage.success('计划已启动')
        fetchData()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    })
  }).catch(() => {})
}

function handleProductChange(val) {
  const product = productList.value.find(p => p.id === val)
  if (product) {
    planForm.productName = product.productName
    planForm.productSpec = product.model || product.spec || ''
  }
}

function resetForm() {
  planForm.id = null
  planForm.planName = ''
  planForm.productId = null
  planForm.productName = ''
  planForm.productSpec = ''
  planForm.quantity = 1
  planForm.startDate = ''
  planForm.endDate = ''
  planForm.responsible = ''
  planForm.remark = ''
  if (planFormRef.value) {
    planFormRef.value.resetFields()
  }
}

async function handleSubmit() {
  if (!planFormRef.value) return
  
  try {
    await planFormRef.value.validate()
    submitLoading.value = true
    
    const api = isEdit.value ? updateProductionPlan : createProductionPlan
    const res = await api(planForm)
    
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('表单验证失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchData()
  loadProducts()
})
</script>

<style scoped>
.page-container {
  width: 100%;
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
