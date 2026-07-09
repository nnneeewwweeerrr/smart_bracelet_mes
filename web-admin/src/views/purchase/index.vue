<template>
  <div>
    <el-form :model="query" inline class="search-form">
      <el-form-item label="采购单号">
        <el-input v-model="query.purchaseNo" placeholder="请输入采购单号" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="success" @click="handleAdd">新增采购单</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="purchaseNo" label="采购单号" width="170" />
      <el-table-column label="供应商名称" min-width="140">
        <template #default="{ row }">{{ row.supplierName || supplierMap[row.supplierId] }}</template>
      </el-table-column>
      <el-table-column label="物料名称" min-width="140">
        <template #default="{ row }">{{ row.materialName || materialMap[row.materialId] }}</template>
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
      <el-table-column prop="expectedDate" label="预计到货日期" width="130" align="center" />
      <el-table-column label="操作" width="240" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" link type="warning" @click="handleAudit(row)">审核</el-button>
          <el-button v-if="row.status === 1" link type="primary" @click="handleArrive(row)">到货登记</el-button>
          <el-button v-if="row.status === 2" link type="success" @click="handleComplete(row)">完成</el-button>
          <el-button v-if="row.status !== 3 && row.status !== 0" link type="info" @click="handleCancel(row)">取消</el-button>
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

    <el-dialog v-model="dialogVisible" title="新增采购单" width="560px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="form.supplierId" placeholder="请选择供应商" filterable style="width: 100%">
            <el-option v-for="item in supplierList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="物料" prop="materialId">
          <el-select v-model="form.materialId" placeholder="请选择物料" filterable style="width: 100%">
            <el-option v-for="item in materialList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="form.unitPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预计到货日期" prop="expectedDate">
          <el-date-picker v-model="form.expectedDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择预计到货日期" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPurchaseList, savePurchase, deletePurchase,
  auditPurchase, arrivePurchase, completePurchase, cancelPurchase
} from '../../api/purchase'
import { getSupplierList } from '../../api/supplier'
import { getMaterialList } from '../../api/material'

const statusOptions = [
  { value: 0, label: '待审批' },
  { value: 1, label: '已审批' },
  { value: 2, label: '已到货' },
  { value: 3, label: '已完成' }
]

const statusText = (status) => {
  const item = statusOptions.find(s => s.value === status)
  return item ? item.label : '未知'
}
const statusType = (status) => {
  const map = { 0: 'info', 1: 'primary', 2: 'warning', 3: 'success' }
  return map[status] || 'info'
}

const query = reactive({ page: 1, size: 10, purchaseNo: '', status: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)

const supplierList = ref([])
const materialList = ref([])
const supplierMap = computed(() => Object.fromEntries(supplierList.value.map(i => [i.id, i.name])))
const materialMap = computed(() => Object.fromEntries(materialList.value.map(i => [i.id, i.name])))

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getPurchaseList({ ...query })
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
  query.purchaseNo = ''
  query.status = ''
  query.page = 1
  fetchList()
}

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  supplierId: null,
  materialId: null,
  quantity: 1,
  unitPrice: 0,
  expectedDate: ''
})

const rules = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  materialId: [{ required: true, message: '请选择物料', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  expectedDate: [{ required: true, message: '请选择预计到货日期', trigger: 'change' }]
}

const resetForm = () => {
  form.supplierId = null
  form.materialId = null
  form.quantity = 1
  form.unitPrice = 0
  form.expectedDate = ''
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await savePurchase({ ...form })
    ElMessage.success('新增成功')
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const handleAudit = (row) => {
  ElMessageBox.confirm(`确定审核通过采购单「${row.purchaseNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await auditPurchase(row.id)
      ElMessage.success('审核成功')
      fetchList()
    }).catch(() => {})
}

const handleArrive = (row) => {
  ElMessageBox.confirm(`确定对采购单「${row.purchaseNo}」进行到货登记吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await arrivePurchase(row.id)
      ElMessage.success('到货登记成功')
      fetchList()
    }).catch(() => {})
}

const handleComplete = (row) => {
  ElMessageBox.confirm(`确定完成采购单「${row.purchaseNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await completePurchase(row.id)
      ElMessage.success('完成成功')
      fetchList()
    }).catch(() => {})
}

const handleCancel = (row) => {
  ElMessageBox.confirm(`确定取消采购单「${row.purchaseNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await cancelPurchase(row.id)
      ElMessage.success('取消成功')
      fetchList()
    }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除采购单「${row.purchaseNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deletePurchase(row.id)
      ElMessage.success('删除成功')
      fetchList()
    }).catch(() => {})
}

const loadOptions = async () => {
  try {
    const [s, m] = await Promise.all([getSupplierList({}), getMaterialList({})])
    supplierList.value = s.data || []
    materialList.value = m.data || []
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
