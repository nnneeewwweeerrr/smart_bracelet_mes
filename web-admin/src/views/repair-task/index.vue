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
        <el-form-item label="任务编号">
          <el-input v-model="searchForm.taskNo" placeholder="请输入任务编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="设备编号">
          <el-input v-model="searchForm.equipmentNo" placeholder="请输入设备编号" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="searchForm.priority" placeholder="请选择" clearable style="width: 120px">
            <el-option label="紧急" value="major" />
            <el-option label="一般" value="minor" />
            <el-option label="轻微" value="warning" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 130px">
            <el-option label="待派工" :value="0" />
            <el-option label="已派工" :value="1" />
            <el-option label="维修中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已验收" :value="4" />
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
        <span class="table-title">维修任务列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增任务
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="taskNo" label="任务编号" width="170" />
        <el-table-column prop="equipmentNo" label="设备编号" width="110" />
        <el-table-column prop="equipmentName" label="设备名称" min-width="140" />
        <el-table-column prop="priority" label="优先级" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getPriorityTag(row.priority)" size="small">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="faultDescription" label="故障描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="assignee" label="维修人" width="100" />
        <el-table-column prop="assignTime" label="派工时间" width="160" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status === 0" type="warning" link @click="handleAssign(row)">派工</el-button>
            <el-button v-if="row.status === 1" type="success" link @click="handleAccept(row)">接单</el-button>
            <el-button v-if="row.status === 2" type="primary" link @click="handleComplete(row)">完成维修</el-button>
            <el-button v-if="row.status === 3" type="success" link @click="handleAcceptance(row)">验收</el-button>
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

    <el-dialog v-model="detailVisible" title="维修任务详情" width="750px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务编号">{{ currentDetail.taskNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(currentDetail.status)">
            {{ getStatusText(currentDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="设备编号">{{ currentDetail.equipmentNo }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentDetail.equipmentName }}</el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityTag(currentDetail.priority)">
            {{ getPriorityText(currentDetail.priority) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="维修费用">{{ currentDetail.repairCost }} 元</el-descriptions-item>
        <el-descriptions-item label="报修人">{{ currentDetail.reporter }}</el-descriptions-item>
        <el-descriptions-item label="报修时间">{{ currentDetail.reportTime }}</el-descriptions-item>
        <el-descriptions-item label="维修人">{{ currentDetail.assignee }}</el-descriptions-item>
        <el-descriptions-item label="派工时间">{{ currentDetail.assignTime }}</el-descriptions-item>
        <el-descriptions-item label="接单时间">{{ currentDetail.acceptTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentDetail.startTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ currentDetail.completeTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="验收结果">
          <el-tag v-if="currentDetail.acceptanceResult === 'pass'" type="success">通过</el-tag>
          <el-tag v-else-if="currentDetail.acceptanceResult === 'fail'" type="danger">不通过</el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">{{ currentDetail.faultDescription }}</el-descriptions-item>
        <el-descriptions-item label="维修内容" :span="2" v-if="currentDetail.repairContent">
          {{ currentDetail.repairContent }}
        </el-descriptions-item>
        <el-descriptions-item label="使用配件" :span="2" v-if="currentDetail.partsUsed">
          {{ currentDetail.partsUsed }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentDetail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignVisible" title="派工" width="500px" :close-on-click-modal="false">
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="100px">
        <el-form-item label="故障ID" v-if="assignForm.faultId">
          <span>{{ assignForm.faultId }}</span>
        </el-form-item>
        <el-form-item label="设备" prop="equipmentId">
          <el-select v-model="assignForm.equipmentId" placeholder="请选择设备" style="width: 100%" filterable>
            <el-option
              v-for="item in equipmentList"
              :key="item.id"
              :label="`${item.equipmentNo} - ${item.equipmentName}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
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
            <el-option label="张维修" :value="10" />
            <el-option label="李维修" :value="11" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="assignForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleAssignSubmit">确定派工</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="completeVisible" title="完成维修" width="550px" :close-on-click-modal="false">
      <el-form ref="completeFormRef" :model="completeForm" :rules="completeRules" label-width="100px">
        <el-form-item label="维修内容" prop="repairContent">
          <el-input v-model="completeForm.repairContent" type="textarea" :rows="4" placeholder="请详细描述维修内容" />
        </el-form-item>
        <el-form-item label="使用配件">
          <el-input v-model="completeForm.partsUsed" placeholder="请输入使用的配件，多个用逗号分隔" />
        </el-form-item>
        <el-form-item label="维修费用(元)" prop="repairCost">
          <el-input-number v-model="completeForm.repairCost" :min="0" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="completeForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleCompleteSubmit">提交完成</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="acceptanceVisible" title="验收维修任务" width="500px" :close-on-click-modal="false">
      <el-form ref="acceptanceFormRef" :model="acceptanceForm" :rules="acceptanceRules" label-width="100px">
        <el-form-item label="验收结果" prop="acceptanceResult">
          <el-radio-group v-model="acceptanceForm.acceptanceResult">
            <el-radio value="pass">通过</el-radio>
            <el-radio value="fail">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="验收人" prop="acceptanceBy">
          <el-input v-model="acceptanceForm.acceptanceBy" placeholder="请输入验收人姓名" />
        </el-form-item>
        <el-form-item label="验收意见">
          <el-input v-model="acceptanceForm.remark" type="textarea" :rows="3" placeholder="请输入验收意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="acceptanceVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleAcceptanceSubmit">确认验收</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { demoEquipment, demoRepairTask } from '@/api/demo'

const loading = ref(false)
const submitLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const detailVisible = ref(false)
const assignVisible = ref(false)
const completeVisible = ref(false)
const acceptanceVisible = ref(false)

const assignFormRef = ref(null)
const completeFormRef = ref(null)
const acceptanceFormRef = ref(null)

const tableData = ref([])
const equipmentList = ref([])
const currentDetail = ref({})

const statCards = ref([
  { title: '待派工', value: 0, icon: 'Warning', color: '#f56c6c', status: 0 },
  { title: '已派工', value: 0, icon: 'Clock', color: '#e6a23c', status: 1 },
  { title: '维修中', value: 0, icon: 'Tools', color: '#409eff', status: 2 },
  { title: '已完成', value: 0, icon: 'CircleCheck', color: '#67c23a', status: 3 },
  { title: '已验收', value: 0, icon: 'SuccessFilled', color: '#909399', status: 4 }
])

const searchForm = reactive({
  taskNo: '',
  equipmentNo: '',
  priority: '',
  status: null
})

const assignForm = reactive({
  faultId: null,
  equipmentId: null,
  faultDescription: '',
  priority: 'minor',
  repairerId: null,
  remark: ''
})

const completeForm = reactive({
  id: null,
  repairContent: '',
  partsUsed: '',
  repairCost: 0,
  remark: ''
})

const acceptanceForm = reactive({
  id: null,
  acceptanceResult: 'pass',
  acceptanceBy: '',
  remark: ''
})

const assignRules = {
  equipmentId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  faultDescription: [{ required: true, message: '请输入故障描述', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  repairerId: [{ required: true, message: '请选择维修人员', trigger: 'change' }]
}

const completeRules = {
  repairContent: [{ required: true, message: '请输入维修内容', trigger: 'blur' }],
  repairCost: [{ required: true, message: '请输入维修费用', trigger: 'blur' }]
}

const acceptanceRules = {
  acceptanceResult: [{ required: true, message: '请选择验收结果', trigger: 'change' }],
  acceptanceBy: [{ required: true, message: '请输入验收人', trigger: 'blur' }]
}

function getPriorityTag(priority) {
  const map = { major: 'danger', minor: 'warning', warning: 'info' }
  return map[priority] || 'info'
}

function getPriorityText(priority) {
  const map = { major: '紧急', minor: '一般', warning: '轻微' }
  return map[priority] || priority
}

function getStatusTag(status) {
  const map = { 0: 'danger', 1: 'warning', 2: 'primary', 3: 'success', 4: 'info' }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = { 0: '待派工', 1: '已派工', 2: '维修中', 3: '已完成', 4: '已验收' }
  return map[status] || '未知'
}

async function fetchStats() {
  try {
    const res = await demoRepairTask.getStatistics()
    if (res.code === 200 && res.data) {
      statCards.value[0].value = res.data.pending || 0
      statCards.value[1].value = res.data.assigned || 0
      statCards.value[2].value = res.data.repairing || 0
      statCards.value[3].value = res.data.completed || 0
      statCards.value[4].value = res.data.accepted || 0
    }
  } catch (e) {
    console.error('获取统计失败:', e)
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      taskNo: searchForm.taskNo || undefined,
      priority: searchForm.priority || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    }
    const res = await demoRepairTask.getList(params)
    if (res.code === 200) {
      tableData.value = res.data.records || res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

async function fetchEquipment() {
  try {
    const res = await demoEquipment.getAll()
    if (res.code === 200) {
      equipmentList.value = res.data || []
    }
  } catch (e) {
    console.error('加载设备列表失败:', e)
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.taskNo = ''
  searchForm.equipmentNo = ''
  searchForm.priority = ''
  searchForm.status = null
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
  searchForm.status = item.status !== undefined ? item.status : null
  currentPage.value = 1
  fetchData()
}

async function handleView(row) {
  try {
    const res = await demoRepairTask.getById(row.id)
    if (res.code === 200) {
      currentDetail.value = res.data || {}
      detailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

function handleAdd() {
  assignForm.faultId = null
  assignForm.equipmentId = null
  assignForm.faultDescription = ''
  assignForm.priority = 'minor'
  assignForm.repairerId = null
  assignForm.remark = ''
  assignVisible.value = true
}

function handleAssign(row) {
  assignForm.faultId = row.faultId || null
  assignForm.equipmentId = row.equipmentId
  assignForm.faultDescription = row.faultDescription || row.faultDesc || ''
  assignForm.priority = row.priority || 'minor'
  assignForm.repairerId = row.repairerId || null
  assignForm.remark = ''
  assignVisible.value = true
}

async function handleAssignSubmit() {
  if (!assignFormRef.value) return
  try {
    await assignFormRef.value.validate()
    submitLoading.value = true
    const res = await demoRepairTask.save({
      faultId: assignForm.faultId,
      equipmentId: assignForm.equipmentId,
      faultDescription: assignForm.faultDescription,
      priority: assignForm.priority,
      assignee: assignForm.repairerId === 10 ? '张维修' : '李维修',
      remark: assignForm.remark
    })
    if (res.code === 200) {
      ElMessage.success('派工成功')
      assignVisible.value = false
      fetchData()
      fetchStats()
    } else {
      ElMessage.error(res.message || '派工失败')
    }
  } catch (e) {
    if (e !== false) {
      ElMessage.error('派工失败')
    }
  } finally {
    submitLoading.value = false
  }
}

async function handleAccept(row) {
  try {
    await ElMessageBox.confirm('确定要接单吗？', '确认接单', { type: 'info' })
    const res = await demoRepairTask.accept(row.id)
    if (res.code === 200) {
      ElMessage.success('接单成功')
      fetchData()
      fetchStats()
    } else {
      ElMessage.error(res.message || '接单失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('接单失败')
    }
  }
}

function handleComplete(row) {
  completeForm.id = row.id
  completeForm.repairContent = ''
  completeForm.partsUsed = ''
  completeForm.repairCost = 0
  completeForm.remark = ''
  completeVisible.value = true
}

async function handleCompleteSubmit() {
  if (!completeFormRef.value) return
  try {
    await completeFormRef.value.validate()
    submitLoading.value = true
    const res = await demoRepairTask.complete(completeForm.id, {
      repairContent: completeForm.repairContent,
      partsUsed: completeForm.partsUsed,
      repairCost: completeForm.repairCost,
      remark: completeForm.remark
    })
    if (res.code === 200) {
      ElMessage.success('提交成功')
      completeVisible.value = false
      fetchData()
      fetchStats()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (e) {
    if (e !== false) {
      ElMessage.error('提交失败')
    }
  } finally {
    submitLoading.value = false
  }
}

function handleAcceptance(row) {
  acceptanceForm.id = row.id
  acceptanceForm.acceptanceResult = 'pass'
  acceptanceForm.acceptanceBy = ''
  acceptanceForm.remark = ''
  acceptanceVisible.value = true
}

async function handleAcceptanceSubmit() {
  if (!acceptanceFormRef.value) return
  try {
    await acceptanceFormRef.value.validate()
    submitLoading.value = true
    const res = await demoRepairTask.acceptance(acceptanceForm.id, {
      acceptanceResult: acceptanceForm.acceptanceResult,
      acceptanceBy: acceptanceForm.acceptanceBy,
      remark: acceptanceForm.remark
    })
    if (res.code === 200) {
      ElMessage.success('验收完成')
      acceptanceVisible.value = false
      fetchData()
      fetchStats()
    } else {
      ElMessage.error(res.message || '验收失败')
    }
  } catch (e) {
    if (e !== false) {
      ElMessage.error('验收失败')
    }
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除任务「${row.taskNo}」吗？`, '提示', { type: 'warning' })
    const res = await demoRepairTask.remove(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
      fetchStats()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchEquipment()
  fetchStats()
  fetchData()
})
</script>

<style scoped>
.page-container { width: 100%; }
.stat-row { margin-bottom: 20px; }
.stat-card { border-radius: 8px; cursor: pointer; transition: all 0.3s; }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.stat-content { display: flex; align-items: center; gap: 14px; }
.stat-icon {
  width: 52px; height: 52px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; flex-shrink: 0;
}
.stat-info { flex: 1; }
.stat-value { font-size: 26px; font-weight: bold; color: #303133; line-height: 1.2; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
.search-card { margin-bottom: 20px; }
.table-card { margin-bottom: 20px; }
.table-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px;
}
.table-title { font-size: 16px; font-weight: bold; color: #303133; }
.pagination-wrapper {
  margin-top: 20px; display: flex; justify-content: flex-end;
}
</style>
