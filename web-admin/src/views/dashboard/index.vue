<template>
  <div>
    <h1>欢迎使用智能手环MES系统</h1>
    <p>当前用户：{{ userStore.userInfo.realName || userStore.userInfo.userName }}</p>
  </div>
</template>

<script setup>
import { useUserStore } from '../../stores/user'
const userStore = useUserStore()
</script>