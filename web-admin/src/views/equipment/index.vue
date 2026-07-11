<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="设备编号">
          <el-input v-model="searchForm.equipmentNo" placeholder="请输入设备编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="searchForm.name" placeholder="请输入设备名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 140px">
            <el-option label="生产设备" value="production" />
            <el-option label="检测设备" value="inspection" />
            <el-option label="辅助设备" value="auxiliary" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="运行中" value="running" />
            <el-option label="空闲" value="idle" />
            <el-option label="维修中" value="maintenance" />
            <el-option label="停用" value="disabled" />
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
        <span class="table-title">设备列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增设备
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="equipmentNo" label="设备编号" width="140" />
        <el-table-column prop="name" label="设备名称" min-width="160" />
        <el-table-column prop="type" label="设备类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'production'" type="primary">生产设备</el-tag>
            <el-tag v-else-if="row.type === 'inspection'" type="success">检测设备</el-tag>
            <el-tag v-else type="info">辅助设备</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="model" label="规格型号" width="140" />
        <el-table-column prop="workshop" label="所属车间" width="120" />
        <el-table-column prop="manager" label="负责人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'running'" type="success">运行中</el-tag>
            <el-tag v-else-if="row.status === 'idle'" type="info">空闲</el-tag>
            <el-tag v-else-if="row.status === 'maintenance'" type="warning">维修中</el-tag>
            <el-tag v-else type="danger">停用</el-tag>
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
      :title="isEdit ? '编辑设备' : '新增设备'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="equipmentFormRef"
        :model="equipmentForm"
        :rules="equipmentRules"
        label-width="100px"
      >
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="equipmentForm.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="type">
          <el-select v-model="equipmentForm.type" placeholder="请选择设备类型" style="width: 100%">
            <el-option label="生产设备" value="production" />
            <el-option label="检测设备" value="inspection" />
            <el-option label="辅助设备" value="auxiliary" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格型号" prop="model">
          <el-input v-model="equipmentForm.model" placeholder="请输入规格型号" />
        </el-form-item>
        <el-form-item label="所属车间" prop="workshop">
          <el-select v-model="equipmentForm.workshop" placeholder="请选择车间" style="width: 100%">
            <el-option label="组装车间" value="组装车间" />
            <el-option label="贴片车间" value="贴片车间" />
            <el-option label="质检车间" value="质检车间" />
            <el-option label="包装车间" value="包装车间" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="manager">
          <el-input v-model="equipmentForm.manager" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="equipmentForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="购置日期" prop="purchaseDate">
          <el-date-picker
            v-model="equipmentForm.purchaseDate"
            type="date"
            placeholder="请选择购置日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="equipmentForm.status">
            <el-radio value="running">运行中</el-radio>
            <el-radio value="idle">空闲</el-radio>
            <el-radio value="maintenance">维修中</el-radio>
            <el-radio value="disabled">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="equipmentForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="设备详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="设备编号">{{ currentEquipment.equipmentNo }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentEquipment.name }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">
          <el-tag v-if="currentEquipment.type === 'production'" type="primary">生产设备</el-tag>
          <el-tag v-else-if="currentEquipment.type === 'inspection'" type="success">检测设备</el-tag>
          <el-tag v-else type="info">辅助设备</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ currentEquipment.model }}</el-descriptions-item>
        <el-descriptions-item label="所属车间">{{ currentEquipment.workshop }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ currentEquipment.manager }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentEquipment.phone }}</el-descriptions-item>
        <el-descriptions-item label="购置日期">{{ currentEquipment.purchaseDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentEquipment.status === 'running'" type="success">运行中</el-tag>
          <el-tag v-else-if="currentEquipment.status === 'idle'" type="info">空闲</el-tag>
          <el-tag v-else-if="currentEquipment.status === 'maintenance'" type="warning">维修中</el-tag>
          <el-tag v-else type="danger">停用</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentEquipment.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentEquipment.remark || '无' }}</el-descriptions-item>
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
const equipmentFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  equipmentNo: '',
  name: '',
  type: '',
  status: ''
})

const equipmentForm = reactive({
  id: null,
  equipmentNo: '',
  name: '',
  type: 'production',
  model: '',
  workshop: '',
  manager: '',
  phone: '',
  purchaseDate: '',
  status: 'idle',
  remark: ''
})

const currentEquipment = reactive({})

