<template>
  <div class="dashboard-container">
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <span>{{ $t('dashboard.welcome') }}</span>
        </div>
      </template>
      <div class="welcome-content">
        <h2>{{ $t('dashboard.welcomeMessage', { username: userInfo?.username || $t('common.anonymous') }) }}</h2>
        <p>{{ $t('dashboard.todayDate', { date: formattedDate }) }}</p>
      </div>
    </el-card>

    <div class="stats-grid">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <h3 class="stat-title">{{ $t('dashboard.totalUsers') }}</h3>
            <p class="stat-number">{{ totalUsers }}</p>
          </div>
          <el-icon class="stat-icon"><user /></el-icon>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <h3 class="stat-title">{{ $t('dashboard.todayActive') }}</h3>
            <p class="stat-number">{{ todayActive }}</p>
          </div>
          <el-icon class="stat-icon"><view /></el-icon>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <h3 class="stat-title">{{ $t('dashboard.totalTasks') }}</h3>
            <p class="stat-number">{{ totalTasks }}</p>
          </div>
          <el-icon class="stat-icon"><document /></el-icon>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <h3 class="stat-title">{{ $t('dashboard.pendingTasks') }}</h3>
            <p class="stat-number">{{ pendingTasks }}</p>
          </div>
          <el-icon class="stat-icon"><timer /></el-icon>
        </div>
      </el-card>
    </div>

    <div class="charts-grid">
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>{{ $t('dashboard.recentActivities') }}</span>
          </div>
        </template>
        <div class="activity-list">
          <el-timeline>
            <el-timeline-item v-for="(activity, index) in recentActivities" :key="index">
              <h4>{{ activity.title }}</h4>
              <p class="activity-time">{{ formatTime(activity.time) }}</p>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-card>

      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>{{ $t('dashboard.upcomingEvents') }}</span>
          </div>
        </template>
        <div class="event-list">
          <el-empty v-if="upcomingEvents.length === 0" description="{{ $t('common.noData') }}" />
          <div v-else>
            <div v-for="(event, index) in upcomingEvents" :key="index" class="event-item">
              <el-alert
                :title="event.title"
                :description="event.description"
                :type="event.type || 'info'"
                show-icon
                :closable="false"
              />
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useI18n } from 'vue-i18n'
import { User, View, Document, Timer } from '@element-plus/icons-vue'

const userStore = useUserStore()
const { t } = useI18n()

// 用户信息
const userInfo = ref(null)

// 统计数据
const totalUsers = ref(1258)
const todayActive = ref(342)
const totalTasks = ref(568)
const pendingTasks = ref(128)

// 最近活动
const recentActivities = ref([
  { title: t('dashboard.activity1'), time: Date.now() - 3600000 },
  { title: t('dashboard.activity2'), time: Date.now() - 7200000 },
  { title: t('dashboard.activity3'), time: Date.now() - 10800000 },
  { title: t('dashboard.activity4'), time: Date.now() - 14400000 },
  { title: t('dashboard.activity5'), time: Date.now() - 18000000 }
])

// 即将到来的事件
const upcomingEvents = ref([
  { 
    title: t('dashboard.event1Title'), 
    description: t('dashboard.event1Desc'), 
    type: 'warning' 
  },
  { 
    title: t('dashboard.event2Title'), 
    description: t('dashboard.event2Desc'), 
    type: 'info' 
  }
])

// 格式化日期
const formattedDate = computed(() => {
  const date = new Date()
  return date.toLocaleDateString()
})

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleString()
}

onMounted(() => {
  userInfo.value = userStore.userInfo
  // 可以在这里添加获取真实数据的API调用
})
</script>

<style scoped>
.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.welcome-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-content {
  text-align: center;
  padding: 20px 0;
}

.welcome-content h2 {
  font-size: 24px;
  margin: 0 0 10px 0;
  color: #303133;
}

.welcome-content p {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  height: 100%;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.12), 0 4px 8px rgba(0, 0, 0, 0.06);
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #606266;
  margin: 0 0 10px 0;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.stat-icon {
  font-size: 40px;
  color: #409eff;
  opacity: 0.8;
}

.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.chart-card {
  height: 100%;
}

.activity-list {
  max-height: 300px;
  overflow-y: auto;
}

.activity-time {
  color: #909399;
  font-size: 14px;
  margin: 5px 0 0 0;
}

.event-list {
  max-height: 300px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.event-item {
  margin-bottom: 10px;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>