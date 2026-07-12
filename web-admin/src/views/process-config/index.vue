<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true">
        <el-form-item label="工艺编号">
          <el-input v-model="searchForm.processNo" placeholder="请输入工艺编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="工艺名称">
          <el-input v-model="searchForm.processName" placeholder="请输入工艺名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入产品名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="状态">
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
        <span class="table-title">工艺配置列表</span>
        <div class="header-btns">
          <el-button type="primary" plain @click="handleAskAi">
            <el-icon><ChatDotRound /></el-icon>
            问AI
          </el-button>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增工艺
          </el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="processNo" label="工艺编号" width="140" />
        <el-table-column prop="processName" label="工艺名称" min-width="160" />
        <el-table-column prop="productName" label="适用产品" min-width="180" />
        <el-table-column prop="version" label="版本号" width="100" align="center" />
        <el-table-column prop="processCount" label="工序数量" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
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
      :title="isView ? '工艺详情' : (isEdit ? '编辑工艺' : '新增工艺')"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="processFormRef"
        :model="processForm"
        :rules="processRules"
        label-width="100px"
        :disabled="isView"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工艺编号" prop="processNo">
              <el-input v-model="processForm.processNo" placeholder="请输入工艺编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工艺名称" prop="processName">
              <el-input v-model="processForm.processName" placeholder="请输入工艺名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="适用产品" prop="productId">
              <el-select v-model="processForm.productId" placeholder="请选择产品" style="width: 100%">
                <el-option
                  v-for="item in productList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本号" prop="version">
              <el-input v-model="processForm.version" placeholder="如：V1.0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="工艺描述" prop="description">
          <el-input v-model="processForm.description" type="textarea" :rows="2" placeholder="请输入工艺描述" />
        </el-form-item>

        <el-tabs v-model="activeTab" class="config-tabs">
          <el-tab-pane label="工序配置" name="process">
            <div v-if="!isView" class="process-toolbar">
              <el-button type="primary" size="small" @click="handleAddProcess">
                <el-icon><Plus /></el-icon>
                添加工序
              </el-button>
              <span class="process-tip">提示：拖拽工序行可调整顺序</span>
            </div>

            <el-table
              :data="processForm.processList"
              border
              stripe
              style="width: 100%"
              row-key="id"
              :key="tableKey"
            >
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column prop="processName" label="工序名称" min-width="140">
                <template #default="{ row }">
                  <el-input v-if="!isView" v-model="row.processName" size="small" placeholder="请输入工序名称" />
                  <span v-else>{{ row.processName }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="equipmentId" label="生产设备" width="180">
                <template #default="{ row }">
                  <el-select v-if="!isView" v-model="row.equipmentId" size="small" placeholder="请选择设备" style="width: 100%">
                    <el-option
                      v-for="item in equipmentList"
                      :key="item.id"
                      :label="item.name"
                      :value="item.id"
                    />
                  </el-select>
                  <span v-else>{{ row.equipmentName }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="standardTime" label="标准工时(分钟)" width="140" align="center">
                <template #default="{ row }">
                  <el-input-number
                    v-if="!isView"
                    v-model="row.standardTime"
                    :min="1"
                    :max="9999"
                    size="small"
                    style="width: 100%"
                  />
                  <span v-else>{{ row.standardTime }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="sortOrder" label="排序" width="80" align="center">
                <template #default="{ row, $index }">
                  <span>{{ $index + 1 }}</span>
                </template>
              </el-table-column>
              <el-table-column v-if="!isView" label="操作" width="120" align="center">
                <template #default="{ row, $index }">
                  <el-button
                    type="primary"
                    link
                    size="small"
                    :disabled="$index === 0"
                    @click="handleMoveUp($index)"
                  >
                    上移
                  </el-button>
                  <el-button
                    type="danger"
                    link
                    size="small"
                    @click="handleDeleteProcess($index)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="SOP配置" name="sop">
            <div v-loading="sopLoading">
              <!-- SOP 基本信息 -->
              <div class="sop-section-title">
                <el-icon><Document /></el-icon>
                SOP基本信息
              </div>
              <el-form :model="sopForm" label-width="100px" :disabled="isView" class="sop-form">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="SOP名称">
                      <el-input v-model="sopForm.sopName" placeholder="请输入SOP名称" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="版本号">
                      <el-input v-model="sopForm.version" placeholder="如：V1.0" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item label="设备要求">
                  <el-input v-model="sopForm.equipmentRequire" placeholder="请输入设备要求" />
                </el-form-item>
                <el-form-item label="注意事项">
                  <el-input
                    v-model="sopForm.precautions"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入注意事项"
                  />
                </el-form-item>
              </el-form>

              <!-- 操作步骤 -->
              <div class="sop-section-title">
                <el-icon><Tickets /></el-icon>
                操作步骤
                <el-button
                  v-if="!isView"
                  type="primary"
                  size="small"
                  plain
                  class="sop-section-btn"
                  @click="handleAddStep"
                >
                  <el-icon><Plus /></el-icon>
                  添加步骤
                </el-button>
              </div>
              <div class="sop-step-list">
                <div v-for="(step, index) in sopForm.stepList" :key="index" class="sop-step-item">
                  <div class="sop-step-index">{{ index + 1 }}</div>
                  <el-input
                    v-model="step.content"
                    :disabled="isView"
                    placeholder="请输入操作步骤内容"
                    class="sop-step-input"
                  />
                  <div v-if="!isView" class="sop-step-ops">
                    <el-button
                      type="primary"
                      link
                      size="small"
                      :disabled="index === 0"
                      @click="handleMoveStep(index, -1)"
                    >
                      上移
                    </el-button>
                    <el-button
                      type="primary"
                      link
                      size="small"
                      :disabled="index === sopForm.stepList.length - 1"
                      @click="handleMoveStep(index, 1)"
                    >
                      下移
                    </el-button>
                    <el-button
                      type="danger"
                      link
                      size="small"
                      @click="handleDeleteStep(index)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
                <el-empty v-if="sopForm.stepList.length === 0" description="暂无操作步骤" :image-size="60" />
              </div>

              <!-- 工艺参数标准 -->
              <div class="sop-section-title">
                <el-icon><DataLine /></el-icon>
                工艺参数标准
                <el-button
                  v-if="!isView"
                  type="primary"
                  size="small"
                  plain
                  class="sop-section-btn"
                  @click="handleAddParam"
                >
                  <el-icon><Plus /></el-icon>
                  添加参数
                </el-button>
              </div>
              <el-table :data="sopParamList" border stripe size="small" style="width: 100%">
                <el-table-column type="index" label="序号" width="55" align="center" />
                <el-table-column prop="paramName" label="参数名称" min-width="120">
                  <template #default="{ row }">
                    <el-input v-if="!isView" v-model="row.paramName" size="small" placeholder="参数名称" />
                    <span v-else>{{ row.paramName }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="paramCode" label="编码" width="110">
                  <template #default="{ row }">
                    <el-input v-if="!isView" v-model="row.paramCode" size="small" placeholder="编码" />
                    <span v-else>{{ row.paramCode }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="unit" label="单位" width="80">
                  <template #default="{ row }">
                    <el-input v-if="!isView" v-model="row.unit" size="small" placeholder="单位" />
                    <span v-else>{{ row.unit }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="minValue" label="最小值" width="100" align="center">
                  <template #default="{ row }">
                    <el-input-number v-if="!isView" v-model="row.minValue" size="small" :controls="false" style="width: 100%" />
                    <span v-else>{{ row.minValue }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="maxValue" label="最大值" width="100" align="center">
                  <template #default="{ row }">
                    <el-input-number v-if="!isView" v-model="row.maxValue" size="small" :controls="false" style="width: 100%" />
                    <span v-else>{{ row.maxValue }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="targetValue" label="目标值" width="100" align="center">
                  <template #default="{ row }">
                    <el-input-number v-if="!isView" v-model="row.targetValue" size="small" :controls="false" style="width: 100%" />
                    <span v-else>{{ row.targetValue }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="required" label="必检" width="70" align="center">
                  <template #default="{ row }">
                    <el-switch v-if="!isView" v-model="row.required" :active-value="1" :inactive-value="0" size="small" />
                    <el-tag v-else :type="row.required === 1 ? 'danger' : 'info'" size="small">
                      {{ row.required === 1 ? '是' : '否' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column v-if="!isView" label="操作" width="80" align="center">
                  <template #default="{ $index }">
                    <el-button type="danger" link size="small" @click="handleDeleteParam($index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>

              <!-- 物料BOM配置 -->
              <div class="sop-section-title">
                <el-icon><Box /></el-icon>
                物料BOM配置
                <el-button
                  v-if="!isView"
                  type="primary"
                  size="small"
                  plain
                  class="sop-section-btn"
                  @click="handleAddBom"
                >
                  <el-icon><Plus /></el-icon>
                  添加物料
                </el-button>
              </div>
              <el-table :data="sopBomList" border stripe size="small" style="width: 100%">
                <el-table-column type="index" label="序号" width="55" align="center" />
                <el-table-column prop="materialId" label="物料" min-width="200">
                  <template #default="{ row }">
                    <el-select
                      v-if="!isView"
                      v-model="row.materialId"
                      size="small"
                      placeholder="请选择物料"
                      filterable
                      style="width: 100%"
                      @change="handleBomMaterialChange(row)"
                    >
                      <el-option
                        v-for="item in materialList"
                        :key="item.id"
                        :label="item.materialCode + ' / ' + item.materialName"
                        :value="item.id"
                      />
                    </el-select>
                    <span v-else>{{ row.materialNo }} / {{ row.materialName }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="unit" label="单位" width="80" align="center">
                  <template #default="{ row }">
                    <span>{{ row.unit }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="quantity" label="用量" width="120" align="center">
                  <template #default="{ row }">
                    <el-input-number v-if="!isView" v-model="row.quantity" size="small" :min="0" :precision="3" :controls="false" style="width: 100%" />
                    <span v-else>{{ row.quantity }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="required" label="必检" width="70" align="center">
                  <template #default="{ row }">
                    <el-switch v-if="!isView" v-model="row.required" :active-value="1" :inactive-value="0" size="small" />
                    <el-tag v-else :type="row.required === 1 ? 'danger' : 'info'" size="small">
                      {{ row.required === 1 ? '是' : '否' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column v-if="!isView" label="操作" width="80" align="center">
                  <template #default="{ $index }">
                    <el-button type="danger" link size="small" @click="handleDeleteBom($index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>

              <div v-if="!isView" class="sop-save-bar">
                <el-button type="primary" :loading="sopSaving" @click="handleSaveSop">
                  <el-icon><Check /></el-icon>
                  保存SOP配置
                </el-button>
                <span class="sop-save-tip">SOP配置独立保存，不影响工艺基本信息</span>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ isView ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getSopByProcessId, saveSop, updateSop,
  saveParam, deleteParam,
  saveBom, deleteBom
} from '../../api/sop'
import { getMaterialList } from '../../api/material'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false)
const processFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableKey = ref(0)
const activeTab = ref('process')

// SOP 相关状态
const sopLoading = ref(false)
const sopSaving = ref(false)
const materialList = ref([])
const sopParamList = ref([])
const sopBomList = ref([])
const sopForm = reactive({
  id: null,
  processId: null,
  sopName: '',
  version: '',
  equipmentRequire: '',
  precautions: '',
  stepList: []
})

const searchForm = reactive({
  processNo: '',
  processName: '',
  productName: '',
  status: ''
})

const processForm = reactive({
  id: null,
  processNo: '',
  processName: '',
  productId: null,
  productName: '',
  version: '',
  description: '',
  status: 1,
  processList: []
})

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

const processRules = {
  processNo: [
    { required: true, message: '请输入工艺编号', trigger: 'blur' }
  ],
  processName: [
    { required: true, message: '请输入工艺名称', trigger: 'blur' }
  ],
  productId: [
    { required: true, message: '请选择适用产品', trigger: 'change' }
  ],
  version: [
    { required: true, message: '请输入版本号', trigger: 'blur' }
  ]
}

const productList = ref([
  { id: 1, name: '智能健康手环 Pro' },
  { id: 2, name: '智能运动手环 Lite' },
  { id: 3, name: '智能手表 Max' }
])

const equipmentList = ref([
  { id: 1, name: 'SMT贴片机01' },
  { id: 2, name: 'SMT贴片机02' },
  { id: 3, name: '回流焊机01' },
  { id: 4, name: '回流焊机02' },
  { id: 5, name: '自动组装线01' },
  { id: 6, name: '自动组装线02' },
  { id: 7, name: '测试台01' },
  { id: 8, name: '测试台02' },
  { id: 9, name: '自动包装机01' },
  { id: 10, name: '自动包装机02' }
])

const mockData = ref([
  {
    id: 1,
    processNo: 'GY-2024001',
    processName: '智能健康手环 Pro 标准工艺',
    productId: 1,
    productName: '智能健康手环 Pro',
    version: 'V1.0',
    description: '智能健康手环 Pro 的标准生产工艺流程',
    status: 1,
    processCount: 5,
    createTime: '2024-01-01 10:00:00',
    processList: [
      { id: 1, processName: 'SMT贴片', equipmentId: 1, equipmentName: 'SMT贴片机01', standardTime: 30, sortOrder: 1 },
      { id: 2, processName: '回流焊接', equipmentId: 3, equipmentName: '回流焊机01', standardTime: 15, sortOrder: 2 },
      { id: 3, processName: '组装', equipmentId: 5, equipmentName: '自动组装线01', standardTime: 45, sortOrder: 3 },
      { id: 4, processName: '测试', equipmentId: 7, equipmentName: '测试台01', standardTime: 20, sortOrder: 4 },
      { id: 5, processName: '包装', equipmentId: 9, equipmentName: '自动包装机01', standardTime: 10, sortOrder: 5 }
    ]
  },
  {
    id: 2,
    processNo: 'GY-2024002',
    processName: '智能运动手环 Lite 标准工艺',
    productId: 2,
    productName: '智能运动手环 Lite',
    version: 'V1.0',
    description: '智能运动手环 Lite 的标准生产工艺流程',
    status: 1,
    processCount: 5,
    createTime: '2024-01-02 14:30:00',
    processList: [
      { id: 1, processName: 'SMT贴片', equipmentId: 2, equipmentName: 'SMT贴片机02', standardTime: 25, sortOrder: 1 },
      { id: 2, processName: '回流焊接', equipmentId: 4, equipmentName: '回流焊机02', standardTime: 12, sortOrder: 2 },
      { id: 3, processName: '组装', equipmentId: 6, equipmentName: '自动组装线02', standardTime: 35, sortOrder: 3 },
      { id: 4, processName: '测试', equipmentId: 8, equipmentName: '测试台02', standardTime: 15, sortOrder: 4 },
      { id: 5, processName: '包装', equipmentId: 10, equipmentName: '自动包装机02', standardTime: 8, sortOrder: 5 }
    ]
  },
  {
    id: 3,
    processNo: 'GY-2024003',
    processName: '智能手表 Max 标准工艺',
    productId: 3,
    productName: '智能手表 Max',
    version: 'V1.2',
    description: '智能手表 Max 的标准生产工艺流程，含老化测试',
    status: 1,
    processCount: 6,
    createTime: '2024-01-05 09:00:00',
    processList: [
      { id: 1, processName: 'SMT贴片', equipmentId: 1, equipmentName: 'SMT贴片机01', standardTime: 40, sortOrder: 1 },
      { id: 2, processName: '回流焊接', equipmentId: 3, equipmentName: '回流焊机01', standardTime: 20, sortOrder: 2 },
      { id: 3, processName: '组装', equipmentId: 5, equipmentName: '自动组装线01', standardTime: 60, sortOrder: 3 },
      { id: 4, processName: '老化测试', equipmentId: 7, equipmentName: '测试台01', standardTime: 120, sortOrder: 4 },
      { id: 5, processName: '功能测试', equipmentId: 7, equipmentName: '测试台01', standardTime: 30, sortOrder: 5 },
      { id: 6, processName: '包装', equipmentId: 9, equipmentName: '自动包装机01', standardTime: 15, sortOrder: 6 }
    ]
  },
  {
    id: 4,
    processNo: 'GY-2024004',
    processName: '智能健康手环 Pro 简化工艺',
    productId: 1,
    productName: '智能健康手环 Pro',
    version: 'V0.9',
    description: '试产阶段简化工艺',
    status: 0,
    processCount: 4,
    createTime: '2024-01-08 16:00:00',
    processList: [
      { id: 1, processName: 'SMT贴片', equipmentId: 1, equipmentName: 'SMT贴片机01', standardTime: 30, sortOrder: 1 },
      { id: 2, processName: '回流焊接', equipmentId: 3, equipmentName: '回流焊机01', standardTime: 15, sortOrder: 2 },
      { id: 3, processName: '手工组装', equipmentId: null, equipmentName: '-', standardTime: 90, sortOrder: 3 },
      { id: 4, processName: '测试包装', equipmentId: 7, equipmentName: '测试台01', standardTime: 25, sortOrder: 4 }
    ]
  }
])

const tableData = ref([])

onMounted(() => {
  fetchData()
})

function fetchData() {
  loading.value = true
  setTimeout(() => {
    let filtered = [...mockData.value]
    if (searchForm.processNo) {
      filtered = filtered.filter(item => item.processNo.includes(searchForm.processNo))
    }
    if (searchForm.processName) {
      filtered = filtered.filter(item => item.processName.includes(searchForm.processName))
    }
    if (searchForm.productName) {
      filtered = filtered.filter(item => item.productName.includes(searchForm.productName))
    }
    if (searchForm.status !== '') {
      filtered = filtered.filter(item => item.status === searchForm.status)
    }
    total.value = filtered.length
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    tableData.value = filtered.slice(start, end)
    loading.value = false
  }, 300)
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.processNo = ''
  searchForm.processName = ''
  searchForm.productName = ''
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
  isView.value = false
  resetForm()
  resetSopState()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  isView.value = false
  processForm.id = row.id
  processForm.processNo = row.processNo
  processForm.processName = row.processName
  processForm.productId = row.productId
  processForm.productName = row.productName
  processForm.version = row.version
  processForm.description = row.description
  processForm.status = row.status
  processForm.processList = JSON.parse(JSON.stringify(row.processList))
  activeTab.value = 'process'
  loadSopData(row.id)
  dialogVisible.value = true
}

function handleView(row) {
  isEdit.value = false
  isView.value = true
  processForm.id = row.id
  processForm.processNo = row.processNo
  processForm.processName = row.processName
  processForm.productId = row.productId
  processForm.productName = row.productName
  processForm.version = row.version
  processForm.description = row.description
  processForm.status = row.status
  processForm.processList = JSON.parse(JSON.stringify(row.processList))
  activeTab.value = 'process'
  loadSopData(row.id)
  dialogVisible.value = true
}

function handleAskAi() {
  let question
  if (processForm.id) {
    const processNames = processForm.processList.map(p => p.processName).filter(Boolean).join('、')
    question = `请介绍工艺「${processForm.processName}」（编号${processForm.processNo}，版本${processForm.version}）的SOP操作规范。该工艺适用于产品「${processForm.productName}」，包含工序：${processNames || '暂无'}。请详细说明各工序的SOP标准、操作步骤和注意事项。`
  } else {
    question = '请介绍当前工艺配置中各工序的SOP操作规范、标准工时和注意事项。'
  }
  window.dispatchEvent(new CustomEvent('ask-ai', { detail: { question } }))
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除工艺「${row.processName}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = mockData.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      mockData.value.splice(index, 1)
    }
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

function handleToggleStatus(row) {
  const action = row.status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${action}工艺「${row.processName}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const item = mockData.value.find(i => i.id === row.id)
    if (item) {
      item.status = item.status === 1 ? 0 : 1
    }
    ElMessage.success(`${action}成功`)
    fetchData()
  }).catch(() => {})
}

function handleAddProcess() {
  const maxId = Math.max(...processForm.processList.map(p => p.id), 0)
  processForm.processList.push({
    id: maxId + 1,
    processName: '',
    equipmentId: null,
    equipmentName: '',
    standardTime: 30,
    sortOrder: processForm.processList.length + 1
  })
  tableKey.value++
}

function handleDeleteProcess(index) {
  processForm.processList.splice(index, 1)
  processForm.processList.forEach((p, i) => {
    p.sortOrder = i + 1
  })
  tableKey.value++
}

function handleMoveUp(index) {
  if (index > 0) {
    const temp = processForm.processList[index]
    processForm.processList[index] = processForm.processList[index - 1]
    processForm.processList[index - 1] = temp
    processForm.processList.forEach((p, i) => {
      p.sortOrder = i + 1
    })
    tableKey.value++
  }
}

function resetForm() {
  processForm.id = null
  processForm.processNo = ''
  processForm.processName = ''
  processForm.productId = null
  processForm.productName = ''
  processForm.version = ''
  processForm.description = ''
  processForm.status = 1
  processForm.processList = []
  if (processFormRef.value) {
    processFormRef.value.resetFields()
  }
}

async function handleSubmit() {
  if (!processFormRef.value) return
  
  try {
    await processFormRef.value.validate()
    
    if (processForm.processList.length === 0) {
      ElMessage.warning('请至少添加工序')
      return
    }
    
    const hasEmptyName = processForm.processList.some(p => !p.processName)
    if (hasEmptyName) {
      ElMessage.warning('请填写所有工序名称')
      return
    }
    
    submitLoading.value = true
    
    setTimeout(() => {
      const product = productList.value.find(p => p.id === processForm.productId)
      const processListWithEquipName = processForm.processList.map(p => {
        const equip = equipmentList.value.find(e => e.id === p.equipmentId)
        return {
          ...p,
          equipmentName: equip ? equip.name : '-'
        }
      })
      
      if (isEdit.value) {
        const index = mockData.value.findIndex(item => item.id === processForm.id)
        if (index !== -1) {
          mockData.value[index] = {
            ...mockData.value[index],
            processNo: processForm.processNo,
            processName: processForm.processName,
            productId: processForm.productId,
            productName: product ? product.name : '',
            version: processForm.version,
            description: processForm.description,
            processCount: processForm.processList.length,
            processList: processListWithEquipName
          }
        }
        ElMessage.success('编辑成功')
      } else {
        const maxId = Math.max(...mockData.value.map(item => item.id), 0)
        const newProcess = {
          id: maxId + 1,
          processNo: processForm.processNo,
          processName: processForm.processName,
          productId: processForm.productId,
          productName: product ? product.name : '',
          version: processForm.version,
          description: processForm.description,
          status: 1,
          processCount: processForm.processList.length,
          createTime: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
          processList: processListWithEquipName
        }
        mockData.value.unshift(newProcess)
        ElMessage.success('新增成功')
      }
      
      dialogVisible.value = false
      submitLoading.value = false
      fetchData()
    }, 300)
  } catch (error) {
    submitLoading.value = false
  }
}

// ============ SOP 配置相关 ============

function resetSopState() {
  sopForm.id = null
  sopForm.processId = null
  sopForm.sopName = ''
  sopForm.version = ''
  sopForm.equipmentRequire = ''
  sopForm.precautions = ''
  sopForm.stepList = []
  sopParamList.value = []
  sopBomList.value = []
}

async function loadMaterialList() {
  if (materialList.value.length > 0) return
  try {
    const res = await getMaterialList({ pageNum: 1, pageSize: 1000 })
    materialList.value = res.data?.records || res.data || []
  } catch (e) {
    materialList.value = []
  }
}

async function loadSopData(processId) {
  resetSopState()
  sopForm.processId = processId
  sopLoading.value = true
  try {
    await loadMaterialList()
    // 后端 /sop/process/{processId} 一次返回：{process, sop, paramStandards, materialBom}
    const sopRes = await getSopByProcessId(processId).catch(() => null)
    const data = sopRes?.data || {}
    const sopData = data.sop
    if (sopData) {
      sopForm.id = sopData.id
      sopForm.processId = sopData.processId || processId
      sopForm.sopName = sopData.sopName || ''
      sopForm.version = sopData.sopVersion || ''
      sopForm.equipmentRequire = sopData.equipmentRequirement || ''
      sopForm.precautions = sopData.precautions || ''
      sopForm.stepList = parseSteps(sopData.operationSteps)
    }
    // 参数标准：后端字段 isRequired，前端表格用 required
    sopParamList.value = (data.paramStandards || []).map(p => ({
      ...p,
      required: p.isRequired
    }))
    // 物料BOM：后端返回 materialCode/materialName/unit，前端用 materialNo
    sopBomList.value = (data.materialBom || []).map(b => ({
      ...b,
      materialNo: b.materialCode,
      materialName: b.materialName,
      unit: b.unit,
      required: b.isRequired
    }))
  } catch (e) {
    // 接口未就绪时静默处理
  } finally {
    sopLoading.value = false
  }
}

function parseSteps(steps) {
  if (!steps) return []
  if (Array.isArray(steps)) {
    return steps.map((s, i) => ({ sortOrder: s.sortOrder || i + 1, content: typeof s === 'string' ? s : (s.content || '') }))
  }
  if (typeof steps === 'string') {
    // 后端 operationSteps 存储为 JSON 数组字符串，如 ["步骤1","步骤2"]
    const trimmed = steps.trim()
    if (trimmed.startsWith('[')) {
      try {
        const arr = JSON.parse(trimmed)
        return arr.map((s, i) => ({ sortOrder: i + 1, content: typeof s === 'string' ? s : (s.content || '') }))
      } catch (e) {
        // JSON 解析失败，回退到换行分割
      }
    }
    return steps.split('\n').filter(s => s.trim()).map((s, i) => ({ sortOrder: i + 1, content: s }))
  }
  return []
}

function handleAddStep() {
  sopForm.stepList.push({
    sortOrder: sopForm.stepList.length + 1,
    content: ''
  })
}

function handleDeleteStep(index) {
  sopForm.stepList.splice(index, 1)
  sopForm.stepList.forEach((s, i) => { s.sortOrder = i + 1 })
}

function handleMoveStep(index, direction) {
  const target = index + direction
  if (target < 0 || target >= sopForm.stepList.length) return
  const temp = sopForm.stepList[index]
  sopForm.stepList[index] = sopForm.stepList[target]
  sopForm.stepList[target] = temp
  sopForm.stepList.forEach((s, i) => { s.sortOrder = i + 1 })
}

function handleAddParam() {
  sopParamList.value.push({
    paramName: '',
    paramCode: '',
    unit: '',
    minValue: 0,
    maxValue: 0,
    targetValue: 0,
    required: 1
  })
}

function handleDeleteParam(index) {
  sopParamList.value.splice(index, 1)
}

function handleAddBom() {
  sopBomList.value.push({
    materialId: null,
    materialNo: '',
    materialName: '',
    unit: '',
    quantity: 1,
    required: 0
  })
}

function handleDeleteBom(index) {
  sopBomList.value.splice(index, 1)
}

function handleBomMaterialChange(row) {
  const material = materialList.value.find(m => m.id === row.materialId)
  if (material) {
    row.materialNo = material.materialCode
    row.materialName = material.materialName
    row.unit = material.unit
  }
}

async function handleSaveSop() {
  if (!processForm.id) {
    ElMessage.warning('请先保存工艺基本信息后再配置SOP')
    return
  }
  if (!sopForm.sopName) {
    ElMessage.warning('请输入SOP名称')
    return
  }
  sopSaving.value = true
  try {
    const stepsText = JSON.stringify(sopForm.stepList.map(s => s.content).filter(c => c))
    const sopPayload = {
      id: sopForm.id,
      processId: processForm.id,
      sopName: sopForm.sopName,
      sopVersion: sopForm.version,
      equipmentRequirement: sopForm.equipmentRequire,
      precautions: sopForm.precautions,
      operationSteps: stepsText,
      status: 1
    }
    let savedSop
    if (sopForm.id) {
      savedSop = await updateSop(sopPayload)
    } else {
      savedSop = await saveSop(sopPayload)
      if (savedSop.data?.id) {
        sopForm.id = savedSop.data.id
      }
    }

    // 保存参数标准（前端 required → 后端 isRequired）
    const paramSaves = sopParamList.value.map(p => {
      const { required, ...rest } = p
      return saveParam({ ...rest, processId: processForm.id, isRequired: required })
    })
    // 保存物料BOM（前端 required → 后端 isRequired）
    const bomSaves = sopBomList.value.map(b => {
      const { required, materialNo, materialName, ...rest } = b
      return saveBom({ ...rest, processId: processForm.id, isRequired: required })
    })
    await Promise.all([...paramSaves, ...bomSaves])

    ElMessage.success('SOP配置保存成功')
    // 重新加载
    loadSopData(processForm.id)
  } catch (e) {
    // 错误已由 request 拦截器提示
  } finally {
    sopSaving.value = false
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

.header-btns {
  display: flex;
  align-items: center;
  gap: 10px;
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

.divider-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: bold;
  color: #303133;
}

.process-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.process-tip {
  font-size: 12px;
  color: #909399;
}

.config-tabs {
  margin-top: 8px;
}

.sop-section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin: 18px 0 12px;
}

.sop-section-title:first-child {
  margin-top: 0;
}

.sop-section-btn {
  margin-left: 12px;
}

.sop-form {
  margin-bottom: 8px;
}

.sop-step-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sop-step-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sop-step-index {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0ea5e9, #0369a1);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: bold;
  flex-shrink: 0;
}

.sop-step-input {
  flex: 1;
}

.sop-step-ops {
  flex-shrink: 0;
  display: flex;
  gap: 4px;
}

.sop-save-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px dashed #e4e7ed;
}

.sop-save-tip {
  font-size: 12px;
  color: #909399;
}
</style>
