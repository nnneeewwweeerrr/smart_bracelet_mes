import request from './request'

export function getProcessList(params) {
  return request({
    url: '/process/list',
    method: 'get',
    params
  })
}

export function getProcessDetail(id) {
  return request({
    url: '/process/' + id,
    method: 'get'
  })
}

export function createProcess(data) {
  return request({
    url: '/process/save',
    method: 'post',
    data
  })
}

export function updateProcess(data) {
  return request({
    url: '/process/update',
    method: 'put',
    data
  })
}

export function deleteProcess(id) {
  return request({
    url: '/process/delete/' + id,
    method: 'delete'
  })
}
