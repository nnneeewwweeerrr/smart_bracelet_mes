<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="出库单号">
          <el-input v-model="searchForm.stockOutNo" placeholder="请输入出库单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="关联单号">
          <el-input v-model="searchForm.relatedNo" placeholder="请输入关联单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="出库类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 140px">
            <el-option label="销售出库" value="sale" />
            <el-option label="生产领料" value="production" />
            <el-option label="调拨出库" value="transfer" />
            <el-option label="其他出库" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
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
        <span class="table-title">出库记录列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增出库单
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="stockOutNo" label="出库单号" width="160" />
        <el-table-column prop="type" label="出库类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'sale'" type="primary">销售出库</el-tag>
            <el-tag v-else-if="row.type === 'production'" type="success">生产领料</el-tag>
            <el-tag v-else-if="row.type === 'transfer'" type="warning">调拨出库</el-tag>
            <el-tag v-else type="info">其他出库</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="relatedNo" label="关联单号" width="150" />
        <el-table-column prop="warehouse" label="出库仓库" width="110" />
        <el-table-column prop="itemCount" label="物料种类" width="90" align="center" />
        <el-table-column prop="totalQuantity" label="总数量" width="100" align="center" />
        <el-table-column prop="totalAmount" label="总金额（元）" width="130" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right" align="center">
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
              审核
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
      :title="isEdit ? '编辑出库单' : '新增出库单'"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="stockOutFormRef"
        :model="stockOutForm"
        :rules="stockOutRules"
        label-width="100px"
      >
        <el-form-item label="出库类型" prop="type">
          <el-select v-model="stockOutForm.type" placeholder="请选择出库类型" style="width: 100%">
            <el-option label="销售出库" value="sale" />
            <el-option label="生产领料" value="production" />
            <el-option label="调拨出库" value="transfer" />
            <el-option label="其他出库" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="出库仓库" prop="warehouse">
          <el-select v-model="stockOutForm.warehouse" placeholder="请选择仓库" style="width: 100%">
            <el-option label="原料仓" value="原料仓" />
            <el-option label="半成品仓" value="半成品仓" />
            <el-option label="成品仓" value="成品仓" />
            <el-option label="辅料仓" value="辅料仓" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联单号" prop="relatedNo">
          <el-input v-model="stockOutForm.relatedNo" placeholder="请输入关联单号（如工单号）" />
        </el-form-item>
        <el-form-item label="出库明细" prop="items">
          <div style="width: 100%">
            <el-table :data="stockOutForm.items" border style="width: 100%; margin-bottom: 10px">
              <el-table-column label="物料" min-width="200">
                <template #default="{ row, $index }">
                  <el-select v-model="row.materialId" placeholder="请选择物料" style="width: 100%" @change="handleMaterialChange($index)">
                    <el-option
                      v-for="item in materialList"
                      :key="item.id"
                      :label="item.name"
                      :value="item.id"
                    />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="数量" width="120">
                <template #default="{ row }">
                  <el-input-number v-model="row.quantity" :min="1" :max="99999" style="width: 100%" @change="calculateTotal" />
                </template>
              </el-table-column>
              <el-table-column label="单价（元）" width="140">
                <template #default="{ row }">
                  <el-input-number v-model="row.price" :min="0" :precision="2" :step="0.01" style="width: 100%" @change="calculateTotal" />
                </template>
              </el-table-column>
              <el-table-column label="小计（元）" width="120" align="right">
                <template #default="{ row }">
                  ¥{{ (row.quantity * row.price).toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center">
                <template #default="{ $index }">
                  <el-button type="danger" link @click="removeItem($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button type="primary" plain @click="addItem">
              <el-icon><Plus /></el-icon>
              添加物料
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="stockOutForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div class="dialog-footer-total">
        <span>合计金额：<span class="total-amount">¥{{ dialogTotalAmount.toFixed(2) }}</span></span>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="出库单详情" width="750px">
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="出库单号">{{ currentStockOut.stockOutNo }}</el-descriptions-item>
        <el-descriptions-item label="出库状态">
          <el-tag :type="getStatusType(currentStockOut.status)" size="small">
            {{ getStatusText(currentStockOut.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="出库类型">
          <el-tag v-if="currentStockOut.type === 'sale'" type="primary">销售出库</el-tag>
          <el-tag v-else-if="currentStockOut.type === 'production'" type="success">生产领料</el-tag>
          <el-tag v-else-if="currentStockOut.type === 'transfer'" type="warning">调拨出库</el-tag>
          <el-tag v-else type="info">其他出库</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="出库仓库">{{ currentStockOut.warehouse }}</el-descriptions-item>
        <el-descriptions-item label="关联单号">{{ currentStockOut.relatedNo || '无' }}</el-descriptions-item>
        <el-descriptions-item label="物料种类">{{ currentStockOut.itemCount }}</el-descriptions-item>
        <el-descriptions-item label="总数量">{{ currentStockOut.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="总金额">
          <span style="color: #f56c6c; font-weight: bold; font-size: 16px">
            ¥{{ currentStockOut.totalAmount?.toFixed(2) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentStockOut.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentStockOut.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <div class="detail-section-title">出库明细</div>
      <el-table :data="currentStockOut.items || []" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialName" label="物料名称" min-width="160" />
        <el-table-column prop="spec" label="规格型号" width="140" />
        <el-table-column prop="quantity" label="数量" width="100" align="center" />
        <el-table-column prop="price" label="单价（元）" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="小计（元）" width="120" align="right">
          <template #default="{ row }">
            ¥{{ (row.quantity * row.price).toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
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
const detailVisible = ref(false)
const isEdit = ref(false)
const stockOutFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  stockOutNo: '',
  relatedNo: '',
  type: '',
  status: ''
})

const stockOutForm = reactive({
  id: null,
  type: 'production',
  warehouse: '',
  relatedNo: '',
  items: [
    { materialId: null, materialName: '', spec: '', quantity: 1, price: 0 }
  ],
  remark: ''
})

const currentStockOut = reactive({})

const statusOptions = [
  { label: '待审核', value: 0 },
  { label: '已出库', value: 1 },
  { label: '已取消', value: 2 }
]

const stockOutRules = {
  type: [
    { required: true, message: '请选择出库类型', trigger: 'change' }
  ],
  warehouse: [
    { required: true, message: '请选择出库仓库', trigger: 'change' }
  ]
}

const materialList = ref([
  { id: 1, name: '主板芯片', spec: 'STM32F103', price: 15.50 },
  { id: 2, name: '电池', spec: '3.7V 200mAh', price: 8.00 },
  { id: 3, name: '塑胶外壳', spec: 'ABS 白色', price: 3.50 },
  { id: 4, name: '显示屏', spec: '0.96寸 OLED', price: 12.00 },
  { id: 5, name: '心率传感器', spec: 'MAX30102', price: 20.00 },
  { id: 6, name: '包装盒', spec: '彩盒 白色', price: 2.00 },
  { id: 7, name: '表带', spec: '硅胶 黑色', price: 5.50 },
  { id: 8, name: 'USB充电线', spec: 'Type-C 20cm', price: 3.00 },
  { id: 11, name: '智能健康手环 Pro', spec: 'SH-P001', price: 299.00 },
  { id: 12, name: '智能运动手环 Lite', spec: 'SH-L002', price: 199.00 }
])

const mockData = ref([
  {
    id: 1,
    stockOutNo: 'SO-2024001',
    type: 'production',
    relatedNo: 'WO-2024001',
    warehouse: '原料仓',
    itemCount: 3,
    totalQuantity: 2500,
    totalAmount: 47500.00,
    status: 1,
    createTime: '2024-01-12 09:00:00',
    remark: '生产领料',
    items: [
      { materialId: 1, materialName: '主板芯片', spec: 'STM32F103', quantity: 1000, price: 15.50 },
      { materialId: 2, materialName: '电池', spec: '3.7V 200mAh', quantity: 1000, price: 8.00 },
      { materialId: 4, materialName: '显示屏', spec: '0.96寸 OLED', quantity: 500, price: 12.00 }
    ]
  },
  {
    id: 2,
    stockOutNo: 'SO-2024002',
    type: 'sale',
    relatedNo: 'ORD-2024004',
    warehouse: '成品仓',
    itemCount: 1,
    totalQuantity: 300,
    totalAmount: 89700.00,
    status: 1,
    createTime: '2024-01-18 10:00:00',
    remark: '销售发货',
    items: [
      { materialId: 11, materialName: '智能健康手环 Pro', spec: 'SH-P001', quantity: 300, price: 299.00 }
    ]
  },
  {
    id: 3,
    stockOutNo: 'SO-2024003',
    type: 'production',
    relatedNo: 'WO-2024002',
    warehouse: '原料仓',
    itemCount: 2,
    totalQuantity: 3500,
    totalAmount: 46000.00,
    status: 0,
    createTime: '2024-01-18 14:00:00',
    remark: '',
    items: [
      { materialId: 2, materialName: '电池', spec: '3.7V 200mAh', quantity: 2000, price: 8.00 },
      { materialId: 5, materialName: '心率传感器', spec: 'MAX30102', quantity: 1500, price: 20.00 }
    ]
  },
  {
    id: 4,
    stockOutNo: 'SO-2024004',
    type: 'transfer',
    relatedNo: 'TR-2024002',
    warehouse: '半成品仓',
    itemCount: 1,
    totalQuantity: 500,
    totalAmount: 2750.00,
    status: 1,
    createTime: '2024-01-17 11:00:00',
    remark: '调拨到组装车间',
    items: [
      { materialId: 7, materialName: '表带', spec: '硅胶 黑色', quantity: 500, price: 5.50 }
    ]
  },
  {
    id: 5,
    stockOutNo: 'SO-2024005',
    type: 'sale',
    relatedNo: 'ORD-2024005',
    warehouse: '成品仓',
    itemCount: 1,
    totalQuantity: 200,
    totalAmount: 39800.00,
    status: 0,
    createTime: '2024-01-18 16:30:00',
    remark: '紧急订单',
    items: [
      { materialId: 12, materialName: '智能运动手环 Lite', spec: 'SH-L002', quantity: 200, price: 199.00 }
    ]
  },
  {
    id: 6,
    stockOutNo: 'SO-2024006',
    type: 'other',
    relatedNo: '',
    warehouse: '原料仓',
    itemCount: 1,
    totalQuantity: 50,
    totalAmount: 1000.00,
    status: 2,
    createTime: '2024-01-15 15:00:00',
    remark: '物料报废',
    items: [
      { materialId: 3, materialName: '塑胶外壳', spec: 'ABS 白色', quantity: 50, price: 20.00 }
    ]
  }
])

const tableData = ref([])

const dialogTotalAmount = computed(() => {
  return stockOutForm.items.reduce((sum, item) => sum + item.quantity * item.price, 0)
})

function getStatusType(status) {
  const types = ['warning', 'success', 'danger']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['待审核', '已出库', '已取消']
  return texts[status] || '未知'
}

onMounted(() => {
  fetchData()
})

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filtered = [...mockData.value]
    if (searchForm.stockOutNo) {
      filtered = filtered.filter(item => item.stockOutNo.includes(searchForm.stockOutNo))
    }
    if (searchForm.relatedNo) {
      filtered = filtered.filter(item => item.relatedNo.includes(searchForm.relatedNo))
    }
    if (searchForm.type) {
      filtered = filtered.filter(item => item.type === searchForm.type)
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
  searchForm.stockOutNo = ''
  searchForm.relatedNo = ''
  searchForm.type = ''
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
  stockOutForm.id = row.id
  stockOutForm.type = row.type
  stockOutForm.warehouse = row.warehouse
  stockOutForm.relatedNo = row.relatedNo
  stockOutForm.items = row.items.map(item => ({ ...item }))
  stockOutForm.remark = row.remark
  dialogVisible.value = true
}

function handleView(row) {
  Object.assign(currentStockOut, row)
  detailVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除出库单「${row.stockOutNo}」吗？`, '提示', {
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
  ElMessageBox.confirm(`确定要审核通过出库单「${row.stockOutNo}」吗？`, '审核确认', {
    confirmButtonText: '通过',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => {
    const order = mockData.value.find(item => item.id === row.id)
    if (order) {
      order.status = 1
    }
    ElMessage.success('审核通过，已出库')
    fetchData()
  }).catch(() => {})
}

function handleMaterialChange(index) {
  const material = materialList.value.find(m => m.id === stockOutForm.items[index].materialId)
  if (material) {
    stockOutForm.items[index].materialName = material.name
    stockOutForm.items[index].spec = material.spec
    stockOutForm.items[index].price = material.price
    calculateTotal()
  }
}

function addItem() {
  stockOutForm.items.push({
    materialId: null,
    materialName: '',
    spec: '',
    quantity: 1,
    price: 0
  })
}

function removeItem(index) {
  if (stockOutForm.items.length <= 1) {
    ElMessage.warning('至少保留一条明细')
    return
  }
  stockOutForm.items.splice(index, 1)
  calculateTotal()
}

function calculateTotal() {
}

function resetForm() {
  stockOutForm.id = null
  stockOutForm.type = 'production'
  stockOutForm.warehouse = ''
  stockOutForm.relatedNo = ''
  stockOutForm.items = [
    { materialId: null, materialName: '', spec: '', quantity: 1, price: 0 }
  ]
  stockOutForm.remark = ''
  if (stockOutFormRef.value) {
    stockOutFormRef.value.resetFields()
  }
}

async function handleSubmit() {
  if (!stockOutFormRef.value) return
  
  if (stockOutForm.items.length === 0) {
    ElMessage.warning('请至少添加一条出库明细')
    return
  }
  
  const hasInvalidItem = stockOutForm.items.some(item => !item.materialId || item.quantity <= 0)
  if (hasInvalidItem) {
    ElMessage.warning('请完善所有出库明细信息')
    return
  }
  
  try {
    await stockOutFormRef.value.validate()
    submitLoading.value = true
    
    setTimeout(() => {
      const itemCount = stockOutForm.items.length
      const totalQuantity = stockOutForm.items.reduce((sum, item) => sum + item.quantity, 0)
      const totalAmount = stockOutForm.items.reduce((sum, item) => sum + item.quantity * item.price, 0)
      
      if (isEdit.value) {
        const index = mockData.value.findIndex(item => item.id === stockOutForm.id)
        if (index !== -1) {
          mockData.value[index] = {
            ...mockData.value[index],
            type: stockOutForm.type,
            warehouse: stockOutForm.warehouse,
            relatedNo: stockOutForm.relatedNo,
            items: stockOutForm.items.map(item => ({ ...item })),
            itemCount,
            totalQuantity,
            totalAmount,
            remark: stockOutForm.remark
          }
        }
        ElMessage.success('编辑成功')
      } else {
        const maxId = Math.max(...mockData.value.map(item => item.id), 0)
        const newStockOut = {
          id: maxId + 1,
          stockOutNo: 'SO-2024' + String(maxId + 1).padStart(3, '0'),
          type: stockOutForm.type,
          warehouse: stockOutForm.warehouse,
          relatedNo: stockOutForm.relatedNo,
          items: stockOutForm.items.map(item => ({ ...item })),
          itemCount,
          totalQuantity,
          totalAmount,
          status: 0,
          createTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
          remark: stockOutForm.remark
        }
        mockData.value.unshift(newStockOut)
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

.dialog-footer-total {
  padding: 10px 20px;
  background-color: #f5f7fa;
  border-top: 1px solid #e4e7ed;
  text-align: right;
  font-size: 14px;
}

.total-amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}

.detail-section-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}
</style>
