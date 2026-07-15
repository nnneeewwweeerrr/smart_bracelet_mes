<template>
  <div class="repair-task-page">
    <div class="page-header">
      <h2 class="page-title">我的维修任务</h2>
      <div class="header-actions">
        <el-radio-group v-model="filterStatus" size="small" @change="handleFilterChange">
          <el-radio-button :label="null">全部</el-radio-button>
          <el-radio-button :label="0">待处理</el-radio-button>
          <el-radio-button :label="1">维修中</el-radio-button>
          <el-radio-button :label="2">已完成</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <el-row :gutter="16" class="stat-row">
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon warning"><el-icon><Warning /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ pendingCount }}</div>
            <div class="stat-label">待处理</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon primary"><el-icon><Tools /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ processingCount }}</div>
            <div class="stat-label">维修中</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon success"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ completedCount }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-table :data="taskList" border stripe v-loading="loading" class="task-table">
      <el-table-column type="index" width="50" />
      <el-table-column prop="faultNo" label="故障编号" width="140" />
      <el-table-column prop="equipmentName" label="设备名称" width="140" />
      <el-table-column prop="faultLevel" label="故障等级" width="100">
        <template #default="{ row }">
          <el-tag :type="row.faultLevel === 3 ? 'danger' : row.faultLevel === 2 ? 'warning' : 'info'" size="small">
            {{ row.faultLevel === 3 ? '紧急' : row.faultLevel === 2 ? '严重' : '一般' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="faultDesc" label="故障描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="reporter" label="报修人" width="100" />
      <el-table-column prop="reportTime" label="报修时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.reportTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="assignType" label="派工类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.assignType === 'auto' ? 'success' : 'primary'" size="small">
            {{ row.assignType === 'auto' ? '自动' : '手动' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="primary" size="small" @click="handleStart(row)">
            开始维修
          </el-button>
          <el-button v-if="row.status === 1" type="success" size="small" @click="handleComplete(row)">
            完成维修
          </el-button>
          <el-button v-if="row.status === 2" type="info" size="small" disabled>
            已完成
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 完成维修弹窗 -->
    <el-dialog v-model="completeVisible" title="完成维修" width="500px">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="故障编号">
          <span>{{ currentRow?.faultNo }}</span>
        </el-form-item>
        <el-form-item label="设备名称">
          <span>{{ currentRow?.equipmentName }}</span>
        </el-form-item>
        <el-form-item label="处理方案" required>
          <el-input v-model="completeForm.solution" type="textarea" :rows="3" placeholder="请填写处理方案" />
        </el-form-item>
        <el-form-item label="维修时长" required>
          <el-input-number v-model="completeForm.repairHours" :min="0.5" :step="0.5" :precision="1" />
          <span class="unit-text">小时</span>
        </el-form-item>
        <el-form-item label="处理结果" required>
          <el-radio-group v-model="completeForm.resultStatus">
            <el-radio :label="2">已修复</el-radio>
            <el-radio :label="3">无法修复</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComplete">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning, Tools, CircleCheck } from '@element-plus/icons-vue'

const mockData = [
  { id: 1, faultNo: 'F20240701001', equipmentName: '智能手表组装线A', equipmentNo: 'EQ-A001', faultLevel: 3, faultDesc: '焊接头温度异常，导致焊点质量不稳定', status: 0, reportTime: '2024-07-15 08:30:00', reportPerson: '张工', reporter: '张工', handler: '', assignType: 'auto', completeTime: '', solution: '', repairHours: null },
  { id: 2, faultNo: 'F20240701002', equipmentName: '表带装配机B', equipmentNo: 'EQ-B002', faultLevel: 2, faultDesc: '传送带卡顿，影响生产效率', status: 0, reportTime: '2024-07-15 09:15:00', reportPerson: '李工', reporter: '李工', handler: '', assignType: 'manual', completeTime: '', solution: '', repairHours: null },
  { id: 3, faultNo: 'F20240701003', equipmentName: '屏幕检测设备', equipmentNo: 'EQ-C003', faultLevel: 1, faultDesc: '检测精度略有偏差，需要校准', status: 1, reportTime: '2024-07-15 10:00:00', reportPerson: '王工', reporter: '王工', handler: '赵维修', assignType: 'auto', completeTime: '', solution: '', repairHours: null },
  { id: 4, faultNo: 'F20240701004', equipmentName: '电池封装机', equipmentNo: 'EQ-D004', faultLevel: 3, faultDesc: '封装膜断裂，无法正常封装', status: 1, reportTime: '2024-07-15 11:20:00', reportPerson: '陈工', reporter: '陈工', handler: '孙维修', assignType: 'manual', completeTime: '', solution: '', repairHours: null },
  { id: 5, faultNo: 'F20240701005', equipmentName: '外观检查台', equipmentNo: 'EQ-E005', faultLevel: 1, faultDesc: '照明灯光亮度不足', status: 2, reportTime: '2024-07-14 14:30:00', reportPerson: '周工', reporter: '周工', handler: '吴维修', assignType: 'auto', completeTime: '2024-07-14 16:00:00', solution: '更换LED灯珠，调整亮度参数', repairHours: 1.5 },
  { id: 6, faultNo: 'F20240701006', equipmentName: '主板测试机', equipmentNo: 'EQ-F006', faultLevel: 2, faultDesc: '部分测试接口无响应', status: 2, reportTime: '2024-07-14 15:45:00', reportPerson: '郑工', reporter: '郑工', handler: '钱维修', assignType: 'manual', completeTime: '2024-07-14 18:30:00', solution: '修复接口线路，更换损坏元件', repairHours: 2.5 },
  { id: 7, faultNo: 'F20240701007', equipmentName: '包装机G', equipmentNo: 'EQ-G007', faultLevel: 1, faultDesc: '标签打印位置偏移', status: 3, reportTime: '2024-07-13 09:00:00', reportPerson: '冯工', reporter: '冯工', handler: '赵维修', assignType: 'auto', completeTime: '2024-07-13 11:00:00', solution: '设备老化严重，建议更换', repairHours: 2.0 },
  { id: 8, faultNo: 'F20240701008', equipmentName: '智能手表组装线B', equipmentNo: 'EQ-A002', faultLevel: 3, faultDesc: '主控板芯片烧录失败率过高', status: 0, reportTime: '2024-07-15 13:00:00', reportPerson: '何工', reporter: '何工', handler: '', assignType: 'manual', completeTime: '', solution: '', repairHours: null },
  { id: 9, faultNo: 'F20240701009', equipmentName: '外壳抛光机', equipmentNo: 'EQ-H008', faultLevel: 2, faultDesc: '抛光轮磨损严重，需要更换', status: 1, reportTime: '2024-07-15 14:20:00', reportPerson: '梁工', reporter: '梁工', handler: '孙维修', assignType: 'auto', completeTime: '', solution: '', repairHours: null },
  { id: 10, faultNo: 'F20240701010', equipmentName: '充电器测试站', equipmentNo: 'EQ-I009', faultLevel: 1, faultDesc: 'USB接口接触不良', status: 2, reportTime: '2024-07-14 10:30:00', reportPerson: '谢工', reporter: '谢工', handler: '吴维修', assignType: 'auto', completeTime: '2024-07-14 11:30:00', solution: '更换USB接口组件', repairHours: 1.0 },
  { id: 11, faultNo: 'F20240701011', equipmentName: '传感器校准仪', equipmentNo: 'EQ-J010', faultLevel: 2, faultDesc: '校准精度下降，超出误差范围', status: 0, reportTime: '2024-07-15 15:00:00', reportPerson: '韩工', reporter: '韩工', handler: '', assignType: 'manual', completeTime: '', solution: '', repairHours: null },
  { id: 12, faultNo: 'F20240701012', equipmentName: '激光打标机', equipmentNo: 'EQ-K011', faultLevel: 3, faultDesc: '激光功率不足，打标不清晰', status: 1, reportTime: '2024-07-15 16:30:00', reportPerson: '唐工', reporter: '唐工', handler: '钱维修', assignType: 'auto', completeTime: '', solution: '', repairHours: null },
  { id: 13, faultNo: 'F20240701013', equipmentName: '防水测试机', equipmentNo: 'EQ-L012', faultLevel: 1, faultDesc: '水压传感器读数不稳定', status: 2, reportTime: '2024-07-13 16:00:00', reportPerson: '曹工', reporter: '曹工', handler: '赵维修', assignType: 'manual', completeTime: '2024-07-13 17:30:00', solution: '校准传感器，更换密封圈', repairHours: 1.5 },
  { id: 14, faultNo: 'F20240701014', equipmentName: '包装材料切割机', equipmentNo: 'EQ-M013', faultLevel: 2, faultDesc: '刀片磨损，切割不整齐', status: 0, reportTime: '2024-07-15 17:00:00', reportPerson: '许工', reporter: '许工', handler: '', assignType: 'auto', completeTime: '', solution: '', repairHours: null },
  { id: 15, faultNo: 'F20240701015', equipmentName: '防静电工作台', equipmentNo: 'EQ-N014', faultLevel: 1, faultDesc: '静电消除器效果减弱', status: 2, reportTime: '2024-07-14 08:00:00', reportPerson: '邓工', reporter: '邓工', handler: '孙维修', assignType: 'auto', completeTime: '2024-07-14 09:00:00', solution: '更换静电消除器电极', repairHours: 1.0 }
]

const loading = ref(false)
const taskList = ref([])
const filterStatus = ref(null)
const completeVisible = ref(false)
const currentRow = ref(null)
const completeForm = ref({
  solution: '',
  repairHours: 1.0,
  resultStatus: 2
})

const pendingCount = computed(() => taskList.value.filter(t => t.status === 0).length)
const processingCount = computed(() => taskList.value.filter(t => t.status === 1).length)
const completedCount = computed(() => taskList.value.filter(t => t.status === 2 || t.status === 3).length)

function getStatusType(status) {
  const map = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = { 0: '待处理', 1: '维修中', 2: '已修复', 3: '无法修复' }
  return map[status] || '未知'
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN')
}

function fetchTasks() {
  loading.value = true
  setTimeout(() => {
    if (filterStatus.value !== null) {
      taskList.value = mockData.filter(t => t.status === filterStatus.value)
    } else {
      taskList.value = [...mockData]
    }
    loading.value = false
  }, 300)
}

function handleFilterChange() {
  fetchTasks()
}

function handleStart(row) {
  ElMessage.info('演示模式，无法操作')
}

function handleComplete(row) {
  ElMessage.info('演示模式，无法操作')
}

function submitComplete() {
  ElMessage.info('演示模式，无法操作')
}

onMounted(() => {
  fetchTasks()
})
</script>

<style scoped>
.repair-task-page {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}
.stat-row {
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid #f1f5f9;
}
.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}
.stat-icon.warning {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}
.stat-icon.primary {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}
.stat-icon.success {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
}
.stat-label {
  font-size: 12px;
  color: #94a3b8;
}
.task-table {
  background: #fff;
  border-radius: 12px;
}
.unit-text {
  margin-left: 8px;
  color: #94a3b8;
  font-size: 13px;
}
</style>