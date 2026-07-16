import request from './request'

export function getList(params) {
  return request({
    url: '/exception/list',
    method: 'get',
    params
  })
}

export function getById(id) {
  return request({
    url: '/exception/' + id,
    method: 'get'
  })
}

export function getStatistics() {
  return request({
    url: '/exception/statistics',
    method: 'get'
  })
}

export function save(data) {
  return request({
    url: '/exception/save',
    method: 'post',
    data
  })
}

export function update(data) {
  return request({
    url: '/exception/update',
    method: 'put',
    data
  })
}

export function handle(id, data) {
  return request({
    url: '/exception/handle/' + id,
    method: 'put',
    data
  })
}

export function resolve(id) {
  return request({
    url: '/exception/resolve/' + id,
    method: 'put'
  })
}

export function remove(id) {
  return request({
    url: '/exception/delete/' + id,
    method: 'delete'
  })
}
