<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="物料编号">
          <el-input v-model="searchForm.materialNo" placeholder="请输入物料编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="物料名称">
          <el-input v-model="searchForm.name" placeholder="请输入物料名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="物料类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 140px">
            <el-option label="原材料" value="raw" />
            <el-option label="半成品" value="semi" />
            <el-option label="辅料" value="aux" />
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
        <el-table-column prop="name" label="物料名称" min-width="160" />
        <el-table-column prop="type" label="物料类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'raw'" type="primary">原材料</el-tag>
            <el-tag v-else-if="row.type === 'semi'" type="warning">半成品</el-tag>
            <el-tag v-else type="info">辅料</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="spec" label="规格型号" width="140" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="price" label="单价（元）" width="120" align="right" />
        <el-table-column prop="supplierName" label="供应商" width="160" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'danger'">
              {{ row.status === '1' ? '启用' : '停用' }}
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
        <el-form-item label="物料名称" prop="name">
          <el-input v-model="materialForm.name" placeholder="请输入物料名称" />
        </el-form-item>
        <el-form-item label="物料类型" prop="type">
          <el-select v-model="materialForm.type" placeholder="请选择物料类型" style="width: 100%">
            <el-option label="原材料" value="raw" />
            <el-option label="半成品" value="semi" />
            <el-option label="辅料" value="aux" />
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
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="materialForm.supplierId" placeholder="请选择供应商" style="width: 100%">
            <el-option label="深圳市电子元件有限公司" :value="1" />
            <el-option label="东莞市塑胶制品厂" :value="2" />
            <el-option label="广州市包装材料公司" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="materialForm.status">
            <el-radio value="1">启用</el-radio>
            <el-radio value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="materialForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
        <el-descriptions-item label="物料名称">{{ currentMaterial.name }}</el-descriptions-item>
        <el-descriptions-item label="物料类型">
          <el-tag v-if="currentMaterial.type === 'raw'" type="primary">原材料</el-tag>
          <el-tag v-else-if="currentMaterial.type === 'semi'" type="warning">半成品</el-tag>
          <el-tag v-else type="info">辅料</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ currentMaterial.spec }}</el-descriptions-item>
        <el-descriptions-item label="单位">{{ currentMaterial.unit }}</el-descriptions-item>
        <el-descriptions-item label="单价">¥{{ currentMaterial.price }}</el-descriptions-item>
        <el-descriptions-item label="供应商" :span="2">{{ currentMaterial.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentMaterial.status === '1' ? 'success' : 'danger'">
            {{ currentMaterial.status === '1' ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentMaterial.createTime }}</el-descriptions-item>
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
  name: '',
  type: ''
})

const materialForm = reactive({
  id: null,
  materialNo: '',
  name: '',
  type: 'raw',
  spec: '',
  unit: '',
  price: 0,
  supplierId: null,
  supplierName: '',
  status: '1',
  remark: ''
})

const currentMaterial = reactive({})

const materialRules = {
  name: [
    { required: true, message: '请输入物料名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择物料类型', trigger: 'change' }
  ],
  unit: [
    { required: true, message: '请输入单位', trigger: 'blur' }
  ]
}

const mockData = ref([
  { id: 1, materialNo: 'M001', name: '主板芯片', type: 'raw', spec: 'STM32F103', unit: '个', price: 15.50, supplierId: 1, supplierName: '深圳市电子元件有限公司', status: '1', createTime: '2024-01-10 09:00:00', remark: '核心元器件' },
  { id: 2, materialNo: 'M002', name: '电池', type: 'raw', spec: '3.7V 200mAh', unit: '个', price: 8.00, supplierId: 8, supplierName: '惠州市电池制造厂', status: '1', createTime: '2024-01-11 10:30:00', remark: '' },
  { id: 3, materialNo: 'M003', name: '塑胶外壳', type: 'raw', spec: 'ABS 白色', unit: '个', price: 3.50, supplierId: 2, supplierName: '东莞市塑胶制品厂', status: '1', createTime: '2024-01-12 14:20:00', remark: '' },
  { id: 4, materialNo: 'M004', name: '显示屏', type: 'raw', spec: '0.96寸 OLED', unit: '个', price: 12.00, supplierId: 1, supplierName: '深圳市电子元件有限公司', status: '1', createTime: '2024-01-13 11:15:00', remark: '' },
  { id: 5, materialNo: 'M005', name: '心率传感器', type: 'raw', spec: 'MAX30102', unit: '个', price: 20.00, supplierId: 1, supplierName: '深圳市电子元件有限公司', status: '1', createTime: '2024-01-14 16:40:00', remark: '进口元器件' },
  { id: 6, materialNo: 'M006', name: '包装盒', type: 'aux', spec: '彩盒 白色', unit: '个', price: 2.00, supplierId: 3, supplierName: '广州市包装材料公司', status: '0', createTime: '2024-01-15 08:50:00', remark: '暂停使用' },
  { id: 7, materialNo: 'M007', name: '表带', type: 'semi', spec: '硅胶 黑色', unit: '条', price: 5.50, supplierId: 2, supplierName: '东莞市塑胶制品厂', status: '1', createTime: '2024-01-16 13:25:00', remark: '' },
  { id: 8, materialNo: 'M008', name: 'USB充电线', type: 'aux', spec: 'Type-C 20cm', unit: '条', price: 3.00, supplierId: 7, supplierName: '中山市灯饰配件公司', status: '1', createTime: '2024-01-17 10:10:00', remark: '' }
])

const tableData = ref([])

onMounted(() => {
  fetchData()
})

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filtered = [...mockData.value]
    if (searchForm.materialNo) {
      filtered = filtered.filter(item => item.materialNo.includes(searchForm.materialNo))
    }
    if (searchForm.name) {
      filtered = filtered.filter(item => item.name.includes(searchForm.name))
    }
    if (searchForm.type) {
      filtered = filtered.filter(item => item.type === searchForm.type)
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
  searchForm.materialNo = ''
  searchForm.name = ''
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
    name: '',
    type: 'raw',
    spec: '',
    unit: '',
    price: 0,
    supplierId: null,
    supplierName: '',
    status: '1',
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

const supplierMap = {
  1: '深圳市电子元件有限公司',
  2: '东莞市塑胶制品厂',
  3: '广州市包装材料公司',
  8: '惠州市电池制造厂',
  7: '中山市灯饰配件公司'
}

async function handleSubmit() {
  if (!materialFormRef.value) return
  try {
    await materialFormRef.value.validate()
    submitLoading.value = true
    setTimeout(() => {
      if (isEdit.value) {
        const index = mockData.value.findIndex(item => item.id === materialForm.id)
        if (index !== -1) {
          const supplierName = supplierMap[materialForm.supplierId] || ''
          mockData.value[index] = { ...mockData.value[index], ...materialForm, supplierName }
        }
        ElMessage.success('编辑成功')
      } else {
        const maxId = Math.max(...mockData.value.map(item => item.id), 0)
        const supplierName = supplierMap[materialForm.supplierId] || ''
        const newMaterial = {
          ...materialForm,
          id: maxId + 1,
          materialNo: 'M' + String(maxId + 1).padStart(3, '0'),
          supplierName,
          createTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
        }
        mockData.value.unshift(newMaterial)
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
  ElMessageBox.confirm('确定要删除该物料吗？', '提示', {
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
