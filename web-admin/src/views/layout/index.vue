<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar">
      <div class="logo">
        <div class="logo-icon">
          <el-icon :size="20"><Watch /></el-icon>
        </div>
        <span v-if="!isCollapse" class="logo-text">智能手环MES</span>
      </div>
      <div v-if="!isCollapse" class="sidebar-user">
        <el-avatar :size="36" class="user-avatar">
          <el-icon :size="18"><UserFilled /></el-icon>
        </el-avatar>
        <div class="user-info">
          <div class="user-name">{{ userStore.userInfo.realName || '管理员' }}</div>
          <div class="user-role">{{ roleText }}</div>
        </div>
      </div>
      <div class="menu-section">
        <div v-if="!isCollapse" class="section-title">导航菜单</div>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          :unique-opened="true"
          router
          class="sidebar-menu"
        >
          <template v-for="item in menuList" :key="item.path || item.title">
            <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.title">
              <template #title>
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.title }}</span>
              </template>
              <el-menu-item
                v-for="child in item.children"
                :key="child.path"
                :index="'/' + child.path"
              >
                <el-icon><component :is="child.icon" /></el-icon>
                <template #title>{{ child.title }}</template>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="'/' + item.path">
              <el-icon><component :is="item.icon" /></el-icon>
              <template #title>{{ item.title }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </div>
    </el-aside>

    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <div class="header-search" ref="searchRef">
            <el-icon class="search-icon"><Search /></el-icon>
            <input
              type="text"
              v-model="searchKeyword"
              placeholder="搜索菜单、功能..."
              class="search-input"
              @focus="showSearchResult = true"
              @keydown.down="handleKeyDown"
              @keydown.up="handleKeyUp"
              @keydown.enter="handleSearchEnter"
              @keydown.esc="showSearchResult = false"
            />
            <div v-if="showSearchResult && searchKeyword && matchedMenus.length > 0" class="search-dropdown">
              <div
                v-for="(item, index) in matchedMenus"
                :key="item.path"
                class="search-item"
                :class="{ active: activeSearchIndex === index }"
                @click="goToMenu(item)"
                @mouseenter="activeSearchIndex = index"
              >
                <el-icon class="item-icon"><component :is="item.icon" /></el-icon>
                <span class="item-title">{{ item.title }}</span>
                <span class="item-path">/{{ item.path }}</span>
              </div>
            </div>
            <div v-if="showSearchResult && searchKeyword && matchedMenus.length === 0" class="search-dropdown">
              <div class="search-empty">没有找到相关菜单</div>
            </div>
          </div>
        </div>
        <div class="header-right">
          <div class="header-datetime">
            <span class="date-text">{{ currentDate }}</span>
            <span class="time-text">{{ currentTime }}</span>
          </div>
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-dropdown-trigger">
              <el-avatar :size="32" class="header-avatar">
                <el-icon :size="16"><UserFilled /></el-icon>
              </el-avatar>
              <span class="header-username">{{ userStore.userInfo.realName || '管理员' }}</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown">
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <div class="content-wrapper">
        <div class="breadcrumb-bar">
          <div class="breadcrumb-left">
            <h2 class="page-title">{{ currentTitle || '首页看板' }}</h2>
            <el-breadcrumb separator="/" class="breadcrumb">
              <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
        </div>
        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </div>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Fold, Expand, ArrowDown, UserFilled, SwitchButton,
  Search, Watch
} from '@element-plus/icons-vue'
import { getMenusByRole, roleNames } from '../../config/roleMenu'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const currentDate = ref('')
const currentTime = ref('')
let timer = null

const searchKeyword = ref('')
const showSearchResult = ref(false)
const activeSearchIndex = ref(0)
const searchRef = ref(null)

const flatMenuList = computed(() => {
  const result = []
  const menus = menuList.value
  menus.forEach(item => {
    if (item.children && item.children.length > 0) {
      item.children.forEach(child => {
        result.push(child)
      })
    } else {
      result.push(item)
    }
  })
  return result
})

const matchedMenus = computed(() => {
  if (!searchKeyword.value.trim()) return []
  const keyword = searchKeyword.value.toLowerCase().trim()
  return flatMenuList.value.filter(item => 
    item.title.toLowerCase().includes(keyword) || 
    item.path.toLowerCase().includes(keyword)
  )
})

function handleKeyDown() {
  if (matchedMenus.value.length === 0) return
  activeSearchIndex.value = (activeSearchIndex.value + 1) % matchedMenus.value.length
}

function handleKeyUp() {
  if (matchedMenus.value.length === 0) return
  activeSearchIndex.value = (activeSearchIndex.value - 1 + matchedMenus.value.length) % matchedMenus.value.length
}

function handleSearchEnter() {
  if (matchedMenus.value.length > 0) {
    goToMenu(matchedMenus.value[activeSearchIndex.value])
  }
}

function goToMenu(item) {
  router.push('/' + item.path)
  searchKeyword.value = ''
  showSearchResult.value = false
  activeSearchIndex.value = 0
}

function handleClickOutside(e) {
  if (searchRef.value && !searchRef.value.contains(e.target)) {
    showSearchResult.value = false
  }
}

