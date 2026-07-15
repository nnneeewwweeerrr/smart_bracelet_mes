<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="工单号">
          <el-input v-model="searchForm.workOrderNo" placeholder="请输入工单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="关联订单">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入产品名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="工单状态">
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
        <span class="table-title">生产工单列表</span>
        <div class="header-btns">
          <el-button type="primary" plain @click="handleAskAi">
            <el-icon><ChatDotRound /></el-icon>
            问AI
          </el-button>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增工单
          </el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="workOrderNo" label="工单号" width="160" />
        <el-table-column prop="orderNo" label="关联订单" width="150" />
        <el-table-column prop="productName" label="产品名称" min-width="180" />
        <el-table-column prop="spec" label="规格型号" width="140" />
        <el-table-column prop="quantity" label="计划数量" width="100" align="center" />
        <el-table-column prop="finishedQuantity" label="已完成数量" width="110" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="Math.round(row.finishedQuantity / row.quantity * 100)"
              :stroke-width="18"
              :text-inside="true"
              size="small"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="planStartDate" label="计划开始" width="110" />
        <el-table-column prop="planEndDate" label="计划完成" width="110" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="340" fixed="right" align="center">
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
              @click="handleStart(row)"
              :disabled="row.status !== 0"
            >
              开始生产
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
      :title="isEdit ? '编辑工单' : '新增工单'"
      width="650px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="workOrderFormRef"
        :model="workOrderForm"
        :rules="workOrderRules"
        label-width="100px"
      >
        <el-form-item label="关联订单" prop="orderId">
          <el-select v-model="workOrderForm.orderId" placeholder="请选择订单" style="width: 100%" @change="handleOrderChange">
            <el-option
              v-for="item in orderList"
              :key="item.id"
              :label="item.orderNo"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="产品名称" prop="productId">
          <el-select v-model="workOrderForm.productId" placeholder="请选择产品" style="width: 100%" @change="handleProductChange">
            <el-option
              v-for="item in productList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="workOrderForm.spec" placeholder="自动带出" disabled />
        </el-form-item>
        <el-form-item label="计划数量" prop="quantity">
          <el-input-number v-model="workOrderForm.quantity" :min="1" :max="99999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计划开始" prop="planStartDate">
          <el-date-picker
            v-model="workOrderForm.planStartDate"
            type="date"
            placeholder="选择开始日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="计划完成" prop="planEndDate">
          <el-date-picker
            v-model="workOrderForm.planEndDate"
            type="date"
            placeholder="选择完成日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="负责人" prop="manager">
          <el-input v-model="workOrderForm.manager" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="workOrderForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="工单详情" width="700px">
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="工单号">{{ currentWorkOrder.workOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="工单状态">
          <el-tag :type="getStatusType(currentWorkOrder.status)" size="small">
            {{ getStatusText(currentWorkOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="关联订单">{{ currentWorkOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ currentWorkOrder.productName }}</el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ currentWorkOrder.spec }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ currentWorkOrder.manager }}</el-descriptions-item>
        <el-descriptions-item label="计划数量">{{ currentWorkOrder.quantity }}</el-descriptions-item>
        <el-descriptions-item label="已完成数量">{{ currentWorkOrder.finishedQuantity }}</el-descriptions-item>
        <el-descriptions-item label="计划开始">{{ currentWorkOrder.planStartDate }}</el-descriptions-item>
        <el-descriptions-item label="计划完成">{{ currentWorkOrder.planEndDate }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentWorkOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentWorkOrder.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <div class="detail-section-title">生产进度</div>
      <el-table :data="currentWorkOrder.processList || []" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="processName" label="工序名称" min-width="140" />
        <el-table-column prop="equipment" label="设备" width="140" />
        <el-table-column prop="quantity" label="计划数量" width="100" align="center" />
        <el-table-column prop="finishedQuantity" label="完成数量" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'pending'" type="info">待开始</el-tag>
            <el-tag v-else-if="row.status === 'processing'" type="primary">进行中</el-tag>
            <el-tag v-else type="success">已完成</el-tag>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const workOrderFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  workOrderNo: '',
  orderNo: '',
  productName: '',
  status: ''
})

const workOrderForm = reactive({
  id: null,
  orderId: null,
  orderNo: '',
  productId: null,
  productName: '',
  spec: '',
  quantity: 1,
  planStartDate: '',
  planEndDate: '',
  manager: '',
  remark: ''
})

const currentWorkOrder = reactive({})

const statusOptions = [
  { label: '待生产', value: 0 },
  { label: '生产中', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 }
]

const workOrderRules = {
  orderId: [
    { required: true, message: '请选择关联订单', trigger: 'change' }
  ],
  productId: [
    { required: true, message: '请选择产品', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入计划数量', trigger: 'blur' }
  ],
  planStartDate: [
    { required: true, message: '请选择计划开始日期', trigger: 'change' }
  ],
  planEndDate: [
    { required: true, message: '请选择计划完成日期', trigger: 'change' }
  ],
  manager: [
    { required: true, message: '请输入负责人', trigger: 'blur' }
  ]
}

const orderList = ref([
  { id: 1, orderNo: 'ORD-2026001', productName: '智能健康手环 Pro', quantity: 1000 },
  { id: 2, orderNo: 'ORD-2026002', productName: '智能运动手环 Lite', quantity: 500 },
  { id: 3, orderNo: 'ORD-2026003', productName: '智能健康手环 Pro', quantity: 2000 },
  { id: 4, orderNo: 'ORD-2026004', productName: '智能运动手环 Lite', quantity: 800 },
  { id: 5, orderNo: 'ORD-2026005', productName: '智能手表 Max', quantity: 600 },
  { id: 6, orderNo: 'ORD-2026006', productName: '智能健康手环 Pro', quantity: 1500 }
])

const productList = ref([
  { id: 1, name: '智能健康手环 Pro', spec: 'SH-P001' },
  { id: 2, name: '智能运动手环 Lite', spec: 'SH-L002' },
  { id: 3, name: '智能手表 Max', spec: 'SW-M003' },
  { id: 4, name: '智能心率手环', spec: 'SH-H004' },
  { id: 5, name: '智能睡眠手环', spec: 'SH-S005' }
])

const mockData = ref([
  { id: 1, workOrderNo: 'WO-2026001', orderNo: 'ORD-2026001', productName: '智能健康手环 Pro', spec: 'SH-P001', quantity: 1000, finishedQuantity: 650, status: 1, planStartDate: '2026-07-10', planEndDate: '2026-07-18', manager: '张工', remark: '加急订单', createTime: '2026-07-08 10:30:00', orderId: 1, productId: 1, processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机01', quantity: 1000, finishedQuantity: 1000, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机01', quantity: 1000, finishedQuantity: 1000, status: 'finished' }, { processName: '组装', equipment: '自动组装线01', quantity: 1000, finishedQuantity: 650, status: 'processing' }, { processName: '测试', equipment: '测试台01', quantity: 1000, finishedQuantity: 0, status: 'pending' }, { processName: '包装', equipment: '自动包装机01', quantity: 1000, finishedQuantity: 0, status: 'pending' }] },
  { id: 2, workOrderNo: 'WO-2026002', orderNo: 'ORD-2026002', productName: '智能运动手环 Lite', spec: 'SH-L002', quantity: 500, finishedQuantity: 500, status: 2, planStartDate: '2026-07-05', planEndDate: '2026-07-12', manager: '李工', remark: '', createTime: '2026-07-03 14:20:00', orderId: 2, productId: 2, processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机02', quantity: 500, finishedQuantity: 500, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机01', quantity: 500, finishedQuantity: 500, status: 'finished' }, { processName: '组装', equipment: '自动组装线01', quantity: 500, finishedQuantity: 500, status: 'finished' }, { processName: '测试', equipment: '测试台02', quantity: 500, finishedQuantity: 500, status: 'finished' }, { processName: '包装', equipment: '自动包装机01', quantity: 500, finishedQuantity: 500, status: 'finished' }] },
  { id: 3, workOrderNo: 'WO-2026003', orderNo: 'ORD-2026003', productName: '智能健康手环 Pro', spec: 'SH-P001', quantity: 800, finishedQuantity: 0, status: 0, planStartDate: '2026-07-15', planEndDate: '2026-07-22', manager: '王工', remark: '新批次订单', createTime: '2026-07-12 09:00:00', orderId: 3, productId: 1, processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机01', quantity: 800, finishedQuantity: 0, status: 'pending' }, { processName: '回流焊接', equipment: '回流焊机01', quantity: 800, finishedQuantity: 0, status: 'pending' }, { processName: '组装', equipment: '自动组装线01', quantity: 800, finishedQuantity: 0, status: 'pending' }, { processName: '测试', equipment: '测试台01', quantity: 800, finishedQuantity: 0, status: 'pending' }, { processName: '包装', equipment: '自动包装机01', quantity: 800, finishedQuantity: 0, status: 'pending' }] },
  { id: 4, workOrderNo: 'WO-2026004', orderNo: 'ORD-2026004', productName: '智能运动手环 Lite', spec: 'SH-L002', quantity: 600, finishedQuantity: 200, status: 1, planStartDate: '2026-07-12', planEndDate: '2026-07-20', manager: '赵工', remark: '', createTime: '2026-07-10 11:45:00', orderId: 4, productId: 2, processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机02', quantity: 600, finishedQuantity: 600, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机02', quantity: 600, finishedQuantity: 600, status: 'finished' }, { processName: '组装', equipment: '自动组装线02', quantity: 600, finishedQuantity: 200, status: 'processing' }, { processName: '测试', equipment: '测试台01', quantity: 600, finishedQuantity: 0, status: 'pending' }, { processName: '包装', equipment: '自动包装机02', quantity: 600, finishedQuantity: 0, status: 'pending' }] },
  { id: 5, workOrderNo: 'WO-2026005', orderNo: 'ORD-2026005', productName: '智能手表 Max', spec: 'SW-M003', quantity: 600, finishedQuantity: 600, status: 2, planStartDate: '2026-07-01', planEndDate: '2026-07-10', manager: '刘工', remark: '首批量产订单', createTime: '2026-06-28 16:30:00', orderId: 5, productId: 3, processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机01', quantity: 600, finishedQuantity: 600, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机01', quantity: 600, finishedQuantity: 600, status: 'finished' }, { processName: '组装', equipment: '自动组装线01', quantity: 600, finishedQuantity: 600, status: 'finished' }, { processName: '测试', equipment: '测试台02', quantity: 600, finishedQuantity: 600, status: 'finished' }, { processName: '包装', equipment: '自动包装机01', quantity: 600, finishedQuantity: 600, status: 'finished' }] },
  { id: 6, workOrderNo: 'WO-2026006', orderNo: 'ORD-2026006', productName: '智能健康手环 Pro', spec: 'SH-P001', quantity: 1500, finishedQuantity: 450, status: 1, planStartDate: '2026-07-14', planEndDate: '2026-07-25', manager: '张工', remark: '大客户订单', createTime: '2026-07-11 08:15:00', orderId: 6, productId: 1, processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机01', quantity: 1500, finishedQuantity: 1500, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机01', quantity: 1500, finishedQuantity: 1500, status: 'finished' }, { processName: '组装', equipment: '自动组装线01', quantity: 1500, finishedQuantity: 450, status: 'processing' }, { processName: '测试', equipment: '测试台01', quantity: 1500, finishedQuantity: 0, status: 'pending' }, { processName: '包装', equipment: '自动包装机01', quantity: 1500, finishedQuantity: 0, status: 'pending' }] },
  { id: 7, workOrderNo: 'WO-2026007', orderNo: 'ORD-2026001', productName: '智能心率手环', spec: 'SH-H004', quantity: 300, finishedQuantity: 0, status: 0, planStartDate: '2026-07-16', planEndDate: '2026-07-23', manager: '孙工', remark: '样品订单', createTime: '2026-07-13 13:00:00', orderId: 1, productId: 4, processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机02', quantity: 300, finishedQuantity: 0, status: 'pending' }, { processName: '回流焊接', equipment: '回流焊机02', quantity: 300, finishedQuantity: 0, status: 'pending' }, { processName: '组装', equipment: '自动组装线02', quantity: 300, finishedQuantity: 0, status: 'pending' }, { processName: '测试', equipment: '测试台02', quantity: 300, finishedQuantity: 0, status: 'pending' }, { processName: '包装', equipment: '自动包装机02', quantity: 300, finishedQuantity: 0, status: 'pending' }] },
  { id: 8, workOrderNo: 'WO-2026008', orderNo: 'ORD-2026002', productName: '智能睡眠手环', spec: 'SH-S005', quantity: 400, finishedQuantity: 400, status: 2, planStartDate: '2026-07-03', planEndDate: '2026-07-10', manager: '周工', remark: '', createTime: '2026-07-01 10:00:00', orderId: 2, productId: 5, processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机02', quantity: 400, finishedQuantity: 400, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机02', quantity: 400, finishedQuantity: 400, status: 'finished' }, { processName: '组装', equipment: '自动组装线02', quantity: 400, finishedQuantity: 400, status: 'finished' }, { processName: '测试', equipment: '测试台02', quantity: 400, finishedQuantity: 400, status: 'finished' }, { processName: '包装', equipment: '自动包装机02', quantity: 400, finishedQuantity: 400, status: 'finished' }] },
  { id: 9, workOrderNo: 'WO-2026009', orderNo: 'ORD-2026003', productName: '智能运动手环 Lite', spec: 'SH-L002', quantity: 700, finishedQuantity: 350, status: 1, planStartDate: '2026-07-11', planEndDate: '2026-07-19', manager: '吴工', remark: '', createTime: '2026-07-09 15:20:00', orderId: 3, productId: 2, processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机01', quantity: 700, finishedQuantity: 700, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机01', quantity: 700, finishedQuantity: 700, status: 'finished' }, { processName: '组装', equipment: '自动组装线01', quantity: 700, finishedQuantity: 350, status: 'processing' }, { processName: '测试', equipment: '测试台01', quantity: 700, finishedQuantity: 0, status: 'pending' }, { processName: '包装', equipment: '自动包装机01', quantity: 700, finishedQuantity: 0, status: 'pending' }] },
  { id: 10, workOrderNo: 'WO-2026010', orderNo: 'ORD-2026004', productName: '智能健康手环 Pro', spec: 'SH-P001', quantity: 900, finishedQuantity: 0, status: 3, planStartDate: '2026-07-08', planEndDate: '2026-07-16', manager: '郑工', remark: '订单已取消', createTime: '2026-07-06 09:30:00', orderId: 4, productId: 1, processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机01', quantity: 900, finishedQuantity: 0, status: 'pending' }, { processName: '回流焊接', equipment: '回流焊机01', quantity: 900, finishedQuantity: 0, status: 'pending' }, { processName: '组装', equipment: '自动组装线01', quantity: 900, finishedQuantity: 0, status: 'pending' }, { processName: '测试', equipment: '测试台01', quantity: 900, finishedQuantity: 0, status: 'pending' }, { processName: '包装', equipment: '自动包装机01', quantity: 900, finishedQuantity: 0, status: 'pending' }] },
  { id: 11, workOrderNo: 'WO-2026011', orderNo: 'ORD-2026005', productName: '智能手表 Max', spec: 'SW-M003', quantity: 800, finishedQuantity: 200, status: 1, planStartDate: '2026-07-13', planEndDate: '2026-07-24', manager: '陈工', remark: '', createTime: '2026-07-10 14:00:00', orderId: 5, productId: 3, processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机01', quantity: 800, finishedQuantity: 800, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机01', quantity: 800, finishedQuantity: 800, status: 'finished' }, { processName: '组装', equipment: '自动组装线01', quantity: 800, finishedQuantity: 200, status: 'processing' }, { processName: '测试', equipment: '测试台02', quantity: 800, finishedQuantity: 0, status: 'pending' }, { processName: '包装', equipment: '自动包装机01', quantity: 800, finishedQuantity: 0, status: 'pending' }] },
  { id: 12, workOrderNo: 'WO-2026012', orderNo: 'ORD-2026006', productName: '智能心率手环', spec: 'SH-H004', quantity: 500, finishedQuantity: 500, status: 2, planStartDate: '2026-07-06', planEndDate: '2026-07-13', manager: '黄工', remark: '', createTime: '2026-07-04 11:15:00', orderId: 6, productId: 4, processList: [{ processName: 'SMT贴片', equipment: 'SMT贴片机02', quantity: 500, finishedQuantity: 500, status: 'finished' }, { processName: '回流焊接', equipment: '回流焊机02', quantity: 500, finishedQuantity: 500, status: 'finished' }, { processName: '组装', equipment: '自动组装线02', quantity: 500, finishedQuantity: 500, status: 'finished' }, { processName: '测试', equipment: '测试台02', quantity: 500, finishedQuantity: 500, status: 'finished' }, { processName: '包装', equipment: '自动包装机02', quantity: 500, finishedQuantity: 500, status: 'finished' }] }
])

