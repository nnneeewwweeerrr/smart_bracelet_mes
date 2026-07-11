<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="物料编号">
          <el-input v-model="searchForm.materialNo" placeholder="请输入物料编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="物料名称">
          <el-input v-model="searchForm.materialName" placeholder="请输入物料名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="物料类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 140px">
            <el-option label="原材料" value="raw" />
            <el-option label="半成品" value="semi" />
            <el-option label="辅料" value="aux" />
            <el-option label="成品" value="finished" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouse" placeholder="请选择" clearable style="width: 140px">
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
        <span class="table-title">库存列表</span>
        <div class="header-stats">
          <el-tag type="primary" size="medium">物料种类：{{ total }}</el-tag>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialNo" label="物料编号" width="140" />
        <el-table-column prop="materialName" label="物料名称" min-width="160" />
        <el-table-column prop="type" label="物料类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'raw'" type="primary">原材料</el-tag>
            <el-tag v-else-if="row.type === 'semi'" type="warning">半成品</el-tag>
            <el-tag v-else-if="row.type === 'aux'" type="info">辅料</el-tag>
            <el-tag v-else type="success">成品</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="spec" label="规格型号" width="140" />
        <el-table-column prop="unit" label="单位" width="70" align="center" />
        <el-table-column prop="warehouse" label="仓库" width="110" />
        <el-table-column prop="quantity" label="库存数量" width="110" align="right">
          <template #default="{ row }">
            <span :class="{ 'low-stock': row.quantity < row.safetyStock }">
              {{ row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="safetyStock" label="安全库存" width="100" align="right" />
        <el-table-column prop="avgPrice" label="均价（元）" width="110" align="right">
          <template #default="{ row }">
            ¥{{ row.avgPrice?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalValue" label="库存金额（元）" width="140" align="right">
          <template #default="{ row }">
            ¥{{ row.totalValue?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
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

    <el-dialog v-model="detailVisible" title="库存详情" width="700px">
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="物料编号">{{ currentStock.materialNo }}</el-descriptions-item>
        <el-descriptions-item label="物料名称">{{ currentStock.materialName }}</el-descriptions-item>
        <el-descriptions-item label="物料类型">
          <el-tag v-if="currentStock.type === 'raw'" type="primary">原材料</el-tag>
          <el-tag v-else-if="currentStock.type === 'semi'" type="warning">半成品</el-tag>
          <el-tag v-else-if="currentStock.type === 'aux'" type="info">辅料</el-tag>
          <el-tag v-else type="success">成品</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ currentStock.spec }}</el-descriptions-item>
        <el-descriptions-item label="单位">{{ currentStock.unit }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ currentStock.warehouse }}</el-descriptions-item>
        <el-descriptions-item label="库存数量">
          <span :class="{ 'low-stock': currentStock.quantity < currentStock.safetyStock }">
            {{ currentStock.quantity }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="安全库存">{{ currentStock.safetyStock }}</el-descriptions-item>
        <el-descriptions-item label="均价">¥{{ currentStock.avgPrice?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="库存金额">
          <span style="color: #f56c6c; font-weight: bold">
            ¥{{ currentStock.totalValue?.toFixed(2) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">{{ currentStock.updateTime }}</el-descriptions-item>
      </el-descriptions>
      <div class="detail-section-title">库存变动记录</div>
      <el-table :data="currentStock.records || []" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="type" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'in'" type="success">入库</el-tag>
            <el-tag v-else type="danger">出库</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" align="right" />
        <el-table-column prop="billNo" label="单据号" width="150" />
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column prop="createTime" label="时间" width="160" />
      </el-table>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

const loading = ref(false)
const detailVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  materialNo: '',
  materialName: '',
  type: '',
  warehouse: ''
})

const currentStock = reactive({})

const mockData = ref([
  {
    id: 1,
    materialNo: 'M001',
    materialName: '主板芯片',
    type: 'raw',
    spec: 'STM32F103',
    unit: '个',
    warehouse: '原料仓',
    quantity: 8500,
    safetyStock: 2000,
    avgPrice: 15.50,
    totalValue: 131750.00,
    updateTime: '2024-01-18 10:30:00',
    records: [
      { type: 'in', quantity: 5000, billNo: 'PO-2024001', remark: '采购入库', createTime: '2024-01-15 10:00:00' },
      { type: 'out', quantity: 1500, billNo: 'WO-2024001', remark: '生产领料', createTime: '2024-01-16 09:00:00' },
      { type: 'out', quantity: 2000, billNo: 'WO-2024002', remark: '生产领料', createTime: '2024-01-17 14:00:00' }
    ]
  },
  {
    id: 2,
    materialNo: 'M002',
    materialName: '电池',
    type: 'raw',
    spec: '3.7V 200mAh',
    unit: '个',
    warehouse: '原料仓',
    quantity: 12000,
    safetyStock: 3000,
    avgPrice: 8.00,
    totalValue: 96000.00,
    updateTime: '2024-01-18 09:20:00',
    records: [
      { type: 'in', quantity: 5000, billNo: 'PO-2024003', remark: '采购入库', createTime: '2024-01-10 11:00:00' },
      { type: 'out', quantity: 3000, billNo: 'WO-2024001', remark: '生产领料', createTime: '2024-01-12 08:30:00' }
    ]
  },
  {
    id: 3,
    materialNo: 'M003',
    materialName: '塑胶外壳',
    type: 'raw',
    spec: 'ABS 白色',
    unit: '个',
    warehouse: '原料仓',
    quantity: 500,
    safetyStock: 1000,
    avgPrice: 3.50,
    totalValue: 1750.00,
    updateTime: '2024-01-18 11:00:00',
    records: [
      { type: 'in', quantity: 2000, billNo: 'PO-2024002', remark: '采购入库', createTime: '2024-01-16 15:00:00' },
      { type: 'out', quantity: 1500, billNo: 'WO-2024003', remark: '生产领料', createTime: '2024-01-17 10:00:00' }
    ]
  },
  {
    id: 4,
    materialNo: 'M004',
    materialName: '显示屏',
    type: 'raw',
    spec: '0.96寸 OLED',
    unit: '个',
    warehouse: '原料仓',
    quantity: 3200,
    safetyStock: 1500,
    avgPrice: 12.00,
    totalValue: 38400.00,
    updateTime: '2024-01-18 08:45:00',
    records: []
  },
  {
    id: 5,
    materialNo: 'M007',
    materialName: '表带',
    type: 'semi',
    spec: '硅胶 黑色',
    unit: '条',
    warehouse: '半成品仓',
    quantity: 6000,
    safetyStock: 2000,
    avgPrice: 5.50,
    totalValue: 33000.00,
    updateTime: '2024-01-17 16:30:00',
    records: []
  },
  {
    id: 6,
    materialNo: 'M006',
    materialName: '包装盒',
    type: 'aux',
    spec: '彩盒 白色',
    unit: '个',
    warehouse: '辅料仓',
    quantity: 8000,
    safetyStock: 2000,
    avgPrice: 2.00,
    totalValue: 16000.00,
    updateTime: '2024-01-17 14:00:00',
    records: []
  },
  {
    id: 7,
    materialNo: 'P001',
    materialName: '智能健康手环 Pro',
    type: 'finished',
    spec: 'SH-P001',
    unit: '台',
    warehouse: '成品仓',
    quantity: 800,
    safetyStock: 500,
    avgPrice: 120.00,
    totalValue: 96000.00,
    updateTime: '2024-01-18 15:00:00',
    records: []
  },
  {
    id: 8,
    materialNo: 'P002',
    materialName: '智能运动手环 Lite',
    type: 'finished',
    spec: 'SH-L002',
    unit: '台',
    warehouse: '成品仓',
    quantity: 200,
    safetyStock: 300,
    avgPrice: 80.00,
    totalValue: 16000.00,
    updateTime: '2024-01-18 14:30:00',
    records: []
  },
  {
    id: 9,
    materialNo: 'M008',
    materialName: 'USB充电线',
    type: 'aux',
    spec: 'Type-C 20cm',
    unit: '条',
    warehouse: '辅料仓',
    quantity: 4500,
    safetyStock: 1000,
    avgPrice: 3.00,
    totalValue: 13500.00,
    updateTime: '2024-01-17 11:20:00',
    records: []
  },
  {
    id: 10,
    materialNo: 'M005',
    materialName: '心率传感器',
    type: 'raw',
    spec: 'MAX30102',
    unit: '个',
    warehouse: '原料仓',
    quantity: 150,
    safetyStock: 500,
    avgPrice: 20.00,
    totalValue: 3000.00,
    updateTime: '2024-01-18 10:00:00',
    records: []
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
    if (searchForm.materialNo) {
      filtered = filtered.filter(item => item.materialNo.includes(searchForm.materialNo))
    }
    if (searchForm.materialName) {
      filtered = filtered.filter(item => item.materialName.includes(searchForm.materialName))
    }
    if (searchForm.type) {
      filtered = filtered.filter(item => item.type === searchForm.type)
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
  searchForm.materialNo = ''
  searchForm.materialName = ''
  searchForm.type = ''
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
  Object.assign(currentStock, row)
  detailVisible.value = true
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

.low-stock {
  color: #f56c6c;
  font-weight: bold;
}

.detail-section-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.header-stats {
  display: flex;
  gap: 10px;
}
</style>
