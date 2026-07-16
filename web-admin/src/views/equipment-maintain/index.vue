<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="维保单号">
          <el-input v-model="searchForm.maintainNo" placeholder="请输入维保单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="设备编号">
          <el-input v-model="searchForm.equipmentNo" placeholder="请输入设备编号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="searchForm.equipmentName" placeholder="请输入设备名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="维保类型">
          <el-select v-model="searchForm.maintainType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="日常保养" value="日常保养" />
            <el-option label="定期保养" value="定期保养" />
            <el-option label="故障维修" value="故障维修" />
            <el-option label="大修" value="大修" />
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
        <span class="table-title">设备维保记录</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增维保
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="maintainNo" label="维保单号" width="160" />
        <el-table-column prop="equipmentNo" label="设备编号" width="120" />
        <el-table-column prop="equipmentName" label="设备名称" min-width="160" />
        <el-table-column prop="maintainType" label="维保类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getMaintainTypeTag(row.maintainType)">{{ row.maintainType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maintainLevel" label="维保级别" width="100" align="center" />
        <el-table-column prop="actualDate" label="维保日期" width="160" />
        <el-table-column prop="maintainer" label="维保人" width="100" />
        <el-table-column prop="cost" label="费用(元)" width="100" align="right" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
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
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="maintainFormRef"
        :model="maintainForm"
        :rules="maintainRules"
        label-width="100px"
        :disabled="isView"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="维保单号" prop="maintainNo">
              <el-input v-model="maintainForm.maintainNo" placeholder="如：MT202607001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备" prop="equipmentId">
              <el-select v-model="maintainForm.equipmentId" placeholder="请选择设备" style="width: 100%" @change="handleEquipmentChange" filterable>
                <el-option
                  v-for="item in equipmentList"
                  :key="item.id"
                  :label="`${item.equipmentNo} - ${item.equipmentName}`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备编号">
              <el-input v-model="maintainForm.equipmentNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备名称">
              <el-input v-model="maintainForm.equipmentName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="维保类型" prop="maintainType">
              <el-select v-model="maintainForm.maintainType" placeholder="请选择" style="width: 100%">
                <el-option label="日常保养" value="日常保养" />
                <el-option label="定期保养" value="定期保养" />
                <el-option label="故障维修" value="故障维修" />
                <el-option label="大修" value="大修" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="维保级别" prop="maintainLevel">
              <el-select v-model="maintainForm.maintainLevel" placeholder="请选择" style="width: 100%">
                <el-option label="一级" value="一级" />
                <el-option label="二级" value="二级" />
                <el-option label="三级" value="三级" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="维保人" prop="maintainer">
              <el-input v-model="maintainForm.maintainer" placeholder="请输入维保人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="耗时(小时)" prop="durationHours">
              <el-input-number v-model="maintainForm.durationHours" :min="0" :precision="1" :step="0.5" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="维保费用(元)" prop="cost">
              <el-input-number v-model="maintainForm.cost" :min="0" :precision="2" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="维保结果" prop="maintainResult">
              <el-select v-model="maintainForm.maintainResult" placeholder="请选择" style="width: 100%">
                <el-option label="合格" value="合格" />
                <el-option label="不合格" value="不合格" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="维保内容" prop="maintainContent">
              <el-input v-model="maintainForm.maintainContent" type="textarea" :rows="3" placeholder="请输入维保内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="maintainForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ isView ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { demoEquipment, demoEquipmentMaintain } from '@/api/demo'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false)
const maintainFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const equipmentList = ref([])

const searchForm = reactive({
  maintainNo: '',
  equipmentNo: '',
  equipmentName: '',
  maintainType: ''
})

const maintainForm = reactive({
  id: null,
  maintainNo: '',
  equipmentId: null,
  equipmentNo: '',
  equipmentName: '',
  maintainType: '日常保养',
  maintainLevel: '一级',
  planDate: '',
  actualDate: '',
  maintainContent: '',
  maintainResult: '合格',
  cost: 0,
  maintainer: '',
  durationHours: 0,
  status: 1,
  remark: ''
})

const maintainRules = {
  maintainNo: [{ required: true, message: '请输入维保单号', trigger: 'blur' }],
  equipmentId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  maintainType: [{ required: true, message: '请选择维保类型', trigger: 'change' }],
  maintainer: [{ required: true, message: '请输入维保人', trigger: 'blur' }],
  maintainContent: [{ required: true, message: '请输入维保内容', trigger: 'blur' }]
}

const dialogTitle = computed(() => {
  if (isView.value) return '维保详情'
  return isEdit.value ? '编辑维保记录' : '新增维保记录'
})

const tableData = ref([])

function getMaintainTypeTag(type) {
  const map = { '日常保养': 'info', '定期保养': 'primary', '故障维修': 'warning', '大修': 'danger' }
  return map[type] || 'info'
}

function getStatusType(status) {
  const map = ['', 'success', 'info']
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = ['未完成', '已完成', '已取消']
  return map[status] || '未知'
}

function fetchData() {
  loading.value = true
  demoEquipmentMaintain.getList({
    pageNum: currentPage.value,
    pageSize: pageSize.value,
    ...searchForm
  }).then(res => {
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
    loading.value = false
  }).catch(err => {
    ElMessage.error('加载维保记录失败: ' + (err.message || '未知错误'))
    loading.value = false
  })
}

function fetchEquipments() {
  demoEquipment.getAll().then(res => {
    equipmentList.value = res.data || []
  }).catch(() => {})
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.maintainNo = ''
  searchForm.equipmentNo = ''
  searchForm.equipmentName = ''
  searchForm.maintainType = ''
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
  isView.value = false
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  isView.value = false
  Object.assign(maintainForm, row)
  dialogVisible.value = true
}

function handleView(row) {
  isEdit.value = false
  isView.value = true
  Object.assign(maintainForm, row)
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除维保记录「${row.maintainNo}」吗？`, '提示', { type: 'warning' })
    .then(() => {
      demoEquipmentMaintain.remove(row.id).then(() => {
        ElMessage.success('删除成功')
        fetchData()
      }).catch(err => {
        ElMessage.error('删除失败: ' + (err.message || '未知错误'))
      })
    }).catch(() => {})
}

function handleEquipmentChange(val) {
  const equip = equipmentList.value.find(e => e.id === val)
  if (equip) {
    maintainForm.equipmentNo = equip.equipmentNo
    maintainForm.equipmentName = equip.equipmentName
  }
}

function resetForm() {
  Object.keys(maintainForm).forEach(key => {
    if (typeof maintainForm[key] === 'number') {
      maintainForm[key] = 0
    } else {
      maintainForm[key] = ''
    }
  })
  maintainForm.status = 1
  maintainForm.maintainType = '日常保养'
  maintainForm.maintainLevel = '一级'
  maintainForm.maintainResult = '合格'
  maintainForm.durationHours = 0
  maintainForm.cost = 0
  if (maintainFormRef.value) maintainFormRef.value.resetFields()
}

function handleSubmit() {
  maintainFormRef.value.validate(valid => {
    if (!valid) return
    submitLoading.value = true
    const action = isEdit.value ? demoEquipmentMaintain.update(maintainForm) : demoEquipmentMaintain.save(maintainForm)
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

onMounted(() => {
  fetchEquipments()
  fetchData()
})
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
