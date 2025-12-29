<template>
  <div class="dashboard-container">
    <!-- 任务统计卡片 -->
    <div class="task-stats-section">
      <el-card class="stat-card" v-for="stat in taskStats" :key="stat.key">
        <div class="stat-content">
          <div class="stat-info">
            <h3 class="stat-number">{{ stat.count }}</h3>
            <p class="stat-label">{{ stat.label }}</p>
          </div>
          <div class="stat-icon">
            <component :is="stat.icon" />
          </div>
        </div>
      </el-card>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 待办待阅区域 -->
      <el-card class="content-card task-section">
        <template #header>
          <div class="card-header">
            <h3 class="card-title">任务中心</h3>
            <el-tabs v-model="activeTaskTab" class="task-tabs">
              <el-tab-pane label="待办" name="pending"></el-tab-pane>
              <el-tab-pane label="待阅" name="pendingReview"></el-tab-pane>
              <el-tab-pane label="已办" name="completed"></el-tab-pane>
              <el-tab-pane label="已阅" name="reviewed"></el-tab-pane>
            </el-tabs>
          </div>
        </template>
        <div class="task-list">
          <el-empty v-if="tasks.length === 0" description="暂无任务" />
          <el-timeline v-else>
            <el-timeline-item v-for="(task, index) in tasks" :key="index">
              <div class="task-item">
                <h4 class="task-title">{{ task.title }}</h4>
                <p class="task-desc">{{ task.description }}</p>
                <div class="task-meta">
                  <span class="task-date">{{ task.date }}</span>
                  <el-tag :type="task.type">{{ task.status }}</el-tag>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-card>

      <!-- 日程区域 -->
      <el-card class="content-card schedule-section">
        <template #header>
          <div class="card-header">
            <h3 class="card-title">日程安排</h3>
            <el-button type="primary" size="small">
              <el-icon><Plus /></el-icon>
              添加日程
            </el-button>
          </div>
        </template>
        <div class="schedule-content">
          <div class="calendar-view">
            <!-- 日历视图 -->
            <div class="calendar-header">
              <el-button link size="small">
              <el-icon><ArrowLeft /></el-icon>
            </el-button>
            <span class="calendar-title">{{ currentMonth }}</span>
            <el-button link size="small">
              <el-icon><ArrowRight /></el-icon>
            </el-button>
            </div>
            <div class="calendar-grid">
              <div class="calendar-day" v-for="day in 7" :key="day">
                <div class="day-number">{{ day }}</div>
                <div class="day-events">
                  <div class="event-dot" v-for="n in Math.floor(Math.random() * 3)" :key="n"></div>
                </div>
              </div>
            </div>
          </div>
          <div class="today-schedule">
            <h4 class="schedule-subtitle">今日安排</h4>
            <div class="event-item" v-for="(event, index) in todayEvents" :key="index">
              <div class="event-time">{{ event.time }}</div>
              <div class="event-info">
                <h5 class="event-title">{{ event.title }}</h5>
                <p class="event-location">{{ event.location }}</p>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 会议区域 -->
      <el-card class="content-card meeting-section">
        <template #header>
          <div class="card-header">
            <h3 class="card-title">会议安排</h3>
          </div>
        </template>
        <div class="meeting-list">
          <el-empty v-if="meetings.length === 0" description="暂无会议" />
          <div class="meeting-item" v-for="(meeting, index) in meetings" :key="index">
            <div class="meeting-time">
              <div class="meeting-hour">{{ meeting.time }}</div>
              <div class="meeting-date">{{ meeting.date }}</div>
            </div>
            <div class="meeting-info">
              <h4 class="meeting-title">{{ meeting.title }}</h4>
              <p class="meeting-location">{{ meeting.location }}</p>
              <div class="meeting-participants">
                <el-avatar :size="24" v-for="(participant, i) in meeting.participants" :key="i" :src="participant.avatar">{{ participant.name.charAt(0) }}</el-avatar>
                <span class="participant-count" v-if="meeting.participants.length > 3">+{{ meeting.participants.length - 3 }}</span>
              </div>
            </div>
            <el-button type="primary" size="small">加入会议</el-button>
          </div>
        </div>
      </el-card>

      <!-- 文件区域 -->
      <el-card class="content-card file-section">
        <template #header>
          <div class="card-header">
            <h3 class="card-title">最近文件</h3>
          </div>
        </template>
        <div class="file-list">
          <el-empty v-if="files.length === 0" description="暂无文件" />
          <div class="file-item" v-for="(file, index) in files" :key="index">
            <el-icon class="file-icon"><component :is="file.icon" /></el-icon>
            <div class="file-info">
              <h4 class="file-name">{{ file.name }}</h4>
              <p class="file-meta">{{ file.size }} • {{ file.date }}</p>
            </div>
            <el-button link size="small">
              <el-icon><Download /></el-icon>
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, markRaw } from 'vue'
import { useUserStore } from '@/stores/user'
import {
  Clock, Bell, CircleCheck, Checked, List,
  Plus, ArrowLeft, ArrowRight, Files, Download
} from '@element-plus/icons-vue'

