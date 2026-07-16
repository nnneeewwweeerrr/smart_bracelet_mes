<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="设备编号">
          <el-input v-model="searchForm.equipmentNo" placeholder="请输入设备编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="searchForm.equipmentName" placeholder="请输入设备名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-input v-model="searchForm.equipmentType" placeholder="请输入设备类型" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="车间">
          <el-input v-model="searchForm.workshop" placeholder="请输入车间" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="停用" :value="0" />
            <el-option label="运行" :value="1" />
            <el-option label="维修中" :value="2" />
            <el-option label="报废" :value="3" />
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
        <span class="table-title">设备列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增设备
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="equipmentNo" label="设备编号" width="120" />
        <el-table-column prop="equipmentName" label="设备名称" min-width="140" />
        <el-table-column prop="equipmentType" label="设备类型" width="110" />
        <el-table-column prop="model" label="规格型号" width="140" />
        <el-table-column prop="workshop" label="所在车间" width="110" />
        <el-table-column prop="manager" label="负责人" width="90" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">运行</el-tag>
            <el-tag v-else-if="row.status === 0" type="info">停用</el-tag>
            <el-tag v-else-if="row.status === 2" type="warning">维修中</el-tag>
            <el-tag v-else type="danger">报废</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="runningHours" label="运行时长(h)" width="100" align="right" />
        <el-table-column prop="nextMaintainDate" label="下次保养" width="160" />
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
      :title="isEdit ? '编辑设备' : '新增设备'"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="equipmentFormRef"
        :model="equipmentForm"
        :rules="equipmentRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备编号" prop="equipmentNo">
              <el-input v-model="equipmentForm.equipmentNo" placeholder="请输入设备编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备名称" prop="equipmentName">
              <el-input v-model="equipmentForm.equipmentName" placeholder="请输入设备名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备类型" prop="equipmentType">
              <el-input v-model="equipmentForm.equipmentType" placeholder="请输入设备类型" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格型号" prop="model">
              <el-input v-model="equipmentForm.model" placeholder="请输入规格型号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生产厂商" prop="manufacturer">
              <el-input v-model="equipmentForm.manufacturer" placeholder="请输入生产厂商" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所在车间" prop="workshop">
              <el-input v-model="equipmentForm.workshop" placeholder="请输入所在车间" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="安装位置" prop="location">
              <el-input v-model="equipmentForm.location" placeholder="请输入安装位置" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="manager">
              <el-input v-model="equipmentForm.manager" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="equipmentForm.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采购日期" prop="purchaseDate">
              <el-date-picker
                v-model="equipmentForm.purchaseDate"
                type="date"
                placeholder="请选择采购日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用日期" prop="commissionDate">
              <el-date-picker
                v-model="equipmentForm.commissionDate"
                type="date"
                placeholder="请选择启用日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="equipmentForm.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="停用" :value="0" />
                <el-option label="运行" :value="1" />
                <el-option label="维修中" :value="2" />
                <el-option label="报废" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="运行时长(h)" prop="runningHours">
              <el-input-number v-model="equipmentForm.runningHours" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="equipmentForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="设备详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="设备编号">{{ currentEquipment.equipmentNo }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentEquipment.equipmentName }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">{{ currentEquipment.equipmentType }}</el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ currentEquipment.model }}</el-descriptions-item>
        <el-descriptions-item label="生产厂商">{{ currentEquipment.manufacturer }}</el-descriptions-item>
        <el-descriptions-item label="所在车间">{{ currentEquipment.workshop }}</el-descriptions-item>
        <el-descriptions-item label="安装位置">{{ currentEquipment.location }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ currentEquipment.manager }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentEquipment.phone }}</el-descriptions-item>
        <el-descriptions-item label="采购日期">{{ currentEquipment.purchaseDate }}</el-descriptions-item>
        <el-descriptions-item label="启用日期">{{ currentEquipment.commissionDate }}</el-descriptions-item>
        <el-descriptions-item label="运行时长(小时)">{{ currentEquipment.runningHours }}</el-descriptions-item>
        <el-descriptions-item label="上次保养">{{ currentEquipment.lastMaintainDate }}</el-descriptions-item>
        <el-descriptions-item label="下次保养">{{ currentEquipment.nextMaintainDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentEquipment.status === 1" type="success">运行</el-tag>
          <el-tag v-else-if="currentEquipment.status === 0" type="info">停用</el-tag>
          <el-tag v-else-if="currentEquipment.status === 2" type="warning">维修中</el-tag>
          <el-tag v-else type="danger">报废</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentEquipment.remark || '无' }}</el-descriptions-item>
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
import { demoEquipment } from '@/api/demo'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const equipmentFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  equipmentNo: '',
  equipmentName: '',
  equipmentType: '',
  workshop: '',
  status: null
})

const equipmentForm = reactive({
  id: null,
  equipmentNo: '',
  equipmentName: '',
  equipmentType: '',
  model: '',
  manufacturer: '',
  workshop: '',
  location: '',
  manager: '',
  phone: '',
  purchaseDate: '',
  commissionDate: '',
  runningHours: 0,
  status: 1,
  remark: ''
})

const currentEquipment = reactive({})

const equipmentRules = {
  equipmentNo: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  equipmentName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  equipmentType: [{ required: true, message: '请输入设备类型', trigger: 'blur' }],
  workshop: [{ required: true, message: '请输入车间', trigger: 'blur' }],
  manager: [{ required: true, message: '请输入负责人', trigger: 'blur' }]
}

const tableData = ref([])

onMounted(() => {
  fetchData()
})

function fetchData() {
  loading.value = true
  demoEquipment.getList({
    pageNum: currentPage.value,
    pageSize: pageSize.value,
    ...searchForm
  }).then(res => {
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
    loading.value = false
  }).catch(err => {
    ElMessage.error('加载设备列表失败: ' + (err.message || '未知错误'))
    loading.value = false
  })
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.equipmentNo = ''
  searchForm.equipmentName = ''
  searchForm.equipmentType = ''
  searchForm.workshop = ''
  searchForm.status = null
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
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(equipmentForm, row)
  dialogVisible.value = true
}

function resetForm() {
  Object.keys(equipmentForm).forEach(key => {
    if (typeof equipmentForm[key] === 'number') {
      equipmentForm[key] = key === 'status' ? 1 : 0
    } else {
      equipmentForm[key] = ''
    }
  })
  equipmentForm.id = null
}

function handleView(row) {
  Object.assign(currentEquipment, row)
  detailVisible.value = true
}

function handleSubmit() {
  equipmentFormRef.value.validate(valid => {
    if (!valid) return
    submitLoading.value = true
    const action = isEdit.value ? demoEquipment.update(equipmentForm) : demoEquipment.save(equipmentForm)
    action.then(() => {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      submitLoading.value = false
      fetchData()
    }).catch(err => {
      ElMessage.error('操作失败: ' + (err.message || '未知错误'))
      submitLoading.value = false
    })
  })
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该设备吗？', '提示', {
    type: 'warning'
  }).then(() => {
    demoEquipment.remove(row.id).then(() => {
      ElMessage.success('删除成功')
      fetchData()
    }).catch(err => {
      ElMessage.error('删除失败: ' + (err.message || '未知错误'))
    })
  }).catch(() => {})
}
</script>

<style scoped>
.page-container { width: 100%; }
.search-card { margin-bottom: 20px; }
.table-card { margin-bottom: 20px; }
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
