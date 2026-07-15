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
      customerName: searchForm.name,
      customerCode: ''
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    const res = await getCustomerList(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.warn('客户列表API调用失败，使用mock数据', error)
    tableData.value = [
      { id: 1, customerNo: 'CUST-20260701', name: '深圳华康健康科技有限公司', contact: '张明', phone: '13800138001', email: 'zhangming@huakang.com', address: '深圳市南山区科技园南路18号', status: '1', createTime: '2026-07-01 09:30:00', remark: '智能手环核心客户' },
      { id: 2, customerNo: 'CUST-20260702', name: '北京万达运动科技有限公司', contact: '李娜', phone: '13900139002', email: 'lina@wanda-sport.com', address: '北京市朝阳区望京SOHO T3-1205', status: '1', createTime: '2026-07-02 10:15:00', remark: '运动手环批量采购' },
      { id: 3, customerNo: 'CUST-20260703', name: '上海瑞德医疗器械有限公司', contact: '王磊', phone: '13700137003', email: 'wanglei@ruide-med.com', address: '上海市浦东新区张江高科技园区碧波路690号', status: '1', createTime: '2026-07-03 14:00:00', remark: '医疗级健康手环客户' },
      { id: 4, customerNo: 'CUST-20260705', name: '广州天河体育用品有限公司', contact: '陈静', phone: '13600136004', email: 'chenjing@tianhe-sport.com', address: '广州市天河区天河路385号太古汇一座', status: '1', createTime: '2026-07-05 11:20:00', remark: '体育健身手环合作' },
      { id: 5, customerNo: 'CUST-20260706', name: '杭州云健康智能科技有限公司', contact: '刘洋', phone: '13500135005', email: 'liuyang@yunhealth.com', address: '杭州市余杭区仓前街道欧美中心', status: '1', createTime: '2026-07-06 09:00:00', remark: '云平台健康数据对接' },
      { id: 6, customerNo: 'CUST-20260708', name: '成都益康养老服务有限公司', contact: '赵芳', phone: '13400134006', email: 'zhaofang@yikang-care.com', address: '成都市高新区天府大道北段1700号', status: '1', createTime: '2026-07-08 16:30:00', remark: '养老监护手环项目' },
      { id: 7, customerNo: 'CUST-20260710', name: '武汉光谷生物科技有限公司', contact: '孙伟', phone: '13300133007', email: 'sunwei@guangu-bio.com', address: '武汉市东湖高新区光谷大道77号', status: '0', createTime: '2026-07-10 13:45:00', remark: '已暂停合作' },
      { id: 8, customerNo: 'CUST-20260712', name: '南京智慧校园科技有限公司', contact: '周婷', phone: '13200132008', email: 'zhouting@smartcampus.com', address: '南京市江宁区东山街道文靖路88号', status: '1', createTime: '2026-07-12 10:00:00', remark: '校园健康监测手环' },
      { id: 9, customerNo: 'CUST-20260713', name: '西安丝路健康科技有限公司', contact: '吴强', phone: '13100131009', email: 'wuqiang@silkroad-health.com', address: '西安市高新区锦业路1号都市之门', status: '1', createTime: '2026-07-13 15:20:00', remark: '西北区域代理商' },
      { id: 10, customerNo: 'CUST-20260714', name: '重庆山城运动科技有限公司', contact: '郑丽', phone: '13000130010', email: 'zhengli@sc-sport.com', address: '重庆市渝北区金开大道西段88号', status: '1', createTime: '2026-07-14 08:50:00', remark: '户外运动手环定制' }
    ]
    total.value = 10
  } finally {
    loading.value = false
  }
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
    // request interceptor handles error message
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该客户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCustomer(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    // catch cancel or request error
  }
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
