<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="盘点单号">
          <el-input v-model="searchForm.takeNo" placeholder="请输入盘点单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouse" placeholder="全部仓库" clearable style="width: 140px">
            <el-option v-for="item in warehouseList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="盘点状态">
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
        <span class="table-title">库存盘点列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增盘点
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="takeNo" label="盘点单号" width="160" />
        <el-table-column prop="warehouse" label="仓库" width="120" />
        <el-table-column prop="takeDate" label="盘点日期" width="120" />
        <el-table-column prop="takePerson" label="盘点人" width="100" />
        <el-table-column prop="itemCount" label="物料数" width="90" align="center" />
        <el-table-column prop="diffCount" label="差异数" width="90" align="center">
          <template #default="{ row }">
            <span :class="row.diffCount > 0 ? 'text-danger' : ''">{{ row.diffCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
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
              @click="handleComplete(row)"
              :disabled="row.status !== 0"
            >
              完成
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
      :title="dialogTitle"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="takeFormRef"
        :model="takeForm"
        :rules="takeRules"
        label-width="100px"
        :disabled="isView"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="仓库" prop="warehouse">
              <el-select v-model="takeForm.warehouse" placeholder="请选择仓库" style="width: 100%" @change="handleWarehouseChange">
                <el-option v-for="item in warehouseList" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="盘点人" prop="takePerson">
              <el-input v-model="takeForm.takePerson" placeholder="请输入盘点人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="takeForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>

        <div class="detail-title">盘点明细</div>
        <el-table :data="takeForm.items" border stripe style="width: 100%">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="materialCode" label="物料编码" width="140" />
          <el-table-column prop="materialName" label="物料名称" min-width="160" />
          <el-table-column prop="spec" label="规格" width="120" />
          <el-table-column prop="unit" label="单位" width="70" align="center" />
          <el-table-column prop="systemQty" label="系统数量" width="110" align="center" />
          <el-table-column prop="actualQty" label="实盘数量" width="110" align="center">
            <template #default="{ row }">
              <el-input-number
                v-if="!isView"
                v-model="row.actualQty"
                :min="0"
                :max="99999"
                size="small"
                style="width: 100%"
                @change="handleQtyChange"
              />
              <span v-else>{{ row.actualQty }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="diffQty" label="差异数量" width="110" align="center">
            <template #default="{ row }">
              <span :class="row.diffQty !== 0 ? 'text-danger' : ''">{{ row.diffQty }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ isView ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
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
const isEdit = ref(false)
const isView = ref(false)
const takeFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  takeNo: '',
  warehouse: '',
  status: ''
})

const takeForm = reactive({
  id: null,
  takeNo: '',
  warehouse: '',
  takeDate: '',
  takePerson: '',
  remark: '',
  items: []
})

const warehouseList = [
  { label: '原材料仓', value: '原材料仓' },
  { label: '成品仓', value: '成品仓' },
  { label: '半成品仓', value: '半成品仓' }
]

const statusOptions = [
  { label: '盘点中', value: 0 },
  { label: '已完成', value: 1 },
  { label: '已审核', value: 2 }
]

const takeRules = {
  warehouse: [
    { required: true, message: '请选择仓库', trigger: 'change' }
  ],
  takePerson: [
    { required: true, message: '请输入盘点人', trigger: 'blur' }
  ]
}

const stockItems = [
  { materialCode: 'M-001', materialName: 'PCB主板', spec: 'SH-P001', unit: '块', systemQty: 500 },
  { materialCode: 'M-002', materialName: '显示屏', spec: '1.5寸', unit: '块', systemQty: 480 },
  { materialCode: 'M-003', materialName: '电池', spec: '100mAh', unit: '个', systemQty: 600 },
  { materialCode: 'M-004', materialName: '表带', spec: '硅胶黑色', unit: '条', systemQty: 520 },
  { materialCode: 'M-005', materialName: '心率传感器', spec: 'MAX30102', unit: '个', systemQty: 450 }
]

const finishedItems = [
  { materialCode: 'P-001', materialName: '智能手环Pro', spec: 'SH-P001', unit: '台', systemQty: 200 },
  { materialCode: 'P-002', materialName: '智能手环Lite', spec: 'SH-L002', unit: '台', systemQty: 350 },
  { materialCode: 'P-003', materialName: '智能手环Ultra', spec: 'SH-U003', unit: '台', systemQty: 80 }
]

const mockData = ref([
  {
    id: 1,
    takeNo: 'ST-20240115001',
    warehouse: '原材料仓',
    takeDate: '2024-01-15',
    takePerson: '王仓库',
    itemCount: 5,
    diffCount: 1,
    status: 1,
    createTime: '2024-01-15 09:00:00',
    remark: '月末盘点',
    items: [
      { materialCode: 'M-001', materialName: 'PCB主板', spec: 'SH-P001', unit: '块', systemQty: 500, actualQty: 500, diffQty: 0 },
      { materialCode: 'M-002', materialName: '显示屏', spec: '1.5寸', unit: '块', systemQty: 480, actualQty: 478, diffQty: -2 },
      { materialCode: 'M-003', materialName: '电池', spec: '100mAh', unit: '个', systemQty: 600, actualQty: 600, diffQty: 0 },
      { materialCode: 'M-004', materialName: '表带', spec: '硅胶黑色', unit: '条', systemQty: 520, actualQty: 520, diffQty: 0 },
      { materialCode: 'M-005', materialName: '心率传感器', spec: 'MAX30102', unit: '个', systemQty: 450, actualQty: 450, diffQty: 0 }
    ]
  },
  {
    id: 2,
    takeNo: 'ST-20240110001',
    warehouse: '成品仓',
    takeDate: '2024-01-10',
    takePerson: '李仓库',
    itemCount: 3,
    diffCount: 0,
    status: 2,
    createTime: '2024-01-10 14:00:00',
    remark: '',
    items: [
      { materialCode: 'P-001', materialName: '智能手环Pro', spec: 'SH-P001', unit: '台', systemQty: 200, actualQty: 200, diffQty: 0 },
      { materialCode: 'P-002', materialName: '智能手环Lite', spec: 'SH-L002', unit: '台', systemQty: 350, actualQty: 350, diffQty: 0 },
      { materialCode: 'P-003', materialName: '智能手环Ultra', spec: 'SH-U003', unit: '台', systemQty: 80, actualQty: 80, diffQty: 0 }
    ]
  },
  {
    id: 3,
    takeNo: 'ST-20240116001',
    warehouse: '半成品仓',
    takeDate: '2024-01-16',
    takePerson: '张仓库',
    itemCount: 4,
    diffCount: 2,
    status: 0,
    createTime: '2024-01-16 08:30:00',
    remark: '周盘点',
    items: []
  }
])

const tableData = ref([])

const dialogTitle = computed(() => {
  if (isView.value) return '盘点详情'
  return isEdit.value ? '编辑盘点' : '新增盘点'
})

function getStatusType(status) {
  const types = ['warning', 'primary', 'success']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['盘点中', '已完成', '已审核']
  return texts[status] || '未知'
}

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filtered = [...mockData.value]
    if (searchForm.takeNo) {
      filtered = filtered.filter(item => item.takeNo.includes(searchForm.takeNo))
    }
    if (searchForm.warehouse) {
      filtered = filtered.filter(item => item.warehouse === searchForm.warehouse)
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
  searchForm.takeNo = ''
  searchForm.warehouse = ''
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
  isView.value = false
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  isView.value = false
  Object.assign(takeForm, JSON.parse(JSON.stringify(row)))
  dialogVisible.value = true
}

function handleView(row) {
  isEdit.value = false
  isView.value = true
  Object.assign(takeForm, JSON.parse(JSON.stringify(row)))
  dialogVisible.value = true
}

function handleComplete(row) {
  ElMessageBox.confirm(`确定要完成盘点「${row.takeNo}」吗？完成后不可修改。`, '确认', {
    confirmButtonText: '确定完成',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => {
    const take = mockData.value.find(item => item.id === row.id)
    if (take) {
      take.status = 1
      let diffCount = 0
      take.items.forEach(item => {
        if (item.diffQty !== 0) diffCount++
      })
      take.diffCount = diffCount
    }
    ElMessage.success('盘点完成')
    fetchData()
  }).catch(() => {})
}

function handleWarehouseChange(val) {
  let items = []
  if (val === '原材料仓') {
    items = JSON.parse(JSON.stringify(stockItems))
  } else if (val === '成品仓') {
    items = JSON.parse(JSON.stringify(finishedItems))
  } else {
    items = JSON.parse(JSON.stringify(stockItems.slice(0, 3)))
  }
  items.forEach(item => {
    item.actualQty = item.systemQty
    item.diffQty = 0
  })
  takeForm.items = items
}

function handleQtyChange() {
  takeForm.items.forEach(item => {
    item.diffQty = item.actualQty - item.systemQty
  })
}

function resetForm() {
  takeForm.id = null
  takeForm.takeNo = ''
  takeForm.warehouse = ''
  takeForm.takeDate = ''
  takeForm.takePerson = ''
  takeForm.remark = ''
  takeForm.items = []
  if (takeFormRef.value) {
    takeFormRef.value.resetFields()
  }
}

async function handleSubmit() {
  if (!takeFormRef.value) return
  
  try {
    await takeFormRef.value.validate()
    submitLoading.value = true
    
    setTimeout(() => {
      let diffCount = 0
      takeForm.items.forEach(item => {
        if (item.diffQty !== 0) diffCount++
      })
      
      if (isEdit.value) {
        const index = mockData.value.findIndex(item => item.id === takeForm.id)
        if (index !== -1) {
          mockData.value[index] = {
            ...mockData.value[index],
            warehouse: takeForm.warehouse,
            takePerson: takeForm.takePerson,
            remark: takeForm.remark,
            items: takeForm.items,
            itemCount: takeForm.items.length,
            diffCount: diffCount
          }
        }
        ElMessage.success('编辑成功')
      } else {
        const maxId = Math.max(...mockData.value.map(item => item.id), 0)
        const now = new Date()
        const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
        const newTake = {
          id: maxId + 1,
          takeNo: 'ST-' + dateStr + String(maxId + 1).padStart(3, '0'),
          warehouse: takeForm.warehouse,
          takeDate: now.toISOString().slice(0, 10),
          takePerson: takeForm.takePerson,
          itemCount: takeForm.items.length,
          diffCount: diffCount,
          status: 0,
          createTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
          remark: takeForm.remark,
          items: takeForm.items
        }
        mockData.value.unshift(newTake)
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

onMounted(() => {
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

.detail-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin: 10px 0;
}

.text-danger {
  color: #F56C6C;
}
</style>
