<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="采购单号">
          <el-input v-model="searchForm.purchaseNo" placeholder="请输入采购单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-select v-model="searchForm.supplierId" placeholder="全部供应商" clearable filterable style="width: 200px">
            <el-option v-for="item in supplierList" :key="item.id" :label="item.supplierName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="采购状态">
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
        <span class="table-title">采购单列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增采购单
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="purchaseNo" label="采购单号" width="160" />
        <el-table-column prop="supplierName" label="供应商名称" min-width="180">
          <template #default="{ row }">
            {{ getSupplierName(row.supplierId) }}
          </template>
        </el-table-column>
        <el-table-column prop="materialName" label="物料名称" min-width="150">
          <template #default="{ row }">
            {{ getMaterialName(row.materialId) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="采购数量" width="100" align="center" />
        <el-table-column prop="unitPrice" label="单价（元）" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.unitPrice?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额（元）" width="140" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">
            {{ row.createTime ? row.createTime.replace('T', ' ').substring(0, 19) : '-' }}
          </template>
        </el-table-column>
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
      :title="isEdit ? '编辑采购单' : '新增采购单'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="purchaseFormRef"
        :model="purchaseForm"
        :rules="purchaseRules"
        label-width="100px"
      >
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="purchaseForm.supplierId" placeholder="请选择供应商" style="width: 100%" filterable>
            <el-option
              v-for="item in supplierList"
              :key="item.id"
              :label="item.supplierName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="物料" prop="materialId">
          <el-select v-model="purchaseForm.materialId" placeholder="请选择物料" style="width: 100%" filterable @change="handleMaterialChange">
            <el-option
              v-for="item in materialList"
              :key="item.id"
              :label="item.materialName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="采购数量" prop="quantity">
          <el-input-number v-model="purchaseForm.quantity" :min="1" :max="99999" style="width: 100%" @change="calcTotal" />
        </el-form-item>
        <el-form-item label="单价（元）" prop="unitPrice">
          <el-input-number v-model="purchaseForm.unitPrice" :min="0" :precision="2" :step="0.01" style="width: 100%" @change="calcTotal" />
        </el-form-item>
        <el-form-item label="总金额">
          <span style="color: #f56c6c; font-size: 18px; font-weight: bold">
            ¥{{ (purchaseForm.quantity * purchaseForm.unitPrice).toFixed(2) }}
          </span>
        </el-form-item>
        <el-form-item label="预计到货日期" prop="expectedDate">
          <el-date-picker
            v-model="purchaseForm.expectedDate"
            type="date"
            placeholder="选择预计到货日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="purchaseForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="采购单详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="采购单号">{{ currentPurchase.purchaseNo }}</el-descriptions-item>
        <el-descriptions-item label="采购状态">
          <el-tag :type="getStatusType(currentPurchase.status)" size="small">
            {{ getStatusText(currentPurchase.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="供应商">{{ getSupplierName(currentPurchase.supplierId) }}</el-descriptions-item>
        <el-descriptions-item label="物料">{{ getMaterialName(currentPurchase.materialId) }}</el-descriptions-item>
        <el-descriptions-item label="采购数量">{{ currentPurchase.quantity }}</el-descriptions-item>
        <el-descriptions-item label="单价">¥{{ currentPurchase.unitPrice?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="总金额">
          <span style="color: #f56c6c; font-weight: bold; font-size: 16px">
            ¥{{ currentPurchase.totalAmount?.toFixed(2) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="预计到货">{{ currentPurchase.expectedDate }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentPurchase.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentPurchase.remark || '无' }}</el-descriptions-item>
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
import { getPurchaseList, createPurchase, updatePurchase, deletePurchase, approvePurchase } from '../../api/purchase'
import { getSupplierList } from '../../api/supplier'
import { getMaterialList } from '../../api/material'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const purchaseFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  purchaseNo: '',
  supplierId: '',
  status: ''
})

const purchaseForm = reactive({
  id: null,
  supplierId: null,
  materialId: null,
  quantity: 1,
  unitPrice: 0,
  expectedDate: '',
  remark: ''
})

const currentPurchase = reactive({})

const statusOptions = [
  { label: '待审批', value: 0 },
  { label: '已审批', value: 1 },
  { label: '已到货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
]

const purchaseRules = {
  supplierId: [
    { required: true, message: '请选择供应商', trigger: 'change' }
  ],
  materialId: [
    { required: true, message: '请选择物料', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入采购数量', trigger: 'blur' }
  ],
  unitPrice: [
    { required: true, message: '请输入单价', trigger: 'blur' }
  ]
}

const tableData = ref([])
const supplierList = ref([])
const materialList = ref([])

function getStatusType(status) {
  const types = ['warning', 'primary', 'success', 'info', 'danger']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['待审批', '已审批', '已到货', '已完成', '已取消']
  return texts[status] || '未知'
}

function getSupplierName(supplierId) {
  const supplier = supplierList.value.find(s => s.id === supplierId)
  return supplier?.supplierName || '-'
}

function getMaterialName(materialId) {
  const material = materialList.value.find(m => m.id === materialId)
  return material?.materialName || '-'
}

function handleMaterialChange(materialId) {
  const material = materialList.value.find(m => m.id === materialId)
  if (material && material.unitPrice) {
    purchaseForm.unitPrice = material.unitPrice
  }
}

function calcTotal() {
  // 总金额自动计算，在模板中显示
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      purchaseNo: searchForm.purchaseNo || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined,
      supplierId: searchForm.supplierId || undefined
    }
    const res = await getPurchaseList(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取采购列表失败:', error)
    ElMessage.error('获取采购列表失败')
  } finally {
    loading.value = false
  }
}

async function loadSupplierList() {
  try {
    const res = await getSupplierList({ pageNum: 1, pageSize: 100 })
    supplierList.value = res.data?.records || []
  } catch (error) {
    console.error('获取供应商列表失败:', error)
  }
}

async function loadMaterialList() {
  try {
    const res = await getMaterialList({ pageNum: 1, pageSize: 100 })
    materialList.value = res.data?.records || []
  } catch (error) {
    console.error('获取物料列表失败:', error)
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.purchaseNo = ''
  searchForm.supplierId = ''
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
  purchaseForm.id = row.id
  purchaseForm.supplierId = row.supplierId
  purchaseForm.materialId = row.materialId
  purchaseForm.quantity = row.quantity
  purchaseForm.unitPrice = row.unitPrice
  purchaseForm.expectedDate = row.expectedDate ? row.expectedDate.substring(0, 10) : ''
  purchaseForm.remark = row.remark || ''
  dialogVisible.value = true
}

function handleView(row) {
  Object.assign(currentPurchase, row)
  detailVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除采购单「${row.purchaseNo}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePurchase(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除采购单失败:', error)
    }
  }
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm(`确定要审批通过采购单「${row.purchaseNo}」吗？`, '审批确认', {
      confirmButtonText: '通过',
      cancelButtonText: '取消',
      type: 'success'
    })
    await approvePurchase(row.id)
    ElMessage.success('审批通过')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审批采购单失败:', error)
    }
  }
}

function resetForm() {
  purchaseForm.id = null
  purchaseForm.supplierId = null
  purchaseForm.materialId = null
  purchaseForm.quantity = 1
  purchaseForm.unitPrice = 0
  purchaseForm.expectedDate = ''
  purchaseForm.remark = ''
  if (purchaseFormRef.value) {
    purchaseFormRef.value.resetFields()
  }
}

async function handleSubmit() {
  if (!purchaseFormRef.value) return
  
  try {
    await purchaseFormRef.value.validate()
    submitLoading.value = true
    
    const data = {
      supplierId: purchaseForm.supplierId,
      materialId: purchaseForm.materialId,
      quantity: purchaseForm.quantity,
      unitPrice: purchaseForm.unitPrice,
      totalAmount: purchaseForm.quantity * purchaseForm.unitPrice,
      expectedDate: purchaseForm.expectedDate,
      remark: purchaseForm.remark
    }
    
    if (isEdit.value) {
      data.id = purchaseForm.id
      await updatePurchase(data)
      ElMessage.success('编辑成功')
    } else {
      await createPurchase(data)
      ElMessage.success('新增成功')
    }
    
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('表单提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadSupplierList()
  loadMaterialList()
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