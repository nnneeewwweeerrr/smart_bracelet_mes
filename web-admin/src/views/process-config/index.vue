<template>
  <div class="process-config">
    <div class="left-panel">
      <div class="panel-header">
        <span>产品列表</span>
      </div>
      <div class="product-search">
        <el-input v-model="productKeyword" placeholder="搜索产品" clearable size="small" />
      </div>
      <div class="product-list">
        <div
          v-for="product in filteredProducts"
          :key="product.id"
          class="product-item"
          :class="{ active: selectedProductId === product.id }"
          @click="handleSelectProduct(product)"
        >
          <div class="product-name">{{ product.name }}</div>
          <div class="product-code">{{ product.code }}</div>
        </div>
        <div v-if="filteredProducts.length === 0" class="empty-tip">
          <el-empty description="暂无产品" :image-size="60" />
        </div>
      </div>
    </div>

    <div class="right-panel">
      <div class="panel-header">
        <span>工艺路线 - {{ currentProductName }}</span>
        <div class="header-actions">
          <el-button type="primary" size="small" :disabled="!selectedProductId" @click="handleAdd">
            新增工序
          </el-button>
        </div>
      </div>

      <div v-if="selectedProductId" class="process-list">
        <div
          v-for="(item, index) in processList"
          :key="item.id"
          class="process-item"
        >
          <div class="process-order">{{ index + 1 }}</div>
          <div class="process-info">
            <div class="process-name">{{ item.name }}</div>
            <div class="process-desc">{{ item.description || '暂无描述' }}</div>
          </div>
          <div class="process-actions">
            <el-button size="small" :disabled="index === 0" @click="handleMoveUp(index)">
              <el-icon><Top /></el-icon>
            </el-button>
            <el-button size="small" :disabled="index === processList.length - 1" @click="handleMoveDown(index)">
              <el-icon><Bottom /></el-icon>
            </el-button>
            <el-button size="small" type="primary" @click="handleEdit(item)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(item)">删除</el-button>
          </div>
        </div>
        <div v-if="processList.length === 0" class="empty-tip">
          <el-empty description="暂无工序，点击上方按钮新增" :image-size="80" />
        </div>
      </div>
      <div v-else class="empty-tip">
        <el-empty description="请从左侧选择产品" :image-size="100" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑工序' : '新增工序'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="工序名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入工序名称" />
        </el-form-item>
        <el-form-item label="工序编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入工序编码" />
        </el-form-item>
        <el-form-item label="排序号" prop="sort">
          <el-input-number v-model="form.sort" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入工序描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Top, Bottom } from '@element-plus/icons-vue'
import { getList as getProductList } from '../../api/product'
import { getByProductId, save, update, remove } from '../../api/process'

const productList = ref([])
const productKeyword = ref('')
const selectedProductId = ref(null)
const processList = ref([])
const loading = ref(false)

const filteredProducts = computed(() => {
  if (!productKeyword.value) return productList.value
  const kw = productKeyword.value.toLowerCase()
  return productList.value.filter(p =>
    p.name?.toLowerCase().includes(kw) || p.code?.toLowerCase().includes(kw)
  )
})

const currentProductName = computed(() => {
  const p = productList.value.find(item => item.id === selectedProductId.value)
  return p ? p.name : '请选择产品'
})

const fetchProducts = async () => {
  try {
    const res = await getProductList({})
    productList.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const handleSelectProduct = async (product) => {
  selectedProductId.value = product.id
  await fetchProcessList()
}

const fetchProcessList = async () => {
  if (!selectedProductId.value) return
  loading.value = true
  try {
    const res = await getByProductId(selectedProductId.value)
    processList.value = (res.data || []).sort((a, b) => (a.sort || 0) - (b.sort || 0))
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  productId: null,
  name: '',
  code: '',
  sort: 1,
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入工序名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入工序编码', trigger: 'blur' }]
}

const resetForm = () => {
  form.id = null
  form.productId = selectedProductId.value
  form.name = ''
  form.code = ''
  form.sort = processList.value.length + 1
  form.description = ''
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    form.productId = selectedProductId.value
    if (form.id) {
      await update(form)
      ElMessage.success('修改成功')
    } else {
      await save(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchProcessList()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除工序「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await remove(row.id)
      ElMessage.success('删除成功')
      fetchProcessList()
    }).catch(() => {})
}

const handleMoveUp = (index) => {
  if (index === 0) return
  const list = [...processList.value]
  const temp = list[index]
  list[index] = list[index - 1]
  list[index - 1] = temp
  list.forEach((item, i) => {
    item.sort = i + 1
  })
  processList.value = list
  updateSort(list[index])
  updateSort(list[index - 1])
}

const handleMoveDown = (index) => {
  if (index === processList.value.length - 1) return
  const list = [...processList.value]
  const temp = list[index]
  list[index] = list[index + 1]
  list[index + 1] = temp
  list.forEach((item, i) => {
    item.sort = i + 1
  })
  processList.value = list
  updateSort(list[index])
  updateSort(list[index + 1])
}

const updateSort = async (item) => {
  try {
    await update({ id: item.id, sort: item.sort })
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.process-config {
  display: flex;
  height: calc(100vh - 120px);
  gap: 16px;
}
.left-panel {
  width: 260px;
  background: #fff;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.right-panel {
  flex: 1;
  background: #fff;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.panel-header {
  padding: 14px 16px;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
  font-size: 15px;
  color: #303133;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.product-search {
  padding: 12px;
  border-bottom: 1px solid #f0f2f5;
}
.product-list {
  flex: 1;
  overflow-y: auto;
}
.product-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: background 0.2s;
}
.product-item:hover {
  background: #f5f7fa;
}
.product-item.active {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}
.product-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}
.product-code {
  font-size: 12px;
  color: #909399;
}
.process-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}
.process-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin-bottom: 12px;
  background: #fafafa;
  transition: all 0.2s;
}
.process-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.process-order {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  margin-right: 16px;
  flex-shrink: 0;
}
.process-info {
  flex: 1;
}
.process-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}
.process-desc {
  font-size: 13px;
  color: #909399;
}
.process-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
.empty-tip {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
