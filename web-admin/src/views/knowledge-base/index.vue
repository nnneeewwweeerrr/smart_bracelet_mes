<template>
  <div class="knowledge-page">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="8" v-for="item in statCards" :key="item.title">
        <div class="stat-card" :class="'card-' + item.type">
          <div class="stat-top">
            <div class="stat-label-group">
              <span class="stat-label">{{ item.title }}</span>
              <span class="stat-sub">{{ item.subtitle }}</span>
            </div>
            <div class="stat-icon">
              <el-icon :size="20"><component :is="item.icon" /></el-icon>
            </div>
          </div>
          <div class="stat-main">
            <span class="stat-value">{{ item.value }}</span>
            <span class="stat-unit">{{ item.unit }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 搜索区 -->
    <div class="chart-card">
      <div class="search-bar">
        <el-input v-model="searchForm.docNo" placeholder="文档编号" clearable style="width: 160px" />
        <el-input v-model="searchForm.docName" placeholder="文档名称" clearable style="width: 180px" />
        <el-input v-model="searchForm.category" placeholder="分类" clearable style="width: 140px" />
        <el-select v-model="searchForm.docType" placeholder="类型" clearable style="width: 120px">
          <el-option label="文本" value="TEXT" />
          <el-option label="Markdown" value="MARKDOWN" />
          <el-option label="PDF" value="PDF" />
          <el-option label="Word" value="WORD" />
        </el-select>
        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>&nbsp;搜索
        </el-button>
        <el-button @click="handleReset">重置</el-button>
        <div class="bar-actions">
          <el-button type="success" @click="openAddDialog">
            <el-icon><Plus /></el-icon>&nbsp;新增文档
          </el-button>
          <el-button type="primary" @click="syncKnowledgeBase" :loading="syncLoading">
            <el-icon><Refresh /></el-icon>&nbsp;同步知识库
          </el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        class="dark-table"
        :header-cell-style="{ background: 'rgba(14,165,233,0.08)', color: '#94a3b8', borderBottom: '1px solid rgba(14,165,233,0.15)' }"
        :row-style="{ background: 'transparent' }"
        :cell-style="{ borderBottom: '1px solid rgba(14,165,233,0.08)' }"
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="docNo" label="文档编号" width="140" />
        <el-table-column prop="docName" label="文档名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="docType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.docType)" size="small">{{ getTypeLabel(row.docType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="keywords" label="关键词" min-width="160" show-overflow-tooltip />
        <el-table-column prop="summary" label="摘要" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <span class="status-tag" :class="row.status === 1 ? 'enabled' : 'disabled'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="uploader" label="上传人" width="100" />
        <el-table-column prop="uploadTime" label="上传时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleView(row)">详情</el-button>
            <el-button size="small" type="warning" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'info' : 'success'" link @click="handleToggle(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="formVisible" :title="formTitle" width="680px" class="dark-dialog">
      <el-form :model="formData" label-width="100px" class="dark-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="文档编号">
              <el-input v-model="formData.docNo" placeholder="如 DOC001" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="文档名称">
              <el-input v-model="formData.docName" placeholder="请输入文档名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="文档类型">
              <el-select v-model="formData.docType" placeholder="请选择" style="width: 100%">
                <el-option label="文本" value="TEXT" />
                <el-option label="Markdown" value="MARKDOWN" />
                <el-option label="PDF" value="PDF" />
                <el-option label="Word" value="WORD" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类">
              <el-input v-model="formData.category" placeholder="如 工艺规范" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="关键词">
          <el-input v-model="formData.keywords" placeholder="多个关键词用逗号分隔" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="formData.summary" type="textarea" :rows="2" placeholder="文档摘要" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="formData.content" type="textarea" :rows="6" placeholder="文档正文内容" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="文档详情" width="680px" class="dark-dialog">
      <el-descriptions :column="2" border class="dark-desc">
        <el-descriptions-item label="文档编号">{{ currentRow.docNo }}</el-descriptions-item>
        <el-descriptions-item label="文档名称">{{ currentRow.docName }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ getTypeLabel(currentRow.docType) }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentRow.category }}</el-descriptions-item>
        <el-descriptions-item label="关键词" :span="2">{{ currentRow.keywords }}</el-descriptions-item>
        <el-descriptions-item label="摘要" :span="2">{{ currentRow.summary }}</el-descriptions-item>
        <el-descriptions-item label="上传人">{{ currentRow.uploader }}</el-descriptions-item>
        <el-descriptions-item label="上传时间">{{ currentRow.uploadTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <span class="status-tag" :class="currentRow.status === 1 ? 'enabled' : 'disabled'">
            {{ currentRow.status === 1 ? '启用' : '禁用' }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentRow.remark }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">
          <pre class="content-pre">{{ currentRow.content }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh, Document, Files, DataBoard } from '@element-plus/icons-vue'
import {
  getAiKnowledgeList, getAiKnowledgeStats, addAiKnowledge,
  updateAiKnowledge, toggleAiKnowledge, deleteAiKnowledge, syncAiKnowledge
} from '@/api/aiKnowledge'

const loading = ref(false)
const submitLoading = ref(false)
const syncLoading = ref(false)
const formVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const formTitle = ref('新增文档')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const currentRow = reactive({})

const statCards = ref([
  { title: '文档总数', subtitle: 'Total Documents', value: '0', unit: '篇', icon: 'Document', type: 'primary' },
  { title: '已启用', subtitle: 'Enabled', value: '0', unit: '篇', icon: 'Files', type: 'success' },
  { title: '已禁用', subtitle: 'Disabled', value: '0', unit: '篇', icon: 'DataBoard', type: 'warning' }
])

const searchForm = reactive({ docNo: '', docName: '', category: '', docType: '', status: '' })
const formData = reactive({
  id: null, docNo: '', docName: '', docType: 'TEXT', category: '',
  keywords: '', summary: '', content: '', uploader: '', remark: '', status: 1
})

function getTypeLabel(type) {
  const map = { TEXT: '文本', MARKDOWN: 'Markdown', PDF: 'PDF', WORD: 'Word' }
  return map[type] || type || '未知'
}

function getTypeTag(type) {
  const map = { TEXT: 'success', MARKDOWN: 'primary', PDF: 'danger', WORD: 'warning' }
  return map[type] || 'info'
}

function fetchData() {
  loading.value = true
  getAiKnowledgeList({
    pageNum: currentPage.value,
    pageSize: pageSize.value,
    ...searchForm
  }).then(res => {
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }).catch(() => {
    ElMessage.error('加载失败')
  }).finally(() => {
    loading.value = false
  })
}

function fetchStats() {
  getAiKnowledgeStats().then(res => {
    const d = res.data || {}
    statCards.value[0].value = String(d.total || 0)
    statCards.value[1].value = String(d.enabled || 0)
    statCards.value[2].value = String((d.total || 0) - (d.enabled || 0))
  }).catch(() => {})
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  Object.assign(searchForm, { docNo: '', docName: '', category: '', docType: '', status: '' })
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

function openAddDialog() {
  isEdit.value = false
  formTitle.value = '新增文档'
  Object.assign(formData, {
    id: null, docNo: '', docName: '', docType: 'TEXT', category: '',
    keywords: '', summary: '', content: '', uploader: '', remark: '', status: 1
  })
  formVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  formTitle.value = '编辑文档'
  Object.assign(formData, row)
  formVisible.value = true
}

function handleView(row) {
  Object.assign(currentRow, row)
  detailVisible.value = true
}

function submitForm() {
  if (!formData.docName) {
    ElMessage.warning('请输入文档名称')
    return
  }
  submitLoading.value = true
  const action = isEdit.value ? updateAiKnowledge(formData) : addAiKnowledge({
    ...formData,
    uploader: JSON.parse(localStorage.getItem('userInfo') || '{}')?.username || 'admin'
  })
  action.then(() => {
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    formVisible.value = false
    fetchData()
    fetchStats()
  }).catch(() => {
    ElMessage.error('操作失败')
  }).finally(() => {
    submitLoading.value = false
  })
}

function handleToggle(row) {
  toggleAiKnowledge(row.id).then(() => {
    ElMessage.success('状态已切换')
    fetchData()
    fetchStats()
  }).catch(() => {
    ElMessage.error('操作失败')
  })
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除该文档吗？', '提示', { type: 'warning' }).then(() => {
    deleteAiKnowledge(row.id).then(() => {
      ElMessage.success('删除成功')
      fetchData()
      fetchStats()
    }).catch(() => {
      ElMessage.error('删除失败')
    })
  }).catch(() => {})
}

function syncKnowledgeBase() {
  syncLoading.value = true
  syncAiKnowledge().then(() => {
    ElMessage.success('同步成功')
  }).catch(() => {
    ElMessage.error('同步失败')
  }).finally(() => {
    syncLoading.value = false
  })
}

onMounted(() => {
  fetchData()
  fetchStats()
})
</script>

<style scoped>
.knowledge-page {
  width: 100%;
  background: radial-gradient(ellipse at top, #0a1929 0%, #030a1a 100%);
  border-radius: 12px;
  padding: 20px;
  min-height: calc(100vh - 120px);
  color: #e2e8f0;
}

.stat-row { margin-bottom: 16px; }

.stat-card {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.08) 0%, rgba(3, 10, 26, 0.6) 100%);
  border: 1px solid rgba(14, 165, 233, 0.15);
  border-radius: 12px;
  padding: 18px 20px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}
