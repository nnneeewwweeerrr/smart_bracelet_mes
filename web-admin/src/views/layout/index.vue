<template>
  <el-container class="layout-container">
    <el-aside width="210px" class="aside">
      <div class="logo">MES系统</div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <el-sub-menu index="business">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>业务管理</span>
          </template>
          <el-menu-item index="/order">订单管理</el-menu-item>
          <el-menu-item index="/purchase">采购管理</el-menu-item>
          <el-menu-item index="/supplier">供应商管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="archive">
          <template #title>
            <el-icon><Folder /></el-icon>
            <span>基础档案</span>
          </template>
          <el-menu-item index="/customer">客户档案</el-menu-item>
          <el-menu-item index="/material">物料档案</el-menu-item>
          <el-menu-item index="/product">产品档案</el-menu-item>
          <el-menu-item index="/equipment">设备档案</el-menu-item>
          <el-menu-item index="/equipment-monitor">设备监控</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="warehouse">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>仓储管理</span>
          </template>
          <el-menu-item index="/stock">库存查询</el-menu-item>
          <el-menu-item index="/stock-in">入库记录</el-menu-item>
          <el-menu-item index="/stock-out">出库记录</el-menu-item>
          <el-menu-item index="/stock-flow">库存流水</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="production">
          <template #title>
            <el-icon><Tools /></el-icon>
            <span>生产管理</span>
          </template>
          <el-menu-item index="/work-order">生产工单</el-menu-item>
          <el-menu-item index="/production-progress">生产进度</el-menu-item>
          <el-menu-item index="/process-config">工艺配置</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="quality">
          <template #title>
            <el-icon><CircleCheck /></el-icon>
            <span>质量管理</span>
          </template>
          <el-menu-item index="/quality-record">质检记录</el-menu-item>
          <el-menu-item index="/exception-workbench">异常处理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="system">
          <template #title>
            <el-icon><FileText /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/operation-log">操作日志</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="username">{{ userStore.userInfo.realName || userStore.userInfo.userName }}</span>
        <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { HomeFilled, Document, Folder, Box, Tools, CircleCheck, Monitor, FileText } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.aside {
  background: #304156;
  overflow-y: auto;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #1f2d3d;
}
.header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.username {
  margin-right: 16px;
  color: #606266;
}
.main {
  background: #f0f2f5;
  padding: 20px;
}
</style>
