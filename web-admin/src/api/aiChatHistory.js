import request from './request'

// 问答历史列表（分页+搜索）
export function getAiChatHistoryList(params) {
  return request({ url: '/ai-chat/list', method: 'get', params })
}

// 会话列表
export function getAiChatSessions() {
  return request({ url: '/ai-chat/sessions', method: 'get' })
}

// 问答统计
export function getAiChatStats() {
  return request({ url: '/ai-chat/statistics', method: 'get' })
}

// 提交反馈（1好评/-1差评）
export function feedbackAiChat(id, feedback) {
  return request({ url: `/ai-chat/feedback/${id}`, method: 'put', params: { feedback } })
}

// 删除问答记录
export function deleteAiChatHistory(id) {
  return request({ url: `/ai-chat/delete/${id}`, method: 'delete' })
}
