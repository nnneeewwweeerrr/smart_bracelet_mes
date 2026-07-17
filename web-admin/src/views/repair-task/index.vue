<template>
  <div class="repair-task-container">
    <div class="page-header">
      <h2>维修任务管理</h2>
      <el-button type="primary" @click="handleAdd">新增维修任务</el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="4">
        <el-card class="stat-card" :body-style="{ padding: '12px' }">
          <div class="stat-label">全部</div>
          <div class="stat-value">{{ statistics.total || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card stat-pending" :body-style="{ padding: '12px' }">
          <div class="stat-label">待派工</div>
          <div class="stat-value">{{ statistics.pending || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card stat-assigned" :body-style="{ padding: '12px' }">
          <div class="stat-label">已派工</div>
          <div class="stat-value">{{ statistics.assigned || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card stat-repairing" :body-style="{ padding: '12px' }">
          <div class="stat-label">维修中</div>
          <div class="stat-value">{{ statistics.repairing || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card stat-completed" :body-style="{ padding: '12px' }">
          <div class="stat-label">已完成</div>
          <div class="stat-value">{{ statistics.completed || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card stat-accepted" :body-style="{ padding: '12px' }">
          <div class="stat-label">已验收</div>
          <div class="stat-value">{{ statistics.accepted || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="任务编号">
          <el-input v-model="searchForm.taskNo" placeholder="请输入任务编号" clearable />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="searchForm.equipmentName" placeholder="请输入设备名称" clearable />
        </el-form-item>
        <el-form-item label="故障类型">
          <el-select v-model="searchForm.faultType" placeholder="请选择" clearable>
            <el-option label="机械故障" value="机械故障" />
            <el-option label="电气故障" value="电气故障" />
            <el-option label="软件故障" value="软件故障" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="待派工" :value="0" />
            <el-option label="已派工" :value="1" />
            <el-option label="维修中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已验收" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card>
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="taskNo" label="任务编号" width="130" />
        <el-table-column prop="equipmentName" label="设备名称" width="140" />
        <el-table-column prop="faultType" label="故障类型" width="100" />
        <el-table-column prop="faultLevel" label="故障等级" width="90" />
        <el-table-column prop="faultDesc" label="故障描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="reporter" label="报修人" width="90" />
        <el-table-column prop="assignee" label="维修人" width="90" />
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.priority === 1" type="danger">紧急</el-tag>
            <el-tag v-else-if="row.priority === 2" type="warning">高</el-tag>
            <el-tag v-else type="info">普通</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0">待派工</el-tag>
            <el-tag v-else-if="row.status === 1" type="warning">已派工</el-tag>
            <el-tag v-else-if="row.status === 2" type="primary">维修中</el-tag>
            <el-tag v-else-if="row.status === 3" type="success">已完成</el-tag>
            <el-tag v-else type="info">已验收</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reportTime" label="报修时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="primary" size="small" @click="handleAssign(row)">派工</el-button>
            <el-button v-if="row.status === 1" type="primary" size="small" @click="handleAccept(row)">接单</el-button>
            <el-button v-if="row.status === 2" type="success" size="small" @click="handleComplete(row)">完成</el-button>
            <el-button v-if="row.status === 3" type="success" size="small" @click="handleAcceptance(row)">验收</el-button>
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pagination"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="任务编号">
          <el-input v-model="form.taskNo" placeholder="系统自动生成" disabled />
        </el-form-item>
        <el-form-item label="设备编号">
          <el-input v-model="form.equipmentNo" placeholder="请输入设备编号" />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="form.equipmentName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="故障类型">
          <el-select v-model="form.faultType" placeholder="请选择">
            <el-option label="机械故障" value="机械故障" />
            <el-option label="电气故障" value="电气故障" />
            <el-option label="软件故障" value="软件故障" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障等级">
          <el-select v-model="form.faultLevel" placeholder="请选择">
            <el-option label="一般" value="一般" />
            <el-option label="严重" value="严重" />
            <el-option label="紧急" value="紧急" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障描述">
          <el-input v-model="form.faultDesc" type="textarea" rows="2" placeholder="请输入故障描述" />
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input v-model="form.faultDescription" type="textarea" rows="2" placeholder="请输入详细描述" />
        </el-form-item>
        <el-form-item label="报修人">
          <el-input v-model="form.reporter" placeholder="请输入报修人" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority" placeholder="请选择">
            <el-option label="紧急" :value="1" />
            <el-option label="高" :value="2" />
            <el-option label="普通" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 派工弹窗 -->
    <el-dialog v-model="assignVisible" title="派工" width="400px">
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="维修人">
          <el-input v-model="assignForm.assignee" placeholder="请输入维修人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign">确定</el-button>
      </template>
    </el-dialog>

    <!-- 完成弹窗 -->
    <el-dialog v-model="completeVisible" title="完成维修" width="500px">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="维修内容">
          <el-input v-model="completeForm.repairContent" type="textarea" rows="3" placeholder="请输入维修内容" />
        </el-form-item>
        <el-form-item label="使用配件">
          <el-input v-model="completeForm.partsUsed" placeholder="请输入使用配件" />
        </el-form-item>
        <el-form-item label="维修费用">
          <el-input-number v-model="completeForm.repairCost" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="completeForm.remark" type="textarea" rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComplete">确定</el-button>
      </template>
    </el-dialog>

    <!-- 验收弹窗 -->
    <el-dialog v-model="acceptanceVisible" title="验收" width="400px">
      <el-form :model="acceptanceForm" label-width="100px">
        <el-form-item label="验收结果">
          <el-select v-model="acceptanceForm.acceptanceResult" placeholder="请选择">
            <el-option label="合格" value="合格" />
            <el-option label="不合格" value="不合格" />
          </el-select>
        </el-form-item>
        <el-form-item label="验收人">
          <el-input v-model="acceptanceForm.acceptanceBy" placeholder="请输入验收人" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="acceptanceForm.remark" type="textarea" rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="acceptanceVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAcceptance">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as repairTask from '@/api/repairTask'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const statistics = reactive({
  total: 0,
  pending: 0,
  assigned: 0,
  repairing: 0,
  completed: 0,
  accepted: 0
})

const searchForm = reactive({
  taskNo: '',
  equipmentName: '',
  faultType: '',
  status: null,
  priority: null
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = reactive({
  id: null,
  taskNo: '',
  equipmentNo: '',
  equipmentName: '',
  faultType: '',
  faultLevel: '',
  faultDesc: '',
  faultDescription: '',
  reporter: '',
  priority: 2,
  status: 0,
  remark: ''
})

const assignVisible = ref(false)
const assignForm = reactive({ id: null, assignee: '' })

const completeVisible = ref(false)
const completeForm = reactive({
  id: null,
  repairContent: '',
  partsUsed: '',
  repairCost: 0,
  remark: ''
})

const acceptanceVisible = ref(false)
const acceptanceForm = reactive({
  id: null,
  acceptanceResult: '',
  acceptanceBy: '',
  remark: ''
})

function resetForm() {
  Object.assign(form, {
    id: null,
    taskNo: '',
    equipmentNo: '',
    equipmentName: '',
    faultType: '',
    faultLevel: '',
    faultDesc: '',
    faultDescription: '',
    reporter: '',
    priority: 2,
    status: 0,
    remark: ''
  })
}

async function loadStatistics() {
  try {
    const res = await repairTask.getStatistics()
    if (res && res.data) {
      Object.assign(statistics, res.data)
    }
  } catch (e) {
    console.error('统计加载失败', e)
  }
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (searchForm.taskNo) params.taskNo = searchForm.taskNo
    if (searchForm.equipmentName) params.equipmentName = searchForm.equipmentName
    if (searchForm.faultType) params.faultType = searchForm.faultType
    if (searchForm.status !== null && searchForm.status !== '') params.status = searchForm.status
    if (searchForm.priority !== null && searchForm.priority !== '') params.priority = searchForm.priority

    const res = await repairTask.getList(params)
    if (res && res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error('数据加载失败', e)
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchForm.taskNo = ''
  searchForm.equipmentName = ''
  searchForm.faultType = ''
  searchForm.status = null
  searchForm.priority = null
  pageNum.value = 1
  loadData()
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '新增维修任务'
  dialogVisible.value = true
}

function handleEdit(row) {
  resetForm()
  dialogTitle.value = '编辑维修任务'
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (form.id) {
      await repairTask.update(form)
      ElMessage.success('修改成功')
    } else {
      await repairTask.save(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
    loadStatistics()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

function handleAssign(row) {
  assignForm.id = row.id
  assignForm.assignee = ''
  assignVisible.value = true
}

async function submitAssign() {
  try {
    await repairTask.assign({ id: assignForm.id, assignee: assignForm.assignee })
    ElMessage.success('派工成功')
    assignVisible.value = false
    loadData()
    loadStatistics()
  } catch (e) {
    ElMessage.error('派工失败')
  }
}

async function handleAccept(row) {
  try {
    await ElMessageBox.confirm('确认接单？', '提示', { type: 'warning' })
    await repairTask.accept(row.id)
    ElMessage.success('接单成功')
    loadData()
    loadStatistics()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('接单失败')
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

async function submitComplete() {
  try {
    await repairTask.complete(completeForm.id, {
      repairContent: completeForm.repairContent,
      partsUsed: completeForm.partsUsed,
      repairCost: completeForm.repairCost,
      remark: completeForm.remark
    })
    ElMessage.success('完成维修')
    completeVisible.value = false
    loadData()
    loadStatistics()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

function handleAcceptance(row) {
  acceptanceForm.id = row.id
  acceptanceForm.acceptanceResult = ''
  acceptanceForm.acceptanceBy = ''
  acceptanceForm.remark = ''
  acceptanceVisible.value = true
}

async function submitAcceptance() {
  try {
    await repairTask.acceptance(acceptanceForm.id, {
      acceptanceResult: acceptanceForm.acceptanceResult,
      acceptanceBy: acceptanceForm.acceptanceBy,
      remark: acceptanceForm.remark
    })
    ElMessage.success('验收成功')
    acceptanceVisible.value = false
    loadData()
    loadStatistics()
  } catch (e) {
    ElMessage.error('验收失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该维修任务？', '提示', { type: 'warning' })
    await repairTask.remove(row.id)
    ElMessage.success('删除成功')
    loadData()
    loadStatistics()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadData()
  loadStatistics()
})
</script>

<style scoped>
.repair-task-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
  font-size: 20px;
}
.stats-row {
  margin-bottom: 20px;
}
.stat-card {
  text-align: center;
  border-radius: 8px;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}
.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}
.stat-pending .stat-value { color: #f56c6c; }
.stat-assigned .stat-value { color: #e6a23c; }
.stat-repairing .stat-value { color: #409eff; }
.stat-completed .stat-value { color: #67c23a; }
.stat-accepted .stat-value { color: #909399; }
.search-card {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>