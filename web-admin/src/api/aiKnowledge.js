import request from './request'

// 获取知识库文档列表
export function getAiKnowledgeList(params) {
  return request({
    url: '/ai-knowledge/list',
    method: 'get',
    params
  })
}

// 获取文档详情
export function getAiKnowledgeDetail(id) {
  return request({
    url: `/ai-knowledge/${id}`,
    method: 'get'
  })
}

// 新增文档
export function addAiKnowledge(data) {
  return request({
    url: '/ai-knowledge/save',
    method: 'post',
    data
  })
}

// 更新文档
export function updateAiKnowledge(data) {
  return request({
    url: '/ai-knowledge/update',
    method: 'put',
    data
  })
}

// 删除文档
export function deleteAiKnowledge(id) {
  return request({
    url: `/ai-knowledge/delete/${id}`,
    method: 'delete'
  })
}

// 同步知识库
export function syncAiKnowledge() {
  return request({
    url: '/ai-knowledge/sync',
    method: 'post'
  })
}