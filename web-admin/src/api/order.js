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
    url: '/order/save',
    method: 'post',
    data
  })
}

export function updateOrder(data) {
  return request({
    url: '/order/update',
    method: 'put',
    data
  })
}

export function deleteOrder(id) {
  return request({
    url: '/order/delete/' + id,
    method: 'delete'
  })
}

export function approveOrder(id) {
  return request({
    url: '/order/audit/' + id,
    method: 'put'
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
