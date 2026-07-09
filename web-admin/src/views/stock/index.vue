<template>
  <div class="page">
    <div class="search-bar">
      <el-select v-model="query.materialId" placeholder="请选择物料" clearable filterable style="width: 200px">
        <el-option v-for="m in materialList" :key="m.id" :label="m.name" :value="m.id" />
      </el-select>
      <el-select v-model="query.warehouseId" placeholder="请选择仓库" clearable filterable style="width: 180px">
        <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
      </el-select>
      <el-button type="primary" @click="fetchList">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column label="物料名称" min-width="160">
        <template #default="{ row }">{{ materialName(row.materialId) }}</template>
      </el-table-column>
      <el-table-column label="仓库名称" width="160">
        <template #default="{ row }">{{ warehouseName(row.warehouseId) }}</template>
      </el-table-column>
      <el-table-column prop="quantity" label="库存数量" width="120" />
      <el-table-column prop="location" label="库位" width="140" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getList as getStockList } from '../../api/stock'
import { getList as getMaterialList } from '../../api/material'
import { getList as getWarehouseList } from '../../api/warehouse'

const loading = ref(false)
const list = ref([])
const query = reactive({ materialId: '', warehouseId: '' })
const materialList = ref([])
const warehouseList = ref([])

const materialName = (id) => materialList.value.find(m => m.id === id)?.name || id
const warehouseName = (id) => warehouseList.value.find(w => w.id === id)?.name || id

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getStockList(query)
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  query.materialId = ''
  query.warehouseId = ''
  fetchList()
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
</style>
