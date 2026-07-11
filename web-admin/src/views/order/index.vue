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
            <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable style="width: 180px" />
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
        <el-table-column prop="customerName" label="客户名称" />
        <el-table-column prop="productName" label="产品名称" />
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
        <el-table-column prop="deliveryDate" label="交货日期" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
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
          <el-select v-model="orderForm.customerId" placeholder="请选择客户" style="width: 100%">
            <el-option 
              v-for="item in customerList" 
              :key="item.id" 
              :label="item.customerName" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="产品名称" prop="productId">
          <el-select v-model="orderForm.productId" placeholder="请选择产品" style="width: 100%">
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
          />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number 
            v-model="orderForm.unitPrice" 
            :min="0" 
            :precision="2"
            :step="0.01"
            style="width: 100%" 
          />
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
        <el-descriptions-item label="客户名称">{{ currentOrder.customerName }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ currentOrder.productName }}</el-descriptions-item>
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
  customerName: '',
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

const mockOrders = ref([
  { id: 1, orderNo: 'ORD-2024001', customerId: 1, customerName: '深圳科技有限公司', productId: 1, productName: '智能手环Pro', quantity: 1000, unitPrice: 199.00, totalAmount: 199000.00, status: 0, deliveryDate: '2024-02-15', createTime: '2024-01-15 10:30:00', remark: '首批订单，请注意质量' },
  { id: 2, orderNo: 'ORD-2024002', customerId: 2, customerName: '北京数码公司', productId: 2, productName: '智能手环Lite', quantity: 500, unitPrice: 99.00, totalAmount: 49500.00, status: 1, deliveryDate: '2024-02-20', createTime: '2024-01-16 14:20:00', remark: '' },
  { id: 3, orderNo: 'ORD-2024003', customerId: 3, customerName: '上海电子科技', productId: 1, productName: '智能手环Pro', quantity: 2000, unitPrice: 189.00, totalAmount: 378000.00, status: 2, deliveryDate: '2024-02-10', createTime: '2024-01-10 09:15:00', remark: '大客户长期合作' },
  { id: 4, orderNo: 'ORD-2024004', customerId: 1, customerName: '深圳科技有限公司', productId: 3, productName: '智能手环Ultra', quantity: 300, unitPrice: 299.00, totalAmount: 89700.00, status: 3, deliveryDate: '2024-01-20', createTime: '2024-01-05 16:45:00', remark: '' },
  { id: 5, orderNo: 'ORD-2024005', customerId: 4, customerName: '广州贸易公司', productId: 2, productName: '智能手环Lite', quantity: 800, unitPrice: 95.00, totalAmount: 76000.00, status: 0, deliveryDate: '2024-02-25', createTime: '2024-01-17 11:00:00', remark: '出口订单' },
  { id: 6, orderNo: 'ORD-2024006', customerId: 5, customerName: '杭州智能设备', productId: 1, productName: '智能手环Pro', quantity: 1500, unitPrice: 195.00, totalAmount: 292500.00, status: 4, deliveryDate: '2024-02-05', createTime: '2024-01-08 08:30:00', remark: '客户取消订单' },
  { id: 7, orderNo: 'ORD-2024007', customerId: 2, customerName: '北京数码公司', productId: 3, productName: '智能手环Ultra', quantity: 200, unitPrice: 289.00, totalAmount: 57800.00, status: 1, deliveryDate: '2024-02-18', createTime: '2024-01-12 13:20:00', remark: '' },
  { id: 8, orderNo: 'ORD-2024008', customerId: 6, customerName: '成都电子厂', productId: 2, productName: '智能手环Lite', quantity: 1200, unitPrice: 98.00, totalAmount: 117600.00, status: 2, deliveryDate: '2024-02-12', createTime: '2024-01-14 10:10:00', remark: '' }
])

const customerList = ref([
  { id: 1, customerName: '深圳科技有限公司' },
  { id: 2, customerName: '北京数码公司' },
  { id: 3, customerName: '上海电子科技' },
  { id: 4, customerName: '广州贸易公司' },
  { id: 5, customerName: '杭州智能设备' },
  { id: 6, customerName: '成都电子厂' }
])

