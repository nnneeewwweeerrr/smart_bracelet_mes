import request from './request'

export function aiChat(params) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data: params
  })
}

export function getChatHistory(params) {
  return request({
    url: '/ai/history',
    method: 'get',
    params
  })
}

export function getRecentHistory(params) {
  return request({
    url: '/ai/history/recent',
    method: 'get',
    params
  })
}

export function getAiStats() {
  return request({
    url: '/ai/stats',
    method: 'get'
  })
}
