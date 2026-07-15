<template>
  <div class="knowledge-base">
    <div class="page-header">
      <h2 class="page-title">知识库管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="syncKnowledgeBase" :loading="syncLoading">
          <el-icon><Refresh /></el-icon>
          同步知识库
        </el-button>
        <el-button type="success" @click="showUploadDialog = true">
          <el-icon><Upload /></el-icon>
          上传文档
        </el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索文档名称"
        class="search-input"
        @keyup.enter="loadDocuments"
      >
        <template #append>
          <el-button @click="loadDocuments">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="loadDocuments">
        <el-option label="已同步" :value="1" />
        <el-option label="未同步" :value="0" />
      </el-select>
    </div>

    <el-table :data="documentList" border class="document-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="fileName" label="文档名称" min-width="200">
        <template #default="{ row }">
          <div class="file-name-wrapper">
            <el-icon :size="18" class="file-icon">
              <Document v-if="row.fileType === 'markdown'" />
              <Folder v-else-if="row.fileType === 'text'" />
              <Folder v-else />
            </el-icon>
            <span>{{ row.fileName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="fileType" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getFileTypeTag(row.fileType)">
            {{ getFileTypeLabel(row.fileType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="summary" label="摘要" min-width="300" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'">
            {{ row.status === 1 ? '已同步' : '未同步' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="uploadedBy" label="上传人" width="100" />
      <el-table-column prop="uploadTime" label="上传时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDocument(row)">查看</el-button>
          <el-button size="small" type="danger" @click="deleteDocument(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadDocuments"
        @current-change="loadDocuments"
      />
    </div>

    <el-dialog title="上传文档" v-model="showUploadDialog" width="500px">
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="文档名称">
          <el-input v-model="uploadForm.fileName" placeholder="请输入文档名称" />
        </el-form-item>
        <el-form-item label="文档内容">
          <el-input
            v-model="uploadForm.content"
            type="textarea"
            :rows="10"
            placeholder="请输入文档内容（支持Markdown格式）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" @click="submitUpload" :loading="uploadLoading">确认上传</el-button>
      </template>
    </el-dialog>

    <el-dialog title="文档详情" v-model="showDetailDialog" width="600px">
      <div class="detail-content">
        <div class="detail-header">
          <div class="detail-title">{{ selectedDocument?.fileName }}</div>
          <div class="detail-meta">
            <span>类型：{{ getFileTypeLabel(selectedDocument?.fileType) }}</span>
            <span>状态：{{ selectedDocument?.status === 1 ? '已同步' : '未同步' }}</span>
          </div>
        </div>
        <div class="detail-body">
          <pre>{{ selectedDocument?.content }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Upload, Document, Folder } from '@element-plus/icons-vue'
import { getDocumentList, uploadDocument, deleteDocumentById, syncKnowledgeBaseApi } from '../../api/knowledge'

const documentList = ref([])
const searchKeyword = ref('')
const statusFilter = ref(null)
const showUploadDialog = ref(false)
const showDetailDialog = ref(false)
const selectedDocument = ref(null)
const uploadLoading = ref(false)
const syncLoading = ref(false)

const uploadForm = ref({
  fileName: '',
  content: ''
})

const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

function getFileTypeLabel(type) {
  const map = {
    markdown: 'Markdown',
    text: '文本',
    docx: 'Word',
    other: '其他'
  }
  return map[type] || '未知'
}

function getFileTypeTag(type) {
  const map = {
    markdown: 'primary',
    text: 'success',
    docx: 'warning',
    other: 'info'
  }
  return map[type] || 'info'
}

async function loadDocuments() {
  try {
    const res = await getDocumentList({
      current: pagination.value.current,
      size: pagination.value.size,
      fileName: searchKeyword.value,
      status: statusFilter.value
    })
    documentList.value = res?.data?.records || []
    pagination.value.total = res?.data?.total || 0
  } catch (e) {
    console.error('加载文档失败', e)
  }
}

async function submitUpload() {
  if (!uploadForm.value.fileName || !uploadForm.value.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  uploadLoading.value = true
  try {
    await uploadDocument({
      fileName: uploadForm.value.fileName,
      content: uploadForm.value.content,
      uploadedBy: 'admin'
    })
    ElMessage.success('上传成功')
    showUploadDialog.value = false
    uploadForm.value = { fileName: '', content: '' }
    loadDocuments()
  } catch (e) {
    ElMessage.error('上传失败')
  } finally {
    uploadLoading.value = false
  }
}

function viewDocument(row) {
  selectedDocument.value = row
  showDetailDialog.value = true
}

async function deleteDocument(row) {
  try {
    await ElMessageBox.confirm('确定删除该文档吗？', '提示', { type: 'warning' })
    await deleteDocumentById(row.id)
    ElMessage.success('删除成功')
    loadDocuments()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

async function syncKnowledgeBase() {
  syncLoading.value = true
  try {
    await syncKnowledgeBaseApi()
    ElMessage.success('同步成功')
    loadDocuments()
  } catch (e) {
    ElMessage.error('同步失败')
  } finally {
    syncLoading.value = false
  }
}

onMounted(() => {
  loadDocuments()
})
</script>

<style scoped>
.knowledge-base {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e2e8f0;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  align-items: center;
}

.search-input {
  width: 300px;
}

.document-table {
  width: 100%;
}

.file-name-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-icon {
  color: #64748b;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.detail-content {
  padding: 10px;
}

.detail-header {
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e2e8f0;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.detail-meta {
  display: flex;
  gap: 20px;
  margin-top: 8px;
  font-size: 13px;
  color: #64748b;
}

.detail-body {
  max-height: 400px;
  overflow-y: auto;
}

.detail-body pre {
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 14px;
  line-height: 1.6;
  color: #334155;
  margin: 0;
}
</style>