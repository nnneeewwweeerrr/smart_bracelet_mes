import request from './request'

export function getExceptionList(params) {
  return request({
    url: '/exception/list',
    method: 'get',
    params
  })
}

export function getExceptionDetail(id) {
  return request({
    url: '/exception/' + id,
    method: 'get'
  })
}

export function createException(data) {
  return request({
    url: '/exception/save',
    method: 'post',
    data
  })
}

export function handleException(id, data) {
  return request({
    url: '/exception/handle/' + id,
    method: 'put',
    data
  })
}

export function closeException(id) {
  return request({
    url: '/exception/resolve/' + id,
    method: 'put'
  })
}