.stat-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 1px;
  background: linear-gradient(90deg, transparent, rgba(14, 165, 233, 0.5), transparent);
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.15);
  border-color: rgba(14, 165, 233, 0.3);
}
.card-primary .stat-icon { background: rgba(56, 189, 248, 0.15); color: #38bdf8; }
.card-success .stat-icon { background: rgba(74, 222, 128, 0.15); color: #4ade80; }
.card-warning .stat-icon { background: rgba(251, 191, 36, 0.15); color: #fbbf24; }

.stat-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 14px; }
.stat-label-group { display: flex; flex-direction: column; gap: 4px; }
.stat-label { font-size: 14px; font-weight: 600; color: #e2e8f0; }
.stat-sub { font-size: 10px; color: #475569; letter-spacing: 0.5px; }
.stat-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.stat-main { display: flex; align-items: baseline; gap: 4px; }
.stat-value { font-size: 30px; font-weight: 700; line-height: 1; font-family: 'Courier New', monospace; }
.card-primary .stat-value { color: #38bdf8; }
.card-success .stat-value { color: #4ade80; }
.card-warning .stat-value { color: #fbbf24; }
.stat-unit { font-size: 13px; color: #64748b; font-weight: 500; }

.chart-card {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.06) 0%, rgba(3, 10, 26, 0.5) 100%);
  border: 1px solid rgba(14, 165, 233, 0.15);
  border-radius: 12px;
  padding: 18px 20px;
  position: relative;
  overflow: hidden;
}
.chart-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 1px;
  background: linear-gradient(90deg, transparent, rgba(14, 165, 233, 0.4), transparent);
}

.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-bottom: 18px;
}
.bar-actions { margin-left: auto; display: flex; gap: 10px; }

.status-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}
.status-tag.enabled { color: #4ade80; background: rgba(74, 222, 128, 0.12); border: 1px solid rgba(74, 222, 128, 0.25); }
.status-tag.disabled { color: #fbbf24; background: rgba(251, 191, 36, 0.12); border: 1px solid rgba(251, 191, 36, 0.25); }

.pagination-wrapper { margin-top: 18px; display: flex; justify-content: flex-end; }

.content-pre {
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
  line-height: 1.6;
  color: #cbd5e1;
  margin: 0;
  max-height: 300px;
  overflow-y: auto;
}

:deep(.dark-table) { background: transparent !important; color: #e2e8f0; }
:deep(.dark-table .el-table__inner-wrapper) { background: transparent; }
:deep(.dark-table tr) { background: transparent !important; }
:deep(.dark-table td) { color: #cbd5e1; background: transparent !important; }
:deep(.dark-table td .cell) { color: #cbd5e1; }
:deep(.dark-table--enable-row-hover:hover > td) { background: rgba(14, 165, 233, 0.05) !important; }
:deep(.dark-table::before) { display: none; }
:deep(.dark-table .el-table__body-wrapper) { background: transparent; }
:deep(.dark-table .el-scrollbar) { background: transparent; }
</style>
