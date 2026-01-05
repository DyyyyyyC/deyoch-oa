<template>
  <!-- 使用el-config-provider包裹整个应用，用于动态切换Element Plus语言 -->
  <el-config-provider :locale="elementLocale">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
// 导入Element Plus语言包
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import en from 'element-plus/dist/locale/en.mjs'

// 获取当前语言
const { locale } = useI18n()

// 计算当前的Element Plus语言包
const elementLocale = computed(() => {
  return locale.value === 'zh-CN' ? zhCn : en
})
</script>

<style>
/* 全局样式已移至style.css */
</style>