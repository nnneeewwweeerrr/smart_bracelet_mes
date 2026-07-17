<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="工单号">
          <el-input v-model="searchForm.workNo" placeholder="请输入工单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入产品名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="工单状态">
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
        <span class="table-title">生产工单列表</span>
        <div class="header-btns">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增工单
          </el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="workNo" label="工单号" width="160" />
        <el-table-column prop="productName" label="产品名称" min-width="180" />
        <el-table-column prop="spec" label="规格型号" width="140" />
        <el-table-column prop="quantity" label="计划数量" width="100" align="center" />
        <el-table-column prop="finishedQuantity" label="已完成数量" width="120" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="row.quantity ? Math.round(row.finishedQuantity / row.quantity * 100) : 0"
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
        <el-table-column prop="planStartDate" label="计划开始" width="110" />
        <el-table-column prop="planEndDate" label="计划完成" width="110" />
        <el-table-column prop="manager" label="负责人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="300" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)" :disabled="row.status !== 0">编辑</el-button>
            <el-button type="success" link @click="handleStart(row)" :disabled="row.status !== 0">开始生产</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑工单' : '新增工单'" width="650px" :close-on-click-modal="false">
      <el-form ref="workOrderFormRef" :model="workOrderForm" :rules="workOrderRules" label-width="100px">
        <el-form-item label="产品名称" prop="productId">
          <el-select v-model="workOrderForm.productId" placeholder="请选择产品" style="width: 100%" @change="handleProductChange" filterable>
            <el-option v-for="item in productList" :key="item.id" :label="item.productName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="workOrderForm.spec" placeholder="自动带出" disabled />
        </el-form-item>
        <el-form-item label="关联计划" prop="planId">
          <el-select v-model="workOrderForm.planId" placeholder="请选择生产计划（选填）" style="width: 100%" filterable clearable>
            <el-option v-for="item in planList" :key="item.id" :label="item.planNo + ' - ' + item.planName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划数量" prop="quantity">
          <el-input-number v-model="workOrderForm.quantity" :min="1" :max="99999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计划开始" prop="planStartDate">
          <el-date-picker
            v-model="workOrderForm.planStartDate"
            type="date"
            placeholder="选择开始日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="计划完成" prop="planEndDate">
          <el-date-picker
            v-model="workOrderForm.planEndDate"
            type="date"
            placeholder="选择完成日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="负责人" prop="manager">
          <el-input v-model="workOrderForm.manager" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="workOrderForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="工单详情" width="700px">
      <el-descriptions :column="2" border style="margin-bottom: 20px" v-if="currentWorkOrder">
        <el-descriptions-item label="工单号">{{ currentWorkOrder.workNo }}</el-descriptions-item>
        <el-descriptions-item label="工单状态">
          <el-tag :type="getStatusType(currentWorkOrder.status)" size="small">
            {{ getStatusText(currentWorkOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ currentWorkOrder.productName }}</el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ currentWorkOrder.spec }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ currentWorkOrder.manager }}</el-descriptions-item>
        <el-descriptions-item label="计划数量">{{ currentWorkOrder.quantity }}</el-descriptions-item>
        <el-descriptions-item label="已完成数量">{{ currentWorkOrder.finishedQuantity || 0 }}</el-descriptions-item>
        <el-descriptions-item label="计划开始">{{ currentWorkOrder.planStartDate }}</el-descriptions-item>
        <el-descriptions-item label="计划完成">{{ currentWorkOrder.planEndDate }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentWorkOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentWorkOrder.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
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
  getWorkOrderList,
  getWorkOrderDetail,
  createWorkOrder,
  updateWorkOrder,
  deleteWorkOrder,
  startWorkOrder
} from '../../api/workOrder'
import { getProductAll } from '../../api/product'
import { getProductionPlanList } from '../../api/productionPlan'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const workOrderFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