const userStore = useUserStore()

// 当前月份
const currentMonth = ref('2025年12月')

// 用户信息
const userInfo = ref({})

// 任务统计数据
const taskStats = ref([
  { key: 'pending', label: '待办', count: 12, icon: markRaw(Clock), color: '#409eff' },
  { key: 'pendingReview', label: '待阅', count: 8, icon: markRaw(Bell), color: '#67c23a' },
  { key: 'completed', label: '已办', count: 24, icon: markRaw(CircleCheck), color: '#e6a23c' },
  { key: 'reviewed', label: '已阅', count: 16, icon: markRaw(Checked), color: '#909399' }
])

// 活动标签页
const activeTaskTab = ref('pending')

// 任务列表数据
const tasks = ref([
  {
    title: '项目方案评审',
    description: '请对项目A的实施方案进行评审并提供反馈',
    date: '2025-12-29 14:30',
    status: '待办',
    type: 'warning'
  },
  {
    title: '季度工作总结',
    description: '提交第三季度的工作总结报告',
    date: '2025-12-30 17:00',
    status: '待办',
    type: 'danger'
  },
  {
    title: '新员工入职培训',
    description: '参加新员工入职培训会议',
    date: '2025-12-29 09:00',
    status: '待办',
    type: 'info'
  }
])

// 今日日程
const todayEvents = ref([
  {
    time: '09:00',
    title: '团队晨会',
    location: '会议室A'
  },
  {
    time: '14:30',
    title: '项目评审会',
    location: '会议室B'
  },
  {
    time: '16:00',
    title: '技术分享',
    location: '线上会议'
  }
])

// 会议安排
const meetings = ref([
  {
    time: '09:00-10:00',
    date: '2025-12-29',
    title: '周度项目例会',
    location: '会议室A',
    participants: [
      { name: '张三', avatar: '' },
      { name: '李四', avatar: '' },
      { name: '王五', avatar: '' },
      { name: '赵六', avatar: '' }
    ]
  },
  {
    time: '14:30-16:00',
    date: '2025-12-30',
    title: '产品需求评审',
    location: '会议室B',
    participants: [
      { name: '张三', avatar: '' },
      { name: '李四', avatar: '' },
      { name: '王五', avatar: '' }
    ]
  }
])

// 最近文件
const files = ref([
  {
    name: '项目实施方案.docx',
    size: '2.3 MB',
    date: '2025-12-28 15:30',
    icon: markRaw(Files)
  },
  {
    name: '季度工作总结.pptx',
    size: '5.6 MB',
    date: '2025-12-27 14:15',
    icon: markRaw(Files)
  },
  {
    name: '技术架构设计.pdf',
    size: '3.8 MB',
    date: '2025-12-26 11:00',
    icon: markRaw(Files)
  },
  {
    name: '团队成员名单.xlsx',
    size: '1.2 MB',
    date: '2025-12-25 09:45',
    icon: markRaw(Files)
  }
])

onMounted(() => {
  userInfo.value = userStore.userInfo
  // 可以在这里添加获取真实数据的API调用
})
</script>

<style scoped>
/* CSS变量定义 */
:root {
  --primary-color: #409eff;
  --secondary-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --info-color: #909399;
  --bg-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --card-bg: rgba(255, 255, 255, 0.95);
  --text-primary: #303133;
  --text-secondary: #606266;
  --border-color: #e4e7ed;
  --shadow-light: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  --shadow-hover: 0 10px 30px rgba(0, 0, 0, 0.15);
  --transition: all 0.3s ease;
}

/* 全局容器样式 */
.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 0;
  min-height: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  animation: fadeIn 0.5s ease-in;
}

