<template>
  <div class="page-container">
    <el-card shadow="never">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="操作人">
          <el-input v-model="searchForm.userName" placeholder="请输入操作人" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 150px">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作模块">
          <el-select v-model="searchForm.module" placeholder="请选择" clearable style="width: 150px">
            <el-option v-for="item in moduleList" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="logList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userName" label="操作人" width="120" />
        <el-table-column prop="operationModule" label="操作模块" width="120">
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.operationModule }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.operationType)" size="small">{{ row.operationType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationDesc" label="操作描述" min-width="250" />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="操作日志详情" width="600px">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="操作人">{{ currentLog.userName }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentLog.operationModule }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentLog.operationType }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ currentLog.operationDesc }}</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentLog.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { operationLog } from '../../api/system'

const loading = ref(false)
const searchForm = reactive({
  userName: '',
  type: '',
  module: '',
  dateRange: []
})

const typeOptions = [
  { label: '新增', value: '新增' },
  { label: '修改', value: '修改' },
  { label: '删除', value: '删除' },
  { label: '查询', value: '查询' },
  { label: '登录', value: '登录' },
  { label: '登出', value: '登出' },
  { label: '派发', value: '派发' },
  { label: '结案', value: '结案' },
  { label: '启动', value: '启动' },
  { label: '完成', value: '完成' }
]

const moduleList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const logList = ref([])

const detailDialogVisible = ref(false)
const currentLog = ref(null)

function getTypeTagType(type) {
  const map = {
    '新增': 'success',
    '修改': 'warning',
    '删除': 'danger',
    '查询': 'info',
    '登录': 'primary',
    '登出': 'info',
    '派发': 'primary',
    '结案': 'success',
    '启动': 'primary',
    '完成': 'success'
  }
  return map[type] || ''
}

function fetchData() {
  loading.value = true
  const params = {
    page: page.value,
    size: pageSize.value
  }
  if (searchForm.userName) {
    params.userName = searchForm.userName
  }
  if (searchForm.type) {
    params.type = searchForm.type
  }
  if (searchForm.module) {
    params.module = searchForm.module
  }
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    params.startTime = searchForm.dateRange[0] + ' 00:00:00'
    params.endTime = searchForm.dateRange[1] + ' 23:59:59'
  }

  operationLog.getList(params).then(res => {
    if (res.code === 200) {
      logList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  }).finally(() => {
    loading.value = false
  })
}

function loadModules() {
  operationLog.getModules().then(res => {
    if (res.code === 200) {
      moduleList.value = res.data || []
    }
  })
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function handleReset() {
  searchForm.userName = ''
  searchForm.type = ''
  searchForm.module = ''
  searchForm.dateRange = []
  page.value = 1
  fetchData()
}

function handleSizeChange(size) {
  pageSize.value = size
  page.value = 1
  fetchData()
}

function handleCurrentChange(p) {
  page.value = p
  fetchData()
}

function handleViewDetail(row) {
  currentLog.value = row
  detailDialogVisible.value = true
}

onMounted(() => {
  fetchData()
  loadModules()
})
</script>

<style scoped>
.page-container {
  width: 100%;
}

.search-form {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
