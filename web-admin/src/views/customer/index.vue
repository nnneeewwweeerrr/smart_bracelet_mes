<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.name" placeholder="请输入客户名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="searchForm.contact" placeholder="请输入联系人" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="客户状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 160px">
            <el-option label="正常" value="1" />
            <el-option label="停用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <span class="table-title">客户列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增客户
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="customerNo" label="客户编号" width="140" />
        <el-table-column prop="name" label="客户名称" min-width="160" />
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'danger'">
              {{ row.status === '1' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑客户' : '新增客户'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="customerFormRef"
        :model="customerForm"
        :rules="customerRules"
        label-width="100px"
      >
        <el-form-item label="客户名称" prop="name">
          <el-input v-model="customerForm.name" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="customerForm.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="customerForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="customerForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="customerForm.address" type="textarea" :rows="3" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="客户状态" prop="status">
          <el-radio-group v-model="customerForm.status">
            <el-radio value="1">正常</el-radio>
            <el-radio value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="customerForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="客户详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="客户编号">{{ currentCustomer.customerNo }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ currentCustomer.name }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentCustomer.contact }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentCustomer.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱" :span="2">{{ currentCustomer.email }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentCustomer.address }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentCustomer.status === '1' ? 'success' : 'danger'">
            {{ currentCustomer.status === '1' ? '正常' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentCustomer.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentCustomer.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const customerFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  name: '',
  contact: '',
  status: ''
})

const customerForm = reactive({
  id: null,
  customerNo: '',
  name: '',
  contact: '',
  phone: '',
  email: '',
  address: '',
  status: '1',
  remark: ''
})

const currentCustomer = reactive({})

const customerRules = {
  name: [
    { required: true, message: '请输入客户名称', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' }
  ]
}

const mockData = ref([
  { id: 1, customerNo: 'C001', name: '深圳市科技有限公司', contact: '张三', phone: '13800138001', email: 'zhangsan@tech.com', address: '深圳市南山区科技园', status: '1', createTime: '2024-01-15 10:30:00', remark: '长期合作客户' },
  { id: 2, customerNo: 'C002', name: '北京智能设备公司', contact: '李四', phone: '13800138002', email: 'lisi@smart.com', address: '北京市朝阳区望京SOHO', status: '1', createTime: '2024-01-16 14:20:00', remark: '' },
  { id: 3, customerNo: 'C003', name: '上海电子科技有限公司', contact: '王五', phone: '13800138003', email: 'wangwu@shanghai.com', address: '上海市浦东新区张江高科', status: '1', createTime: '2024-01-17 09:15:00', remark: '大客户' },
  { id: 4, customerNo: 'C004', name: '广州数码产品公司', contact: '赵六', phone: '13800138004', email: 'zhaoliu@guangzhou.com', address: '广州市天河区珠江新城', status: '0', createTime: '2024-01-18 16:45:00', remark: '暂停合作' },
  { id: 5, customerNo: 'C005', name: '杭州互联网公司', contact: '钱七', phone: '13800138005', email: 'qianqi@hangzhou.com', address: '杭州市西湖区文三路', status: '1', createTime: '2024-01-19 11:00:00', remark: '' },
  { id: 6, customerNo: 'C006', name: '成都信息技术公司', contact: '孙八', phone: '13800138006', email: 'sunba@chengdu.com', address: '成都市高新区天府大道', status: '1', createTime: '2024-01-20 13:30:00', remark: '新客户' },
  { id: 7, customerNo: 'C007', name: '武汉电子有限公司', contact: '周九', phone: '13800138007', email: 'zhoujiu@wuhan.com', address: '武汉市洪山区光谷广场', status: '1', createTime: '2024-01-21 08:50:00', remark: '' },
  { id: 8, customerNo: 'C008', name: '南京智能科技公司', contact: '吴十', phone: '13800138008', email: 'wushi@nanjing.com', address: '南京市鼓楼区新街口', status: '0', createTime: '2024-01-22 15:20:00', remark: '已终止合作' }
])

const tableData = ref([])

onMounted(() => {
  fetchData()
})

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filtered = [...mockData.value]
    if (searchForm.name) {
      filtered = filtered.filter(item => item.name.includes(searchForm.name))
    }
    if (searchForm.contact) {
      filtered = filtered.filter(item => item.contact.includes(searchForm.contact))
    }
    if (searchForm.status) {
      filtered = filtered.filter(item => item.status === searchForm.status)
    }
    total.value = filtered.length
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    tableData.value = filtered.slice(start, end)
    loading.value = false
  }, 300)
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.name = ''
  searchForm.contact = ''
  searchForm.status = ''
  currentPage.value = 1
  fetchData()
}

function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  fetchData()
}

function handleCurrentChange(page) {
  currentPage.value = page
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  Object.assign(customerForm, {
    id: null,
    customerNo: '',
    name: '',
    contact: '',
    phone: '',
    email: '',
    address: '',
    status: '1',
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(customerForm, row)
  dialogVisible.value = true
}

function handleView(row) {
  Object.assign(currentCustomer, row)
  detailVisible.value = true
}

async function handleSubmit() {
  if (!customerFormRef.value) return
  try {
    await customerFormRef.value.validate()
    submitLoading.value = true
    setTimeout(() => {
      if (isEdit.value) {
        const index = mockData.value.findIndex(item => item.id === customerForm.id)
        if (index !== -1) {
          mockData.value[index] = { ...mockData.value[index], ...customerForm }
        }
        ElMessage.success('编辑成功')
      } else {
        const maxId = Math.max(...mockData.value.map(item => item.id), 0)
        const newCustomer = {
          ...customerForm,
          id: maxId + 1,
          customerNo: 'C' + String(maxId + 1).padStart(3, '0'),
          createTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
        }
        mockData.value.unshift(newCustomer)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      submitLoading.value = false
      fetchData()
    }, 300)
  } catch (error) {
    submitLoading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该客户吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = mockData.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      mockData.value.splice(index, 1)
    }
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}
</script>

<style scoped>
.page-container {
  width: 100%;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.table-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
