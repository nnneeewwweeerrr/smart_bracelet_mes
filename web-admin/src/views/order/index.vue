<template>
  <div>
    <el-form :model="query" inline class="search-form">
      <el-form-item label="订单号">
        <el-input v-model="query.orderNo" placeholder="请输入订单号" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="success" @click="handleAdd">新增订单</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="orderNo" label="订单号" width="170" />
      <el-table-column label="客户名称" min-width="140">
        <template #default="{ row }">{{ row.customerName || customerMap[row.customerId] }}</template>
      </el-table-column>
      <el-table-column label="产品名称" min-width="140">
        <template #default="{ row }">{{ row.productName || productMap[row.productId] }}</template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="90" align="center" />
      <el-table-column label="单价" width="110" align="right">
        <template #default="{ row }">{{ Number(row.unitPrice).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="总金额" width="120" align="right">
        <template #default="{ row }">{{ Number(row.totalAmount).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deliveryDate" label="交货日期" width="130" align="center" />
      <el-table-column label="操作" width="230" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button v-if="row.status === 0" link type="warning" @click="handleAudit(row)">审核</el-button>
          <el-button v-if="row.status === 1" link type="primary" @click="handleDispatch(row)">派工</el-button>
          <el-button v-if="row.status !== 4 && row.status !== 3" link type="info" @click="handleCancel(row)">取消</el-button>
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
      <el-descriptions v-if="isView" :column="2" border>
        <el-descriptions-item label="订单号">{{ current.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(current.status)">{{ statusText(current.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ current.customerName || customerMap[current.customerId] }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ current.productName || productMap[current.productId] }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ current.quantity }}</el-descriptions-item>
        <el-descriptions-item label="单价">{{ Number(current.unitPrice || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="总金额">{{ Number(current.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="交货日期">{{ current.deliveryDate }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-else :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="form.customerId" placeholder="请选择客户" filterable style="width: 100%">
            <el-option v-for="item in customerList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择产品" filterable style="width: 100%">
            <el-option v-for="item in productList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="form.unitPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="交货日期" prop="deliveryDate">
          <el-date-picker v-model="form.deliveryDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择交货日期" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOrderList, getOrderDetail, saveOrder, updateOrder,
  deleteOrder, auditOrder, dispatchOrder, cancelOrder
} from '../../api/order'
import { getCustomerList } from '../../api/customer'
import { getProductList } from '../../api/product'

const statusOptions = [
  { value: 0, label: '待审批' },
  { value: 1, label: '已审批' },
  { value: 2, label: '生产中' },
  { value: 3, label: '已完成' },
  { value: 4, label: '已取消' }
]

const statusText = (status) => {
  const item = statusOptions.find(s => s.value === status)
  return item ? item.label : '未知'
}
const statusType = (status) => {
  const map = { 0: 'info', 1: 'primary', 2: 'warning', 3: 'success', 4: 'danger' }
  return map[status] || 'info'
}

const query = reactive({ page: 1, size: 10, orderNo: '', status: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)

const customerList = ref([])
const productList = ref([])
const customerMap = computed(() => Object.fromEntries(customerList.value.map(i => [i.id, i.name])))
const productMap = computed(() => Object.fromEntries(productList.value.map(i => [i.id, i.name])))

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getOrderList({ ...query })
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
  query.orderNo = ''
  query.status = ''
  query.page = 1
  fetchList()
}

const dialogVisible = ref(false)
const isView = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const current = ref({})
const form = reactive({
  id: null,
  customerId: null,
  productId: null,
  quantity: 1,
  unitPrice: 0,
  deliveryDate: ''
})

const rules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  productId: [{ required: true, message: '请选择产品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  deliveryDate: [{ required: true, message: '请选择交货日期', trigger: 'change' }]
}

const dialogTitle = computed(() => {
  if (isView.value) return '订单详情'
  return isEdit.value ? '编辑订单' : '新增订单'
})

const resetForm = () => {
  form.id = null
  form.customerId = null
  form.productId = null
  form.quantity = 1
  form.unitPrice = 0
  form.deliveryDate = ''
}

const handleAdd = () => {
  isView.value = false
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isView.value = false
  isEdit.value = true
  form.id = row.id
  form.customerId = row.customerId
  form.productId = row.productId
  form.quantity = row.quantity
  form.unitPrice = row.unitPrice
  form.deliveryDate = row.deliveryDate
  dialogVisible.value = true
}

const handleView = async (row) => {
  try {
    const res = await getOrderDetail(row.id)
    current.value = res.data || row
    isView.value = true
    dialogVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateOrder({ ...form })
      ElMessage.success('修改成功')
    } else {
      await saveOrder({ ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const handleAudit = (row) => {
  ElMessageBox.confirm(`确定审核通过订单「${row.orderNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await auditOrder(row.id)
      ElMessage.success('审核成功')
      fetchList()
    }).catch(() => {})
}

const handleDispatch = (row) => {
  ElMessageBox.confirm(`确定派工订单「${row.orderNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await dispatchOrder(row.id)
      ElMessage.success('派工成功')
      fetchList()
    }).catch(() => {})
}

const handleCancel = (row) => {
  ElMessageBox.confirm(`确定取消订单「${row.orderNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await cancelOrder(row.id)
      ElMessage.success('取消成功')
      fetchList()
    }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除订单「${row.orderNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteOrder(row.id)
      ElMessage.success('删除成功')
      fetchList()
    }).catch(() => {})
}

const loadOptions = async () => {
  try {
    const [c, p] = await Promise.all([getCustomerList({}), getProductList({})])
    customerList.value = c.data || []
    productList.value = p.data || []
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
