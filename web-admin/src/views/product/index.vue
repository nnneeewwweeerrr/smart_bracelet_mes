<template>
  <div class="page">
    <div class="search-bar">
      <el-input v-model="query.name" placeholder="产品名称" clearable style="width: 200px" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="success" @click="handleAdd">新增</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="code" label="产品编码" width="140" />
      <el-table-column prop="name" label="产品名称" min-width="160" />
      <el-table-column prop="type" label="产品类型" width="120" />
      <el-table-column prop="spec" label="规格型号" width="140" />
      <el-table-column prop="price" label="参考单价" width="120" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑产品' : '新增产品'" width="560px">
      <el-form :model="form" label-width="90px" ref="formRef" :rules="rules">
        <el-form-item label="产品编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入产品编码" />
        </el-form-item>
        <el-form-item label="产品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="产品类型" prop="type">
          <el-input v-model="form.type" placeholder="请输入产品类型" />
        </el-form-item>
        <el-form-item label="规格型号" prop="spec">
          <el-input v-model="form.spec" placeholder="请输入规格型号" />
        </el-form-item>
        <el-form-item label="参考单价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getList, save, update, remove } from '../../api/product'

const loading = ref(false)
const list = ref([])
const query = reactive({ name: '' })

const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null, code: '', name: '', type: '', spec: '', price: 0, status: 1
})
const rules = {
  code: [{ required: true, message: '请输入产品编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getList(query.name)
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => fetchList()
const handleReset = () => {
  query.name = ''
  fetchList()
}

const resetForm = () => {
  Object.assign(form, { id: null, code: '', name: '', type: '', spec: '', price: 0, status: 1 })
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
  ElMessageBox.confirm('确认删除该产品吗？', '提示', { type: 'warning' }).then(async () => {
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
