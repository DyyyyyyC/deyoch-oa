<template>
  <div class="dashboard-container">
    <!-- 加载状态 -->
    <el-skeleton :loading="loading" animated>
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h2 class="welcome-title">{{ $t('dashboard.welcome', { username: userInfo.username || $t('common.anonymous') }) }}</h2>
        <p class="welcome-subtitle">{{ $t('dashboard.welcomeSubtitle', { date: currentDate }) }}</p>
        <div class="quick-stats">
          <div class="quick-stat-item">
            <span class="stat-value">{{ taskStats.find(s => s.key === 'pending')?.count || 0 }}</span>
            <span class="stat-label">{{ $t('dashboard.pendingTasks') }}</span>
          </div>
          <div class="divider"></div>
          <div class="quick-stat-item">
            <span class="stat-value">{{ selectedDateEvents.length }}</span>
            <span class="stat-label">{{ $t('dashboard.todayMeetings') }}</span>
          </div>
        </div>
      </div>
      <div class="welcome-image">
        <el-icon :size="120" color="rgba(255,255,255,0.2)"><Calendar /></el-icon>
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
                <el-tab-pane :label="`${$t('dashboard.pending')} (${taskStats.find(s => s.key === 'pending')?.count || 0})`" name="pending"></el-tab-pane>
                <el-tab-pane :label="`${$t('dashboard.pendingReview')} (${taskStats.find(s => s.key === 'pendingReview')?.count || 0})`" name="pendingReview"></el-tab-pane>
                <el-tab-pane :label="`${$t('dashboard.completed')} (${taskStats.find(s => s.key === 'completed')?.count || 0})`" name="completed"></el-tab-pane>
              </el-tabs>
            </div>
          </template>
          
          <div class="task-list-modern">
            <el-empty v-if="filteredTasks.length === 0" :description="$t('common.noData')" />
            <div v-else v-for="(task, index) in filteredTasks" :key="index" class="task-item-modern">
              <!-- 根据优先级显示不同颜色的状态点 -->
              <div class="task-status-dot" :class="task.priority === '紧急' ? 'danger' : task.priority === '高' ? 'warning' : 'info'"></div>
              <div class="task-info">
                <div class="task-top">
                  <span class="task-title">{{ task.title }}</span>
                  <div class="task-tags">
                    <el-tag size="small" :type="task.priority === '紧急' ? 'danger' : task.priority === '高' ? 'warning' : 'info'" effect="light">
                      {{ task.status }}
                    </el-tag>
                    <el-tag size="small" type="success" effect="light">
                      {{ task.priority }}
                    </el-tag>
                  </div>
                </div>
                <div class="task-desc">{{ task.description }}</div>
                <div class="task-meta">
                  <div class="meta-item">
                    <el-icon class="small-icon"><User /></el-icon>
                    <span>{{ task.assignee }}</span>
                  </div>
                  <div class="meta-item">
                    <el-icon class="small-icon"><Clock /></el-icon>
                    <span>{{ new Date(task.date).toLocaleDateString() }}</span>
                  </div>
                  <div class="meta-item">
                    <el-icon class="small-icon"><UserFilled /></el-icon>
                    <span>{{ task.creator }}</span>
                  </div>
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
                <el-button link size="small" @click="prevMonth"><el-icon><ArrowLeft /></el-icon></el-button>
                <span class="month-label">{{ displayMonth }}</span>
                <el-button link size="small" @click="nextMonth"><el-icon><ArrowRight /></el-icon></el-button>
              </div>
              <div class="calendar-days">
                <div v-for="d in ['日','一','二','三','四','五','六']" :key="d" class="day-head">{{ d }}</div>
                <div v-for="(day, index) in calendarDays" :key="index" class="day-cell" 
                     :class="{ 
                       active: day.isToday, 
                       'no-date': !day.date,
                       'selected': day.isSelected
                     }" @click="handleDateClick(day)">
                  <span v-if="day.date">{{ day.date }}</span>
                  <div v-if="day.hasEvent" class="has-event"></div>
                </div>
              </div>
            </div>
            <div class="selected-date-events">
              <div class="section-label">
                {{ selectedDate.toLocaleDateString() }} 的日程
              </div>
              <el-empty v-if="selectedDateEvents.length === 0" :description="$t('common.noData')" />
              <div v-else v-for="(event, index) in selectedDateEvents" :key="index" class="event-card-modern">
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
    </el-skeleton>
  </div>
</template>

<script setup>
import { ref, onMounted, markRaw, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { useI18n } from 'vue-i18n'
import {
  Clock, Bell, CircleCheck, Checked, List,
  Plus, ArrowLeft, ArrowRight, Files, Download,
  Calendar, Document, Location, User, UserFilled
} from '@element-plus/icons-vue'
import { getTaskList } from '@/api/task'
import { getScheduleList } from '@/api/schedule'
import { get } from '@/utils/axios'

const userStore = useUserStore()
const { t } = useI18n()

// 当前日期格式化
const currentDate = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
})