/* 任务统计卡片 */
.task-stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  animation: fadeInUp 0.6s ease-out 0.1s both;
}

.stat-card {
  background: var(--card-bg);
  border: none;
  border-radius: 12px;
  box-shadow: var(--shadow-light);
  transition: var(--transition);
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-hover);
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28px 24px;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
  font-weight: 500;
}

.stat-icon {
  font-size: 48px;
  opacity: 0.8;
  transition: var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon svg {
  width: 48px;
  height: 48px;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1);
}

/* 主要内容区域 */
.main-content {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

/* 内容卡片 */
.content-card {
  background: var(--card-bg);
  border: none;
  border-radius: 12px;
  box-shadow: var(--shadow-light);
  transition: var(--transition);
  backdrop-filter: blur(10px);
}

.content-card:hover {
  box-shadow: var(--shadow-hover);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

/* 任务标签页 */
.task-tabs {
  margin-left: auto;
}

.task-tabs .el-tabs__nav-wrap {
  margin: 0;
}

/* 任务列表 */
.task-list {
  max-height: 400px;
  overflow-y: auto;
}

.task-item {
  padding: 16px 0;
}

.task-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.task-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 12px 0;
  line-height: 1.5;
}

.task-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-date {
  font-size: 13px;
  color: var(--info-color);
}

/* 日程区域 */
.schedule-content {
  display: flex;
  gap: 24px;
}

.calendar-view {
  flex: 1;
  min-width: 0;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.calendar-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12px;
}

.calendar-day {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
  transition: var(--transition);
}

.calendar-day:hover {
  background: #e9ecef;
}

.day-number {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.day-events {
  display: flex;
  justify-content: center;
  gap: 4px;
}

.event-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--primary-color);
}

.today-schedule {
  flex: 1;
  min-width: 0;
}

.schedule-subtitle {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 16px 0;
}

.event-item {
  display: flex;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
}

.event-item:last-child {
  border-bottom: none;
}

.event-time {
  font-size: 14px;
  font-weight: 600;
  color: var(--primary-color);
  min-width: 80px;
  display: flex;
  align-items: center;
}

.event-info {
  flex: 1;
  min-width: 0;
}

.event-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px 0;
}

.event-location {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

/* 会议区域 */
.meeting-list {
  max-height: 400px;
  overflow-y: auto;
}

.meeting-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 0;
  border-bottom: 1px solid var(--border-color);
  transition: var(--transition);
}

.meeting-item:last-child {
  border-bottom: none;
}

.meeting-item:hover {
  background: #f8f9fa;
  padding-left: 12px;
  border-radius: 8px;
}

.meeting-time {
  text-align: center;
  min-width: 100px;
}

.meeting-hour {
  font-size: 16px;
  font-weight: 600;
  color: var(--primary-color);
  margin: 0 0 4px 0;
}

.meeting-date {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

.meeting-info {
  flex: 1;
  min-width: 0;
}

.meeting-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.meeting-location {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 12px 0;
}

.meeting-participants {
  display: flex;
  align-items: center;
  gap: 8px;
}

.participant-count {
  font-size: 12px;
  color: var(--text-secondary);
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 10px;
}

/* 文件区域 */
.file-list {
  max-height: 400px;
  overflow-y: auto;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
  transition: var(--transition);
}

.file-item:last-child {
  border-bottom: none;
}

.file-item:hover {
  background: #f8f9fa;
  padding-left: 12px;
  border-radius: 8px;
}

.file-icon {
  font-size: 28px;
  color: var(--primary-color);
  min-width: 40px;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-meta {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

/* 动画定义 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-container {
    padding: 16px;
    gap: 16px;
  }

  .welcome-section {
    padding: 32px 24px;
  }

  .welcome-title {
    font-size: 28px;
  }

  .task-stats-section {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .stat-content {
    padding: 20px 16px;
  }

  .stat-number {
    font-size: 28px;
  }

  .schedule-content {
    flex-direction: column;
    gap: 20px;
  }

  .calendar-grid {
    gap: 8px;
  }

  .calendar-day {
    padding: 8px;
  }

  .meeting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 16px;
  }

  .meeting-time {
    text-align: left;
    min-width: auto;
  }
}

@media (max-width: 480px) {
  .task-stats-section {
    grid-template-columns: 1fr;
  }

  .welcome-title {
    font-size: 24px;
  }

  .stat-number {
    font-size: 32px;
  }
}
</style>