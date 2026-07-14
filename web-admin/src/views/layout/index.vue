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
          <el-dropdown 
            ref="notificationRef"
            @command="handleNotificationCommand" 
            trigger="click"
            @visible-change="handleNotificationVisibleChange"
          >
            <div class="notification-trigger">
              <el-icon class="notification-icon"><Bell /></el-icon>
              <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
            </div>
            <template #dropdown>
              <div class="notification-dropdown">
                <div class="notification-header">
                  <span class="notification-title">实时消息</span>
                  <span class="notification-action" @click.stop="markAllAsRead">全部已读</span>
                </div>
                <div v-if="notificationList.length === 0" class="notification-empty">
                  <el-icon :size="48" class="empty-icon"><Bell /></el-icon>
                  <span>暂无消息</span>
                </div>
                <div v-else class="notification-list">
                  <div 
                    v-for="item in notificationList" 
                    :key="item.id"
                    class="notification-item"
                    :class="{ 'notification-unread': !item.read }"
                    @click="handleNotificationClick(item)"
                  >
                    <div class="notification-icon-wrapper" :class="getNotificationIconClass(item.type)">
                      <el-icon><component :is="getNotificationIcon(item.type)" /></el-icon>
                    </div>
                    <div class="notification-content">
                      <div class="notification-title-text">{{ getNotificationTitle(item.type) }}</div>
                      <div class="notification-desc">{{ getNotificationDesc(item) }}</div>
                      <div class="notification-time">{{ item.time }}</div>
                    </div>
                  </div>
                </div>
                <div v-if="notificationList.length > 0" class="notification-footer">
                  <span class="notification-action" @click.stop="goToMessagePage">查看更多</span>
                </div>
              </div>
            </template>
          </el-dropdown>
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
    <AiAssistant />
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Fold, Expand, ArrowDown, UserFilled, SwitchButton,
  Search, Watch, Key, Monitor, Bell, Warning, TrendCharts, CircleCheck, CircleClose
} from '@element-plus/icons-vue'
import { getMenusByRole, roleNames } from '../../config/roleMenu'
import AiAssistant from '../../components/AiAssistant.vue'
import wsClient from '../../utils/websocket'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const currentDate = ref('')
const currentTime = ref('')
let timer = null

const unreadCount = ref(0)
const notificationList = ref([])
const notificationRef = ref(null)

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
  initNotifications()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('message-update', handleMessageUpdate)
})

function initNotifications() {
  notificationList.value = wsClient.getMessages()
  unreadCount.value = wsClient.getUnreadCount()
  document.addEventListener('message-update', handleMessageUpdate)
}

function handleMessageUpdate(e) {
  notificationList.value = e.detail.messages
  unreadCount.value = e.detail.count
}

function handleNotificationCommand(command) {
  if (command === 'mark-all-read') {
    markAllAsRead()
  }
}

function handleNotificationVisibleChange(visible) {
  if (visible) {
    notificationList.value = wsClient.getMessages().slice(0, 10)
  }
}

function handleNotificationClick(item) {
  wsClient.markAsRead(item.id)
  unreadCount.value = wsClient.getUnreadCount()
  if (item.type === 'NEW_EXCEPTION') {
    router.push('/exception-workbench')
  } else if (item.type === 'PROGRESS_UPDATE') {
    router.push('/production-progress')
  }
}

function markAllAsRead() {
  wsClient.markAllAsRead()
  unreadCount.value = 0
  notificationList.value = wsClient.getMessages().slice(0, 10)
  ElMessage.success('已全部标记为已读')
}

function goToMessagePage() {
  router.push('/exception-workbench')
}

function getNotificationIcon(type) {
  const icons = {
    NEW_EXCEPTION: Warning,
    EXCEPTION_HANDLED: CircleCheck,
    EXCEPTION_RESOLVED: CircleCheck,
    PROGRESS_UPDATE: TrendCharts
  }
  return icons[type] || Bell
}

function getNotificationIconClass(type) {
  const classes = {
    NEW_EXCEPTION: 'icon-warning',
    EXCEPTION_HANDLED: 'icon-info',
    EXCEPTION_RESOLVED: 'icon-success',
    PROGRESS_UPDATE: 'icon-success'
  }
  return classes[type] || 'icon-info'
}

function getNotificationTitle(type) {
  const titles = {
    NEW_EXCEPTION: '新异常告警',
    EXCEPTION_HANDLED: '异常已处理',
    EXCEPTION_RESOLVED: '异常已解决',
    PROGRESS_UPDATE: '生产进度更新'
  }
  return titles[type] || '系统消息'
}

function getNotificationDesc(item) {
  const payload = item.payload
  if (item.type === 'NEW_EXCEPTION') {
    const typeNames = ['', '设备故障', '质量异常', '物料短缺', '工艺问题', '人员异常', '其他']
    return `【${typeNames[payload.exceptionType] || payload.exceptionTypeName || '异常'}】${payload.workOrderNo} - ${(payload.exceptionDesc || '').slice(0, 30)}${(payload.exceptionDesc || '').length > 30 ? '...' : ''}`
  } else if (item.type === 'EXCEPTION_HANDLED') {
    return `异常 ${payload.exceptionNo} 已开始处理`
  } else if (item.type === 'EXCEPTION_RESOLVED') {
    return `异常 ${payload.exceptionNo} 已解决`
  } else if (item.type === 'PROGRESS_UPDATE') {
    return `工单 ${payload.workOrderNo} 在 ${payload.processName} 工序新增报工 ${payload.addQuantity} 件`
  }
  return '查看详情'
}

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

.notification-trigger {
  position: relative;
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
  margin-left: 4px;
}

.notification-trigger:hover {
  background: var(--bg-color);
  color: var(--primary-color);
}

.notification-icon {
  animation: bell-pulse 3s infinite;
}

@keyframes bell-pulse {
  0%, 100% { transform: rotate(0deg); }
  10% { transform: rotate(-10deg); }
  20% { transform: rotate(10deg); }
  30% { transform: rotate(-10deg); }
  40% { transform: rotate(0deg); }
}

.notification-badge {
  position: absolute;
  top: -2px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #f56c6c;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: badge-bounce 2s infinite;
}

@keyframes badge-bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.notification-dropdown {
  min-width: 360px;
  max-height: 480px;
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-light);
  background: #fff;
}

.notification-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.notification-action {
  font-size: 12px;
  color: var(--primary-color);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.notification-action:hover {
  color: var(--primary-dark);
}

.notification-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--text-placeholder);
  font-size: 13px;
}

.empty-icon {
  color: var(--border-color);
  margin-bottom: 12px;
}

.notification-list {
  flex: 1;
  overflow-y: auto;
  background: #fafafa;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 20px;
  cursor: pointer;
  transition: background var(--transition-fast);
  border-bottom: 1px solid var(--border-light);
}

.notification-item:hover {
  background: #fff;
}

.notification-unread {
  background: rgba(59, 130, 246, 0.03);
}

.notification-icon-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 16px;
}

.notification-icon-wrapper.icon-warning {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.notification-icon-wrapper.icon-success {
  background: rgba(82, 196, 26, 0.1);
  color: #52c41a;
}

.notification-icon-wrapper.icon-info {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title-text {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.notification-desc {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-time {
  font-size: 11px;
  color: var(--text-placeholder);
  margin-top: 6px;
}

.notification-footer {
  padding: 12px 20px;
  border-top: 1px solid var(--border-light);
  background: #fff;
  text-align: center;
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
