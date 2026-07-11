<template>
  <div class="page">
    <div class="search-bar">
      <el-input v-model="searchForm.userName" placeholder="操作人" style="width: 160px" clearable />
      <el-select v-model="searchForm.module" placeholder="操作模块" style="width: 160px" clearable>
        <el-option v-for="m in modules" :key="m" :label="m" :value="m" />
      </el-select>
      <el-select v-model="searchForm.type" placeholder="操作类型" style="width: 120px" clearable>
        <el-option label="新增" value="新增" />
        <el-option label="修改" value="修改" />
        <el-option label="删除" value="删除" />
        <el-option label="查询" value="查询" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至" 
        start-placeholder="开始日期" end-placeholder="结束日期" 
        value-format="YYYY-MM-DD" style="width: 300px" />
      <el-button type="primary" @click="fetchList">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="userName" label="操作人" width="120" />
      <el-table-column prop="operationModule" label="操作模块" width="140" />
      <el-table-column prop="operationType" label="操作类型" width="100">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.operationType)">{{ row.operationType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operationDesc" label="操作描述" min-width="300" />
      <el-table-column prop="ipAddress" label="IP地址" width="150" />
      <el-table-column prop="createTime" label="操作时间" width="180" />
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getList, getModules } from '../../api/operationLog'

const loading = ref(false)
const tableData = ref([])
const modules = ref([])
const dateRange = ref([])

const searchForm = reactive({
  userName: '',
  module: '',
  type: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const typeTag = (t) => ({
  '新增': 'success',
  '修改': 'warning',
  '删除': 'danger',
  '查询': 'info'
}[t] || 'info')

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      userName: searchForm.userName || undefined,
      module: searchForm.module || undefined,
      type: searchForm.type || undefined
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0] + 'T00:00:00'
      params.endTime = dateRange.value[1] + 'T23:59:59'
    }
    const res = await getList(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.userName = ''
  searchForm.module = ''
  searchForm.type = ''
  dateRange.value = []
  pagination.page = 1
  fetchList()
}

const fetchModules = async () => {
  try {
    const res = await getModules()
    modules.value = res.data || []
  } catch (e) {
    console.error('获取模块列表失败', e)
  }
}

onMounted(() => {
  fetchModules()
  fetchList()
})
</script>

<style scoped>
.page { padding: 0; }
.search-bar { margin-bottom: 16px; display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>