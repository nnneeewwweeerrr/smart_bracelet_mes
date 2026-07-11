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
    url: '/stock-in',
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
