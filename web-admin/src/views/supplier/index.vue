<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="供应商名称">
          <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="searchForm.contact" placeholder="请输入联系人" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="供应商状态">
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
        <span class="table-title">供应商列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增供应商
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="supplierNo" label="供应商编号" width="140" />
        <el-table-column prop="supplierName" label="供应商名称" min-width="180" />
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
        <el-table-column prop="creditRating" label="信用评级" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="primary">{{ row.creditRating || '-' }}</el-tag>
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
        <el-form-item label="供应商编号" prop="supplierNo">
          <el-input v-model="supplierForm.supplierNo" placeholder="请输入供应商编号" />
        </el-form-item>
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="supplierForm.supplierName" placeholder="请输入供应商名称" />
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
        <el-form-item label="省份" prop="province">
          <el-input v-model="supplierForm.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="supplierForm.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="supplierForm.address" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="供应商等级" prop="level">
          <el-select v-model="supplierForm.level" placeholder="请选择等级" style="width: 100%">
            <el-option label="A级（优质）" value="A" />
            <el-option label="B级（普通）" value="B" />
            <el-option label="C级（一般）" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item label="信用评级" prop="creditRating">
          <el-select v-model="supplierForm.creditRating" placeholder="请选择信用评级" style="width: 100%">
            <el-option label="AAA" value="AAA" />
            <el-option label="AA" value="AA" />
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商状态" prop="status">
          <el-radio-group v-model="supplierForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="supplierForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
        <el-descriptions-item label="供应商名称">{{ currentSupplier.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentSupplier.contact }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentSupplier.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentSupplier.email }}</el-descriptions-item>
        <el-descriptions-item label="省份">{{ currentSupplier.province }}</el-descriptions-item>
        <el-descriptions-item label="城市">{{ currentSupplier.city }}</el-descriptions-item>
        <el-descriptions-item label="等级">
          <el-tag :type="currentSupplier.level === 'A' ? 'danger' : currentSupplier.level === 'B' ? 'warning' : 'info'">
            {{ currentSupplier.level }}级
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="信用评级">
          <el-tag type="primary">{{ currentSupplier.creditRating || '-' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentSupplier.status === 1 ? 'success' : 'danger'">
            {{ currentSupplier.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentSupplier.address }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ currentSupplier.createTime ? currentSupplier.createTime.replace('T', ' ').substring(0, 19) : '-' }}
        </el-descriptions-item>
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
import { getSupplierList, createSupplier, updateSupplier, deleteSupplier } from '../../api/supplier'

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
  supplierName: '',
  contact: '',
  status: ''
})

const supplierForm = reactive({
  id: null,
  supplierNo: '',
  supplierName: '',
  contact: '',
  phone: '',
  email: '',
  province: '',
  city: '',
  level: 'B',
  creditRating: 'A',
  address: '',
  status: 1,
  remark: ''
})

const currentSupplier = reactive({})

const supplierRules = {
  supplierNo: [
    { required: true, message: '请输入供应商编号', trigger: 'blur' }
  ],
  supplierName: [
    { required: true, message: '请输入供应商名称', trigger: 'blur' }
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
      supplierName: searchForm.supplierName || undefined,
      contact: searchForm.contact || undefined,
      status: searchForm.status || undefined
    }
    const res = await getSupplierList(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取供应商列表失败:', error)
    ElMessage.error('获取供应商列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.supplierName = ''
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
    supplierName: '',
    contact: '',
    phone: '',
    email: '',
    province: '',
    city: '',
    level: 'B',
    creditRating: 'A',
    address: '',
    status: 1,
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
    if (isEdit.value) {
      await updateSupplier(supplierForm)
      ElMessage.success('编辑成功')
    } else {
      await createSupplier(supplierForm)
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
    await ElMessageBox.confirm(`确定要删除供应商「${row.supplierName}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteSupplier(row.id)
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