// 当前月份信息
const currentMonth = ref(new Date())
const displayMonth = computed(() => {
  return `${currentMonth.value.getFullYear()}年${currentMonth.value.getMonth() + 1}月`
})

// 用户信息
const userInfo = ref({})

// 加载状态
const loading = ref(true)
const taskLoading = ref(false)
const scheduleLoading = ref(false)

// 任务统计数据（真实数据）
const taskStats = ref([
  { key: 'pending', label: t('dashboard.stats.pendingTask'), count: 0, icon: markRaw(Clock), color: '#409eff' },
  { key: 'pendingReview', label: t('dashboard.stats.pendingReview'), count: 0, icon: markRaw(Bell), color: '#67c23a' },
  { key: 'completed', label: t('dashboard.stats.completedTask'), count: 0, icon: markRaw(CircleCheck), color: '#e6a23c' },
  { key: 'reviewed', label: t('dashboard.stats.reviewedRecord'), count: 0, icon: markRaw(Checked), color: '#909399' }
])

// 活动标签页
const activeTaskTab = ref('pending')

// 任务列表数据（真实数据）
const tasks = ref([])

// 根据标签页过滤任务
const filteredTasks = computed(() => {
  const statusMap = {
    pending: '待办',
    pendingReview: '待阅',
    completed: '已办'
  }
  return tasks.value.filter(task => task.status === statusMap[activeTaskTab.value])
})

// 完整日程数据（真实数据）
const scheduleList = ref([])

// 选中日期的日程（真实数据）
const selectedDateEvents = ref([])

// 最近文件（真实数据）
const files = ref([])

// 日历相关数据
const calendarDays = ref([])
const today = new Date()
const selectedDate = ref(new Date())

// 生成日历数据
const generateCalendar = () => {
  const year = currentMonth.value.getFullYear()
  const month = currentMonth.value.getMonth()
  
  // 获取当月第一天和最后一天
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const lastDate = lastDay.getDate()
  
  // 获取当月第一天是星期几（0-6，0是周日）
  const startDay = firstDay.getDay()
  
  // 生成日历数据
  const days = []
  
  // 添加空白日期，使第一天正确对齐
  for (let i = 0; i < startDay; i++) {
    days.push({ date: null, hasEvent: false, isToday: false, isSelected: false })
  }
  
  // 添加当月日期
  for (let date = 1; date <= lastDate; date++) {
    const currentDate = new Date(year, month, date)
    const isToday = currentDate.toDateString() === today.toDateString()
    const isSelected = currentDate.toDateString() === selectedDate.value.toDateString()
    
    // 检查当天是否有事件
    const hasEvent = scheduleList.value.some(event => {
      if (event.date) {
        const eventDate = new Date(event.date)
        return eventDate.toDateString() === currentDate.toDateString()
      }
      return false
    })
    
    days.push({ date, hasEvent, isToday, isSelected })
  }
  
  calendarDays.value = days
}

// 处理日历日期点击
const handleDateClick = (day) => {
  if (!day.date) return
  
  // 更新选中日期
  selectedDate.value = new Date(
    currentMonth.value.getFullYear(),
    currentMonth.value.getMonth(),
    day.date
  )
  
  // 重新生成日历，更新选中状态
  generateCalendar()
  
  // 更新选中日期的日程
  selectedDateEvents.value = filterEventsByDate(selectedDate.value)
  
}

// 切换月份
const prevMonth = () => {
  currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() - 1)
  generateCalendar()
}

const nextMonth = () => {
  currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + 1)
  generateCalendar()
}

// 获取任务数据
const fetchTaskData = async () => {
  try {
    taskLoading.value = true
    const taskData = await getTaskList()
    
    // 状态映射：后端数字状态 -> 前端中文状态
    const statusMap = {
      0: '待办',
      1: '待阅',
      2: '进行中',
      3: '已办'
    }
    
    // 优先级映射
    const priorityMap = {
      0: '低',
      1: '中',
      2: '高',
      3: '紧急'
    }
    
    // 转换任务状态并更新任务列表
    const transformedTasks = taskData.map(task => ({
      ...task,
      status: statusMap[task.status] || '待办', // 默认待办
      priority: priorityMap[task.priority] || '中', // 默认中等优先级
      // 转换日期格式，只显示年月日
      date: task.date || task.deadline || task.endTime || new Date().toISOString(),
      // 添加更多默认值
      title: task.title || '无标题',
      description: task.description || '无描述',
      creator: task.creator || task.createUser || '未知创建者',
      assignee: task.assignee || task.assignUser || '未分配'
    }))
    
    // 更新任务列表
    tasks.value = transformedTasks
    
    // 更新任务统计
    taskStats.value[0].count = transformedTasks.filter(task => task.status === '待办').length
    taskStats.value[1].count = transformedTasks.filter(task => task.status === '待阅').length
    taskStats.value[2].count = transformedTasks.filter(task => task.status === '已办').length
    taskStats.value[3].count = transformedTasks.filter(task => task.status === '已办' || task.status === '进行中').length
  } catch (error) {
    console.error('获取任务数据失败:', error)
  } finally {
    taskLoading.value = false
  }
}

