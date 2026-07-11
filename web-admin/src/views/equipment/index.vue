<template>
  <div class="page">
    <div class="search-bar">
      <el-input v-model="searchText" placeholder="搜索设备编码/名称" style="width: 260px" clearable @keyup.enter="fetchList" />
      <el-select v-model="searchStatus" placeholder="状态筛选" style="width: 120px" clearable>
        <el-option label="停用" :value="0" />
        <el-option label="运行" :value="1" />
        <el-option label="维修" :value="2" />
      </el-select>
      <el-button type="success" @click="handleAdd">新增</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="equipmentCode" label="设备编码" width="140" />
      <el-table-column prop="equipmentName" label="设备名称" min-width="160" />
      <el-table-column prop="equipmentType" label="设备类型" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="location" label="位置" width="160" />
      <el-table-column prop="purchaseDate" label="购入日期" width="120" />
      <el-table-column prop="nextMaintainDate" label="下次保养" width="140">
        <template #default="{ row }">
          <span :class="{ 'text-warning': isNeedMaintain(row.nextMaintainDate) }">{{ row.nextMaintainDate }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="runningHours" label="运行时长" width="100" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleViewMaintain(row)">保养记录</el-button>
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑设备' : '新增设备'" width="600px">
      <el-form :model="form" label-width="100px" ref="formRef" :rules="rules">
        <el-form-item label="设备编码" prop="equipmentCode">
          <el-input v-model="form.equipmentCode" placeholder="请输入设备编码" />
        </el-form-item>
        <el-form-item label="设备名称" prop="equipmentName">
          <el-input v-model="form.equipmentName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="equipmentType">
          <el-input v-model="form.equipmentType" placeholder="请输入设备类型" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="停用" :value="0" />
            <el-option label="运行" :value="1" />
            <el-option label="维修" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入位置" />
        </el-form-item>
        <el-form-item label="购入日期" prop="purchaseDate">
          <el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="上次保养日期" prop="lastMaintainDate">
          <el-date-picker v-model="form.lastMaintainDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="下次保养日期" prop="nextMaintainDate">
          <el-date-picker v-model="form.nextMaintainDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="运行时长(小时)" prop="runningHours">
          <el-input-number v-model="form.runningHours" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="maintainDialogVisible" title="设备保养记录" width="700px">
      <div style="margin-bottom: 16px">
        <el-button type="primary" @click="handleAddMaintain">新增保养记录</el-button>
      </div>
      <el-table :data="maintainList" v-loading="maintainLoading" border stripe>
        <el-table-column prop="maintainNo" label="保养单号" width="140" />
        <el-table-column prop="maintainType" label="保养类型" width="120" />
        <el-table-column prop="maintainDate" label="保养日期" width="140" />
        <el-table-column prop="maintainContent" label="保养内容" min-width="200" />
        <el-table-column prop="maintainResult" label="保养结果" width="100">
          <template #default="{ row }">
            <el-tag :type="resultType(row.maintainResult)">{{ row.maintainResult }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maintainerName" label="保养人" width="100" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" @click="handleEditMaintain(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteMaintain(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="maintainFormVisible" :title="maintainForm.id ? '编辑保养记录' : '新增保养记录'" width="600px">
        <el-form :model="maintainForm" label-width="100px" ref="maintainFormRef" :rules="maintainRules">
          <el-form-item label="保养类型" prop="maintainType">
            <el-select v-model="maintainForm.maintainType" style="width: 100%">
              <el-option label="日常保养" value="日常保养" />
              <el-option label="定期保养" value="定期保养" />
              <el-option label="专项保养" value="专项保养" />
            </el-select>
          </el-form-item>
          <el-form-item label="保养日期" prop="maintainDate">
            <el-date-picker v-model="maintainForm.maintainDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
          </el-form-item>
          <el-form-item label="保养内容" prop="maintainContent">
            <el-input v-model="maintainForm.maintainContent" type="textarea" :rows="3" placeholder="请输入保养内容" />
          </el-form-item>
          <el-form-item label="保养结果" prop="maintainResult">
            <el-select v-model="maintainForm.maintainResult" style="width: 100%">
              <el-option label="正常" value="正常" />
              <el-option label="异常" value="异常" />
              <el-option label="待处理" value="待处理" />
            </el-select>
          </el-form-item>
          <el-form-item label="保养人" prop="maintainerName">
            <el-input v-model="maintainForm.maintainerName" placeholder="请输入保养人姓名" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="maintainForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="maintainFormVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitMaintain">确定</el-button>
        </template>
      </el-dialog>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getList, save, update, remove } from '../../api/equipment'
import { getByEquipmentId, save as saveMaintain, update as updateMaintain, remove as removeMaintain } from '../../api/equipmentMaintain'

const loading = ref(false)
const list = ref([])
const searchText = ref('')
const searchStatus = ref(null)

const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null, equipmentCode: '', equipmentName: '', equipmentType: '', status: 1, 
  location: '', purchaseDate: '', lastMaintainDate: '', nextMaintainDate: '', runningHours: 0
})
const rules = {
  equipmentCode: [{ required: true, message: '请输入设备编码', trigger: 'blur' }],
  equipmentName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }]
}

