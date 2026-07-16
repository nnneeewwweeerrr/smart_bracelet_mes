import request from './request'

// 获取AI问答历史列表
export function getAiChatHistoryList(params) {
  return request({
    url: '/ai-chat/list',
    method: 'get',
    params
  })
}

// 获取问答详情
export function getAiChatHistoryDetail(id) {
  return request({
    url: `/ai-chat/${id}`,
    method: 'get'
  })
}

// 获取问答统计
export function getAiChatStats() {
  return request({
    url: '/ai-chat/statistics',
    method: 'get'
  })
}

// 删除问答记录
export function deleteAiChatHistory(id) {
  return request({
    url: `/ai-chat/delete/${id}`,
    method: 'delete'
  })
}