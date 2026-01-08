import request from '@/utils/axios'

/**
 * 消息通知API
 */

/**
 * 发送消息
 * @param {Object} data 消息数据
 * @param {string} data.title 消息标题
 * @param {string} data.content 消息内容
 * @param {number} data.receiverId 接收者ID
 * @param {number} data.type 消息类型
 * @param {number} data.priority 优先级
 */
export function sendMessage(data) {
  return request({
    url: '/message/send',
    method: 'post',
    data
  })
}

/**
 * 批量发送消息
 * @param {Array} receiverIds 接收者ID列表
 * @param {Object} message 消息数据
 */
export function sendBatchMessage(receiverIds, message) {
  return request({
    url: '/message/send/batch',
    method: 'post',
    params: { receiverIds },
    data: message
  })
}

/**
 * 分页查询用户消息
 * @param {Object} params 查询参数
 * @param {number} params.userId 用户ID
 * @param {number} params.page 页码
 * @param {number} params.size 每页大小
 * @param {number} params.type 消息类型
 * @param {number} params.isRead 是否已读
 */
export function getUserMessages(params) {
  return request({
    url: '/message/list',
    method: 'get',
    params
  })
}

/**
 * 标记消息为已读
 * @param {number} messageId 消息ID
 * @param {number} userId 用户ID
 */
export function markMessageAsRead(messageId, userId) {
  return request({
    url: `/message/${messageId}/read`,
    method: 'put',
    params: { userId }
  })
}

/**
 * 批量标记消息为已读
 * @param {Array} messageIds 消息ID列表
 * @param {number} userId 用户ID
 */
export function markMessagesAsRead(messageIds, userId) {
  return request({
    url: '/message/read/batch',
    method: 'put',
    params: { messageIds, userId }
  })
}

/**
 * 删除消息
 * @param {number} messageId 消息ID
 * @param {number} userId 用户ID
 */
export function deleteMessage(messageId, userId) {
  return request({
    url: `/message/${messageId}`,
    method: 'delete',
    params: { userId }
  })
}

/**
 * 获取用户未读消息数量
 * @param {number} userId 用户ID
 */
export function getUnreadMessageCount(userId) {
  return request({
    url: '/message/unread/count',
    method: 'get',
    params: { userId }
  })
}

/**
 * 获取各类型未读消息数量
 * @param {number} userId 用户ID
 */
export function getUnreadMessageCountByType(userId) {
  return request({
    url: '/message/unread/count/by-type',
    method: 'get',
    params: { userId }
  })
}

/**
 * 清空所有消息
 * @param {number} userId 用户ID
 */
export function clearAllMessages(userId) {
  return request({
    url: '/message/clear',
    method: 'delete',
    params: { userId }
  })
}

/**
 * 获取WebSocket连接状态
 */
export function getWebSocketStatus() {
  return request({
    url: '/message/websocket/status',
    method: 'get'
  })
}

/**
 * 检查用户是否在线
 * @param {string} userId 用户ID
 */
export function isUserOnline(userId) {
  return request({
    url: '/message/websocket/online',
    method: 'get',
    params: { userId }
  })
}