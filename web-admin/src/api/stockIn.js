import request from './request'

export function getStockInList(params) {
  return request({
    url: '/stock-in/list',
    method: 'get',
    params
  })
}

export function getStockInDetail(id) {
  return request({
    url: '/stock-in/' + id,
    method: 'get'
  })
}

export function createStockIn(data) {
  return request({
    url: '/stock-in/save',
    method: 'post',
    data
  })
}

export function approveStockIn(id) {
  return request({
    url: '/stock-in/' + id + '/approve',
    method: 'post'
  })
}

export function updateStockIn(data) {
  return request({
    url: '/stock-in/update',
    method: 'put',
    data
  })
}

export function deleteStockIn(id) {
  return request({
    url: '/stock-in/delete/' + id,
    method: 'delete'
  })
}
