import request from './request'

export function getList(params) {
  return request({
    url: '/stock-in/list',
    method: 'get',
    params
  })
}

export function getById(id) {
  return request({
    url: '/stock-in/' + id,
    method: 'get'
  })
}

export function getStatistics() {
  return request({
    url: '/stock-in/statistics',
    method: 'get'
  })
}

export function save(data) {
  return request({
    url: '/stock-in/save',
    method: 'post',
    data
  })
}

export function update(data) {
  return request({
    url: '/stock-in/update',
    method: 'put',
    data
  })
}

export function remove(id) {
  return request({
    url: '/stock-in/delete/' + id,
    method: 'delete'
  })
}
