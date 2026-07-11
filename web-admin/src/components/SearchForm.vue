<template>
  <el-form :model="formData" :inline="true" class="search-form">
    <slot></slot>
    <el-form-item>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <slot name="extra"></slot>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive, watch } from 'vue'

const props = defineProps({
  initialForm: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['search', 'reset', 'update:formData'])

const formData = reactive({ ...props.initialForm })

watch(() => props.initialForm, (val) => {
  Object.assign(formData, val)
}, { deep: true })

function handleSearch() {
  emit('search', { ...formData })
}

function handleReset() {
  Object.assign(formData, props.initialForm)
  emit('reset', { ...formData })
}

defineExpose({
  formData,
  reset: handleReset
})
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}
</style>
