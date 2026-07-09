<template>
  <div class="page">
    <div class="search-bar">
      <el-input v-model="query.name" placeholder="物料名称" clearable style="width: 160px" @keyup.enter="handleSearch" />
      <el-input v-model="query.code" placeholder="物料编码" clearable style="width: 160px" @keyup.enter="handleSearch" />
      <el-input v-model="query.type" placeholder="物料类型" clearable style="width: 140px" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="success" @click="handleAdd">新增</el-button>
      <el-button type="warning" @click="scanVisible = true">扫码查物料</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="code" label="物料编码" width="140" />
      <el-table-column prop="name" label="物料名称" min-width="160" />
      <el-table-column prop="type" label="物料类型" width="120" />
      <el-table-column prop="unit" label="计量单位" width="100" />
      <el-table-column prop="price" label="参考单价" width="110" />
      <el-table-column prop="safetyStock" label="安全库存" width="100" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑物料' : '新增物料'" width="560px">
      <el-form :model="form" label-width="90px" ref="formRef" :rules="rules">
        <el-form-item label="物料编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入物料编码" />
        </el-form-item>
        <el-form-item label="物料名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入物料名称" />
        </el-form-item>
        <el-form-item label="物料类型" prop="type">
          <el-input v-model="form.type" placeholder="请输入物料类型" />
        </el-form-item>
        <el-form-item label="计量单位" prop="unit">
          <el-input v-model="form.unit" placeholder="请输入计量单位" />
        </el-form-item>
        <el-form-item label="参考单价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="安全库存" prop="safetyStock">
          <el-input-number v-model="form.safetyStock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="scanVisible" title="扫码查物料" width="480px">
      <el-form label-width="80px">
        <el-form-item label="物料编码">
          <el-input v-model="scanCode" placeholder="请输入物料编码" @keyup.enter="handleScan" />
        </el-form-item>
      </el-form>
      <div v-if="scanResult" class="scan-result">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="物料编码">{{ scanResult.code }}</el-descriptions-item>
          <el-descriptions-item label="物料名称">{{ scanResult.name }}</el-descriptions-item>
          <el-descriptions-item label="物料类型">{{ scanResult.type }}</el-descriptions-item>
          <el-descriptions-item label="计量单位">{{ scanResult.unit }}</el-descriptions-item>
          <el-descriptions-item label="参考单价">{{ scanResult.price }}</el-descriptions-item>
          <el-descriptions-item label="安全库存">{{ scanResult.safetyStock }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="scanResult.status === 1 ? 'success' : 'info'">{{ scanResult.status === 1 ? '启用' : '停用' }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="scanVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleScan">查询</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getList, getByCode, save, update, remove } from '../../api/material'

const loading = ref(false)
const list = ref([])
const query = reactive({ name: '', code: '', type: '' })

const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null, code: '', name: '', type: '', unit: '', price: 0, safetyStock: 0, status: 1
})
const rules = {
  code: [{ required: true, message: '请输入物料编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入物料名称', trigger: 'blur' }]
}

const scanVisible = ref(false)
const scanCode = ref('')
const scanResult = ref(null)

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getList(query)
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => fetchList()
const handleReset = () => {
  query.name = ''
  query.code = ''
  query.type = ''
  fetchList()
}

const resetForm = () => {
  Object.assign(form, { id: null, code: '', name: '', type: '', unit: '', price: 0, safetyStock: 0, status: 1 })
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
  ElMessageBox.confirm('确认删除该物料吗？', '提示', { type: 'warning' }).then(async () => {
    await remove(row.id)
    ElMessage.success('删除成功')
    fetchList()
  }).catch(() => {})
}

const handleScan = async () => {
  if (!scanCode.value) {
    ElMessage.warning('请输入物料编码')
    return
  }
  const res = await getByCode(scanCode.value)
  scanResult.value = res.data
}

onMounted(() => fetchList())
</script>

<style scoped>
.page { padding: 0; }
.search-bar { margin-bottom: 16px; display: flex; gap: 8px; flex-wrap: wrap; }
.scan-result { margin-top: 16px; }
</style>
