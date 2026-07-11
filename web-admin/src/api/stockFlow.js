import request from './request'

export function getStockFlowList(params) {
  return request({
    url: '/stock-flow/list',
    method: 'get',
    params
  })
}

export function getStockFlowDetail(id) {
  return request({
    url: '/stock-flow/' + id,
    method: 'get'
  })
}
