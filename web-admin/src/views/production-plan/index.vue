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
  { id: 1, productName: '智能健康手环 Pro' },
  { id: 2, productName: '智能运动手环 Lite' },
  { id: 3, productName: '智能手表 Max' },
  { id: 4, productName: '智能心率手环' },
  { id: 5, productName: '智能睡眠手环' }
])

const mockData = ref([
  { id: 1, planNo: 'PP-2026001', planName: 'Q3健康手环生产计划', productName: '智能健康手环 Pro', productId: 1, quantity: 5000, startDate: '2026-07-01', endDate: '2026-09-30', manager: '张工', remark: '季度生产计划', status: 2, createTime: '2026-06-25 10:00:00' },
  { id: 2, planNo: 'PP-2026002', planName: 'Q3运动手环生产计划', productName: '智能运动手环 Lite', productId: 2, quantity: 3000, startDate: '2026-07-01', endDate: '2026-09-30', manager: '李工', remark: '季度生产计划', status: 2, createTime: '2026-06-25 10:30:00' },
  { id: 3, planNo: 'PP-2026003', planName: '智能手表Max首产计划', productName: '智能手表 Max', productId: 3, quantity: 2000, startDate: '2026-07-15', endDate: '2026-08-31', manager: '王工', remark: '新产品量产计划', status: 1, createTime: '2026-07-01 14:00:00' },
  { id: 4, planNo: 'PP-2026004', planName: '心率手环增量计划', productName: '智能心率手环', productId: 4, quantity: 1500, startDate: '2026-07-20', endDate: '2026-08-20', manager: '赵工', remark: '市场需求增加', status: 0, createTime: '2026-07-10 09:00:00' },
  { id: 5, planNo: 'PP-2026005', planName: '睡眠手环试产计划', productName: '智能睡眠手环', productId: 5, quantity: 500, startDate: '2026-07-10', endDate: '2026-07-25', manager: '刘工', remark: '试产验证', status: 3, createTime: '2026-07-05 16:00:00' },
  { id: 6, planNo: 'PP-2026006', planName: '健康手环加急计划', productName: '智能健康手环 Pro', productId: 1, quantity: 1000, startDate: '2026-07-12', endDate: '2026-07-18', manager: '张工', remark: '紧急订单', status: 2, createTime: '2026-07-10 11:00:00' },
  { id: 7, planNo: 'PP-2026007', planName: 'Q4产品储备计划', productName: '智能健康手环 Pro', productId: 1, quantity: 8000, startDate: '2026-10-01', endDate: '2026-12-31', manager: '孙工', remark: 'Q4备货', status: 0, createTime: '2026-07-08 08:30:00' },
  { id: 8, planNo: 'PP-2026008', planName: 'Lite版本迭代计划', productName: '智能运动手环 Lite', productId: 2, quantity: 4000, startDate: '2026-08-01', endDate: '2026-10-31', manager: '周工', remark: '新版本生产', status: 1, createTime: '2026-07-05 13:00:00' },
  { id: 9, planNo: 'PP-2026009', planName: '心率手环海外计划', productName: '智能心率手环', productId: 4, quantity: 2000, startDate: '2026-07-25', endDate: '2026-08-30', manager: '吴工', remark: '海外订单', status: 0, createTime: '2026-07-12 10:00:00' },
  { id: 10, planNo: 'PP-2026010', planName: '智能手表Max扩产计划', productName: '智能手表 Max', productId: 3, quantity: 3000, startDate: '2026-09-01', endDate: '2026-10-31', manager: '郑工', remark: '首产成功后扩产', status: 0, createTime: '2026-07-14 15:00:00' },
  { id: 11, planNo: 'PP-2026011', planName: '睡眠手环量产计划', productName: '智能睡眠手环', productId: 5, quantity: 2500, startDate: '2026-08-01', endDate: '2026-09-30', manager: '陈工', remark: '试产成功后量产', status: 1, createTime: '2026-07-13 09:00:00' },
  { id: 12, planNo: 'PP-2026012', planName: '内部测试计划', productName: '智能健康手环 Pro', productId: 1, quantity: 100, startDate: '2026-07-18', endDate: '2026-07-20', manager: '黄工', remark: '内部测试用', status: 4, createTime: '2026-07-15 11:00:00' }
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
  try {
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
  } catch (error) {
    console.error('Fetch production plans failed:', error)
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
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
    ElMessage.info('演示模式，无法操作')
  }).catch(() => {})
}

function handleApprove(row) {
  ElMessageBox.confirm(`确定要审批通过生产计划「${row.planNo}」吗？`, '审批确认', {
    confirmButtonText: '通过',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => {
    ElMessage.info('演示模式，无法操作')
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
    ElMessage.info('演示模式，无法操作')
  } catch (error) {
    ElMessage.error('表单验证失败')
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
