<template>
  <div class="page-container">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="5" v-for="item in statCards" :key="item.title">
        <el-card class="stat-card" shadow="hover" @click="handleStatClick(item)">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: item.color }">
              <el-icon :size="24"><component :is="item.icon" /></el-icon>
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
        <el-table-column prop="equipmentNo" label="设备编号" width="120" />
        <el-table-column prop="equipmentName" label="设备名称" min-width="140" />
        <el-table-column prop="faultType" label="故障类型" width="100" />
        <el-table-column prop="faultLevel" label="故障等级" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getFaultLevelTag(row.faultLevel)" size="small">
              {{ getFaultLevelText(row.faultLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="faultDesc" label="故障描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="reporter" label="报修人" width="90" />
        <el-table-column prop="reportTime" label="报修时间" width="160" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button
              v-if="row.status === 0"
              type="warning"
              link
              @click="handleAssign(row)"
            >
              派工
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
              <el-select v-model="faultForm.equipmentId" placeholder="请选择设备" style="width: 100%" filterable @change="handleEquipmentChange">
                <el-option
                  v-for="item in equipmentList"
                  :key="item.id"
                  :label="`${item.equipmentNo} - ${item.equipmentName}`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="故障类型" prop="faultType">
              <el-input v-model="faultForm.faultType" placeholder="请输入故障类型" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="故障等级" prop="faultLevel">
              <el-select v-model="faultForm.faultLevel" placeholder="请选择等级" style="width: 100%">
                <el-option label="一般" value="一般" />
                <el-option label="严重" value="严重" />
                <el-option label="紧急" value="紧急" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报修人" prop="reporter">
              <el-input v-model="faultForm.reporter" placeholder="请输入报修人" />
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
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentItem.status)" size="small">
            {{ getStatusText(currentItem.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="设备编号">{{ currentItem.equipmentNo }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentItem.equipmentName }}</el-descriptions-item>
        <el-descriptions-item label="故障类型">{{ currentItem.faultType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="故障等级">
          <el-tag :type="getFaultLevelTag(currentItem.faultLevel)" size="small">
            {{ getFaultLevelText(currentItem.faultLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="报修人">{{ currentItem.reporter }}</el-descriptions-item>
        <el-descriptions-item label="报修时间">{{ currentItem.reportTime }}</el-descriptions-item>
        <el-descriptions-item label="关联维修任务" v-if="currentItem.repairTaskId">
          {{ currentItem.repairTaskId }}
        </el-descriptions-item>
        <el-descriptions-item label="派工人员" v-if="currentItem.assignee">
          {{ currentItem.assignee }}
        </el-descriptions-item>
        <el-descriptions-item label="派工时间" v-if="currentItem.assignTime">
          {{ currentItem.assignTime }}
        </el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">{{ currentItem.faultDesc }}</el-descriptions-item>
        <el-descriptions-item label="处理方案" :span="2" v-if="currentItem.solution">
          {{ currentItem.solution }}
        </el-descriptions-item>
        <el-descriptions-item label="维修费用" v-if="currentItem.cost">
          {{ currentItem.cost }} 元
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentItem.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="assignVisible"
      title="派工"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="100px">
        <el-form-item label="故障描述" prop="faultDescription">
          <el-input v-model="assignForm.faultDescription" type="textarea" :rows="2" placeholder="请输入故障描述" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="assignForm.priority" placeholder="请选择" style="width: 100%">
            <el-option label="紧急" value="major" />
            <el-option label="一般" value="minor" />
            <el-option label="轻微" value="warning" />
          </el-select>
        </el-form-item>
        <el-form-item label="维修人员" prop="repairerId">
          <el-select v-model="assignForm.repairerId" placeholder="请选择维修人员" style="width: 100%">
            <el-option
              v-for="item in repairerList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="assignForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignLoading" @click="handleAssignSubmit">确定派工</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { demoEquipment, demoEquipmentFault, demoRepairTask } from '@/api/demo'

const loading = ref(false)
const submitLoading = ref(false)
const assignLoading = ref(false)
const dialogVisible = ref(false)
const viewVisible = ref(false)
const assignVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false)
const faultFormRef = ref(null)
const assignFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  faultNo: '',
  status: ''
})

const faultForm = reactive({
  id: null,
  faultNo: '',
  equipmentId: null,
  equipmentNo: '',
  equipmentName: '',
  faultType: '',
  faultLevel: '',
  faultDesc: '',
  reporter: '',
  remark: ''
})

const currentItem = reactive({})

const assignForm = reactive({
  faultId: null,
  equipmentId: null,
  faultDescription: '',
  priority: 'minor',
  repairerId: null,
  remark: ''
})

const faultLevelOptions = [
  { label: '一般', value: '一般' },
  { label: '严重', value: '严重' },
  { label: '紧急', value: '紧急' }
]

const statusOptions = [
  { label: '待派工', value: 0 },
  { label: '已派工', value: 1 },
  { label: '维修中', value: 2 },
  { label: '已修复', value: 3 },
  { label: '已关闭', value: 4 }
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

const assignRules = {
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  repairerId: [
    { required: true, message: '请选择维修人员', trigger: 'change' }
  ]
}

const equipmentList = ref([])

const repairerList = ref([
  { id: 10, name: '张维修' },
  { id: 11, name: '李维修' },
  { id: 12, name: '王师傅' }
])

const statCards = ref([
  { title: '待派工', value: 0, icon: 'Warning', color: '#f56c6c', status: 0 },
  { title: '已派工', value: 0, icon: 'Clock', color: '#e6a23c', status: 1 },
  { title: '维修中', value: 0, icon: 'Tools', color: '#409eff', status: 2 },
  { title: '已修复', value: 0, icon: 'CircleCheck', color: '#67c23a', status: 3 },
  { title: '已关闭', value: 0, icon: 'SuccessFilled', color: '#909399', status: 4 }
])

const tableData = ref([])

const dialogTitle = computed(() => {
  if (isView.value) return '故障详情'
  return isEdit.value ? '编辑故障' : '故障报修'
})

function getFaultLevelText(level) {
  if (!level) return '未知'
  const map = { '一般': '一般', '严重': '严重', '紧急': '紧急' }
  return map[level] || level
}

function getFaultLevelTag(level) {
  if (!level) return 'info'
  const map = { '一般': 'info', '严重': 'warning', '紧急': 'danger' }
  return map[level] || 'info'
}

function getStatusType(status) {
  const types = ['danger', 'warning', 'primary', 'success', 'info']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['待派工', '已派工', '维修中', '已修复', '已关闭']
  return texts[status] || '未知'
}

async function fetchEquipmentList() {
  try {
    const res = await demoEquipment.getAll()
    if (res.code === 200) {
      equipmentList.value = res.data || []
    }
  } catch (e) {
    console.error('加载设备列表失败:', e)
  }
}

async function fetchStats() {
  try {
    const res = await demoEquipmentFault.getStatistics()
    if (res.code === 200 && res.data) {
      statCards.value[0].value = res.data.pending || res.data.pendingCount || 0
      statCards.value[1].value = res.data.assigned || 0
      statCards.value[2].value = res.data.repairing || res.data.repairingCount || 0
      statCards.value[3].value = res.data.fixed || res.data.fixedCount || 0
      statCards.value[4].value = res.data.closed || 0
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      faultNo: searchForm.faultNo || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined
    }
    const res = await demoEquipmentFault.getList(params)
    if (res.code === 200) {
      tableData.value = res.data.records || res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.faultNo = ''
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

function handleStatClick(item) {
  searchForm.status = item.status !== undefined ? item.status : ''
  currentPage.value = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  isView.value = false
  resetForm()
  dialogVisible.value = true
}

async function handleView(row) {
  try {
    const res = await demoEquipmentFault.getById(row.id)
    if (res.code === 200) {
      Object.assign(currentItem, res.data)
      viewVisible.value = true
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除故障「${row.faultNo}」吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await demoEquipmentFault.remove(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
      fetchStats()
    } else {
      ElMessage.error(res.msg || res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

function handleEquipmentChange(val) {
  const equip = equipmentList.value.find(e => e.id === val)
  if (equip) {
    faultForm.equipmentName = equip.equipmentName
    faultForm.equipmentNo = equip.equipmentNo
  }
}

function resetForm() {
  faultForm.id = null
  faultForm.faultNo = ''
  faultForm.equipmentId = null
  faultForm.equipmentNo = ''
  faultForm.equipmentName = ''
  faultForm.faultType = ''
  faultForm.faultLevel = ''
  faultForm.faultDesc = ''
  faultForm.reporter = ''
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
    
    const data = {
      equipmentId: faultForm.equipmentId,
      equipmentNo: faultForm.equipmentNo,
      equipmentName: faultForm.equipmentName,
      faultType: faultForm.faultType,
      faultLevel: faultForm.faultLevel,
      faultDesc: faultForm.faultDesc,
      reporter: faultForm.reporter,
      remark: faultForm.remark
    }
    
    let res
    if (isEdit.value) {
      data.id = faultForm.id
      res = await demoEquipmentFault.update(data)
    } else {
      res = await demoEquipmentFault.save(data)
    }
    
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '报修成功')
      dialogVisible.value = false
      fetchData()
      fetchStats()
    } else {
      ElMessage.error(res.msg || res.message || '操作失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  } finally {
    submitLoading.value = false
  }
}

function handleAssign(row) {
  assignForm.faultId = row.id
  assignForm.equipmentId = row.equipmentId
  assignForm.faultDescription = row.faultDesc
  const levelMap = { '紧急': 'major', '严重': 'minor', '一般': 'warning' }
  assignForm.priority = levelMap[row.faultLevel] || 'minor'
  assignForm.repairerId = null
  assignForm.remark = ''
  assignVisible.value = true
}

async function handleAssignSubmit() {
  if (!assignFormRef.value) return
  
  try {
    await assignFormRef.value.validate()
    assignLoading.value = true
    
    const repairer = repairerList.value.find(r => r.id === assignForm.repairerId)
    
    const data = {
      assignee: repairer ? repairer.name : ''
    }
    
    const res = await demoEquipmentFault.dispatch(assignForm.faultId, data)
    if (res.code === 200) {
      ElMessage.success('派工成功，维修任务已创建')
      assignVisible.value = false
      fetchData()
      fetchStats()
    } else {
      ElMessage.error(res.msg || res.message || '派工失败')
    }
  } catch (error) {
    console.error('派工失败:', error)
    ElMessage.error('派工失败')
  } finally {
    assignLoading.value = false
  }
}

onMounted(() => {
  fetchEquipmentList()
  fetchData()
  fetchStats()
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