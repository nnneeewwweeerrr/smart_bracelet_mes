<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="产品编号">
          <el-input v-model="searchForm.productNo" placeholder="请输入产品编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="searchForm.name" placeholder="请输入产品名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="产品状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="在售" value="1" />
            <el-option label="停产" value="0" />
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
        <el-table-column prop="name" label="产品名称" min-width="180" />
        <el-table-column prop="model" label="产品型号" width="140" />
        <el-table-column prop="category" label="产品分类" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="success">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="售价（元）" width="120" align="right" />
        <el-table-column prop="cost" label="成本（元）" width="120" align="right" />
        <el-table-column prop="unit" label="单位" width="70" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'info'">
              {{ row.status === '1' ? '在售' : '停产' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
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
        <el-form-item label="产品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="产品型号" prop="model">
          <el-input v-model="productForm.model" placeholder="请输入产品型号" />
        </el-form-item>
        <el-form-item label="产品分类" prop="category">
          <el-select v-model="productForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="智能手环" value="智能手环" />
            <el-option label="智能手表" value="智能手表" />
            <el-option label="健康监测设备" value="健康监测设备" />
          </el-select>
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
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="productForm.status">
            <el-radio value="1">在售</el-radio>
            <el-radio value="0">停产</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="产品描述" prop="description">
          <el-input v-model="productForm.description" type="textarea" :rows="3" placeholder="请输入产品描述" />
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
        <el-descriptions-item label="产品名称">{{ currentProduct.name }}</el-descriptions-item>
        <el-descriptions-item label="产品型号">{{ currentProduct.model }}</el-descriptions-item>
        <el-descriptions-item label="产品分类">
          <el-tag type="success">{{ currentProduct.category }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="售价">¥{{ currentProduct.price }}</el-descriptions-item>
        <el-descriptions-item label="成本">¥{{ currentProduct.cost }}</el-descriptions-item>
        <el-descriptions-item label="单位">{{ currentProduct.unit }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentProduct.status === '1' ? 'success' : 'info'">
            {{ currentProduct.status === '1' ? '在售' : '停产' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="产品描述" :span="2">{{ currentProduct.description }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentProduct.createTime }}</el-descriptions-item>
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
  name: '',
  status: ''
})

const productForm = reactive({
  id: null,
  productNo: '',
  name: '',
  model: '',
  category: '智能手环',
  price: 0,
  cost: 0,
  unit: '台',
  status: '1',
  description: '',
  remark: ''
})

const currentProduct = reactive({})

const productRules = {
  name: [
    { required: true, message: '请输入产品名称', trigger: 'blur' }
  ],
  model: [
    { required: true, message: '请输入产品型号', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择产品分类', trigger: 'change' }
  ]
}

const mockData = ref([
  { id: 1, productNo: 'P001', name: '智能健康手环 Pro', model: 'SH-P001', category: '智能手环', price: 299.00, cost: 120.00, unit: '台', status: '1', description: '心率监测、血氧检测、睡眠追踪、运动模式', createTime: '2024-01-10 09:00:00', remark: '旗舰款' },
  { id: 2, productNo: 'P002', name: '智能运动手环 Lite', model: 'SH-L002', category: '智能手环', price: 199.00, cost: 80.00, unit: '台', status: '1', description: '基础心率、计步、睡眠监测', createTime: '2024-01-11 10:30:00', remark: '入门款' },
  { id: 3, productNo: 'P003', name: '智能手表 Max', model: 'SW-M003', category: '智能手表', price: 599.00, cost: 250.00, unit: '台', status: '1', description: '大屏、GPS定位、多功能运动模式', createTime: '2024-01-12 14:20:00', remark: '高端款' },
  { id: 4, productNo: 'P004', name: '健康监测手环 S', model: 'SH-S004', category: '健康监测设备', price: 399.00, cost: 160.00, unit: '台', status: '1', description: '专业健康监测，血压、血氧、心率', createTime: '2024-01-13 11:15:00', remark: '健康款' },
  { id: 5, productNo: 'P005', name: '儿童智能手环', model: 'SH-K005', category: '智能手环', price: 249.00, cost: 100.00, unit: '台', status: '0', description: '定位、通话、安全围栏', createTime: '2024-01-14 16:40:00', remark: '已停产' },
  { id: 6, productNo: 'P006', name: '智能手环 青春版', model: 'SH-Q006', category: '智能手环', price: 149.00, cost: 60.00, unit: '台', status: '1', description: '轻巧时尚，年轻人首选', createTime: '2024-01-15 08:50:00', remark: '' },
  { id: 7, productNo: 'P007', name: '老人健康手表', model: 'SW-L007', category: '智能手表', price: 459.00, cost: 180.00, unit: '台', status: '1', description: '大字体、SOS求助、健康监测', createTime: '2024-01-16 13:25:00', remark: '' },
  { id: 8, productNo: 'P008', name: '运动手环 竞技版', model: 'SH-J008', category: '智能手环', price: 349.00, cost: 140.00, unit: '台', status: '1', description: '专业运动数据、GPS、心率带', createTime: '2024-01-17 10:10:00', remark: '运动款' }
])

const tableData = ref([])

onMounted(() => {
  fetchData()
})

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filtered = [...mockData.value]
    if (searchForm.productNo) {
      filtered = filtered.filter(item => item.productNo.includes(searchForm.productNo))
    }
    if (searchForm.name) {
      filtered = filtered.filter(item => item.name.includes(searchForm.name))
    }
    if (searchForm.status) {
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
  searchForm.productNo = ''
  searchForm.name = ''
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
    name: '',
    model: '',
    category: '智能手环',
    price: 0,
    cost: 0,
    unit: '台',
    status: '1',
    description: '',
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
    setTimeout(() => {
      if (isEdit.value) {
        const index = mockData.value.findIndex(item => item.id === productForm.id)
        if (index !== -1) {
          mockData.value[index] = { ...mockData.value[index], ...productForm }
        }
        ElMessage.success('编辑成功')
      } else {
        const maxId = Math.max(...mockData.value.map(item => item.id), 0)
        const newProduct = {
          ...productForm,
          id: maxId + 1,
          productNo: 'P' + String(maxId + 1).padStart(3, '0'),
          createTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
        }
        mockData.value.unshift(newProduct)
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

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该产品吗？', '提示', {
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
