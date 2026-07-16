import request from './request'

// 获取不合格品列表
export function getUnqualifiedList(params) {
  return request({
    url: '/unqualified/list',
    method: 'get',
    params
  })
}

// 获取不合格品详情
export function getUnqualifiedDetail(id) {
  return request({
    url: `/unqualified/${id}`,
    method: 'get'
  })
}

// 获取不合格品统计
export function getUnqualifiedStats() {
  return request({
    url: '/unqualified/statistics',
    method: 'get'
  })
}

// 新增不合格品
export function addUnqualified(data) {
  return request({
    url: '/unqualified/save',
    method: 'post',
    data
  })
}

// 更新不合格品
export function updateUnqualified(data) {
  return request({
    url: '/unqualified/update',
    method: 'put',
    data
  })
}

// 处置不合格品
export function handleUnqualified(id, data) {
  return request({
    url: `/unqualified/handle/${id}`,
    method: 'put',
    data
  })
}

// 删除不合格品
export function deleteUnqualified(id) {
  return request({
    url: `/unqualified/delete/${id}`,
    method: 'delete'
  })
}