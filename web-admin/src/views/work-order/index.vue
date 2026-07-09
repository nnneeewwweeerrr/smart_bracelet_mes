<template>
  <div>
    <el-form :model="query" inline class="search-form">
      <el-form-item label="工单号">
        <el-input v-model="query.workNo" placeholder="请输入工单号" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="产品">
        <el-select v-model="query.productId" placeholder="请选择产品" filterable clearable style="width: 180px">
          <el-option v-for="item in productList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="success" @click="handleAdd">新增工单</el-button>
        <el-button type="warning" @click="handleBatchGenerate">批量生成</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="workNo" label="工单号" width="170" />
      <el-table-column prop="planNo" label="计划号" width="170" />
      <el-table-column label="产品" min-width="140">
        <template #default="{ row }">{{ productMap[row.productId] || row.productName }}</template>
      </el-table-column>
      <el-table-column label="工序" min-width="120">
        <template #default="{ row }">{{ processMap[row.processId] || row.processName }}</template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="90" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
      <el-table-column label="操作" width="260" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" link type="primary" @click="handleDispatch(row)">派发</el-button>
          <el-button v-if="row.status === 1" link type="success" @click="handleComplete(row)">结案</el-button>
          <el-button link type="info" @click="handleViewReport(row)">查看报工</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无数据" />
      </template>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="生产计划" prop="planId">
          <el-select v-model="form.planId" placeholder="请选择生产计划" filterable style="width: 100%">
            <el-option v-for="item in planList" :key="item.id" :label="item.planNo" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择产品" filterable style="width: 100%" @change="handleProductChange">
            <el-option v-for="item in productList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="工序" prop="processId">
          <el-select v-model="form.processId" placeholder="请选择工序" filterable style="width: 100%">
            <el-option v-for="item in processList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchDialogVisible" title="批量生成工单" width="460px">
      <el-form :model="batchForm" label-width="90px">
        <el-form-item label="生产计划">
          <el-select v-model="batchForm.planId" placeholder="请选择生产计划" filterable style="width: 100%">
            <el-option v-for="item in planList" :key="item.id" :label="item.planNo" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchSubmitting" @click="handleBatchSubmit">生成</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reportDialogVisible" title="报工记录" width="700px">
      <el-table :data="reportList" v-loading="reportLoading" border>
        <el-table-column prop="reportDate" label="报工日期" width="130" align="center" />
        <el-table-column label="工序" min-width="120">
          <template #default="{ row }">{{ processMap[row.processId] || row.processName }}</template>
        </el-table-column>
        <el-table-column prop="workerId" label="工人ID" width="100" align="center" />
        <el-table-column prop="goodQuantity" label="良品数" width="90" align="center" />
        <el-table-column prop="badQuantity" label="不良品数" width="90" align="center" />
      </el-table>
      <template #footer>
        <el-button @click="reportDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getList, save, remove, dispatch, complete, generateFromPlan } from '../../api/workOrder'
import { getList as getPlanList } from '../../api/productionPlan'
import { getList as getProductList } from '../../api/product'
import { getByProductId } from '../../api/process'
import { getList as getReportList } from '../../api/productionReport'

const statusOptions = [
  { value: 0, label: '待派发' },
  { value: 1, label: '执行中' },
  { value: 2, label: '已完成' }
]

const statusText = (status) => {
  const item = statusOptions.find(s => s.value === status)
  return item ? item.label : '未知'
}
const statusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success' }
  return map[status] || 'info'
}

const query = reactive({ page: 1, size: 10, workNo: '', status: '', productId: '', planId: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)

const planList = ref([])
const productList = ref([])
const processList = ref([])
const productMap = computed(() => Object.fromEntries(productList.value.map(i => [i.id, i.name])))
const processMap = computed(() => Object.fromEntries(processList.value.map(i => [i.id, i.name])))

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getList({ ...query })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  fetchList()
}

const handleReset = () => {
  query.workNo = ''
  query.status = ''
  query.productId = ''
  query.planId = ''
  query.page = 1
  fetchList()
}

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  planId: null,
  productId: null,
  processId: null,
  quantity: 1
})

const rules = {
  productId: [{ required: true, message: '请选择产品', trigger: 'change' }],
  processId: [{ required: true, message: '请选择工序', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑工单' : '新增工单')

const resetForm = () => {
  form.id = null
  form.planId = null
  form.productId = null
  form.processId = null
  form.quantity = 1
}

const handleProductChange = async (productId) => {
  if (productId) {
    try {
      const res = await getByProductId(productId)
      processList.value = res.data || []
    } catch (e) {
      console.error(e)
    }
  } else {
    processList.value = []
  }
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await save({ ...form })
    ElMessage.success('新增成功')
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const handleDispatch = (row) => {
  ElMessageBox.confirm(`确定派发工单「${row.workNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await dispatch(row.id)
      ElMessage.success('派发成功')
      fetchList()
    }).catch(() => {})
}

const handleComplete = (row) => {
  ElMessageBox.confirm(`确定结案工单「${row.workNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await complete(row.id)
      ElMessage.success('结案成功')
      fetchList()
    }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除工单「${row.workNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await remove(row.id)
      ElMessage.success('删除成功')
      fetchList()
    }).catch(() => {})
}

const batchDialogVisible = ref(false)
const batchSubmitting = ref(false)
const batchForm = reactive({ planId: null })

const handleBatchGenerate = () => {
  batchForm.planId = null
  batchDialogVisible.value = true
}

const handleBatchSubmit = async () => {
  if (!batchForm.planId) {
    ElMessage.warning('请选择生产计划')
    return
  }
  batchSubmitting.value = true
  try {
    await generateFromPlan(batchForm.planId)
    ElMessage.success('批量生成成功')
    batchDialogVisible.value = false
    fetchList()
  } catch (e) {
    console.error(e)
  } finally {
    batchSubmitting.value = false
  }
}

const reportDialogVisible = ref(false)
const reportList = ref([])
const reportLoading = ref(false)

const handleViewReport = async (row) => {
  reportDialogVisible.value = true
  reportLoading.value = true
  try {
    const res = await getReportList({ workOrderId: row.id })
    reportList.value = res.data.records || res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    reportLoading.value = false
  }
}

const loadOptions = async () => {
  try {
    const [plans, products] = await Promise.all([
      getPlanList({ page: 1, size: 100 }),
      getProductList({})
    ])
    planList.value = plans.data.records || plans.data || []
    productList.value = products.data || []
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadOptions()
  fetchList()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 4px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
