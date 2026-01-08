import { get, post, put, del } from '@/utils/axios'

/**
 * 流程管理相关API
 */

// 获取流程列表
export const getProcessList = (params = {}) => {
  return get('/process/list', { params })
}

// 根据ID获取流程详情
export const getProcessById = (id) => {
  return get(`/process/${id}`)
}

// 创建流程
export const createProcess = (data) => {
  return post('/process', data)
}

// 更新流程
export const updateProcess = (id, data) => {
  return put(`/process/${id}`, data)
}

// 删除流程
export const deleteProcess = (id) => {
  return del(`/process/${id}`)
}

/**
 * 流程实例管理相关API
 */

// 获取流程实例列表
export const getProcessInstanceList = (params = {}) => {
  return get('/process-instance/list', { params })
}

// 根据ID获取流程实例详情
export const getProcessInstanceById = (id) => {
  return get(`/process-instance/${id}`)
}

// 创建流程实例
export const createProcessInstance = (data) => {
  return post('/process-instance', data)
}

// 更新流程实例
export const updateProcessInstance = (id, data) => {
  return put(`/process-instance/${id}`, data)
}

// 删除流程实例
export const deleteProcessInstance = (id) => {
  return del(`/process-instance/${id}`)
}

// 启动流程实例
export const startProcessInstance = (id) => {
  return post(`/process-instance/${id}/start`)
}

// 完成流程实例
export const completeProcessInstance = (id) => {
  return post(`/process-instance/${id}/complete`)
}
