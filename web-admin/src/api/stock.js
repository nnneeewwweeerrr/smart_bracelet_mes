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

export function saveStock(data) {
  return request({
    url: '/stock/save',
    method: 'post',
    data
  })
}

export function updateStock(data) {
  return request({
    url: '/stock/update',
    method: 'put',
    data
  })
}

export function deleteStock(id) {
  return request({
    url: '/stock/delete/' + id,
    method: 'delete'
  })
}
