import request from './request'

export function getPurchaseList(params) {
  return request({
    url: '/purchase/list',
    method: 'get',
    params
  })
}

export function getPurchaseDetail(id) {
  return request({
    url: '/purchase/' + id,
    method: 'get'
  })
}

export function createPurchase(data) {
  return request({
    url: '/purchase/save',
    method: 'post',
    data
  })
}

export function updatePurchase(data) {
  return request({
    url: '/purchase/update',
    method: 'put',
    data
  })
}

export function deletePurchase(id) {
  return request({
    url: '/purchase/delete/' + id,
    method: 'delete'
  })
}

export function approvePurchase(id) {
  return request({
    url: '/purchase/audit/' + id,
    method: 'put'
  })
}
