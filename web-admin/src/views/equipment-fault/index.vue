<template>
  <div class="page-container">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="item in statCards" :key="item.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: item.color }">
              <el-icon :size="28"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="故障编号">
          <el-input v-model="searchForm.faultNo" placeholder="请输入故障编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="searchForm.equipmentName" placeholder="请输入设备名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="故障等级">
          <el-select v-model="searchForm.faultLevel" placeholder="全部等级" clearable style="width: 140px">
            <el-option v-for="item in faultLevelOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障状态">
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
        <span class="table-title">设备故障列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          故障报修
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="faultNo" label="故障编号" width="160" />
        <el-table-column prop="equipmentName" label="设备名称" min-width="160" />
        <el-table-column prop="faultLevel" label="故障等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getFaultLevelTag(row.faultLevel)" size="small">
              {{ getFaultLevelText(row.faultLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="faultDesc" label="故障描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reporter" label="报修人" width="100" />
        <el-table-column prop="reportTime" label="报修时间" width="170" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button
              type="warning"
              link
              @click="handleProcess(row)"
              :disabled="row.status === 2 || row.status === 3"
            >
              处理
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
      :title="dialogTitle"
      width="650px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="faultFormRef"
        :model="faultForm"
        :rules="faultRules"
        label-width="100px"
        :disabled="isView"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备" prop="equipmentId">
              <el-select v-model="faultForm.equipmentId" placeholder="请选择设备" style="width: 100%" @change="handleEquipmentChange">
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
            <el-form-item label="故障等级" prop="faultLevel">
              <el-select v-model="faultForm.faultLevel" placeholder="请选择等级" style="width: 100%">
                <el-option label="一般" :value="1" />
                <el-option label="严重" :value="2" />
                <el-option label="紧急" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="报修人" prop="reporter">
              <el-input v-model="faultForm.reporter" placeholder="请输入报修人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所在工序" prop="process">
              <el-input v-model="faultForm.process" placeholder="请输入工序" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="故障描述" prop="faultDesc">
          <el-input v-model="faultForm.faultDesc" type="textarea" :rows="4" placeholder="请详细描述故障现象" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="faultForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ isView ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewVisible" title="故障详情" width="700px">
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="故障编号">{{ currentItem.faultNo }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentItem.equipmentName }}</el-descriptions-item>
        <el-descriptions-item label="故障等级">
          <el-tag :type="getFaultLevelTag(currentItem.faultLevel)" size="small">
            {{ getFaultLevelText(currentItem.faultLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="所在工序">{{ currentItem.process }}</el-descriptions-item>
        <el-descriptions-item label="报修人">{{ currentItem.reporter }}</el-descriptions-item>
        <el-descriptions-item label="报修时间">{{ currentItem.reportTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentItem.status)" size="small">
            {{ getStatusText(currentItem.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理人" v-if="currentItem.handler">
          {{ currentItem.handler }}
        </el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">{{ currentItem.faultDesc }}</el-descriptions-item>
        <el-descriptions-item label="处理方案" :span="2" v-if="currentItem.solution">
          {{ currentItem.solution }}
        </el-descriptions-item>
        <el-descriptions-item label="处理时间" v-if="currentItem.handleTime">
          {{ currentItem.handleTime }}
        </el-descriptions-item>
        <el-descriptions-item label="维修时长(h)" v-if="currentItem.repairHours">
          {{ currentItem.repairHours }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentItem.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="processVisible"
      title="故障处理"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="processFormRef" :model="processForm" :rules="processRules" label-width="100px">
        <el-form-item label="处理状态" prop="status">
          <el-radio-group v-model="processForm.status">
            <el-radio :value="1">维修中</el-radio>
            <el-radio :value="2">已修复</el-radio>
            <el-radio :value="3">无法修复</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理人" prop="handler">
          <el-input v-model="processForm.handler" placeholder="请输入处理人" />
        </el-form-item>
        <el-form-item label="维修时长(小时)" prop="repairHours" v-if="processForm.status === 2">
          <el-input-number v-model="processForm.repairHours" :min="0" :precision="1" :step="0.5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="处理方案" prop="solution">
          <el-input v-model="processForm.solution" type="textarea" :rows="4" placeholder="请输入处理方案" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processVisible = false">取消</el-button>
        <el-button type="primary" :loading="processSubmitLoading" @click="handleProcessSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const processSubmitLoading = ref(false)
const dialogVisible = ref(false)
const viewVisible = ref(false)
const processVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false)
const faultFormRef = ref(null)
const processFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  faultNo: '',
  equipmentName: '',
  faultLevel: '',
  status: ''
})

const faultForm = reactive({
  id: null,
  faultNo: '',
  equipmentId: null,
  equipmentName: '',
  faultLevel: 1,
  faultDesc: '',
  reporter: '',
  process: '',
  remark: ''
})

const currentItem = reactive({})

const processForm = reactive({
  id: null,
  status: 1,
  handler: '',
  repairHours: 0,
  solution: ''
})

const faultLevelOptions = [
  { label: '一般', value: 1 },
  { label: '严重', value: 2 },
  { label: '紧急', value: 3 }
]

const statusOptions = [
  { label: '待处理', value: 0 },
  { label: '维修中', value: 1 },
  { label: '已修复', value: 2 },
  { label: '无法修复', value: 3 }
]

const faultRules = {
  equipmentId: [
    { required: true, message: '请选择设备', trigger: 'change' }
  ],
  faultLevel: [
    { required: true, message: '请选择故障等级', trigger: 'change' }
  ],
  faultDesc: [
    { required: true, message: '请输入故障描述', trigger: 'blur' }
  ],
  reporter: [
    { required: true, message: '请输入报修人', trigger: 'blur' }
  ]
}

const processRules = {
  status: [
    { required: true, message: '请选择处理状态', trigger: 'change' }
  ],
  handler: [
    { required: true, message: '请输入处理人', trigger: 'blur' }
  ],
  solution: [
    { required: true, message: '请输入处理方案', trigger: 'blur' }
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
    faultNo: 'EF-20240115001',
    equipmentId: 4,
    equipmentName: '回流焊机02',
    faultLevel: 3,
    faultDesc: '设备突然跳闸，无法启动，疑似加热模块故障',
    reporter: '李操作',
    process: 'SMT车间B区',
    status: 1,
    handler: '王维修',
    solution: '正在检查加热模块，预计今天下午修好',
    reportTime: '2024-01-15 09:30:00',
    remark: '紧急，影响生产'
  },
  {
    id: 2,
    faultNo: 'EF-20240114001',
    equipmentId: 7,
    equipmentName: '测试台01',
    faultLevel: 2,
    faultDesc: '心率测试模块读数不准，偏差较大',
    reporter: '张测试',
    process: '测试车间',
    status: 2,
    handler: '赵维修',
    solution: '已更换传感器模块，校准完成',
    repairHours: 3,
    handleTime: '2024-01-14 16:00:00',
    reportTime: '2024-01-14 10:00:00',
    remark: ''
  },
  {
    id: 3,
    faultNo: 'EF-20240113001',
    equipmentId: 9,
    equipmentName: '自动包装机01',
    faultLevel: 1,
    faultDesc: '热封温度偶尔不稳定，包装袋封口不平整',
    reporter: '周包装',
    process: '包装车间',
    status: 0,
    reportTime: '2024-01-13 14:30:00',
    remark: '问题不大，但需要处理'
  },
  {
    id: 4,
    faultNo: 'EF-20240112001',
    equipmentId: 2,
    equipmentName: 'SMT贴片机02',
    faultLevel: 2,
    faultDesc: '吸嘴磨损，贴片偏移，需要更换吸嘴',
    reporter: '郑操作',
    process: 'SMT车间A区',
    status: 2,
    handler: '王维修',
    solution: '已更换吸嘴，校准完成',
    repairHours: 1.5,
    handleTime: '2024-01-12 11:00:00',
    reportTime: '2024-01-12 08:30:00',
    remark: ''
  },
  {
    id: 5,
    faultNo: 'EF-20240115002',
    equipmentId: 5,
    equipmentName: '自动组装线01',
    faultLevel: 3,
    faultDesc: '传送皮带断裂，整条线停产',
    reporter: '孙组长',
    process: '组装车间A区',
    status: 3,
    handler: '李维修',
    solution: '皮带已老化，需要订购新皮带，预计3天后到货',
    handleTime: '2024-01-15 11:00:00',
    reportTime: '2024-01-15 10:00:00',
    remark: '已安排使用备用线'
  }
])

const statCards = computed(() => [
  { title: '待处理', value: pendingCount.value, icon: 'Warning', color: '#F56C6C' },
  { title: '维修中', value: repairingCount.value, icon: 'Clock', color: '#E6A23C' },
  { title: '已修复', value: fixedCount.value, icon: 'CircleCheck', color: '#67C23A' },
  { title: '本月累计', value: mockData.value.length, icon: 'Document', color: '#409EFF' }
])

const pendingCount = computed(() => mockData.value.filter(item => item.status === 0).length)
const repairingCount = computed(() => mockData.value.filter(item => item.status === 1).length)
const fixedCount = computed(() => mockData.value.filter(item => item.status === 2).length)

const tableData = ref([])

const dialogTitle = computed(() => {
  if (isView.value) return '故障详情'
  return isEdit.value ? '编辑故障' : '故障报修'
})

function getFaultLevelText(level) {
  const texts = ['', '一般', '严重', '紧急']
  return texts[level] || '未知'
}

function getFaultLevelTag(level) {
  const types = ['', 'info', 'warning', 'danger']
  return types[level] || 'info'
}

function getStatusType(status) {
  const types = ['danger', 'warning', 'success', 'info']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['待处理', '维修中', '已修复', '无法修复']
  return texts[status] || '未知'
}

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filtered = [...mockData.value]
    if (searchForm.faultNo) {
      filtered = filtered.filter(item => item.faultNo.includes(searchForm.faultNo))
    }
    if (searchForm.equipmentName) {
      filtered = filtered.filter(item => item.equipmentName.includes(searchForm.equipmentName))
    }
    if (searchForm.faultLevel !== '') {
      filtered = filtered.filter(item => item.faultLevel === searchForm.faultLevel)
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
  searchForm.faultNo = ''
  searchForm.equipmentName = ''
  searchForm.faultLevel = ''
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

function handleView(row) {
  Object.assign(currentItem, row)
  viewVisible.value = true
}

function handleProcess(row) {
  processForm.id = row.id
  processForm.status = row.status === 0 ? 1 : row.status
  processForm.handler = row.handler || ''
  processForm.repairHours = row.repairHours || 0
  processForm.solution = row.solution || ''
  processVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除故障记录「${row.faultNo}」吗？`, '提示', {
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
    faultForm.equipmentName = equip.equipmentName
  }
}

function resetForm() {
  faultForm.id = null
  faultForm.faultNo = ''
  faultForm.equipmentId = null
  faultForm.equipmentName = ''
  faultForm.faultLevel = 1
  faultForm.faultDesc = ''
  faultForm.reporter = ''
  faultForm.process = ''
  faultForm.remark = ''
  if (faultFormRef.value) {
    faultFormRef.value.resetFields()
  }
}

async function handleSubmit() {
  if (!faultFormRef.value) return
  
  try {
    await faultFormRef.value.validate()
    submitLoading.value = true
    
    setTimeout(() => {
      if (isEdit.value) {
        const index = mockData.value.findIndex(item => item.id === faultForm.id)
        if (index !== -1) {
          mockData.value[index] = {
            ...mockData.value[index],
            equipmentId: faultForm.equipmentId,
            equipmentName: faultForm.equipmentName,
            faultLevel: faultForm.faultLevel,
            faultDesc: faultForm.faultDesc,
            reporter: faultForm.reporter,
            process: faultForm.process,
            remark: faultForm.remark
          }
        }
        ElMessage.success('编辑成功')
      } else {
        const maxId = Math.max(...mockData.value.map(item => item.id), 0)
        const now = new Date()
        const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
        const newItem = {
          id: maxId + 1,
          faultNo: 'EF-' + dateStr + String(maxId + 1).padStart(3, '0'),
          equipmentId: faultForm.equipmentId,
          equipmentName: faultForm.equipmentName,
          faultLevel: faultForm.faultLevel,
          faultDesc: faultForm.faultDesc,
          reporter: faultForm.reporter,
          process: faultForm.process,
          status: 0,
          reportTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
          remark: faultForm.remark
        }
        mockData.value.unshift(newItem)
        ElMessage.success('报修成功')
      }
      
      dialogVisible.value = false
      submitLoading.value = false
      fetchData()
    }, 300)
  } catch (error) {
    submitLoading.value = false
  }
}

async function handleProcessSubmit() {
  if (!processFormRef.value) return
  
  try {
    await processFormRef.value.validate()
    processSubmitLoading.value = true
    
    setTimeout(() => {
      const item = mockData.value.find(i => i.id === processForm.id)
      if (item) {
        item.status = processForm.status
        item.handler = processForm.handler
        item.solution = processForm.solution
        if (processForm.status === 2) {
          item.repairHours = processForm.repairHours
          item.handleTime = new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
        } else if (processForm.status === 3) {
          item.handleTime = new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
        }
      }
      ElMessage.success('处理成功')
      processVisible.value = false
      processSubmitLoading.value = false
      fetchData()
    }, 300)
  } catch (error) {
    processSubmitLoading.value = false
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

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
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