const tableData = ref([])

function getStatusType(status) {
  const types = ['info', 'primary', 'success', 'danger']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['待生产', '生产中', '已完成', '已取消']
  return texts[status] || '未知'
}

onMounted(() => {
  fetchData()
})

function fetchData() {
  loading.value = true
  try {
    let filtered = [...mockData.value]
    
    if (searchForm.workOrderNo) {
      filtered = filtered.filter(item => item.workOrderNo.includes(searchForm.workOrderNo))
    }
    if (searchForm.orderNo) {
      filtered = filtered.filter(item => item.orderNo.includes(searchForm.orderNo))
    }
    if (searchForm.productName) {
      filtered = filtered.filter(item => item.productName.includes(searchForm.productName))
    }
    if (searchForm.status !== '') {
      filtered = filtered.filter(item => item.status === searchForm.status)
    }
    
    total.value = filtered.length
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    tableData.value = filtered.slice(start, end)
  } catch (error) {
    console.error('Fetch work orders failed:', error)
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.workOrderNo = ''
  searchForm.orderNo = ''
  searchForm.productName = ''
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
  workOrderForm.id = row.id
  workOrderForm.orderId = row.orderId
  workOrderForm.orderNo = row.orderNo
  workOrderForm.productId = row.productId
  workOrderForm.productName = row.productName
  workOrderForm.spec = row.spec
  workOrderForm.quantity = row.quantity
  workOrderForm.planStartDate = row.planStartDate
  workOrderForm.planEndDate = row.planEndDate
  workOrderForm.manager = row.manager
  workOrderForm.remark = row.remark
  dialogVisible.value = true
}

function handleView(row) {
  Object.assign(currentWorkOrder, row)
  detailVisible.value = true
}

function handleAskAi() {
  let question
  if (currentWorkOrder.workOrderNo) {
    const statusText = getStatusText(currentWorkOrder.status)
    const progress = currentWorkOrder.quantity
      ? Math.round(currentWorkOrder.finishedQuantity / currentWorkOrder.quantity * 100)
      : 0
    question = `请帮我分析工单「${currentWorkOrder.workOrderNo}」的生产进度和状态。当前状态为「${statusText}」，计划数量${currentWorkOrder.quantity}，已完成${currentWorkOrder.finishedQuantity}，完成进度${progress}%，请给出进度分析和后续建议。`
  } else {
    question = '请帮我分析当前生产工单的整体进度和状态情况，包括各工单的完成情况和需要注意的事项。'
  }
  window.dispatchEvent(new CustomEvent('ask-ai', { detail: { question } }))
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除工单「${row.workOrderNo || row.workNo}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.info('演示模式，无法操作')
  }).catch(() => {})
}

