<template>
  <div class="auth-page">
    <div class="grid-bg"></div>
    
    <div class="auth-wrapper">
      <div class="auth-panel">
        <div class="panel-card">
          <div class="card-top">
            <div class="brand-row">
              <div class="brand-badge">
                <el-icon :size="22"><Watch /></el-icon>
              </div>
              <div class="brand-text-group">
                <span class="brand-main">智能手环MES系统</span>
                <span class="brand-sub">Smart Bracelet MES</span>
              </div>
            </div>
          </div>

          <div class="card-center">
            <div class="tab-bar">
              <span
                class="tab-label"
                :class="{ on: currentTab === 'signin' }"
                @click="currentTab = 'signin'"
              >登录</span>
              <span
                class="tab-label"
                :class="{ on: currentTab === 'signup' }"
                @click="currentTab = 'signup'"
              >注册</span>
            </div>

            <div class="slide-container">
              <div class="slide-track" :class="{ shift: currentTab === 'signup' }">
                <div class="signin-pane">
                  <h3 class="pane-title">欢迎登录</h3>
                  <p class="pane-desc">请输入您的账号信息</p>

                  <div class="quick-role">
                    <div class="role-heading">快速选择角色</div>
                    <div class="role-options">
                      <div
                        v-for="u in userList"
                        :key="u.role"
                        class="role-chip"
                        :class="{ chosen: pickedRole === u.role }"
                        @click="pickRole(u)"
                      >
                        <el-avatar :size="24" class="role-ico">
                          <el-icon :size="12"><UserFilled /></el-icon>
                        </el-avatar>
                        <span class="role-txt">{{ u.realName }}</span>
                      </div>
                    </div>
                  </div>

                  <el-form
                    ref="signinRef"
                    :model="signinData"
                    :rules="signinRules"
                    class="auth-form"
                  >
                    <el-form-item prop="username">
                      <div class="field-block">
                        <label class="field-name">用户名</label>
                        <div class="input-box">
                          <el-icon class="prefix-ico"><User /></el-icon>
                          <el-input
                            v-model="signinData.username"
                            placeholder="请输入用户名"
                            size="large"
                            class="styled-input"
                          />
                        </div>
                      </div>
                    </el-form-item>

                    <el-form-item prop="password">
                      <div class="field-block">
                        <label class="field-name">密码</label>
                        <div class="input-box">
                          <el-icon class="prefix-ico"><Lock /></el-icon>
                          <el-input
                            v-model="signinData.password"
                            type="password"
                            placeholder="请输入密码"
                            size="large"
                            show-password
                            class="styled-input"
                            @keyup.enter="doSignin"
                          />
                        </div>
                      </div>
                    </el-form-item>

                    <div class="form-extras">
                      <label class="remember-check">
                        <el-checkbox v-model="keepLogin" size="small" />
                        <span>记住我</span>
                      </label>
                    </div>

                    <el-button
                      type="primary"
                      size="large"
                      class="submit-btn"
                      :loading="signinLoading"
                      @click="doSignin"
                    >
                      <span v-if="!signinLoading">登 录</span>
                      <span v-else>登录中...</span>
                    </el-button>
                  </el-form>
                </div>

                <div class="signup-pane">
                  <h3 class="pane-title">创建账号</h3>
                  <p class="pane-desc">填写以下信息完成注册</p>

                  <el-form
                    ref="signupRef"
                    :model="signupData"
                    :rules="signupRules"
                    class="auth-form"
                  >
                    <el-form-item prop="username">
                      <div class="field-block">
                        <label class="field-name">用户名</label>
                        <div class="input-box">
                          <el-icon class="prefix-ico"><User /></el-icon>
                          <el-input
                            v-model="signupData.username"
                            placeholder="请输入用户名（3-20个字符）"
                            size="large"
                            class="styled-input"
                          />
                        </div>
                      </div>
                    </el-form-item>

                    <div class="dual-row">
                      <el-form-item prop="realName" class="dual-col">
                        <div class="field-block">
                          <label class="field-name">真实姓名</label>
                          <div class="input-box">
                            <el-icon class="prefix-ico small"><UserFilled /></el-icon>
                            <el-input
                              v-model="signupData.realName"
                              placeholder="选填"
                              size="large"
                              class="styled-input compact"
                            />
                          </div>
                        </div>
                      </el-form-item>

                      <el-form-item prop="phone" class="dual-col">
                        <div class="field-block">
                          <label class="field-name">手机号</label>
                          <div class="input-box">
                            <el-icon class="prefix-ico small"><Phone /></el-icon>
                            <el-input
                              v-model="signupData.phone"
                              placeholder="选填"
                              size="large"
                              class="styled-input compact"
                            />
                          </div>
                        </div>
                      </el-form-item>
                    </div>

                    <div class="dual-row">
                      <el-form-item prop="password" class="dual-col">
                        <div class="field-block">
                          <label class="field-name">密码</label>
                          <div class="input-box">
                            <el-icon class="prefix-ico small"><Lock /></el-icon>
                            <el-input
                              v-model="signupData.password"
                              type="password"
                              placeholder="6-20个字符"
                              size="large"
                              show-password
                              class="styled-input compact"
                            />
                          </div>
                        </div>
                      </el-form-item>

                      <el-form-item prop="confirmPassword" class="dual-col">
                        <div class="field-block">
                          <label class="field-name">确认密码</label>
                          <div class="input-box">
                            <el-icon class="prefix-ico small"><Lock /></el-icon>
                            <el-input
                              v-model="signupData.confirmPassword"
                              type="password"
                              placeholder="再次输入"
                              size="large"
                              show-password
                              class="styled-input compact"
                              @keyup.enter="doSignup"
                            />
                          </div>
                        </div>
                      </el-form-item>
                    </div>

                    <el-button
                      type="primary"
                      size="large"
                      class="submit-btn"
                      :loading="signupLoading"
                      @click="doSignup"
                    >
                      <span v-if="!signupLoading">注 册</span>
                      <span v-else>注册中...</span>
                    </el-button>
                  </el-form>

                  <div class="tip-bar">
                    <el-icon :size="12"><InfoFilled /></el-icon>
                    <span>注册成功后默认角色为订单员</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="card-bottom">
            <span>© 2026 智能手环MES系统 v1.0.0</span>
          </div>
        </div>
      </div>

      <div class="showcase">
        <div class="showcase-inner">
          <div class="big-icon-wrap">
            <div class="big-icon">
              <el-icon :size="52"><Watch /></el-icon>
            </div>
          </div>
          <div class="showcase-title">智能手环MES系统</div>
          <div class="showcase-subtitle">Smart Bracelet MES</div>
          <div class="showcase-slogan">
            <span>智能制造</span>
            <em>·</em>
            <span>高效管理</span>
            <em>·</em>
            <span>品质保障</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'
