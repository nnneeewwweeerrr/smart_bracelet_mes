<template>
  <div class="page-container">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="用户管理" name="user">
          <div class="toolbar">
            <el-button type="primary" :icon="Plus" @click="handleAddUser">新增用户</el-button>
          </div>
          <el-table :data="userList" border stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" width="150" />
            <el-table-column prop="realName" label="姓名" width="120" />
            <el-table-column prop="role" label="角色" width="150">
              <template #default="{ row }">
                <el-tag :type="getRoleTagType(row.role)">{{ getRoleName(row.role) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="手机号" width="150" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '启用' ? 'success' : 'danger'">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditUser(row)">编辑</el-button>
                <el-button type="warning" link size="small" @click="handleResetPassword(row)">重置密码</el-button>
                <el-button type="danger" link size="small" @click="handleDeleteUser(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="pagination"
            v-model:current-page="userPage"
            v-model:page-size="userPageSize"
            :total="userTotal"
            layout="total, sizes, prev, pager, next, jumper"
            :page-sizes="[10, 20, 50, 100]"
          />
        </el-tab-pane>

        <el-tab-pane label="角色管理" name="role">
          <div class="toolbar">
            <el-button type="primary" :icon="Plus" @click="handleAddRole">新增角色</el-button>
          </div>
          <el-table :data="roleList" border stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="roleKey" label="角色标识" width="180" />
            <el-table-column prop="roleName" label="角色名称" width="150" />
            <el-table-column prop="description" label="角色描述" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '启用' ? 'success' : 'danger'">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditRole(row)">编辑</el-button>
                <el-button type="success" link size="small" @click="handleAssignMenu(row)">分配菜单</el-button>
                <el-button type="danger" link size="small" @click="handleDeleteRole(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="pagination"
            v-model:current-page="rolePage"
            v-model:page-size="rolePageSize"
            :total="roleTotal"
            layout="total, sizes, prev, pager, next, jumper"
            :page-sizes="[10, 20, 50, 100]"
          />
        </el-tab-pane>

        <el-tab-pane label="菜单管理" name="menu">
          <div class="toolbar">
            <el-button type="primary" :icon="Plus" @click="handleAddMenu">新增菜单</el-button>
          </div>
          <el-table :data="menuTree" row-key="id" border stripe default-expand-all>
            <el-table-column prop="name" label="菜单名称" />
            <el-table-column prop="path" label="路径" width="180" />
            <el-table-column prop="icon" label="图标" width="100">
              <template #default="{ row }">
                <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.type === '目录' ? 'primary' : row.type === '菜单' ? 'success' : 'info'" size="small">
                  {{ row.type }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sort" label="排序" width="80" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '启用' ? 'success' : 'danger'">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditMenu(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDeleteMenu(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="字典管理" name="dict">
          <div class="toolbar">
            <el-button type="primary" :icon="Plus" @click="handleAddDict">新增字典</el-button>
          </div>
          <el-table :data="dictList" border stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="dictName" label="字典名称" width="180" />
            <el-table-column prop="dictType" label="字典类型" width="180" />
            <el-table-column prop="description" label="描述" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '启用' ? 'success' : 'danger'">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditDict(row)">编辑</el-button>
                <el-button type="success" link size="small" @click="handleViewDictItem(row)">字典项</el-button>
                <el-button type="danger" link size="small" @click="handleDeleteDict(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="pagination"
            v-model:current-page="dictPage"
            v-model:page-size="dictPageSize"
            :total="dictTotal"
            layout="total, sizes, prev, pager, next, jumper"
            :page-sizes="[10, 20, 50, 100]"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="userDialogVisible" :title="userDialogTitle" width="500px">
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio value="启用">启用</el-radio>
            <el-radio value="禁用">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveUser">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" :title="roleDialogTitle" width="500px">
      <el-form :model="roleForm" :rules="roleRules" ref="roleFormRef" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-select v-model="roleForm.roleName" placeholder="请选择角色名称" style="width: 100%" @change="handleRoleNameChange">
            <el-option v-for="item in roleNameOptions" :key="item.value" :label="item.label" :value="item.label" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色标识" prop="roleKey">
          <el-input v-model="roleForm.roleKey" placeholder="自动填充" disabled />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio value="启用">启用</el-radio>
            <el-radio value="禁用">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRole">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="menuDialogVisible" :title="menuDialogTitle" width="500px">
      <el-form :model="menuForm" :rules="menuRules" ref="menuFormRef" label-width="80px">
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="menuForm.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路径" prop="path">
          <el-input v-model="menuForm.path" placeholder="请输入菜单路径" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="menuForm.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="menuForm.type" style="width: 100%">
            <el-option label="目录" value="目录" />
            <el-option label="菜单" value="菜单" />
            <el-option label="按钮" value="按钮" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="menuForm.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="menuForm.status">
            <el-radio value="启用">启用</el-radio>
            <el-radio value="禁用">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveMenu">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dictDialogVisible" :title="dictDialogTitle" width="500px">
      <el-form :model="dictForm" :rules="dictRules" ref="dictFormRef" label-width="80px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="dictForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="dictForm.dictType" placeholder="请输入字典类型" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="dictForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dictForm.status">
            <el-radio value="启用">启用</el-radio>
            <el-radio value="禁用">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dictDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveDict">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roleNames } from '../../config/roleMenu'

const activeTab = ref('user')

const userList = ref([
  { id: 1, username: 'admin', realName: '系统管理员', role: 'admin', phone: '13800000001', email: 'admin@mes.com', status: '启用', createTime: '2024-01-01 10:00:00' },
  { id: 2, username: 'order_clerk', realName: '王订单', role: 'order_clerk', phone: '13800000002', email: 'order@mes.com', status: '启用', createTime: '2024-01-02 10:00:00' },
  { id: 3, username: 'purchase_clerk', realName: '李采购', role: 'purchase_clerk', phone: '13800000003', email: 'purchase@mes.com', status: '启用', createTime: '2024-01-03 10:00:00' },
  { id: 4, username: 'warehouse_keeper', realName: '张仓管', role: 'warehouse_keeper', phone: '13800000004', email: 'warehouse@mes.com', status: '启用', createTime: '2024-01-04 10:00:00' },
  { id: 5, username: 'production_manager', realName: '赵生产', role: 'production_manager', phone: '13800000005', email: 'production@mes.com', status: '启用', createTime: '2024-01-05 10:00:00' },
  { id: 6, username: 'quality_inspector', realName: '陈质检', role: 'quality_inspector', phone: '13800000006', email: 'quality@mes.com', status: '启用', createTime: '2024-01-06 10:00:00' },
  { id: 7, username: 'equipment_admin', realName: '钱设备', role: 'equipment_admin', phone: '13800138009', email: 'equipment@mes.com', status: '启用', createTime: '2024-01-07 10:00:00' },
  { id: 8, username: 'finance', realName: '周财务', role: 'finance', phone: '13800138010', email: 'finance@mes.com', status: '启用', createTime: '2024-01-08 10:00:00' },
  { id: 9, username: 'guest', realName: '访客用户', role: 'guest', phone: '13800138011', email: 'guest@mes.com', status: '启用', createTime: '2024-01-09 10:00:00' },
  { id: 10, username: 'exception_handler', realName: '吴异常', role: 'exception_handler', phone: '13800138012', email: 'exception@mes.com', status: '启用', createTime: '2024-01-10 10:00:00' }
])
const userPage = ref(1)
const userPageSize = ref(10)
const userTotal = ref(10)
const userDialogVisible = ref(false)
const userDialogTitle = ref('新增用户')
const userFormRef = ref(null)
const userForm = ref({
  id: null,
  username: '',
  realName: '',
  role: '',
  phone: '',
  email: '',
  status: '启用'
})
const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const roleOptions = Object.entries(roleNames).map(([key, value]) => ({
  value: key,
  label: value
}))

function getRoleName(role) {
  return roleNames[role] || role
}

function getRoleTagType(role) {
  const map = {
    admin: 'danger',
    order_clerk: 'primary',
    purchase_clerk: 'warning',
    warehouse_keeper: 'success',
    production_manager: 'info',
    quality_inspector: '',
    equipment_admin: 'primary',
    finance: 'warning',
    guest: 'info',
    exception_handler: 'danger'
  }
  return map[role] || ''
}

function handleAddUser() {
  userDialogTitle.value = '新增用户'
  userForm.value = { id: null, username: '', realName: '', role: '', phone: '', email: '', status: '启用' }
  userDialogVisible.value = true
}

function handleEditUser(row) {
  userDialogTitle.value = '编辑用户'
  userForm.value = { ...row }
  userDialogVisible.value = true
}

function handleSaveUser() {
  userFormRef.value?.validate(valid => {
    if (valid) {
      if (userForm.value.id) {
        const index = userList.value.findIndex(item => item.id === userForm.value.id)
        if (index > -1) {
          userList.value[index] = { ...userForm.value }
        }
        ElMessage.success('修改成功')
      } else {
        const newId = Math.max(...userList.value.map(item => item.id)) + 1
        userList.value.push({
          ...userForm.value,
          id: newId,
          createTime: new Date().toLocaleString()
        })
        userTotal.value++
        ElMessage.success('新增成功')
      }
      userDialogVisible.value = false
    }
  })
}

function handleResetPassword(row) {
  ElMessageBox.confirm(`确定要重置用户「${row.realName}」的密码吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('密码重置成功，新密码：123456')
  }).catch(() => {})
}

function handleDeleteUser(row) {
  ElMessageBox.confirm(`确定要删除用户「${row.realName}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = userList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      userList.value.splice(index, 1)
      userTotal.value--
    }
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const roleList = ref([
  { id: 1, roleKey: 'admin', roleName: '超级管理员', description: '拥有所有系统权限', status: '启用', createTime: '2024-01-01 10:00:00' },
  { id: 2, roleKey: 'order_clerk', roleName: '订单专员', description: '负责订单管理相关工作', status: '启用', createTime: '2024-01-02 10:00:00' },
  { id: 3, roleKey: 'purchase_clerk', roleName: '采购专员', description: '负责采购管理相关工作', status: '启用', createTime: '2024-01-03 10:00:00' },
  { id: 4, roleKey: 'warehouse_keeper', roleName: '仓库管理员', description: '负责仓储管理相关工作', status: '启用', createTime: '2024-01-04 10:00:00' },
  { id: 5, roleKey: 'production_manager', roleName: '生产主管', description: '负责生产管理相关工作', status: '启用', createTime: '2024-01-05 10:00:00' },
  { id: 6, roleKey: 'quality_inspector', roleName: '质检人员', description: '负责质量管理相关工作', status: '启用', createTime: '2024-01-06 10:00:00' },
  { id: 7, roleKey: 'worker', roleName: '一线工人', description: '负责生产报工、异常上报', status: '启用', createTime: '2024-01-07 10:00:00' },
  { id: 8, roleKey: 'equipment_admin', roleName: '设备管理员', description: '负责设备管理和维护保养', status: '启用', createTime: '2024-01-08 10:00:00' },
  { id: 9, roleKey: 'finance', roleName: '财务人员', description: '负责财务核算和成本统计', status: '启用', createTime: '2024-01-09 10:00:00' },
  { id: 10, roleKey: 'guest', roleName: '访客', description: '只读权限，用于参观和查看', status: '启用', createTime: '2024-01-10 10:00:00' },
  { id: 11, roleKey: 'exception_handler', roleName: '异常处理员', description: '负责异常接收、判定等级、处理闭环', status: '启用', createTime: '2024-01-11 10:00:00' }
])
const rolePage = ref(1)
const rolePageSize = ref(10)
const roleTotal = ref(11)
const roleDialogVisible = ref(false)
const roleDialogTitle = ref('新增角色')
const roleFormRef = ref(null)
const roleForm = ref({
  id: null,
  roleKey: '',
  roleName: '',
  description: '',
  status: '启用'
})
const roleNameOptions = Object.entries(roleNames).map(([key, value]) => ({
  value: key,
  label: value
}))

const roleRules = {
  roleKey: [{ required: true, message: '请输入角色标识', trigger: 'blur' }],
  roleName: [{ required: true, message: '请选择角色名称', trigger: 'change' }]
}

function handleRoleNameChange(roleName) {
  const matched = roleNameOptions.find(item => item.label === roleName)
  if (matched) {
    roleForm.value.roleKey = matched.value
  }
}

function handleAddRole() {
  roleDialogTitle.value = '新增角色'
  roleForm.value = { id: null, roleKey: '', roleName: '', description: '', status: '启用' }
  roleDialogVisible.value = true
}

function handleEditRole(row) {
  roleDialogTitle.value = '编辑角色'
  roleForm.value = { ...row }
  roleDialogVisible.value = true
}

function handleSaveRole() {
  roleFormRef.value?.validate(valid => {
    if (valid) {
      if (roleForm.value.id) {
        const index = roleList.value.findIndex(item => item.id === roleForm.value.id)
        if (index > -1) {
          roleList.value[index] = { ...roleForm.value }
        }
        ElMessage.success('修改成功')
      } else {
        const newId = Math.max(...roleList.value.map(item => item.id)) + 1
        roleList.value.push({
          ...roleForm.value,
          id: newId,
          createTime: new Date().toLocaleString()
        })
        roleTotal.value++
        ElMessage.success('新增成功')
      }
      roleDialogVisible.value = false
    }
  })
}

function handleAssignMenu(row) {
  ElMessage.info(`为角色「${row.roleName}」分配菜单功能开发中...`)
}

function handleDeleteRole(row) {
  ElMessageBox.confirm(`确定要删除角色「${row.roleName}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = roleList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      roleList.value.splice(index, 1)
      roleTotal.value--
    }
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const menuTree = ref([
  {
    id: 1, name: '首页看板', path: 'dashboard', icon: 'Odometer', type: '菜单', sort: 1, status: '启用', children: []
  },
  {
    id: 2, name: '基础档案', path: '', icon: 'Files', type: '目录', sort: 2, status: '启用',
    children: [
      { id: 21, name: '客户档案', path: 'customer', icon: 'User', type: '菜单', sort: 1, status: '启用' },
      { id: 22, name: '供应商管理', path: 'supplier', icon: 'OfficeBuilding', type: '菜单', sort: 2, status: '启用' },
      { id: 23, name: '物料档案', path: 'material', icon: 'Box', type: '菜单', sort: 3, status: '启用' },
      { id: 24, name: '产品档案', path: 'product', icon: 'Present', type: '菜单', sort: 4, status: '启用' },
      { id: 25, name: '设备档案', path: 'equipment', icon: 'Cpu', type: '菜单', sort: 5, status: '启用' }
    ]
  },
  {
    id: 3, name: '订单管理', path: 'order', icon: 'Document', type: '菜单', sort: 3, status: '启用', children: []
  },
  {
    id: 4, name: '采购管理', path: 'purchase', icon: 'ShoppingCart', type: '菜单', sort: 4, status: '启用', children: []
  },
  {
    id: 5, name: '仓储管理', path: '', icon: 'Goods', type: '目录', sort: 5, status: '启用',
    children: [
      { id: 51, name: '库存查询', path: 'stock', icon: 'DataAnalysis', type: '菜单', sort: 1, status: '启用' },
      { id: 52, name: '入库记录', path: 'stock-in', icon: 'Bottom', type: '菜单', sort: 2, status: '启用' },
      { id: 53, name: '出库记录', path: 'stock-out', icon: 'Top', type: '菜单', sort: 3, status: '启用' },
      { id: 54, name: '库存流水', path: 'stock-flow', icon: 'List', type: '菜单', sort: 4, status: '启用' }
    ]
  },
  {
    id: 6, name: '生产管理', path: '', icon: 'Tickets', type: '目录', sort: 6, status: '启用',
    children: [
      { id: 61, name: '生产工单', path: 'work-order', icon: 'Tickets', type: '菜单', sort: 1, status: '启用' },
      { id: 62, name: '生产进度', path: 'production-progress', icon: 'TrendCharts', type: '菜单', sort: 2, status: '启用' },
      { id: 63, name: '工艺配置', path: 'process-config', icon: 'SetUp', type: '菜单', sort: 3, status: '启用' },
      { id: 64, name: '质检记录', path: 'quality-record', icon: 'CircleCheck', type: '菜单', sort: 4, status: '启用' },
      { id: 65, name: '异常处理', path: 'exception-workbench', icon: 'Warning', type: '菜单', sort: 5, status: '启用' }
    ]
  },
  {
    id: 7, name: '系统管理', path: '', icon: 'Tools', type: '目录', sort: 7, status: '启用',
    children: [
      { id: 71, name: '系统管理', path: 'system', icon: 'Tools', type: '菜单', sort: 1, status: '启用' },
      { id: 72, name: '操作日志', path: 'operation-log', icon: 'Notebook', type: '菜单', sort: 2, status: '启用' }
    ]
  }
])
const menuDialogVisible = ref(false)
const menuDialogTitle = ref('新增菜单')
const menuFormRef = ref(null)
const menuForm = ref({
  id: null,
  name: '',
  path: '',
  icon: '',
  type: '菜单',
  sort: 0,
  status: '启用'
})
const menuRules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }]
}

function handleAddMenu() {
  menuDialogTitle.value = '新增菜单'
  menuForm.value = { id: null, name: '', path: '', icon: '', type: '菜单', sort: 0, status: '启用' }
  menuDialogVisible.value = true
}

function handleEditMenu(row) {
  menuDialogTitle.value = '编辑菜单'
  menuForm.value = { ...row }
  menuDialogVisible.value = true
}

function handleSaveMenu() {
  menuFormRef.value?.validate(valid => {
    if (valid) {
      ElMessage.success('保存成功')
      menuDialogVisible.value = false
    }
  })
}

function handleDeleteMenu(row) {
  ElMessageBox.confirm(`确定要删除菜单「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const dictList = ref([
  { id: 1, dictName: '工单状态', dictType: 'work_order_status', description: '生产工单状态枚举', status: '启用', createTime: '2024-01-01 10:00:00' },
  { id: 2, dictName: '采购状态', dictType: 'purchase_status', description: '采购单状态枚举', status: '启用', createTime: '2024-01-02 10:00:00' },
  { id: 3, dictName: '出入库类型', dictType: 'stock_type', description: '出入库单据类型', status: '启用', createTime: '2024-01-03 10:00:00' },
  { id: 4, dictName: '质检结果', dictType: 'quality_result', description: '质量检验结果', status: '启用', createTime: '2024-01-04 10:00:00' },
  { id: 5, dictName: '异常等级', dictType: 'exception_level', description: '异常事件等级', status: '启用', createTime: '2024-01-05 10:00:00' }
])
const dictPage = ref(1)
const dictPageSize = ref(10)
const dictTotal = ref(5)
const dictDialogVisible = ref(false)
const dictDialogTitle = ref('新增字典')
const dictFormRef = ref(null)
const dictForm = ref({
  id: null,
  dictName: '',
  dictType: '',
  description: '',
  status: '启用'
})
const dictRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
}

function handleAddDict() {
  dictDialogTitle.value = '新增字典'
  dictForm.value = { id: null, dictName: '', dictType: '', description: '', status: '启用' }
  dictDialogVisible.value = true
}

function handleEditDict(row) {
  dictDialogTitle.value = '编辑字典'
  dictForm.value = { ...row }
  dictDialogVisible.value = true
}

function handleSaveDict() {
  dictFormRef.value?.validate(valid => {
    if (valid) {
      if (dictForm.value.id) {
        const index = dictList.value.findIndex(item => item.id === dictForm.value.id)
        if (index > -1) {
          dictList.value[index] = { ...dictForm.value }
        }
        ElMessage.success('修改成功')
      } else {
        const newId = Math.max(...dictList.value.map(item => item.id)) + 1
        dictList.value.push({
          ...dictForm.value,
          id: newId,
          createTime: new Date().toLocaleString()
        })
        dictTotal.value++
        ElMessage.success('新增成功')
      }
      dictDialogVisible.value = false
    }
  })
}

function handleViewDictItem(row) {
  ElMessage.info(`查看字典「${row.dictName}」的字典项功能开发中...`)
}

function handleDeleteDict(row) {
  ElMessageBox.confirm(`确定要删除字典「${row.dictName}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = dictList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      dictList.value.splice(index, 1)
      dictTotal.value--
    }
    ElMessage.success('删除成功')
  }).catch(() => {})
}
</script>

<style scoped>
.page-container {
  width: 100%;
}

.toolbar {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
