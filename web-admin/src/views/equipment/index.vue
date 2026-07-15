<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="设备编号">
          <el-input v-model="searchForm.equipmentNo" placeholder="请输入设备编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="searchForm.name" placeholder="请输入设备名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 140px">
            <el-option label="生产设备" value="production" />
            <el-option label="检测设备" value="inspection" />
            <el-option label="辅助设备" value="auxiliary" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="运行中" value="running" />
            <el-option label="空闲" value="idle" />
            <el-option label="维修中" value="maintenance" />
            <el-option label="停用" value="disabled" />
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
        <el-table-column prop="equipmentNo" label="设备编号" width="140" />
        <el-table-column prop="name" label="设备名称" min-width="160" />
        <el-table-column prop="type" label="设备类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'production'" type="primary">生产设备</el-tag>
            <el-tag v-else-if="row.type === 'inspection'" type="success">检测设备</el-tag>
            <el-tag v-else type="info">辅助设备</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="model" label="规格型号" width="140" />
        <el-table-column prop="workshop" label="所属车间" width="120" />
        <el-table-column prop="manager" label="负责人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'running'" type="success">运行中</el-tag>
            <el-tag v-else-if="row.status === 'idle'" type="info">空闲</el-tag>
            <el-tag v-else-if="row.status === 'maintenance'" type="warning">维修中</el-tag>
            <el-tag v-else type="danger">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
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
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="equipmentFormRef"
        :model="equipmentForm"
        :rules="equipmentRules"
        label-width="100px"
      >
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="equipmentForm.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="type">
          <el-select v-model="equipmentForm.type" placeholder="请选择设备类型" style="width: 100%">
            <el-option label="生产设备" value="production" />
            <el-option label="检测设备" value="inspection" />
            <el-option label="辅助设备" value="auxiliary" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格型号" prop="model">
          <el-input v-model="equipmentForm.model" placeholder="请输入规格型号" />
        </el-form-item>
        <el-form-item label="所属车间" prop="workshop">
          <el-select v-model="equipmentForm.workshop" placeholder="请选择车间" style="width: 100%">
            <el-option label="组装车间" value="组装车间" />
            <el-option label="贴片车间" value="贴片车间" />
            <el-option label="质检车间" value="质检车间" />
            <el-option label="包装车间" value="包装车间" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="manager">
          <el-input v-model="equipmentForm.manager" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="equipmentForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="购置日期" prop="purchaseDate">
          <el-date-picker
            v-model="equipmentForm.purchaseDate"
            type="date"
            placeholder="请选择购置日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="equipmentForm.status">
            <el-radio value="running">运行中</el-radio>
            <el-radio value="idle">空闲</el-radio>
            <el-radio value="maintenance">维修中</el-radio>
            <el-radio value="disabled">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="equipmentForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="设备详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="设备编号">{{ currentEquipment.equipmentNo }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentEquipment.name }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">
          <el-tag v-if="currentEquipment.type === 'production'" type="primary">生产设备</el-tag>
          <el-tag v-else-if="currentEquipment.type === 'inspection'" type="success">检测设备</el-tag>
          <el-tag v-else type="info">辅助设备</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ currentEquipment.model }}</el-descriptions-item>
        <el-descriptions-item label="所属车间">{{ currentEquipment.workshop }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ currentEquipment.manager }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentEquipment.phone }}</el-descriptions-item>
        <el-descriptions-item label="购置日期">{{ currentEquipment.purchaseDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentEquipment.status === 'running'" type="success">运行中</el-tag>
          <el-tag v-else-if="currentEquipment.status === 'idle'" type="info">空闲</el-tag>
          <el-tag v-else-if="currentEquipment.status === 'maintenance'" type="warning">维修中</el-tag>
          <el-tag v-else type="danger">停用</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentEquipment.createTime }}</el-descriptions-item>
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
import { ElMessage } from 'element-plus'

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
  name: '',
  type: '',
  status: ''
})

const equipmentForm = reactive({
  id: null,
  equipmentNo: '',
  name: '',
  type: 'production',
  model: '',
  workshop: '',
  manager: '',
  phone: '',
  purchaseDate: '',
  status: 'idle',
  remark: ''
})

