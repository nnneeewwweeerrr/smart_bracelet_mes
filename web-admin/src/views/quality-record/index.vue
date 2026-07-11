<template>
  <div class="page-container">
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="item in statCards" :key="item.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: item.color }">
              <el-icon :size="28"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="质检单号">
          <el-input v-model="searchForm.qualityNo" placeholder="请输入质检单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="工单号">
          <el-input v-model="searchForm.workOrderNo" placeholder="请输入工单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入产品名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="质检类型">
          <el-select v-model="searchForm.qualityType" placeholder="全部类型" clearable style="width: 140px">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="质检结果">
          <el-select v-model="searchForm.result" placeholder="全部结果" clearable style="width: 140px">
            <el-option v-for="item in resultOptions" :key="item.value" :label="item.label" :value="item.value" />
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
        <span class="table-title">质检记录列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增质检
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="qualityNo" label="质检单号" width="160" />
        <el-table-column prop="workOrderNo" label="工单号" width="150" />
        <el-table-column prop="productName" label="产品名称" min-width="180" />
        <el-table-column prop="qualityType" label="质检类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.qualityType === 1" type="primary" size="small">入库质检</el-tag>
            <el-tag v-else-if="row.qualityType === 2" type="success" size="small">过程质检</el-tag>
            <el-tag v-else-if="row.qualityType === 3" type="warning" size="small">成品质检</el-tag>
            <el-tag v-else type="info" size="small">出货质检</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="抽检数量" width="100" align="center" />
        <el-table-column prop="qualifiedQuantity" label="合格数量" width="100" align="center" />
        <el-table-column prop="unqualifiedQuantity" label="不合格数量" width="110" align="center" />
        <el-table-column prop="result" label="质检结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.result === 1 ? 'success' : 'danger'" size="small">
              {{ row.result === 1 ? '合格' : '不合格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="inspector" label="质检员" width="100" align="center" />
        <el-table-column prop="createTime" label="质检时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
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
      :title="isView ? '质检详情' : (isEdit ? '编辑质检' : '新增质检')"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="qualityFormRef"
        :model="qualityForm"
        :rules="qualityRules"
        label-width="120px"
        :disabled="isView"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工单号" prop="workOrderId">
              <el-select v-model="qualityForm.workOrderId" placeholder="请选择工单" style="width: 100%" @change="handleWorkOrderChange">
                <el-option
                  v-for="item in workOrderList"
                  :key="item.id"
                  :label="item.workOrderNo"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="质检类型" prop="qualityType">
              <el-select v-model="qualityForm.qualityType" placeholder="请选择类型" style="width: 100%">
                <el-option label="入库质检" :value="1" />
                <el-option label="过程质检" :value="2" />
                <el-option label="成品质检" :value="3" />
                <el-option label="出货质检" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品名称">
              <el-input v-model="qualityForm.productName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="质检员" prop="inspector">
              <el-input v-model="qualityForm.inspector" placeholder="请输入质检员" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="抽检数量" prop="quantity">
              <el-input-number v-model="qualityForm.quantity" :min="1" :max="99999" style="width: 100%" @change="handleQuantityChange" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="合格数量" prop="qualifiedQuantity">
              <el-input-number v-model="qualityForm.qualifiedQuantity" :min="0" :max="qualityForm.quantity" style="width: 100%" @change="handleQualifiedChange" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="不合格数量">
              <el-input-number v-model="qualityForm.unqualifiedQuantity" :min="0" disabled style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="质检结果" prop="result">
          <el-radio-group v-model="qualityForm.result">
            <el-radio :value="1">合格</el-radio>
            <el-radio :value="0">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="不合格项" v-if="qualityForm.result === 0">
          <el-checkbox-group v-model="qualityForm.unqualifiedItems">
            <el-checkbox label="外观不良">外观不良</el-checkbox>
            <el-checkbox label="尺寸超差">尺寸超差</el-checkbox>
            <el-checkbox label="功能异常">功能异常</el-checkbox>
            <el-checkbox label="包装缺陷">包装缺陷</el-checkbox>
            <el-checkbox label="其他">其他</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="质检备注" prop="remark">
          <el-input v-model="qualityForm.remark" type="textarea" :rows="4" placeholder="请输入质检备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ isView ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
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
const isEdit = ref(false)
const isView = ref(false)
const qualityFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  qualityNo: '',
  workOrderNo: '',
  productName: '',
  qualityType: '',
  result: ''
})

const qualityForm = reactive({
  id: null,
  qualityNo: '',
  workOrderId: null,
  workOrderNo: '',
  productName: '',
  qualityType: 2,
  quantity: 1,
  qualifiedQuantity: 0,
  unqualifiedQuantity: 0,
  result: 1,
  inspector: '',
  remark: '',
  unqualifiedItems: []
})

const typeOptions = [
  { label: '入库质检', value: 1 },
  { label: '过程质检', value: 2 },
  { label: '成品质检', value: 3 },
  { label: '出货质检', value: 4 }
]

const resultOptions = [
  { label: '合格', value: 1 },
  { label: '不合格', value: 0 }
]

