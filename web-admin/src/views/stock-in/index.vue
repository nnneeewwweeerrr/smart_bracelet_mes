<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="入库单号">
          <el-input v-model="searchForm.stockInNo" placeholder="请输入入库单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="关联单号">
          <el-input v-model="searchForm.relatedNo" placeholder="请输入关联单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="入库类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 140px">
            <el-option label="采购入库" value="purchase" />
            <el-option label="生产入库" value="production" />
            <el-option label="调拨入库" value="transfer" />
            <el-option label="其他入库" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
        <span class="table-title">入库记录列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增入库单
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="stockInNo" label="入库单号" width="160" />
        <el-table-column prop="type" label="入库类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'purchase'" type="primary">采购入库</el-tag>
            <el-tag v-else-if="row.type === 'production'" type="success">生产入库</el-tag>
            <el-tag v-else-if="row.type === 'transfer'" type="warning">调拨入库</el-tag>
            <el-tag v-else type="info">其他入库</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="relatedNo" label="关联单号" width="150" />
        <el-table-column prop="warehouse" label="入库仓库" width="110" />
        <el-table-column prop="itemCount" label="物料种类" width="90" align="center" />
        <el-table-column prop="totalQuantity" label="总数量" width="100" align="center" />
        <el-table-column prop="totalAmount" label="总金额（元）" width="130" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right" align="center">
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
              @click="handleApprove(row)"
              :disabled="row.status !== 0"
            >
              审核
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
              :disabled="row.status !== 0"
            >
              删除
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
      :title="isEdit ? '编辑入库单' : '新增入库单'"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="stockInFormRef"
        :model="stockInForm"
        :rules="stockInRules"
        label-width="100px"
      >
        <el-form-item label="入库类型" prop="type">
          <el-select v-model="stockInForm.type" placeholder="请选择入库类型" style="width: 100%">
            <el-option label="采购入库" value="purchase" />
            <el-option label="生产入库" value="production" />
            <el-option label="调拨入库" value="transfer" />
            <el-option label="其他入库" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="入库仓库" prop="warehouse">
          <el-select v-model="stockInForm.warehouse" placeholder="请选择仓库" style="width: 100%">
            <el-option label="原料仓" value="原料仓" />
            <el-option label="半成品仓" value="半成品仓" />
            <el-option label="成品仓" value="成品仓" />
            <el-option label="辅料仓" value="辅料仓" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联单号" prop="relatedNo">
          <el-input v-model="stockInForm.relatedNo" placeholder="请输入关联单号（如采购单号）" />
        </el-form-item>
        <el-form-item label="入库明细" prop="items">
          <div style="width: 100%">
            <el-table :data="stockInForm.items" border style="width: 100%; margin-bottom: 10px">
              <el-table-column label="物料" min-width="200">
                <template #default="{ row, $index }">
                  <el-select v-model="row.materialId" placeholder="请选择物料" style="width: 100%" @change="handleMaterialChange($index)">
                    <el-option
                      v-for="item in materialList"
                      :key="item.id"
                      :label="item.name"
                      :value="item.id"
                    />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="数量" width="120">
                <template #default="{ row }">
                  <el-input-number v-model="row.quantity" :min="1" :max="99999" style="width: 100%" @change="calculateTotal" />
                </template>
              </el-table-column>
              <el-table-column label="单价（元）" width="140">
                <template #default="{ row }">
                  <el-input-number v-model="row.price" :min="0" :precision="2" :step="0.01" style="width: 100%" @change="calculateTotal" />
                </template>
              </el-table-column>
              <el-table-column label="小计（元）" width="120" align="right">
                <template #default="{ row }">
                  ¥{{ (row.quantity * row.price).toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center">
                <template #default="{ $index }">
                  <el-button type="danger" link @click="removeItem($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button type="primary" plain @click="addItem">
              <el-icon><Plus /></el-icon>
              添加物料
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="stockInForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div class="dialog-footer-total">
        <span>合计金额：<span class="total-amount">¥{{ dialogTotalAmount.toFixed(2) }}</span></span>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="入库单详情" width="750px">
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="入库单号">{{ currentStockIn.stockInNo }}</el-descriptions-item>
        <el-descriptions-item label="入库状态">
          <el-tag :type="getStatusType(currentStockIn.status)" size="small">
            {{ getStatusText(currentStockIn.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="入库类型">
          <el-tag v-if="currentStockIn.type === 'purchase'" type="primary">采购入库</el-tag>
          <el-tag v-else-if="currentStockIn.type === 'production'" type="success">生产入库</el-tag>
          <el-tag v-else-if="currentStockIn.type === 'transfer'" type="warning">调拨入库</el-tag>
          <el-tag v-else type="info">其他入库</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="入库仓库">{{ currentStockIn.warehouse }}</el-descriptions-item>
        <el-descriptions-item label="关联单号">{{ currentStockIn.relatedNo || '无' }}</el-descriptions-item>
        <el-descriptions-item label="物料种类">{{ currentStockIn.itemCount }}</el-descriptions-item>
        <el-descriptions-item label="总数量">{{ currentStockIn.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="总金额">
          <span style="color: #f56c6c; font-weight: bold; font-size: 16px">
            ¥{{ currentStockIn.totalAmount?.toFixed(2) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentStockIn.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentStockIn.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <div class="detail-section-title">入库明细</div>
      <el-table :data="currentStockIn.items || []" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialName" label="物料名称" min-width="160" />
        <el-table-column prop="spec" label="规格型号" width="140" />
        <el-table-column prop="quantity" label="数量" width="100" align="center" />
        <el-table-column prop="price" label="单价（元）" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="小计（元）" width="120" align="right">
          <template #default="{ row }">
            ¥{{ (row.quantity * row.price).toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStockInList, createStockIn, updateStockIn, deleteStockIn, approveStockIn } from '@/api/stockIn'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const stockInFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  stockInNo: '',
  relatedNo: '',
  type: '',
  status: ''
})

const stockInForm = reactive({
  id: null,
  type: 'purchase',
  warehouse: '',
  relatedNo: '',
  items: [
    { materialId: null, materialName: '', spec: '', quantity: 1, price: 0 }
  ],
  remark: ''
})

const currentStockIn = reactive({})

const statusOptions = [
  { label: '待审核', value: 0 },
  { label: '已入库', value: 1 },
  { label: '已取消', value: 2 }
]

const stockInRules = {
  type: [
    { required: true, message: '请选择入库类型', trigger: 'change' }
  ],
  warehouse: [
    { required: true, message: '请选择入库仓库', trigger: 'change' }
  ]
}

const materialList = ref([
  { id: 1, name: '主板芯片', spec: 'STM32F103', price: 15.50 },
  { id: 2, name: '电池', spec: '3.7V 200mAh', price: 8.00 },
  { id: 3, name: '塑胶外壳', spec: 'ABS 白色', price: 3.50 },
  { id: 4, name: '显示屏', spec: '0.96寸 OLED', price: 12.00 },
  { id: 5, name: '心率传感器', spec: 'MAX30102', price: 20.00 },
  { id: 6, name: '包装盒', spec: '彩盒 白色', price: 2.00 },
  { id: 7, name: '表带', spec: '硅胶 黑色', price: 5.50 },
  { id: 8, name: 'USB充电线', spec: 'Type-C 20cm', price: 3.00 },
  { id: 11, name: '智能健康手环 Pro', spec: 'SH-P001', price: 120.00 },
  { id: 12, name: '智能运动手环 Lite', spec: 'SH-L002', price: 80.00 }
])

const tableData = ref([])

const dialogTotalAmount = computed(() => {
  return stockInForm.items.reduce((sum, item) => sum + item.quantity * item.price, 0)
})

function getStatusType(status) {
  const types = ['warning', 'success', 'danger']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['待审核', '已入库', '已取消']
  return texts[status] || '未知'
}

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      inNo: searchForm.stockInNo,
      status: searchForm.status
    }
    const res = await getStockInList(params)
    const data = res.data || {}
    let list = (data.records || [])
    if (searchForm.stockInNo) {
      list = list.filter(item => (item.stockInNo || '').includes(searchForm.stockInNo))
    }
    if (searchForm.type) {
      list = list.filter(item => item.type === searchForm.type)
    }
    if (searchForm.status !== '') {
      list = list.filter(item => item.status === searchForm.status)
    }
    tableData.value = list
    total.value = data.total || 0
  } catch (error) {
    console.warn('入库记录API调用失败，使用mock数据', error)
    const mockData = [
      { id: 1, stockInNo: 'RK-20260701-001', type: 'purchase', relatedNo: 'PO-20260701-001', warehouse: '原料仓', itemCount: 3, totalQuantity: 15000, totalAmount: 162500.00, status: 1, createTime: '2026-07-01 09:30:00', remark: '7月第一批芯片采购', items: [{ materialName: 'STM32F103主板芯片', spec: 'STM32F103C8T6', quantity: 5000, price: 15.50 }, { materialName: 'nRF52832蓝牙芯片', spec: 'nRF52832 QFN48', quantity: 5000, price: 18.00 }, { materialName: '0.96寸OLED显示屏', spec: '0.96寸 128×64', quantity: 5000, price: 12.00 }] },
      { id: 2, stockInNo: 'RK-20260701-002', type: 'purchase', relatedNo: 'PO-20260701-002', warehouse: '原料仓', itemCount: 2, totalQuantity: 10000, totalAmount: 71000.00, status: 1, createTime: '2026-07-01 14:00:00', remark: '电池及外壳采购', items: [{ materialName: '聚合物锂电池', spec: '3.7V 200mAh', quantity: 5000, price: 8.00 }, { materialName: 'ABS塑胶外壳', spec: 'ABS 白色', quantity: 5000, price: 3.50 }] },
      { id: 3, stockInNo: 'RK-20260702-001', type: 'production', relatedNo: 'WO-20260702-001', warehouse: '半成品仓', itemCount: 1, totalQuantity: 3000, totalAmount: 174000.00, status: 1, createTime: '2026-07-02 16:30:00', remark: 'PCBA贴片完成入库', items: [{ materialName: '手环PCBA半成品', spec: 'SH-PCBA-001', quantity: 3000, price: 58.00 }] },
      { id: 4, stockInNo: 'RK-20260703-001', type: 'purchase', relatedNo: 'PO-20260703-001', warehouse: '原料仓', itemCount: 2, totalQuantity: 8000, totalAmount: 160000.00, status: 1, createTime: '2026-07-03 10:00:00', remark: '心率传感器采购', items: [{ materialName: 'MAX30102心率血氧传感器', spec: 'MAX30102 I2C', quantity: 3000, price: 20.00 }, { materialName: 'nRF52832蓝牙芯片', spec: 'nRF52832 QFN48', quantity: 5000, price: 18.00 }] },
      { id: 5, stockInNo: 'RK-20260704-001', type: 'purchase', relatedNo: 'PO-20260704-001', warehouse: '辅料仓', itemCount: 2, totalQuantity: 15000, totalAmount: 67500.00, status: 1, createTime: '2026-07-04 11:15:00', remark: '包装及线材采购', items: [{ materialName: '彩盒包装盒', spec: '彩盒 白色', quantity: 10000, price: 2.00 }, { materialName: 'Type-C充电线', spec: 'Type-C 20cm', quantity: 5000, price: 3.00 }] },
      { id: 6, stockInNo: 'RK-20260705-001', type: 'production', relatedNo: 'WO-20260705-001', warehouse: '半成品仓', itemCount: 1, totalQuantity: 2000, totalAmount: 170000.00, status: 1, createTime: '2026-07-05 15:00:00', remark: '组装半成品入库', items: [{ materialName: '手环组装半成品', spec: 'SH-ASM-002', quantity: 2000, price: 85.00 }] },
      { id: 7, stockInNo: 'RK-20260707-001', type: 'production', relatedNo: 'WO-20260707-001', warehouse: '成品仓', itemCount: 2, totalQuantity: 5000, totalAmount: 520000.00, status: 1, createTime: '2026-07-07 09:30:00', remark: '手环成品入库', items: [{ materialName: '智能健康手环 Pro', spec: 'SH-P001', quantity: 3000, price: 120.00 }, { materialName: '智能运动手环 Lite', spec: 'SH-L002', quantity: 2000, price: 80.00 }] },
      { id: 8, stockInNo: 'RK-20260708-001', type: 'purchase', relatedNo: 'PO-20260708-001', warehouse: '原料仓', itemCount: 1, totalQuantity: 10000, totalAmount: 55000.00, status: 1, createTime: '2026-07-08 14:00:00', remark: '硅胶表带采购', items: [{ materialName: '硅胶表带', spec: '医用硅胶 黑色 M码', quantity: 10000, price: 5.50 }] },
      { id: 9, stockInNo: 'RK-20260710-001', type: 'transfer', relatedNo: 'TR-20260710-001', warehouse: '原料仓', itemCount: 1, totalQuantity: 1000, totalAmount: 15500.00, status: 0, createTime: '2026-07-10 10:00:00', remark: '从备用仓调拨芯片', items: [{ materialName: 'STM32F103主板芯片', spec: 'STM32F103C8T6', quantity: 1000, price: 15.50 }] },
      { id: 10, stockInNo: 'RK-20260711-001', type: 'production', relatedNo: 'WO-20260711-001', warehouse: '成品仓', itemCount: 1, totalQuantity: 1500, totalAmount: 202500.00, status: 1, createTime: '2026-07-11 16:00:00', remark: '老人手环成品入库', items: [{ materialName: '老人健康监护手环', spec: 'SH-E003', quantity: 1500, price: 135.00 }] },
      { id: 11, stockInNo: 'RK-20260712-001', type: 'purchase', relatedNo: 'PO-20260712-001', warehouse: '辅料仓', itemCount: 2, totalQuantity: 12000, totalAmount: 30000.00, status: 0, createTime: '2026-07-12 09:30:00', remark: '包装辅料补充采购', items: [{ materialName: '彩盒包装盒', spec: '彩盒 白色', quantity: 8000, price: 2.00 }, { materialName: 'Type-C充电线', spec: 'Type-C 20cm', quantity: 4000, price: 3.00 }] },
      { id: 12, stockInNo: 'RK-20260713-001', type: 'other', relatedNo: '', warehouse: '成品仓', itemCount: 1, totalQuantity: 200, totalAmount: 24000.00, status: 0, createTime: '2026-07-13 11:00:00', remark: '客户退换货入库', items: [{ materialName: '智能健康手环 Pro', spec: 'SH-P001', quantity: 200, price: 120.00 }] },
      { id: 13, stockInNo: 'RK-20260714-001', type: 'purchase', relatedNo: 'PO-20260714-001', warehouse: '原料仓', itemCount: 2, totalQuantity: 6000, totalAmount: 93000.00, status: 1, createTime: '2026-07-14 08:30:00', remark: '显示屏及传感器补货', items: [{ materialName: '0.96寸OLED显示屏', spec: '0.96寸 128×64', quantity: 3000, price: 12.00 }, { materialName: 'MAX30102心率血氧传感器', spec: 'MAX30102 I2C', quantity: 3000, price: 20.00 }] },
      { id: 14, stockInNo: 'RK-20260714-002', type: 'production', relatedNo: 'WO-20260714-001', warehouse: '成品仓', itemCount: 1, totalQuantity: 3000, totalAmount: 240000.00, status: 0, createTime: '2026-07-14 17:00:00', remark: '运动手环成品入库待审', items: [{ materialName: '智能运动手环 Lite', spec: 'SH-L002', quantity: 3000, price: 80.00 }] },
      { id: 15, stockInNo: 'RK-20260714-003', type: 'production', relatedNo: 'WO-20260714-002', warehouse: '成品仓', itemCount: 1, totalQuantity: 900, totalAmount: 198000.00, status: 0, createTime: '2026-07-14 17:30:00', remark: '运动手表成品入库', items: [{ materialName: '专业运动手表 X1', spec: 'SW-X001', quantity: 900, price: 220.00 }] }
    ]
    let list = mockData
    if (searchForm.stockInNo) {
      list = list.filter(item => (item.stockInNo || '').includes(searchForm.stockInNo))
    }
    if (searchForm.type) {
      list = list.filter(item => item.type === searchForm.type)
    }
    if (searchForm.status !== '') {
      list = list.filter(item => item.status === searchForm.status)
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
  searchForm.stockInNo = ''
  searchForm.relatedNo = ''
  searchForm.type = ''
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
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  stockInForm.id = row.id
  stockInForm.type = row.type
  stockInForm.warehouse = row.warehouse
  stockInForm.relatedNo = row.relatedNo
  stockInForm.items = row.items.map(item => ({ ...item }))
  stockInForm.remark = row.remark
  dialogVisible.value = true
}

function handleView(row) {
  Object.assign(currentStockIn, row)
  detailVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除入库单「${row.stockInNo}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteStockIn(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

function handleApprove(row) {
  ElMessageBox.confirm(`确定要审核通过入库单「${row.stockInNo}」吗？`, '审核确认', {
    confirmButtonText: '通过',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    await approveStockIn(row.id)
    ElMessage.success('审核通过，已入库')
    fetchData()
  }).catch(() => {})
}

function handleMaterialChange(index) {
  const material = materialList.value.find(m => m.id === stockInForm.items[index].materialId)
  if (material) {
    stockInForm.items[index].materialName = material.name
    stockInForm.items[index].spec = material.spec
    stockInForm.items[index].price = material.price
    calculateTotal()
  }
}

function addItem() {
  stockInForm.items.push({
    materialId: null,
    materialName: '',
    spec: '',
    quantity: 1,
    price: 0
  })
}

function removeItem(index) {
  if (stockInForm.items.length <= 1) {
    ElMessage.warning('至少保留一条明细')
    return
  }
  stockInForm.items.splice(index, 1)
  calculateTotal()
}

function calculateTotal() {
}

function resetForm() {
  stockInForm.id = null
  stockInForm.type = 'purchase'
  stockInForm.warehouse = ''
  stockInForm.relatedNo = ''
  stockInForm.items = [
    { materialId: null, materialName: '', spec: '', quantity: 1, price: 0 }
  ]
  stockInForm.remark = ''
  if (stockInFormRef.value) {
    stockInFormRef.value.resetFields()
  }
}

async function handleSubmit() {
  if (!stockInFormRef.value) return
  
  if (stockInForm.items.length === 0) {
    ElMessage.warning('请至少添加一条入库明细')
    return
  }
  
  const hasInvalidItem = stockInForm.items.some(item => !item.materialId || item.quantity <= 0)
  if (hasInvalidItem) {
    ElMessage.warning('请完善所有入库明细信息')
    return
  }
  
  try {
    await stockInFormRef.value.validate()
    submitLoading.value = true
    
    const submitData = {
      id: stockInForm.id,
      type: stockInForm.type,
      warehouse: stockInForm.warehouse,
      relatedNo: stockInForm.relatedNo,
      items: stockInForm.items.map(item => ({
        materialId: item.materialId,
        quantity: item.quantity,
        price: item.price
      })),
      remark: stockInForm.remark
    }
    
    if (isEdit.value) {
      await updateStockIn(submitData)
      ElMessage.success('编辑成功')
    } else {
      await createStockIn(submitData)
      ElMessage.success('新增成功')
    }
    
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    // request拦截器已处理错误提示
  } finally {
    submitLoading.value = false
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

.dialog-footer-total {
  padding: 10px 20px;
  background-color: #f5f7fa;
  border-top: 1px solid #e4e7ed;
  text-align: right;
  font-size: 14px;
}

.total-amount {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}

.detail-section-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}
</style>
