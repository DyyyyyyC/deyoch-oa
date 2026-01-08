/**
 * WebSocket 工具类
 * 用于管理WebSocket连接和消息处理
 */

let socket = null
let reconnectTimer = null
let heartbeatTimer = null
let reconnectAttempts = 0
const maxReconnectAttempts = 5
const reconnectInterval = 5000
const heartbeatInterval = 30000

/**
 * 初始化WebSocket连接
 * @param {string} userId 用户ID
 * @param {Object} options 配置选项
 * @param {Function} options.onMessage 消息接收回调
 * @param {Function} options.onOpen 连接打开回调
 * @param {Function} options.onClose 连接关闭回调
 * @param {Function} options.onError 连接错误回调
 */
export function initWebSocket(userId, options = {}) {
  if (!userId) {
    console.warn('WebSocket初始化失败：用户ID不能为空')
    return
  }

  // 如果已有连接，先关闭
  if (socket) {
    closeWebSocket()
  }

  const wsUrl = `${getWebSocketUrl()}/ws/message?userId=${userId}`
  
  try {
    socket = new WebSocket(wsUrl)
    
    socket.onopen = (event) => {
      console.log('WebSocket连接已建立')
      reconnectAttempts = 0
      startHeartbeat()
      
      if (options.onOpen) {
        options.onOpen(event)
      }
    }
    
    socket.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data)
        console.log('收到WebSocket消息:', message)
        
        if (options.onMessage) {
          options.onMessage(message)
        }
      } catch (error) {
        console.error('解析WebSocket消息失败:', error)
      }
    }
    
    socket.onclose = (event) => {
      console.log('WebSocket连接已关闭', event.code, event.reason)
      stopHeartbeat()
      
      if (options.onClose) {
        options.onClose(event)
      }
      
      // 如果不是主动关闭，尝试重连
      if (event.code !== 1000 && reconnectAttempts < maxReconnectAttempts) {
        scheduleReconnect(userId, options)
      }
    }
    
    socket.onerror = (error) => {
      console.error('WebSocket连接错误:', error)
      
      if (options.onError) {
        options.onError(error)
      }
    }
    
  } catch (error) {
    console.error('创建WebSocket连接失败:', error)
  }
}

/**
 * 关闭WebSocket连接
 */
export function closeWebSocket() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  
  stopHeartbeat()
  
  if (socket) {
    socket.close(1000, '主动关闭')
    socket = null
  }
  
  reconnectAttempts = 0
}

/**
 * 发送WebSocket消息
 * @param {Object} message 要发送的消息
 */
export function sendWebSocketMessage(message) {
  if (socket && socket.readyState === WebSocket.OPEN) {
    try {
      socket.send(JSON.stringify(message))
      return true
    } catch (error) {
      console.error('发送WebSocket消息失败:', error)
      return false
    }
  } else {
    console.warn('WebSocket连接未建立或已关闭')
    return false
  }
}

/**
 * 获取WebSocket连接状态
 */
export function getWebSocketState() {
  if (!socket) {
    return 'CLOSED'
  }
  
  switch (socket.readyState) {
    case WebSocket.CONNECTING:
      return 'CONNECTING'
    case WebSocket.OPEN:
      return 'OPEN'
    case WebSocket.CLOSING:
      return 'CLOSING'
    case WebSocket.CLOSED:
      return 'CLOSED'
    default:
      return 'UNKNOWN'
  }
}

/**
 * 检查WebSocket是否已连接
 */
export function isWebSocketConnected() {
  return socket && socket.readyState === WebSocket.OPEN
}

/**
 * 安排重连
 */
function scheduleReconnect(userId, options) {
  if (reconnectAttempts >= maxReconnectAttempts) {
    console.error('WebSocket重连次数已达上限，停止重连')
    return
  }
  
  reconnectAttempts++
  console.log(`WebSocket将在${reconnectInterval}ms后进行第${reconnectAttempts}次重连`)
  
  reconnectTimer = setTimeout(() => {
    console.log(`开始第${reconnectAttempts}次WebSocket重连`)
    initWebSocket(userId, options)
  }, reconnectInterval)
}

/**
 * 开始心跳
 */
function startHeartbeat() {
  stopHeartbeat()
  
  heartbeatTimer = setInterval(() => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send('ping')
    }
  }, heartbeatInterval)
}

/**
 * 停止心跳
 */
function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

/**
 * 获取WebSocket URL
 */
function getWebSocketUrl() {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  
  // 开发环境
  if (process.env.NODE_ENV === 'development') {
    return `${protocol}//localhost:8080/api`
  }
  
  // 生产环境
  return `${protocol}//${host}/api`
}

/**
 * WebSocket事件类型常量
 */
export const WebSocketEvents = {
  MESSAGE_RECEIVED: 'message_received',
  CONNECTION_OPENED: 'connection_opened',
  CONNECTION_CLOSED: 'connection_closed',
  CONNECTION_ERROR: 'connection_error'
}

/**
 * 消息类型常量
 */
export const MessageTypes = {
  SYSTEM: 1,      // 系统消息
  APPROVAL: 2,    // 审批通知
  TASK: 3,        // 任务通知
  ANNOUNCEMENT: 4 // 公告通知
}

/**
 * 消息优先级常量
 */
export const MessagePriority = {
  NORMAL: 1,    // 普通
  IMPORTANT: 2, // 重要
  URGENT: 3     // 紧急
}