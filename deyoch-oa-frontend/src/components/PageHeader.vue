<template>
  <div class="page-header">
    <div class="header-content">
      <h2 class="page-title">{{ title }}</h2>
      <div v-if="$slots.extra" class="header-extra">
        <slot name="extra"></slot>
      </div>
    </div>
    <div v-if="description" class="page-description">
      {{ description }}
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.header-extra {
  display: flex;
  gap: 10px;
  align-items: center;
}

.page-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
}
</style>