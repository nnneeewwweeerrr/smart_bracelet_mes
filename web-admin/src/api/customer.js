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
    url: `/customer/${id}`,
    method: 'get'
  })
}

export function getCustomerAll() {
  return request({
    url: '/customer/all',
    method: 'get'
  })
}

export function createCustomer(data) {
  return request({
    url: '/customer/save',
    method: 'post',
    data
  })
}

export function updateCustomer(data) {
  return request({
    url: '/customer/update',
    method: 'put',
    data
  })
}

export function deleteCustomer(id) {
  return request({
    url: `/customer/delete/${id}`,
    method: 'delete'
  })
}