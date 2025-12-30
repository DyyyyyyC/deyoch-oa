<template>
  <div class="dashboard-container">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h2 class="welcome-title">{{ $t('dashboard.welcome', { username: userInfo.username || $t('common.anonymous') }) }}</h2>
        <p class="welcome-subtitle">{{ $t('dashboard.welcomeSubtitle', { date: currentDate }) }}</p>
        <div class="quick-stats">
          <div class="quick-stat-item">
            <span class="stat-value">12</span>
            <span class="stat-label">{{ $t('dashboard.pendingTasks') }}</span>
          </div>
          <div class="divider"></div>
          <div class="quick-stat-item">
            <span class="stat-value">3</span>
            <span class="stat-label">{{ $t('dashboard.todayMeetings') }}</span>
          </div>
        </div>
      </div>
      <div class="welcome-image">
        <el-icon :size="120" color="rgba(255,255,255,0.2)"><Calendar /></el-icon>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div v-for="stat in taskStats" :key="stat.key" class="stat-card-new" :style="{ '--accent-color': stat.color }">
        <div class="stat-icon-wrapper">
          <el-icon><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-details">
          <div class="stat-count">{{ stat.count }}</div>
          <div class="stat-name">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 主要内容布局 -->
    <div class="dashboard-grid">
      <!-- 左侧列 -->
      <div class="grid-left">
        <!-- 任务中心 -->
        <el-card class="modern-card task-section">
          <template #header>
            <div class="card-header task-header">
              <div class="header-title">
                <el-icon><List /></el-icon>
                <span>{{ $t('dashboard.taskCenter') }}</span>
              </div>
              <el-tabs v-model="activeTaskTab" class="modern-tabs">
                <el-tab-pane :label="$t('dashboard.pending')" name="pending"></el-tab-pane>
                <el-tab-pane :label="$t('dashboard.pendingReview')" name="pendingReview"></el-tab-pane>
                <el-tab-pane :label="$t('dashboard.completed')" name="completed"></el-tab-pane>
              </el-tabs>
            </div>
          </template>
          
          <div class="task-list-modern">
            <el-empty v-if="tasks.length === 0" :description="$t('common.noData')" />
            <div v-else v-for="(task, index) in tasks" :key="index" class="task-item-modern">
              <div class="task-status-dot" :class="task.type"></div>
              <div class="task-info">
                <div class="task-top">
                  <span class="task-title">{{ task.title }}</span>
                  <el-tag size="small" :type="task.type" effect="light">{{ task.status }}</el-tag>
                </div>
                <div class="task-desc">{{ task.description }}</div>
                <div class="task-bottom">
                  <el-icon><Clock /></el-icon>
                  <span>{{ task.date }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <el-button link type="primary">{{ $t('dashboard.viewAllTasks') }} <el-icon><ArrowRight /></el-icon></el-button>
          </div>
        </el-card>

        <!-- 日程安排 -->
        <el-card class="modern-card schedule-section">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><Calendar /></el-icon>
                <span>{{ $t('dashboard.schedule') }}</span>
              </div>
              <el-button type="primary" size="small" round>
                <el-icon><Plus /></el-icon>{{ $t('dashboard.addSchedule') }}
              </el-button>
            </div>
          </template>
          <div class="schedule-container">
            <div class="mini-calendar">
              <div class="calendar-nav">
                <el-button link size="small"><el-icon><ArrowLeft /></el-icon></el-button>
                <span class="month-label">{{ currentMonth }}</span>
                <el-button link size="small"><el-icon><ArrowRight /></el-icon></el-button>
              </div>
              <div class="calendar-days">
                <div v-for="d in ['日','一','二','三','四','五','六']" :key="d" class="day-head">{{ d }}</div>
                <div v-for="day in 31" :key="day" class="day-cell" :class="{ active: day === 29 }">
                  {{ day }}
                  <div v-if="[5, 12, 29].includes(day)" class="has-event"></div>
                </div>
              </div>
            </div>
            <div class="today-events">
              <div class="section-label">{{ $t('dashboard.todaySchedule') }}</div>
              <div v-for="(event, index) in todayEvents" :key="index" class="event-card-modern">
                <div class="event-time-box">
                  <span class="time">{{ event.time }}</span>
                </div>
                <div class="event-content">
                  <div class="event-title">{{ event.title }}</div>
                  <div class="event-loc">
                    <el-icon><Location /></el-icon> {{ event.location }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧列 -->
      <div class="grid-right">
        <!-- 会议安排 -->
        <el-card class="modern-card meeting-section">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><VideoCamera /></el-icon>
                <span>{{ $t('dashboard.meetingSchedule') }}</span>
              </div>
            </div>
          </template>
          <div class="meeting-list-modern">
            <el-empty v-if="meetings.length === 0" :description="$t('common.noData')" />
            <div v-for="(meeting, index) in meetings" :key="index" class="meeting-card-modern">
              <div class="meeting-date-badge">
                <span class="day">{{ meeting.date.split('-')[2] }}</span>
                <span class="month">12月</span>
              </div>
              <div class="meeting-info">
                <div class="meeting-title">{{ meeting.title }}</div>
                <div class="meeting-time-loc">{{ meeting.time }} | {{ meeting.location }}</div>
                <div class="meeting-footer">
                  <div class="avatars">
                    <el-avatar v-for="(p, i) in meeting.participants.slice(0, 3)" :key="i" :size="24">{{ p.name[0] }}</el-avatar>
                    <span v-if="meeting.participants.length > 3" class="more-count">+{{ meeting.participants.length - 3 }}</span>
                  </div>
                  <el-button type="primary" size="small" plain round>{{ $t('dashboard.joinMeeting') }}</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 最近文件 -->
        <el-card class="modern-card file-section">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><Files /></el-icon>
                <span>{{ $t('dashboard.recentFiles') }}</span>
              </div>
            </div>
          </template>
          <div class="file-list-modern">
            <div v-for="(file, index) in files" :key="index" class="file-item-modern">
              <div class="file-icon-box">
                <el-icon><Document /></el-icon>
              </div>
              <div class="file-details">
                <div class="file-name">{{ file.name }}</div>
                <div class="file-meta">{{ file.size }} · {{ file.date.split(' ')[0] }}</div>
              </div>
              <el-button link><el-icon><Download /></el-icon></el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, markRaw, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { useI18n } from 'vue-i18n'
import {
  Clock, Bell, CircleCheck, Checked, List,
  Plus, ArrowLeft, ArrowRight, Files, Download,
  Calendar, VideoCamera, Document, Location
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const { t } = useI18n()

// 当前日期格式化
const currentDate = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
})

// 当前月份
const currentMonth = ref('2025年12月')

// 用户信息
const userInfo = ref({})

// 任务统计数据
const taskStats = computed(() => [
  { key: 'pending', label: t('dashboard.stats.pendingTask'), count: 12, icon: markRaw(Clock), color: '#409eff' },
  { key: 'pendingReview', label: t('dashboard.stats.pendingReview'), count: 8, icon: markRaw(Bell), color: '#67c23a' },
  { key: 'completed', label: t('dashboard.stats.completedTask'), count: 24, icon: markRaw(CircleCheck), color: '#e6a23c' },
  { key: 'reviewed', label: t('dashboard.stats.reviewedRecord'), count: 16, icon: markRaw(Checked), color: '#909399' }
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
.dashboard-container {
  padding: 24px;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  border-radius: 16px;
  padding: 32px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.2);
}

.welcome-title {
  font-size: 28px;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.welcome-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 24px 0;
}

.quick-stats {
  display: flex;
  gap: 32px;
  align-items: center;
}

.quick-stat-item {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
}

.stat-label {
  font-size: 12px;
  opacity: 0.8;
}

.divider {
  width: 1px;
  height: 30px;
  background: rgba(255, 255, 255, 0.3);
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card-new {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid #f0f0f0;
}

.stat-card-new:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.05);
}

.stat-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background-color: var(--accent-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.stat-count {
  font-size: 24px;
  font-weight: bold;
  color: #262626;
}

.stat-name {
  font-size: 14px;
  color: #8c8c8c;
}


/* 布局网格 */
.dashboard-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.grid-left, .grid-right {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 通用卡片样式 */
.modern-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-header {
  flex-wrap: wrap;
  gap: 12px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
  color: #262626;
}

.header-title .el-icon {
  color: #1890ff;
}

/* 任务列表 */
.task-list-modern {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.task-item-modern {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.task-item-modern:hover {
  background: #f5f7fa;
}

.task-status-dot {
  width: 4px;
  height: 40px;
  border-radius: 2px;
}

.task-status-dot.warning { background: #faad14; }
.task-status-dot.danger { background: #ff4d4f; }
.task-status-dot.info { background: #909399; }

.task-info { flex: 1; }
.task-top { display: flex; justify-content: space-between; margin-bottom: 4px; }
.task-title { font-weight: 500; color: #262626; }
.task-desc { font-size: 13px; color: #8c8c8c; margin-bottom: 8px; }
.task-bottom { font-size: 12px; color: #bfbfbf; display: flex; align-items: center; gap: 4px; }

.card-footer {
  margin-top: 16px;
  text-align: center;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

/* 日程安排 */
.schedule-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.mini-calendar {
  background: #fafafa;
  padding: 16px;
  border-radius: 8px;
}

.calendar-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.month-label { font-weight: 600; font-size: 14px; }

.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  text-align: center;
}

.day-head { font-size: 12px; color: #bfbfbf; padding-bottom: 8px; }
.day-cell {
  font-size: 12px;
  height: 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 4px;
  position: relative;
}

.day-cell:hover { background: #e6f7ff; color: #1890ff; }
.day-cell.active { background: #1890ff; color: white; }

.has-event {
  width: 4px;
  height: 4px;
  background: #ff4d4f;
  border-radius: 50%;
  position: absolute;
  bottom: 2px;
}

.event-card-modern {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.event-time-box {
  min-width: 50px;
  font-weight: 600;
  color: #1890ff;
  font-size: 13px;
}

.event-title { font-size: 14px; font-weight: 500; margin-bottom: 2px; }
.event-loc { font-size: 12px; color: #8c8c8c; display: flex; align-items: center; gap: 4px; }

/* 会议卡片 */
.meeting-card-modern {
  display: flex;
  gap: 16px;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.meeting-card-modern:last-child { border-bottom: none; }

.meeting-date-badge {
  width: 50px;
  height: 50px;
  background: #e6f7ff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #1890ff;
}

.meeting-date-badge .day { font-size: 18px; font-weight: bold; line-height: 1; }
.meeting-date-badge .month { font-size: 10px; }

.meeting-info { flex: 1; }
.meeting-title { font-weight: 500; margin-bottom: 4px; }
.meeting-time-loc { font-size: 12px; color: #8c8c8c; margin-bottom: 8px; }
.meeting-footer { display: flex; justify-content: space-between; align-items: center; }

.avatars { display: flex; align-items: center; }
.avatars .el-avatar { border: 2px solid white; margin-right: -8px; }
.more-count { font-size: 12px; color: #8c8c8c; margin-left: 12px; }

/* 文件列表 */
.file-item-modern {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.file-item-modern:hover { background: #f5f7fa; }

.file-icon-box {
  width: 40px;
  height: 40px;
  background: #f0f5ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #2f54eb;
  font-size: 20px;
}

.file-details { flex: 1; }
.file-name { font-size: 14px; font-weight: 500; color: #262626; }
.file-meta { font-size: 12px; color: #8c8c8c; }

/* 响应式 */
@media (max-width: 1200px) {
  .dashboard-grid { grid-template-columns: 1fr; }
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: 1fr; }
  .schedule-container { grid-template-columns: 1fr; }
  .welcome-banner { flex-direction: column; text-align: center; }
  .welcome-image { display: none; }
  .quick-stats { margin-top: 20px; }
}
</style>