const equipmentRules = {
  name: [
    { required: true, message: '请输入设备名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ],
  workshop: [
    { required: true, message: '请选择所属车间', trigger: 'change' }
  ],
  manager: [
    { required: true, message: '请输入负责人', trigger: 'blur' }
  ]
}

const mockData = ref([
  { id: 1, equipmentNo: 'EQ001', name: 'SMT贴片机', type: 'production', model: 'JUKI-2070', workshop: '贴片车间', manager: '张工', phone: '13800138001', purchaseDate: '2023-06-15', status: 'running', createTime: '2024-01-10 09:00:00', remark: '高速贴片机，产能12000点/小时' },
  { id: 2, equipmentNo: 'EQ002', name: '回流焊机', type: 'production', model: 'ETC-N40', workshop: '贴片车间', manager: '李工', phone: '13800138002', purchaseDate: '2023-07-20', status: 'running', createTime: '2024-01-11 10:30:00', remark: '八温区回流焊' },
  { id: 3, equipmentNo: 'EQ003', name: '自动组装线', type: 'production', model: 'ASM-L01', workshop: '组装车间', manager: '王工', phone: '13800138003', purchaseDate: '2023-08-10', status: 'running', createTime: '2024-01-12 14:20:00', remark: '手环自动组装生产线' },
  { id: 4, equipmentNo: 'EQ004', name: 'AOI检测仪', type: 'inspection', model: 'SAKI-BF', workshop: '质检车间', manager: '赵工', phone: '13800138004', purchaseDate: '2023-09-05', status: 'running', createTime: '2024-01-13 11:15:00', remark: '自动光学检测设备' },
  { id: 5, equipmentNo: 'EQ005', name: '老化测试柜', type: 'inspection', model: 'TLC-100', workshop: '质检车间', manager: '钱工', phone: '13800138005', purchaseDate: '2023-10-12', status: 'idle', createTime: '2024-01-14 16:40:00', remark: '可同时测试100台设备' },
  { id: 6, equipmentNo: 'EQ006', name: '激光打标机', type: 'auxiliary', model: 'LP-20W', workshop: '包装车间', manager: '孙工', phone: '13800138006', purchaseDate: '2023-11-08', status: 'maintenance', createTime: '2024-01-15 08:50:00', remark: '设备维护中，预计3天后恢复' },
  { id: 7, equipmentNo: 'EQ007', name: '自动包装机', type: 'auxiliary', model: 'PK-500', workshop: '包装车间', manager: '周工', phone: '13800138007', purchaseDate: '2023-12-01', status: 'running', createTime: '2024-01-16 13:25:00', remark: '' },
  { id: 8, equipmentNo: 'EQ008', name: '波峰焊机', type: 'production', model: 'WS-350', workshop: '贴片车间', manager: '吴工', phone: '13800138008', purchaseDate: '2023-05-20', status: 'disabled', createTime: '2024-01-17 10:10:00', remark: '设备老旧，已停用' }
])

const tableData = ref([])

onMounted(() => {
  fetchData()
})

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filtered = [...mockData.value]
    if (searchForm.equipmentNo) {
      filtered = filtered.filter(item => item.equipmentNo.includes(searchForm.equipmentNo))
    }
    if (searchForm.name) {
      filtered = filtered.filter(item => item.name.includes(searchForm.name))
    }
    if (searchForm.type) {
      filtered = filtered.filter(item => item.type === searchForm.type)
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
  searchForm.equipmentNo = ''
  searchForm.name = ''
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
  Object.assign(equipmentForm, {
    id: null,
    equipmentNo: '',
    name: '',
    type: 'production',
    model: '',
    workshop: '',
    manager: '',
    phone: '',
    purchaseDate: '',
    status: 'idle',
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(equipmentForm, row)
  dialogVisible.value = true
}

function handleView(row) {
  Object.assign(currentEquipment, row)
  detailVisible.value = true
}

async function handleSubmit() {
  if (!equipmentFormRef.value) return
  try {
    await equipmentFormRef.value.validate()
    submitLoading.value = true
    setTimeout(() => {
      if (isEdit.value) {
        const index = mockData.value.findIndex(item => item.id === equipmentForm.id)
        if (index !== -1) {
          mockData.value[index] = { ...mockData.value[index], ...equipmentForm }
        }
        ElMessage.success('编辑成功')
      } else {
        const maxId = Math.max(...mockData.value.map(item => item.id), 0)
        const newEquipment = {
          ...equipmentForm,
          id: maxId + 1,
          equipmentNo: 'EQ' + String(maxId + 1).padStart(3, '0'),
          createTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
        }
        mockData.value.unshift(newEquipment)
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
  ElMessageBox.confirm('确定要删除该设备吗？', '提示', {
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
