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
            <div class="quick-stat-item" v-for="stat in taskStats" :key="stat.key">
              <div class="stat-icon" :style="{ backgroundColor: stat.color }">
                <el-icon><component :is="stat.icon" /></el-icon>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ stat.count }}</span>
                <span class="stat-label">{{ stat.label }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="welcome-actions">
          <el-button type="primary" size="large" round @click="handleQuickAction('task')">
            <el-icon><Plus /></el-icon>新建任务
          </el-button>
          <el-button type="success" size="large" round @click="handleQuickAction('schedule')">
            <el-icon><Calendar /></el-icon>添加日程
          </el-button>
        </div>
      </div>

      <!-- 主要内容布局 -->
      <div class="dashboard-grid">
        <!-- 左侧列 -->
        <div class="grid-left">
          <!-- 任务中心 - 优化版 -->
          <el-card class="modern-card task-section">
            <template #header>
              <div class="card-header task-header">
                <div class="header-title">
                  <el-icon><List /></el-icon>
                  <span>{{ $t('dashboard.taskCenter') }}</span>
                  <el-badge :value="totalTaskCount" :max="99" class="task-badge" />
                </div>
                <div class="header-actions">
                  <el-button-group>
                    <el-button 
                      v-for="tab in taskTabs" 
                      :key="tab.key"
                      :type="activeTaskTab === tab.key ? 'primary' : ''"
                      size="small"
                      @click="activeTaskTab = tab.key"
                    >
                      {{ tab.label }} ({{ tab.count }})
                    </el-button>
                  </el-button-group>
                  <el-button link type="primary" @click="goToTaskManagement">
                    查看全部 <el-icon><ArrowRight /></el-icon>
                  </el-button>
                </div>
              </div>
            </template>
            
            <div class="task-list-modern">
              <el-empty v-if="filteredTasks.length === 0" :description="$t('common.noData')" />
              <div v-else>
                <!-- 任务列表 -->
                <div v-for="(task, index) in displayTasks" :key="task.id || index" class="task-item-modern">
                  <div class="task-priority" :class="getPriorityClass(task.priority)"></div>
                  <div class="task-info">
                    <div class="task-top">
                      <span class="task-title" @click="handleTaskClick(task)">{{ task.title }}</span>
                      <div class="task-tags">
                        <el-tag size="small" :type="getStatusType(task.status)" effect="light">
                          {{ task.status }}
                        </el-tag>
                        <el-tag size="small" :type="getPriorityType(task.priority)" effect="plain">
                          {{ task.priority }}
                        </el-tag>
                      </div>
                    </div>
                    <div class="task-desc">{{ task.description }}</div>
                    <div class="task-meta">
                      <div class="meta-item">
                        <el-icon><User /></el-icon>
                        <span>{{ task.assignee }}</span>
                      </div>
                      <div class="meta-item">
                        <el-icon><Clock /></el-icon>
                        <span>{{ formatDate(task.date) }}</span>
                      </div>
                      <div class="meta-item" v-if="task.creator">
                        <el-icon><UserFilled /></el-icon>
                        <span>{{ task.creator }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="task-actions">
                    <el-button size="small" type="primary" link @click="handleTaskAction(task, 'complete')">
                      完成
                    </el-button>
                  </div>
                </div>
                
                <!-- 显示更多按钮 -->
                <div v-if="filteredTasks.length > displayLimit" class="show-more">
                  <el-button link @click="toggleShowMore">
                    {{ showMore ? '收起' : `还有 ${filteredTasks.length - displayLimit} 个任务` }}
                    <el-icon><component :is="showMore ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 数据统计卡片 -->
          <div class="stats-grid">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon-large" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
                  <el-icon><TrendCharts /></el-icon>
                </div>
                <div class="stat-details">
                  <div class="stat-number">{{ weeklyStats.completed }}</div>
                  <div class="stat-text">本周完成任务</div>
                  <div class="stat-trend">
                    <span :class="weeklyStats.trend > 0 ? 'trend-up' : 'trend-down'">
                      {{ weeklyStats.trend > 0 ? '+' : '' }}{{ weeklyStats.trend }}%
                    </span>
                  </div>
                </div>
              </div>
            </el-card>
            
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon-large" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
                  <el-icon><Files /></el-icon>
                </div>
                <div class="stat-details">
                  <div class="stat-number">{{ monthlyStats.documents }}</div>
                  <div class="stat-text">本月处理文档</div>
                  <div class="stat-trend">
                    <span class="trend-neutral">{{ monthlyStats.size }}</span>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 右侧列 -->
        <div class="grid-right">
          <!-- 日程安排 - 优化版 -->
          <el-card class="modern-card schedule-section">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ $t('dashboard.schedule') }}</span>
                </div>
                <el-button type="primary" size="small" round @click="handleAddSchedule">
                  <el-icon><Plus /></el-icon>{{ $t('dashboard.addSchedule') }}
                </el-button>
              </div>
            </template>
            <div class="schedule-container">
              <div class="mini-calendar">
                <div class="calendar-nav">
                  <el-button link size="small" @click="prevMonth">
                    <el-icon><ArrowLeft /></el-icon>
                  </el-button>
                  <span class="month-label">{{ displayMonth }}</span>
                  <el-button link size="small" @click="nextMonth">
                    <el-icon><ArrowRight /></el-icon>
                  </el-button>
                </div>
                <div class="calendar-days">
                  <div v-for="d in ['日','一','二','三','四','五','六']" :key="d" class="day-head">{{ d }}</div>
                  <div v-for="(day, index) in calendarDays" :key="index" class="day-cell" 
                       :class="{ 
                         active: day.isToday, 
                         'no-date': !day.date,
                         'selected': day.isSelected,
                         'has-events': day.hasEvent
                       }" @click="handleDateClick(day)">
                    <span v-if="day.date">{{ day.date }}</span>
                    <div v-if="day.hasEvent" class="event-indicator">{{ day.eventCount }}</div>
                  </div>
                </div>
              </div>
              <div class="selected-date-events">
                <div class="section-label">
                  {{ formatSelectedDate(selectedDate) }} 的日程
                </div>
                <el-empty v-if="selectedDateEvents.length === 0" :description="$t('common.noData')" />
                <div v-else class="events-list">
                  <div v-for="(event, index) in selectedDateEvents" :key="event.id || index" class="event-card-modern">
                    <div class="event-time-box" :class="getEventTimeClass(event)">
                      <span class="time">{{ formatEventTime(event.startTime) }}</span>
                    </div>
                    <div class="event-content">
                      <div class="event-title">{{ event.title }}</div>
                      <div class="event-loc" v-if="event.location">
                        <el-icon><Location /></el-icon> {{ event.location }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 最近文件 - 优化版 -->
          <el-card class="modern-card file-section">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon><Files /></el-icon>
                  <span>{{ $t('dashboard.recentFiles') }}</span>
                </div>
                <el-button link type="primary" @click="goToDocumentManagement">
                  查看全部 <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </template>
            <div class="file-list-modern">
              <el-empty v-if="files.length === 0" :description="$t('common.noData')" />
              <div v-else v-for="(file, index) in files" :key="file.id || index" class="file-item-modern">
                <div class="file-icon-box" :class="getFileTypeClass(file.fileType)">
                  <el-icon><component :is="getFileIcon(file.fileType)" /></el-icon>
                </div>
                <div class="file-details">
                  <div class="file-name" @click="handleFileClick(file)">{{ file.name }}</div>
                  <div class="file-meta">
                    <span>{{ formatFileSize(file.fileSize) }}</span>
                    <span class="separator">·</span>
                    <span>{{ formatDate(file.date) }}</span>
                    <span class="separator">·</span>
                    <span>{{ file.uploaderName || '未知' }}</span>
                  </div>
                </div>
                <div class="file-actions">
                  <el-button link @click="handleDownload(file)">
                    <el-icon><Download /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 快捷操作 -->
          <el-card class="modern-card quick-actions">
            <template #header>
              <div class="header-title">
                <el-icon><Operation /></el-icon>
                <span>快捷操作</span>
              </div>
            </template>
            <div class="quick-action-grid">
              <div class="quick-action-item" @click="handleQuickAction('announcement')">
                <el-icon><Bell /></el-icon>
                <span>发布公告</span>
              </div>
              <div class="quick-action-item" @click="handleQuickAction('meeting')">
                <el-icon><VideoCamera /></el-icon>
                <span>创建会议</span>
              </div>
              <div class="quick-action-item" @click="handleQuickAction('report')">
                <el-icon><Document /></el-icon>
                <span>工作报告</span>
              </div>
              <div class="quick-action-item" @click="handleQuickAction('approval')">
                <el-icon><Check /></el-icon>
                <span>审批流程</span>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </el-skeleton>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import {
  Clock, Bell, CircleCheck, Checked, List, Plus, ArrowLeft, ArrowRight, 
  Files, Download, Calendar, Document, Location, User, UserFilled,
  TrendCharts, Operation, VideoCamera, Check, ArrowUp, ArrowDown
} from '@element-plus/icons-vue'
import { getDashboardTasks } from '@/api/task'
import { getScheduleList, getScheduleByDate } from '@/api/schedule'
import { getRecentDocuments } from '@/api/document'

const userStore = useUserStore()
const router = useRouter()
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

// 任务相关数据
const tasks = ref([])
const activeTaskTab = ref('pending')
const displayLimit = ref(5)
const showMore = ref(false)

// 任务统计数据
const taskStats = ref([
  { key: 'pending', label: '待办任务', count: 0, icon: Clock, color: '#409eff' },
  { key: 'pendingReview', label: '待阅任务', count: 0, icon: Bell, color: '#67c23a' },
  { key: 'completed', label: '已完成', count: 0, icon: CircleCheck, color: '#e6a23c' },
  { key: 'overdue', label: '逾期任务', count: 0, icon: Checked, color: '#f56c6c' }
])

// 任务标签页
const taskTabs = computed(() => [
  { key: 'pending', label: '待办', count: taskStats.value.find(s => s.key === 'pending')?.count || 0 },
  { key: 'pendingReview', label: '待阅', count: taskStats.value.find(s => s.key === 'pendingReview')?.count || 0 },
  { key: 'completed', label: '已完成', count: taskStats.value.find(s => s.key === 'completed')?.count || 0 }
])

// 总任务数
const totalTaskCount = computed(() => {
  return taskStats.value.reduce((sum, stat) => sum + stat.count, 0)
})

// 根据标签页过滤任务
const filteredTasks = computed(() => {
  const statusMap = {
    pending: '待办',
    pendingReview: '待阅',
    completed: '已办'
  }
  return tasks.value.filter(task => task.status === statusMap[activeTaskTab.value])
})

// 显示的任务列表
const displayTasks = computed(() => {
  const limit = showMore.value ? filteredTasks.value.length : displayLimit.value
  return filteredTasks.value.slice(0, limit)
})

// 统计数据
const weeklyStats = ref({
  completed: 12,
  trend: 15.6
})

const monthlyStats = ref({
  documents: 48,
  size: '2.3GB'
})

// 日程相关数据
const scheduleList = ref([])
const selectedDateEvents = ref([])
const calendarDays = ref([])
const selectedDate = ref(new Date())

// 文件数据
const files = ref([])

// 方法定义
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const formatSelectedDate = (date) => {
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const formatEventTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.split(' ')[1]?.slice(0, 5) || ''
}

const formatFileSize = (size) => {
  if (!size) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(size) / Math.log(k))
  return parseFloat((size / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const getPriorityClass = (priority) => {
  const map = { '紧急': 'priority-urgent', '高': 'priority-high', '中': 'priority-medium', '低': 'priority-low' }
  return map[priority] || 'priority-medium'
}

const getStatusType = (status) => {
  const map = { '待办': 'warning', '待阅': 'info', '已办': 'success', '进行中': 'primary' }
  return map[status] || 'info'
}

const getPriorityType = (priority) => {
  const map = { '紧急': 'danger', '高': 'warning', '中': 'info', '低': 'info' }
  return map[priority] || 'info'
}

const getFileTypeClass = (fileType) => {
  const map = {
    'pdf': 'file-pdf', 'doc': 'file-doc', 'docx': 'file-doc',
    'xls': 'file-excel', 'xlsx': 'file-excel',
    'ppt': 'file-ppt', 'pptx': 'file-ppt',
    'jpg': 'file-image', 'png': 'file-image', 'gif': 'file-image'
  }
  return map[fileType?.toLowerCase()] || 'file-default'
}

const getFileIcon = (fileType) => {
  const map = {
    'pdf': Document, 'doc': Document, 'docx': Document,
    'xls': Files, 'xlsx': Files,
    'ppt': Files, 'pptx': Files
  }
  return map[fileType?.toLowerCase()] || Document
}

const getEventTimeClass = (event) => {
  const now = new Date()
  const eventTime = new Date(event.startTime)
  if (eventTime < now) return 'time-past'
  if (eventTime.getTime() - now.getTime() < 30 * 60 * 1000) return 'time-soon'
  return 'time-future'
}

// 事件处理
const toggleShowMore = () => {
  showMore.value = !showMore.value
}

const handleTaskClick = (task) => {
  // 跳转到任务详情
  router.push(`/task/${task.id}`)
}

const handleTaskAction = (task, action) => {
  if (action === 'complete') {
    // 完成任务逻辑
    ElMessage.success('任务已完成')
    fetchDashboardData()
  }
}

const handleQuickAction = (type) => {
  const routes = {
    task: '/task/create',
    schedule: '/schedule/create',
    announcement: '/announcement/create',
    meeting: '/meeting/create',
    report: '/report/create',
    approval: '/approval/create'
  }
  if (routes[type]) {
    router.push(routes[type])
  }
}

const handleAddSchedule = () => {
  router.push('/schedule/create')
}

const handleFileClick = (file) => {
  router.push(`/document/${file.id}`)
}

const handleDownload = (file) => {
  // 下载文件逻辑
  ElMessage.success('开始下载文件')
}

const goToTaskManagement = () => {
  router.push('/task')
}

const goToDocumentManagement = () => {
  router.push('/document')
}

// 日历相关方法
const generateCalendar = () => {
  const year = currentMonth.value.getFullYear()
  const month = currentMonth.value.getMonth()
  
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const lastDate = lastDay.getDate()
  const startDay = firstDay.getDay()
  
  const days = []
  
  // 添加空白日期
  for (let i = 0; i < startDay; i++) {
    days.push({ date: null, hasEvent: false, isToday: false, isSelected: false })
  }
  
  // 添加当月日期
  for (let date = 1; date <= lastDate; date++) {
    const currentDate = new Date(year, month, date)
    const isToday = currentDate.toDateString() === new Date().toDateString()
    const isSelected = currentDate.toDateString() === selectedDate.value.toDateString()
    
    // 检查当天是否有事件
    const dayEvents = scheduleList.value.filter(event => {
      if (event.startTime) {
        const eventDate = new Date(event.startTime)
        return eventDate.toDateString() === currentDate.toDateString()
      }
      return false
    })
    
    days.push({ 
      date, 
      hasEvent: dayEvents.length > 0, 
      eventCount: dayEvents.length,
      isToday, 
      isSelected 
    })
  }
  
  calendarDays.value = days
}

const handleDateClick = async (day) => {
  if (!day.date) return
  
  selectedDate.value = new Date(
    currentMonth.value.getFullYear(),
    currentMonth.value.getMonth(),
    day.date
  )
  
  generateCalendar()
  
  // 根据选中日期获取日程
  const dateStr = selectedDate.value.toISOString().split('T')[0]
  try {
    const events = await getScheduleByDate(dateStr)
    selectedDateEvents.value = Array.isArray(events) ? events : []
  } catch (error) {
    console.error('获取日程失败:', error)
    selectedDateEvents.value = []
  }
}

const prevMonth = () => {
  currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() - 1)
  generateCalendar()
}

const nextMonth = () => {
  currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + 1)
  generateCalendar()
}

// 数据获取方法
const fetchDashboardData = async () => {
  loading.value = true
  try {
    // 并行获取所有数据
    const [taskResponse, scheduleResponse, fileResponse] = await Promise.all([
      getDashboardTasks().catch(() => ({ data: { records: [] } })),
      getScheduleList().catch(() => ({ data: [] })),
      getRecentDocuments().catch(() => ({ data: { records: [] } }))
    ])
    
    // 处理任务数据
    let taskData = []
    if (taskResponse && taskResponse.data) {
      if (taskResponse.data.records && Array.isArray(taskResponse.data.records)) {
        taskData = taskResponse.data.records
      } else if (Array.isArray(taskResponse.data)) {
        taskData = taskResponse.data
      }
    }
    
    tasks.value = taskData.map(task => ({
      ...task,
      status: ['待办', '待阅', '进行中', '已办'][task.status] || '待办',
      priority: ['低', '中', '高', '紧急'][task.priority] || '中',
      title: task.title || '无标题',
      description: task.description || '无描述',
      creator: task.creatorName || task.createUser || '未知',
      assignee: task.assigneeName || task.assignUser || '未分配',
      date: task.deadline || task.endTime || new Date().toISOString()
    }))
    
    // 更新任务统计
    taskStats.value[0].count = taskData.filter(t => t.status === 0).length
    taskStats.value[1].count = taskData.filter(t => t.status === 1).length
    taskStats.value[2].count = taskData.filter(t => t.status === 3).length
    taskStats.value[3].count = taskData.filter(t => {
      const deadline = new Date(t.deadline || t.endTime)
      return deadline < new Date() && t.status !== 3
    }).length
    
    // 处理日程数据
    let scheduleData = []
    if (scheduleResponse && scheduleResponse.data) {
      scheduleData = Array.isArray(scheduleResponse.data) ? scheduleResponse.data : []
    }
    scheduleList.value = scheduleData
    
    // 处理文件数据
    let fileData = []
    if (fileResponse && fileResponse.data) {
      if (fileResponse.data.records && Array.isArray(fileResponse.data.records)) {
        fileData = fileResponse.data.records
      } else if (Array.isArray(fileResponse.data)) {
        fileData = fileResponse.data
      }
    }
    
    files.value = fileData.slice(0, 5).map(file => ({
      ...file,
      name: file.fileName || file.name || '未知文件',
      date: file.createdAt || file.createTime || new Date().toISOString(),
      fileSize: file.fileSize || 0
    }))
    
    // 生成日历
    generateCalendar()
    
    // 获取今天的日程
    const today = new Date().toISOString().split('T')[0]
    try {
      const todayEvents = await getScheduleByDate(today)
      selectedDateEvents.value = Array.isArray(todayEvents) ? todayEvents : []
    } catch (error) {
      selectedDateEvents.value = []
    }
    
  } catch (error) {
    console.error('获取工作台数据失败:', error)
    ElMessage.error('获取工作台数据失败')
  } finally {
    loading.value = false
  }
}

// 监听月份变化，重新获取日程数据
watch(currentMonth, async () => {
  try {
    const scheduleData = await getScheduleList()
    scheduleList.value = Array.isArray(scheduleData) ? scheduleData : (scheduleData?.records || [])
    generateCalendar()
  } catch (error) {
    console.error('获取日程数据失败:', error)
  }
})

onMounted(() => {
  userInfo.value = userStore.userInfo
  fetchDashboardData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
  min-height: 100vh;
  background: #f5f7fa;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
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
  gap: 24px;
}

.quick-stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: rgba(255, 255, 255, 0.2);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  opacity: 0.8;
}

.welcome-actions {
  display: flex;
  gap: 16px;
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

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.stat-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon-large {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-details {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #262626;
  line-height: 1;
}

.stat-text {
  font-size: 14px;
  color: #8c8c8c;
  margin: 4px 0;
}

.stat-trend {
  font-size: 12px;
}

.trend-up { color: #52c41a; }
.trend-down { color: #ff4d4f; }
.trend-neutral { color: #8c8c8c; }

/* 通用卡片样式 */
.modern-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.task-badge {
  margin-left: 8px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 任务列表 */
.task-list-modern {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-item-modern {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.2s;
  cursor: pointer;
}

.task-item-modern:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
}

.task-priority {
  width: 4px;
  border-radius: 2px;
}

.priority-urgent { background: #ff4d4f; }
.priority-high { background: #faad14; }
.priority-medium { background: #1890ff; }
.priority-low { background: #52c41a; }

.task-info { 
  flex: 1; 
  min-width: 0;
}

.task-top { 
  display: flex; 
  justify-content: space-between; 
  align-items: flex-start; 
  margin-bottom: 8px; 
  gap: 12px;
}

.task-title { 
  font-weight: 500; 
  color: #262626; 
  cursor: pointer;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-title:hover {
  color: #1890ff;
}

.task-tags { 
  display: flex; 
  gap: 4px; 
  flex-shrink: 0;
}

.task-desc { 
  font-size: 13px; 
  color: #8c8c8c; 
  margin-bottom: 8px; 
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-meta { 
  display: flex; 
  gap: 16px; 
  font-size: 12px; 
  color: #bfbfbf; 
}

.task-meta .meta-item { 
  display: flex; 
  align-items: center; 
  gap: 4px; 
}

.task-actions {
  display: flex;
  align-items: center;
}

.show-more {
  text-align: center;
  padding: 12px;
  border-top: 1px solid #f0f0f0;
  margin-top: 8px;
}

/* 日程安排 */
.schedule-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
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

.month-label { 
  font-weight: 600; 
  font-size: 14px; 
}

.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  text-align: center;
}

.day-head { 
  font-size: 12px; 
  color: #bfbfbf; 
  padding-bottom: 8px; 
}

.day-cell {
  font-size: 12px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 4px;
  position: relative;
}

.day-cell:hover { 
  background: #e6f7ff; 
  color: #1890ff; 
}

.day-cell.active { 
  background: #1890ff; 
  color: white; 
}

.day-cell.selected { 
  background: #e6f7ff; 
  color: #1890ff; 
  border: 1px solid #1890ff; 
}

.day-cell.no-date { 
  pointer-events: none; 
  color: transparent; 
}

.day-cell.has-events {
  font-weight: 600;
}

.event-indicator {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 16px;
  height: 16px;
  background: #ff4d4f;
  color: white;
  border-radius: 50%;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.selected-date-events {
  min-height: 200px;
}

.section-label {
  font-weight: 600;
  margin-bottom: 12px;
  color: #262626;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.event-card-modern {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.event-time-box {
  min-width: 60px;
  font-weight: 600;
  font-size: 13px;
  padding: 4px 8px;
  border-radius: 4px;
  text-align: center;
}

.time-past { background: #f5f5f5; color: #8c8c8c; }
.time-soon { background: #fff2e8; color: #fa8c16; }
.time-future { background: #e6f7ff; color: #1890ff; }

.event-title { 
  font-size: 14px; 
  font-weight: 500; 
  margin-bottom: 4px; 
}

.event-loc { 
  font-size: 12px; 
  color: #8c8c8c; 
  display: flex; 
  align-items: center; 
  gap: 4px; 
}

/* 文件列表 */
.file-list-modern {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.file-item-modern {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.2s;
}

.file-item-modern:hover { 
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
}

.file-icon-box {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.file-pdf { background: #fff2e8; color: #fa541c; }
.file-doc { background: #e6f7ff; color: #1890ff; }
.file-excel { background: #f6ffed; color: #52c41a; }
.file-ppt { background: #fff0f6; color: #eb2f96; }
.file-image { background: #f9f0ff; color: #722ed1; }
.file-default { background: #f5f5f5; color: #8c8c8c; }

.file-details { 
  flex: 1; 
  min-width: 0;
}

.file-name { 
  font-size: 14px; 
  font-weight: 500; 
  color: #262626;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-name:hover {
  color: #1890ff;
}

.file-meta { 
  font-size: 12px; 
  color: #8c8c8c;
  margin-top: 2px;
}

.separator {
  margin: 0 4px;
}

.file-actions {
  display: flex;
  align-items: center;
}

/* 快捷操作 */
.quick-actions {
  margin-top: auto;
}

.quick-action-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-radius: 8px;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-action-item:hover {
  background: #e6f7ff;
  color: #1890ff;
}

.quick-action-item .el-icon {
  font-size: 24px;
}

.quick-action-item span {
  font-size: 12px;
  font-weight: 500;
}

/* 响应式 */
@media (max-width: 1200px) {
  .dashboard-grid { 
    grid-template-columns: 1fr; 
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 16px;
  }
  
  .welcome-banner { 
    flex-direction: column; 
    text-align: center; 
    gap: 20px;
  }
  
  .quick-stats { 
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .welcome-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .schedule-container {
    gap: 16px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>