const maintainDialogVisible = ref(false)
const maintainLoading = ref(false)
const maintainList = ref([])
const currentEquipmentId = ref(null)

const maintainFormVisible = ref(false)
const maintainFormRef = ref(null)
const maintainForm = reactive({
  id: null, equipmentId: null, maintainType: '', maintainDate: '', 
  maintainContent: '', maintainResult: '', maintainerName: '', remark: ''
})
const maintainRules = {
  maintainType: [{ required: true, message: '请选择保养类型', trigger: 'change' }],
  maintainDate: [{ required: true, message: '请选择保养日期', trigger: 'change' }],
  maintainContent: [{ required: true, message: '请输入保养内容', trigger: 'blur' }],
  maintainResult: [{ required: true, message: '请选择保养结果', trigger: 'change' }]
}

const statusText = (s) => ({ 0: '停用', 1: '运行', 2: '维修' }[s] || '未知')
const statusType = (s) => ({ 0: 'info', 1: 'success', 2: 'warning' }[s] || 'info')
const resultType = (r) => ({ '正常': 'success', '异常': 'danger', '待处理': 'warning' }[r] || 'info')

const isNeedMaintain = (date) => {
  if (!date) return false
  const next = new Date(date)
  const now = new Date()
  return next <= now
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getList()
    let data = res.data || []
    if (searchText.value) {
      data = data.filter(item => 
        item.equipmentCode?.includes(searchText.value) || 
        item.equipmentName?.includes(searchText.value)
      )
    }
    if (searchStatus.value !== null) {
      data = data.filter(item => item.status === searchStatus.value)
    }
    list.value = data
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, { id: null, equipmentCode: '', equipmentName: '', equipmentType: '', status: 1, 
    location: '', purchaseDate: '', lastMaintainDate: '', nextMaintainDate: '', runningHours: 0 })
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  if (form.id) {
    await update(form)
  } else {
    await save(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchList()
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该设备吗？', '提示', { type: 'warning' }).then(async () => {
    await remove(row.id)
    ElMessage.success('删除成功')
    fetchList()
  }).catch(() => {})
}

const handleViewMaintain = async (row) => {
  currentEquipmentId.value = row.id
  maintainLoading.value = true
  try {
    const res = await getByEquipmentId(row.id)
    maintainList.value = res.data || []
    maintainDialogVisible.value = true
  } finally {
    maintainLoading.value = false
  }
}

const resetMaintainForm = () => {
  Object.assign(maintainForm, { id: null, equipmentId: currentEquipmentId.value, maintainType: '', 
    maintainDate: '', maintainContent: '', maintainResult: '', maintainerName: '', remark: '' })
}

const handleAddMaintain = () => {
  resetMaintainForm()
  maintainForm.equipmentId = currentEquipmentId.value
  maintainForm.maintainDate = new Date().toISOString().slice(0, 19).replace('T', ' ')
  maintainFormVisible.value = true
}

const handleEditMaintain = (row) => {
  resetMaintainForm()
  Object.assign(maintainForm, row)
  maintainFormVisible.value = true
}

const handleSubmitMaintain = async () => {
  await maintainFormRef.value.validate()
  if (maintainForm.id) {
    await updateMaintain(maintainForm)
  } else {
    await saveMaintain(maintainForm)
  }
  ElMessage.success('保存成功')
  maintainFormVisible.value = false
  handleViewMaintain({ id: currentEquipmentId.value })
}

const handleDeleteMaintain = (row) => {
  ElMessageBox.confirm('确认删除该保养记录吗？', '提示', { type: 'warning' }).then(async () => {
    await removeMaintain(row.id)
    ElMessage.success('删除成功')
    handleViewMaintain({ id: currentEquipmentId.value })
  }).catch(() => {})
}

onMounted(() => fetchList())
</script>

<style scoped>
.page { padding: 0; }
.search-bar { margin-bottom: 16px; display: flex; gap: 8px; align-items: center; }
.text-warning { color: #E6A23C; }
</style>