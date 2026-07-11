import request from './request'

export function getStockList(params) {
  return request({
    url: '/stock/list',
    method: 'get',
    params
  })
}

export function getStockDetail(id) {
  return request({
    url: '/stock/' + id,
    method: 'get'
  })
}

export function getStockWarning() {
  return request({
    url: '/mini/stock/warning',
    method: 'get'
  })
}

export function stockTakeStock(data) {
  return request({
    url: '/stock/take-stock',
    method: 'post',
    data
  })
}
