<template>
  <div class="page">
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="flowNo" label="单号" width="180" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.flowType === '入库' ? 'success' : 'danger'">{{ row.flowType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="物料" min-width="160">
        <template #default="{ row }">{{ materialName(row.materialId) }}</template>
      </el-table-column>
      <el-table-column label="仓库" width="140">
        <template #default="{ row }">{{ warehouseName(row.warehouseId) }}</template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="100" />
      <el-table-column prop="createTime" label="时间" width="180" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getList as getStockFlowList } from '../../api/stockFlow'
import { getList as getMaterialList } from '../../api/material'
import { getList as getWarehouseList } from '../../api/warehouse'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10 })
const materialList = ref([])
const warehouseList = ref([])

const materialName = (id) => materialList.value.find(m => m.id === id)?.name || id
const warehouseName = (id) => warehouseList.value.find(w => w.id === id)?.name || id

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getStockFlowList(query)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
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
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
