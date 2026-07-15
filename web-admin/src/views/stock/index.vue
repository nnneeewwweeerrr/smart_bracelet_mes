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
import { getStockList } from '@/api/stock'

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
      warehouseId: searchForm.warehouse,
      materialId: searchForm.materialNo
    }
    const res = await getStockList(params)
    const data = res.data || {}
    let list = (data.records || [])
    // 前端补充过滤（后端参数未覆盖的字段）
    if (searchForm.materialNo) {
      list = list.filter(item => (item.materialNo || '').includes(searchForm.materialNo))
    }
    if (searchForm.materialName) {
      list = list.filter(item => (item.materialName || '').includes(searchForm.materialName))
    }
    if (searchForm.type) {
      list = list.filter(item => item.type === searchForm.type)
    }
    if (searchForm.warehouse) {
      list = list.filter(item => item.warehouse === searchForm.warehouse)
    }
    tableData.value = list
    total.value = data.total || 0
  } catch (error) {
    console.warn('库存列表API调用失败，使用mock数据', error)
    const mockData = [
      { materialNo: 'MAT-R001', materialName: 'STM32F103主板芯片', type: 'raw', spec: 'STM32F103C8T6', unit: '个', warehouse: '原料仓', quantity: 12500, safetyStock: 5000, avgPrice: 15.50, totalValue: 193750.00, updateTime: '2026-07-14 16:30:00' },
      { materialNo: 'MAT-R002', materialName: '聚合物锂电池', type: 'raw', spec: '3.7V 200mAh', unit: '个', warehouse: '原料仓', quantity: 8600, safetyStock: 3000, avgPrice: 8.00, totalValue: 68800.00, updateTime: '2026-07-14 15:20:00' },
      { materialNo: 'MAT-R003', materialName: 'ABS塑胶外壳', type: 'raw', spec: 'ABS 白色 注塑', unit: '个', warehouse: '原料仓', quantity: 15200, safetyStock: 8000, avgPrice: 3.50, totalValue: 53200.00, updateTime: '2026-07-14 14:10:00' },
      { materialNo: 'MAT-R004', materialName: '0.96寸OLED显示屏', type: 'raw', spec: '0.96寸 128×64 SPI', unit: '个', warehouse: '原料仓', quantity: 9800, safetyStock: 4000, avgPrice: 12.00, totalValue: 117600.00, updateTime: '2026-07-14 13:00:00' },
      { materialNo: 'MAT-R005', materialName: 'MAX30102心率血氧传感器', type: 'raw', spec: 'MAX30102 I2C', unit: '个', warehouse: '原料仓', quantity: 2800, safetyStock: 3000, avgPrice: 20.00, totalValue: 56000.00, updateTime: '2026-07-14 11:45:00' },
      { materialNo: 'MAT-R006', materialName: '硅胶表带', type: 'raw', spec: '医用硅胶 黑色 M码', unit: '条', warehouse: '原料仓', quantity: 20000, safetyStock: 10000, avgPrice: 5.50, totalValue: 110000.00, updateTime: '2026-07-13 16:30:00' },
      { materialNo: 'MAT-R007', materialName: 'nRF52832蓝牙芯片', type: 'raw', spec: 'nRF52832 QFN48', unit: '个', warehouse: '原料仓', quantity: 6500, safetyStock: 3000, avgPrice: 18.00, totalValue: 117000.00, updateTime: '2026-07-13 10:00:00' },
      { materialNo: 'MAT-A001', materialName: '彩盒包装盒', type: 'aux', spec: '彩盒 白色 120×90×60mm', unit: '个', warehouse: '辅料仓', quantity: 25000, safetyStock: 10000, avgPrice: 2.00, totalValue: 50000.00, updateTime: '2026-07-13 09:20:00' },
      { materialNo: 'MAT-A002', materialName: 'Type-C充电线', type: 'aux', spec: 'Type-C 20cm 白色', unit: '条', warehouse: '辅料仓', quantity: 18000, safetyStock: 8000, avgPrice: 3.00, totalValue: 54000.00, updateTime: '2026-07-12 17:00:00' },
      { materialNo: 'MAT-S001', materialName: '手环PCBA半成品', type: 'semi', spec: 'SH-PCBA-001', unit: '片', warehouse: '半成品仓', quantity: 3200, safetyStock: 1500, avgPrice: 58.00, totalValue: 185600.00, updateTime: '2026-07-12 15:30:00' },
      { materialNo: 'MAT-S002', materialName: '手环组装半成品', type: 'semi', spec: 'SH-ASM-002', unit: '台', warehouse: '半成品仓', quantity: 1800, safetyStock: 1000, avgPrice: 85.00, totalValue: 153000.00, updateTime: '2026-07-12 14:00:00' },
      { materialNo: 'PRD-001', materialName: '智能健康手环 Pro', type: 'finished', spec: 'SH-P001', unit: '台', warehouse: '成品仓', quantity: 5600, safetyStock: 2000, avgPrice: 120.00, totalValue: 672000.00, updateTime: '2026-07-14 17:00:00' },
      { materialNo: 'PRD-002', materialName: '智能运动手环 Lite', type: 'finished', spec: 'SH-L002', unit: '台', warehouse: '成品仓', quantity: 8200, safetyStock: 3000, avgPrice: 80.00, totalValue: 656000.00, updateTime: '2026-07-14 16:45:00' },
      { materialNo: 'PRD-003', materialName: '老人健康监护手环', type: 'finished', spec: 'SH-E003', unit: '台', warehouse: '成品仓', quantity: 1500, safetyStock: 1000, avgPrice: 135.00, totalValue: 202500.00, updateTime: '2026-07-14 15:30:00' },
      { materialNo: 'PRD-004', materialName: '专业运动手表 X1', type: 'finished', spec: 'SW-X001', unit: '台', warehouse: '成品仓', quantity: 900, safetyStock: 500, avgPrice: 220.00, totalValue: 198000.00, updateTime: '2026-07-14 14:20:00' }
    ]
    let list = mockData
    if (searchForm.materialNo) {
      list = list.filter(item => (item.materialNo || '').includes(searchForm.materialNo))
    }
    if (searchForm.materialName) {
      list = list.filter(item => (item.materialName || '').includes(searchForm.materialName))
    }
    if (searchForm.type) {
      list = list.filter(item => item.type === searchForm.type)
    }
    if (searchForm.warehouse) {
      list = list.filter(item => item.warehouse === searchForm.warehouse)
    }
    tableData.value = list
    total.value = mockData.length
  } finally {
    loading.value = false
  }
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
