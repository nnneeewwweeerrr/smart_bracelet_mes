<template>
  <div class="page-container">
    <!-- 搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="物料编号">
          <el-input v-model="searchForm.materialNo" placeholder="请输入" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="物料名称">
          <el-input v-model="searchForm.materialName" placeholder="请输入" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="仓库">
          <el-input v-model="searchForm.warehouseName" placeholder="请输入" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区 -->
    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <span class="table-title">库存查询</span>
        <el-tag type="primary" size="medium">物料种类：{{ total }}</el-tag>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialNo" label="物料编号" width="120" />
        <el-table-column prop="materialName" label="物料名称" min-width="140" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="warehouseCode" label="仓库编码" width="110" />
        <el-table-column prop="quantity" label="库存数量" width="110" align="right" />
        <el-table-column prop="location" label="库位" width="100" />
        <el-table-column prop="batchNo" label="批次号" width="130" />
        <el-table-column prop="expireDate" label="过期日期" width="120" />
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right" align="center">
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="库存详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="物料编号">{{ currentStock.materialNo }}</el-descriptions-item>
        <el-descriptions-item label="物料名称">{{ currentStock.materialName }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ currentStock.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="仓库编码">{{ currentStock.warehouseCode }}</el-descriptions-item>
        <el-descriptions-item label="库存数量">{{ currentStock.quantity }}</el-descriptions-item>
        <el-descriptions-item label="库位">{{ currentStock.location }}</el-descriptions-item>
        <el-descriptions-item label="批次号">{{ currentStock.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="过期日期">{{ currentStock.expireDate }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentStock.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentStock.updateTime }}</el-descriptions-item>
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
import { demoStock } from '@/api/demo'

const loading = ref(false)
const detailVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({ materialNo: '', materialName: '', warehouseName: '' })

const currentStock = reactive({})
const tableData = ref([])

function fetchData() {
  loading.value = true
  demoStock.getList({
    pageNum: currentPage.value,
    pageSize: pageSize.value,
    ...searchForm
  }).then(res => {
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }).catch(err => {
    ElMessage.error('加载失败: ' + (err.message || ''))
  }).finally(() => {
    loading.value = false
  })
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.materialNo = ''
  searchForm.materialName = ''
  searchForm.warehouseName = ''
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

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container { width: 100%; }
.search-card { margin-bottom: 20px; }
.table-card { margin-bottom: 20px; }
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
