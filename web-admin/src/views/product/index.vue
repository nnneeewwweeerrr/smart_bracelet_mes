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
      productName: searchForm.name,
      productCode: searchForm.productNo
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    const res = await getProductList(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.warn('产品列表API调用失败，使用mock数据', error)
    tableData.value = [
      { id: 1, productNo: 'PRD-20260701', name: '智能健康手环 Pro', model: 'SH-P001', category: '智能手环', price: 299.00, cost: 120.00, unit: '台', status: '1', createTime: '2026-07-01 09:00:00', description: '专业健康监测手环，支持心率、血氧、睡眠监测', remark: '旗舰款' },
      { id: 2, productNo: 'PRD-20260701', name: '智能运动手环 Lite', model: 'SH-L002', category: '智能手环', price: 199.00, cost: 80.00, unit: '台', status: '1', createTime: '2026-07-01 10:30:00', description: '轻量运动手环，支持计步、运动模式、消息提醒', remark: '入门款' },
      { id: 3, productNo: 'PRD-20260702', name: '老人健康监护手环', model: 'SH-E003', category: '智能手环', price: 349.00, cost: 135.00, unit: '台', status: '1', createTime: '2026-07-02 14:00:00', description: '老人专用监护手环，支持跌倒检测、紧急呼叫、血压趋势', remark: '养老定制款' },
      { id: 4, productNo: 'PRD-20260703', name: '儿童安全定位手环', model: 'SH-K004', category: '智能手环', price: 259.00, cost: 105.00, unit: '台', status: '1', createTime: '2026-07-03 11:15:00', description: '儿童安全手环，支持GPS定位、电子围栏、SOS一键求助', remark: '校园版' },
      { id: 5, productNo: 'PRD-20260704', name: '专业运动手表 X1', model: 'SW-X001', category: '智能手表', price: 599.00, cost: 220.00, unit: '台', status: '1', createTime: '2026-07-04 09:30:00', description: '专业运动手表，支持100+运动模式、GPS轨迹、VO2Max', remark: '高端运动款' },
      { id: 6, productNo: 'PRD-20260705', name: '商务智能手表 S1', model: 'SW-S002', category: '智能手表', price: 499.00, cost: 185.00, unit: '台', status: '1', createTime: '2026-07-05 16:00:00', description: '商务手表，金属表壳、蓝宝石镜面、NFC支付', remark: '商务款' },
      { id: 7, productNo: 'PRD-20260707', name: '便携血氧仪', model: 'HM-O001', category: '健康监测设备', price: 159.00, cost: 65.00, unit: '台', status: '1', createTime: '2026-07-07 10:00:00', description: '指夹式血氧仪，快速测量血氧饱和度和脉率', remark: '家用款' },
      { id: 8, productNo: 'PRD-20260709', name: '智能体温贴片', model: 'HM-T002', category: '健康监测设备', price: 129.00, cost: 52.00, unit: '片', status: '1', createTime: '2026-07-09 13:45:00', description: '24小时连续体温监测贴片，蓝牙传输数据', remark: '医疗辅助款' },
      { id: 9, productNo: 'PRD-20260711', name: '智能健康手环 Max', model: 'SH-M005', category: '智能手环', price: 399.00, cost: 155.00, unit: '台', status: '1', createTime: '2026-07-11 15:30:00', description: '大屏健康手环，AMOLED屏幕、NFC、血氧心电图', remark: '新款旗舰' },
      { id: 10, productNo: 'PRD-20260713', name: '运动手环 SE', model: 'SH-SE006', category: '智能手环', price: 149.00, cost: 58.00, unit: '台', status: '0', createTime: '2026-07-13 08:30:00', description: '基础运动手环，已停产退市', remark: '已停产' }
    ]
    total.value = 10
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
    // request interceptor handles error message
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该产品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    // catch cancel or request error
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
</style>