const qualityRules = {
  workOrderId: [
    { required: true, message: '请选择工单', trigger: 'change' }
  ],
  qualityType: [
    { required: true, message: '请选择质检类型', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入抽检数量', trigger: 'blur' }
  ],
  result: [
    { required: true, message: '请选择质检结果', trigger: 'change' }
  ],
  inspector: [
    { required: true, message: '请输入质检员', trigger: 'blur' }
  ]
}

const workOrderList = ref([
  { id: 1, workOrderNo: 'WO-2024001', productName: '智能健康手环 Pro' },
  { id: 2, workOrderNo: 'WO-2024002', productName: '智能运动手环 Lite' },
  { id: 3, workOrderNo: 'WO-2024003', productName: '智能健康手环 Pro' },
  { id: 4, workOrderNo: 'WO-2024004', productName: '智能运动手环 Lite' },
  { id: 6, workOrderNo: 'WO-2024006', productName: '智能健康手环 Pro' }
])

const statCards = ref([
  { title: '今日质检', value: 28, icon: 'CircleCheck', color: '#409EFF' },
  { title: '合格数', value: 26, icon: 'SuccessFilled', color: '#67C23A' },
  { title: '不合格数', value: 2, icon: 'CircleClose', color: '#F56C6C' },
  { title: '合格率', value: '92.8%', icon: 'DataAnalysis', color: '#E6A23C' }
])

const mockData = ref([
  {
    id: 1,
    qualityNo: 'QC-20240115001',
    workOrderId: 1,
    workOrderNo: 'WO-2024001',
    productName: '智能健康手环 Pro',
    qualityType: 2,
    quantity: 50,
    qualifiedQuantity: 48,
    unqualifiedQuantity: 2,
    result: 0,
    inspector: '张质检',
    createTime: '2024-01-15 10:30:00',
    remark: '外观不良2件，已退回返工',
    unqualifiedItems: ['外观不良']
  },
  {
    id: 2,
    qualityNo: 'QC-20240115002',
    workOrderId: 1,
    workOrderNo: 'WO-2024001',
    productName: '智能健康手环 Pro',
    qualityType: 3,
    quantity: 100,
    qualifiedQuantity: 100,
    unqualifiedQuantity: 0,
    result: 1,
    inspector: '李质检',
    createTime: '2024-01-15 14:00:00',
    remark: '全部合格',
    unqualifiedItems: []
  },
  {
    id: 3,
    qualityNo: 'QC-20240114001',
    workOrderId: 2,
    workOrderNo: 'WO-2024002',
    productName: '智能运动手环 Lite',
    qualityType: 3,
    quantity: 80,
    qualifiedQuantity: 80,
    unqualifiedQuantity: 0,
    result: 1,
    inspector: '王质检',
    createTime: '2024-01-14 16:30:00',
    remark: '全部合格',
    unqualifiedItems: []
  },
  {
    id: 4,
    qualityNo: 'QC-20240114002',
    workOrderId: 4,
    workOrderNo: 'WO-2024004',
    productName: '智能运动手环 Lite',
    qualityType: 2,
    quantity: 30,
    qualifiedQuantity: 29,
    unqualifiedQuantity: 1,
    result: 0,
    inspector: '张质检',
    createTime: '2024-01-14 11:00:00',
    remark: '功能异常1件，已记录',
    unqualifiedItems: ['功能异常']
  },
  {
    id: 5,
    qualityNo: 'QC-20240113001',
    workOrderId: 6,
    workOrderNo: 'WO-2024006',
    productName: '智能健康手环 Pro',
    qualityType: 1,
    quantity: 200,
    qualifiedQuantity: 198,
    unqualifiedQuantity: 2,
    result: 1,
    inspector: '李质检',
    createTime: '2024-01-13 09:00:00',
    remark: '入库抽检，合格率达标',
    unqualifiedItems: ['外观不良']
  },
  {
    id: 6,
    qualityNo: 'QC-20240113002',
    workOrderId: 6,
    workOrderNo: 'WO-2024006',
    productName: '智能健康手环 Pro',
    qualityType: 2,
    quantity: 60,
    qualifiedQuantity: 60,
    unqualifiedQuantity: 0,
    result: 1,
    inspector: '王质检',
    createTime: '2024-01-13 15:00:00',
    remark: '过程质检合格',
    unqualifiedItems: []
  }
])

const tableData = ref([])

onMounted(() => {
  fetchData()
})

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filtered = [...mockData.value]
    if (searchForm.qualityNo) {
      filtered = filtered.filter(item => item.qualityNo.includes(searchForm.qualityNo))
    }
    if (searchForm.workOrderNo) {
      filtered = filtered.filter(item => item.workOrderNo.includes(searchForm.workOrderNo))
    }
    if (searchForm.productName) {
      filtered = filtered.filter(item => item.productName.includes(searchForm.productName))
    }
    if (searchForm.qualityType !== '') {
      filtered = filtered.filter(item => item.qualityType === searchForm.qualityType)
    }
    if (searchForm.result !== '') {
      filtered = filtered.filter(item => item.result === searchForm.result)
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
  searchForm.qualityNo = ''
  searchForm.workOrderNo = ''
  searchForm.productName = ''
  searchForm.qualityType = ''
  searchForm.result = ''
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
  qualityForm.id = row.id
  qualityForm.qualityNo = row.qualityNo
  qualityForm.workOrderId = row.workOrderId
  qualityForm.workOrderNo = row.workOrderNo
  qualityForm.productName = row.productName
  qualityForm.qualityType = row.qualityType
  qualityForm.quantity = row.quantity
  qualityForm.qualifiedQuantity = row.qualifiedQuantity
  qualityForm.unqualifiedQuantity = row.unqualifiedQuantity
  qualityForm.result = row.result
  qualityForm.inspector = row.inspector
  qualityForm.remark = row.remark
  qualityForm.unqualifiedItems = [...row.unqualifiedItems]
  dialogVisible.value = true
}

function handleView(row) {
  isEdit.value = false
  isView.value = true
  qualityForm.id = row.id
  qualityForm.qualityNo = row.qualityNo
  qualityForm.workOrderId = row.workOrderId
  qualityForm.workOrderNo = row.workOrderNo
  qualityForm.productName = row.productName
  qualityForm.qualityType = row.qualityType
  qualityForm.quantity = row.quantity
  qualityForm.qualifiedQuantity = row.qualifiedQuantity
  qualityForm.unqualifiedQuantity = row.unqualifiedQuantity
  qualityForm.result = row.result
  qualityForm.inspector = row.inspector
  qualityForm.remark = row.remark
  qualityForm.unqualifiedItems = [...row.unqualifiedItems]
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除质检记录「${row.qualityNo}」吗？`, '提示', {
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

function handleWorkOrderChange(val) {
  const order = workOrderList.value.find(o => o.id === val)
  if (order) {
    qualityForm.workOrderNo = order.workOrderNo
    qualityForm.productName = order.productName
  }
}

function handleQuantityChange() {
  if (qualityForm.qualifiedQuantity > qualityForm.quantity) {
    qualityForm.qualifiedQuantity = qualityForm.quantity
  }
  qualityForm.unqualifiedQuantity = qualityForm.quantity - qualityForm.qualifiedQuantity
}

function handleQualifiedChange() {
  qualityForm.unqualifiedQuantity = qualityForm.quantity - qualityForm.qualifiedQuantity
  if (qualityForm.unqualifiedQuantity > 0 && qualityForm.result === 1) {
    qualityForm.result = 0
  } else if (qualityForm.unqualifiedQuantity === 0 && qualityForm.result === 0) {
    qualityForm.result = 1
  }
}

function resetForm() {
  qualityForm.id = null
  qualityForm.qualityNo = ''
  qualityForm.workOrderId = null
  qualityForm.workOrderNo = ''
  qualityForm.productName = ''
  qualityForm.qualityType = 2
  qualityForm.quantity = 1
  qualityForm.qualifiedQuantity = 0
  qualityForm.unqualifiedQuantity = 0
  qualityForm.result = 1
  qualityForm.inspector = ''
  qualityForm.remark = ''
  qualityForm.unqualifiedItems = []
  if (qualityFormRef.value) {
    qualityFormRef.value.resetFields()
  }
}

async function handleSubmit() {
  if (!qualityFormRef.value) return
  
  try {
    await qualityFormRef.value.validate()
    submitLoading.value = true
    
    setTimeout(() => {
      if (isEdit.value) {
        const index = mockData.value.findIndex(item => item.id === qualityForm.id)
        if (index !== -1) {
          mockData.value[index] = {
            ...mockData.value[index],
            workOrderId: qualityForm.workOrderId,
            workOrderNo: qualityForm.workOrderNo,
            productName: qualityForm.productName,
            qualityType: qualityForm.qualityType,
            quantity: qualityForm.quantity,
            qualifiedQuantity: qualityForm.qualifiedQuantity,
            unqualifiedQuantity: qualityForm.unqualifiedQuantity,
            result: qualityForm.result,
            inspector: qualityForm.inspector,
            remark: qualityForm.remark,
            unqualifiedItems: [...qualityForm.unqualifiedItems]
          }
        }
        ElMessage.success('编辑成功')
      } else {
        const maxId = Math.max(...mockData.value.map(item => item.id), 0)
        const now = new Date()
        const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
        const newQuality = {
          id: maxId + 1,
          qualityNo: 'QC-' + dateStr + String(maxId + 1).padStart(3, '0'),
          workOrderId: qualityForm.workOrderId,
          workOrderNo: qualityForm.workOrderNo,
          productName: qualityForm.productName,
          qualityType: qualityForm.qualityType,
          quantity: qualityForm.quantity,
          qualifiedQuantity: qualityForm.qualifiedQuantity,
          unqualifiedQuantity: qualityForm.unqualifiedQuantity,
          result: qualityForm.result,
          inspector: qualityForm.inspector,
          createTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
          remark: qualityForm.remark,
          unqualifiedItems: [...qualityForm.unqualifiedItems]
        }
        mockData.value.unshift(newQuality)
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
</script>

<style scoped>
.page-container {
  width: 100%;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
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
