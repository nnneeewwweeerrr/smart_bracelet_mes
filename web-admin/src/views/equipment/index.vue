<template>
  <div class="page">
    <div class="search-bar">
      <el-button type="success" @click="handleAdd">新增</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="code" label="设备编码" width="140" />
      <el-table-column prop="name" label="设备名称" min-width="160" />
      <el-table-column prop="type" label="设备类型" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="location" label="位置" width="160" />
      <el-table-column prop="purchaseDate" label="购入日期" width="120" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑设备' : '新增设备'" width="560px">
      <el-form :model="form" label-width="90px" ref="formRef" :rules="rules">
        <el-form-item label="设备编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入设备编码" />
        </el-form-item>
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="type">
          <el-input v-model="form.type" placeholder="请输入设备类型" />
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
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getList, save, update, remove } from '../../api/equipment'

const loading = ref(false)
const list = ref([])

const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null, code: '', name: '', type: '', status: 0, location: '', purchaseDate: ''
})
const rules = {
  code: [{ required: true, message: '请输入设备编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }]
}

const statusText = (s) => ({ 0: '停用', 1: '运行', 2: '维修' }[s] || '未知')
const statusType = (s) => ({ 0: 'info', 1: 'success', 2: 'warning' }[s] || 'info')

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getList()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, { id: null, code: '', name: '', type: '', status: 0, location: '', purchaseDate: '' })
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

onMounted(() => fetchList())
</script>

<style scoped>
.page { padding: 0; }
.search-bar { margin-bottom: 16px; display: flex; gap: 8px; }
</style>