const searchForm = reactive({
  workNo: '',
  productName: '',
  status: ''
})

const workOrderForm = reactive({
  id: null,
  planId: null,
  productId: null,
  productName: '',
  spec: '',
  quantity: 1,
  planStartDate: '',
  planEndDate: '',
  manager: '',
  remark: ''
})

const currentWorkOrder = ref(null)

const statusOptions = [
  { label: '待生产', value: 0 },
  { label: '生产中', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 }
]

const productList = ref([])
const planList = ref([])

const workOrderRules = {
  productId: [
    { required: true, message: '请选择产品', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入计划数量', trigger: 'blur' }
  ],
  planStartDate: [
    { required: true, message: '请选择计划开始日期', trigger: 'change' }
  ],
  planEndDate: [
    { required: true, message: '请选择计划完成日期', trigger: 'change' }
  ],
  manager: [
    { required: true, message: '请输入负责人', trigger: 'blur' }
  ]
}

function getStatusType(status) {
  const types = ['info', 'primary', 'success', 'danger']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['待生产', '生产中', '已完成', '已取消']
  return texts[status] || '未知'
}

function fetchData() {
  loading.value = true
  const params = {
    current: currentPage.value,
    size: pageSize.value
  }
  if (searchForm.workNo) {
    params.workNo = searchForm.workNo
  }
  if (searchForm.status !== '') {
    params.status = searchForm.status
  }

  getWorkOrderList(params).then(res => {
    if (res.code === 200) {
      const records = res.data.records || []
      if (searchForm.productName) {
        tableData.value = records.filter(item => 
          item.productName && item.productName.includes(searchForm.productName)
        )
        total.value = tableData.value.length
      } else {
        tableData.value = records
        total.value = res.data.total || 0
      }
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

function loadPlans() {
  getProductionPlanList({ current: 1, size: 100 }).then(res => {
    if (res.code === 200) {
      planList.value = res.data.records || []
    }
  })
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.workNo = ''
  searchForm.productName = ''
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
  workOrderForm.id = row.id
  workOrderForm.planId = row.planId || null
  workOrderForm.productId = row.productId
  workOrderForm.productName = row.productName
  workOrderForm.spec = row.spec
  workOrderForm.quantity = row.quantity
  workOrderForm.planStartDate = row.planStartDate
  workOrderForm.planEndDate = row.planEndDate
  workOrderForm.manager = row.manager
  workOrderForm.remark = row.remark
  dialogVisible.value = true
}

function handleView(row) {
  getWorkOrderDetail(row.id).then(res => {
    if (res.code === 200) {
      currentWorkOrder.value = res.data
      detailVisible.value = true
    }
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除工单「${row.workNo}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteWorkOrder(row.id).then(res => {
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
  ElMessageBox.confirm(`确定要开始生产工单「${row.workNo}」吗？`, '确认', {
    confirmButtonText: '开始生产',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => {
    startWorkOrder(row.id).then(res => {
      if (res.code === 200) {
        ElMessage.success('工单已开始生产')
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
    workOrderForm.productName = product.productName
    workOrderForm.spec = product.model || product.spec || ''
  }
}

function resetForm() {
  workOrderForm.id = null
  workOrderForm.planId = null
  workOrderForm.productId = null
  workOrderForm.productName = ''
  workOrderForm.spec = ''
  workOrderForm.quantity = 1
  workOrderForm.planStartDate = ''
  workOrderForm.planEndDate = ''
  workOrderForm.manager = ''
  workOrderForm.remark = ''
  if (workOrderFormRef.value) {
    workOrderFormRef.value.resetFields()
  }
}

async function handleSubmit() {
  if (!workOrderFormRef.value) return
  
  try {
    await workOrderFormRef.value.validate()
    submitLoading.value = true
    
    const api = isEdit.value ? updateWorkOrder : createWorkOrder
    const res = await api(workOrderForm)
    
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
  loadPlans()
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
