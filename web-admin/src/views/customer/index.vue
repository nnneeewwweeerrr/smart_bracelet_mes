<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="searchForm.contact" placeholder="请输入联系人" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="客户状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 160px">
            <el-option label="正常" :value="1" />
            <el-option label="停用" :value="0" />
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
        <el-table-column prop="customerName" label="客户名称" min-width="160" />
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="level" label="等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.level === 'A' ? 'danger' : row.level === 'B' ? 'warning' : 'info'">
              {{ row.level }}级
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">
            {{ row.createTime ? row.createTime.replace('T', ' ').substring(0, 19) : '-' }}
          </template>
        </el-table-column>
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
        <el-form-item label="客户编号" prop="customerNo">
          <el-input v-model="customerForm.customerNo" placeholder="请输入客户编号" />
        </el-form-item>
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="customerForm.customerName" placeholder="请输入客户名称" />
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
        <el-form-item label="省份" prop="province">
          <el-input v-model="customerForm.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="customerForm.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="customerForm.address" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="所属行业" prop="industry">
          <el-select v-model="customerForm.industry" placeholder="请选择行业" style="width: 100%">
            <el-option label="通信" value="通信" />
            <el-option label="电子" value="电子" />
            <el-option label="电商" value="电商" />
            <el-option label="智能硬件" value="智能硬件" />
            <el-option label="互联网" value="互联网" />
            <el-option label="物流" value="物流" />
            <el-option label="本地生活" value="本地生活" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户等级" prop="level">
          <el-select v-model="customerForm.level" placeholder="请选择等级" style="width: 100%">
            <el-option label="A级" value="A" />
            <el-option label="B级" value="B" />
            <el-option label="C级" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户状态" prop="status">
          <el-radio-group v-model="customerForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="customerForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
        <el-descriptions-item label="客户名称">{{ currentCustomer.customerName }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentCustomer.contact }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentCustomer.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentCustomer.email }}</el-descriptions-item>
        <el-descriptions-item label="省份">{{ currentCustomer.province }}</el-descriptions-item>
        <el-descriptions-item label="城市">{{ currentCustomer.city }}</el-descriptions-item>
        <el-descriptions-item label="行业">{{ currentCustomer.industry }}</el-descriptions-item>
        <el-descriptions-item label="等级">
          <el-tag :type="currentCustomer.level === 'A' ? 'danger' : currentCustomer.level === 'B' ? 'warning' : 'info'">
            {{ currentCustomer.level }}级
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentCustomer.status === 1 ? 'success' : 'danger'">
            {{ currentCustomer.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentCustomer.address }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ currentCustomer.createTime ? currentCustomer.createTime.replace('T', ' ').substring(0, 19) : '-' }}
        </el-descriptions-item>
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
import { getCustomerList, createCustomer, updateCustomer, deleteCustomer } from '../../api/customer'

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
  customerName: '',
  contact: '',
  status: ''
})

const customerForm = reactive({
  id: null,
  customerNo: '',
  customerName: '',
  contact: '',
  phone: '',
  email: '',
  province: '',
  city: '',
  industry: '',
  level: 'B',
  address: '',
  status: 1,
  remark: ''
})

const currentCustomer = reactive({})

const customerRules = {
  customerNo: [
    { required: true, message: '请输入客户编号', trigger: 'blur' }
  ],
  customerName: [
    { required: true, message: '请输入客户名称', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' }
  ]
}

const tableData = ref([])

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      customerName: searchForm.customerName || undefined,
      contact: searchForm.contact || undefined,
      status: searchForm.status || undefined
    }
    const res = await getCustomerList(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取客户列表失败:', error)
    ElMessage.error('获取客户列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.customerName = ''
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
    customerName: '',
    contact: '',
    phone: '',
    email: '',
    province: '',
    city: '',
    industry: '',
    level: 'B',
    address: '',
    status: 1,
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
    if (isEdit.value) {
      await updateCustomer(customerForm)
      ElMessage.success('编辑成功')
    } else {
      await createCustomer(customerForm)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除客户「${row.customerName}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCustomer(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}
</script>

<style scoped>
.page-container { width: 100%; }
.search-card { margin-bottom: 20px; }
.table-card { margin-bottom: 20px; }
.table-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.table-title { font-size: 16px; font-weight: bold; color: #303133; }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>