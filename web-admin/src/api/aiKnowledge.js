import request from './request'

// 知识库文档列表（分页+搜索）
export function getAiKnowledgeList(params) {
  return request({ url: '/ai-knowledge/list', method: 'get', params })
}

// 全部已启用文档（下拉用）
export function getAiKnowledgeAll() {
  return request({ url: '/ai-knowledge/all', method: 'get' })
}

// 文档详情
export function getAiKnowledgeDetail(id) {
  return request({ url: `/ai-knowledge/${id}`, method: 'get' })
}

// 分类列表
export function getAiKnowledgeCategories() {
  return request({ url: '/ai-knowledge/categories', method: 'get' })
}

// 统计信息
export function getAiKnowledgeStats() {
  return request({ url: '/ai-knowledge/statistics', method: 'get' })
}

// 新增文档
export function addAiKnowledge(data) {
  return request({ url: '/ai-knowledge/save', method: 'post', data })
}

// 更新文档
export function updateAiKnowledge(data) {
  return request({ url: '/ai-knowledge/update', method: 'put', data })
}

// 启用/禁用切换
export function toggleAiKnowledge(id) {
  return request({ url: `/ai-knowledge/toggle/${id}`, method: 'put' })
}

// 删除文档
export function deleteAiKnowledge(id) {
  return request({ url: `/ai-knowledge/delete/${id}`, method: 'delete' })
}

// 同步知识库
export function syncAiKnowledge() {
  return request({ url: '/ai-knowledge/sync', method: 'post' })
}
