<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="物料编号">
          <el-input v-model="searchForm.materialNo" placeholder="请输入物料编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="物料名称">
          <el-input v-model="searchForm.materialName" placeholder="请输入物料名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="物料类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 140px">
            <el-option label="电子件" value="电子件" />
            <el-option label="配件" value="配件" />
            <el-option label="结构件" value="结构件" />
            <el-option label="包材" value="包材" />
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
        <span class="table-title">物料列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增物料
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialNo" label="物料编号" width="140" />
        <el-table-column prop="materialName" label="物料名称" min-width="160" />
        <el-table-column prop="type" label="物料类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === '电子件' ? 'danger' : row.type === '配件' ? 'warning' : row.type === '结构件' ? 'primary' : 'info'">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="spec" label="规格型号" width="140" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="price" label="单价（元）" width="120" align="right">
          <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="stockQty" label="库存" width="80" align="center" />
        <el-table-column prop="supplierName" label="供应商" width="160" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">
            {{ row.createTime ? row.createTime.replace('T', ' ').substring(0, 19) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
      :title="isEdit ? '编辑物料' : '新增物料'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="materialFormRef"
        :model="materialForm"
        :rules="materialRules"
        label-width="100px"
      >
        <el-form-item label="物料编号" prop="materialNo">
          <el-input v-model="materialForm.materialNo" placeholder="请输入物料编号" />
        </el-form-item>
        <el-form-item label="物料名称" prop="materialName">
          <el-input v-model="materialForm.materialName" placeholder="请输入物料名称" />
        </el-form-item>
        <el-form-item label="物料类型" prop="type">
          <el-select v-model="materialForm.type" placeholder="请选择物料类型" style="width: 100%">
            <el-option label="电子件" value="电子件" />
            <el-option label="配件" value="配件" />
            <el-option label="结构件" value="结构件" />
            <el-option label="包材" value="包材" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格型号" prop="spec">
          <el-input v-model="materialForm.spec" placeholder="请输入规格型号" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="materialForm.unit" placeholder="请输入单位（如：个、kg）" style="width: 50%" />
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number v-model="materialForm.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存数量" prop="stockQty">
          <el-input-number v-model="materialForm.stockQty" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="安全库存" prop="safetyStock">
          <el-input-number v-model="materialForm.safetyStock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="materialForm.supplierId" placeholder="请选择供应商" style="width: 100%" filterable>
            <el-option v-for="item in supplierList" :key="item.id" :label="item.supplierName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="materialForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="materialForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="物料详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="物料编号">{{ currentMaterial.materialNo }}</el-descriptions-item>
        <el-descriptions-item label="物料名称">{{ currentMaterial.materialName }}</el-descriptions-item>
        <el-descriptions-item label="物料类型">
          <el-tag :type="currentMaterial.type === '电子件' ? 'danger' : currentMaterial.type === '配件' ? 'warning' : currentMaterial.type === '结构件' ? 'primary' : 'info'">
            {{ currentMaterial.type }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ currentMaterial.spec }}</el-descriptions-item>
        <el-descriptions-item label="单位">{{ currentMaterial.unit }}</el-descriptions-item>
        <el-descriptions-item label="单价">¥{{ currentMaterial.price?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="库存">{{ currentMaterial.stockQty }}</el-descriptions-item>
        <el-descriptions-item label="安全库存">{{ currentMaterial.safetyStock }}</el-descriptions-item>
        <el-descriptions-item label="供应商" :span="2">{{ currentMaterial.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentMaterial.status === 1 ? 'success' : 'danger'">
            {{ currentMaterial.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentMaterial.createTime ? currentMaterial.createTime.replace('T', ' ').substring(0, 19) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentMaterial.remark || '无' }}</el-descriptions-item>
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
import { getMaterialList, createMaterial, updateMaterial, deleteMaterial } from '../../api/material'
import { getSupplierAll } from '../../api/supplier'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const materialFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  materialNo: '',
  materialName: '',
  type: ''
})

const materialForm = reactive({
  id: null,
  materialNo: '',
  materialName: '',
  type: '电子件',
  spec: '',
  unit: '',
  price: 0,
  stockQty: 0,
  safetyStock: 0,
  supplierId: null,
  supplierName: '',
  status: 1,
  remark: ''
})

const currentMaterial = reactive({})
const supplierList = ref([])

const materialRules = {
  materialNo: [
    { required: true, message: '请输入物料编号', trigger: 'blur' }
  ],
  materialName: [
    { required: true, message: '请输入物料名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择物料类型', trigger: 'change' }
  ],
  unit: [
    { required: true, message: '请输入单位', trigger: 'blur' }
  ]
}

const tableData = ref([])

onMounted(() => {
  loadSupplierList()
  fetchData()
})

async function loadSupplierList() {
  try {
    const res = await getSupplierAll()
    supplierList.value = res.data || []
  } catch (error) {
    console.error('获取供应商列表失败:', error)
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      materialName: searchForm.materialName || undefined,
      materialNo: searchForm.materialNo || undefined,
      type: searchForm.type || undefined
    }
    const res = await getMaterialList(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取物料列表失败:', error)
    ElMessage.error('获取物料列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.materialNo = ''
  searchForm.materialName = ''
  searchForm.type = ''
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
  Object.assign(materialForm, {
    id: null,
    materialNo: '',
    materialName: '',
    type: '电子件',
    spec: '',
    unit: '',
    price: 0,
    stockQty: 0,
    safetyStock: 0,
    supplierId: null,
    supplierName: '',
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(materialForm, row)
  dialogVisible.value = true
}

function handleView(row) {
  Object.assign(currentMaterial, row)
  detailVisible.value = true
}

async function handleSubmit() {
  if (!materialFormRef.value) return
  try {
    await materialFormRef.value.validate()
    submitLoading.value = true
    
    const supplier = supplierList.value.find(s => s.id === materialForm.supplierId)
    if (supplier) {
      materialForm.supplierName = supplier.supplierName
    }
    
    if (isEdit.value) {
      await updateMaterial(materialForm)
      ElMessage.success('编辑成功')
    } else {
      await createMaterial(materialForm)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除物料「${row.materialName}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteMaterial(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}
</script>

<style scoped>
.page-container { width: 100%; }
.search-card { margin-bottom: 20px; }
.table-card { margin-bottom: 20px; }
.table-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.table-title { font-size: 16px; font-weight: bold; color: #303133; }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>