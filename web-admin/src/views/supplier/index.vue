<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="供应商名称">
          <el-input v-model="searchForm.name" placeholder="请输入供应商名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="searchForm.contact" placeholder="请输入联系人" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="供应商状态">
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
        <span class="table-title">供应商列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增供应商
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="supplierNo" label="供应商编号" width="140" />
        <el-table-column prop="name" label="供应商名称" min-width="180" />
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="level" label="等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.level === 'A'" type="danger">A级</el-tag>
            <el-tag v-else-if="row.level === 'B'" type="warning">B级</el-tag>
            <el-tag v-else type="info">C级</el-tag>
          </template>
        </el-table-column>
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
      :title="isEdit ? '编辑供应商' : '新增供应商'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="supplierFormRef"
        :model="supplierForm"
        :rules="supplierRules"
        label-width="100px"
      >
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="supplierForm.name" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="supplierForm.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="supplierForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="supplierForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="supplierForm.address" type="textarea" :rows="3" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="供应商等级" prop="level">
          <el-select v-model="supplierForm.level" placeholder="请选择等级" style="width: 100%">
            <el-option label="A级（优质）" value="A" />
            <el-option label="B级（普通）" value="B" />
            <el-option label="C级（一般）" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商状态" prop="status">
          <el-radio-group v-model="supplierForm.status">
            <el-radio value="1">正常</el-radio>
            <el-radio value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="supplierForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="供应商详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="供应商编号">{{ currentSupplier.supplierNo }}</el-descriptions-item>
        <el-descriptions-item label="供应商名称">{{ currentSupplier.name }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentSupplier.contact }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentSupplier.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱" :span="2">{{ currentSupplier.email }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentSupplier.address }}</el-descriptions-item>
        <el-descriptions-item label="供应商等级">
          <el-tag v-if="currentSupplier.level === 'A'" type="danger">A级</el-tag>
          <el-tag v-else-if="currentSupplier.level === 'B'" type="warning">B级</el-tag>
          <el-tag v-else type="info">C级</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentSupplier.status === '1' ? 'success' : 'danger'">
            {{ currentSupplier.status === '1' ? '正常' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentSupplier.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentSupplier.remark || '无' }}</el-descriptions-item>
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
const supplierFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  name: '',
  contact: '',
  status: ''
})

const supplierForm = reactive({
  id: null,
  supplierNo: '',
  name: '',
  contact: '',
  phone: '',
  email: '',
  address: '',
  level: 'B',
  status: '1',
  remark: ''
})

const currentSupplier = reactive({})

const supplierRules = {
  name: [
    { required: true, message: '请输入供应商名称', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' }
  ]
}

const mockData = ref([
  { id: 1, supplierNo: 'S001', name: '深圳市电子元件有限公司', contact: '陈经理', phone: '13900139001', email: 'chen@elec.com', address: '深圳市宝安区新安街道', level: 'A', status: '1', createTime: '2024-01-10 09:00:00', remark: '核心供应商' },
  { id: 2, supplierNo: 'S002', name: '东莞市塑胶制品厂', contact: '刘厂长', phone: '13900139002', email: 'liu@sujiao.com', address: '东莞市长安镇', level: 'B', status: '1', createTime: '2024-01-11 10:30:00', remark: '' },
  { id: 3, supplierNo: 'S003', name: '广州市包装材料公司', contact: '王总', phone: '13900139003', email: 'wang@baozhuang.com', address: '广州市白云区', level: 'B', status: '1', createTime: '2024-01-12 14:20:00', remark: '' },
  { id: 4, supplierNo: 'S004', name: '苏州市精密机械公司', contact: '张工', phone: '13900139004', email: 'zhang@jimi.com', address: '苏州市工业园区', level: 'A', status: '1', createTime: '2024-01-13 11:15:00', remark: '设备供应商' },
  { id: 5, supplierNo: 'S005', name: '杭州市化工原料公司', contact: '李经理', phone: '13900139005', email: 'li@huagong.com', address: '杭州市萧山区', level: 'C', status: '0', createTime: '2024-01-14 16:40:00', remark: '暂停合作' },
  { id: 6, supplierNo: 'S006', name: '佛山市五金制品厂', contact: '赵厂长', phone: '13900139006', email: 'zhao@wujin.com', address: '佛山市南海区', level: 'B', status: '1', createTime: '2024-01-15 08:50:00', remark: '' },
  { id: 7, supplierNo: 'S007', name: '中山市灯饰配件公司', contact: '孙经理', phone: '13900139007', email: 'sun@dengshi.com', address: '中山市古镇镇', level: 'C', status: '1', createTime: '2024-01-16 13:25:00', remark: '小供应商' },
  { id: 8, supplierNo: 'S008', name: '惠州市电池制造厂', contact: '周总', phone: '13900139008', email: 'zhou@dianchi.com', address: '惠州市仲恺高新区', level: 'A', status: '1', createTime: '2024-01-17 10:10:00', remark: '战略合作伙伴' }
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
  Object.assign(supplierForm, {
    id: null,
    supplierNo: '',
    name: '',
    contact: '',
    phone: '',
    email: '',
    address: '',
    level: 'B',
    status: '1',
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(supplierForm, row)
  dialogVisible.value = true
}

function handleView(row) {
  Object.assign(currentSupplier, row)
  detailVisible.value = true
}

async function handleSubmit() {
  if (!supplierFormRef.value) return
  try {
    await supplierFormRef.value.validate()
    submitLoading.value = true
    setTimeout(() => {
      if (isEdit.value) {
        const index = mockData.value.findIndex(item => item.id === supplierForm.id)
        if (index !== -1) {
          mockData.value[index] = { ...mockData.value[index], ...supplierForm }
        }
        ElMessage.success('编辑成功')
      } else {
        const maxId = Math.max(...mockData.value.map(item => item.id), 0)
        const newSupplier = {
          ...supplierForm,
          id: maxId + 1,
          supplierNo: 'S' + String(maxId + 1).padStart(3, '0'),
          createTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
        }
        mockData.value.unshift(newSupplier)
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
  ElMessageBox.confirm('确定要删除该供应商吗？', '提示', {
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
