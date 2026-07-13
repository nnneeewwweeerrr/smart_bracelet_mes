import request from './request'

export function getStockOutList(params) {
  return request({
    url: '/stock-out/list',
    method: 'get',
    params
  })
}

export function getStockOutDetail(id) {
  return request({
    url: '/stock-out/' + id,
    method: 'get'
  })
}

export function createStockOut(data) {
  return request({
    url: '/stock-out/save',
    method: 'post',
    data
  })
}

export function approveStockOut(id) {
  return request({
    url: '/stock-out/' + id + '/approve',
    method: 'post'
  })
}
