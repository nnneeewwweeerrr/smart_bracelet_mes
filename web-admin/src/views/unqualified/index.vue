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
        <el-form-item label="不合格编号">
          <el-input v-model="searchForm.unqualifiedNo" placeholder="请输入编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入产品名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理方式">
          <el-select v-model="searchForm.handleType" placeholder="全部方式" clearable style="width: 140px">
            <el-option v-for="item in handleTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
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
        <span class="table-title">不合格品列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          登记不合格
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="unqualifiedNo" label="不合格编号" width="160" />
        <el-table-column prop="qualityNo" label="关联质检单" width="160" />
        <el-table-column prop="productName" label="产品名称" min-width="160" />
        <el-table-column prop="unqualifiedQty" label="不合格数量" width="110" align="center" />
        <el-table-column prop="unqualifiedItems" label="不合格项" min-width="160">
          <template #default="{ row }">
            <el-tag v-for="(item, idx) in row.unqualifiedItems" :key="idx" size="small" style="margin-right: 4px; margin-bottom: 4px;">
              {{ item }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handleType" label="处理方式" width="110" align="center" v-if="showHandleType">
          <template #default="{ row }">
            <span v-if="row.handleType">{{ getHandleTypeText(row.handleType) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="登记时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button
              type="primary"
              link
              @click="handleEdit(row)"
              :disabled="row.status !== 0"
            >
              编辑
            </el-button>
            <el-button
              type="success"
              link
              @click="handleHandle(row)"
              :disabled="row.status !== 0"
            >
              处理
            </el-button>
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
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="unqualifiedFormRef"
        :model="unqualifiedForm"
        :rules="unqualifiedRules"
        label-width="120px"
        :disabled="isView"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="关联质检单" prop="qualityId">
              <el-select v-model="unqualifiedForm.qualityId" placeholder="请选择质检单" style="width: 100%" @change="handleQualityChange">
                <el-option
                  v-for="item in qualityList"
                  :key="item.id"
                  :label="item.qualityNo"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品名称">
              <el-input v-model="unqualifiedForm.productName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="不合格数量" prop="unqualifiedQty">
              <el-input-number v-model="unqualifiedForm.unqualifiedQty" :min="1" :max="99999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发现工序" prop="processName">
              <el-input v-model="unqualifiedForm.processName" placeholder="请输入工序" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="不合格项" prop="unqualifiedItems">
          <el-checkbox-group v-model="unqualifiedForm.unqualifiedItems">
            <el-checkbox label="外观不良">外观不良</el-checkbox>
            <el-checkbox label="尺寸超差">尺寸超差</el-checkbox>
            <el-checkbox label="功能异常">功能异常</el-checkbox>
            <el-checkbox label="性能不达标">性能不达标</el-checkbox>
            <el-checkbox label="包装缺陷">包装缺陷</el-checkbox>
            <el-checkbox label="其他">其他</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="问题描述" prop="description">
          <el-input v-model="unqualifiedForm.description" type="textarea" :rows="3" placeholder="请输入问题描述" />
        </el-form-item>
        <el-form-item label="责任人" prop="responsiblePerson">
          <el-input v-model="unqualifiedForm.responsiblePerson" placeholder="请输入责任人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ isView ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewVisible" title="不合格品详情" width="700px">
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="不合格编号">{{ currentItem.unqualifiedNo }}</el-descriptions-item>
        <el-descriptions-item label="关联质检单">{{ currentItem.qualityNo }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ currentItem.productName }}</el-descriptions-item>
        <el-descriptions-item label="不合格数量">{{ currentItem.unqualifiedQty }}</el-descriptions-item>
        <el-descriptions-item label="发现工序">{{ currentItem.processName }}</el-descriptions-item>
        <el-descriptions-item label="责任人">{{ currentItem.responsiblePerson }}</el-descriptions-item>
        <el-descriptions-item label="登记时间" :span="2">{{ currentItem.createTime }}</el-descriptions-item>
        <el-descriptions-item label="不合格项" :span="2">
          <el-tag v-for="(item, idx) in currentItem.unqualifiedItems" :key="idx" size="small" style="margin-right: 4px;">
            {{ item }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">{{ currentItem.description }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentItem.status)" size="small">
            {{ getStatusText(currentItem.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理方式" v-if="currentItem.handleType">
          {{ getHandleTypeText(currentItem.handleType) }}
        </el-descriptions-item>
        <el-descriptions-item label="处理人" v-if="currentItem.handlePerson">
          {{ currentItem.handlePerson }}
        </el-descriptions-item>
        <el-descriptions-item label="处理时间" v-if="currentItem.handleTime" :span="2">
          {{ currentItem.handleTime }}
        </el-descriptions-item>
        <el-descriptions-item label="处理意见" v-if="currentItem.handleRemark" :span="2">
          {{ currentItem.handleRemark }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="handleVisible"
      title="不合格品处理"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="handleFormRef" :model="handleForm" :rules="handleRules" label-width="120px">
        <el-form-item label="处理方式" prop="handleType">
          <el-radio-group v-model="handleForm.handleType">
            <el-radio value="rework">返工</el-radio>
            <el-radio value="scrap">报废</el-radio>
            <el-radio value="concession">让步接收</el-radio>
            <el-radio value="other">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理人" prop="handlePerson">
          <el-input v-model="handleForm.handlePerson" placeholder="请输入处理人" />
        </el-form-item>
        <el-form-item label="处理意见" prop="handleRemark">
          <el-input v-model="handleForm.handleRemark" type="textarea" :rows="4" placeholder="请输入处理意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" :loading="handleSubmitLoading" @click="handleHandleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const handleSubmitLoading = ref(false)
const dialogVisible = ref(false)
const viewVisible = ref(false)
const handleVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false)
const unqualifiedFormRef = ref(null)
const handleFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  unqualifiedNo: '',
  productName: '',
  status: '',
  handleType: ''
})

const unqualifiedForm = reactive({
  id: null,
  unqualifiedNo: '',
  qualityId: null,
  qualityNo: '',
  productName: '',
  unqualifiedQty: 1,
  processName: '',
  unqualifiedItems: [],
  description: '',
  responsiblePerson: ''
})

const currentItem = reactive({})

const handleForm = reactive({
  id: null,
  handleType: '',
  handlePerson: '',
  handleRemark: ''
})

const statusOptions = [
  { label: '待处理', value: 0 },
  { label: '处理中', value: 1 },
  { label: '已处理', value: 2 },
  { label: '已关闭', value: 3 }
]

const handleTypeOptions = [
  { label: '返工', value: 'rework' },
  { label: '报废', value: 'scrap' },
  { label: '让步接收', value: 'concession' },
  { label: '其他', value: 'other' }
]

const unqualifiedRules = {
  qualityId: [
    { required: true, message: '请选择关联质检单', trigger: 'change' }
  ],
  unqualifiedQty: [
    { required: true, message: '请输入不合格数量', trigger: 'blur' }
  ],
  processName: [
    { required: true, message: '请输入发现工序', trigger: 'blur' }
  ],
  unqualifiedItems: [
    { required: true, message: '请选择不合格项', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入问题描述', trigger: 'blur' }
  ],
  responsiblePerson: [
    { required: true, message: '请输入责任人', trigger: 'blur' }
  ]
}

const handleRules = {
  handleType: [
    { required: true, message: '请选择处理方式', trigger: 'change' }
  ],
  handlePerson: [
    { required: true, message: '请输入处理人', trigger: 'blur' }
  ],
  handleRemark: [
    { required: true, message: '请输入处理意见', trigger: 'blur' }
  ]
}

const qualityList = ref([
  { id: 1, qualityNo: 'QC-20240115001', productName: '智能手环Pro', unqualifiedQty: 2 },
  { id: 4, qualityNo: 'QC-20240114002', productName: '智能手环Lite', unqualifiedQty: 1 },
  { id: 6, qualityNo: 'QC-20240113002', productName: '智能手环Pro', unqualifiedQty: 0 }
])

const mockData = ref([
  {
    id: 1,
    unqualifiedNo: 'UQ-20240115001',
    qualityId: 1,
    qualityNo: 'QC-20240115001',
    productName: '智能手环Pro',
    unqualifiedQty: 2,
    processName: '组装工序',
    unqualifiedItems: ['外观不良'],
    description: '外壳有划痕，2件',
    responsiblePerson: '张组装',
    status: 0,
    createTime: '2024-01-15 10:30:00'
  },
  {
    id: 2,
    unqualifiedNo: 'UQ-20240114001',
    qualityId: 4,
    qualityNo: 'QC-20240114002',
    productName: '智能手环Lite',
    unqualifiedQty: 1,
    processName: '测试工序',
    unqualifiedItems: ['功能异常'],
    description: '心率检测不准，1件',
    responsiblePerson: '李测试',
    status: 1,
    handleType: 'rework',
    handlePerson: '王工艺',
    handleTime: '2024-01-14 16:00:00',
    handleRemark: '已安排返工，重新烧录固件',
    createTime: '2024-01-14 11:00:00'
  },
  {
    id: 3,
    unqualifiedNo: 'UQ-20240113001',
    qualityId: 5,
    qualityNo: 'QC-20240113001',
    productName: '智能手环Pro',
    unqualifiedQty: 5,
    processName: 'SMT工序',
    unqualifiedItems: ['尺寸超差', '外观不良'],
    description: '来料PCB板尺寸偏差，导致5件虚焊',
    responsiblePerson: '赵质检',
    status: 2,
    handleType: 'scrap',
    handlePerson: '孙主管',
    handleTime: '2024-01-13 17:00:00',
    handleRemark: '判定报废，已退回供应商',
    createTime: '2024-01-13 09:30:00'
  },
  {
    id: 4,
    unqualifiedNo: 'UQ-20240112001',
    qualityId: 3,
    qualityNo: 'QC-20240112001',
    productName: '智能手环Ultra',
    unqualifiedQty: 3,
    processName: '包装工序',
    unqualifiedItems: ['包装缺陷'],
    description: '包装盒印刷模糊，3件',
    responsiblePerson: '周包装',
    status: 2,
    handleType: 'concession',
    handlePerson: '吴经理',
    handleTime: '2024-01-12 15:00:00',
    handleRemark: '不影响产品功能，让步接收',
    createTime: '2024-01-12 10:00:00'
  }
])

const showHandleType = computed(() => {
  return mockData.value.some(item => item.handleType)
})

const statCards = computed(() => [
  { title: '待处理', value: pendingCount.value, icon: 'Warning', color: '#F56C6C' },
  { title: '处理中', value: processingCount.value, icon: 'Clock', color: '#E6A23C' },
  { title: '已处理', value: processedCount.value, icon: 'CircleCheck', color: '#67C23A' },
  { title: '本月累计', value: mockData.value.length, icon: 'Document', color: '#409EFF' }
])

const pendingCount = computed(() => mockData.value.filter(item => item.status === 0).length)
const processingCount = computed(() => mockData.value.filter(item => item.status === 1).length)
const processedCount = computed(() => mockData.value.filter(item => item.status === 2 || item.status === 3).length)

const tableData = ref([])

const dialogTitle = computed(() => {
  if (isView.value) return '不合格品详情'
  return isEdit.value ? '编辑不合格品' : '登记不合格品'
})

function getStatusType(status) {
  const types = ['danger', 'warning', 'success', 'info']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['待处理', '处理中', '已处理', '已关闭']
  return texts[status] || '未知'
}

function getHandleTypeText(type) {
  const map = {
    rework: '返工',
    scrap: '报废',
    concession: '让步接收',
    other: '其他'
  }
  return map[type] || type
}

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filtered = [...mockData.value]
    if (searchForm.unqualifiedNo) {
      filtered = filtered.filter(item => item.unqualifiedNo.includes(searchForm.unqualifiedNo))
    }
    if (searchForm.productName) {
      filtered = filtered.filter(item => item.productName.includes(searchForm.productName))
    }
    if (searchForm.status !== '') {
      filtered = filtered.filter(item => item.status === searchForm.status)
    }
    if (searchForm.handleType) {
      filtered = filtered.filter(item => item.handleType === searchForm.handleType)
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
  searchForm.unqualifiedNo = ''
  searchForm.productName = ''
  searchForm.status = ''
  searchForm.handleType = ''
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
  Object.assign(unqualifiedForm, JSON.parse(JSON.stringify(row)))
  dialogVisible.value = true
}

function handleView(row) {
  Object.assign(currentItem, row)
  viewVisible.value = true
}

function handleHandle(row) {
  handleForm.id = row.id
  handleForm.handleType = ''
  handleForm.handlePerson = ''
  handleForm.handleRemark = ''
  handleVisible.value = true
}

function handleQualityChange(val) {
  const quality = qualityList.value.find(q => q.id === val)
  if (quality) {
    unqualifiedForm.qualityNo = quality.qualityNo
    unqualifiedForm.productName = quality.productName
  }
}

function resetForm() {
  unqualifiedForm.id = null
  unqualifiedForm.unqualifiedNo = ''
  unqualifiedForm.qualityId = null
  unqualifiedForm.qualityNo = ''
  unqualifiedForm.productName = ''
  unqualifiedForm.unqualifiedQty = 1
  unqualifiedForm.processName = ''
  unqualifiedForm.unqualifiedItems = []
  unqualifiedForm.description = ''
  unqualifiedForm.responsiblePerson = ''
  if (unqualifiedFormRef.value) {
    unqualifiedFormRef.value.resetFields()
  }
}

async function handleSubmit() {
  if (!unqualifiedFormRef.value) return
  
  try {
    await unqualifiedFormRef.value.validate()
    submitLoading.value = true
    
    setTimeout(() => {
      if (isEdit.value) {
        const index = mockData.value.findIndex(item => item.id === unqualifiedForm.id)
        if (index !== -1) {
          mockData.value[index] = {
            ...mockData.value[index],
            qualityId: unqualifiedForm.qualityId,
            qualityNo: unqualifiedForm.qualityNo,
            productName: unqualifiedForm.productName,
            unqualifiedQty: unqualifiedForm.unqualifiedQty,
            processName: unqualifiedForm.processName,
            unqualifiedItems: [...unqualifiedForm.unqualifiedItems],
            description: unqualifiedForm.description,
            responsiblePerson: unqualifiedForm.responsiblePerson
          }
        }
        ElMessage.success('编辑成功')
      } else {
        const maxId = Math.max(...mockData.value.map(item => item.id), 0)
        const now = new Date()
        const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
        const newItem = {
          id: maxId + 1,
          unqualifiedNo: 'UQ-' + dateStr + String(maxId + 1).padStart(3, '0'),
          qualityId: unqualifiedForm.qualityId,
          qualityNo: unqualifiedForm.qualityNo,
          productName: unqualifiedForm.productName,
          unqualifiedQty: unqualifiedForm.unqualifiedQty,
          processName: unqualifiedForm.processName,
          unqualifiedItems: [...unqualifiedForm.unqualifiedItems],
          description: unqualifiedForm.description,
          responsiblePerson: unqualifiedForm.responsiblePerson,
          status: 0,
          createTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
        }
        mockData.value.unshift(newItem)
        ElMessage.success('登记成功')
      }
      
      dialogVisible.value = false
      submitLoading.value = false
      fetchData()
    }, 300)
  } catch (error) {
    submitLoading.value = false
  }
}

async function handleHandleSubmit() {
  if (!handleFormRef.value) return
  
  try {
    await handleFormRef.value.validate()
    handleSubmitLoading.value = true
    
    setTimeout(() => {
      const item = mockData.value.find(i => i.id === handleForm.id)
      if (item) {
        item.status = 2
        item.handleType = handleForm.handleType
        item.handlePerson = handleForm.handlePerson
        item.handleTime = new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
        item.handleRemark = handleForm.handleRemark
      }
      ElMessage.success('处理成功')
      handleVisible.value = false
      handleSubmitLoading.value = false
      fetchData()
    }, 300)
  } catch (error) {
    handleSubmitLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
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
