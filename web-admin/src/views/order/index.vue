<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <el-button type="primary" size="small" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增订单
          </el-button>
        </div>
      </template>
      
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="订单号">
            <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable style="width: 180px" />
          </el-form-item>
          <el-form-item label="客户名称">
            <el-select v-model="searchForm.customerId" placeholder="全部客户" clearable filterable style="width: 200px">
              <el-option v-for="item in customerList" :key="item.id" :label="item.customerName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 150px">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" style="width: 100%" border v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="orderNo" label="订单编号" width="160" />
        <el-table-column prop="customerName" label="客户名称" min-width="150">
          <template #default="{ row }">
            {{ getCustomerName(row.customerId) }}
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="产品名称" min-width="150">
          <template #default="{ row }">
            {{ getProductName(row.productId) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="unitPrice" label="单价(元)" width="120">
          <template #default="{ row }">
            ¥{{ row.unitPrice?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额(元)" width="140">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deliveryDate" label="交货日期" width="120">
          <template #default="{ row }">
            {{ row.deliveryDate ? row.deliveryDate.substring(0, 10) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">
            {{ row.createTime ? row.createTime.replace('T', ' ').substring(0, 19) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              link 
              type="primary" 
              size="small" 
              @click="handleEdit(row)"
              :disabled="row.status !== 0"
            >
              编辑
            </el-button>
            <el-button 
              link 
              type="success" 
              size="small" 
              @click="handleApprove(row)"
              :disabled="row.status !== 0"
            >
              审批
            </el-button>
            <el-button 
              link 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
              :disabled="row.status !== 0"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="orderFormRef"
        :model="orderForm"
        :rules="orderRules"
        label-width="100px"
      >
        <el-form-item label="客户名称" prop="customerId">
          <el-select v-model="orderForm.customerId" placeholder="请选择客户" style="width: 100%" filterable>
            <el-option 
              v-for="item in customerList" 
              :key="item.id" 
              :label="item.customerName" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="产品名称" prop="productId">
          <el-select v-model="orderForm.productId" placeholder="请选择产品" style="width: 100%" filterable @change="handleProductChange">
            <el-option 
              v-for="item in productList" 
              :key="item.id" 
              :label="item.productName" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="订单数量" prop="quantity">
          <el-input-number 
            v-model="orderForm.quantity" 
            :min="1" 
            :max="99999"
            style="width: 100%" 
            @change="calcTotal"
          />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number 
            v-model="orderForm.unitPrice" 
            :min="0" 
            :precision="2"
            :step="0.01"
            style="width: 100%" 
            @change="calcTotal"
          />
        </el-form-item>
        <el-form-item label="总金额">
          <span style="color: #f56c6c; font-size: 18px; font-weight: bold">
            ¥{{ (orderForm.quantity * orderForm.unitPrice).toFixed(2) }}
          </span>
        </el-form-item>
        <el-form-item label="交货日期" prop="deliveryDate">
          <el-date-picker
            v-model="orderForm.deliveryDate"
            type="date"
            placeholder="选择交货日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="orderForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单编号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(currentOrder.status)" size="small">
            {{ getStatusText(currentOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ getCustomerName(currentOrder.customerId) }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ getProductName(currentOrder.productId) }}</el-descriptions-item>
        <el-descriptions-item label="订单数量">{{ currentOrder.quantity }}</el-descriptions-item>
        <el-descriptions-item label="单价">¥{{ currentOrder.unitPrice?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ currentOrder.totalAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="交货日期">{{ currentOrder.deliveryDate }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, createOrder, updateOrder, deleteOrder, approveOrder } from '../../api/order'
import { getCustomerList } from '../../api/customer'
import { getProductList } from '../../api/product'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const orderFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  orderNo: '',
  customerId: '',
  status: ''
})

const orderForm = reactive({
  id: null,
  customerId: null,
  productId: null,
  quantity: 1,
  unitPrice: 0,
  deliveryDate: '',
  remark: ''
})

const currentOrder = reactive({})

const orderRules = {
  customerId: [
    { required: true, message: '请选择客户', trigger: 'change' }
  ],
  productId: [
    { required: true, message: '请选择产品', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入订单数量', trigger: 'blur' }
  ],
  unitPrice: [
    { required: true, message: '请输入单价', trigger: 'blur' }
  ],
  deliveryDate: [
    { required: true, message: '请选择交货日期', trigger: 'change' }
  ]
}

const statusOptions = [
  { label: '待审批', value: 0 },
  { label: '已审批', value: 1 },
  { label: '生产中', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
]

const tableData = ref([])
const customerList = ref([])
const productList = ref([])

const dialogTitle = computed(() => isEdit.value ? '编辑订单' : '新增订单')

function getStatusType(status) {
  const types = ['warning', 'primary', 'success', 'info', 'danger']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['待审批', '已审批', '生产中', '已完成', '已取消']
  return texts[status] || '未知'
}

function getCustomerName(customerId) {
  const customer = customerList.value.find(c => c.id === customerId)
  return customer?.customerName || '-'
}

function getProductName(productId) {
  const product = productList.value.find(p => p.id === productId)
  return product?.productName || '-'
}

function handleProductChange(productId) {
  const product = productList.value.find(p => p.id === productId)
  if (product && product.unitPrice) {
    orderForm.unitPrice = product.unitPrice
  }
}

function calcTotal() {
  // 总金额自动计算，在模板中显示
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      orderNo: searchForm.orderNo || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined,
      customerId: searchForm.customerId || undefined
    }
    const res = await getOrderList(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

async function loadCustomerList() {
  try {
    const res = await getCustomerList({ current: 1, size: 100 })
    customerList.value = res.data?.records || res.data || []
  } catch (error) {
    console.error('获取客户列表失败:', error)
  }
}

async function loadProductList() {
  try {
    const res = await getProductList({ current: 1, size: 100 })
    productList.value = res.data?.records || res.data || []
  } catch (error) {
    console.error('获取产品列表失败:', error)
  }
}

function handleSearch() {
  currentPage.value = 1
  loadData()
}

function handleReset() {
  searchForm.orderNo = ''
  searchForm.customerId = ''
  searchForm.status = ''
  currentPage.value = 1
  loadData()
}

function handlePageChange(page) {
  currentPage.value = page
  loadData()
}

function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  orderForm.id = row.id
  orderForm.customerId = row.customerId
  orderForm.productId = row.productId
  orderForm.quantity = row.quantity
  orderForm.unitPrice = row.unitPrice
  orderForm.deliveryDate = row.deliveryDate ? row.deliveryDate.substring(0, 10) : ''
  orderForm.remark = row.remark || ''
  dialogVisible.value = true
}

function handleView(row) {
  Object.assign(currentOrder, row)
  detailVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除订单「${row.orderNo}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteOrder(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除订单失败:', error)
    }
  }
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(`确定要审批通过订单「${row.orderNo}」吗？`, '审批确认', {
      confirmButtonText: '通过',
      cancelButtonText: '取消',
      type: 'success'
    })
    await approveOrder(row.id)
    ElMessage.success('审批通过')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审批订单失败:', error)
    }
  }
}

function resetForm() {
  orderForm.id = null
  orderForm.customerId = null
  orderForm.productId = null
  orderForm.quantity = 1
  orderForm.unitPrice = 0
  orderForm.deliveryDate = ''
  orderForm.remark = ''
  if (orderFormRef.value) {
    orderFormRef.value.resetFields()
  }
}

async function handleSubmit() {
  if (!orderFormRef.value) return
  
  try {
    await orderFormRef.value.validate()
    submitLoading.value = true
    
    const data = {
      customerId: orderForm.customerId,
      productId: orderForm.productId,
      quantity: orderForm.quantity,
      unitPrice: orderForm.unitPrice,
      totalAmount: orderForm.quantity * orderForm.unitPrice,
      deliveryDate: orderForm.deliveryDate,
      remark: orderForm.remark
    }
    
    if (isEdit.value) {
      data.id = orderForm.id
      await updateOrder(data)
      ElMessage.success('编辑成功')
    } else {
      await createOrder(data)
      ElMessage.success('新增成功')
    }
    
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('表单提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadCustomerList()
  loadProductList()
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>