const currentEquipment = reactive({})

const equipmentRules = {
  name: [
    { required: true, message: '请输入设备名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ],
  workshop: [
    { required: true, message: '请选择所属车间', trigger: 'change' }
  ],
  manager: [
    { required: true, message: '请输入负责人', trigger: 'blur' }
  ]
}

const mockData = [
  { id: 1, equipmentNo: 'PRD-001', name: '全自动焊接机', type: 'production', model: 'WD-2000', workshop: '贴片车间', status: 'running', manager: '张工', phone: '13800138001', purchaseDate: '2024-01-15', remark: '用于主板焊接', createTime: '2024-01-15 10:30:00' },
  { id: 2, equipmentNo: 'PRD-002', name: '高速贴片机', type: 'production', model: 'TP-5000', workshop: '贴片车间', status: 'running', manager: '李工', phone: '13800138002', purchaseDate: '2024-02-20', remark: 'SMT贴片作业', createTime: '2024-02-20 09:15:00' },
  { id: 3, equipmentNo: 'PRD-003', name: '注塑成型机', type: 'production', model: 'ZS-3000', workshop: '组装车间', status: 'idle', manager: '王工', phone: '13800138003', purchaseDate: '2024-03-10', remark: '外壳注塑', createTime: '2024-03-10 14:20:00' },
  { id: 4, equipmentNo: 'PRD-004', name: '智能组装线', type: 'production', model: 'ZL-1000', workshop: '组装车间', status: 'running', manager: '赵工', phone: '13800138004', purchaseDate: '2024-01-05', remark: '手环整机组装', createTime: '2024-01-05 08:00:00' },
  { id: 5, equipmentNo: 'PRD-005', name: '激光焊接机', type: 'production', model: 'JG-1500', workshop: '组装车间', status: 'maintenance', manager: '孙工', phone: '13800138005', purchaseDate: '2023-12-20', remark: '电池焊接', createTime: '2023-12-20 11:45:00' },
  { id: 6, equipmentNo: 'PRD-006', name: 'AOI检测仪', type: 'production', model: 'AOI-800', workshop: '贴片车间', status: 'running', manager: '周工', phone: '13800138006', purchaseDate: '2024-04-18', remark: '贴片质量检测', createTime: '2024-04-18 13:30:00' },
  { id: 7, equipmentNo: 'INS-001', name: '老化测试仪', type: 'inspection', model: 'LH-200', workshop: '质检车间', status: 'running', manager: '吴工', phone: '13800138007', purchaseDate: '2024-05-05', remark: '72小时老化测试', createTime: '2024-05-05 10:00:00' },
  { id: 8, equipmentNo: 'INS-002', name: '性能测试仪', type: 'inspection', model: 'XN-300', workshop: '质检车间', status: 'idle', manager: '郑工', phone: '13800138008', purchaseDate: '2024-05-20', remark: '心率、血氧检测', createTime: '2024-05-20 09:30:00' },
  { id: 9, equipmentNo: 'INS-003', name: '防水测试仪', type: 'inspection', model: 'FS-150', workshop: '质检车间', status: 'running', manager: '黄工', phone: '13800138009', purchaseDate: '2024-06-01', remark: 'IP68防水测试', createTime: '2024-06-01 14:00:00' },
  { id: 10, equipmentNo: 'INS-004', name: '包装检测仪', type: 'inspection', model: 'BZ-100', workshop: '包装车间', status: 'idle', manager: '杨工', phone: '13800138010', purchaseDate: '2024-06-15', remark: '包装完整性检测', createTime: '2024-06-15 11:20:00' },
  { id: 11, equipmentNo: 'AUX-001', name: '螺杆空压机', type: 'auxiliary', model: 'KY-50', workshop: '贴片车间', status: 'running', manager: '陈工', phone: '13800138011', purchaseDate: '2023-11-10', remark: '提供压缩空气', createTime: '2023-11-10 08:30:00' },
  { id: 12, equipmentNo: 'AUX-002', name: '电动叉车', type: 'auxiliary', model: 'CC-3T', workshop: '仓库', status: 'idle', manager: '许工', phone: '13800138012', purchaseDate: '2024-02-28', remark: '货物搬运', createTime: '2024-02-28 15:00:00' },
  { id: 13, equipmentNo: 'AUX-003', name: 'AGV搬运车', type: 'auxiliary', model: 'AGV-200', workshop: '组装车间', status: 'running', manager: '何工', phone: '13800138013', purchaseDate: '2024-04-01', remark: '自动物料配送', createTime: '2024-04-01 10:15:00' },
  { id: 14, equipmentNo: 'AUX-004', name: '无尘净化系统', type: 'auxiliary', model: 'JH-1000', workshop: '贴片车间', status: 'running', manager: '胡工', phone: '13800138014', purchaseDate: '2023-10-01', remark: '车间空气净化', createTime: '2023-10-01 09:00:00' },
  { id: 15, equipmentNo: 'AUX-005', name: 'UPS电源', type: 'auxiliary', model: 'UPS-80K', workshop: '质检车间', status: 'idle', manager: '郭工', phone: '13800138015', purchaseDate: '2024-03-20', remark: '备用电源', createTime: '2024-03-20 16:45:00' },
  { id: 16, equipmentNo: 'PRD-007', name: '丝印机', type: 'production', model: 'SY-600', workshop: '组装车间', status: 'disabled', manager: '林工', phone: '13800138016', purchaseDate: '2023-08-15', remark: '屏幕丝印', createTime: '2023-08-15 12:30:00' },
  { id: 17, equipmentNo: 'INS-005', name: '跌落测试仪', type: 'inspection', model: 'DL-200', workshop: '质检车间', status: 'maintenance', manager: '罗工', phone: '13800138017', purchaseDate: '2024-01-25', remark: '产品跌落测试', createTime: '2024-01-25 14:10:00' },
  { id: 18, equipmentNo: 'PRD-008', name: '热压贴合机', type: 'production', model: 'RY-800', workshop: '组装车间', status: 'running', manager: '梁工', phone: '13800138018', purchaseDate: '2024-05-10', remark: '屏幕贴合', createTime: '2024-05-10 11:00:00' },
  { id: 19, equipmentNo: 'AUX-006', name: '除湿机', type: 'auxiliary', model: 'CS-30', workshop: '仓库', status: 'running', manager: '宋工', phone: '13800138019', purchaseDate: '2024-04-12', remark: '仓库防潮', createTime: '2024-04-12 10:20:00' },
  { id: 20, equipmentNo: 'PRD-009', name: '点胶机', type: 'production', model: 'DJ-400', workshop: '组装车间', status: 'idle', manager: '唐工', phone: '13800138020', purchaseDate: '2024-06-20', remark: '精密点胶', createTime: '2024-06-20 09:45:00' }
]

const tableData = ref([])

onMounted(() => {
  fetchData()
})

function fetchData() {
  loading.value = true
  let filteredData = [...mockData]
  if (searchForm.equipmentNo) {
    filteredData = filteredData.filter(item => item.equipmentNo.toLowerCase().includes(searchForm.equipmentNo.toLowerCase()))
  }
  if (searchForm.name) {
    filteredData = filteredData.filter(item => item.name.toLowerCase().includes(searchForm.name.toLowerCase()))
  }
  if (searchForm.type) {
    filteredData = filteredData.filter(item => item.type === searchForm.type)
  }
  if (searchForm.status) {
    filteredData = filteredData.filter(item => item.status === searchForm.status)
  }
  total.value = filteredData.length
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  tableData.value = filteredData.slice(startIndex, endIndex)
  loading.value = false
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.equipmentNo = ''
  searchForm.name = ''
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
  ElMessage.info('演示模式，无法编辑')
}

function handleEdit(row) {
  ElMessage.info('演示模式，无法编辑')
}

function handleView(row) {
  Object.assign(currentEquipment, row)
  detailVisible.value = true
}

function handleSubmit() {
  ElMessage.info('演示模式，无法编辑')
}

function handleDelete(row) {
  ElMessage.info('演示模式，无法删除')
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
</style>
