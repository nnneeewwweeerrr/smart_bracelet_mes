<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>库存流水</span>
        </div>
      </template>
      <el-empty description="库存流水 - 开发中..." />
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.page-container {
  width: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