function handleStart(row) {
  ElMessageBox.confirm(`确定要开始生产工单「${row.workOrderNo || row.workNo}」吗？`, '确认', {
    confirmButtonText: '开始生产',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => {
    ElMessage.info('演示模式，无法操作')
  }).catch(() => {})
}

function handleOrderChange(val) {
  const order = orderList.value.find(o => o.id === val)
  if (order) {
    workOrderForm.orderNo = order.orderNo
  }
}

function handleProductChange(val) {
  const product = productList.value.find(p => p.id === val)
  if (product) {
    workOrderForm.productName = product.name
    workOrderForm.spec = product.spec
  }
}

function resetForm() {
  workOrderForm.id = null
  workOrderForm.orderId = null
  workOrderForm.orderNo = ''
  workOrderForm.productId = null
  workOrderForm.productName = ''
  workOrderForm.spec = ''
  workOrderForm.quantity = 1
  workOrderForm.planStartDate = ''
  workOrderForm.planEndDate = ''
  workOrderForm.manager = ''
  workOrderForm.remark = ''
  if (workOrderFormRef.value) {
    workOrderFormRef.value.resetFields()
  }
}

async function handleSubmit() {
  if (!workOrderFormRef.value) return
  
  try {
    await workOrderFormRef.value.validate()
    ElMessage.info('演示模式，无法操作')
  } catch (error) {
    ElMessage.error('表单验证失败')
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

.header-btns {
  display: flex;
  align-items: center;
  gap: 10px;
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

.detail-section-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}
</style>