const productList = ref([
  { id: 1, productName: '智能手环Pro', unitPrice: 199.00 },
  { id: 2, productName: '智能手环Lite', unitPrice: 99.00 },
  { id: 3, productName: '智能手环Ultra', unitPrice: 299.00 }
])

const tableData = computed(() => {
  let data = [...mockOrders.value]
  
  if (searchForm.orderNo) {
    data = data.filter(item => item.orderNo.includes(searchForm.orderNo))
  }
  if (searchForm.customerName) {
    data = data.filter(item => item.customerName.includes(searchForm.customerName))
  }
  if (searchForm.status !== '') {
    data = data.filter(item => item.status === searchForm.status)
  }
  
  total.value = data.length
  
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return data.slice(start, end)
})

const dialogTitle = computed(() => isEdit.value ? '编辑订单' : '新增订单')

function getStatusType(status) {
  const types = ['warning', 'primary', 'success', 'info', 'danger']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['待审批', '已审批', '生产中', '已完成', '已取消']
  return texts[status] || '未知'
}

function loadData() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 300)
}

function handleSearch() {
  currentPage.value = 1
  loadData()
}

function handleReset() {
  searchForm.orderNo = ''
  searchForm.customerName = ''
  searchForm.status = ''
  currentPage.value = 1
  loadData()
}

function handlePageChange(page) {
  currentPage.value = page
}

function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
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
  orderForm.deliveryDate = row.deliveryDate
  orderForm.remark = row.remark
  dialogVisible.value = true
}

function handleView(row) {
  Object.assign(currentOrder, row)
  detailVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除订单「${row.orderNo}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = mockOrders.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      mockOrders.value.splice(index, 1)
    }
    ElMessage.success('删除成功')
  }).catch(() => {})
}

function handleApprove(row) {
  ElMessageBox.confirm(`确定要审批通过订单「${row.orderNo}」吗？`, '审批确认', {
    confirmButtonText: '通过',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => {
    const order = mockOrders.value.find(item => item.id === row.id)
    if (order) {
      order.status = 1
    }
    ElMessage.success('审批通过')
  }).catch(() => {})
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
    
    setTimeout(() => {
      const customer = customerList.value.find(c => c.id === orderForm.customerId)
      const product = productList.value.find(p => p.id === orderForm.productId)
      
      if (isEdit.value) {
        const order = mockOrders.value.find(o => o.id === orderForm.id)
        if (order) {
          order.customerId = orderForm.customerId
          order.customerName = customer?.customerName || ''
          order.productId = orderForm.productId
          order.productName = product?.productName || ''
          order.quantity = orderForm.quantity
          order.unitPrice = orderForm.unitPrice
          order.totalAmount = orderForm.quantity * orderForm.unitPrice
          order.deliveryDate = orderForm.deliveryDate
          order.remark = orderForm.remark
        }
        ElMessage.success('编辑成功')
      } else {
        const newId = Math.max(...mockOrders.value.map(o => o.id)) + 1
        const newOrder = {
          id: newId,
          orderNo: `ORD-2024${String(newId).padStart(3, '0')}`,
          customerId: orderForm.customerId,
          customerName: customer?.customerName || '',
          productId: orderForm.productId,
          productName: product?.productName || '',
          quantity: orderForm.quantity,
          unitPrice: orderForm.unitPrice,
          totalAmount: orderForm.quantity * orderForm.unitPrice,
          status: 0,
          deliveryDate: orderForm.deliveryDate,
          createTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
          remark: orderForm.remark
        }
        mockOrders.value.unshift(newOrder)
        ElMessage.success('新增成功')
      }
      
      submitLoading.value = false
      dialogVisible.value = false
    }, 500)
    
  } catch (error) {
    submitLoading.value = false
    console.error('表单验证失败:', error)
  }
}

onMounted(() => {
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
