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
        <el-form-item label="预警类型">
          <el-select v-model="searchForm.warningType" placeholder="全部类型" clearable style="width: 140px">
            <el-option label="低库存预警" value="low" />
            <el-option label="超储预警" value="high" />
          </el-select>
        </el-form-item>
        <el-form-item label="物料编号">
          <el-input v-model="searchForm.materialNo" placeholder="请输入物料编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="物料名称">
          <el-input v-model="searchForm.materialName" placeholder="请输入物料名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouse" placeholder="全部仓库" clearable style="width: 140px">
            <el-option label="原料仓" value="原料仓" />
            <el-option label="半成品仓" value="半成品仓" />
            <el-option label="成品仓" value="成品仓" />
            <el-option label="辅料仓" value="辅料仓" />
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
        <span class="table-title">预警列表</span>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="warningType" label="预警类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.warningType === 'low' ? 'danger' : 'warning'" size="small">
              {{ row.warningType === 'low' ? '低库存' : '超储' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="materialNo" label="物料编号" width="140" />
        <el-table-column prop="materialName" label="物料名称" min-width="160" />
        <el-table-column prop="spec" label="规格型号" width="140" />
        <el-table-column prop="warehouse" label="仓库" width="110" />
        <el-table-column prop="quantity" label="当前库存" width="110" align="right">
          <template #default="{ row }">
            <span :class="row.warningType === 'low' ? 'text-danger' : 'text-warning'">
              {{ row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="safetyStock" label="安全库存" width="100" align="right" />
        <el-table-column prop="maxStock" label="最高库存" width="100" align="right" />
        <el-table-column prop="diffQty" label="差异数量" width="100" align="right">
          <template #default="{ row }">
            <span :class="row.warningType === 'low' ? 'text-danger' : 'text-warning'">
              {{ row.warningType === 'low' ? '-' : '+' }}{{ Math.abs(row.diffQty) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="warningTime" label="预警时间" width="170" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'danger' : 'success'" size="small">
              {{ row.status === 0 ? '待处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button
              type="success"
              link
              @click="handleProcess(row)"
              :disabled="row.status === 1"
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

    <el-dialog v-model="detailVisible" title="预警详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="预警类型">
          <el-tag :type="currentWarning.warningType === 'low' ? 'danger' : 'warning'" size="small">
            {{ currentWarning.warningType === 'low' ? '低库存预警' : '超储预警' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预警状态">
          <el-tag :type="currentWarning.status === 0 ? 'danger' : 'success'" size="small">
            {{ currentWarning.status === 0 ? '待处理' : '已处理' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="物料编号">{{ currentWarning.materialNo }}</el-descriptions-item>
        <el-descriptions-item label="物料名称">{{ currentWarning.materialName }}</el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ currentWarning.spec }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ currentWarning.warehouse }}</el-descriptions-item>
        <el-descriptions-item label="当前库存">{{ currentWarning.quantity }}</el-descriptions-item>
        <el-descriptions-item label="安全库存">{{ currentWarning.safetyStock }}</el-descriptions-item>
        <el-descriptions-item label="最高库存">{{ currentWarning.maxStock }}</el-descriptions-item>
        <el-descriptions-item label="差异数量">
          <span :class="currentWarning.warningType === 'low' ? 'text-danger' : 'text-warning'">
            {{ currentWarning.warningType === 'low' ? '-' : '+' }}{{ Math.abs(currentWarning.diffQty || 0) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="预警时间" :span="2">{{ currentWarning.warningTime }}</el-descriptions-item>
        <el-descriptions-item label="处理人" v-if="currentWarning.status === 1">{{ currentWarning.processPerson }}</el-descriptions-item>
        <el-descriptions-item label="处理时间" v-if="currentWarning.status === 1">{{ currentWarning.processTime }}</el-descriptions-item>
        <el-descriptions-item label="处理备注" :span="2" v-if="currentWarning.status === 1">
          {{ currentWarning.processRemark || '无' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="processVisible" title="处理预警" width="500px" :close-on-click-modal="false">
      <el-form ref="processFormRef" :model="processForm" :rules="processRules" label-width="100px">
        <el-form-item label="处理人" prop="processPerson">
          <el-input v-model="processForm.processPerson" placeholder="请输入处理人" />
        </el-form-item>
        <el-form-item label="处理方式" prop="processType">
          <el-select v-model="processForm.processType" placeholder="请选择处理方式" style="width: 100%">
            <el-option label="已通知采购" value="notify_purchase" />
            <el-option label="已安排补货" value="arrange" />
            <el-option label="已调整库存上限" value="adjust_max" />
            <el-option label="已安排出库" value="arrange_out" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注" prop="processRemark">
          <el-input v-model="processForm.processRemark" type="textarea" :rows="3" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleProcessSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const detailVisible = ref(false)
const processVisible = ref(false)
const processFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  warningType: '',
  materialNo: '',
  materialName: '',
  warehouse: ''
})

const currentWarning = reactive({})

const processForm = reactive({
  id: null,
  processPerson: '',
  processType: '',
  processRemark: ''
})

const processRules = {
  processPerson: [
    { required: true, message: '请输入处理人', trigger: 'blur' }
  ],
  processType: [
    { required: true, message: '请选择处理方式', trigger: 'change' }
  ]
}

const statCards = computed(() => [
  { title: '低库存预警', value: lowStockCount.value, icon: 'Warning', color: '#f1a7b8' },
  { title: '超储预警', value: highStockCount.value, icon: 'WarningFilled', color: '#fae1a4' },
  { title: '待处理', value: pendingCount.value, icon: 'Clock', color: '#93c5dd' },
  { title: '已处理', value: processedCount.value, icon: 'CircleCheck', color: '#83d8cf' }
])

const mockData = ref([
  {
    id: 1,
    warningType: 'low',
    materialNo: 'M-002',
    materialName: '显示屏',
    spec: '1.5寸',
    warehouse: '原料仓',
    quantity: 80,
    safetyStock: 100,
    maxStock: 1000,
    diffQty: -20,
    warningTime: '2024-01-15 08:00:00',
    status: 0
  },
  {
    id: 2,
    warningType: 'low',
    materialNo: 'M-005',
    materialName: '心率传感器',
    spec: 'MAX30102',
    warehouse: '原料仓',
    quantity: 50,
    safetyStock: 200,
    maxStock: 800,
    diffQty: -150,
    warningTime: '2024-01-14 16:30:00',
    status: 0
  },
  {
    id: 3,
    warningType: 'high',
    materialNo: 'M-003',
    materialName: '电池',
    spec: '100mAh',
    warehouse: '原料仓',
    quantity: 1200,
    safetyStock: 200,
    maxStock: 1000,
    diffQty: 200,
    warningTime: '2024-01-15 09:00:00',
    status: 0
  },
  {
    id: 4,
    warningType: 'low',
    materialNo: 'P-003',
    materialName: '智能手环Ultra',
    spec: 'SH-U003',
    warehouse: '成品仓',
    quantity: 20,
    safetyStock: 50,
    maxStock: 300,
    diffQty: -30,
    warningTime: '2024-01-13 10:00:00',
    status: 1,
    processPerson: '王仓库',
    processTime: '2024-01-13 14:00:00',
    processRemark: '已通知生产部加急生产'
  },
  {
    id: 5,
    warningType: 'low',
    materialNo: 'M-007',
    materialName: '充电线',
    spec: 'Type-C',
    warehouse: '辅料仓',
    quantity: 100,
    safetyStock: 300,
    maxStock: 2000,
    diffQty: -200,
    warningTime: '2024-01-12 11:00:00',
    status: 1,
    processPerson: '李采购',
    processTime: '2024-01-12 15:30:00',
    processRemark: '已下采购订单，预计3天后到货'
  },
  {
    id: 6,
    warningType: 'high',
    materialNo: 'M-004',
    materialName: '表带',
    spec: '硅胶黑色',
    warehouse: '原料仓',
    quantity: 1500,
    safetyStock: 200,
    maxStock: 1000,
    diffQty: 500,
    warningTime: '2024-01-14 08:00:00',
    status: 0
  }
])

const lowStockCount = computed(() => mockData.value.filter(item => item.warningType === 'low').length)
const highStockCount = computed(() => mockData.value.filter(item => item.warningType === 'high').length)
const pendingCount = computed(() => mockData.value.filter(item => item.status === 0).length)
const processedCount = computed(() => mockData.value.filter(item => item.status === 1).length)

const tableData = ref([])

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filtered = [...mockData.value]
    if (searchForm.warningType) {
      filtered = filtered.filter(item => item.warningType === searchForm.warningType)
    }
    if (searchForm.materialNo) {
      filtered = filtered.filter(item => item.materialNo.includes(searchForm.materialNo))
    }
    if (searchForm.materialName) {
      filtered = filtered.filter(item => item.materialName.includes(searchForm.materialName))
    }
    if (searchForm.warehouse) {
      filtered = filtered.filter(item => item.warehouse === searchForm.warehouse)
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
  searchForm.warningType = ''
  searchForm.materialNo = ''
  searchForm.materialName = ''
  searchForm.warehouse = ''
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

function handleView(row) {
  Object.assign(currentWarning, row)
  detailVisible.value = true
}

function handleProcess(row) {
  processForm.id = row.id
  processForm.processPerson = ''
  processForm.processType = ''
  processForm.processRemark = ''
  processVisible.value = true
}

async function handleProcessSubmit() {
  if (!processFormRef.value) return
  
  try {
    await processFormRef.value.validate()
    submitLoading.value = true
    
    setTimeout(() => {
      const warning = mockData.value.find(item => item.id === processForm.id)
      if (warning) {
        warning.status = 1
        warning.processPerson = processForm.processPerson
        warning.processTime = new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
        warning.processRemark = processForm.processRemark
      }
      ElMessage.success('处理成功')
      processVisible.value = false
      submitLoading.value = false
      fetchData()
    }, 300)
  } catch (error) {
    submitLoading.value = false
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

.text-danger {
  color: #f1a7b8;
  font-weight: bold;
}

.text-warning {
  color: #fae1a4;
  font-weight: bold;
}
</style>