import {
  Watch, User, Lock, InfoFilled, UserFilled, Phone
} from '@element-plus/icons-vue'
import { mockUsers, roleNames } from '../../config/roleMenu'
import { login as apiLogin, register } from '../../api/auth'

const router = useRouter()
const userStore = useUserStore()

const signinRef = ref(null)
const signupRef = ref(null)
const signinLoading = ref(false)
const signupLoading = ref(false)
const pickedRole = ref('admin')
const keepLogin = ref(false)
const currentTab = ref('signin')

const userList = mockUsers

const signinData = reactive({
  username: 'admin',
  password: '123456'
})

const signupData = reactive({
  username: '',
  realName: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const signinRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const signupRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== signupData.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

function pickRole(u) {
  pickedRole.value = u.role
  signinData.username = u.username
  signinData.password = u.password
}

async function doSignin() {
  if (!signinRef.value) return
  
  try {
    await signinRef.value.validate()
    signinLoading.value = true
    
    const res = await apiLogin({
      username: signinData.username,
      password: signinData.password
    })
    
    if (res.code === 200) {
      userStore.token = res.data.token
      localStorage.setItem('token', res.data.token)
      
      const matchedUser = mockUsers.find(u => u.username === signinData.username)
      const role = matchedUser ? matchedUser.role : 'admin'
      const realName = res.data.realName || signinData.username
      
      userStore.userInfo = {
        username: signinData.username,
        realName: realName,
        role: role
      }
      localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
      
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res.message || '登录失败')
    }
    signinLoading.value = false
    
  } catch (error) {
    signinLoading.value = false
    console.error('登录失败:', error)
  }
}

async function doSignup() {
  if (!signupRef.value) return
  
  try {
    await signupRef.value.validate()
    signupLoading.value = true
    
    const res = await register({
      username: signupData.username,
      password: signupData.password,
      realName: signupData.realName,
      phone: signupData.phone
    })
    
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      currentTab.value = 'signin'
      signinData.username = signupData.username
      signinData.password = signupData.password
      signupData.username = ''
      signupData.realName = ''
      signupData.phone = ''
      signupData.password = ''
      signupData.confirmPassword = ''
    } else {
      ElMessage.error(res.message || '注册失败')
    }
    signupLoading.value = false
    
  } catch (error) {
    signupLoading.value = false
    ElMessage.error(error.response?.data?.message || '注册失败，请稍后重试')
    console.error('注册失败:', error)
  }
}
</script>

<style scoped>
.auth-page {
  width: 100%;
  height: 100%;
  position: relative;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 50%, #bfdbfe 100%);
  overflow: hidden;
}

.grid-bg {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(59, 130, 246, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(59, 130, 246, 0.04) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
}

.auth-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  position: relative;
  z-index: 1;
}

.auth-panel {
  width: 45%;
  min-width: 500px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  order: 2;
}

.panel-card {
  width: 100%;
  max-width: 500px;
  height: 700px;
  background: #fff;
  border-radius: 20px;
  padding: 28px 48px 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: none;
  overflow: hidden;
}

