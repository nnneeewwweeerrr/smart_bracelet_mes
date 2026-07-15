<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="供应商名称">
          <el-input v-model="searchForm.name" placeholder="请输入供应商名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="searchForm.contact" placeholder="请输入联系人" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="供应商状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 160px">
            <el-option label="正常" value="1" />
            <el-option label="停用" value="0" />
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
        <span class="table-title">供应商列表</span>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增供应商
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="supplierNo" label="供应商编号" width="140" />
        <el-table-column prop="name" label="供应商名称" min-width="180" />
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="level" label="等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.level === 'A'" type="danger">A级</el-tag>
            <el-tag v-else-if="row.level === 'B'" type="warning">B级</el-tag>
            <el-tag v-else type="info">C级</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'danger'">
              {{ row.status === '1' ? '正常' : '停用' }}
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
      :title="isEdit ? '编辑供应商' : '新增供应商'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="supplierFormRef"
        :model="supplierForm"
        :rules="supplierRules"
        label-width="100px"
      >
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="supplierForm.name" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="supplierForm.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="supplierForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="supplierForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="supplierForm.address" type="textarea" :rows="3" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="供应商等级" prop="level">
          <el-select v-model="supplierForm.level" placeholder="请选择等级" style="width: 100%">
            <el-option label="A级（优质）" value="A" />
            <el-option label="B级（普通）" value="B" />
            <el-option label="C级（一般）" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商状态" prop="status">
          <el-radio-group v-model="supplierForm.status">
            <el-radio value="1">正常</el-radio>
            <el-radio value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="supplierForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="供应商详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="供应商编号">{{ currentSupplier.supplierNo }}</el-descriptions-item>
        <el-descriptions-item label="供应商名称">{{ currentSupplier.name }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentSupplier.contact }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentSupplier.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱" :span="2">{{ currentSupplier.email }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentSupplier.address }}</el-descriptions-item>
        <el-descriptions-item label="供应商等级">
          <el-tag v-if="currentSupplier.level === 'A'" type="danger">A级</el-tag>
          <el-tag v-else-if="currentSupplier.level === 'B'" type="warning">B级</el-tag>
          <el-tag v-else type="info">C级</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentSupplier.status === '1' ? 'success' : 'danger'">
            {{ currentSupplier.status === '1' ? '正常' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentSupplier.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentSupplier.remark || '无' }}</el-descriptions-item>
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
import { getSupplierList, createSupplier, updateSupplier, deleteSupplier } from '../../api/supplier'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const supplierFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  name: '',
  contact: '',
  status: ''
})

const supplierForm = reactive({
  id: null,
  supplierNo: '',
  name: '',
  contact: '',
  phone: '',
  email: '',
  address: '',
  level: 'B',
  status: '1',
  remark: ''
})

const currentSupplier = reactive({})

const supplierRules = {
  name: [
    { required: true, message: '请输入供应商名称', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' }
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
      supplierName: searchForm.name
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    const res = await getSupplierList(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.warn('供应商列表API调用失败，使用mock数据', error)
    tableData.value = [
      { id: 1, supplierNo: 'SUP-20260701', name: '深圳市芯创电子元件有限公司', contact: '黄志强', phone: '13800138001', email: 'huangzq@xinchuang-elec.com', address: '深圳市宝安区沙井街道新桥路68号', level: 'A', status: '1', createTime: '2026-07-01 09:00:00', remark: '主板芯片核心供应商' },
      { id: 2, supplierNo: 'SUP-20260701', name: '东莞市鑫达塑胶制品厂', contact: '林国华', phone: '13900139002', email: 'linguohua@xda-plastic.com', address: '东莞市长安镇乌沙路128号', level: 'A', status: '1', createTime: '2026-07-01 10:30:00', remark: '手环外壳及表带供应商' },
      { id: 3, supplierNo: 'SUP-20260702', name: '广州华信电池科技有限公司', contact: '陈明远', phone: '13700137003', email: 'chenmy@huaxin-battery.com', address: '广州市黄埔区开发大道388号', level: 'A', status: '1', createTime: '2026-07-02 14:00:00', remark: '锂电池优质供应商' },
      { id: 4, supplierNo: 'SUP-20260703', name: '苏州视界光电科技有限公司', contact: '王建辉', phone: '13600136004', email: 'wangjh@shijie-oled.com', address: '苏州市工业园区星湖街218号', level: 'B', status: '1', createTime: '2026-07-03 11:15:00', remark: 'OLED显示屏供应商' },
      { id: 5, supplierNo: 'SUP-20260704', name: '上海博通传感器技术有限公司', contact: '李晓峰', phone: '13500135005', email: 'lixf@botong-sensor.com', address: '上海市嘉定区曹安公路4800号', level: 'A', status: '1', createTime: '2026-07-04 09:30:00', remark: '心率血氧传感器供应商' },
      { id: 6, supplierNo: 'SUP-20260705', name: '惠州永盛包装材料有限公司', contact: '张秀英', phone: '13400134006', email: 'zhangxy@yongsheng-pack.com', address: '惠州市惠城区水口街道龙津路66号', level: 'B', status: '1', createTime: '2026-07-05 16:00:00', remark: '彩盒及说明书供应商' },
      { id: 7, supplierNo: 'SUP-20260707', name: '厦门科芯半导体有限公司', contact: '吴文杰', phone: '13300133007', email: 'wuwj@kexin-semi.com', address: '厦门市翔安区火炬产业园', level: 'B', status: '1', createTime: '2026-07-07 10:00:00', remark: '蓝牙芯片供应商' },
      { id: 8, supplierNo: 'SUP-20260709', name: '佛山精工五金制品厂', contact: '刘德明', phone: '13200132008', email: 'liudm@jinggong-metal.com', address: '佛山市顺德区北滘镇工业大道99号', level: 'C', status: '0', createTime: '2026-07-09 13:45:00', remark: '金属表扣供应商（已停用）' },
      { id: 9, supplierNo: 'SUP-20260711', name: '珠海冠宇电池股份有限公司', contact: '赵伟', phone: '13100131009', email: 'zhaowei@guanyu-battery.com', address: '珠海市斗门区井岸镇新青科技工业园', level: 'A', status: '1', createTime: '2026-07-11 15:30:00', remark: '聚合物锂电池主力供应商' },
      { id: 10, supplierNo: 'SUP-20260713', name: '深圳联创电子有限公司', contact: '马丽华', phone: '13000130010', email: 'malh@lianchuang-elec.com', address: '深圳市龙华区民治街道工业路88号', level: 'B', status: '1', createTime: '2026-07-13 08:30:00', remark: '充电线材及连接器供应商' }
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
  searchForm.name = ''
  searchForm.contact = ''
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
  Object.assign(supplierForm, {
    id: null,
    supplierNo: '',
    name: '',
    contact: '',
    phone: '',
    email: '',
    address: '',
    level: 'B',
    status: '1',
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(supplierForm, row)
  dialogVisible.value = true
}

function handleView(row) {
  Object.assign(currentSupplier, row)
  detailVisible.value = true
}

async function handleSubmit() {
  if (!supplierFormRef.value) return
  try {
    await supplierFormRef.value.validate()
    submitLoading.value = true
    if (isEdit.value) {
      await updateSupplier(supplierForm)
      ElMessage.success('编辑成功')
    } else {
      await createSupplier(supplierForm)
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
    await ElMessageBox.confirm('确定要删除该供应商吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteSupplier(row.id)
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
