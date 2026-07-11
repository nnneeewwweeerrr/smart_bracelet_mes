import request from './request'

export function getOrderList(params) {
  return request({
    url: '/order/list',
    method: 'get',
    params
  })
}

export function getOrderDetail(id) {
  return request({
    url: '/order/' + id,
    method: 'get'
  })
}

export function createOrder(data) {
  return request({
    url: '/order',
    method: 'post',
    data
  })
}

export function updateOrder(data) {
  return request({
    url: '/order',
    method: 'put',
    data
  })
}

export function deleteOrder(id) {
  return request({
    url: '/order/' + id,
    method: 'delete'
  })
}

export function approveOrder(id) {
  return request({
    url: '/order/' + id + '/approve',
    method: 'post'
  })
}

export function getCustomerList() {
  return request({
    url: '/customer/list',
    method: 'get'
  })
}

export function getProductList() {
  return request({
    url: '/product/list',
    method: 'get'
  })
}
