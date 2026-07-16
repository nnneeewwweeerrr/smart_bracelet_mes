import request from './request'

export function getList(params) {
  return request({
    url: '/repair-task/list',
    method: 'get',
    params
  })
}

export function getById(id) {
  return request({
    url: '/repair-task/' + id,
    method: 'get'
  })
}

export function getStatistics() {
  return request({
    url: '/repair-task/statistics',
    method: 'get'
  })
}

export function save(data) {
  return request({
    url: '/repair-task/save',
    method: 'post',
    data
  })
}

export function update(data) {
  return request({
    url: '/repair-task/update',
    method: 'put',
    data
  })
}

export function assign(data) {
  return request({
    url: '/repair-task/assign',
    method: 'put',
    data
  })
}

export function remove(id) {
  return request({
    url: '/repair-task/delete/' + id,
    method: 'delete'
  })
}
