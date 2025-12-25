<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <span class="language-switch">
      {{ currentLanguage }}
      <el-icon class="el-icon--right"><arrow-down /></el-icon>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="zh-CN">中文</el-dropdown-item>
        <el-dropdown-item command="en">English</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowDown } from '@element-plus/icons-vue'

const { locale } = useI18n()

const currentLanguage = computed(() => {
  return locale.value === 'zh-CN' ? '中文' : 'English'
})

const handleCommand = (command) => {
  locale.value = command
  localStorage.setItem('locale', command)
}
</script>

<style scoped>
.language-switch {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.el-icon--right {
  margin-left: 5px;
}
</style>