import request from './request'

export function getCustomerList(params) {
  return request({
    url: '/customer/list',
    method: 'get',
    params
  })
}

export function getCustomerDetail(id) {
  return request({
    url: '/customer/' + id,
    method: 'get'
  })
}

export function createCustomer(data) {
  return request({
    url: '/customer',
    method: 'post',
    data
  })
}

export function updateCustomer(data) {
  return request({
    url: '/customer',
    method: 'put',
    data
  })
}

export function deleteCustomer(id) {
  return request({
    url: '/customer/' + id,
    method: 'delete'
  })
}
