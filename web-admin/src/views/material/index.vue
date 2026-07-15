<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="物料编号">
          <el-input v-model="searchForm.materialNo" placeholder="请输入物料编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="物料名称">
          <el-input v-model="searchForm.name" placeholder="请输入物料名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="物料类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 140px">
            <el-option label="原材料" value="raw" />
            <el-option label="半成品" value="semi" />
            <el-option label="辅料" value="aux" />
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
        <span class="table-title">物料列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增物料
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialNo" label="物料编号" width="140" />
        <el-table-column prop="name" label="物料名称" min-width="160" />
        <el-table-column prop="type" label="物料类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'raw'" type="primary">原材料</el-tag>
            <el-tag v-else-if="row.type === 'semi'" type="warning">半成品</el-tag>
            <el-tag v-else type="info">辅料</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="spec" label="规格型号" width="140" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="price" label="单价（元）" width="120" align="right" />
        <el-table-column prop="supplierName" label="供应商" width="160" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'danger'">
              {{ row.status === '1' ? '启用' : '停用' }}
            </el-tag>
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
      :title="isEdit ? '编辑物料' : '新增物料'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="materialFormRef"
        :model="materialForm"
        :rules="materialRules"
        label-width="100px"
      >
        <el-form-item label="物料名称" prop="name">
          <el-input v-model="materialForm.name" placeholder="请输入物料名称" />
        </el-form-item>
        <el-form-item label="物料类型" prop="type">
          <el-select v-model="materialForm.type" placeholder="请选择物料类型" style="width: 100%">
            <el-option label="原材料" value="raw" />
            <el-option label="半成品" value="semi" />
            <el-option label="辅料" value="aux" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格型号" prop="spec">
          <el-input v-model="materialForm.spec" placeholder="请输入规格型号" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="materialForm.unit" placeholder="请输入单位（如：个、kg）" style="width: 50%" />
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number v-model="materialForm.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="materialForm.supplierId" placeholder="请选择供应商" style="width: 100%">
            <el-option label="深圳市电子元件有限公司" :value="1" />
            <el-option label="东莞市塑胶制品厂" :value="2" />
            <el-option label="广州市包装材料公司" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="materialForm.status">
            <el-radio value="1">启用</el-radio>
            <el-radio value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="materialForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="物料详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="物料编号">{{ currentMaterial.materialNo }}</el-descriptions-item>
        <el-descriptions-item label="物料名称">{{ currentMaterial.name }}</el-descriptions-item>
        <el-descriptions-item label="物料类型">
          <el-tag v-if="currentMaterial.type === 'raw'" type="primary">原材料</el-tag>
          <el-tag v-else-if="currentMaterial.type === 'semi'" type="warning">半成品</el-tag>
          <el-tag v-else type="info">辅料</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ currentMaterial.spec }}</el-descriptions-item>
        <el-descriptions-item label="单位">{{ currentMaterial.unit }}</el-descriptions-item>
        <el-descriptions-item label="单价">¥{{ currentMaterial.price }}</el-descriptions-item>
        <el-descriptions-item label="供应商" :span="2">{{ currentMaterial.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentMaterial.status === '1' ? 'success' : 'danger'">
            {{ currentMaterial.status === '1' ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentMaterial.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentMaterial.remark || '无' }}</el-descriptions-item>
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
import { getMaterialList, createMaterial, updateMaterial, deleteMaterial } from '../../api/material'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const materialFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  materialNo: '',
  name: '',
  type: ''
})

const materialForm = reactive({
  id: null,
  materialNo: '',
  name: '',
  type: 'raw',
  spec: '',
  unit: '',
  price: 0,
  supplierId: null,
  supplierName: '',
  status: '1',
  remark: ''
})

const currentMaterial = reactive({})

const materialRules = {
  name: [
    { required: true, message: '请输入物料名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择物料类型', trigger: 'change' }
  ],
  unit: [
    { required: true, message: '请输入单位', trigger: 'blur' }
  ]
}


