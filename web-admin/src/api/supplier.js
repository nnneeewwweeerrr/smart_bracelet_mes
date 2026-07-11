import request from './request'

export function getSupplierList(params) {
  return request({
    url: '/supplier/list',
    method: 'get',
    params
  })
}

export function getSupplierDetail(id) {
  return request({
    url: '/supplier/' + id,
    method: 'get'
  })
}

export function createSupplier(data) {
  return request({
    url: '/supplier',
    method: 'post',
    data
  })
}

export function updateSupplier(data) {
  return request({
    url: '/supplier',
    method: 'put',
    data
  })
}

export function deleteSupplier(id) {
  return request({
    url: '/supplier/' + id,
    method: 'delete'
  })
}
