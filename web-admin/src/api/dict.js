import request from './request'

export function getList() {
  return request({
    url: '/dict/list',
    method: 'get'
  })
}

export function getByType(dictType) {
  return request({
    url: `/dict/type/${dictType}`,
    method: 'get'
  })
}

export function save(data) {
  return request({
    url: '/dict/save',
    method: 'post',
    data
  })
}

export function update(data) {
  return request({
    url: '/dict/update',
    method: 'put',
    data
  })
}

export function remove(id) {
  return request({
    url: `/dict/delete/${id}`,
    method: 'delete'
  })
}