.card-top {
  flex-shrink: 0;
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-badge {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.35);
}

.brand-text-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-main {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.2;
}

.brand-sub {
  font-size: 11px;
  color: #94a3b8;
  letter-spacing: 0.5px;
  font-weight: 500;
}

.card-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 12px 0;
}

.tab-bar {
  display: flex;
  gap: 36px;
  margin-bottom: 22px;
  position: relative;
}

.tab-label {
  font-size: 17px;
  font-weight: 500;
  color: #94a3b8;
  cursor: pointer;
  padding-bottom: 10px;
  position: relative;
  transition: color 0.3s ease;
}

.tab-label.on {
  color: #1e293b;
  font-weight: 600;
}

.tab-label.on::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  border-radius: 2px;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6);
}

.slide-container {
  overflow: hidden;
}

.slide-track {
  display: flex;
  width: 200%;
  transition: transform 0.4s ease;
}

.slide-track.shift {
  transform: translateX(-50%);
}

.signin-pane,
.signup-pane {
  width: 50%;
  flex-shrink: 0;
}

.pane-title {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 4px;
  letter-spacing: -0.5px;
}

.pane-desc {
  font-size: 13px;
  color: #94a3b8;
  margin: 0 0 16px;
}

.quick-role {
  margin-bottom: 14px;
}

.role-heading {
  font-size: 12px;
  font-weight: 500;
  color: #64748b;
  margin-bottom: 7px;
}

.role-options {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.role-chip {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 4px 8px 4px 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid #e2e8f0;
  background: #fff;
}

.role-chip:hover {
  border-color: #93c5fd;
  background: #eff6ff;
}

.role-chip.chosen {
  background: #eff6ff;
  border-color: #3b82f6;
}

.role-ico {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: #fff;
  flex-shrink: 0;
}

.role-chip.chosen .role-ico {
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.4);
}

.role-txt {
  font-size: 11px;
  color: #475569;
  font-weight: 500;
  white-space: nowrap;
}

.role-chip.chosen .role-txt {
  color: #2563eb;
  font-weight: 600;
}

.auth-form {
  margin-bottom: 0;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.auth-form :deep(.el-form-item:last-of-type) {
  margin-bottom: 0;
}

.dual-row {
  display: flex;
  gap: 12px;
}

.dual-row .dual-col {
  flex: 1;
  margin-bottom: 12px !important;
}

.field-block {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.field-name {
  font-size: 12px;
  font-weight: 500;
  color: #475569;
}

.input-box {
  position: relative;
}

.prefix-ico {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  font-size: 15px;
  z-index: 1;
}

.prefix-ico.small {
  left: 10px;
  font-size: 14px;
}

.styled-input :deep(.el-input__wrapper) {
  padding-left: 38px;
  height: 40px;
  border-radius: 10px;
  box-shadow: none;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.styled-input.compact :deep(.el-input__wrapper) {
  padding-left: 34px;
}

.styled-input :deep(.el-input__wrapper:hover) {
  box-shadow: none;
  border-color: #93c5fd;
}

.styled-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: none;
  border-color: #3b82f6;
}

.styled-input :deep(.el-input__inner) {
  font-size: 13px;
}

.form-extras {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin: 2px 0 14px;
}

.remember-check {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #64748b;
  cursor: pointer;
  user-select: none;
}

.submit-btn {
  width: 100%;
  height: 42px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border: none;
  box-shadow: none;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: none;
  opacity: 0.9;
}

.submit-btn:active {
  transform: translateY(0);
  opacity: 1;
}

.tip-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  font-size: 11px;
  color: #64748b;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 8px;
  margin-top: 12px;
  border: 1px solid #f1f5f9;
}

.card-bottom {
  text-align: center;
  font-size: 11px;
  color: #94a3b8;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
  flex-shrink: 0;
}

.showcase {
  flex: 1;
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  order: 1;
}

.showcase-inner {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 60px;
  max-width: 500px;
}

.big-icon-wrap {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.big-icon {
  width: 100px;
  height: 100px;
  border-radius: 28px;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 20px 50px rgba(59, 130, 246, 0.3);
}

.showcase-title {
  font-size: 36px;
  font-weight: 700;
  color: #1e3a8a;
  margin-bottom: 10px;
  letter-spacing: 2px;
}

.showcase-subtitle {
  font-size: 14px;
  color: #3b82f6;
  margin-bottom: 28px;
  letter-spacing: 3px;
  font-weight: 400;
  opacity: 0.8;
}

.showcase-slogan {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 14px;
  color: #2563eb;
  letter-spacing: 4px;
  opacity: 0.8;
}

.showcase-slogan em {
  font-style: normal;
  color: #93c5fd;
}

@media (max-width: 1024px) {
  .showcase {
    display: none;
  }
  
  .auth-panel {
    width: 100%;
    min-width: auto;
  }
}
</style>
