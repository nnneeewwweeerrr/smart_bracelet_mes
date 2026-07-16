import request from './request'

export function getList(params) {
  return request({
    url: '/stock-out/list',
    method: 'get',
    params
  })
}

export function getById(id) {
  return request({
    url: '/stock-out/' + id,
    method: 'get'
  })
}

export function getStatistics() {
  return request({
    url: '/stock-out/statistics',
    method: 'get'
  })
}

export function save(data) {
  return request({
    url: '/stock-out/save',
    method: 'post',
    data
  })
}

export function update(data) {
  return request({
    url: '/stock-out/update',
    method: 'put',
    data
  })
}

export function remove(id) {
  return request({
    url: '/stock-out/delete/' + id,
    method: 'delete'
  })
}
