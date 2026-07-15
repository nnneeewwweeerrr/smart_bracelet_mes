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
        <el-form-item label="故障编号">
          <el-input v-model="searchForm.faultNo" placeholder="请输入故障编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="故障状态">
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
        <span class="table-title">设备故障列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          故障报修
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="faultNo" label="故障编号" width="160" />
        <el-table-column prop="equipmentName" label="设备名称" min-width="160" />
        <el-table-column prop="faultLevel" label="故障等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getFaultLevelTag(row.faultLevel)" size="small">
              {{ getFaultLevelText(row.faultLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="faultDesc" label="故障描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reporter" label="报修人" width="100" />
        <el-table-column prop="reportTime" label="报修时间" width="170" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.status === 0"
              type="warning"
              link
              @click="handleAssign(row)"
            >
              手动派工
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              link
              @click="handleAutoAssign(row)"
            >
              自动派工
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="primary"
              link
              @click="handleProcess(row)"
            >
              处理故障
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
      :title="dialogTitle"
      width="650px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="faultFormRef"
        :model="faultForm"
        :rules="faultRules"
        label-width="100px"
        :disabled="isView"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备" prop="equipmentId">
              <el-select v-model="faultForm.equipmentId" placeholder="请选择设备" style="width: 100%" @change="handleEquipmentChange">
                <el-option
                  v-for="item in equipmentList"
                  :key="item.id"
                  :label="item.equipmentName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="故障等级" prop="faultLevel">
              <el-select v-model="faultForm.faultLevel" placeholder="请选择等级" style="width: 100%">
                <el-option label="一般" :value="1" />
                <el-option label="严重" :value="2" />
                <el-option label="紧急" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="报修人" prop="reporter">
              <el-input v-model="faultForm.reporter" placeholder="请输入报修人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所在工序" prop="process">
              <el-input v-model="faultForm.process" placeholder="请输入工序" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="故障描述" prop="faultDesc">
          <el-input v-model="faultForm.faultDesc" type="textarea" :rows="4" placeholder="请详细描述故障现象" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="faultForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ isView ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewVisible" title="故障详情" width="700px">
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="故障编号">{{ currentItem.faultNo }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentItem.equipmentName }}</el-descriptions-item>
        <el-descriptions-item label="故障等级">
          <el-tag :type="getFaultLevelTag(currentItem.faultLevel)" size="small">
            {{ getFaultLevelText(currentItem.faultLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="所在工序">{{ currentItem.process }}</el-descriptions-item>
        <el-descriptions-item label="报修人">{{ currentItem.reporter }}</el-descriptions-item>
        <el-descriptions-item label="报修时间">{{ currentItem.reportTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentItem.status)" size="small">
            {{ getStatusText(currentItem.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理人" v-if="currentItem.handler">
          {{ currentItem.handler }}
        </el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">{{ currentItem.faultDesc }}</el-descriptions-item>
        <el-descriptions-item label="处理方案" :span="2" v-if="currentItem.solution">
          {{ currentItem.solution }}
        </el-descriptions-item>
        <el-descriptions-item label="处理时间" v-if="currentItem.handleTime">
          {{ currentItem.handleTime }}
        </el-descriptions-item>
        <el-descriptions-item label="维修时长(h)" v-if="currentItem.repairHours">
          {{ currentItem.repairHours }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentItem.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="processVisible"
      title="故障处理"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="processFormRef" :model="processForm" :rules="processRules" label-width="100px">
        <el-form-item label="处理状态" prop="status">
          <el-radio-group v-model="processForm.status">
            <el-radio :value="2">已修复</el-radio>
            <el-radio :value="3">无法修复</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理人" prop="handler">
          <el-input v-model="processForm.handler" placeholder="请输入处理人" />
        </el-form-item>
        <el-form-item label="维修时长(小时)" prop="repairHours" v-if="processForm.status === 2">
          <el-input-number v-model="processForm.repairHours" :min="0" :precision="1" :step="0.5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="处理方案" prop="solution">
          <el-input v-model="processForm.solution" type="textarea" :rows="4" placeholder="请输入处理方案" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processVisible = false">取消</el-button>
        <el-button type="primary" :loading="processSubmitLoading" @click="handleProcessSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="assignVisible"
      title="手动派工"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="100px">
        <el-form-item label="维修人员" prop="assigneeId">
          <el-select v-model="assignForm.assigneeId" placeholder="请选择维修人员" style="width: 100%">
            <el-option label="张维修" :value="10" />
            <el-option label="李维修" :value="11" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignLoading" @click="handleAssignSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const processSubmitLoading = ref(false)
const assignLoading = ref(false)
const dialogVisible = ref(false)
const viewVisible = ref(false)
const processVisible = ref(false)
const assignVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false)
const faultFormRef = ref(null)
const processFormRef = ref(null)
const assignFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  faultNo: '',
  status: ''
})

const faultForm = reactive({
  id: null,
  faultNo: '',
  equipmentId: null,
  equipmentName: '',
  faultLevel: 1,
  faultDesc: '',
  reporter: '',
  process: '',
  remark: ''
})

const currentItem = reactive({})

const processForm = reactive({
  id: null,
  status: 2,
  handler: '',
  repairHours: 0,
  solution: ''
})

const assignForm = reactive({
  id: null,
  assigneeId: null
})

const faultLevelOptions = [
  { label: '一般', value: 1 },
  { label: '严重', value: 2 },
  { label: '紧急', value: 3 }
]

const statusOptions = [
  { label: '待处理', value: 0 },
  { label: '维修中', value: 1 },
  { label: '已修复', value: 2 },
  { label: '无法修复', value: 3 }
]

const faultRules = {
  equipmentId: [
    { required: true, message: '请选择设备', trigger: 'change' }
  ],
  faultLevel: [
    { required: true, message: '请选择故障等级', trigger: 'change' }
  ],
  faultDesc: [
    { required: true, message: '请输入故障描述', trigger: 'blur' }
  ],
  reporter: [
    { required: true, message: '请输入报修人', trigger: 'blur' }
  ]
}

const processRules = {
  status: [
    { required: true, message: '请选择处理状态', trigger: 'change' }
  ],
  handler: [
    { required: true, message: '请输入处理人', trigger: 'blur' }
  ],
  solution: [
    { required: true, message: '请输入处理方案', trigger: 'blur' }
  ]
}

const assignRules = {
  assigneeId: [
    { required: true, message: '请选择维修人员', trigger: 'change' }
  ]
}

const mockEquipmentList = [
  { id: 1, equipmentName: '贴片焊接机-001', equipmentNo: 'SMT-001' },
  { id: 2, equipmentName: '贴片焊接机-002', equipmentNo: 'SMT-002' },
  { id: 3, equipmentName: '注塑机-001', equipmentNo: 'INJ-001' },
  { id: 4, equipmentName: '注塑机-002', equipmentNo: 'INJ-002' },
  { id: 5, equipmentName: '激光雕刻机', equipmentNo: 'LAS-001' },
  { id: 6, equipmentName: '质量检测机', equipmentNo: 'TST-001' },
  { id: 7, equipmentName: '包装机', equipmentNo: 'PKG-001' },
  { id: 8, equipmentName: '电路板测试仪', equipmentNo: 'TST-002' }
]

const mockData = [
  { id: 1, faultNo: 'FAULT-20260701001', equipmentName: '贴片焊接机-001', equipmentNo: 'SMT-001', faultLevel: 3, faultDesc: '焊接头温度异常，无法完成焊接', status: 0, reportTime: '2026-07-01 09:30:00', reporter: '张三', handler: '', process: '贴片工序', completeTime: '', remark: '' },
  { id: 2, faultNo: 'FAULT-20260701002', equipmentName: '注塑机-001', equipmentNo: 'INJ-001', faultLevel: 2, faultDesc: '注塑压力不稳定，产品出现气泡', status: 1, reportTime: '2026-07-01 10:15:00', reporter: '李四', handler: '张维修', process: '注塑工序', completeTime: '', remark: '正在维修中' },
  { id: 3, faultNo: 'FAULT-20260702001', equipmentName: '激光雕刻机', equipmentNo: 'LAS-001', faultLevel: 1, faultDesc: '激光功率下降，雕刻深度不足', status: 2, reportTime: '2026-07-02 14:20:00', reporter: '王五', handler: '李维修', process: '雕刻工序', completeTime: '2026-07-02 16:30:00', remark: '更换激光管后恢复正常' },
  { id: 4, faultNo: 'FAULT-20260702002', equipmentName: '质量检测机', equipmentNo: 'TST-001', faultLevel: 2, faultDesc: '检测传感器读数异常', status: 0, reportTime: '2026-07-02 15:45:00', reporter: '赵六', handler: '', process: '检测工序', completeTime: '', remark: '' },
  { id: 5, faultNo: 'FAULT-20260703001', equipmentName: '包装机', equipmentNo: 'PKG-001', faultLevel: 1, faultDesc: '包装膜张力调节困难', status: 1, reportTime: '2026-07-03 08:00:00', reporter: '孙七', handler: '张维修', process: '包装工序', completeTime: '', remark: '' },
  { id: 6, faultNo: 'FAULT-20260703002', equipmentName: '贴片焊接机-002', equipmentNo: 'SMT-002', faultLevel: 3, faultDesc: '送料器卡料，生产线中断', status: 2, reportTime: '2026-07-03 11:30:00', reporter: '周八', handler: '李维修', process: '贴片工序', completeTime: '2026-07-03 13:00:00', remark: '清理送料器，恢复生产' },
  { id: 7, faultNo: 'FAULT-20260704001', equipmentName: '电路板测试仪', equipmentNo: 'TST-002', faultLevel: 1, faultDesc: '部分测试点接触不良', status: 3, reportTime: '2026-07-04 09:00:00', reporter: '吴九', handler: '张维修', process: '测试工序', completeTime: '2026-07-04 10:30:00', remark: '无法修复，需更换设备' },
  { id: 8, faultNo: 'FAULT-20260704002', equipmentName: '注塑机-002', equipmentNo: 'INJ-002', faultLevel: 2, faultDesc: '模具更换困难，定位不准', status: 0, reportTime: '2026-07-04 14:00:00', reporter: '郑十', handler: '', process: '注塑工序', completeTime: '', remark: '' },
  { id: 9, faultNo: 'FAULT-20260705001', equipmentName: '贴片焊接机-001', equipmentNo: 'SMT-001', faultLevel: 1, faultDesc: '吸嘴堵塞，元件吸取率下降', status: 1, reportTime: '2026-07-05 10:20:00', reporter: '张三', handler: '李维修', process: '贴片工序', completeTime: '', remark: '' },
  { id: 10, faultNo: 'FAULT-20260705002', equipmentName: '激光雕刻机', equipmentNo: 'LAS-001', faultLevel: 2, faultDesc: '冷却系统故障，设备过热', status: 2, reportTime: '2026-07-05 16:00:00', reporter: '王五', handler: '张维修', process: '雕刻工序', completeTime: '2026-07-05 18:00:00', remark: '更换冷却液，清理散热片' },
  { id: 11, faultNo: 'FAULT-20260706001', equipmentName: '质量检测机', equipmentNo: 'TST-001', faultLevel: 3, faultDesc: '设备死机，无法启动', status: 0, reportTime: '2026-07-06 08:30:00', reporter: '赵六', handler: '', process: '检测工序', completeTime: '', remark: '' },
  { id: 12, faultNo: 'FAULT-20260706002', equipmentName: '包装机', equipmentNo: 'PKG-001', faultLevel: 1, faultDesc: '封口温度偏低，包装不牢固', status: 2, reportTime: '2026-07-06 13:45:00', reporter: '孙七', handler: '李维修', process: '包装工序', completeTime: '2026-07-06 15:00:00', remark: '调整温度参数' },
  { id: 13, faultNo: 'FAULT-20260707001', equipmentName: '注塑机-001', equipmentNo: 'INJ-001', faultLevel: 2, faultDesc: '液压油泄漏', status: 1, reportTime: '2026-07-07 11:00:00', reporter: '李四', handler: '张维修', process: '注塑工序', completeTime: '', remark: '等待配件' },
  { id: 14, faultNo: 'FAULT-20260707002', equipmentName: '贴片焊接机-002', equipmentNo: 'SMT-002', faultLevel: 1, faultDesc: '程序运行缓慢', status: 0, reportTime: '2026-07-07 15:30:00', reporter: '周八', handler: '', process: '贴片工序', completeTime: '', remark: '' },
  { id: 15, faultNo: 'FAULT-20260708001', equipmentName: '电路板测试仪', equipmentNo: 'TST-002', faultLevel: 2, faultDesc: '测试软件报错', status: 2, reportTime: '2026-07-08 09:45:00', reporter: '吴九', handler: '李维修', process: '测试工序', completeTime: '2026-07-08 11:00:00', remark: '重新安装测试软件' },
  { id: 16, faultNo: 'FAULT-20260708002', equipmentName: '激光雕刻机', equipmentNo: 'LAS-001', faultLevel: 3, faultDesc: '激光器故障，无法工作', status: 0, reportTime: '2026-07-08 14:00:00', reporter: '王五', handler: '', process: '雕刻工序', completeTime: '', remark: '' },
  { id: 17, faultNo: 'FAULT-20260709001', equipmentName: '注塑机-002', equipmentNo: 'INJ-002', faultLevel: 1, faultDesc: '料斗堵塞', status: 2, reportTime: '2026-07-09 10:15:00', reporter: '郑十', handler: '张维修', process: '注塑工序', completeTime: '2026-07-09 11:30:00', remark: '清理料斗' },
  { id: 18, faultNo: 'FAULT-20260709002', equipmentName: '质量检测机', equipmentNo: 'TST-001', faultLevel: 2, faultDesc: '显示屏花屏', status: 1, reportTime: '2026-07-09 16:30:00', reporter: '赵六', handler: '李维修', process: '检测工序', completeTime: '', remark: '' },
  { id: 19, faultNo: 'FAULT-20260710001', equipmentName: '包装机', equipmentNo: 'PKG-001', faultLevel: 3, faultDesc: '电机烧毁，包装线停机', status: 0, reportTime: '2026-07-10 08:00:00', reporter: '孙七', handler: '', process: '包装工序', completeTime: '', remark: '' },
  { id: 20, faultNo: 'FAULT-20260710002', equipmentName: '贴片焊接机-001', equipmentNo: 'SMT-001', faultLevel: 1, faultDesc: '指示灯不亮', status: 3, reportTime: '2026-07-10 13:00:00', reporter: '张三', handler: '张维修', process: '贴片工序', completeTime: '2026-07-10 14:00:00', remark: '无法修复，需更换指示灯模块' }
]

const equipmentList = ref(mockEquipmentList)

const statCards = computed(() => [
  { title: '待处理', value: pendingCount.value, icon: 'Warning', color: '#f1a7b8' },
  { title: '维修中', value: repairingCount.value, icon: 'Clock', color: '#fae1a4' },
  { title: '已修复', value: fixedCount.value, icon: 'CircleCheck', color: '#83d8cf' },
  { title: '本月累计', value: total.value, icon: 'Document', color: '#93c5dd' }
])

const filteredData = computed(() => {
  let data = [...mockData]
  if (searchForm.faultNo) {
    data = data.filter(item => item.faultNo.includes(searchForm.faultNo))
  }
  if (searchForm.status !== '') {
    data = data.filter(item => item.status === Number(searchForm.status))
  }
  return data
})

const pendingCount = computed(() => filteredData.value.filter(item => item.status === 0).length)
const repairingCount = computed(() => filteredData.value.filter(item => item.status === 1).length)
const fixedCount = computed(() => filteredData.value.filter(item => item.status === 2).length)

const tableData = ref([])

const dialogTitle = computed(() => {
  if (isView.value) return '故障详情'
  return isEdit.value ? '编辑故障' : '故障报修'
})

function getFaultLevelText(level) {
  const texts = ['', '一般', '严重', '紧急']
  return texts[level] || '未知'
}

function getFaultLevelTag(level) {
  const types = ['', 'info', 'warning', 'danger']
  return types[level] || 'info'
}

function getStatusType(status) {
  const types = ['warning', 'primary', 'success', 'info']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['待处理', '维修中', '已修复', '无法修复']
  return texts[status] || '未知'
}

function fetchEquipmentList() {
  equipmentList.value = mockEquipmentList
}

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filteredData = [...mockData]
    if (searchForm.faultNo) {
      filteredData = filteredData.filter(item => item.faultNo.includes(searchForm.faultNo))
    }
    if (searchForm.status !== '') {
      filteredData = filteredData.filter(item => item.status === Number(searchForm.status))
    }
    total.value = filteredData.length
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    tableData.value = filteredData.slice(start, end)
    loading.value = false
  }, 300)
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.faultNo = ''
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
  ElMessage.info('演示模式，无法操作')
}

function handleView(row) {
  Object.assign(currentItem, row)
  viewVisible.value = true
}

function handleProcess(row) {
  ElMessage.info('演示模式，无法操作')
}

function handleDelete(row) {
  ElMessage.info('演示模式，无法操作')
}

function handleEquipmentChange(val) {
  const equip = equipmentList.value.find(e => e.id === val)
  if (equip) {
    faultForm.equipmentName = equip.equipmentName
  }
}

function resetForm() {
  faultForm.id = null
  faultForm.faultNo = ''
  faultForm.equipmentId = null
  faultForm.equipmentName = ''
  faultForm.faultLevel = 1
  faultForm.faultDesc = ''
  faultForm.reporter = ''
  faultForm.process = ''
  faultForm.remark = ''
  if (faultFormRef.value) {
    faultFormRef.value.resetFields()
  }
}

function handleSubmit() {
  ElMessage.info('演示模式，无法操作')
}

function handleProcessSubmit() {
  ElMessage.info('演示模式，无法操作')
}

function handleAssign(row) {
  ElMessage.info('演示模式，无法操作')
}

function handleAssignSubmit() {
  ElMessage.info('演示模式，无法操作')
}

function handleAutoAssign(row) {
  ElMessage.info('演示模式，无法操作')
}

onMounted(() => {
  fetchEquipmentList()
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
