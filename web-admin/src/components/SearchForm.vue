<template>
  <el-form :model="model" inline class="search-form">
    <slot />
    <el-form-item>
      <el-button type="primary" @click="emit('search')">查询</el-button>
      <el-button @click="emit('reset')">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
defineProps({
  model: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['search', 'reset'])
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 4px;
}
</style>