<template>
  <div class="page-action-bar">
    <!-- 左侧操作按钮区域 -->
    <div class="action-buttons">
      <slot name="actions"></slot>
    </div>
    
    <!-- 右侧搜索区域 -->
    <div class="search-area">
      <slot name="search"></slot>
    </div>
  </div>
</template>

<script setup>
// 无需额外的逻辑，完全通过插槽来实现灵活性
</script>

<style scoped>
.page-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  gap: 20px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.search-area {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-action-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .action-buttons,
  .search-area {
    justify-content: flex-start;
  }
}
</style>