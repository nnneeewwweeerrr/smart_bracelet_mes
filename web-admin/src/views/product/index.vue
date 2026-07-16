<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="产品编号">
          <el-input v-model="searchForm.productNo" placeholder="请输入产品编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入产品名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="产品状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="在售" :value="1" />
            <el-option label="停产" :value="0" />
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
        <span class="table-title">产品列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增产品
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="productNo" label="产品编号" width="140" />
        <el-table-column prop="productName" label="产品名称" min-width="180" />
        <el-table-column prop="category" label="产品分类" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="success">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="model" label="产品型号" width="140" />
        <el-table-column prop="price" label="售价（元）" width="120" align="right">
          <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="cost" label="成本（元）" width="120" align="right">
          <template #default="{ row }">¥{{ row.cost?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="70" align="center" />
        <el-table-column prop="stockQty" label="库存" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '在售' : '停产' }}
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
      :title="isEdit ? '编辑产品' : '新增产品'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productRules"
        label-width="100px"
      >
        <el-form-item label="产品编号" prop="productNo">
          <el-input v-model="productForm.productNo" placeholder="请输入产品编号" />
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="productForm.productName" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="产品分类" prop="category">
          <el-select v-model="productForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="手环" value="手环" />
            <el-option label="手表" value="手表" />
            <el-option label="耳机" value="耳机" />
            <el-option label="健康设备" value="健康设备" />
            <el-option label="智能家居" value="智能家居" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品型号" prop="model">
          <el-input v-model="productForm.model" placeholder="请输入产品型号" />
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number v-model="productForm.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="成本" prop="cost">
          <el-input-number v-model="productForm.cost" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="productForm.unit" placeholder="请输入单位" style="width: 50%" />
        </el-form-item>
        <el-form-item label="库存数量" prop="stockQty">
          <el-input-number v-model="productForm.stockQty" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="安全库存" prop="safetyStock">
          <el-input-number v-model="productForm.safetyStock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="productForm.status">
            <el-radio :value="1">在售</el-radio>
            <el-radio :value="0">停产</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="产品描述" prop="description">
          <el-input v-model="productForm.description" type="textarea" :rows="2" placeholder="请输入产品描述" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="productForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="产品详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="产品编号">{{ currentProduct.productNo }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ currentProduct.productName }}</el-descriptions-item>
        <el-descriptions-item label="产品分类">
          <el-tag type="success">{{ currentProduct.category }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="产品型号">{{ currentProduct.model }}</el-descriptions-item>
        <el-descriptions-item label="售价">¥{{ currentProduct.price?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="成本">¥{{ currentProduct.cost?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="单位">{{ currentProduct.unit }}</el-descriptions-item>
        <el-descriptions-item label="库存">{{ currentProduct.stockQty }}</el-descriptions-item>
        <el-descriptions-item label="安全库存">{{ currentProduct.safetyStock }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentProduct.status === 1 ? 'success' : 'info'">
            {{ currentProduct.status === 1 ? '在售' : '停产' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="产品描述" :span="2">{{ currentProduct.description }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ currentProduct.createTime ? currentProduct.createTime.replace('T', ' ').substring(0, 19) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentProduct.remark || '无' }}</el-descriptions-item>
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
import { getProductList, createProduct, updateProduct, deleteProduct } from '../../api/product'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const productFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  productNo: '',
  productName: '',
  status: ''
})

const productForm = reactive({
  id: null,
  productNo: '',
  productName: '',
  category: '手环',
  model: '',
  price: 0,
  cost: 0,
  unit: '个',
  stockQty: 0,
  safetyStock: 0,
  description: '',
  status: 1,
  remark: ''
})

const currentProduct = reactive({})

const productRules = {
  productNo: [
    { required: true, message: '请输入产品编号', trigger: 'blur' }
  ],
  productName: [
    { required: true, message: '请输入产品名称', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择产品分类', trigger: 'change' }
  ],
  model: [
    { required: true, message: '请输入产品型号', trigger: 'blur' }
  ]
}

const tableData = ref([])

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      productName: searchForm.productName || undefined,
      productNo: searchForm.productNo || undefined,
      status: searchForm.status || undefined
    }
    const res = await getProductList(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取产品列表失败:', error)
    ElMessage.error('获取产品列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.productNo = ''
  searchForm.productName = ''
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
  Object.assign(productForm, {
    id: null,
    productNo: '',
    productName: '',
    category: '手环',
    model: '',
    price: 0,
    cost: 0,
    unit: '个',
    stockQty: 0,
    safetyStock: 0,
    description: '',
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(productForm, row)
  dialogVisible.value = true
}

function handleView(row) {
  Object.assign(currentProduct, row)
  detailVisible.value = true
}

async function handleSubmit() {
  if (!productFormRef.value) return
  try {
    await productFormRef.value.validate()
    submitLoading.value = true
    if (isEdit.value) {
      await updateProduct(productForm)
      ElMessage.success('编辑成功')
    } else {
      await createProduct(productForm)
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
    await ElMessageBox.confirm(`确定要删除产品「${row.productName}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteProduct(row.id)
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