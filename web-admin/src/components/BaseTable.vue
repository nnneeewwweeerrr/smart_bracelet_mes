<template>
  <el-table :data="data" v-loading="loading" border>
    <slot />
    <template #empty>
      <el-empty description="暂无数据" />
    </template>
  </el-table>
  <div class="pagination" v-if="showPagination">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="handleChange"
      @current-change="handleChange"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: { type: Array, default: () => [] },
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  size: { type: Number, default: 10 },
  loading: { type: Boolean, default: false },
  showPagination: { type: Boolean, default: true }
})

const emit = defineEmits(['update:page', 'update:size', 'change'])

const currentPage = computed({
  get: () => props.page,
  set: (val) => emit('update:page', val)
})

const pageSize = computed({
  get: () => props.size,
  set: (val) => emit('update:size', val)
})

const handleChange = () => {
  emit('change', currentPage.value, pageSize.value)
}
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>