const tableData = ref([])

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      materialName: searchForm.name,
      materialCode: searchForm.materialNo
    }
    if (searchForm.type) {
      params.type = searchForm.type
    }
    const res = await getMaterialList(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.warn('物料列表API调用失败，使用mock数据', error)
    tableData.value = [
      { id: 1, materialNo: 'MAT-R001', name: 'STM32F103主板芯片', type: 'raw', spec: 'STM32F103C8T6', unit: '个', price: 15.50, supplierId: 1, supplierName: '深圳市芯创电子元件有限公司', status: '1', createTime: '2026-07-01 09:00:00', remark: '手环主控MCU' },
      { id: 2, materialNo: 'MAT-R002', name: '聚合物锂电池', type: 'raw', spec: '3.7V 200mAh', unit: '个', price: 8.00, supplierId: 3, supplierName: '广州华信电池科技有限公司', status: '1', createTime: '2026-07-01 10:30:00', remark: '手环内置电池' },
      { id: 3, materialNo: 'MAT-R003', name: 'ABS塑胶外壳', type: 'raw', spec: 'ABS 白色 注塑', unit: '个', price: 3.50, supplierId: 2, supplierName: '东莞市鑫达塑胶制品厂', status: '1', createTime: '2026-07-02 14:00:00', remark: '手环主体外壳' },
      { id: 4, materialNo: 'MAT-R004', name: '0.96寸OLED显示屏', type: 'raw', spec: '0.96寸 128×64 SPI', unit: '个', price: 12.00, supplierId: 4, supplierName: '苏州视界光电科技有限公司', status: '1', createTime: '2026-07-03 11:15:00', remark: '手环显示模组' },
      { id: 5, materialNo: 'MAT-R005', name: 'MAX30102心率血氧传感器', type: 'raw', spec: 'MAX30102 I2C', unit: '个', price: 20.00, supplierId: 5, supplierName: '上海博通传感器技术有限公司', status: '1', createTime: '2026-07-04 09:30:00', remark: '健康监测核心传感器' },
      { id: 6, materialNo: 'MAT-A001', name: '彩盒包装盒', type: 'aux', spec: '彩盒 白色 120×90×60mm', unit: '个', price: 2.00, supplierId: 6, supplierName: '惠州永盛包装材料有限公司', status: '1', createTime: '2026-07-05 16:00:00', remark: '产品外包装' },
      { id: 7, materialNo: 'MAT-R006', name: '硅胶表带', type: 'raw', spec: '医用硅胶 黑色 M码', unit: '条', price: 5.50, supplierId: 2, supplierName: '东莞市鑫达塑胶制品厂', status: '1', createTime: '2026-07-06 08:45:00', remark: '手环佩戴表带' },
      { id: 8, materialNo: 'MAT-A002', name: 'Type-C充电线', type: 'aux', spec: 'Type-C 20cm 白色', unit: '条', price: 3.00, supplierId: 10, supplierName: '深圳联创电子有限公司', status: '1', createTime: '2026-07-07 10:00:00', remark: '手环充电配件' },
      { id: 9, materialNo: 'MAT-R007', name: 'nRF52832蓝牙芯片', type: 'raw', spec: 'nRF52832 QFN48', unit: '个', price: 18.00, supplierId: 7, supplierName: '厦门科芯半导体有限公司', status: '1', createTime: '2026-07-08 13:20:00', remark: 'BLE5.0通信模块' },
      { id: 10, materialNo: 'MAT-S001', name: '手环PCBA半成品', type: 'semi', spec: 'SH-PCBA-001', unit: '片', price: 58.00, supplierId: 1, supplierName: '深圳市芯创电子元件有限公司', status: '1', createTime: '2026-07-10 15:00:00', remark: 'SMT贴片完成半成品' }
    ]
    total.value = 10
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.materialNo = ''
  searchForm.name = ''
  searchForm.type = ''
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
  Object.assign(materialForm, {
    id: null,
    materialNo: '',
    name: '',
    type: 'raw',
    spec: '',
    unit: '',
    price: 0,
    supplierId: null,
    supplierName: '',
    status: '1',
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(materialForm, row)
  dialogVisible.value = true
}

function handleView(row) {
  Object.assign(currentMaterial, row)
  detailVisible.value = true
}


async function handleSubmit() {
  if (!materialFormRef.value) return
  try {
    await materialFormRef.value.validate()
    submitLoading.value = true
    if (isEdit.value) {
      await updateMaterial(materialForm)
      ElMessage.success('编辑成功')
    } else {
      await createMaterial(materialForm)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    // request interceptor handles error message
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该物料吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteMaterial(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    // catch cancel or request error
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
</style>
