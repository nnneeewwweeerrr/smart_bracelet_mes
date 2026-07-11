<template>
  <div>
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="IQC来料" name="IQC" />
      <el-tab-pane label="IPQC工序" name="IPQC" />
      <el-tab-pane label="OQC成品" name="OQC" />
    </el-tabs>

    <el-form :model="query" inline class="search-form">
      <el-form-item label="任务编号">
        <el-input v-model="query.taskNo" placeholder="请输入任务编号" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="success" @click="handleAdd">新增任务</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="taskNo" label="任务编号" width="170" />
      <el-table-column label="类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="taskTypeTag(row.taskType)">{{ row.taskType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sourceNo" label="来源单号" width="150" />
      <el-table-column prop="inspector" label="检验员" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
      <el-table-column label="操作" width="280" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" link type="primary" @click="handleStart(row)">开始</el-button>
          <el-button v-if="row.status === 1" link type="warning" @click="handleInputResult(row)">录入结果</el-button>
          <el-button v-if="row.status === 1" link type="success" @click="handleComplete(row)">完成</el-button>
          <el-button link type="info" @click="handleViewReport(row)">查看报告</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无数据" />
      </template>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </div>

    <el-dialog v-model="dialogVisible" title="新增质检任务" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="任务类型" prop="taskType">
          <el-select v-model="form.taskType" style="width: 100%">
            <el-option label="IQC来料" value="IQC" />
            <el-option label="IPQC工序" value="IPQC" />
            <el-option label="OQC成品" value="OQC" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源单号" prop="sourceNo">
          <el-input v-model="form.sourceNo" placeholder="请输入来源单号" />
        </el-form-item>
        <el-form-item label="检验员" prop="inspector">
          <el-input v-model="form.inspector" placeholder="请输入检验员" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resultDialogVisible" title="录入检验结果" width="700px">
      <div class="result-toolbar">
        <el-button type="primary" size="small" @click="handleAddResult">新增检验项</el-button>
      </div>
      <el-table :data="resultList" border>
        <el-table-column prop="itemName" label="检验项目" min-width="140" />
        <el-table-column prop="standard" label="检验标准" min-width="160" />
        <el-table-column prop="result" label="检验结果" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.result === '合格' ? 'success' : 'danger'">{{ row.result }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" />
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row, $index }">
            <el-button link type="primary" @click="handleEditResult(row, $index)">编辑</el-button>
            <el-button link type="danger" @click="handleDeleteResult($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="resultDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="resultSubmitting" @click="handleResultSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reportDialogVisible" title="检验报告" width="700px">
      <div v-if="reportData" class="report-content">
        <div class="report-header">
          <h3>质检报告</h3>
          <div class="report-info">
            <span>任务编号：{{ reportData.taskNo }}</span>
            <span>检验员：{{ reportData.inspector }}</span>
          </div>
        </div>
        <div class="report-summary">
          <div class="summary-item">
            <div class="label">合格率</div>
            <div class="value" :class="reportData.passRate >= 90 ? 'pass' : 'fail'">
              {{ reportData.passRate || 0 }}%
            </div>
          </div>
          <div class="summary-item">
            <div class="label">检验项总数</div>
            <div class="value">{{ reportData.totalCount || 0 }}</div>
          </div>
          <div class="summary-item">
            <div class="label">合格数</div>
            <div class="value pass">{{ reportData.passCount || 0 }}</div>
          </div>
          <div class="summary-item">
            <div class="label">不合格数</div>
            <div class="value fail">{{ reportData.failCount || 0 }}</div>
          </div>
        </div>
        <div class="report-conclusion">
          <div class="conclusion-label">总体结论：</div>
          <el-tag :type="reportData.conclusion === '合格' ? 'success' : 'danger'" size="large">
            {{ reportData.conclusion || '待判定' }}
          </el-tag>
        </div>
        <el-divider>检验明细</el-divider>
        <el-table :data="reportData.items || []" border size="small">
          <el-table-column prop="itemName" label="检验项目" min-width="140" />
          <el-table-column prop="standard" label="检验标准" min-width="160" />
          <el-table-column prop="result" label="检验结果" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.result === '合格' ? 'success' : 'danger'" size="small">{{ row.result }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="120" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="reportDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resultFormVisible" :title="resultForm.id ? '编辑检验项' : '新增检验项'" width="460px">
      <el-form :model="resultForm" :rules="resultRules" ref="resultFormRef" label-width="90px">
        <el-form-item label="检验项目" prop="itemName">
          <el-input v-model="resultForm.itemName" placeholder="请输入检验项目" />
        </el-form-item>
        <el-form-item label="检验标准" prop="standard">
          <el-input v-model="resultForm.standard" placeholder="请输入检验标准" />
        </el-form-item>
        <el-form-item label="检验结果" prop="result">
          <el-select v-model="resultForm.result" style="width: 100%">
            <el-option label="合格" value="合格" />
            <el-option label="不合格" value="不合格" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="resultForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resultFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResultFormSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getList, save, remove, start, complete
} from '../../api/qualityTask'
import {
  getList as getResultList, batchSave, getReport
} from '../../api/qualityResult'

