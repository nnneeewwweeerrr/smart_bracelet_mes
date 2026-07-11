import request from './request'

export function getList(params) {
  return request({
    url: '/operation-log/list',
    method: 'get',
    params
  })
}

export function getModules() {
  return request({
    url: '/operation-log/modules',
    method: 'get'
  })
}

export function getById(id) {
  return request({
    url: `/operation-log/${id}`,
    method: 'get'
  })
}