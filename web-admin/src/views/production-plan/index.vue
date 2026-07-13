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
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增计划
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="planNo" label="计划编号" width="160" />
        <el-table-column prop="planName" label="计划名称" min-width="180" />
        <el-table-column prop="productName" label="产品名称" width="160" />
        <el-table-column prop="quantity" label="计划数量" width="100" align="center" />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button
              type="primary"
              link
              @click="handleEdit(row)"
              :disabled="row.status !== 0"
            >
              编辑
            </el-button>
            <el-button
              type="success"
              link
              @click="handleApprove(row)"
              :disabled="row.status !== 0"
            >
              审批
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
              :disabled="row.status !== 0"
            >
              删除
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
      v-model="dialogVisible"
      :title="dialogTitle"
      width="650px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="planFormRef"
        :model="planForm"
        :rules="planRules"
        label-width="100px"
        :disabled="isView"
      >
        <el-form-item label="计划名称" prop="planName">
          <el-input v-model="planForm.planName" placeholder="请输入计划名称" />
        </el-form-item>
        <el-form-item label="产品名称" prop="productId">
          <el-select v-model="planForm.productId" placeholder="请选择产品" style="width: 100%" @change="handleProductChange">
            <el-option
              v-for="item in productList"
              :key="item.id"
              :label="item.productName"
              :value="item.id"
            />
          </el-select>
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
        <el-form-item label="负责人" prop="manager">
          <el-input v-model="planForm.manager" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="planForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ isView ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false)
const planFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  planNo: '',
  planName: '',
  status: ''
})

const planForm = reactive({
  id: null,
  planNo: '',
  planName: '',
  productId: null,
  productName: '',
  quantity: 1,
  startDate: '',
  endDate: '',
  manager: '',
  remark: ''
})

