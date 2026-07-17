<template>
  <div class="page-container">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="用户管理" name="user">
          <div class="toolbar">
            <el-button type="primary" :icon="Plus" @click="handleAddUser">新增用户</el-button>
          </div>
          <el-table :data="userList" border stripe v-loading="userLoading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" width="150" />
            <el-table-column prop="realName" label="姓名" width="120" />
            <el-table-column prop="phone" label="手机号" width="150" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="220" fixed="right">
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
            @size-change="handleUserSizeChange"
            @current-change="handleUserPageChange"
          />
        </el-tab-pane>

        <el-tab-pane label="角色管理" name="role">
          <div class="toolbar">
            <el-button type="primary" :icon="Plus" @click="handleAddRole">新增角色</el-button>
          </div>
          <el-table :data="roleList" border stripe v-loading="roleLoading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="roleCode" label="角色标识" width="180" />
            <el-table-column prop="roleName" label="角色名称" width="150" />
            <el-table-column prop="description" label="角色描述" />
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditRole(row)">编辑</el-button>
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
            @size-change="handleRoleSizeChange"
            @current-change="handleRolePageChange"
          />
        </el-tab-pane>

        <el-tab-pane label="权限管理" name="permission">
          <div class="toolbar">
            <el-button type="primary" :icon="Plus" @click="handleAddPermission">新增权限</el-button>
          </div>
          <el-table :data="permissionList" border stripe v-loading="permissionLoading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="permissionName" label="权限名称" width="180" />
            <el-table-column prop="permissionCode" label="权限编码" width="200" />
            <el-table-column prop="resourceType" label="资源类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getResourceTagType(row.resourceType)" size="small">
                  {{ row.resourceType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="resourcePath" label="资源路径" />
            <el-table-column prop="parentId" label="父级ID" width="100" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditPermission(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDeletePermission(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
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
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!userForm.id" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="userSubmitLoading" @click="handleSaveUser">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" :title="roleDialogTitle" width="500px">
      <el-form :model="roleForm" :rules="roleRules" ref="roleFormRef" label-width="80px">
        <el-form-item label="角色标识" prop="roleCode">
          <el-input v-model="roleForm.roleCode" placeholder="请输入角色标识，如admin" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSubmitLoading" @click="handleSaveRole">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permissionDialogVisible" :title="permissionDialogTitle" width="500px">
      <el-form :model="permissionForm" :rules="permissionRules" ref="permissionFormRef" label-width="80px">
        <el-form-item label="权限名称" prop="permissionName">
          <el-input v-model="permissionForm.permissionName" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="permissionCode">
          <el-input v-model="permissionForm.permissionCode" placeholder="请输入权限编码" />
        </el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="permissionForm.resourceType" placeholder="请选择资源类型" style="width: 100%">
            <el-option label="菜单" value="菜单" />
            <el-option label="按钮" value="按钮" />
            <el-option label="接口" value="接口" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源路径" prop="resourcePath">
          <el-input v-model="permissionForm.resourcePath" placeholder="请输入资源路径" />
        </el-form-item>
        <el-form-item label="父级ID" prop="parentId">
          <el-input-number v-model="permissionForm.parentId" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permissionSubmitLoading" @click="handleSavePermission">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resetPwdDialogVisible" title="重置密码" width="400px">
      <el-form :model="resetPwdForm" :rules="resetPwdRules" ref="resetPwdFormRef" label-width="80px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetPwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetPwdLoading" @click="handleConfirmResetPwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { systemUser, systemRole, systemPermission } from '../../api/system'

const activeTab = ref('user')

const userLoading = ref(false)
const userList = ref([])
const userPage = ref(1)
const userPageSize = ref(10)
const userTotal = ref(0)
const userDialogVisible = ref(false)
const userDialogTitle = ref('新增用户')
const userSubmitLoading = ref(false)
const userFormRef = ref(null)
const userForm = reactive({
  id: null,
  username: '',
  realName: '',
  phone: '',
  status: 1,
  password: ''
})
const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const roleLoading = ref(false)
const roleList = ref([])
const rolePage = ref(1)
const rolePageSize = ref(10)
const roleTotal = ref(0)
const roleDialogVisible = ref(false)
const roleDialogTitle = ref('新增角色')
const roleSubmitLoading = ref(false)
const roleFormRef = ref(null)
const roleForm = reactive({
  id: null,
  roleCode: '',
  roleName: '',
  description: ''
})
const roleRules = {
  roleCode: [{ required: true, message: '请输入角色标识', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const permissionLoading = ref(false)
const permissionList = ref([])
const permissionDialogVisible = ref(false)
const permissionDialogTitle = ref('新增权限')
const permissionSubmitLoading = ref(false)
const permissionFormRef = ref(null)
const permissionForm = reactive({
  id: null,
  permissionName: '',
  permissionCode: '',
  resourceType: '',
  resourcePath: '',
  parentId: 0
})
const permissionRules = {
  permissionName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  permissionCode: [{ required: true, message: '请输入权限编码', trigger: 'blur' }]
}

const resetPwdDialogVisible = ref(false)
const resetPwdLoading = ref(false)
const resetPwdFormRef = ref(null)
const resetPwdForm = reactive({
  userId: null,
  newPassword: '',
  confirmPassword: ''
})
const resetPwdRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetPwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

function getResourceTagType(type) {
  const map = {
    '菜单': 'primary',
    '按钮': 'success',
    '接口': 'warning'
  }
  return map[type] || 'info'
}

function loadUserList() {
  userLoading.value = true
  systemUser.getList().then(res => {
    if (res.code === 200) {
      const all = res.data || []
      userTotal.value = all.length
      const start = (userPage.value - 1) * userPageSize.value
      const end = start + userPageSize.value
      userList.value = all.slice(start, end)
    }
  }).finally(() => {
    userLoading.value = false
  })
}

function handleUserSizeChange(size) {
  userPageSize.value = size
  userPage.value = 1
  loadUserList()
}

function handleUserPageChange(page) {
  userPage.value = page
  loadUserList()
}

function handleAddUser() {
  userDialogTitle.value = '新增用户'
  Object.assign(userForm, { id: null, username: '', realName: '', phone: '', status: 1, password: '' })
  userDialogVisible.value = true
}

function handleEditUser(row) {
  userDialogTitle.value = '编辑用户'
  Object.assign(userForm, { ...row, password: '' })
  userDialogVisible.value = true
}

function handleSaveUser() {
  userFormRef.value?.validate(valid => {
    if (!valid) return
    userSubmitLoading.value = true
    const api = userForm.id ? systemUser.update : systemUser.save
    const data = { ...userForm }
    if (!data.id) {
      data.status = data.status || 1
    }
    api(data).then(res => {
      if (res.code === 200) {
        ElMessage.success(userForm.id ? '修改成功' : '新增成功')
        userDialogVisible.value = false
        loadUserList()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    }).finally(() => {
      userSubmitLoading.value = false
    })
  })
}

function handleResetPassword(row) {
  resetPwdForm.userId = row.id
  resetPwdForm.newPassword = '123456'
  resetPwdForm.confirmPassword = '123456'
  resetPwdDialogVisible.value = true
}

function handleConfirmResetPwd() {
  resetPwdFormRef.value?.validate(valid => {
    if (!valid) return
    resetPwdLoading.value = true
    systemUser.updatePassword({
      userId: resetPwdForm.userId,
      oldPassword: 'dummy',
      newPassword: resetPwdForm.newPassword
    }).then(res => {
      if (res.code === 200) {
        ElMessage.success('密码重置成功')
        resetPwdDialogVisible.value = false
      } else {
        ElMessage.error(res.message || '密码重置失败')
      }
    }).finally(() => {
      resetPwdLoading.value = false
    })
  })
}

function handleDeleteUser(row) {
  ElMessageBox.confirm(`确定要删除用户「${row.realName || row.username}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    systemUser.remove(row.id).then(res => {
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadUserList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
  }).catch(() => {})
}

function loadRoleList() {
  roleLoading.value = true
  systemRole.getList().then(res => {
    if (res.code === 200) {
      const all = res.data || []
      roleTotal.value = all.length
      const start = (rolePage.value - 1) * rolePageSize.value
      const end = start + rolePageSize.value
      roleList.value = all.slice(start, end)
    }
  }).finally(() => {
    roleLoading.value = false
  })
}

function handleRoleSizeChange(size) {
  rolePageSize.value = size
  rolePage.value = 1
  loadRoleList()
}

function handleRolePageChange(page) {
  rolePage.value = page
  loadRoleList()
}

function handleAddRole() {
  roleDialogTitle.value = '新增角色'
  Object.assign(roleForm, { id: null, roleCode: '', roleName: '', description: '' })
  roleDialogVisible.value = true
}

function handleEditRole(row) {
  roleDialogTitle.value = '编辑角色'
  Object.assign(roleForm, { ...row })
  roleDialogVisible.value = true
}

function handleSaveRole() {
  roleFormRef.value?.validate(valid => {
    if (!valid) return
    roleSubmitLoading.value = true
    const api = roleForm.id ? systemRole.update : systemRole.save
    api(roleForm).then(res => {
      if (res.code === 200) {
        ElMessage.success(roleForm.id ? '修改成功' : '新增成功')
        roleDialogVisible.value = false
        loadRoleList()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    }).finally(() => {
      roleSubmitLoading.value = false
    })
  })
}

function handleDeleteRole(row) {
  ElMessageBox.confirm(`确定要删除角色「${row.roleName}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    systemRole.remove(row.id).then(res => {
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadRoleList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
  }).catch(() => {})
}

function loadPermissionList() {
  permissionLoading.value = true
  systemPermission.getList().then(res => {
    if (res.code === 200) {
      permissionList.value = res.data || []
    }
  }).finally(() => {
    permissionLoading.value = false
  })
}

function handleAddPermission() {
  permissionDialogTitle.value = '新增权限'
  Object.assign(permissionForm, { id: null, permissionName: '', permissionCode: '', resourceType: '', resourcePath: '', parentId: 0 })
  permissionDialogVisible.value = true
}

function handleEditPermission(row) {
  permissionDialogTitle.value = '编辑权限'
  Object.assign(permissionForm, { ...row })
  permissionDialogVisible.value = true
}

function handleSavePermission() {
  permissionFormRef.value?.validate(valid => {
    if (!valid) return
    permissionSubmitLoading.value = true
    const api = permissionForm.id ? systemPermission.update : systemPermission.save
    api(permissionForm).then(res => {
      if (res.code === 200) {
        ElMessage.success(permissionForm.id ? '修改成功' : '新增成功')
        permissionDialogVisible.value = false
        loadPermissionList()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    }).finally(() => {
      permissionSubmitLoading.value = false
    })
  })
}

function handleDeletePermission(row) {
  ElMessageBox.confirm(`确定要删除权限「${row.permissionName}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    systemPermission.remove(row.id).then(res => {
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadPermissionList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    })
  }).catch(() => {})
}

onMounted(() => {
  loadUserList()
  loadRoleList()
  loadPermissionList()
})
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