function updateDateTime() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekDay = weekDays[now.getDay()]
  currentDate.value = `${year}年${month}月${day}日 ${weekDay}`
  
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${hours}:${minutes}:${seconds}`
}

onMounted(() => {
  updateDateTime()
  timer = setInterval(updateDateTime, 1000)
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
  document.removeEventListener('click', handleClickOutside)
})

const menuList = computed(() => {
  const role = userStore.userInfo?.role || 'admin'
  return getMenusByRole(role)
})

const roleText = computed(() => {
  const role = userStore.userInfo?.role || 'admin'
  return roleNames[role] || role
})

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '')

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('退出成功')
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.layout-container {
  height: 100%;
}

.sidebar {
  background: #fff;
  border-right: 1px solid var(--border-light);
  transition: width var(--transition-normal);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 10px;
  padding: 0 20px;
  border-bottom: 1px solid var(--border-light);
  flex-shrink: 0;
}

.logo-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.3px;
  white-space: nowrap;
}

.sidebar-user {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.sidebar-user .user-avatar {
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  color: #fff;
  flex-shrink: 0;
}

.sidebar-user .user-info {
  flex: 1;
  min-width: 0;
}

.sidebar-user .user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-user .user-role {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.menu-section {
  flex: 1;
  padding: 12px 10px;
  overflow-y: auto;
}

.section-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-placeholder);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 8px 12px;
  margin-bottom: 4px;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
}

.sidebar-menu .el-menu-item {
  height: 40px;
  line-height: 40px;
  border-radius: 8px;
  margin-bottom: 2px;
  padding: 0 12px !important;
  font-size: 13px;
}

.sidebar-menu .el-menu-item .el-icon {
  margin-right: 10px !important;
  font-size: 16px;
}

.sidebar-menu .el-sub-menu__title {
  height: 40px;
  line-height: 40px;
  border-radius: 8px;
  margin-bottom: 2px;
  padding: 0 12px !important;
  font-size: 13px;
  font-weight: 500;
}

.sidebar-menu .el-sub-menu__title .el-icon {
  margin-right: 10px !important;
  font-size: 16px;
}

.sidebar-menu .el-sub-menu .el-menu-item {
  padding-left: 40px !important;
  height: 36px;
  line-height: 36px;
  font-size: 13px;
  border-radius: 6px;
  margin-bottom: 1px;
}

.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex: 1;
}

.header {
  background: #fff;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 60px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.collapse-btn {
  font-size: 18px;
  cursor: pointer;
  color: var(--text-secondary);
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.collapse-btn:hover {
  background: var(--bg-color);
  color: var(--primary-color);
}

.header-search {
  position: relative;
  display: flex;
  align-items: center;
  max-width: 360px;
  width: 100%;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: var(--text-placeholder);
  font-size: 16px;
  z-index: 1;
}

.search-input {
  width: 100%;
  height: 36px;
  padding: 0 16px 0 36px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-color);
  font-size: 13px;
  color: var(--text-primary);
  outline: none;
  transition: all var(--transition-fast);
}

.search-input:focus {
  border-color: var(--primary-color);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-input::placeholder {
  color: var(--text-placeholder);
}

.search-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid var(--border-light);
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  z-index: 1000;
  overflow: hidden;
}

.search-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  cursor: pointer;
  transition: background 0.15s ease;
}

.search-item:hover,
.search-item.active {
  background: var(--bg-color);
}

.item-icon {
  color: var(--primary-color);
  font-size: 16px;
  flex-shrink: 0;
}

.item-title {
  flex: 1;
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
}

.item-path {
  font-size: 11px;
  color: var(--text-placeholder);
  font-family: 'SF Mono', 'Consolas', monospace;
}

.search-empty {
  padding: 20px;
  text-align: center;
  font-size: 13px;
  color: var(--text-placeholder);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.header-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 18px;
  transition: all var(--transition-fast);
}

.header-icon:hover {
  background: var(--bg-color);
  color: var(--primary-color);
}

.header-datetime {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  padding: 0 16px;
  margin-right: 4px;
  border-right: 1px solid var(--border-light);
}

.date-text {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
}

.time-text {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 600;
  letter-spacing: 0.5px;
  font-family: 'SF Mono', 'Consolas', 'Monaco', monospace;
}

.user-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px 4px 4px;
  border-radius: 8px;
  transition: background var(--transition-fast);
  margin-left: 4px;
}

.user-dropdown-trigger:hover {
  background: var(--bg-color);
}

.header-avatar {
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  color: #fff;
  flex-shrink: 0;
}

.header-username {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-regular);
}

.arrow-icon {
  color: var(--text-placeholder);
  font-size: 12px;
}

.user-dropdown {
  min-width: 160px;
}

.content-wrapper {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.breadcrumb-bar {
  padding: 16px 24px 0;
  flex-shrink: 0;
}

.breadcrumb-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.3;
}

.breadcrumb {
  font-size: 12px;
}

.main-content {
  background: var(--bg-color);
  padding: 16px 24px 24px;
  overflow-y: auto;
  flex: 1;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