// 根据日期过滤日程
const filterEventsByDate = (date) => {
  const dateStr = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  return scheduleList.value.filter(event => {
    return event.startTime && event.startTime.startsWith(dateStr)
  }).map(event => ({
    ...event,
    // 转换时间格式，只显示时分
    time: event.startTime ? event.startTime.split(' ')[1].slice(0, 5) : '',
    // 添加默认值
    location: event.location || '无地点',
    title: event.title || '无标题'
  }))
}

// 获取日程数据
const fetchScheduleData = async () => {
  try {
    scheduleLoading.value = true
    const scheduleData = await getScheduleList()
    
    // 保存完整日程数据
    scheduleList.value = scheduleData
    
    // 初始化选中日期的日程
    selectedDateEvents.value = filterEventsByDate(selectedDate.value)
  } catch (error) {
    console.error('获取日程数据失败:', error)
    // 如果API调用失败，使用空数组
    scheduleList.value = []
    selectedDateEvents.value = []
  } finally {
    scheduleLoading.value = false
  }
}

// 获取最近文件
const fetchRecentFiles = async () => {
  try {
    // 调用文档列表API，后端没有专门的最近文件接口
    const fileData = await get('/document/list')
    // 按创建时间排序，取最近的4个文件
    const sortedFiles = fileData.sort((a, b) => {
      const dateA = a.createdAt || a.createTime || a.create_at || new Date().toISOString()
      const dateB = b.createdAt || b.createTime || b.create_at || new Date().toISOString()
      return new Date(dateB) - new Date(dateA)
    }).slice(0, 4)
    // 转换文件数据格式，适配前端显示
    files.value = sortedFiles.map(file => ({
      // 使用后端返回的title字段作为文件名
      name: file.title || '未知文件名',
      // 从filePath中提取文件大小信息，或者模拟文件大小
      size: `${(Math.random() * 5 + 1).toFixed(1)} MB`, // 模拟文件大小
      // 检查多种可能的日期字段
      date: file.createdAt || file.createTime || file.create_at || new Date().toISOString(),
      icon: markRaw(Files)
    }))
  } catch (error) {
    console.error('获取最近文件失败:', error)
    // 如果API调用失败，使用模拟数据
    files.value = [
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
    ]
  }
}

// 获取仪表盘数据
const fetchDashboardData = async () => {
  loading.value = true
  try {
    // 并行获取所有数据
    await Promise.all([
      fetchTaskData(),
      fetchScheduleData(),
      fetchRecentFiles()
    ])
    // 数据加载完成后生成日历
    generateCalendar()
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  userInfo.value = userStore.userInfo
  // 初始化日历
  generateCalendar()
  // 获取真实数据
  fetchDashboardData()
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
.task-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 4px; flex-wrap: wrap; gap: 8px; }
.task-title { font-weight: 500; color: #262626; flex: 1; min-width: 100px; }
.task-tags { display: flex; gap: 4px; flex-wrap: wrap; }
.task-desc { font-size: 13px; color: #8c8c8c; margin-bottom: 8px; line-height: 1.4; }
.task-meta { display: flex; gap: 16px; flex-wrap: wrap; font-size: 12px; color: #bfbfbf; }
.task-meta .meta-item { display: flex; align-items: center; gap: 4px; }
.small-icon { font-size: 12px; }
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
  align-items: start; /* 防止项目被拉伸 */
}

.mini-calendar {
  background: #fafafa;
  padding: 16px;
  border-radius: 8px;
  height: fit-content; /* 保持内容高度 */
  min-height: 200px; /* 设置最小高度，确保日历显示完整 */
}

.selected-date-events {
  height: fit-content; /* 保持内容高度 */
  min-height: 200px; /* 设置最小高度，确保布局平衡 */
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.selected-date-events :deep(.el-empty) {
  width: 100%;
  flex-shrink: 0;
  margin-top: 16px;
  padding: 0;
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

.day-cell:hover { background: #e6f7ff; color: #1890ff; cursor: pointer; }
.day-cell.active { background: #1890ff; color: white; }
.day-cell.selected { background: #e6f7ff; color: #1890ff; border: 1px solid #1890ff; }
.day-cell.no-date { pointer-events: none; color: transparent; }
.day-cell.no-date:hover { background: transparent; }

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
}

@media (max-width: 768px) {
  .schedule-container { grid-template-columns: 1fr; }
  .welcome-banner { flex-direction: column; text-align: center; }
  .welcome-image { display: none; }
  .quick-stats { margin-top: 20px; }
}
</style>
