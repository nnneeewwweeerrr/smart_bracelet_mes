import request from './request'

export function getDocumentList(params) {
  return request({
    url: '/document/list',
    method: 'get',
    params
  })
}

export function uploadDocument(params) {
  return request({
    url: '/document/create',
    method: 'post',
    data: params
  })
}

export function deleteDocumentById(id) {
  return request({
    url: `/document/${id}`,
    method: 'delete'
  })
}

export function syncKnowledgeBaseApi() {
  return request({
    url: '/document/sync',
    method: 'post'
  })
}

export function searchKnowledge(query, topK = 5) {
  return request({
    url: '/document/search',
    method: 'get',
    params: { query, topK }
  })
}

export function getGraphNodes() {
  return request({
    url: '/kg/nodes',
    method: 'get'
  })
}

export function getGraphRelationships() {
  return request({
    url: '/kg/relationships',
    method: 'get'
  })
}

export function getGraphData() {
  return request({
    url: '/kg/graph-data',
    method: 'get'
  })
}

export function searchGraphNodes(keyword) {
  return request({
    url: '/kg/search',
    method: 'get',
    params: { keyword }
  })
}

export function getNodeDetails(nodeType, nodeCode) {
  return request({
    url: `/kg/node/${nodeType}/${nodeCode}`,
    method: 'get'
  })
}

export function analyzeDefect(defectName) {
  return request({
    url: `/kg/defect/${defectName}`,
    method: 'get'
  })
}

export function getGraphStats() {
  return request({
    url: '/kg/stats',
    method: 'get'
  })
}