const statusOptions = [
  { value: 0, label: '待处理' },
  { value: 1, label: '处理中' },
  { value: 2, label: '已完成' }
]

const statusText = (status) => {
  const item = statusOptions.find(s => s.value === status)
  return item ? item.label : '未知'
}
const statusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success' }
  return map[status] || 'info'
}

const taskTypeTag = (type) => {
  const map = { IQC: 'primary', IPQC: 'warning', OQC: 'success' }
  return map[type] || 'info'
}

const activeTab = ref('IQC')
const query = reactive({ page: 1, size: 10, taskNo: '', status: '', taskType: 'IQC' })
const list = ref([])
const total = ref(0)
const loading = ref(false)

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getList({ ...query })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleTabChange = (tab) => {
  query.taskType = tab
  query.page = 1
  fetchList()
}

const handleSearch = () => {
  query.page = 1
  fetchList()
}

const handleReset = () => {
  query.taskNo = ''
  query.status = ''
  query.page = 1
  fetchList()
}

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  taskType: 'IQC',
  sourceNo: '',
  inspector: '',
  remark: ''
})

const rules = {
  taskType: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  sourceNo: [{ required: true, message: '请输入来源单号', trigger: 'blur' }]
}

const resetForm = () => {
  form.id = null
  form.taskType = activeTab.value
  form.sourceNo = ''
  form.inspector = ''
  form.remark = ''
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await save({ ...form })
    ElMessage.success('新增成功')
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const handleStart = (row) => {
  ElMessageBox.confirm(`确定开始任务「${row.taskNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await start(row.id)
      ElMessage.success('任务已开始')
      fetchList()
    }).catch(() => {})
}

const handleComplete = (row) => {
  ElMessageBox.confirm(`确定完成任务「${row.taskNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await complete(row.id)
      ElMessage.success('任务已完成')
      fetchList()
    }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除任务「${row.taskNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await remove(row.id)
      ElMessage.success('删除成功')
      fetchList()
    }).catch(() => {})
}

const resultDialogVisible = ref(false)
const resultList = ref([])
const resultSubmitting = ref(false)
const currentTaskId = ref(null)

const handleInputResult = async (row) => {
  currentTaskId.value = row.id
  resultDialogVisible.value = true
  try {
    const res = await getResultList({ taskId: row.id })
    resultList.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const handleResultSubmit = async () => {
  if (!currentTaskId.value) return
  resultSubmitting.value = true
  try {
    const data = resultList.value.map(item => ({
      ...item,
      taskId: currentTaskId.value
    }))
    await batchSave(data)
    ElMessage.success('保存成功')
    resultDialogVisible.value = false
    fetchList()
  } catch (e) {
    console.error(e)
  } finally {
    resultSubmitting.value = false
  }
}

const resultFormVisible = ref(false)
const resultFormRef = ref()
const resultForm = reactive({
  id: null,
  itemName: '',
  standard: '',
  result: '合格',
  remark: ''
})
const resultRules = {
  itemName: [{ required: true, message: '请输入检验项目', trigger: 'blur' }],
  result: [{ required: true, message: '请选择检验结果', trigger: 'change' }]
}
const editResultIndex = ref(-1)

const handleAddResult = () => {
  resultForm.id = null
  resultForm.itemName = ''
  resultForm.standard = ''
  resultForm.result = '合格'
  resultForm.remark = ''
  editResultIndex.value = -1
  resultFormVisible.value = true
}

const handleEditResult = (row, index) => {
  Object.assign(resultForm, row)
  editResultIndex.value = index
  resultFormVisible.value = true
}

const handleDeleteResult = (index) => {
  ElMessageBox.confirm('确定删除该检验项吗？', '提示', { type: 'warning' })
    .then(() => {
      resultList.value.splice(index, 1)
      ElMessage.success('删除成功')
    }).catch(() => {})
}

const handleResultFormSubmit = async () => {
  await resultFormRef.value.validate()
  if (editResultIndex.value >= 0) {
    resultList.value[editResultIndex.value] = { ...resultList.value[editResultIndex.value], ...resultForm }
  } else {
    resultList.value.push({ ...resultForm })
  }
  resultFormVisible.value = false
}

const reportDialogVisible = ref(false)
const reportData = ref(null)

const handleViewReport = async (row) => {
  reportDialogVisible.value = true
  reportData.value = null
  try {
    const res = await getReport(row.id)
    reportData.value = res.data || {}
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 4px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.result-toolbar {
  margin-bottom: 12px;
  display: flex;
  justify-content: flex-end;
}
.report-content {
  padding: 10px 0;
}
.report-header {
  text-align: center;
  margin-bottom: 20px;
}
.report-header h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #303133;
}
.report-info {
  display: flex;
  justify-content: center;
  gap: 30px;
  font-size: 13px;
  color: #606266;
}
.report-summary {
  display: flex;
  justify-content: space-around;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 6px;
  margin-bottom: 20px;
}
.summary-item {
  text-align: center;
}
.summary-item .label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.summary-item .value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}
.summary-item .value.pass {
  color: #67c23a;
}
.summary-item .value.fail {
  color: #f56c6c;
}
.report-conclusion {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 10px;
}
.conclusion-label {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
</style>
