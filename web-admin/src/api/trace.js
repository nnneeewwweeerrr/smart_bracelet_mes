import request from './request'

export function traceOrder(orderNo) {
  return request({
    url: '/trace/order',
    method: 'get',
    params: { orderNo }
  })
}

export function getOrderList(keyword) {
  return request({
    url: '/trace/order-list',
    method: 'get',
    params: { keyword }
  })
}
