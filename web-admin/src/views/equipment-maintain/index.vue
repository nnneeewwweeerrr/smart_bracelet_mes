<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="设备编码">
          <el-input v-model="searchForm.equipmentCode" placeholder="请输入设备编码" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="searchForm.equipmentName" placeholder="请输入设备名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="保养类型">
          <el-select v-model="searchForm.maintainType" placeholder="全部类型" clearable style="width: 140px">
            <el-option v-for="item in maintainTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="保养状态">
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
        <span class="table-title">设备保养记录</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增保养
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="maintainNo" label="保养单号" width="160" />
        <el-table-column prop="equipmentCode" label="设备编码" width="140" />
        <el-table-column prop="equipmentName" label="设备名称" min-width="160" />
        <el-table-column prop="maintainType" label="保养类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getMaintainTypeTag(row.maintainType)" size="small">
              {{ getMaintainTypeText(row.maintainType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maintainDate" label="保养日期" width="120" />
        <el-table-column prop="maintainPerson" label="保养人" width="100" />
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
              :disabled="row.status === 1"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
              :disabled="row.status === 1"
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
      :title="dialogTitle"
      width="650px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="maintainFormRef"
        :model="maintainForm"
        :rules="maintainRules"
        label-width="100px"
        :disabled="isView"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备" prop="equipmentId">
              <el-select v-model="maintainForm.equipmentId" placeholder="请选择设备" style="width: 100%" @change="handleEquipmentChange">
                <el-option
                  v-for="item in equipmentList"
                  :key="item.id"
                  :label="item.equipmentName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备编码">
              <el-input v-model="maintainForm.equipmentCode" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="保养类型" prop="maintainType">
              <el-select v-model="maintainForm.maintainType" placeholder="请选择类型" style="width: 100%">
                <el-option label="日常保养" :value="1" />
                <el-option label="定期保养" :value="2" />
                <el-option label="一级保养" :value="3" />
                <el-option label="二级保养" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保养日期" prop="maintainDate">
              <el-date-picker
                v-model="maintainForm.maintainDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="保养人" prop="maintainPerson">
              <el-input v-model="maintainForm.maintainPerson" placeholder="请输入保养人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="耗时(小时)" prop="costHours">
              <el-input-number v-model="maintainForm.costHours" :min="0" :precision="1" :step="0.5" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="保养内容" prop="content">
          <el-input v-model="maintainForm.content" type="textarea" :rows="4" placeholder="请输入保养内容" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="maintainForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
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
const maintainFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  equipmentCode: '',
  equipmentName: '',
  maintainType: '',
  status: ''
})

const maintainForm = reactive({
  id: null,
  maintainNo: '',
  equipmentId: null,
  equipmentCode: '',
  equipmentName: '',
  maintainType: 1,
  maintainDate: '',
  maintainPerson: '',
  costHours: 0,
  content: '',
  remark: ''
})

const maintainTypeOptions = [
  { label: '日常保养', value: 1 },
  { label: '定期保养', value: 2 },
  { label: '一级保养', value: 3 },
  { label: '二级保养', value: 4 }
]

const statusOptions = [
  { label: '待执行', value: 0 },
  { label: '已完成', value: 1 }
]

const maintainRules = {
  equipmentId: [
    { required: true, message: '请选择设备', trigger: 'change' }
  ],
  maintainType: [
    { required: true, message: '请选择保养类型', trigger: 'change' }
  ],
  maintainDate: [
    { required: true, message: '请选择保养日期', trigger: 'change' }
  ],
  maintainPerson: [
    { required: true, message: '请输入保养人', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入保养内容', trigger: 'blur' }
  ]
}

const equipmentList = ref([
  { id: 1, equipmentCode: 'EQ-001', equipmentName: 'SMT贴片机01' },
  { id: 2, equipmentCode: 'EQ-002', equipmentName: 'SMT贴片机02' },
  { id: 3, equipmentCode: 'EQ-003', equipmentName: '回流焊机01' },
  { id: 4, equipmentCode: 'EQ-004', equipmentName: '回流焊机02' },
  { id: 5, equipmentCode: 'EQ-005', equipmentName: '自动组装线01' },
  { id: 6, equipmentCode: 'EQ-006', equipmentName: '自动组装线02' },
  { id: 7, equipmentCode: 'EQ-007', equipmentName: '测试台01' },
  { id: 8, equipmentCode: 'EQ-008', equipmentName: '测试台02' },
  { id: 9, equipmentCode: 'EQ-009', equipmentName: '自动包装机01' },
  { id: 10, equipmentCode: 'EQ-010', equipmentName: '自动包装机02' }
])

const mockData = ref([
  {
    id: 1,
    maintainNo: 'EM-20240115001',
    equipmentId: 1,
    equipmentCode: 'EQ-001',
    equipmentName: 'SMT贴片机01',
    maintainType: 2,
    maintainDate: '2024-01-15',
    maintainPerson: '王工',
    costHours: 2,
    status: 1,
    content: '清洁设备表面及内部灰尘，检查吸嘴磨损情况，润滑丝杆导轨',
    remark: '保养完成，设备运行正常',
    createTime: '2024-01-15 08:00:00'
  },
  {
    id: 2,
    maintainNo: 'EM-20240114001',
    equipmentId: 3,
    equipmentCode: 'EQ-003',
    equipmentName: '回流焊机01',
    maintainType: 3,
    maintainDate: '2024-01-14',
    maintainPerson: '李工',
    costHours: 4,
    status: 1,
    content: '检查加热模块校准，清洁炉膛，更换过滤网，检查传送带',
    remark: '温区校准完成',
    createTime: '2024-01-14 09:00:00'
  },
  {
    id: 3,
    maintainNo: 'EM-20240116001',
    equipmentId: 5,
    equipmentCode: 'EQ-005',
    equipmentName: '自动组装线01',
    maintainType: 1,
    maintainDate: '2024-01-16',
    maintainPerson: '张工',
    costHours: 1,
    status: 0,
    content: '日常清洁，检查传感器',
    remark: '',
    createTime: '2024-01-16 07:30:00'
  },
  {
    id: 4,
    maintainNo: 'EM-20240113001',
    equipmentId: 9,
    equipmentCode: 'EQ-009',
    equipmentName: '自动包装机01',
    maintainType: 2,
    maintainDate: '2024-01-13',
    maintainPerson: '赵工',
    costHours: 1.5,
    status: 1,
    content: '清洁切刀检查，热封模块清洁，皮带张紧度调整',
    remark: '',
    createTime: '2024-01-13 14:00:00'
  },
  {
    id: 5,
    maintainNo: 'EM-20240117001',
    equipmentId: 2,
    equipmentCode: 'EQ-002',
    equipmentName: 'SMT贴片机02',
    maintainType: 4,
    maintainDate: '2024-01-17',
    maintainPerson: '王工',
    costHours: 8,
    status: 0,
    content: '二级保养：全面检查，更换易损件，精度校准',
    remark: '计划明天开始',
    createTime: '2024-01-12 10:00:00'
  }
])

const tableData = ref([])

const dialogTitle = computed(() => {
  if (isView.value) return '保养详情'
  return isEdit.value ? '编辑保养记录' : '新增保养记录'
})

function getMaintainTypeText(type) {
  const texts = ['', '日常保养', '定期保养', '一级保养', '二级保养']
  return texts[type] || '未知'
}

function getMaintainTypeTag(type) {
  const types = ['', 'info', 'primary', 'warning', 'danger']
  return types[type] || 'info'
}

function getStatusType(status) {
  const types = ['warning', 'success']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['待执行', '已完成']
  return texts[status] || '未知'
}

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filtered = [...mockData.value]
    if (searchForm.equipmentCode) {
      filtered = filtered.filter(item => item.equipmentCode.includes(searchForm.equipmentCode))
    }
    if (searchForm.equipmentName) {
      filtered = filtered.filter(item => item.equipmentName.includes(searchForm.equipmentName))
    }
    if (searchForm.maintainType !== '') {
      filtered = filtered.filter(item => item.maintainType === searchForm.maintainType)
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
  searchForm.equipmentCode = ''
  searchForm.equipmentName = ''
  searchForm.maintainType = ''
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
  Object.assign(maintainForm, JSON.parse(JSON.stringify(row)))
  dialogVisible.value = true
}

function handleView(row) {
  isEdit.value = false
  isView.value = true
  Object.assign(maintainForm, JSON.parse(JSON.stringify(row)))
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除保养记录「${row.maintainNo}」吗？`, '提示', {
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

function handleEquipmentChange(val) {
  const equip = equipmentList.value.find(e => e.id === val)
  if (equip) {
    maintainForm.equipmentCode = equip.equipmentCode
    maintainForm.equipmentName = equip.equipmentName
  }
}

function resetForm() {
  maintainForm.id = null
  maintainForm.maintainNo = ''
  maintainForm.equipmentId = null
  maintainForm.equipmentCode = ''
  maintainForm.equipmentName = ''
  maintainForm.maintainType = 1
  maintainForm.maintainDate = ''
  maintainForm.maintainPerson = ''
  maintainForm.costHours = 0
  maintainForm.content = ''
  maintainForm.remark = ''
  if (maintainFormRef.value) {
    maintainFormRef.value.resetFields()
  }
}

async function handleSubmit() {
  if (!maintainFormRef.value) return
  
  try {
    await maintainFormRef.value.validate()
    submitLoading.value = true
    
    setTimeout(() => {
      if (isEdit.value) {
        const index = mockData.value.findIndex(item => item.id === maintainForm.id)
        if (index !== -1) {
          mockData.value[index] = {
            ...mockData.value[index],
            equipmentId: maintainForm.equipmentId,
            equipmentCode: maintainForm.equipmentCode,
            equipmentName: maintainForm.equipmentName,
            maintainType: maintainForm.maintainType,
            maintainDate: maintainForm.maintainDate,
            maintainPerson: maintainForm.maintainPerson,
            costHours: maintainForm.costHours,
            content: maintainForm.content,
            remark: maintainForm.remark
          }
        }
        ElMessage.success('编辑成功')
      } else {
        const maxId = Math.max(...mockData.value.map(item => item.id), 0)
        const now = new Date()
        const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
        const newItem = {
          id: maxId + 1,
          maintainNo: 'EM-' + dateStr + String(maxId + 1).padStart(3, '0'),
          equipmentId: maintainForm.equipmentId,
          equipmentCode: maintainForm.equipmentCode,
          equipmentName: maintainForm.equipmentName,
          maintainType: maintainForm.maintainType,
          maintainDate: maintainForm.maintainDate,
          maintainPerson: maintainForm.maintainPerson,
          costHours: maintainForm.costHours,
          status: 0,
          content: maintainForm.content,
          remark: maintainForm.remark,
          createTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
        }
        mockData.value.unshift(newItem)
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
</style>