const statusOptions = [
  { label: '待审批', value: 0 },
  { label: '已审批', value: 1 },
  { label: '执行中', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
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
  manager: [
    { required: true, message: '请输入负责人', trigger: 'blur' }
  ]
}

const productList = ref([
  { id: 1, productName: '智能手环Pro' },
  { id: 2, productName: '智能手环Lite' },
  { id: 3, productName: '智能手环Ultra' }
])

const mockData = ref([
  {
    id: 1,
    planNo: 'PP-202401001',
    planName: '1月上旬生产计划',
    productId: 1,
    productName: '智能手环Pro',
    quantity: 2000,
    startDate: '2024-01-01',
    endDate: '2024-01-10',
    status: 3,
    manager: '李主管',
    createTime: '2023-12-25 10:00:00',
    remark: '节前赶工'
  },
  {
    id: 2,
    planNo: 'PP-202401002',
    planName: '1月中旬生产计划',
    productId: 2,
    productName: '智能手环Lite',
    quantity: 1500,
    startDate: '2024-01-11',
    endDate: '2024-01-20',
    status: 2,
    manager: '王主管',
    createTime: '2023-12-28 14:30:00',
    remark: ''
  },
  {
    id: 3,
    planNo: 'PP-202401003',
    planName: '1月下旬生产计划',
    productId: 1,
    productName: '智能手环Pro',
    quantity: 1800,
    startDate: '2024-01-21',
    endDate: '2024-01-31',
    status: 1,
    manager: '张主管',
    createTime: '2024-01-05 09:00:00',
    remark: '订单增加，追加生产'
  },
  {
    id: 4,
    planNo: 'PP-202401004',
    planName: 'Ultra新品试产计划',
    productId: 3,
    productName: '智能手环Ultra',
    quantity: 500,
    startDate: '2024-01-15',
    endDate: '2024-01-25',
    status: 0,
    manager: '赵主管',
    createTime: '2024-01-10 16:00:00',
    remark: '新产品试产'
  },
  {
    id: 5,
    planNo: 'PP-202402001',
    planName: '2月上旬生产计划',
    productId: 2,
    productName: '智能手环Lite',
    quantity: 1000,
    startDate: '2024-02-01',
    endDate: '2024-02-10',
    status: 0,
    manager: '李主管',
    createTime: '2024-01-12 11:00:00',
    remark: ''
  }
])

const tableData = ref([])

const dialogTitle = computed(() => {
  if (isView.value) return '生产计划详情'
  return isEdit.value ? '编辑生产计划' : '新增生产计划'
})

function getStatusType(status) {
  const types = ['warning', 'primary', 'success', 'info', 'danger']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['待审批', '已审批', '执行中', '已完成', '已取消']
  return texts[status] || '未知'
}

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filtered = [...mockData.value]
    if (searchForm.planNo) {
      filtered = filtered.filter(item => item.planNo.includes(searchForm.planNo))
    }
    if (searchForm.planName) {
      filtered = filtered.filter(item => item.planName.includes(searchForm.planName))
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
  isView.value = false
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  isView.value = false
  planForm.id = row.id
  planForm.planNo = row.planNo
  planForm.planName = row.planName
  planForm.productId = row.productId
  planForm.productName = row.productName
  planForm.quantity = row.quantity
  planForm.startDate = row.startDate
  planForm.endDate = row.endDate
  planForm.manager = row.manager
  planForm.remark = row.remark
  dialogVisible.value = true
}

function handleView(row) {
  isEdit.value = false
  isView.value = true
  planForm.id = row.id
  planForm.planNo = row.planNo
  planForm.planName = row.planName
  planForm.productId = row.productId
  planForm.productName = row.productName
  planForm.quantity = row.quantity
  planForm.startDate = row.startDate
  planForm.endDate = row.endDate
  planForm.manager = row.manager
  planForm.remark = row.remark
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除生产计划「${row.planNo}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = mockData.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      mockData.value.splice(index, 1)
    }
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

function handleApprove(row) {
  ElMessageBox.confirm(`确定要审批通过生产计划「${row.planNo}」吗？`, '审批确认', {
    confirmButtonText: '通过',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => {
    const plan = mockData.value.find(item => item.id === row.id)
    if (plan) {
      plan.status = 1
    }
    ElMessage.success('审批通过')
    fetchData()
  }).catch(() => {})
}

function handleProductChange(val) {
  const product = productList.value.find(p => p.id === val)
  if (product) {
    planForm.productName = product.productName
  }
}

function resetForm() {
  planForm.id = null
  planForm.planNo = ''
  planForm.planName = ''
  planForm.productId = null
  planForm.productName = ''
  planForm.quantity = 1
  planForm.startDate = ''
  planForm.endDate = ''
  planForm.manager = ''
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
    
    setTimeout(() => {
      if (isEdit.value) {
        const index = mockData.value.findIndex(item => item.id === planForm.id)
        if (index !== -1) {
          mockData.value[index] = {
            ...mockData.value[index],
            planName: planForm.planName,
            productId: planForm.productId,
            productName: planForm.productName,
            quantity: planForm.quantity,
            startDate: planForm.startDate,
            endDate: planForm.endDate,
            manager: planForm.manager,
            remark: planForm.remark
          }
        }
        ElMessage.success('编辑成功')
      } else {
        const maxId = Math.max(...mockData.value.map(item => item.id), 0)
        const newPlan = {
          id: maxId + 1,
          planNo: 'PP-2024' + String(maxId + 1).padStart(5, '0'),
          planName: planForm.planName,
          productId: planForm.productId,
          productName: planForm.productName,
          quantity: planForm.quantity,
          startDate: planForm.startDate,
          endDate: planForm.endDate,
          status: 0,
          manager: planForm.manager,
          createTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
          remark: planForm.remark
        }
        mockData.value.unshift(newPlan)
        ElMessage.success('新增成功')
      }
      
      dialogVisible.value = false
      submitLoading.value = false
      fetchData()
    }, 300)
  } catch (error) {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchData()
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
