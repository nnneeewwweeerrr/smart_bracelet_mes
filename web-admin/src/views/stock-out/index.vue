<template>
  <div class="page">
    <div class="search-bar">
      <el-button type="success" @click="handleAdd">新增出库</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="stockOutNo" label="出库单号" width="160" />
      <el-table-column label="物料名称" min-width="160">
        <template #default="{ row }">{{ materialName(row.materialId) }}</template>
      </el-table-column>
      <el-table-column label="仓库" width="140">
        <template #default="{ row }">{{ warehouseName(row.warehouseId) }}</template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="100" />
      <el-table-column prop="targetType" label="出库类型" width="120" />
      <el-table-column prop="targetNo" label="目标单号" width="160" />
      <el-table-column prop="operator" label="操作人" width="120" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pagination"
      v-model:current-page="query.page"
      v-model:page-size="query.size"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="fetchList"
      @current-change="fetchList"
    />

    <el-dialog v-model="dialogVisible" title="新增出库" width="520px">
      <el-form :model="form" label-width="90px" ref="formRef" :rules="rules">
        <el-form-item label="物料" prop="materialId">
          <el-select v-model="form.materialId" placeholder="请选择物料" filterable style="width: 100%">
            <el-option v-for="m in materialList" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="form.warehouseId" placeholder="请选择仓库" filterable style="width: 100%">
            <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="出库类型" prop="targetType">
          <el-input v-model="form.targetType" placeholder="如：销售出库、生产领料" />
        </el-form-item>
        <el-form-item label="目标单号" prop="targetNo">
          <el-input v-model="form.targetNo" placeholder="请输入目标单号" />
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
import { getList, save, remove } from '../../api/stockOut'
import { getList as getMaterialList } from '../../api/material'
import { getList as getWarehouseList } from '../../api/warehouse'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10 })
const materialList = ref([])
const warehouseList = ref([])

const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({ materialId: '', warehouseId: '', quantity: 1, targetType: '', targetNo: '' })
const rules = {
  materialId: [{ required: true, message: '请选择物料', trigger: 'change' }],
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

const materialName = (id) => materialList.value.find(m => m.id === id)?.name || id
const warehouseName = (id) => warehouseList.value.find(w => w.id === id)?.name || id

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getList(query)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, { materialId: '', warehouseId: '', quantity: 1, targetType: '', targetNo: '' })
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  await save(form)
  ElMessage.success('出库成功')
  dialogVisible.value = false
  fetchList()
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该出库记录吗？', '提示', { type: 'warning' }).then(async () => {
    await remove(row.id)
    ElMessage.success('删除成功')
    fetchList()
  }).catch(() => {})
}

onMounted(async () => {
  const [m, w] = await Promise.all([getMaterialList({}), getWarehouseList()])
  materialList.value = m.data || []
  warehouseList.value = w.data || []
  fetchList()
})
</script>

<style scoped>
.page { padding: 0; }
.search-bar { margin-bottom: 16px; display: flex; gap: 8px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
