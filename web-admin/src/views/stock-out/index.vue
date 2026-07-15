<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="出库单号">
          <el-input v-model="searchForm.stockOutNo" placeholder="请输入出库单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="关联单号">
          <el-input v-model="searchForm.relatedNo" placeholder="请输入关联单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="出库类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 140px">
            <el-option label="销售出库" value="sale" />
            <el-option label="生产领料" value="production" />
            <el-option label="调拨出库" value="transfer" />
            <el-option label="其他出库" value="other" />
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
        <span class="table-title">出库记录列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增出库单
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="stockOutNo" label="出库单号" width="160" />
        <el-table-column prop="type" label="出库类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'sale'" type="primary">销售出库</el-tag>
            <el-tag v-else-if="row.type === 'production'" type="success">生产领料</el-tag>
            <el-tag v-else-if="row.type === 'transfer'" type="warning">调拨出库</el-tag>
            <el-tag v-else type="info">其他出库</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="relatedNo" label="关联单号" width="150" />
        <el-table-column prop="warehouse" label="出库仓库" width="110" />
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
      :title="isEdit ? '编辑出库单' : '新增出库单'"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="stockOutFormRef"
        :model="stockOutForm"
        :rules="stockOutRules"
        label-width="100px"
      >
        <el-form-item label="出库类型" prop="type">
          <el-select v-model="stockOutForm.type" placeholder="请选择出库类型" style="width: 100%">
            <el-option label="销售出库" value="sale" />
            <el-option label="生产领料" value="production" />
            <el-option label="调拨出库" value="transfer" />
            <el-option label="其他出库" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="出库仓库" prop="warehouse">
          <el-select v-model="stockOutForm.warehouse" placeholder="请选择仓库" style="width: 100%">
            <el-option label="原料仓" value="原料仓" />
            <el-option label="半成品仓" value="半成品仓" />
            <el-option label="成品仓" value="成品仓" />
            <el-option label="辅料仓" value="辅料仓" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联单号" prop="relatedNo">
          <el-input v-model="stockOutForm.relatedNo" placeholder="请输入关联单号（如工单号）" />
        </el-form-item>
        <el-form-item label="出库明细" prop="items">
          <div style="width: 100%">
            <el-table :data="stockOutForm.items" border style="width: 100%; margin-bottom: 10px">
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
          <el-input v-model="stockOutForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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

    <el-dialog v-model="detailVisible" title="出库单详情" width="750px">
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="出库单号">{{ currentStockOut.stockOutNo }}</el-descriptions-item>
        <el-descriptions-item label="出库状态">
          <el-tag :type="getStatusType(currentStockOut.status)" size="small">
            {{ getStatusText(currentStockOut.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="出库类型">
          <el-tag v-if="currentStockOut.type === 'sale'" type="primary">销售出库</el-tag>
          <el-tag v-else-if="currentStockOut.type === 'production'" type="success">生产领料</el-tag>
          <el-tag v-else-if="currentStockOut.type === 'transfer'" type="warning">调拨出库</el-tag>
          <el-tag v-else type="info">其他出库</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="出库仓库">{{ currentStockOut.warehouse }}</el-descriptions-item>
        <el-descriptions-item label="关联单号">{{ currentStockOut.relatedNo || '无' }}</el-descriptions-item>
        <el-descriptions-item label="物料种类">{{ currentStockOut.itemCount }}</el-descriptions-item>
        <el-descriptions-item label="总数量">{{ currentStockOut.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="总金额">
          <span style="color: #f56c6c; font-weight: bold; font-size: 16px">
            ¥{{ currentStockOut.totalAmount?.toFixed(2) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentStockOut.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentStockOut.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <div class="detail-section-title">出库明细</div>
      <el-table :data="currentStockOut.items || []" border stripe style="width: 100%">
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
import { getStockOutList, createStockOut, updateStockOut, deleteStockOut, approveStockOut } from '@/api/stockOut'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const stockOutFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  stockOutNo: '',
  relatedNo: '',
  type: '',
  status: ''
})

const stockOutForm = reactive({
  id: null,
  type: 'production',
  warehouse: '',
  relatedNo: '',
  items: [
    { materialId: null, materialName: '', spec: '', quantity: 1, price: 0 }
  ],
  remark: ''
})

const currentStockOut = reactive({})

const statusOptions = [
  { label: '待审核', value: 0 },
  { label: '已出库', value: 1 },
  { label: '已取消', value: 2 }
]

const stockOutRules = {
  type: [
    { required: true, message: '请选择出库类型', trigger: 'change' }
  ],
  warehouse: [
    { required: true, message: '请选择出库仓库', trigger: 'change' }
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
  { id: 11, name: '智能健康手环 Pro', spec: 'SH-P001', price: 299.00 },
  { id: 12, name: '智能运动手环 Lite', spec: 'SH-L002', price: 199.00 }
])

const tableData = ref([])

const dialogTotalAmount = computed(() => {
  return stockOutForm.items.reduce((sum, item) => sum + item.quantity * item.price, 0)
})

function getStatusType(status) {
  const types = ['warning', 'success', 'danger']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['待审核', '已出库', '已取消']
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
      outNo: searchForm.stockOutNo,
      status: searchForm.status
    }
    const res = await getStockOutList(params)
    const data = res.data || {}
    let list = (data.records || [])
    if (searchForm.stockOutNo) {
      list = list.filter(item => (item.stockOutNo || '').includes(searchForm.stockOutNo))
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
    console.warn('出库记录API调用失败，使用mock数据', error)
    const mockData = [
      { id: 1, stockOutNo: 'CK-20260701-001', type: 'production', relatedNo: 'WO-20260701-001', warehouse: '原料仓', itemCount: 4, totalQuantity: 8000, totalAmount: 99500.00, status: 1, createTime: '2026-07-01 08:00:00', remark: '工单领料-手环Pro生产', items: [{ materialName: 'STM32F103主板芯片', spec: 'STM32F103C8T6', quantity: 2000, price: 15.50 }, { materialName: '0.96寸OLED显示屏', spec: '0.96寸 128×64', quantity: 2000, price: 12.00 }, { materialName: 'MAX30102心率血氧传感器', spec: 'MAX30102 I2C', quantity: 2000, price: 20.00 }, { materialName: '聚合物锂电池', spec: '3.7V 200mAh', quantity: 2000, price: 8.00 }] },
      { id: 2, stockOutNo: 'CK-20260701-002', type: 'sale', relatedNo: 'SO-20260701-001', warehouse: '成品仓', itemCount: 2, totalQuantity: 3000, totalAmount: 322000.00, status: 1, createTime: '2026-07-01 11:00:00', remark: '深圳华康采购订单', items: [{ materialName: '智能健康手环 Pro', spec: 'SH-P001', quantity: 2000, price: 120.00 }, { materialName: '智能运动手环 Lite', spec: 'SH-L002', quantity: 1000, price: 80.00 }] },
      { id: 3, stockOutNo: 'CK-20260702-001', type: 'production', relatedNo: 'WO-20260702-001', warehouse: '原料仓', itemCount: 3, totalQuantity: 6000, totalAmount: 53000.00, status: 1, createTime: '2026-07-02 09:00:00', remark: '工单领料-手环Lite生产', items: [{ materialName: 'ABS塑胶外壳', spec: 'ABS 白色', quantity: 2000, price: 3.50 }, { materialName: '硅胶表带', spec: '医用硅胶 黑色', quantity: 2000, price: 5.50 }, { materialName: '聚合物锂电池', spec: '3.7V 200mAh', quantity: 2000, price: 8.00 }] },
      { id: 4, stockOutNo: 'CK-20260703-001', type: 'sale', relatedNo: 'SO-20260703-001', warehouse: '成品仓', itemCount: 1, totalQuantity: 1000, totalAmount: 135000.00, status: 1, createTime: '2026-07-03 14:30:00', remark: '成都益康养老手环订单', items: [{ materialName: '老人健康监护手环', spec: 'SH-E003', quantity: 1000, price: 135.00 }] },
      { id: 5, stockOutNo: 'CK-20260704-001', type: 'production', relatedNo: 'WO-20260704-001', warehouse: '半成品仓', itemCount: 1, totalQuantity: 1500, totalAmount: 87000.00, status: 1, createTime: '2026-07-04 10:00:00', remark: 'PCBA领用组装', items: [{ materialName: '手环PCBA半成品', spec: 'SH-PCBA-001', quantity: 1500, price: 58.00 }] },
      { id: 6, stockOutNo: 'CK-20260705-001', type: 'sale', relatedNo: 'SO-20260705-001', warehouse: '成品仓', itemCount: 2, totalQuantity: 2000, totalAmount: 520000.00, status: 1, createTime: '2026-07-05 16:00:00', remark: '北京万达运动手环订单', items: [{ materialName: '智能健康手环 Pro', spec: 'SH-P001', quantity: 2000, price: 120.00 }, { materialName: '专业运动手表 X1', spec: 'SW-X001', quantity: 500, price: 220.00 }] },
      { id: 7, stockOutNo: 'CK-20260707-001', type: 'production', relatedNo: 'WO-20260707-001', warehouse: '原料仓', itemCount: 3, totalQuantity: 4500, totalAmount: 71500.00, status: 1, createTime: '2026-07-07 08:30:00', remark: '老人手环生产领料', items: [{ materialName: 'nRF52832蓝牙芯片', spec: 'nRF52832 QFN48', quantity: 1500, price: 18.00 }, { materialName: 'MAX30102心率血氧传感器', spec: 'MAX30102 I2C', quantity: 1500, price: 20.00 }, { materialName: '聚合物锂电池', spec: '3.7V 200mAh', quantity: 1500, price: 8.00 }] },
      { id: 8, stockOutNo: 'CK-20260708-001', type: 'sale', relatedNo: 'SO-20260708-001', warehouse: '成品仓', itemCount: 1, totalQuantity: 500, totalAmount: 110000.00, status: 1, createTime: '2026-07-08 11:00:00', remark: '杭州云健康Pro手环订单', items: [{ materialName: '专业运动手表 X1', spec: 'SW-X001', quantity: 500, price: 220.00 }] },
      { id: 9, stockOutNo: 'CK-20260709-001', type: 'transfer', relatedNo: 'TR-20260709-001', warehouse: '原料仓', itemCount: 2, totalQuantity: 3000, totalAmount: 36000.00, status: 1, createTime: '2026-07-09 09:00:00', remark: '调拨至分厂原料仓', items: [{ materialName: 'STM32F103主板芯片', spec: 'STM32F103C8T6', quantity: 1500, price: 15.50 }, { materialName: '0.96寸OLED显示屏', spec: '0.96寸 128×64', quantity: 1500, price: 12.00 }] },
      { id: 10, stockOutNo: 'CK-20260710-001', type: 'sale', relatedNo: 'SO-20260710-001', warehouse: '成品仓', itemCount: 2, totalQuantity: 1500, totalAmount: 335000.00, status: 1, createTime: '2026-07-10 14:00:00', remark: '南京智慧校园订单', items: [{ materialName: '儿童安全定位手环', spec: 'SH-K004', quantity: 1000, price: 105.00 }, { materialName: '智能运动手环 Lite', spec: 'SH-L002', quantity: 500, price: 80.00 }] },
      { id: 11, stockOutNo: 'CK-20260711-001', type: 'production', relatedNo: 'WO-20260711-001', warehouse: '原料仓', itemCount: 2, totalQuantity: 4000, totalAmount: 44000.00, status: 0, createTime: '2026-07-11 10:30:00', remark: '运动手表领料待审', items: [{ materialName: 'ABS塑胶外壳', spec: 'ABS 白色', quantity: 2000, price: 3.50 }, { materialName: '硅胶表带', spec: '医用硅胶 黑色', quantity: 2000, price: 5.50 }] },
      { id: 12, stockOutNo: 'CK-20260712-001', type: 'sale', relatedNo: 'SO-20260712-001', warehouse: '成品仓', itemCount: 1, totalQuantity: 800, totalAmount: 176000.00, status: 1, createTime: '2026-07-12 15:00:00', remark: '广州天河体育用品订单', items: [{ materialName: '智能健康手环 Pro', spec: 'SH-P001', quantity: 800, price: 120.00 }] },
      { id: 13, stockOutNo: 'CK-20260713-001', type: 'sale', relatedNo: 'SO-20260713-001', warehouse: '成品仓', itemCount: 2, totalQuantity: 2000, totalAmount: 270000.00, status: 0, createTime: '2026-07-13 09:30:00', remark: '西安丝路健康订单待审', items: [{ materialName: '智能健康手环 Pro', spec: 'SH-P001', quantity: 1500, price: 120.00 }, { materialName: '智能运动手环 Lite', spec: 'SH-L002', quantity: 500, price: 80.00 }] },
      { id: 14, stockOutNo: 'CK-20260714-001', type: 'other', relatedNo: '', warehouse: '辅料仓', itemCount: 2, totalQuantity: 500, totalAmount: 2500.00, status: 2, createTime: '2026-07-14 10:00:00', remark: '样品出库已取消', items: [{ materialName: '彩盒包装盒', spec: '彩盒 白色', quantity: 300, price: 2.00 }, { materialName: 'Type-C充电线', spec: 'Type-C 20cm', quantity: 200, price: 3.00 }] },
      { id: 15, stockOutNo: 'CK-20260714-002', type: 'production', relatedNo: 'WO-20260714-001', warehouse: '辅料仓', itemCount: 2, totalQuantity: 3000, totalAmount: 7000.00, status: 0, createTime: '2026-07-14 16:00:00', remark: '包装工位领料待审', items: [{ materialName: '彩盒包装盒', spec: '彩盒 白色', quantity: 2000, price: 2.00 }, { materialName: 'Type-C充电线', spec: 'Type-C 20cm', quantity: 1000, price: 3.00 }] }
    ]
    let list = mockData
    if (searchForm.stockOutNo) {
      list = list.filter(item => (item.stockOutNo || '').includes(searchForm.stockOutNo))
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
  searchForm.stockOutNo = ''
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
  stockOutForm.id = row.id
  stockOutForm.type = row.type
  stockOutForm.warehouse = row.warehouse
  stockOutForm.relatedNo = row.relatedNo
  stockOutForm.items = row.items.map(item => ({ ...item }))
  stockOutForm.remark = row.remark
  dialogVisible.value = true
}

function handleView(row) {
  Object.assign(currentStockOut, row)
  detailVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除出库单「${row.stockOutNo}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteStockOut(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

function handleApprove(row) {
  ElMessageBox.confirm(`确定要审核通过出库单「${row.stockOutNo}」吗？`, '审核确认', {
    confirmButtonText: '通过',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    await approveStockOut(row.id)
    ElMessage.success('审核通过，已出库')
    fetchData()
  }).catch(() => {})
}

function handleMaterialChange(index) {
  const material = materialList.value.find(m => m.id === stockOutForm.items[index].materialId)
  if (material) {
    stockOutForm.items[index].materialName = material.name
    stockOutForm.items[index].spec = material.spec
    stockOutForm.items[index].price = material.price
    calculateTotal()
  }
}

function addItem() {
  stockOutForm.items.push({
    materialId: null,
    materialName: '',
    spec: '',
    quantity: 1,
    price: 0
  })
}

function removeItem(index) {
  if (stockOutForm.items.length <= 1) {
    ElMessage.warning('至少保留一条明细')
    return
  }
  stockOutForm.items.splice(index, 1)
  calculateTotal()
}

function calculateTotal() {
}

function resetForm() {
  stockOutForm.id = null
  stockOutForm.type = 'production'
  stockOutForm.warehouse = ''
  stockOutForm.relatedNo = ''
  stockOutForm.items = [
    { materialId: null, materialName: '', spec: '', quantity: 1, price: 0 }
  ]
  stockOutForm.remark = ''
  if (stockOutFormRef.value) {
    stockOutFormRef.value.resetFields()
  }
}

async function handleSubmit() {
  if (!stockOutFormRef.value) return
  
  if (stockOutForm.items.length === 0) {
    ElMessage.warning('请至少添加一条出库明细')
    return
  }
  
  const hasInvalidItem = stockOutForm.items.some(item => !item.materialId || item.quantity <= 0)
  if (hasInvalidItem) {
    ElMessage.warning('请完善所有出库明细信息')
    return
  }
  
  try {
    await stockOutFormRef.value.validate()
    submitLoading.value = true
    
    const submitData = {
      id: stockOutForm.id,
      type: stockOutForm.type,
      warehouse: stockOutForm.warehouse,
      relatedNo: stockOutForm.relatedNo,
      items: stockOutForm.items.map(item => ({
        materialId: item.materialId,
        quantity: item.quantity,
        price: item.price
      })),
      remark: stockOutForm.remark
    }
    
    if (isEdit.value) {
      await updateStockOut(submitData)
      ElMessage.success('编辑成功')
    } else {
      await createStockOut(submitData)
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
