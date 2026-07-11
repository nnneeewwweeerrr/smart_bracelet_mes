<template>
  <div class="page-container">
    <el-card shadow="never">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="操作人">
          <el-input v-model="searchForm.username" placeholder="请输入操作人" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 150px">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作模块">
          <el-select v-model="searchForm.module" placeholder="请选择" clearable style="width: 150px">
            <el-option v-for="item in moduleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
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
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="module" label="操作模块" width="120">
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" min-width="250" />
        <el-table-column prop="method" label="请求方法" width="100">
          <template #default="{ row }">
            <el-tag :type="getMethodTagType(row.method)" size="small">{{ row.method }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '成功' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="100" align="right" />
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
      />
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="操作日志详情" width="600px">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="操作人">{{ currentLog.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentLog.realName }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentLog.type }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ currentLog.description }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentLog.method }}</el-descriptions-item>
        <el-descriptions-item label="请求路径">{{ currentLog.url }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="currentLog.status === '成功' ? 'success' : 'danger'" size="small">
            {{ currentLog.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentLog.costTime }} ms</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentLog.createTime }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="param-pre">{{ currentLog.params || '无' }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" :span="2">
          <pre class="param-pre">{{ currentLog.result || '无' }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const searchForm = ref({
  username: '',
  type: '',
  module: '',
  dateRange: []
})

const typeOptions = [
  { label: '新增', value: '新增' },
  { label: '修改', value: '修改' },
  { label: '删除', value: '删除' },
  { label: '查询', value: '查询' },
  { label: '导出', value: '导出' },
  { label: '登录', value: '登录' },
  { label: '登出', value: '登出' }
]

const moduleOptions = [
  { label: '用户管理', value: '用户管理' },
  { label: '角色管理', value: '角色管理' },
  { label: '菜单管理', value: '菜单管理' },
  { label: '订单管理', value: '订单管理' },
  { label: '采购管理', value: '采购管理' },
  { label: '仓储管理', value: '仓储管理' },
  { label: '生产管理', value: '生产管理' },
  { label: '质检管理', value: '质检管理' }
]

const page = ref(1)
const pageSize = ref(10)
const total = ref(28)

const logList = ref([
  { id: 1, username: 'admin', realName: '超级管理员', module: '用户管理', type: '新增', description: '新增用户：test001', method: 'POST', url: '/api/user/add', ip: '192.168.1.100', status: '成功', costTime: 120, createTime: '2024-01-16 10:30:00', params: '{"username":"test001","realName":"测试员"}', result: '{"code":200,"msg":"成功"}' },
  { id: 2, username: 'admin', realName: '超级管理员', module: '角色管理', type: '修改', description: '修改角色：订单员', method: 'PUT', url: '/api/role/update', ip: '192.168.1.100', status: '成功', costTime: 85, createTime: '2024-01-16 10:25:00', params: '{"id":2,"roleName":"订单员"}', result: '{"code":200,"msg":"成功"}' },
  { id: 3, username: 'order_clerk', realName: '张三', module: '订单管理', type: '新增', description: '新增订单：ORD-2024001', method: 'POST', url: '/api/order/add', ip: '192.168.1.101', status: '成功', costTime: 156, createTime: '2024-01-16 09:45:00', params: '{"orderNo":"ORD-2024001"}', result: '{"code":200,"msg":"成功"}' },
  { id: 4, username: 'purchase_clerk', realName: '李四', module: '采购管理', type: '修改', description: '修改采购单：PUR-2024002', method: 'PUT', url: '/api/purchase/update', ip: '192.168.1.102', status: '成功', costTime: 98, createTime: '2024-01-16 09:30:00', params: '{"id":2,"status":"已审批"}', result: '{"code":200,"msg":"成功"}' },
  { id: 5, username: 'warehouse_keeper', realName: '王五', module: '仓储管理', type: '新增', description: '新增入库单：IN-2024003', method: 'POST', url: '/api/stock/in/add', ip: '192.168.1.103', status: '成功', costTime: 210, createTime: '2024-01-16 09:15:00', params: '{"orderNo":"IN-2024003"}', result: '{"code":200,"msg":"成功"}' },
  { id: 6, username: 'production_manager', realName: '赵六', module: '生产管理', type: '修改', description: '更新工单状态：WO-2024001', method: 'PUT', url: '/api/work-order/update-status', ip: '192.168.1.104', status: '成功', costTime: 76, createTime: '2024-01-16 09:00:00', params: '{"id":1,"status":"生产中"}', result: '{"code":200,"msg":"成功"}' },
  { id: 7, username: 'quality_inspector', realName: '钱七', module: '质检管理', type: '新增', description: '新增质检记录：Q-2024015', method: 'POST', url: '/api/quality/add', ip: '192.168.1.105', status: '成功', costTime: 145, createTime: '2024-01-16 08:45:00', params: '{"qualityNo":"Q-2024015"}', result: '{"code":200,"msg":"成功"}' },
  { id: 8, username: 'admin', realName: '超级管理员', module: '用户管理', type: '删除', description: '删除用户：test002', method: 'DELETE', url: '/api/user/delete/9', ip: '192.168.1.100', status: '成功', costTime: 65, createTime: '2024-01-15 18:30:00', params: '', result: '{"code":200,"msg":"成功"}' },
  { id: 9, username: 'admin', realName: '超级管理员', module: '菜单管理', type: '查询', description: '查询菜单列表', method: 'GET', url: '/api/menu/list', ip: '192.168.1.100', status: '成功', costTime: 45, createTime: '2024-01-15 18:00:00', params: '{}', result: '{"code":200,"data":[...]}' },
  { id: 10, username: 'order_clerk', realName: '张三', module: '订单管理', type: '查询', description: '查询订单列表', method: 'GET', url: '/api/order/list', ip: '192.168.1.101', status: '成功', costTime: 52, createTime: '2024-01-15 17:30:00', params: '{"page":1,"size":10}', result: '{"code":200,"data":[...]}' }
])

const detailDialogVisible = ref(false)
const currentLog = ref(null)

function getTypeTagType(type) {
  const map = {
    '新增': 'success',
    '修改': 'warning',
    '删除': 'danger',
    '查询': 'info',
    '导出': '',
    '登录': 'primary',
    '登出': 'info'
  }
  return map[type] || ''
}

function getMethodTagType(method) {
  const map = {
    'GET': 'primary',
    'POST': 'success',
    'PUT': 'warning',
    'DELETE': 'danger'
  }
  return map[method] || ''
}

function handleSearch() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

function handleReset() {
  searchForm.value = {
    username: '',
    type: '',
    module: '',
    dateRange: []
  }
  page.value = 1
}

function handleViewDetail(row) {
  currentLog.value = row
  detailDialogVisible.value = true
}

onMounted(() => {
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

.param-pre {
  margin: 0;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
}
</style>
